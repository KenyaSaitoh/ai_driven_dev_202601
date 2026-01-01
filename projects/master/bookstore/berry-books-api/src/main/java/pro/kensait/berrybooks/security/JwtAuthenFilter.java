package pro.kensait.berrybooks.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * JWT認証フィルター
 * CookieからJWTを抽出・検証し、AuthenContextに認証情報を設定する
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenFilter.class);

    @Inject
    private JwtUtil jwtUtil;

    @Inject
    private AuthenContext authenContext;

    @Context
    private HttpServletRequest httpServletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestUri = requestContext.getUriInfo().getPath();
        logger.debug("[ JwtAuthenFilter#filter ] URI: {}", requestUri);

        // 認証不要なパス（公開API）
        if (isPublicPath(requestUri)) {
            logger.debug("[ JwtAuthenFilter ] Public path, skip authentication");
            return;
        }

        try {
            // CookieからJWTを抽出
            String jwt = jwtUtil.extractJwtFromRequest(httpServletRequest);

            if (jwt != null && jwtUtil.validateToken(jwt)) {
                // JWTから顧客情報を取得
                Integer customerId = jwtUtil.getCustomerIdFromToken(jwt);
                String email = jwtUtil.getEmailFromToken(jwt);

                // AuthenContextに認証情報を設定
                authenContext.setCustomerId(customerId);
                authenContext.setEmail(email);

                logger.debug("[ JwtAuthenFilter ] Authenticated customerId: {}, email: {}", 
                        customerId, email);
            } else {
                // JWT認証必須のパスで未認証の場合はエラー
                if (isSecuredPath(requestUri)) {
                    logger.warn("[ JwtAuthenFilter ] Unauthorized access to: {}", requestUri);
                    requestContext.abortWith(
                            Response.status(Response.Status.UNAUTHORIZED)
                                    .entity("{\"error\":\"認証が必要です\"}")
                                    .build()
                    );
                }
            }
        } catch (Exception e) {
            logger.error("[ JwtAuthenFilter ] Authentication error: {}", e.getMessage());
            if (isSecuredPath(requestUri)) {
                requestContext.abortWith(
                        Response.status(Response.Status.UNAUTHORIZED)
                                .entity("{\"error\":\"認証エラー\"}")
                                .build()
                );
            }
        }
    }

    /**
     * 公開パス（認証不要）かどうかを判定
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("auth/login") 
                || path.startsWith("auth/register")
                || path.startsWith("auth/logout")
                || path.startsWith("books")
                || path.startsWith("categories")
                || path.startsWith("images");
    }

    /**
     * 認証必須パスかどうかを判定
     */
    private boolean isSecuredPath(String path) {
        return path.startsWith("orders") 
                || path.startsWith("auth/me");
    }
}

