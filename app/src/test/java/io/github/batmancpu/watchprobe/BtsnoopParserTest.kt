package io.github.batmancpu.watchprobe

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class BtsnoopParserTest {
    @Test fun parsesAttReadResponseMetadataOnly() {
        val output = ByteArrayOutputStream()
        val data = DataOutputStream(output)
        data.write("btsnoop\u0000".toByteArray(Charsets.US_ASCII))
        data.writeInt(1)
        data.writeInt(1002)
        val packet = byteArrayOf(2, 1, 0, 5, 0, 1, 0, 4, 0, 0x0B)
        data.writeInt(packet.size)
        data.writeInt(packet.size)
        data.writeInt(0)
        data.writeInt(0)
        data.writeLong(0)
        data.write(packet)
        val result = BtsnoopParser.parse(ByteArrayInputStream(output.toByteArray()))
        assertEquals(1, result.packets)
        assertEquals(1, result.attPackets)
        assertEquals(1, result.opcodeCounts["0x0B"])
    }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnknownCapture() {
        BtsnoopParser.parse(ByteArrayInputStream(ByteArray(32)))
    }
}
