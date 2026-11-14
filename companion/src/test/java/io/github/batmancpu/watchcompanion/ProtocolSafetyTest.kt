package io.github.batmancpu.watchcompanion

import org.junit.Assert.*
import org.junit.Test

class ProtocolSafetyTest {
    @Test fun onlyDiscoveredNotificationPathsCanBeEnabled() {
        assertTrue(ProtocolSafety.canObserve(ProtocolSafety.BATTERY_SERVICE, ProtocolSafety.BATTERY_LEVEL))
        assertTrue(ProtocolSafety.canObserve(ProtocolSafety.VENDOR_UART_SERVICE, ProtocolSafety.VENDOR_UART_NOTIFY))
        assertTrue(ProtocolSafety.canObserve(ProtocolSafety.OTHER_VENDOR_SERVICE, ProtocolSafety.OTHER_VENDOR_NOTIFY))
        assertFalse(ProtocolSafety.canObserve(ProtocolSafety.VENDOR_UART_SERVICE, "6e400002-b5a3-f393-e0a9-e50e24dcca9d"))
        assertFalse(ProtocolSafety.canObserve(ProtocolSafety.OTHER_VENDOR_SERVICE, "0000ff22-0000-1000-8000-00805f9b34fb"))
        assertFalse(ProtocolSafety.canObserve("6e400001-b5a3-f393-e0a9-e50e24dcca9e", ProtocolSafety.VENDOR_UART_NOTIFY))
        assertFalse(ProtocolSafety.canObserve("00001812-0000-1000-8000-00805f9b34fb", "00002a4d-0000-1000-8000-00805f9b34fb"))
    }
    @Test fun batteryIsUnsignedAndBoundsChecked() {
        assertEquals(100, ProtocolSafety.batteryPercent(byteArrayOf(100)))
        assertEquals(0, ProtocolSafety.batteryPercent(byteArrayOf(0)))
        assertNull(ProtocolSafety.batteryPercent(byteArrayOf()))
        assertNull(ProtocolSafety.batteryPercent(byteArrayOf(0xff.toByte())))
    }
    @Test fun infoFieldsRestrictedToKnownStrings() {
        assertEquals("Firmware", ProtocolSafety.infoLabel("00002a26-0000-1000-8000-00805f9b34fb"))
        assertNull(ProtocolSafety.infoLabel("00002a25-0000-1000-8000-00805f9b34fb"))
    }
    @Test fun hexIsUnsignedUppercase() {
        assertEquals("00A5FF", ProtocolSafety.toHex(byteArrayOf(0, 0xa5.toByte(), 0xff.toByte())))
    }
}
