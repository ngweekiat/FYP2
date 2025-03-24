import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.google.firebase.auth.FirebaseUser
import com.example.fyp_androidapp.viewmodel.AuthViewModel
import com.example.fyp_androidapp.data.repository.AuthRepository
import io.mockk.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @BeforeEach
    fun setUp() {
        // 🔧 Setup the Main dispatcher
        Dispatchers.setMain(testDispatcher)

        authRepository = mockk()
        every { authRepository.getCurrentUser() } returns null

        viewModel = AuthViewModel(authRepository)
    }

    @AfterEach
    fun tearDown() {
        // 🔧 Reset to avoid affecting other tests
        Dispatchers.resetMain()
    }

    @Test
    fun `signInWithGoogle with valid token adds user to accounts list and triggers backend`() = runTest(testDispatcher) {
        val mockUser = mockk<FirebaseUser>()
        every { mockUser.uid } returns "123"
        every { mockUser.email } returns "test@example.com"
        every { mockUser.displayName } returns "Test User"

        coEvery { authRepository.signInWithGoogle("valid_token") } returns mockUser
        every { authRepository.sendTokenToBackend(any(), any(), any(), any(), any()) } just Runs

        viewModel.signInWithGoogle("valid_token", "auth_code")
        advanceUntilIdle()

        val accounts = viewModel.accounts.value
        assertEquals(1, accounts.size)
        assertEquals("123", accounts[0].uid)

        verify { authRepository.sendTokenToBackend("123", "test@example.com", "Test User", "valid_token", "auth_code") }
    }

    @Test
    fun `signInWithGoogle with null token does not add user`() = runTest(testDispatcher) {
        coEvery { authRepository.signInWithGoogle(null) } returns null

        viewModel.signInWithGoogle(null, "auth_code")
        advanceUntilIdle()

        assertTrue(viewModel.accounts.value.isEmpty())
        coVerify { authRepository.signInWithGoogle(null) }
    }

    @Test
    fun `signOut removes user at specified index`() = runTest(testDispatcher) {
        val mockUser = mockk<FirebaseUser>()
        every { mockUser.uid } returns "456"
        every { authRepository.getCurrentUser() } returns mockUser
        coEvery { authRepository.signOut() } just Runs

        val field = AuthViewModel::class.java.getDeclaredField("_accounts")
        field.isAccessible = true
        val accountsFlow = field.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<List<FirebaseUser>>
        accountsFlow.value = listOf(mockUser)

        viewModel.signOut(0)
        advanceUntilIdle()

        assertTrue(viewModel.accounts.value.isEmpty())
        coVerify { authRepository.signOut() }
    }
}
