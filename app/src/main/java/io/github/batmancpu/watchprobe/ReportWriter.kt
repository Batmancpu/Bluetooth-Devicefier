package io.github.batmancpu.watchprobe

import android.os.Build
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class ImageEvidence(val width: Int, val height: Int, val mimeType: String, val sizeBytes: Long?)

object ReportWriter {
    private fun allowedPublic(uuid: String) = listOf(
        "00002a00-", "00002a19-", "00002a24-", "00002a26-",
        "00002a27-", "00002a28-", "00002a29-"
    ).any { uuid.lowercase().startsWith(it) }

    fun writeZip(
        out: OutputStream,
        devices: List<SeenDevice>,
        selected: SeenDevice?,
        snapshot: GattSnapshot?,
        hci: HciSummary?,
        image: ImageEvidence?,
        events: List<String>,
        includeRaw: Boolean
    ) {
        val environment = JSONObject()
            .put("tool", "WatchProbe 0.1.0")
            .put("phone", Build.MANUFACTURER + " " + Build.MODEL)
            .put("android_sdk", Build.VERSION.SDK_INT)
            .put("generated_ms", System.currentTimeMillis())
            .put("mode", "SCAN_DISCOVERY_READ_ONLY")
            .put("no_writes_or_pairing", true)
            .put("include_sensitive_data", includeRaw)
        val device = JSONObject()
            .put("name", selected?.name ?: "not selected")
            .put("address", if (includeRaw) selected?.address ?: JSONObject.NULL else "[REDACTED]")
            .put("rssi", selected?.rssi ?: JSONObject.NULL)
            .put("sample_count", selected?.samples ?: 0)
            .put("chipset", "UNKNOWN")
            .put("firmware", "UNKNOWN unless read through GATT")
            .put("protocol", "UNKNOWN")
            .put("watch_face_format", "UNKNOWN")

        val advertising = JSONArray()
        devices.forEach { item ->
            val ad = item.advertisement
            val obj = JSONObject()
                .put("name", item.name)
                .put("address", if (includeRaw) item.address else "[REDACTED]")
                .put("rssi", item.rssi)
                .put("sample_count", item.samples)
                .put("legacy", ad.legacy)
                .put("connectable", ad.connectable)
                .put("flags", ad.flags ?: JSONObject.NULL)
                .put("service_uuids", JSONArray(ad.serviceUuids))
                .put("manufacturer_ids", JSONArray(ad.manufacturerData.keys.toList()))
                .put("service_data_uuids", JSONArray(ad.serviceData.keys.toList()))
            if (includeRaw) {
                obj.put("raw_record_hex", ad.rawHex ?: JSONObject.NULL)
                obj.put("manufacturer_data", JSONObject(ad.manufacturerData))
                obj.put("service_data", JSONObject(ad.serviceData))
            } else {
                obj.put("raw_data", "[REDACTED; enable raw evidence export explicitly]")
            }
            advertising.put(obj)
        }

        val services = JSONArray()
        val values = JSONArray()
        snapshot?.services?.forEach { service ->
            val chars = JSONArray()
            service.characteristics.forEach { ch ->
                val safe = includeRaw || allowedPublic(ch.uuid)
                chars.put(JSONObject()
                    .put("uuid", ch.uuid)
                    .put("instance_id", ch.instanceId)
                    .put("properties_bits", ch.properties)
                    .put("properties", propertyNames(ch.properties))
                    .put("permissions_bits", ch.permissions)
                    .put("read_status", ch.readStatus)
                    .put("descriptors", JSONArray().apply {
                        ch.descriptors.forEach { d ->
                            put(JSONObject().put("uuid", d.uuid).put("permissions_bits", d.permissions))
                        }
                    })
                )
                if (ch.readStatus == "READ_OK") {
                    values.put(JSONObject()
                        .put("service_uuid", service.uuid)
                        .put("characteristic_uuid", ch.uuid)
                        .put("instance_id", ch.instanceId)
                        .put("value_hex", if (safe) ch.rawHex ?: JSONObject.NULL else "[REDACTED]")
                        .put("decoded", if (safe) ch.decodedText ?: JSONObject.NULL else "[REDACTED]")
                        .put("redacted", !safe)
                    )
                }
            }
            services.put(JSONObject()
                .put("uuid", service.uuid)
                .put("type", service.type)
                .put("characteristics", chars)
            )
        }

        val gatt = JSONObject().put("status", snapshot?.status ?: "not started")
            .put("finished", snapshot?.finished ?: false).put("services", services)
        val capture = JSONObject().put("imported", hci != null)
            .put("packets", hci?.packets ?: JSONObject.NULL)
            .put("acl_packets", hci?.aclPackets ?: JSONObject.NULL)
            .put("att_packets", hci?.attPackets ?: JSONObject.NULL)
            .put("att_opcodes", JSONObject(hci?.opcodeCounts ?: emptyMap<String, Int>()))
            .put("note", hci?.notes ?: "No snoop data imported; other apps cannot be intercepted directly")
        val face = JSONObject().put("inspected", image != null)
            .put("width", image?.width ?: JSONObject.NULL)
            .put("height", image?.height ?: JSONObject.NULL)
            .put("mime", image?.mimeType ?: JSONObject.NULL)
            .put("file_size", image?.sizeBytes ?: JSONObject.NULL)
            .put("watch_supported_format", "UNKNOWN")
            .put("image_content_embedded", false)
        val apk = JSONObject().put("reference_app", "HiWatchPro")
            .put("reference_apk_sha256", "d3d724530619627c563ec94998c0fcf0e9c15eedcf457603a2fbc67c2cf598fc")
            .put("reference_only", "Source: docs/APK_REFERENCE.md; binary not present or redistributed")
            .put("watch_specific_protocol_verified", false)

        val report = buildString {
            appendLine("# WatchProbe — read-only BLE report")
            appendLine()
            appendLine("Device: " + (selected?.name ?: "not selected"))
            appendLine("Status: " + (snapshot?.status ?: "not started"))
            appendLine("Services: " + (snapshot?.services?.size ?: 0))
            appendLine("Raw/private data included: " + includeRaw)
            appendLine()
            appendLine("## GATT inventory")
            snapshot?.services?.forEach { s ->
                appendLine("- " + s.uuid + " (" + s.type + ")")
                s.characteristics.forEach { c ->
                    appendLine("  - " + c.uuid + " | " + propertyNames(c.properties) + " | " + c.readStatus)
                    c.descriptors.forEach { appendLine("    - descriptor " + it.uuid) }
                }
            }
            appendLine()
            appendLine("## Unknown / not tested")
            appendLine("- Chipset, hidden hardware, proprietary command protocol, face encoding/transfer and feature support are not established by scanning.")
            appendLine("- Android cannot directly read HiWatchPro's private Bluetooth exchanges.")
            appendLine("- No pairing, writes, notifications/CCCD configuration, OTA/DFU or firmware changes were attempted by WatchProbe.")
            appendLine()
            appendLine("## Diagnostics")
            events.takeLast(80).forEach { appendLine("- " + it) }
        }
        val readme = """
            WatchProbe local research bundle.
            No cloud account, internet permission, analytics, or telemetry.
            By default, identifiers and unknown raw characteristic values are redacted.
            Review any ZIP with raw evidence enabled before sharing.
            Imported snoop packet bodies, notification contents and source photos are not stored in this export.
            A BLE read or a GATT discovery is not a watch setting change.
            This report cannot prove proprietary protocol semantics or inaccessible hardware details.
        """.trimIndent()

        ZipOutputStream(out).use { zip ->
            fun put(name: String, text: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(text.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            put("README.txt", readme)
            put("environment.json", environment.toString(2))
            put("device.json", device.toString(2))
            put("advertising.json", advertising.toString(2))
            put("gatt.json", gatt.toString(2))
            put("readable-values.json", values.toString(2))
            put("capture-summary.json", capture.toString(2))
            put("watch-face-input-metadata.json", face.toString(2))
            put("apk-reference.json", apk.toString(2))
            put("protocol-events.jsonl", "")
            put("report.md", report)
            put("diagnostics.json", JSONArray(events).toString(2))
        }
    }
}
