package com.vanvault.android.domain.usecase

import com.vanvault.android.domain.model.UserProfile
import com.vanvault.android.domain.model.UserRole
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests para [RegisterUseCase] — usa [FakeUserRepository].
 *
 * Cubre:
 *  - Asignacion automatica de role segun email (admin vs cliente)
 *  - Validacion de email vacio / formato invalido
 *  - Validacion de contrasena (vacia, <6 chars, no coinciden)
 *  - Validacion de nombre (vacio, <3 chars)
 *  - Propagacion de errores del repo
 */
class RegisterUseCaseTest {

    private lateinit var fake: FakeUserRepository
    private lateinit var useCase: RegisterUseCase

    @Before
    fun setUp() {
        fake = FakeUserRepository()
        useCase = RegisterUseCase(fake)
    }

    // ----- Asignacion de role -----

    @Test
    fun `admin email gets ADMIN role assigned`() = runBlocking {
        val result = useCase(
            name = "Alex",
            email = UserRole.PLATFORM_ADMIN_EMAIL,
            password = "Password1",
            confirmPassword = "Password1"
        )

        assertTrue(result.isSuccess)
        assertEquals(UserRole.ADMIN, fake.lastRegisteredRole)
    }

    @Test
    fun `regular email gets CLIENT role assigned`() = runBlocking {
        useCase(
            name = "Maria",
            email = "maria@ejemplo.com",
            password = "Password1",
            confirmPassword = "Password1"
        )

        assertEquals(UserRole.CLIENT, fake.lastRegisteredRole)
    }

    @Test
    fun `email is lowercased before role resolution`() = runBlocking {
        useCase(
            name = "Test",
            email = "ALEX.DANIEL50904@GMAIL.COM",
            password = "Password1",
            confirmPassword = "Password1"
        )

        assertEquals(UserRole.ADMIN, fake.lastRegisteredRole)
    }

    // ----- Validaciones de inputs -----

    @Test
    fun `empty name returns failure without calling repo`() = runBlocking {
        val result = useCase(
            name = "",
            email = "user@x.com",
            password = "Password1",
            confirmPassword = "Password1"
        )

        assertTrue(result.isFailure)
        assertNull(fake.lastRegisteredEmail)  // repo no se llamo
    }

    @Test
    fun `name shorter than 3 chars returns failure`() = runBlocking {
        val result = useCase(
            name = "Ab",
            email = "user@x.com",
            password = "Password1",
            confirmPassword = "Password1"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `empty email returns failure`() = runBlocking {
        val result = useCase(
            name = "Test User",
            email = "",
            password = "Password1",
            confirmPassword = "Password1"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `invalid email format returns failure`() = runBlocking {
        val result = useCase(
            name = "Test User",
            email = "not-an-email",
            password = "Password1",
            confirmPassword = "Password1"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `passwords mismatch returns failure`() = runBlocking {
        val result = useCase(
            name = "Test User",
            email = "user@x.com",
            password = "Password1",
            confirmPassword = "Password2"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `password shorter than 8 chars returns failure`() = runBlocking {
        val result = useCase(
            name = "Test User",
            email = "user@x.com",
            password = "Pass1a",      // 6 chars
            confirmPassword = "Pass1a"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `password without uppercase returns failure`() = runBlocking {
        val result = useCase(
            name = "Test User",
            email = "user@x.com",
            password = "password1",    // 9 chars pero sin mayuscula
            confirmPassword = "password1"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `password without digit returns failure`() = runBlocking {
        val result = useCase(
            name = "Test User",
            email = "user@x.com",
            password = "Password",    // sin numero
            confirmPassword = "Password"
        )

        assertTrue(result.isFailure)
    }

    // ----- Propagacion de datos al repo -----

    @Test
    fun `valid input forwards trimmed data to repo`() = runBlocking {
        useCase(
            name = "  Carlos Ramirez  ",
            email = "  carlos@vanvault.co  ",
            password = "Password1",
            confirmPassword = "Password1",
            phone = "  3001234567  "
        )

        assertEquals("Carlos Ramirez", fake.lastRegisteredDisplayName)
        assertEquals("carlos@vanvault.co", fake.lastRegisteredEmail)
        assertEquals("3001234567", fake.lastRegisteredPhone)
    }

    @Test
    fun `empty phone is forwarded as null`() = runBlocking {
        useCase(
            name = "Test User",
            email = "user@x.com",
            password = "Password1",
            confirmPassword = "Password1",
            phone = ""     // vacio
        )

        assertNull(fake.lastRegisteredPhone)
    }

    // ----- Propagacion de errores del repo -----

    @Test
    fun `repo failure is propagated to caller`() = runBlocking {
        fake.registerResult = Result.failure(
            IllegalStateException("Email ya registrado en Firebase")
        )

        val result = useCase(
            name = "Test User",
            email = "user@x.com",
            password = "Password1",
            confirmPassword = "Password1"
        )

        assertTrue(result.isFailure)
        assertEquals("Email ya registrado en Firebase", result.exceptionOrNull()?.message)
    }

    @Test
    fun `repo success is returned as-is`() = runBlocking {
        val expectedProfile = UserProfile(
            uid = "test-uid",
            displayName = "Test User",
            email = "user@x.com",
            role = UserRole.CLIENT
        )
        fake.registerResult = Result.success(expectedProfile)

        val result = useCase(
            name = "Test User",
            email = "user@x.com",
            password = "Password1",
            confirmPassword = "Password1"
        )

        assertTrue(result.isSuccess)
        assertEquals(expectedProfile, result.getOrNull())
    }
}
