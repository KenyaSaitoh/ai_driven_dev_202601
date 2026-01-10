package pro.kensait.berrybooks.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.core.Response;
import pro.kensait.berrybooks.api.dto.LoginRequest;
import pro.kensait.berrybooks.api.dto.LoginResponse;
import pro.kensait.berrybooks.api.dto.RegisterRequest;
import pro.kensait.berrybooks.external.CustomerHubRestClient;
import pro.kensait.berrybooks.external.dto.CustomerTO;
import pro.kensait.berrybooks.security.JwtUtil;
import pro.kensait.berrybooks.security.AuthenContext;
import pro.kensait.berrybooks.service.exception.EmailAlreadyExistsException;

/**
 * AuthenResourceの単体テスト
 */
@ExtendWith(MockitoExtension.class)
class AuthenResourceTest {

    @Mock
    private CustomerHubRestClient customerHubClient;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenContext authenContext;

    @InjectMocks
    private AuthenResource authenResource;

    private CustomerTO testCustomer;

    @BeforeEach
    void setUp() {
        // テストデータの準備
        testCustomer = new CustomerTO(
            1,
            "Alice",
            "password", // 平文パスワード
            "alice@gmail.com",
            LocalDate.of(1990, 1, 1),
            "東京都渋谷区1-2-3"
        );
    }

    @Test
    @DisplayName("ログイン成功（平文パスワード）")
    void testLogin_Success_PlainPassword() {
        // Given
        LoginRequest request = new LoginRequest("alice@gmail.com", "password");
        when(customerHubClient.findByEmail("alice@gmail.com")).thenReturn(testCustomer);
        when(jwtUtil.generateToken(1, "alice@gmail.com")).thenReturn("test-jwt-token");

        // When
        Response response = authenResource.login(request);

        // Then
        assertEquals(200, response.getStatus());
        LoginResponse loginResponse = (LoginResponse) response.getEntity();
        assertNotNull(loginResponse);
        assertEquals(1, loginResponse.customerId());
        assertEquals("Alice", loginResponse.customerName());
        assertEquals("alice@gmail.com", loginResponse.email());
        
        verify(customerHubClient, times(1)).findByEmail("alice@gmail.com");
        verify(jwtUtil, times(1)).generateToken(1, "alice@gmail.com");
    }

    @Test
    @DisplayName("ログイン成功（BCryptパスワード）")
    void testLogin_Success_BCryptPassword() {
        // Given
        String hashedPassword = BCrypt.hashpw("password", BCrypt.gensalt());
        CustomerTO customerWithHash = new CustomerTO(
            1,
            "Alice",
            hashedPassword,
            "alice@gmail.com",
            LocalDate.of(1990, 1, 1),
            "東京都渋谷区1-2-3"
        );
        
        LoginRequest request = new LoginRequest("alice@gmail.com", "password");
        when(customerHubClient.findByEmail("alice@gmail.com")).thenReturn(customerWithHash);
        when(jwtUtil.generateToken(1, "alice@gmail.com")).thenReturn("test-jwt-token");

        // When
        Response response = authenResource.login(request);

        // Then
        assertEquals(200, response.getStatus());
        LoginResponse loginResponse = (LoginResponse) response.getEntity();
        assertNotNull(loginResponse);
        assertEquals(1, loginResponse.customerId());
        
        verify(customerHubClient, times(1)).findByEmail("alice@gmail.com");
        verify(jwtUtil, times(1)).generateToken(1, "alice@gmail.com");
    }

    @Test
    @DisplayName("ログイン失敗（メールアドレス不存在）")
    void testLogin_Failure_EmailNotFound() {
        // Given
        LoginRequest request = new LoginRequest("unknown@gmail.com", "password");
        when(customerHubClient.findByEmail("unknown@gmail.com")).thenReturn(null);

        // When
        Response response = authenResource.login(request);

        // Then
        assertEquals(401, response.getStatus());
        
        verify(customerHubClient, times(1)).findByEmail("unknown@gmail.com");
        verify(jwtUtil, never()).generateToken(anyInt(), anyString());
    }

    @Test
    @DisplayName("ログイン失敗（パスワード不一致）")
    void testLogin_Failure_PasswordMismatch() {
        // Given
        LoginRequest request = new LoginRequest("alice@gmail.com", "wrong-password");
        when(customerHubClient.findByEmail("alice@gmail.com")).thenReturn(testCustomer);

        // When
        Response response = authenResource.login(request);

        // Then
        assertEquals(401, response.getStatus());
        
        verify(customerHubClient, times(1)).findByEmail("alice@gmail.com");
        verify(jwtUtil, never()).generateToken(anyInt(), anyString());
    }

    @Test
    @DisplayName("ログアウト成功")
    void testLogout_Success() {
        // When
        Response response = authenResource.logout();

        // Then
        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("新規登録成功（住所が都道府県名から始まる）")
    void testRegister_Success_ValidAddress() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "山田太郎",
            "password123",
            "yamada@example.com",
            LocalDate.of(1990, 1, 1),
            "東京都渋谷区1-2-3"
        );
        
        CustomerTO registeredCustomer = new CustomerTO(
            10,
            "山田太郎",
            BCrypt.hashpw("password123", BCrypt.gensalt()),
            "yamada@example.com",
            LocalDate.of(1990, 1, 1),
            "東京都渋谷区1-2-3"
        );
        
        when(customerHubClient.register(any(CustomerTO.class))).thenReturn(registeredCustomer);
        when(jwtUtil.generateToken(10, "yamada@example.com")).thenReturn("test-jwt-token");

        // When
        Response response = authenResource.register(request);

        // Then
        assertEquals(200, response.getStatus());
        LoginResponse loginResponse = (LoginResponse) response.getEntity();
        assertNotNull(loginResponse);
        assertEquals(10, loginResponse.customerId());
        assertEquals("山田太郎", loginResponse.customerName());
        
        verify(customerHubClient, times(1)).register(any(CustomerTO.class));
        verify(jwtUtil, times(1)).generateToken(10, "yamada@example.com");
    }

    @Test
    @DisplayName("新規登録失敗（住所が都道府県名から始まらない）")
    void testRegister_Failure_InvalidAddress() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "山田太郎",
            "password123",
            "yamada@example.com",
            LocalDate.of(1990, 1, 1),
            "渋谷区1-2-3" // 都道府県名がない
        );

        // When
        Response response = authenResource.register(request);

        // Then
        assertEquals(400, response.getStatus());
        
        verify(customerHubClient, never()).register(any(CustomerTO.class));
        verify(jwtUtil, never()).generateToken(anyInt(), anyString());
    }

    @Test
    @DisplayName("新規登録失敗（メールアドレス重複）")
    void testRegister_Failure_EmailAlreadyExists() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "山田太郎",
            "password123",
            "alice@gmail.com",
            LocalDate.of(1990, 1, 1),
            "東京都渋谷区1-2-3"
        );
        
        when(customerHubClient.register(any(CustomerTO.class)))
            .thenThrow(new EmailAlreadyExistsException("alice@gmail.com", "指定されたメールアドレスは既に登録されています"));

        // When
        Response response = authenResource.register(request);

        // Then
        assertEquals(409, response.getStatus());
        
        verify(customerHubClient, times(1)).register(any(CustomerTO.class));
        verify(jwtUtil, never()).generateToken(anyInt(), anyString());
    }

    @Test
    @DisplayName("現在のログインユーザー情報取得成功")
    void testGetCurrentUser_Success() {
        // Given
        when(authenContext.getCustomerId()).thenReturn(1);
        when(customerHubClient.findById(1)).thenReturn(testCustomer);

        // When
        Response response = authenResource.getCurrentUser();

        // Then
        assertEquals(200, response.getStatus());
        LoginResponse loginResponse = (LoginResponse) response.getEntity();
        assertNotNull(loginResponse);
        assertEquals(1, loginResponse.customerId());
        assertEquals("Alice", loginResponse.customerName());
        
        verify(authenContext, times(1)).getCustomerId();
        verify(customerHubClient, times(1)).findById(1);
    }

    @Test
    @DisplayName("現在のログインユーザー情報取得失敗（顧客が見つからない）")
    void testGetCurrentUser_Failure_NotFound() {
        // Given
        when(authenContext.getCustomerId()).thenReturn(999);
        when(customerHubClient.findById(999)).thenReturn(null);

        // When
        Response response = authenResource.getCurrentUser();

        // Then
        assertEquals(404, response.getStatus());
        
        verify(authenContext, times(1)).getCustomerId();
        verify(customerHubClient, times(1)).findById(999);
    }
}
