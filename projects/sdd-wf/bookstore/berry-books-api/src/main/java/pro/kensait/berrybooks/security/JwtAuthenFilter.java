package pro.kensait.berrybooks.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

/**
 * JWT認証フィルター
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenFilter.class);
    
    @Inject
    private JwtUtil jwtUtil;
    
    @Inject
    private AuthenticatedUser authenticatedUser;
    
    @Context
    private HttpServletRequest httpServletRequest;
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestUri = requestContext.getUriInfo().getPath();
        logger.debug("[ JwtAuthenFilter#filter ] Request URI: {}", requestUri);
        
        // 認証不要なパス（公開API）
        if (isPublicPath(requestUri)) {
            logger.debug("[ JwtAuthenFilter#filter ] Public path, skipping authentication");
            return;
        }
        
        try {
            // CookieからJWTを抽出
            String jwt = jwtUtil.extractJwtFromRequest(httpServletRequest);
            
            if (jwt != null) {
                // JWTを検証
                Claims claims = jwtUtil.validateToken(jwt);
                
                // 認証情報をAuthenticatedUserに設定
                Integer customerId = claims.get("customerId", Integer.class);
                String customerName = claims.get("customerName", String.class);
                
                authenticatedUser.setCustomerId(customerId);
                authenticatedUser.setCustomerName(customerName);
                
                logger.info("[ JwtAuthenFilter#filter ] Authentication successful: customerId={}, customerName={}", 
                    customerId, customerName);
            } else {
                // JWT認証必須のパスで未認証の場合はエラー
                if (isSecuredPath(requestUri)) {
                    logger.warn("[ JwtAuthenFilter#filter ] Authentication required but JWT not found");
                    requestContext.abortWith(
                        Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{\"error\":\"認証が必要です\"}")
                            .type("application/json")
                            .build()
                    );
                }
            }
        } catch (JwtException e) {
            logger.error("[ JwtAuthenFilter#filter ] Authentication error: {}", e.getMessage());
            if (isSecuredPath(requestUri)) {
                requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"認証エラー\"}")
                        .type("application/json")
                        .build()
                );
            }
        }
    }
    
    /**
     * 公開パス（認証不要）かどうかを判定
     * 
     * @param path リクエストパス
     * @return 公開パスの場合true
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("auth/login") 
                || path.startsWith("auth/logout")
                || path.startsWith("auth/register")
                || path.startsWith("books")
                || path.startsWith("images");
    }
    
    /**
     * 認証必須パスかどうかを判定
     * 
     * @param path リクエストパス
     * @return 認証必須パスの場合true
     */
    private boolean isSecuredPath(String path) {
        return path.startsWith("orders") 
                || path.startsWith("auth/me");
    }
}