package io.github.batmancpu.watchprobe

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.ArrayDeque

data class Advertisement(
    val serviceUuids: List<String>,
    val manufacturerData: Map<String, String>,
    val serviceData: Map<String, String>,
    val flags: Int?,
    val rawHex: String?,
    val legacy: Boolean,
    val connectable: Boolean
)
data class SeenDevice(
    val device: BluetoothDevice,
    val name: String,
    val address: String,
    val rssi: Int,
    val samples: Int,
    val advertisement: Advertisement
)
data class DescriptorEvidence(val uuid: String, val permissions: Int)
data class CharacterEvidence(
    val uuid: String,
    val instanceId: Int,
    val properties: Int,
    val permissions: Int,
    val descriptors: List<DescriptorEvidence>,
    val readStatus: String,
    val rawHex: String? = null,
    val decodedText: String? = null
)
data class ServiceEvidence(val uuid: String, val type: String, val characteristics: List<CharacterEvidence>)
data class GattSnapshot(val services: List<ServiceEvidence>, val status: String, val finished: Boolean)

fun ByteArray.asHex(): String = joinToString("") { "%02X".format(it.toInt() and 255) }

fun propertyNames(flags: Int): String {
    val names = listOf(
        BluetoothGattCharacteristic.PROPERTY_READ to "READ",
        BluetoothGattCharacteristic.PROPERTY_WRITE to "WRITE",
        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE to "WRITE_NO_RESPONSE",
        BluetoothGattCharacteristic.PROPERTY_NOTIFY to "NOTIFY",
        BluetoothGattCharacteristic.PROPERTY_INDICATE to "INDICATE",
        BluetoothGattCharacteristic.PROPERTY_BROADCAST to "BROADCAST",
        BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE to "SIGNED_WRITE",
        BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS to "EXTENDED"
    ).filter { (flag, _) -> flags and flag != 0 }.map { it.second }
    return names.joinToString(", ").ifEmpty { "NONE" }
}

private fun decodeKnown(uuid: String, bytes: ByteArray): String? {
    val short = uuid.lowercase().removePrefix("0000").take(4)
    return when (short) {
        "2a19" -> bytes.firstOrNull()?.let { (it.toInt() and 255).toString() + "%" }
        "2a00", "2a24", "2a26", "2a27", "2a28", "2a29" ->
            bytes.toString(Charsets.UTF_8).trimEnd('\u0000').take(240)
        else -> null
    }
}

@SuppressLint("MissingPermission") // Permission is checked in MainActivity before calling start/connect.
class BleSurvey(
    private val context: Context,
    private val onDevices: (List<SeenDevice>) -> Unit,
    private val onGatt: (GattSnapshot) -> Unit,
    private val onStatus: (String) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private val adapter get() = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private var leScanner: BluetoothLeScanner? = null
    private var gatt: BluetoothGatt? = null
    private val devices = linkedMapOf<String, SeenDevice>()
    private var services = emptyList<ServiceEvidence>()
    private val characteristicPositions = mutableMapOf<BluetoothGattCharacteristic, Pair<Int, Int>>()
    private val reads = ArrayDeque<BluetoothGattCharacteristic>()
    private var currentRead: BluetoothGattCharacteristic? = null
    private var readTimeout: Runnable? = null
    private var session = 0
    private var connecting = false
    private var finished = false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            handler.post {
                val record = result.scanRecord
                val manufacturer = mutableMapOf<String, String>()
                record?.manufacturerSpecificData?.let { list ->
                    for (i in 0 until list.size()) {
                        val id = list.keyAt(i)
                        manufacturer["0x" + id.toString(16).uppercase().padStart(4, '0')] =
                            list.valueAt(i).asHex()
                    }
                }
                val ad = Advertisement(
                    serviceUuids = record?.serviceUuids?.map { it.uuid.toString() } ?: emptyList(),
                    manufacturerData = manufacturer,
                    serviceData = record?.serviceData?.entries?.associate { it.key.uuid.toString() to it.value.asHex() } ?: emptyMap(),
                    flags = record?.advertiseFlags,
                    rawHex = record?.bytes?.asHex(),
                    legacy = result.isLegacy,
                    connectable = result.isConnectable
                )
                val address = result.device.address ?: return@post
                val previous = devices[address]
                val name = record?.deviceName ?: previous?.name ?: "Unnamed BLE device"
                devices[address] = SeenDevice(result.device, name, address, result.rssi, (previous?.samples ?: 0) + 1, ad)
                onDevices(devices.values.sortedWith(compareByDescending<SeenDevice> {
                    it.name.contains("T700", ignoreCase = true)
                }.thenByDescending { it.rssi }))
            }
        }
        override fun onScanFailed(errorCode: Int) {
            handler.post { onStatus("Scan failed: Android error " + errorCode) }
        }
    }

    fun scan() {
        stopScan()
        devices.clear()
        onDevices(emptyList())
        if (adapter?.isEnabled != true) { onStatus("Enable Bluetooth first."); return }
        try {
            val scanner = adapter.bluetoothLeScanner ?: run {
                onStatus("BLE scanner unavailable"); return
            }
            leScanner = scanner
            scanner.startScan(null, ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), scanCallback)
            onStatus("Scanning for 15 seconds; no connection or pairing")
            handler.postDelayed({ if (leScanner === scanner) stopScan() }, 15_000)
        } catch (ex: Exception) {
            leScanner = null
            onStatus("Scan unavailable: " + ex.javaClass.simpleName)
        }
    }

    fun stopScan() {
        leScanner?.let { try { it.stopScan(scanCallback) } catch (_: Exception) {} }
        leScanner = null
    }

    fun connect(device: SeenDevice) {
        stopScan()
        close()
        session++
        val mySession = session
        finished = false
        connecting = true
        services = emptyList()
        onGatt(GattSnapshot(emptyList(), "Connecting", false))
        try {
            onStatus("Connecting to " + device.name + "; discovery and unencrypted READ only")
            gatt = device.device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
            handler.postDelayed({
                if (mySession == session && connecting && !finished) {
                    onStatus("Connection timed out; no reads performed")
                    close()
                }
            }, 20_000)
        } catch (ex: Exception) {
            onStatus("Connection error: " + ex.javaClass.simpleName)
            close()
        }
    }

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            handler.post {
                if (gatt !== g) { g.close(); return@post }
                if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                    connecting = false
                    onStatus("Connected. Discovering exposed services (no writes)")
                    if (!g.discoverServices()) {
                        onStatus("Service discovery could not start")
                        finish("Discovery failed")
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED || status != BluetoothGatt.GATT_SUCCESS) {
                    val info = "Disconnected (GATT status " + status + ")"
                    onStatus(info)
                    finish(info)
                    g.close()
                    gatt = null
                }
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            handler.post {
                if (gatt !== g) return@post
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    finish("Service discovery failed: " + status)
                    return@post
                }
                characteristicPositions.clear()
                reads.clear()
                services = g.services.mapIndexed { si, service ->
                    ServiceEvidence(
                        service.uuid.toString(),
                        if (service.type == BluetoothGattService.SERVICE_TYPE_PRIMARY) "PRIMARY" else "SECONDARY",
                        service.characteristics.mapIndexed { ci, ch ->
                            characteristicPositions[ch] = si to ci
                            val encrypted = ch.permissions and (
                                BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED or
                                BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM
                            ) != 0
                            val readable = ch.properties and BluetoothGattCharacteristic.PROPERTY_READ != 0
                            if (readable && !encrypted) reads.addLast(ch)
                            CharacterEvidence(
                                ch.uuid.toString(),
                                ch.instanceId,
                                ch.properties,
                                ch.permissions,
                                ch.descriptors.map { DescriptorEvidence(it.uuid.toString(), it.permissions) },
                                when {
                                    !readable -> "NOT_READABLE"
                                    encrypted -> "SKIPPED_REQUIRES_ENCRYPTION"
                                    else -> "QUEUED"
                                }
                            )
                        }
                    )
                }
                publish("Discovered " + services.size + " services; " + reads.size + " unencrypted readable characteristics")
                nextRead()
            }
        }

        override fun onCharacteristicRead(g: BluetoothGatt, ch: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
            handler.post { handleRead(g, ch, value, status) }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(g: BluetoothGatt, ch: BluetoothGattCharacteristic, status: Int) {
            if (android.os.Build.VERSION.SDK_INT < 33) {
                handler.post { handleRead(g, ch, ch.value ?: ByteArray(0), status) }
            }
        }
    }

    private fun handleRead(g: BluetoothGatt, ch: BluetoothGattCharacteristic, bytes: ByteArray, status: Int) {
        if (gatt !== g || currentRead !== ch) return
        readTimeout?.let(handler::removeCallbacks)
        readTimeout = null
        updateRecord(ch, if (status == BluetoothGatt.GATT_SUCCESS) "READ_OK" else "READ_ERROR_" + status,
            if (status == BluetoothGatt.GATT_SUCCESS) bytes.asHex() else null,
            if (status == BluetoothGatt.GATT_SUCCESS) decodeKnown(ch.uuid.toString(), bytes) else null)
        currentRead = null
        nextRead()
    }

    private fun nextRead() {
        if (currentRead != null || finished) return
        val next = if (reads.isEmpty()) null else reads.removeFirst()
        if (next == null) {
            finish("Read-only survey complete")
            return
        }
        currentRead = next
        try {
            if (gatt?.readCharacteristic(next) != true) {
                updateRecord(next, "READ_START_FAILED", null, null)
                currentRead = null
                handler.post { nextRead() }
            } else {
                val timeout = Runnable {
                    if (currentRead === next) {
                        updateRecord(next, "READ_TIMEOUT", null, null)
                        currentRead = null
                        nextRead()
                    }
                }
                readTimeout = timeout
                handler.postDelayed(timeout, 5_000)
            }
        } catch (ex: Exception) {
            updateRecord(next, "READ_EXCEPTION_" + ex.javaClass.simpleName, null, null)
            currentRead = null
            handler.post { nextRead() }
        }
    }

    private fun updateRecord(ch: BluetoothGattCharacteristic, state: String, hex: String?, text: String?) {
        val (si, ci) = characteristicPositions[ch] ?: return
        val updatedService = services[si]
        val chars = updatedService.characteristics.toMutableList()
        chars[ci] = chars[ci].copy(readStatus = state, rawHex = hex, decodedText = text)
        val updatedServices = services.toMutableList()
        updatedServices[si] = updatedService.copy(characteristics = chars)
        services = updatedServices
        publish("Read " + state + ": " + ch.uuid)
    }

    private fun publish(status: String) {
        onGatt(GattSnapshot(services, status, finished))
        onStatus(status)
    }

    private fun finish(status: String) {
        if (finished) return
        finished = true
        connecting = false
        readTimeout?.let(handler::removeCallbacks)
        readTimeout = null
        currentRead = null
        reads.clear()
        publish(status)
    }

    fun close() {
        stopScan()
        session++
        readTimeout?.let(handler::removeCallbacks)
        readTimeout = null
        currentRead = null
        reads.clear()
        val old = gatt
        gatt = null
        connecting = false
        if (old != null) {
            try { old.disconnect() } catch (_: Exception) {}
            try { old.close() } catch (_: Exception) {}
        }
    }
}
