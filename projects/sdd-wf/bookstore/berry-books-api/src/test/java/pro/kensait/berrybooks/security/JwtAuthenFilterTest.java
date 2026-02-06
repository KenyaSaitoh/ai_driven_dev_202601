package pro.kensait.berrybooks.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenFilter単体テスト")
class JwtAuthenFilterTest {
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private AuthenInfo authenInfo;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain chain;
    
    @Mock
    private PrintWriter writer;
    
    @InjectMocks
    private JwtAuthenFilter filter;
    
    @BeforeEach
    void setUp() throws IOException {
        // 共通モック設定（lenient: 使用されない場合でもエラーにしない）
        lenient().when(response.getWriter()).thenReturn(writer);
    }
    
    @Test
    @DisplayName("有効なJWTトークンで認証成功（正常系）")
    void testDoFilter_ValidToken_Success() throws IOException, ServletException {
        // Given: 有効なJWTトークン
        String validToken = "valid.jwt.token";
        Cookie[] cookies = {new Cookie("berry_auth", validToken)};
        
        when(request.getRequestURI()).thenReturn("/berry-books-api/api/orders");
        when(request.getContextPath()).thenReturn("/berry-books-api");
        when(request.getCookies()).thenReturn(cookies);
        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getCustomerIdFromToken(validToken)).thenReturn(1);
        when(jwtUtil.getEmailFromToken(validToken)).thenReturn("test@example.com");
        
        // When: フィルター実行
        filter.doFilter(request, response, chain);
        
        // Then: 検証
        verify(authenInfo).setCustomerId(1);
        verify(authenInfo).setEmail("test@example.com");
        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
    
    @Test
    @DisplayName("JWTトークンが存在しない（異常系）")
    void testDoFilter_NoToken() throws IOException, ServletException {
        // Given: JWTトークンなし
        when(request.getRequestURI()).thenReturn("/berry-books-api/api/orders");
        when(request.getContextPath()).thenReturn("/berry-books-api");
        when(request.getCookies()).thenReturn(null);
        
        // When: フィルター実行
        filter.doFilter(request, response, chain);
        
        // Then: 検証
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json;charset=UTF-8");
        verify(writer).write(anyString());
        verify(chain, never()).doFilter(request, response);
    }
    
    @Test
    @DisplayName("無効なJWTトークン（異常系）")
    void testDoFilter_InvalidToken() throws IOException, ServletException {
        // Given: 無効なJWTトークン
        String invalidToken = "invalid.jwt.token";
        Cookie[] cookies = {new Cookie("berry_auth", invalidToken)};
        
        when(request.getRequestURI()).thenReturn("/berry-books-api/api/orders");
        when(request.getContextPath()).thenReturn("/berry-books-api");
        when(request.getCookies()).thenReturn(cookies);
        when(jwtUtil.validateToken(invalidToken)).thenReturn(false);
        
        // When: フィルター実行
        filter.doFilter(request, response, chain);
        
        // Then: 検証
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json;charset=UTF-8");
        verify(writer).write(anyString());
        verify(chain, never()).doFilter(request, response);
    }
    
    @Test
    @DisplayName("認証除外パスへのアクセス（正常系）")
    void testDoFilter_PublicPath_LoginEndpoint() throws IOException, ServletException {
        // Given: 認証除外パス（/api/auth/login）
        when(request.getRequestURI()).thenReturn("/berry-books-api/api/auth/login");
        when(request.getContextPath()).thenReturn("/berry-books-api");
        
        // When: フィルター実行
        filter.doFilter(request, response, chain);
        
        // Then: 検証（認証処理がスキップされる）
        verify(chain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
    
    @Test
    @DisplayName("認証除外パスへのアクセス（/api/books）")
    void testDoFilter_PublicPath_BooksEndpoint() throws IOException, ServletException {
        // Given: 認証除外パス（/api/books）
        when(request.getRequestURI()).thenReturn("/berry-books-api/api/books");
        when(request.getContextPath()).thenReturn("/berry-books-api");
        
        // When: フィルター実行
        filter.doFilter(request, response, chain);
        
        // Then: 検証（認証処理がスキップされる）
        verify(chain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
    
    @Test
    @DisplayName("認証除外パスへのアクセス（/api/images）")
    void testDoFilter_PublicPath_ImagesEndpoint() throws IOException, ServletException {
        // Given: 認証除外パス（/api/images）
        when(request.getRequestURI()).thenReturn("/berry-books-api/api/images/covers/1");
        when(request.getContextPath()).thenReturn("/berry-books-api");
        
        // When: フィルター実行
        filter.doFilter(request, response, chain);
        
        // Then: 検証（認証処理がスキップされる）
        verify(chain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
