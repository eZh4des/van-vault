package com.vanvault.android.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests para [UserProfile] — especialmente [UserProfile.computeInitials] que
 * es la logica de UI critica para el avatar circular.
 */
class UserProfileTest {

    @Test
    fun `initials takes first letter of first 2 words`() {
        val initials = UserProfile.computeInitials("Carlos Ramirez", "carlos@vanvault.co")
        assertEquals("CR", initials)
    }

    @Test
    fun `initials is uppercase regardless of input case`() {
        val initials = UserProfile.computeInitials("andres lopez", "andres@vanvault.co")
        assertEquals("AL", initials)
    }

    @Test
    fun `initials handles single name correctly`() {
        val initials = UserProfile.computeInitials("Pedro", "pedro@vanvault.co")
        assertEquals("P", initials)
    }

    @Test
    fun `initials handles many words - takes only first 2`() {
        val initials = UserProfile.computeInitials("Maria Del Carmen De La Cruz", "maria@x.com")
        assertEquals("MD", initials)
    }

    @Test
    fun `initials falls back to email when name is empty`() {
        val initials = UserProfile.computeInitials("", "carlos@vanvault.co")
        assertEquals("CA", initials)
    }

    @Test
    fun `initials falls back to U when both name and email are empty`() {
        val initials = UserProfile.computeInitials("", "")
        assertEquals("U", initials)
    }

    @Test
    fun `initials ignores multiple spaces between names`() {
        val initials = UserProfile.computeInitials("Diana    Perez", "diana@x.com")
        assertEquals("DP", initials)
    }

    @Test
    fun `initials trims leading and trailing whitespace`() {
        val initials = UserProfile.computeInitials("   Andres   ", "andres@x.com")
        assertEquals("A", initials)
    }

    @Test
    fun `fullName is alias of displayName`() {
        // Preparar
        val profile = UserProfile(
            uid = "u1",
            displayName = "Test User",
            email = "test@x.com"
        )
        // Verificar
        assertEquals("Test User", profile.fullName)
    }

    @Test
    fun `default role is CLIENT for safety`() {
        // Si un UserProfile se crea sin rol explicito, debe ser CLIENT
        // (nadie es admin por accidente)
        val profile = UserProfile(
            uid = "u1",
            displayName = "Test",
            email = "test@x.com"
        )
        assertEquals(UserRole.CLIENT, profile.role)
    }
}