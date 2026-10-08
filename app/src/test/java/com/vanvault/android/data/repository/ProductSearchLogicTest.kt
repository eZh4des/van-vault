package com.vanvault.android.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductSearchLogicTest {
    @Test fun tokenizeLowercasesAndStripsAccents() =
        assertEquals(setOf("dvr", "4ch", "turbo", "265", "acusense"), tokenize("DVR 4CH TURBO H.265+ Acúsense"))

    @Test fun tokenizeDropsOneCharWords() =
        assertEquals(setOf("cable", "usb"), tokenize("CABLE A USB M"))

    @Test fun tokenizeHandlesSlashesAndBlank() {
        assertEquals(setOf("hdmi", "vga"), tokenize("HDMI/VGA"))
        assertTrue(tokenize("   ").isEmpty())
    }

    @Test fun productKeyReplacesForbiddenChars() {
        assertEquals("TVI_AHD_CVBS H_265+", normalizeProductKey("TVI/AHD/CVBS  H.265+ "))
        assertNull(normalizeProductKey("   "))
    }

    @Test fun intersectRequiresAllWords() {
        val r = intersectAll(listOf(setOf("a", "b", "c"), setOf("b", "c", "d"), setOf("c", "z")))
        assertEquals(setOf("c"), r)
        assertTrue(intersectAll(listOf(setOf("a"), setOf("b"))).isEmpty())
        assertTrue(intersectAll(emptyList()).isEmpty())
    }

    @Test fun indexPathsIncludeNameAndModelWords() {
        val paths = searchIndexPaths("CAMARA DOMO", "DS-2CE56")
        assertEquals(
            setOf("searchIndex/camara/CAMARA DOMO", "searchIndex/domo/CAMARA DOMO",
                "searchIndex/ds/CAMARA DOMO", "searchIndex/2ce56/CAMARA DOMO"),
            paths.toSet()
        )
    }
}
