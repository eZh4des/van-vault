package com.vanvault.android.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitarios para [UserRole].
 *
 * Cubre:
 *  - [UserRole.fromEmail] en todos los casos (admin, cliente, mayusculas, vacio)
 *  - Display names y short labels consistentes
 *  - Constante [UserRole.PLATFORM_ADMIN_EMAIL] bien definida
 *
 * **Pragmatic tip**: testear primero las reglas de negocio (regla admin)
 * y luego los detalles (labels). Cubre los caminos importantes primero.
 */
class UserRoleTest {

    @Test
    fun `fromEmail returns ADMIN for the platform admin email`() {
        val role = UserRole.fromEmail(UserRole.PLATFORM_ADMIN_EMAIL)
        assertEquals(UserRole.ADMIN, role)
    }

    @Test
    fun `fromEmail returns ADMIN regardless of email case`() {
        val lower = UserRole.fromEmail("alex.daniel50904@gmail.com")
        val upper = UserRole.fromEmail("ALEX.DANIEL50904@GMAIL.COM")
        val mixed = UserRole.fromEmail("Alex.Daniel50904@Gmail.Com")

        assertEquals(UserRole.ADMIN, lower)
        assertEquals(UserRole.ADMIN, upper)
        assertEquals(UserRole.ADMIN, mixed)
    }

    @Test
    fun `fromEmail returns CLIENT for any other email`() {
        val role1 = UserRole.fromEmail("cliente@ejemplo.com")
        val role2 = UserRole.fromEmail("user@gmail.com")
        val role3 = UserRole.fromEmail("user@outlook.com")

        assertEquals(UserRole.CLIENT, role1)
        assertEquals(UserRole.CLIENT, role2)
        assertEquals(UserRole.CLIENT, role3)
    }

    @Test
    fun `fromEmail returns CLIENT for similar but different email`() {
        // Emails parecidos al admin pero NO iguales -> deben ser CLIENT
        val role1 = UserRole.fromEmail("alex.daniel50905@gmail.com")
        val role2 = UserRole.fromEmail("alex.daniel50904@hotmail.com")
        val role3 = UserRole.fromEmail("alex.daniel50904")

        assertEquals(UserRole.CLIENT, role1)
        assertEquals(UserRole.CLIENT, role2)
        assertEquals(UserRole.CLIENT, role3)
    }

    @Test
    fun `fromEmail returns CLIENT for empty string`() {
        val role = UserRole.fromEmail("")
        assertEquals(UserRole.CLIENT, role)
    }

    @Test
    fun `ADMIN displayName is Administrador`() {
        assertEquals("Administrador", UserRole.ADMIN.displayName)
    }

    @Test
    fun `CLIENT displayName is Cliente`() {
        assertEquals("Cliente", UserRole.CLIENT.displayName)
    }

    @Test
    fun `shortLabels are non-empty for both roles`() {
        assertTrue(UserRole.ADMIN.shortLabel.isNotBlank())
        assertTrue(UserRole.CLIENT.shortLabel.isNotBlank())
    }

    @Test
    fun `platform admin email constant matches expected value`() {
        // Proteccion contra cambios accidentales: si alguien lo cambia,
        // debe actualizar este test tambien (intencional)
        assertEquals("alex.daniel50904@gmail.com", UserRole.PLATFORM_ADMIN_EMAIL)
    }

    @Test
    fun `role enum has exactly 2 values`() {
        // Si agregamos un nuevo rol, este test nos obliga a actualizarlo.
        assertEquals(2, UserRole.entries.size)
    }
}