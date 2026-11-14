package io.github.batmancpu.watchcompanion

/** Only watch-observed UUIDs. No proprietary application payloads are transmitted. */
object ProtocolSafety {
    const val BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb"
    const val BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb"
    const val INFO_SERVICE = "0000180a-0000-1000-8000-00805f9b34fb"
    const val VENDOR_UART_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9d"
    const val VENDOR_UART_NOTIFY = "6e400003-b5a3-f393-e0a9-e50e24dcca9d"
    const val OTHER_VENDOR_SERVICE = "0000ffff-0000-1000-8000-00805f9b34fb"
    const val OTHER_VENDOR_NOTIFY = "0000ff11-0000-1000-8000-00805f9b34fb"
    const val CCCD = "00002902-0000-1000-8000-00805f9b34fb"

    /** Temporary CCCD subscribe only; never send guessed commands to WRITE characteristics. */
    fun canObserve(serviceUuid: String, characteristicUuid: String): Boolean {
        val pair = serviceUuid.lowercase() to characteristicUuid.lowercase()
        return pair in setOf(
            BATTERY_SERVICE to BATTERY_LEVEL,
            VENDOR_UART_SERVICE to VENDOR_UART_NOTIFY,
            OTHER_VENDOR_SERVICE to OTHER_VENDOR_NOTIFY
        )
    }

    /** Standard Device Information characteristic string fields known from GATT survey. */
    fun infoLabel(uuid: String): String? = when (uuid.lowercase().take(8)) {
        "00002a29" -> "Manufacturer"
        "00002a26" -> "Firmware"
        "00002a27" -> "Hardware"
        "00002a24" -> "Model"
        else -> null
    }

    fun batteryPercent(value: ByteArray): Int? =
        value.firstOrNull()?.let { it.toInt() and 0xFF }?.takeIf { it in 0..100 }

    fun toHex(value: ByteArray): String = value.joinToString("") { "%02X".format(it.toInt() and 255) }
}
