package io.github.batmancpu.watchprobe

import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.InputStream

data class HciSummary(
    val packets: Int,
    val aclPackets: Int,
    val attPackets: Int,
    val opcodeCounts: Map<String, Int>,
    val notes: String
)

/**
 * Reads Android's uncompressed btsnoop H4 format. Never retains packet bodies,
 * handles, addresses or message contents. Imported logs remain outside the app.
 * This is a metadata helper, NOT a HiWatchPro packet decoder.
 */
object BtsnoopParser {
    private const val MAX_CAPTURE_BYTES = 32L * 1024 * 1024
    private const val MAX_RECORD_BYTES = 128 * 1024

    fun parse(input: InputStream): HciSummary {
        val bytes = DataInputStream(BufferedInputStream(input))
        val magic = ByteArray(8)
        bytes.readFully(magic)
        require(String(magic, Charsets.US_ASCII) == "btsnoop\u0000") { "Not an uncompressed btsnoop file" }
        require(bytes.readInt() == 1) { "Unsupported btsnoop version" }
        val linkType = bytes.readInt()
        require(linkType == 1002) { "Only H4 btsnoop capture (datalink 1002) is supported" }
        var inspected = 16L
        var packets = 0
        var acl = 0
        var att = 0
        val opcodes = sortedMapOf<String, Int>()
        while (inspected + 24 <= MAX_CAPTURE_BYTES) {
            val originalSize = try { bytes.readInt() } catch (_: java.io.EOFException) { break }
            val includedSize = bytes.readInt()
            bytes.readInt() // packet flags (direction), not exported
            bytes.readInt() // cumulative dropped
            bytes.readLong() // capture clock, not exported
            require(originalSize >= 0 && includedSize in 0..MAX_RECORD_BYTES && includedSize <= originalSize) {
                "Invalid or excessively large btsnoop record"
            }
            inspected += 24L + includedSize
            require(inspected <= MAX_CAPTURE_BYTES) { "Capture exceeds 32 MiB safety limit" }
            val payload = ByteArray(includedSize)
            bytes.readFully(payload)
            packets++
            if (payload.isNotEmpty() && payload[0].toInt() == 2) {
                acl++
                if (payload.size >= 10) {
                    val handleField = (payload[1].toInt() and 255) or ((payload[2].toInt() and 255) shl 8)
                    val boundary = (handleField ushr 12) and 3
                    val l2capCid = (payload[7].toInt() and 255) or ((payload[8].toInt() and 255) shl 8)
                    if (boundary != 1 && l2capCid == 4) {
                        att++
                        val key = "0x" + (payload[9].toInt() and 255).toString(16).uppercase().padStart(2, '0')
                        opcodes[key] = (opcodes[key] ?: 0) + 1
                    }
                }
            }
        }
        return HciSummary(packets, acl, att, opcodes, "Metadata only; encrypted or fragmented traffic may not decode. No payloads stored.")
    }
}
