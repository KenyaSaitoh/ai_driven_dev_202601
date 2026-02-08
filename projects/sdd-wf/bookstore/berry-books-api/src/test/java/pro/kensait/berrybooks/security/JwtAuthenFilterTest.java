package pro.kensait.berrybooks.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

/**
 * JwtAuthenFilter 単体テスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenFilter 単体テスト")
class JwtAuthenFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticatedUser authenticatedUser;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ContainerRequestContext requestContext;

    @Mock
    private UriInfo uriInfo;

    @InjectMocks
    private JwtAuthenFilter jwtAuthenFilter;

    @BeforeEach
    void setUp() {
        // UriInfoのモック設定
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
    }

    @Test
    @DisplayName("認証除外パス: /auth/login")
    void testFilter_PublicPath_AuthLogin() throws IOException {
        // Given: 公開パス（認証不要）
        when(uriInfo.getPath()).thenReturn("auth/login");

        // When
        jwtAuthenFilter.filter(requestContext);

        // Then: 認証チェックがスキップされる
        verify(jwtUtil, never()).extractJwtFromRequest(any());
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    @DisplayName("認証除外パス: /books")
    void testFilter_PublicPath_Books() throws IOException {
        // Given: 公開パス（認証不要）
        when(uriInfo.getPath()).thenReturn("books");

        // When
        jwtAuthenFilter.filter(requestContext);

        // Then: 認証チェックがスキップされる
        verify(jwtUtil, never()).extractJwtFromRequest(any());
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    @DisplayName("認証除外パス: /images/covers/1")
    void testFilter_PublicPath_Images() throws IOException {
        // Given: 公開パス（認証不要）
        when(uriInfo.getPath()).thenReturn("images/covers/1");

        // When
        jwtAuthenFilter.filter(requestContext);

        // Then: 認証チェックがスキップされる
        verify(jwtUtil, never()).extractJwtFromRequest(any());
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    @DisplayName("認証必須パス: 有効なトークンで認証成功")
    void testFilter_SecuredPath_ValidToken() throws IOException {
        // Given: 認証必須パス
        when(uriInfo.getPath()).thenReturn("orders");

        // 有効なJWTトークン
        String validToken = "valid.jwt.token";
        when(jwtUtil.extractJwtFromRequest(httpServletRequest)).thenReturn(validToken);

        // JWTクレーム（モック）
        Claims claims = mock(Claims.class);
        when(claims.get("customerId", Integer.class)).thenReturn(1);
        when(claims.get("customerName", String.class)).thenReturn("山田太郎");
        when(jwtUtil.validateToken(validToken)).thenReturn(claims);

        // When
        jwtAuthenFilter.filter(requestContext);

        // Then: 認証情報がAuthenticatedUserに設定される
        verify(jwtUtil, times(1)).extractJwtFromRequest(httpServletRequest);
        verify(jwtUtil, times(1)).validateToken(validToken);
        verify(authenticatedUser, times(1)).setCustomerId(1);
        verify(authenticatedUser, times(1)).setCustomerName("山田太郎");
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    @DisplayName("認証必須パス: トークンなしで401エラー")
    void testFilter_SecuredPath_NoToken() throws IOException {
        // Given: 認証必須パス
        when(uriInfo.getPath()).thenReturn("orders");

        // トークンなし
        when(jwtUtil.extractJwtFromRequest(httpServletRequest)).thenReturn(null);

        // When
        jwtAuthenFilter.filter(requestContext);

        // Then: 401 Unauthorizedが返される
        verify(requestContext, times(1)).abortWith(argThat(response ->
            response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()
        ));
    }

    @Test
    @DisplayName("認証必須パス: 無効なトークンで401エラー")
    void testFilter_SecuredPath_InvalidToken() throws IOException {
        // Given: 認証必須パス
        when(uriInfo.getPath()).thenReturn("orders");

        // 無効なJWTトークン
        String invalidToken = "invalid.jwt.token";
        when(jwtUtil.extractJwtFromRequest(httpServletRequest)).thenReturn(invalidToken);
        when(jwtUtil.validateToken(invalidToken)).thenThrow(new JwtException("Invalid token"));

        // When
        jwtAuthenFilter.filter(requestContext);

        // Then: 401 Unauthorizedが返される
        verify(requestContext, times(1)).abortWith(argThat(response ->
            response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()
        ));
    }

    @Test
    @DisplayName("認証必須パス: /auth/me")
    void testFilter_SecuredPath_AuthMe_NoToken() throws IOException {
        // Given: 認証必須パス
        when(uriInfo.getPath()).thenReturn("auth/me");

        // トークンなし
        when(jwtUtil.extractJwtFromRequest(httpServletRequest)).thenReturn(null);

        // When
        jwtAuthenFilter.filter(requestContext);

        // Then: 401 Unauthorizedが返される
        verify(requestContext, times(1)).abortWith(argThat(response ->
            response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()
        ));
    }

    @Test
    @DisplayName("認証除外パス: /auth/logout")
    void testFilter_PublicPath_AuthLogout() throws IOException {
        // Given: 公開パス（認証不要）
        when(uriInfo.getPath()).thenReturn("auth/logout");

        // When
        jwtAuthenFilter.filter(requestContext);

        // Then: 認証チェックがスキップされる
        verify(jwtUtil, never()).extractJwtFromRequest(any());
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    @DisplayName("認証除外パス: /auth/register")
    void testFilter_PublicPath_AuthRegister() throws IOException {
        // Given: 公開パス（認証不要）
        when(uriInfo.getPath()).thenReturn("auth/register");

        // When
        jwtAuthenFilter.filter(requestContext);

        // Then: 認証チェックがスキップされる
        verify(jwtUtil, never()).extractJwtFromRequest(any());
        verify(requestContext, never()).abortWith(any());
    }
}
