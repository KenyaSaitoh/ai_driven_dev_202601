package pro.kensait.backoffice.api;

import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.backoffice.api.dto.LoginRequest;
import pro.kensait.backoffice.api.dto.LoginResponse;
import pro.kensait.backoffice.dao.EmployeeDao;
import pro.kensait.backoffice.entity.Department;
import pro.kensait.backoffice.entity.Employee;
import pro.kensait.backoffice.security.JwtUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AuthenResourceの単体テスト
 */
@ExtendWith(MockitoExtension.class)
class AuthenResourceTest {

    @Mock
    private EmployeeDao employeeDao;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthenResource authenResource;

    private Employee testEmployee;
    private Department testDepartment;

    @BeforeEach
    void setUp() {
        // テスト用部署データ
        testDepartment = new Department(1L, "営業部");

        // テスト用社員データ
        testEmployee = new Employee(
            1L,
            "E0001",
            "山田太郎",
            "yamada@example.com",
            BCrypt.hashpw("password123", BCrypt.gensalt()),
            2,
            testDepartment
        );
    }

    /**
     * テスト: ログイン成功（正常系）
     */
    @Test
    void testLogin_Success() {
        // Given
        LoginRequest request = new LoginRequest("E0001", "password123");
        
        when(employeeDao.findByCode("E0001")).thenReturn(testEmployee);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyLong())).thenReturn("mock-jwt-token");
        when(jwtUtil.getCookieName()).thenReturn("back-office-jwt");
        when(jwtUtil.getExpirationSeconds()).thenReturn(86400);

        // When
        Response response = authenResource.login(request);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        LoginResponse loginResponse = (LoginResponse) response.getEntity();
        assertNotNull(loginResponse);
        assertEquals(1L, loginResponse.employeeId());
        assertEquals("E0001", loginResponse.employeeCode());
        assertEquals("山田太郎", loginResponse.employeeName());
        assertEquals("yamada@example.com", loginResponse.email());
        assertEquals(2, loginResponse.jobRank());
        assertEquals(1L, loginResponse.departmentId());
        assertEquals("営業部", loginResponse.departmentName());

        // Cookie確認
        NewCookie cookie = response.getCookies().get("back-office-jwt");
        assertNotNull(cookie);
        assertEquals("mock-jwt-token", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertEquals(86400, cookie.getMaxAge());

        // モックの呼び出し確認
        verify(employeeDao, times(1)).findByCode("E0001");
        verify(jwtUtil, times(1)).generateToken(1L, "E0001", 1L);
    }

    /**
     * テスト: ログイン失敗（社員コードが存在しない）
     */
    @Test
    void testLogin_InvalidEmployeeCode() {
        // Given
        LoginRequest request = new LoginRequest("INVALID", "password123");
        
        when(employeeDao.findByCode("INVALID")).thenReturn(null);

        // When
        Response response = authenResource.login(request);

        // Then
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());

        // モックの呼び出し確認
        verify(employeeDao, times(1)).findByCode("INVALID");
        verify(jwtUtil, never()).generateToken(anyLong(), anyString(), anyLong());
    }

    /**
     * テスト: ログイン失敗（パスワードが一致しない）
     */
    @Test
    void testLogin_InvalidPassword() {
        // Given
        LoginRequest request = new LoginRequest("E0001", "wrongpassword");
        
        when(employeeDao.findByCode("E0001")).thenReturn(testEmployee);

        // When
        Response response = authenResource.login(request);

        // Then
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());

        // モックの呼び出し確認
        verify(employeeDao, times(1)).findByCode("E0001");
        verify(jwtUtil, never()).generateToken(anyLong(), anyString(), anyLong());
    }

    /**
     * テスト: ログイン成功（平文パスワード）
     */
    @Test
    void testLogin_PlainTextPassword() {
        // Given
        Employee plainPasswordEmployee = new Employee(
            1L,
            "E0001",
            "山田太郎",
            "yamada@example.com",
            "password123",  // 平文パスワード
            2,
            testDepartment
        );
        
        LoginRequest request = new LoginRequest("E0001", "password123");
        
        when(employeeDao.findByCode("E0001")).thenReturn(plainPasswordEmployee);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyLong())).thenReturn("mock-jwt-token");
        when(jwtUtil.getCookieName()).thenReturn("back-office-jwt");
        when(jwtUtil.getExpirationSeconds()).thenReturn(86400);

        // When
        Response response = authenResource.login(request);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        LoginResponse loginResponse = (LoginResponse) response.getEntity();
        assertNotNull(loginResponse);
        assertEquals("E0001", loginResponse.employeeCode());

        // モックの呼び出し確認
        verify(employeeDao, times(1)).findByCode("E0001");
        verify(jwtUtil, times(1)).generateToken(1L, "E0001", 1L);
    }

    /**
     * テスト: ログアウト成功
     */
    @Test
    void testLogout() {
        // Given
        when(jwtUtil.getCookieName()).thenReturn("back-office-jwt");

        // When
        Response response = authenResource.logout();

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        // Cookie削除確認（MaxAge=0）
        NewCookie cookie = response.getCookies().get("back-office-jwt");
        assertNotNull(cookie);
        assertEquals("", cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
        assertTrue(cookie.isHttpOnly());
    }

    /**
     * テスト: 現在のユーザー情報取得（未実装）
     */
    @Test
    void testGetCurrentUser_NotImplemented() {
        // When
        Response response = authenResource.getCurrentUser();

        // Then
        assertEquals(Response.Status.NOT_IMPLEMENTED.getStatusCode(), response.getStatus());
    }
}
