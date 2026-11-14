package io.github.batmancpu.watchcompanion

/** Exact observed watch identity, not a generic T700 substring search. */
object WatchIdentity {
    val allowedNames = listOf("T700Pro Max", "T700 Pro Max")

    fun matchesAdvertisedName(name: String): Boolean =
        allowedNames.any { it.equals(name.trim(), ignoreCase = true) }

    fun matchesObservedGattProfile(serviceUuids: Collection<String>): Boolean {
        val actual = serviceUuids.map { it.lowercase() }.toSet()
        return ProtocolSafety.VENDOR_UART_SERVICE in actual &&
            ProtocolSafety.OTHER_VENDOR_SERVICE in actual &&
            ProtocolSafety.BATTERY_SERVICE in actual
    }
}
