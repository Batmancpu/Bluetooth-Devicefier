package io.github.batmancpu.watchcompanion

import org.junit.Assert.*
import org.junit.Test

class FeatureLabTest {
    @Test fun phoneMetadataWhitelistIsRestricted() {
        assertEquals("WhatsApp", PhoneNoticeLab.labelFor("com.whatsapp"))
        assertEquals("Phone", PhoneNoticeLab.labelFor("com.google.android.dialer"))
        assertEquals("SMS", PhoneNoticeLab.labelFor("com.google.android.apps.messaging"))
        assertNull(PhoneNoticeLab.labelFor("com.bank.app"))
        assertNull(PhoneNoticeLab.labelFor("com.instagram.android"))
    }

    @Test fun faceImageBoundsAreConservative() {
        assertTrue(FaceLab.supportedSize(240, 280))
        assertTrue(FaceLab.supportedSize(640, 640))
        assertFalse(FaceLab.supportedSize(641, 640))
        assertFalse(FaceLab.supportedSize(0, 240))
        assertFalse(FaceLab.supportedSize(9999, 9999))
    }
}
