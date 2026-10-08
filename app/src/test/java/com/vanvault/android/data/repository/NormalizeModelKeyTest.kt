package com.vanvault.android.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NormalizeModelKeyTest {
    @Test fun keepsPlainModel() = assertEquals("DS-7104HGHI-M1", normalizeModelKey("DS-7104HGHI-M1"))

    @Test fun replacesFirebaseForbiddenChars() {
        assertEquals("DS-7108HQHI-M1_SC", normalizeModelKey("DS-7108HQHI-M1/SC"))
        assertEquals("HDMI1_8", normalizeModelKey("HDMI1.8"))
    }

    @Test fun trimsAndCollapsesSpaces() = assertEquals("K32LCD PLUS", normalizeModelKey("  K32LCD   PLUS "))

    @Test fun blankIsNull() {
        assertNull(normalizeModelKey(""))
        assertNull(normalizeModelKey("   "))
    }
}
