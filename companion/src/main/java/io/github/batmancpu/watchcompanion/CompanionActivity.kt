package io.github.batmancpu.watchcompanion

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Functional companion foundation for the observed T700 Pro Max BLE profile.
 * No vendor WRITE commands, OTA operations, bonding, or firmware changes.
 */
class CompanionActivity : ComponentActivity() {
    private lateinit var client: WatchBleClient
    private var targets by mutableStateOf<List<WatchTarget>>(emptyList())
    private var watch by mutableStateOf(WatchState())
    private var includeWatchPayloads by mutableStateOf(false)
    private var status by mutableStateOf("Ready. Scan for your T700 Pro Max.")
    private val messages = mutableStateListOf<String>()
    private var deferredAction: (() -> Unit)? = null

    private val permissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val action = deferredAction
        deferredAction = null
        if (hasPermissions()) action?.invoke() else note("Nearby Devices permission is required.")
    }

    private val save = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri -> if (uri != null) export(uri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client = WatchBleClient(
            this,
            onTargets = { targets = it },
            onState = { watch = it },
            onLog = ::note
        )
        setContent {
            MaterialTheme {
                Surface {
                    CompanionScreen()
                }
            }
        }
    }

    private fun permissionList(): Array<String> =
        if (Build.VERSION.SDK_INT >= 31) arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        ) else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun hasPermissions() =
        permissionList().all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

    private fun withPermissions(action: () -> Unit) {
        if (hasPermissions()) action() else {
            deferredAction = action
            permissions.launch(permissionList())
        }
    }

    private fun bluetoothOn(): Boolean = try {
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter?.isEnabled == true
    } catch (_: Exception) { false }

    private fun note(line: String) {
        status = line
        messages.add(SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()) + " " + line)
        if (messages.size > 150) messages.removeAt(0)
    }

    private fun export(uri: Uri) {
        val snapshot = client.snapshot()
        val packets = client.events()
        val allowPayload = includeWatchPayloads
        val log = messages.toList()
        Thread {
            try {
                val out = contentResolver.openOutputStream(uri, "w") ?: error("Cannot open export")
                writeExport(out, snapshot, packets, log, allowPayload)
                runOnUiThread { note("Report exported. Review its contents before sharing.") }
            } catch (e: Exception) {
                runOnUiThread { note("Export failed: " + (e.message ?: e.javaClass.simpleName)) }
            }
        }.start()
    }

    private fun writeExport(
        output: OutputStream,
        snapshot: WatchState,
        packets: List<WatchEvent>,
        history: List<String>,
        includeRaw: Boolean
    ) {
        val device = JSONObject()
            .put("app", "T700 Companion 0.2.0")
            .put("capture_type", "selected-watch-only")
            .put("time_ms", System.currentTimeMillis())
            .put("connection", snapshot.connection)
            .put("battery_percent", snapshot.battery ?: JSONObject.NULL)
            .put("services_discovered", snapshot.serviceCount)
            .put("info", JSONObject(snapshot.info))
            .put("notifications_enabled", snapshot.listening)
            .put("watch_events_observed", snapshot.totalEvents)
            .put("watch_event_counts", JSONObject(snapshot.packetCounts))
            .put("private_watch_payloads_included", includeRaw)
            .put("vendor_write_commands_sent", 0)
            .put("device_address_included", false)
        val eventJson = JSONArray()
        packets.forEach { event ->
            val item = JSONObject()
                .put("timestamp_ms", event.timestampMs)
                .put("characteristic_uuid", event.channel)
                .put("byte_count", event.bytes.size)
            if (includeRaw) item.put("watch_payload_hex", ProtocolSafety.toHex(event.bytes))
            eventJson.put(item)
        }
        ZipOutputStream(output).use { zip ->
            fun add(name: String, data: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(data.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            add("README.txt",
                "Only selected T700 watch notifications are observed. No other devices' packets are captured.\n" +
                "The optional private watch payloads toggle may expose sensitive watch-origin data.\n" +
                "No unverified vendor commands, firmware updates, resets or watch-face transfers were performed.\n")
            add("companion-state.json", device.toString(2))
            add("watch-notification-events.json", eventJson.toString(2))
            add("session-log.json", JSONArray(history).toString(2))
        }
    }

    override fun onDestroy() {
        if (::client.isInitialized) client.close()
        super.onDestroy()
    }

    @Composable
    private fun CompanionScreen() {
        val connected = watch.connection == "Connected"
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text("T700 COMPANION", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold)
            Text("Functional BLE foundation · 0.2.0")
            Text("Works now: discovery, connection, standard battery and device information, " +
                "and optional passive observation of watch-origin messages.")
            Section("1. Connect to the watch") {
                Text(if (bluetoothOn()) "Bluetooth enabled" else "Bluetooth is OFF — enable it in Settings")
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { withPermissions { client.scan() } }
                ) { Text("Scan for T700 Pro Max") }
                if (targets.isEmpty()) Text("No T700 devices found yet.")
                targets.forEach { item ->
                    HorizontalDivider()
                    Text(item.name + " · " + item.rssi + " dBm · seen " + item.sightings + " times")
                    OutlinedButton(onClick = { withPermissions { client.connect(item) } }) {
                        Text("Connect to this watch")
                    }
                }
                Text("Connection: " + watch.connection, fontWeight = FontWeight.SemiBold)
                OutlinedButton(onClick = { client.disconnect() }) { Text("Disconnect") }
            }
            Section("2. Device status") {
                Text("Battery: " + (watch.battery?.toString()?.plus("%") ?: "Not read"),
                    style = MaterialTheme.typography.titleLarge)
                Text("Discovered GATT services: " + watch.serviceCount)
                if (watch.info.isEmpty()) Text("Device details will appear after connection.")
                watch.info.forEach { (label, value) -> Text(label + ": " + value) }
                Button(enabled = connected, onClick = { withPermissions { client.refresh() } }) {
                    Text("Refresh battery and device info")
                }
            }
            Section("3. Safe protocol observation") {
                Text("Optional experiment: Android writes only the standardized temporary BLE " +
                    "notification enable value 01 00 to CCCDs already discovered on this watch. " +
                    "It never sends guessed commands to the watch's write characteristics.")
                Text("Observable paths: battery 2A19, vendor …CCA9D/003, FFFF/FF11.")
                Button(
                    enabled = connected && !watch.listening,
                    onClick = { withPermissions { client.observeWatchEvents() } }
                ) { Text("Enable watch notifications (optional)") }
                Text("Listening: " + if (watch.listening) "Yes" else "No")
                Text("Watch-origin events received: " + watch.totalEvents)
                watch.packetCounts.forEach { (uuid, count) ->
                    Text(uuid.takeLast(13) + ": " + count + " messages")
                }
                Text("Disconnect to stop observation. No background scanning, capture of " +
                    "other Bluetooth devices, or access to the HiWatchPro app.")
            }
            Section("4. Private export") {
                Row {
                    Checkbox(checked = includeWatchPayloads,
                        onCheckedChange = { includeWatchPayloads = it })
                    Text("Include raw watch notification bytes (private)",
                        modifier = Modifier.padding(top = 12.dp))
                }
                Text("Off by default. Even watch-only payloads could contain health records, " +
                    "identifiers or message content. Exports never include Bluetooth addresses.")
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    val name = "T700-Companion-" +
                        SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date()) + ".zip"
                    save.launch(name)
                }) { Text("Export local diagnostic ZIP") }
            }
            Section("5. Advanced controls — not enabled") {
                Text("Notifications to watch, WhatsApp/call relay, fitness history, " +
                    "heart rate, alarms, find-device, time sync, and watch-face uploads " +
                    "require the exact proprietary protocol. These controls remain disabled " +
                    "until verified from the original APK or watch-specific packet evidence.")
                Text("Firmware flashing, OTA/DFU and resets are prohibited.")
            }
            Section("Session") {
                Text(status, fontWeight = FontWeight.SemiBold)
                messages.takeLast(12).forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    @Composable
    private fun Section(title: String, content: @Composable () -> Unit) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                content()
            }
        }
    }
}
