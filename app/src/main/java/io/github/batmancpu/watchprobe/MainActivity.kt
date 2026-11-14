package io.github.batmancpu.watchprobe

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var survey: BleSurvey
    private var devices by mutableStateOf<List<SeenDevice>>(emptyList())
    private var active by mutableStateOf<SeenDevice?>(null)
    private var gatt by mutableStateOf<GattSnapshot?>(null)
    private var capture by mutableStateOf<HciSummary?>(null)
    private var face by mutableStateOf<ImageEvidence?>(null)
    private var includeRaw by mutableStateOf(false)
    private var status by mutableStateOf("Ready. Nothing has been sent to your watch.")
    private val history = mutableStateListOf<String>()
    private var pendingAction: (() -> Unit)? = null

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (hasBluetoothPermissions()) {
            val action = pendingAction
            pendingAction = null
            action?.invoke()
        } else {
            pendingAction = null
            message("Bluetooth permission denied; scanning is disabled.")
        }
    }
    private val saveReport = registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) export(uri)
    }
    private val openCapture = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) Thread {
            try {
                val result = contentResolver.openInputStream(uri)?.use { BtsnoopParser.parse(it) }
                    ?: error("Cannot open selected file")
                runOnUiThread {
                    capture = result
                    message("Imported btsnoop metadata: " + result.packets + " records / " + result.attPackets + " ATT starts")
                }
            } catch (ex: Exception) {
                runOnUiThread { message("Capture import failed: " + (ex.message ?: ex.javaClass.simpleName)) }
            }
        }.start()
    }
    private val openImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) inspectImage(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        survey = BleSurvey(this,
            onDevices = { devices = it },
            onGatt = { gatt = it },
            onStatus = ::message
        )
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFF78D5EF),
                secondary = Color(0xFFA5B8FF),
                background = Color(0xFF09111F),
                surface = Color(0xFF142238),
                onSurface = Color(0xFFF3F6FF)
            )) {
                AppScreen()
            }
        }
    }

    private fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= 31) arrayOf(
            Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT
        ) else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun hasBluetoothPermissions(): Boolean =
        requiredPermissions().all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

    private fun withPermission(action: () -> Unit) {
        if (hasBluetoothPermissions()) action() else {
            pendingAction = action
            permissionRequest.launch(requiredPermissions())
        }
    }

    private fun bluetoothOn(): Boolean = try {
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter?.isEnabled == true
    } catch (_: Exception) { false }

    private fun message(value: String) {
        status = value
        history.add(SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()) + " " + value)
        if (history.size > 150) history.removeAt(0)
    }

    private fun inspectImage(uri: Uri) {
        try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            if (opts.outWidth <= 0 || opts.outHeight <= 0) error("Image dimensions not available")
            var size: Long? = null
            contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use {
                if (it.moveToFirst()) {
                    val col = it.getColumnIndex(OpenableColumns.SIZE)
                    if (col >= 0 && !it.isNull(col)) size = it.getLong(col)
                }
            }
            face = ImageEvidence(opts.outWidth, opts.outHeight,
                contentResolver.getType(uri) ?: "unknown", size)
            message("Local image metadata inspected; no image sent to watch.")
        } catch (ex: Exception) {
            message("Image inspection failed: " + (ex.message ?: ex.javaClass.simpleName))
        }
    }

    private fun export(uri: Uri) {
        val scanSnapshot = devices.toList()
        val selectedSnapshot = active
        val gattSnapshot = gatt
        val hciSnapshot = capture
        val imageSnapshot = face
        val rawSnapshot = includeRaw
        val eventsSnapshot = history.toList()
        Thread {
            try {
                val output = contentResolver.openOutputStream(uri, "w") ?: error("File not writable")
                ReportWriter.writeZip(output, scanSnapshot, selectedSnapshot, gattSnapshot,
                    hciSnapshot, imageSnapshot, eventsSnapshot, rawSnapshot)
                runOnUiThread { message("ZIP research report saved locally. Review before sharing.") }
            } catch (ex: Exception) {
                runOnUiThread { message("Export failed: " + (ex.message ?: ex.javaClass.simpleName)) }
            }
        }.start()
    }

    override fun onDestroy() {
        if (::survey.isInitialized) survey.close()
        super.onDestroy()
    }

    @Composable
    private fun AppScreen() {
        val ink = Color(0xFFF3F7FF)
        val muted = Color(0xFFB2C4D9)
        val selected = active
        val snapshot = gatt
        Column(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF08101D), Color(0xFF182D46), Color(0xFF0B1525)))
        ).verticalScroll(rememberScrollState()).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Spacer(Modifier.height(8.dp))
            Text("WATCHPROBE", color = Color(0xFF79D8EF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Know your watch.", color = ink, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("T700 Pro Max · diagnostic-only build 0.1.0", color = muted, fontSize = 13.sp)
            Panel {
                Text("READ-ONLY SAFETY MODE", color = Color(0xFF87E5C1), fontWeight = FontWeight.Bold)
                Text("Scan, connect, enumerate GATT and read unencrypted attributes only. No pairing, writes, watch-face transfer, CCCD subscription, reset or firmware commands.",
                    color = ink, fontSize = 14.sp)
            }
            Panel {
                SectionTitle("1 · Discover Bluetooth devices")
                Text("Bluetooth enabled: " + if (bluetoothOn()) "Yes" else "No — enable it in phone settings",
                    color = muted, fontSize = 13.sp)
                Button(onClick = { withPermission { survey.scan() } }, modifier = Modifier.fillMaxWidth()) {
                    Text("Scan nearby devices (15 seconds)")
                }
                Text("Select the entry matching your physical watch. Never connect to an unknown device.",
                    color = muted, fontSize = 12.sp)
                if (devices.isEmpty()) Text("No scan evidence yet.", color = muted)
                devices.take(25).forEach { d ->
                    HorizontalDivider(color = Color(0xFF34465B))
                    Text(d.name + "  ·  " + d.rssi + " dBm  ·  " + d.samples + " sightings",
                        color = ink, fontWeight = FontWeight.SemiBold)
                    Text("Manufacturer IDs: " + d.advertisement.manufacturerData.keys.joinToString().ifEmpty { "none advertised" },
                        color = muted, fontSize = 12.sp)
                    OutlinedButton(onClick = {
                        withPermission {
                            active = d
                            survey.connect(d)
                        }
                    }) { Text("Inspect this device safely") }
                }
            }
            Panel {
                SectionTitle("2 · GATT services and safe reads")
                Text("Selected device: " + (selected?.name ?: "None"), color = ink)
                Text(snapshot?.status ?: "Select a device above to begin.", color = muted, fontSize = 13.sp)
                Text("Services: " + (snapshot?.services?.size ?: 0), color = ink)
                snapshot?.services?.forEach { service ->
                    HorizontalDivider(color = Color(0xFF34465B))
                    Text(service.uuid + "  [" + service.type + "]", color = Color(0xFF8DDDF6), fontSize = 12.sp)
                    service.characteristics.forEach { ch ->
                        Text(ch.uuid, color = ink, fontSize = 12.sp)
                        Text(propertyNames(ch.properties) + " · " + ch.readStatus,
                            color = muted, fontSize = 11.sp)
                        if (ch.decodedText != null && !ch.uuid.lowercase().startsWith("00002a25-")) {
                            Text("Decoded: " + ch.decodedText, color = ink, fontSize = 12.sp)
                        }
                        if (ch.descriptors.isNotEmpty()) {
                            Text("Descriptors: " + ch.descriptors.joinToString { it.uuid },
                                color = muted, fontSize = 11.sp)
                        }
                    }
                }
                OutlinedButton(onClick = {
                    survey.close()
                    message("Connection closed by user.")
                }) { Text("Disconnect") }
            }
            Panel {
                SectionTitle("3 · Optional offline research")
                Text("Import an uncompressed Android H4 btsnoop log (metadata only), or inspect a local watch-face image's dimensions. Neither operation communicates with the watch.",
                    color = muted, fontSize = 13.sp)
                OutlinedButton(onClick = { openCapture.launch("*/*") }) {
                    Text("Import Bluetooth snoop log")
                }
                Text(if (capture == null) "No capture loaded" else
                    "HCI records: " + capture!!.packets + " · ATT starts: " + capture!!.attPackets,
                    color = ink, fontSize = 12.sp)
                OutlinedButton(onClick = { openImage.launch("image/*") }) {
                    Text("Inspect an image from phone")
                }
                Text(if (face == null) "No image inspected" else
                    "Image: " + face!!.width + " × " + face!!.height + " pixels",
                    color = ink, fontSize = 12.sp)
                Text("HiWatchPro APK protocol analysis remains incomplete; its binary is not included in this app.",
                    color = muted, fontSize = 12.sp)
            }
            Panel {
                SectionTitle("4 · Export research bundle")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Switch(checked = includeRaw, onCheckedChange = { includeRaw = it })
                    Column {
                        Text("Include private/raw evidence", color = ink, fontSize = 13.sp)
                        Text("Off by default. May contain Bluetooth addresses, identifiers and private characteristic contents.",
                            color = muted, fontSize = 11.sp)
                    }
                }
                Button(onClick = {
                    val name = "WatchProbe-" + SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date()) + ".zip"
                    saveReport.launch(name)
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Export ZIP: JSON + report")
                }
                Text("Only your chosen storage location receives the export. No internet permission, tracking or account.",
                    color = muted, fontSize = 12.sp)
            }
            Panel {
                SectionTitle("Session status")
                Text(status, color = ink, fontSize = 13.sp)
                history.takeLast(6).forEach { Text(it, color = muted, fontSize = 11.sp) }
            }
            Spacer(Modifier.height(15.dp))
        }
    }

    @Composable
    private fun SectionTitle(title: String) {
        Text(title, color = Color(0xFFF1F6FF), fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }

    @Composable
    private fun Panel(content: @Composable ColumnScope.() -> Unit) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xF0223449)),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
        }
    }
}
