package pro.kensait.berrybooks.security;

import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT認証フィルター
 * 
 * JWT Cookieを検証し、認証情報をSecuredResourceにセットする
 */
@WebFilter(urlPatterns = "/api/*")
public class JwtAuthenticationFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Inject
    private JwtUtil jwtUtil;
    
    @Inject
    private SecuredResource securedResource;
    
    @Inject
    @ConfigProperty(name = "jwt.cookie-name", defaultValue = "berry-books-jwt")
    private String cookieName;
    
    // 認証不要のエンドポイント
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
        "/api/auth/login",
        "/api/auth/logout",
        "/api/auth/register",
        "/api/books",
        "/api/images"
    );
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        
        // コンテキストパスを除いたパスを取得
        String path = requestURI.substring(contextPath.length());
        
        logger.debug("[ JwtAuthenticationFilter#doFilter ] Request URI: {}, Context Path: {}, Path: {}", 
                     requestURI, contextPath, path);
        
        // 認証不要のエンドポイントはスキップ
        if (isPublicEndpoint(path)) {
            logger.debug("[ JwtAuthenticationFilter#doFilter ] Public endpoint, skipping authentication");
            chain.doFilter(request, response);
            return;
        }
        
        // JWT Cookieを取得
        Cookie[] cookies = httpRequest.getCookies();
        String token = null;
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        
        // トークンが存在しない場合
        if (token == null || token.isEmpty()) {
            logger.warn("[ JwtAuthenticationFilter#doFilter ] JWT token not found");
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "認証が必要です");
            return;
        }
        
        // トークンを検証
        if (!jwtUtil.validateToken(token)) {
            logger.warn("[ JwtAuthenticationFilter#doFilter ] Invalid JWT token");
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "認証が必要です");
            return;
        }
        
        // トークンから顧客情報を取得してSecuredResourceにセット
        Integer customerId = jwtUtil.getCustomerIdFromToken(token);
        String email = jwtUtil.getEmailFromToken(token);
        
        securedResource.setCustomerId(customerId);
        securedResource.setEmail(email);
        
        logger.debug("[ JwtAuthenticationFilter#doFilter ] Authentication successful: customerId={}", customerId);
        
        chain.doFilter(request, response);
    }
    
    /**
     * 認証不要のエンドポイントかチェック
     * 
     * @param requestURI リクエストURI
     * @return 認証不要の場合true
     */
    private boolean isPublicEndpoint(String requestURI) {
        for (String endpoint : PUBLIC_ENDPOINTS) {
            if (requestURI.startsWith(endpoint)) {
                return true;
            }
        }
        return false;
    }
}
