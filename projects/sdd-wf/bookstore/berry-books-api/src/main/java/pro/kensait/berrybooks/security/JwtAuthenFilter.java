package pro.kensait.berrybooks.security;

import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * JWT認証フィルター
 */
@WebFilter(urlPatterns = "/api/*")
public class JwtAuthenFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenFilter.class);
    
    private static final String JWT_COOKIE_NAME = "berry_auth";
    
    @Inject
    private JwtUtil jwtUtil;
    
    @Inject
    private AuthenInfo authenInfo;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestUri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestUri.substring(contextPath.length());
        
        logger.debug("[ JwtAuthenFilter#doFilter ] Processing request: {}", path);
        
        // 認証除外パスの判定
        if (isPublicPath(path)) {
            logger.debug("[ JwtAuthenFilter#doFilter ] Public path, skipping authentication");
            chain.doFilter(request, response);
            return;
        }
        
        // CookieからJWTを取得
        String jwt = extractJwtFromCookie(httpRequest);
        
        if (jwt == null) {
            logger.warn("[ JwtAuthenFilter#doFilter ] JWT not found in cookie for secured path: {}", path);
            sendUnauthorizedResponse(httpResponse, "認証が必要です");
            return;
        }
        
        // JWT検証
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("[ JwtAuthenFilter#doFilter ] Invalid JWT token for path: {}", path);
            sendUnauthorizedResponse(httpResponse, "認証トークンが無効です");
            return;
        }
        
        try {
            // JWTから認証情報を取得してAuthenInfoに設定
            Integer customerId = jwtUtil.getCustomerIdFromToken(jwt);
            String email = jwtUtil.getEmailFromToken(jwt);
            
            authenInfo.setCustomerId(customerId);
            authenInfo.setEmail(email);
            
            logger.debug("[ JwtAuthenFilter#doFilter ] Authentication successful: customerId={}, email={}", customerId, email);
            
            chain.doFilter(request, response);
            
        } catch (Exception e) {
            logger.error("[ JwtAuthenFilter#doFilter ] Authentication error: {}", e.getMessage());
            sendUnauthorizedResponse(httpResponse, "認証エラーが発生しました");
        }
    }
    
    /**
     * 認証除外パスかどうかを判定
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/logout") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/books") ||
               path.startsWith("/api/categories") ||
               path.startsWith("/api/images");
    }
    
    /**
     * CookieからJWTを抽出
     */
    private String extractJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    /**
     * 401 Unauthorizedレスポンスを送信
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"" + message + "\"}");
    }
}
