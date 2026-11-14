package io.github.batmancpu.watchcompanion

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import java.util.ArrayDeque
import java.util.UUID

data class WatchTarget(
    val device: BluetoothDevice,
    val name: String,
    val rssi: Int,
    val sightings: Int
)

data class WatchEvent(val timestampMs: Long, val channel: String, val bytes: ByteArray)

data class WatchState(
    val connection: String = "Disconnected",
    val battery: Int? = null,
    val info: Map<String, String> = emptyMap(),
    val serviceCount: Int = 0,
    val listening: Boolean = false,
    val packetCounts: Map<String, Int> = emptyMap(),
    val totalEvents: Int = 0
)

/**
 * Single-device, foreground-only BLE controller.
 * This client never writes application data to the watch. Only optional, user-approved
 * CCCD writes (01 00) to known NOTIFY paths can occur, to observe watch-origin events.
 */
@SuppressLint("MissingPermission")
class WatchBleClient(
    private val context: Context,
    private val onTargets: (List<WatchTarget>) -> Unit,
    private val onState: (WatchState) -> Unit,
    private val onLog: (String) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private val adapter: BluetoothAdapter?
        get() = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private var scanner: BluetoothLeScanner? = null
    private val targets = linkedMapOf<String, WatchTarget>()
    private var gatt: BluetoothGatt? = null
    private var serial = 0
    private var state = WatchState()

    private data class ReadOp(val label: String, val ch: BluetoothGattCharacteristic)
    private data class SubscribeOp(val ch: BluetoothGattCharacteristic, val descriptor: BluetoothGattDescriptor)
    private val reads = ArrayDeque<ReadOp>()
    private var activeRead: ReadOp? = null
    private val subscriptions = ArrayDeque<SubscribeOp>()
    private var activeSubscription: SubscribeOp? = null
    private var operationTimeout: Runnable? = null
    private val observed = mutableListOf<WatchEvent>()
    private val counts = linkedMapOf<String, Int>()

    fun snapshot(): WatchState = state
    fun events(): List<WatchEvent> = observed.toList()

    private fun publish(newState: WatchState) {
        state = newState
        onState(newState)
    }

    private fun log(text: String) = onLog(text)

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            handler.post {
                val name = result.scanRecord?.deviceName
                    ?: try { result.device.name } catch (_: Exception) { null }
                    ?: ""
                // Avoid collecting or displaying unrelated nearby BLE device identities.
                if (!name.contains("T700", ignoreCase = true)) return@post
                val address = result.device.address
                val previous = targets[address]
                targets[address] = WatchTarget(
                    result.device, name, result.rssi, (previous?.sightings ?: 0) + 1
                )
                onTargets(targets.values.sortedByDescending { it.rssi })
            }
        }
        override fun onScanFailed(errorCode: Int) {
            handler.post { log("Bluetooth scan failed: code " + errorCode) }
        }
    }

    fun scan() {
        if (scanner != null) {
            log("A scan is already in progress; wait for results.")
            return
        }
        targets.clear()
        onTargets(emptyList())
        if (adapter?.isEnabled != true) {
            log("Enable phone Bluetooth and scan again.")
            return
        }
        try {
            val s = adapter?.bluetoothLeScanner ?: error("No Bluetooth LE scanner")
            scanner = s
            s.startScan(null,
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(),
                scanCallback
            )
            log("Scanning for T700 nearby (12 seconds).")
            handler.postDelayed({ if (scanner === s) stopScan() }, 12_000)
        } catch (e: Exception) {
            scanner = null
            log("Scan error: " + e.javaClass.simpleName)
        }
    }

    fun stopScan() {
        scanner?.let { try { it.stopScan(scanCallback) } catch (_: Exception) {} }
        scanner = null
    }

    fun connect(target: WatchTarget) {
        if (gatt != null && state.connection == "Connected") {
            log("Already connected. Disconnect before selecting a different device.")
            return
        }
        stopScan()
        disconnect()
        val thisSerial = serial
        publish(WatchState(connection = "Connecting to " + target.name))
        try {
            val connection = target.device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
            if (connection == null) {
                publish(WatchState(connection = "Connection failed to start"))
                return
            }
            gatt = connection
            handler.postDelayed({
                if (serial == thisSerial && state.connection.startsWith("Connecting")) {
                    log("Connection timed out. Disconnecting.")
                    disconnect()
                }
            }, 20_000)
        } catch (e: Exception) {
            log("Connect error: " + e.javaClass.simpleName)
            disconnect()
        }
    }

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            handler.post {
                if (gatt !== g) {
                    try { g.close() } catch (_: Exception) {}
                    return@post
                }
                if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                    publish(state.copy(connection = "Discovering services"))
                    log("Connected to the selected watch.")
                    try {
                        if (!g.discoverServices()) log("Service discovery failed to start.")
                    } catch (e: Exception) {
                        log("Service discovery error: " + e.javaClass.simpleName)
                    }
                } else if (status != BluetoothGatt.GATT_SUCCESS || newState == BluetoothProfile.STATE_DISCONNECTED) {
                    log("Watch disconnected (GATT status " + status + ").")
                    disconnect()
                }
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            handler.post {
                if (gatt !== g) return@post
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    publish(state.copy(connection = "Connected", serviceCount = g.services.size))
                    log("Discovered " + g.services.size + " services. Reading battery and device information.")
                    refresh()
                } else {
                    log("Service discovery failed: GATT " + status)
                }
            }
        }

        override fun onCharacteristicRead(
            g: BluetoothGatt, ch: BluetoothGattCharacteristic, value: ByteArray, status: Int
        ) {
            handler.post { handleRead(g, ch, value, status) }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(g: BluetoothGatt, ch: BluetoothGattCharacteristic, status: Int) {
            if (Build.VERSION.SDK_INT < 33) {
                handler.post { handleRead(g, ch, ch.value ?: byteArrayOf(), status) }
            }
        }

        override fun onDescriptorWrite(g: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            handler.post {
                if (gatt !== g || activeSubscription?.descriptor !== descriptor) return@post
                clearTimeout()
                val channel = descriptor.characteristic.uuid.toString()
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    publish(state.copy(listening = true))
                    log("Observation enabled: " + channel)
                } else {
                    log("Observation refused: " + channel + " (GATT " + status + ")")
                }
                activeSubscription = null
                subscribeNext()
            }
        }

        override fun onCharacteristicChanged(
            g: BluetoothGatt, ch: BluetoothGattCharacteristic, value: ByteArray
        ) {
            handler.post { record(g, ch, value) }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(g: BluetoothGatt, ch: BluetoothGattCharacteristic) {
            if (Build.VERSION.SDK_INT < 33) {
                handler.post { record(g, ch, ch.value ?: byteArrayOf()) }
            }
        }
    }

    private fun characteristic(serviceUuid: String, charUuid: String): BluetoothGattCharacteristic? {
        val service = gatt?.getService(UUID.fromString(serviceUuid)) ?: return null
        return service.getCharacteristic(UUID.fromString(charUuid))
    }

    fun refresh() {
        val g = gatt ?: run { log("Connect to the watch first."); return }
        if (state.connection != "Connected") {
            log("Wait for service discovery.")
            return
        }
        if (activeRead != null || reads.isNotEmpty() || activeSubscription != null || subscriptions.isNotEmpty()) {
            log("Bluetooth operation in progress; retry shortly.")
            return
        }
        // READ-only standard Bluetooth SIG characteristics observed on this watch.
        val targetsToRead = listOf(
            Triple(ProtocolSafety.BATTERY_SERVICE, ProtocolSafety.BATTERY_LEVEL, "Battery"),
            Triple(ProtocolSafety.INFO_SERVICE, "00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer"),
            Triple(ProtocolSafety.INFO_SERVICE, "00002a26-0000-1000-8000-00805f9b34fb", "Firmware"),
            Triple(ProtocolSafety.INFO_SERVICE, "00002a27-0000-1000-8000-00805f9b34fb", "Hardware"),
            Triple(ProtocolSafety.INFO_SERVICE, "00002a24-0000-1000-8000-00805f9b34fb", "Model")
        )
        for ((service, char, label) in targetsToRead) {
            val candidate = characteristic(service, char) ?: continue
            if (candidate.properties and BluetoothGattCharacteristic.PROPERTY_READ == 0) continue
            val prohibited = BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED or
                BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM
            if (candidate.permissions and prohibited != 0) continue
            reads.addLast(ReadOp(label, candidate))
        }
        if (reads.isEmpty()) log("No standard readable attributes found.")
        else readNext(g)
    }

    private fun readNext(g: BluetoothGatt) {
        if (gatt !== g || activeRead != null) return
        val op = if (reads.isEmpty()) null else reads.removeFirst()
        if (op == null) {
            log("Standard watch information refreshed.")
            return
        }
        activeRead = op
        try {
            if (!g.readCharacteristic(op.ch)) {
                log("Read not started: " + op.label)
                activeRead = null
                handler.post { readNext(g) }
            } else {
                scheduleTimeout {
                    if (activeRead === op) {
                        log("Read timed out: " + op.label)
                        activeRead = null
                        readNext(g)
                    }
                }
            }
        } catch (e: Exception) {
            log("Read exception (" + op.label + "): " + e.javaClass.simpleName)
            activeRead = null
            handler.post { readNext(g) }
        }
    }

    private fun handleRead(g: BluetoothGatt, ch: BluetoothGattCharacteristic, bytes: ByteArray, status: Int) {
        val op = activeRead ?: return
        if (gatt !== g || op.ch !== ch) return
        clearTimeout()
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (op.label == "Battery") {
                val percent = ProtocolSafety.batteryPercent(bytes)
                if (percent != null) publish(state.copy(battery = percent))
                else log("Invalid battery value; not displayed.")
            } else {
                val value = bytes.toString(Charsets.UTF_8).trimEnd('\u0000').take(100)
                // Serial number is intentionally never read or exported.
                publish(state.copy(info = state.info + (op.label to value)))
            }
        } else {
            log("Read failed: " + op.label + " (GATT " + status + ")")
        }
        activeRead = null
        readNext(g)
    }

    /**
     * Requires an explicit UI tap after connection. Bluetooth CCCD descriptor is
     * standardized for temporary notification configuration (01 00); this does not
     * write watch settings, launch firmware modes, or transmit application commands.
     */
    fun observeWatchEvents() {
        val g = gatt ?: run { log("Connect to the watch first."); return }
        if (state.connection != "Connected") {
            log("Wait for service discovery.")
            return
        }
        if (state.listening) {
            log("Watch event observation is already active. Disconnect to stop.")
            return
        }
        if (activeRead != null || reads.isNotEmpty() || activeSubscription != null || subscriptions.isNotEmpty()) {
            log("Bluetooth operation in progress; retry shortly.")
            return
        }
        val candidates = listOf(
            ProtocolSafety.BATTERY_SERVICE to ProtocolSafety.BATTERY_LEVEL,
            ProtocolSafety.VENDOR_UART_SERVICE to ProtocolSafety.VENDOR_UART_NOTIFY,
            ProtocolSafety.OTHER_VENDOR_SERVICE to ProtocolSafety.OTHER_VENDOR_NOTIFY
        )
        for ((serviceId, charId) in candidates) {
            if (!ProtocolSafety.canObserve(serviceId, charId)) continue
            val ch = characteristic(serviceId, charId) ?: continue
            if (ch.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY == 0) continue
            val descriptor = ch.getDescriptor(UUID.fromString(ProtocolSafety.CCCD)) ?: continue
            subscriptions.addLast(SubscribeOp(ch, descriptor))
        }
        if (subscriptions.isEmpty()) log("This connection exposes no approved watch notification channels.")
        else {
            log("Enabling temporary CCCD subscriptions on " + subscriptions.size + " known watch channels.")
            subscribeNext()
        }
    }

    private fun subscribeNext() {
        val g = gatt ?: return
        if (activeSubscription != null) return
        val op = if (subscriptions.isEmpty()) null else subscriptions.removeFirst()
        if (op == null) {
            log("Observation setup complete. No app-level vendor commands were sent.")
            return
        }
        activeSubscription = op
        try {
            val enabled = g.setCharacteristicNotification(op.ch, true)
            if (!enabled) {
                log("Android could not register local notification listener: " + op.ch.uuid)
                activeSubscription = null
                handler.post { subscribeNext() }
                return
            }
            val started = if (Build.VERSION.SDK_INT >= 33) {
                g.writeDescriptor(op.descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) ==
                    BluetoothStatusCodes.SUCCESS
            } else {
                @Suppress("DEPRECATION")
                op.descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                g.writeDescriptor(op.descriptor)
            }
            if (!started) {
                log("CCCD subscription could not start: " + op.ch.uuid)
                activeSubscription = null
                handler.post { subscribeNext() }
            } else {
                scheduleTimeout {
                    if (activeSubscription === op) {
                        log("CCCD subscription timed out: " + op.ch.uuid)
                        activeSubscription = null
                        subscribeNext()
                    }
                }
            }
        } catch (e: Exception) {
            log("CCCD subscription exception: " + e.javaClass.simpleName)
            activeSubscription = null
            handler.post { subscribeNext() }
        }
    }

    private fun record(g: BluetoothGatt, ch: BluetoothGattCharacteristic, value: ByteArray) {
        if (gatt !== g || !state.listening) return
        val serviceUuid = ch.service?.uuid?.toString() ?: return
        val uuid = ch.uuid.toString()
        if (!ProtocolSafety.canObserve(serviceUuid, uuid)) return
        counts[uuid] = (counts[uuid] ?: 0) + 1
        if (observed.size >= 256) observed.removeAt(0)
        // Exact watch-origin bytes remain local in RAM. Raw export requires a separate UI toggle.
        observed.add(WatchEvent(System.currentTimeMillis(), uuid, value.copyOf(minOf(value.size, 512))))
        val count = state.totalEvents + 1
        var battery = state.battery
        if (serviceUuid.equals(ProtocolSafety.BATTERY_SERVICE, ignoreCase = true)) {
            battery = ProtocolSafety.batteryPercent(value) ?: battery
        }
        publish(state.copy(totalEvents = count, packetCounts = counts.toMap(), battery = battery))
    }

    private fun scheduleTimeout(action: () -> Unit) {
        clearTimeout()
        val r = Runnable { operationTimeout = null; action() }
        operationTimeout = r
        handler.postDelayed(r, 5_000)
    }

    private fun clearTimeout() {
        operationTimeout?.let(handler::removeCallbacks)
        operationTimeout = null
    }

    fun disconnect() {
        stopScan()
        serial++
        clearTimeout()
        reads.clear()
        subscriptions.clear()
        activeRead = null
        activeSubscription = null
        val old = gatt
        gatt = null
        if (old != null) {
            try { old.disconnect() } catch (_: Exception) {}
            try { old.close() } catch (_: Exception) {}
        }
        publish(state.copy(connection = "Disconnected", listening = false))
    }

    fun close() = disconnect()
}
