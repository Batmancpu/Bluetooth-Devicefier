package io.github.batmancpu.watchcompanion

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

data class ActiveExperiment(
    val label: String, val startedMs: Long, val watchBefore: Int
)
data class ExperimentResult(
    val label: String, val startedMs: Long, val endedMs: Long,
    val watchEvents: Int
)

/** Physical watch test suite; no unverified vendor transmit commands. */
class CompanionActivity : ComponentActivity() {
    private lateinit var client: WatchBleClient
    private var targets by mutableStateOf<List<WatchTarget>>(emptyList())
    private var watch by mutableStateOf(WatchState())
    private var includeWatchPayloads by mutableStateOf(false)
    private var status by mutableStateOf("Ready. No vendor commands have been transmitted.")
    private val messages = mutableStateListOf<String>()
    private var deferredAction: (() -> Unit)? = null
    private var activeExperiment by mutableStateOf<ActiveExperiment?>(null)
    private val results = mutableStateListOf<ExperimentResult>()
    private var faceUri by mutableStateOf<Uri?>(null)
    private var faceSource by mutableStateOf<FaceSource?>(null)
    private var faceWidth by mutableStateOf("240")
    private var faceHeight by mutableStateOf("280")
    private var phoneClock by mutableStateOf("Not checked yet")

    private val permissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val action = deferredAction
        deferredAction = null
        if (hasPermissions()) action?.invoke()
        else note("Grant Nearby Devices permission to communicate with the watch.")
    }
    private val enableBluetooth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (bluetoothOn()) withPermissions { client.scan() }
        else note("Phone Bluetooth remains OFF. Enable it and scan again.")
    }
    private val save = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri -> if (uri != null) export(uri) }
    private val chooseFace = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inspected = FaceLab.inspect(contentResolver, uri)
                faceUri = uri
                faceSource = inspected
                note("Image " + inspected.width + " × " + inspected.height +
                    " selected. Nothing has been sent to the watch.")
            } catch (e: Exception) {
                note("Image inspection failed: " + (e.message ?: e.javaClass.simpleName))
            }
        }
    }
    private val saveFace = registerForActivityResult(
        ActivityResultContracts.CreateDocument("image/png")
    ) { uri -> if (uri != null) exportFace(uri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client = WatchBleClient(this, { targets = it }, { watch = it }, ::note)
        setContent { MaterialTheme { Surface { CompanionScreen() } } }
    }
    private fun permissionList(): Array<String> =
        if (Build.VERSION.SDK_INT >= 31) arrayOf(
            Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT
        ) else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun hasPermissions() =
        permissionList().all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
    private fun withPermissions(action: () -> Unit) {
        if (hasPermissions()) action()
        else { deferredAction = action; permissions.launch(permissionList()) }
    }
    private fun bluetoothOn(): Boolean = try {
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter?.isEnabled == true
    } catch (_: Exception) { false }
    private fun startScan() = withPermissions {
        if (bluetoothOn()) client.scan()
        else enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }
    private fun note(line: String) {
        status = line
        messages.add(SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()) + " " + line)
        if (messages.size > 180) messages.removeAt(0)
    }

    private fun startTest(label: String) {
        if (activeExperiment != null) { note("Finish the existing test first."); return }
        activeExperiment = ActiveExperiment(
            label, System.currentTimeMillis(),
            client.snapshot().totalEvents
        )
        note("Test started: " + label + ". Perform normal action then finish.")
    }
    private fun finishTest() {
        val active = activeExperiment ?: return
        val w = (client.snapshot().totalEvents - active.watchBefore).coerceAtLeast(0)
        results.add(ExperimentResult(active.label, active.startedMs, System.currentTimeMillis(), w))
        activeExperiment = null
        note("Recorded: " + active.label + " / watch-origin packets " + w)
    }

    private fun exportFace(uri: Uri) {
        val source = faceUri ?: run { note("Choose an image."); return }
        val w = faceWidth.toIntOrNull() ?: 0
        val h = faceHeight.toIntOrNull() ?: 0
        if (!FaceLab.supportedSize(w, h)) {
            note("Image dimensions must be 64–640 pixels.")
            return
        }
        Thread {
            try {
                contentResolver.openOutputStream(uri, "w")?.use {
                    FaceLab.renderPng(contentResolver, source, it, w, h)
                } ?: error("Cannot open output")
                runOnUiThread { note("Offline PNG generated; no watch upload was attempted.") }
            } catch (e: Exception) {
                runOnUiThread { note("PNG export failed: " + (e.message ?: e.javaClass.simpleName)) }
            }
        }.start()
    }
    private fun export(uri: Uri) {
        val state = client.snapshot()
        val packets = client.events()
        val raw = includeWatchPayloads
        val tests = results.toList()
        val logs = messages.toList()
        val image = faceSource
        val w = faceWidth
        val h = faceHeight
        Thread {
            try {
                contentResolver.openOutputStream(uri, "w")?.use {
                    writeExport(it, state, packets, raw, tests, logs, image, w, h)
                } ?: error("Cannot open output")
                runOnUiThread { note("Feature test ZIP exported. Review before sharing.") }
            } catch (e: Exception) {
                runOnUiThread { note("ZIP export failed: " + (e.message ?: e.javaClass.simpleName)) }
            }
        }.start()
    }
    private fun writeExport(
        output: OutputStream,
        state: WatchState,
        packets: List<WatchEvent>,
        includeRaw: Boolean,
        tests: List<ExperimentResult>,
        log: List<String>,
        source: FaceSource?,
        width: String, height: String
    ) {
        val info = JSONObject()
            .put("app", "T700 Companion 0.3.1")
            .put("connection", state.connection)
            .put("battery_percent", state.battery ?: JSONObject.NULL)
            .put("services", state.serviceCount)
            .put("device_info", JSONObject(state.info))
            .put("watch_notification_count", state.totalEvents)
            .put("watch_notification_channels", JSONObject(state.packetCounts))
            .put("watch_notifications_enabled", state.listening)
            .put("raw_watch_payload_included", includeRaw)
            .put("vendor_commands_sent", 0)
            .put("bluetooth_address_included", false)
        val received = JSONArray()
        packets.forEach { p ->
            val entry = JSONObject()
                .put("timestamp_ms", p.timestampMs)
                .put("channel_uuid", p.channel)
                .put("byte_count", p.bytes.size)
            if (includeRaw) entry.put("watch_payload_hex", ProtocolSafety.toHex(p.bytes))
            received.put(entry)
        }
        val experiments = JSONArray()
        tests.forEach {
            experiments.put(JSONObject()
                .put("name", it.label)
                .put("started_ms", it.startedMs)
                .put("ended_ms", it.endedMs)
                .put("watch_packets", it.watchEvents)
                .put("phone_notifications_collected", false)
                .put("interpretation", "Temporal correlation only; no decoded command proof"))
        }
        val face = JSONObject()
            .put("input_width", source?.width ?: JSONObject.NULL)
            .put("input_height", source?.height ?: JSONObject.NULL)
            .put("preview_width", width)
            .put("preview_height", height)
            .put("watch_face_upload_attempted", false)
            .put("watch_face_protocol_verified", false)
        ZipOutputStream(output).use { zip ->
            fun put(name: String, data: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(data.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            put("README.txt", """
                T700 Companion 0.3.0 feature-test report.
                Only selected-watch Bluetooth notifications are recorded; raw payloads OFF
                by default. Raw watch payloads may contain sensitive information.
                No Android Notification Listener or sensitive phone permissions are included.
                No SMS/call/message content or metadata is accessed.
                Temporal packet correlation does not prove proprietary command semantics.
                No vendor application writes, watch-face upload, OTA, firmware or reset.
            """.trimIndent())
            put("companion-state.json", info.toString(2))
            put("watch-notification-events.json", received.toString(2))
            put("guided-tests.json", experiments.toString(2))
            put("watch-face-preview.json", face.toString(2))
            put("session-log.json", JSONArray(log).toString(2))
        }
    }
    override fun onDestroy() {
        if (::client.isInitialized) client.close()
        super.onDestroy()
    }

    @Composable private fun CompanionScreen() {
        val connected = watch.connection == "Connected"
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text("T700 COMPANION", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold)
            Text("Hardware-safe feature lab · 0.3.1")
            Text("Only the observed T700 watch is selectable. Earbuds are ignored, and " +
                "no guessed vendor commands or sensitive phone permissions are used.")
            Section("1 · Connect") {
                Text(if (bluetoothOn()) "Phone Bluetooth: ON" else "Phone Bluetooth: OFF")
                Button(onClick = { startScan() }) {
                    Text("Scan for T700 Pro Max")
                }
                if (targets.isEmpty()) Text("No T700 results yet.")
                targets.forEach { target ->
                    HorizontalDivider()
                    Text(target.name + " · " + target.rssi + " dBm")
                    OutlinedButton(onClick = { withPermissions { client.connect(target) } }) {
                        Text("Connect to this watch")
                    }
                }
                Text("State: " + watch.connection, fontWeight = FontWeight.SemiBold)
                OutlinedButton(onClick = { client.disconnect() }) { Text("Disconnect") }
            }
            Section("2 · Battery, firmware and clock check") {
                Text("Battery: " + (watch.battery?.let { it.toString() + "%" } ?: "Not read"),
                    style = MaterialTheme.typography.titleLarge)
                Text("Discovered services: " + watch.serviceCount)
                watch.info.forEach { (label, value) -> Text(label + ": " + value) }
                Button(enabled = connected, onClick = { withPermissions { client.refresh() } }) {
                    Text("Refresh device readings")
                }
                OutlinedButton(onClick = {
                    phoneClock = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
                        .format(Date())
                }) { Text("Read phone clock") }
                Text("Phone time: " + phoneClock)
                Text("Watch time sync is NOT yet enabled: its proprietary command is unverified.")
            }
            Section("3 · Watch-origin BLE packets") {
                Text("Watch-specific channels only: battery, …CCA9D/0003, FFFF/FF11. " +
                    "A previous watch test received two 20-byte packets.")
                Button(enabled = connected && !watch.listening,
                    onClick = { withPermissions { client.observeWatchEvents() } }) {
                    Text("Enable temporary watch notification subscriptions")
                }
                Text("Listening: " + if (watch.listening) "Yes" else "No")
                Text("Events received: " + watch.totalEvents)
                watch.packetCounts.forEach { (uuid, count) ->
                    Text(uuid + " : " + count)
                }
                Text("Disconnect to stop. No vendor packets are transmitted.")
            }
            Section("4 · Phone notification privacy") {
                Text("This sideloaded APK does not request Notification Access, SMS, call logs, " +
                    "contacts, or Accessibility privileges. Sensitive phone permissions are unnecessary " +
                    "for Bluetooth scanning, device information and watch-origin BLE observation.")
                Text("For WhatsApp/SMS/call tests, manually start a named experiment below, " +
                    "perform the normal action, and finish. Only watch-origin packet counts " +
                    "are recorded. No automatic phone message or caller detection is performed.")
                Text("Earbuds and other phone Bluetooth connections are never disconnected.")
            }
            Section("5 · Guided feature experiments") {
                Text("Start one test, perform a normal watch or phone action, then finish. " +
                    "We count only watch-origin BLE packets; phone notifications are not read.")
                if (activeExperiment == null) {
                    listOf(
                        "Watch screen/button action",
                        "Watch fitness screen",
                        "Incoming WhatsApp",
                        "Incoming SMS",
                        "Incoming phone call",
                        "Watch-face image preview"
                    ).forEach { label ->
                        OutlinedButton(onClick = { startTest(label) }) {
                            Text("Start: " + label)
                        }
                    }
                } else {
                    Text("ACTIVE: " + activeExperiment!!.label, fontWeight = FontWeight.Bold)
                    Button(onClick = { finishTest() }) { Text("Finish and record") }
                }
                Text("Completed tests: " + results.size)
                results.takeLast(8).forEach {
                    Text(it.label + " · " + it.watchEvents + " watch-origin packets")
                }
            }
            Section("6 · Offline watch-face preparation") {
                Text("Crop a local image to a PNG preview. Output dimensions are USER-SELECTED " +
                    "and not verified for this watch; it does not upload a watch face.")
                Button(onClick = { chooseFace.launch("image/*") }) { Text("Choose phone image") }
                Text(faceSource?.let { "Image: " + it.width + " × " + it.height }
                    ?: "No image selected")
                OutlinedTextField(
                    value = faceWidth, onValueChange = { if (it.length <= 4) faceWidth = it },
                    label = { Text("Preview width (64–640)") }, singleLine = true)
                OutlinedTextField(
                    value = faceHeight, onValueChange = { if (it.length <= 4) faceHeight = it },
                    label = { Text("Preview height (64–640)") }, singleLine = true)
                Button(
                    enabled = faceUri != null && FaceLab.supportedSize(
                        faceWidth.toIntOrNull() ?: 0, faceHeight.toIntOrNull() ?: 0),
                    onClick = { saveFace.launch("T700-offline-face-preview.png") }
                ) { Text("Export preview PNG — NO watch upload") }
            }
            Section("7 · Local export and privacy") {
                Row {
                    Checkbox(checked = includeWatchPayloads,
                        onCheckedChange = { includeWatchPayloads = it })
                    Text("Include raw watch bytes (PRIVATE)", Modifier.padding(top = 12.dp))
                }
                Text("Off by default. Raw watch packets may contain private data. " +
                    "Phone events never contain message contents or identifiers.")
                Button(onClick = {
                    save.launch("T700-Companion-" +
                        SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date()) + ".zip")
                }) { Text("Export feature test ZIP") }
            }
            Section("8 · Proprietary features not yet enabled") {
                Text("Time sync, notifications and calls TO watch, health/step history, alarms, " +
                    "find watch and watch-face upload need verified HiWatchPro commands.")
                Text("Never guess application writes; no OTA, firmware flash or reset.")
            }
            Section("Session log") {
                Text(status, fontWeight = FontWeight.Bold)
                messages.takeLast(12).forEach {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
    @Composable private fun Section(title: String, content: @Composable () -> Unit) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                content()
            }
        }
    }
}
