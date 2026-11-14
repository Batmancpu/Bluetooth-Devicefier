package io.github.batmancpu.watchcompanion

import org.junit.Assert.*
import org.junit.Test

class FeatureLabTest {
    @Test fun onlyWatchNamesPass() {
        assertTrue(WatchIdentity.matchesAdvertisedName("T700Pro Max"))
        assertTrue(WatchIdentity.matchesAdvertisedName("T700 Pro Max"))
        assertFalse(WatchIdentity.matchesAdvertisedName("OPPO Enco Air"))
        assertFalse(WatchIdentity.matchesAdvertisedName("JBL Tune Buds"))
        assertFalse(WatchIdentity.matchesAdvertisedName("T700 Pro Mini"))
        assertFalse(WatchIdentity.matchesAdvertisedName("Other T700 device"))
        assertFalse(WatchIdentity.matchesAdvertisedName(""))
    }

    @Test fun observedGattServicesRequired() {
        assertTrue(WatchIdentity.matchesObservedGattProfile(listOf(
            ProtocolSafety.BATTERY_SERVICE,
            ProtocolSafety.VENDOR_UART_SERVICE,
            ProtocolSafety.OTHER_VENDOR_SERVICE
        )))
        assertFalse(WatchIdentity.matchesObservedGattProfile(listOf(
            ProtocolSafety.BATTERY_SERVICE,
            ProtocolSafety.VENDOR_UART_SERVICE
        )))
        assertFalse(WatchIdentity.matchesObservedGattProfile(emptyList()))
    }

    @Test fun faceImageBoundsAreConservative() {
        assertTrue(FaceLab.supportedSize(240, 280))
        assertTrue(FaceLab.supportedSize(640, 640))
        assertFalse(FaceLab.supportedSize(641, 640))
        assertFalse(FaceLab.supportedSize(0, 240))
        assertFalse(FaceLab.supportedSize(9999, 9999))
    }
}
