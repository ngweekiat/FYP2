import com.example.fyp_androidapp.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthRepositoryTest {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authRepository: AuthRepository

    @BeforeEach
    fun setUp() {
        firebaseAuth = mockk()
        authRepository = AuthRepository(firebaseAuth)
    }

    @Test
    fun `getCurrentUser returns mock user when signed in`() {
        // Arrange
        val mockUser = mockk<FirebaseUser>()
        every { firebaseAuth.currentUser } returns mockUser

        // Act
        val currentUser = authRepository.getCurrentUser()

        // Assert
        assertNotNull(currentUser)
        assertEquals(mockUser, currentUser)
    }
}
