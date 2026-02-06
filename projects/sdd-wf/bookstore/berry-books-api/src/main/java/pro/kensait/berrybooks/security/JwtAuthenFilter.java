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
 * 
 * リクエストからJWTトークンを抽出し、検証する。
 * 認証成功時はAuthenInfoに認証情報を設定する。
 * 
 * @since 1.0.0
 */
@WebFilter(urlPatterns = "/api/*")
public class JwtAuthenFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenFilter.class);
    
    private static final String COOKIE_NAME = "berry_auth";
    
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
        
        logger.info("[ JwtAuthenFilter#doFilter ] path={}", path);
        
        // 認証除外パスのチェック
        if (isPublicPath(path)) {
            logger.info("[ JwtAuthenFilter#doFilter ] Public path, skipping authentication");
            chain.doFilter(request, response);
            return;
        }
        
        try {
            // CookieからJWTトークンを抽出
            String token = extractTokenFromCookie(httpRequest);
            
            if (token != null && jwtUtil.validateToken(token)) {
                // JWTから認証情報を取得
                Integer customerId = jwtUtil.getCustomerIdFromToken(token);
                String email = jwtUtil.getEmailFromToken(token);
                
                // AuthenInfoに認証情報を設定
                authenInfo.setCustomerId(customerId);
                authenInfo.setEmail(email);
                
                logger.info("[ JwtAuthenFilter#doFilter ] Authentication successful: customerId={}", customerId);
                
                // 次のフィルターまたはリソースへ
                chain.doFilter(request, response);
            } else {
                // 認証必須パスで未認証の場合
                if (isSecuredPath(path)) {
                    logger.warn("[ JwtAuthenFilter#doFilter ] Authentication required: path={}", path);
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    httpResponse.setContentType("application/json");
                    httpResponse.getWriter().write("{\"error\":\"認証が必要です\"}");
                } else {
                    // 認証不要パス
                    chain.doFilter(request, response);
                }
            }
        } catch (Exception e) {
            logger.error("[ JwtAuthenFilter#doFilter ] Authentication error: {}", e.getMessage(), e);
            
            if (isSecuredPath(path)) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\":\"認証エラー\"}");
            } else {
                chain.doFilter(request, response);
            }
        }
    }
    
    /**
     * CookieからJWTトークンを抽出する
     * 
     * @param request HTTPリクエスト
     * @return JWTトークン（存在しない場合はnull）
     */
    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * 公開パス（認証不要）かどうかを判定する
     * 
     * @param path リクエストパス
     * @return 公開パスの場合はtrue
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/logout")
                || path.startsWith("/api/auth/register")
                || path.startsWith("/api/books")
                || path.startsWith("/api/categories")
                || path.startsWith("/api/images");
    }
    
    /**
     * 認証必須パスかどうかを判定する
     * 
     * @param path リクエストパス
     * @return 認証必須パスの場合はtrue
     */
    private boolean isSecuredPath(String path) {
        return path.startsWith("/api/orders")
                || path.startsWith("/api/auth/me");
    }
}
