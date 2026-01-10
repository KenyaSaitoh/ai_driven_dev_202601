package pro.kensait.berrybooks.security;

import java.io.IOException;
import java.util.List;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.berrybooks.api.dto.ErrorResponse;

/**
 * JWT認証フィルター
 * 
 * HTTPリクエストからJWT Cookieを抽出し、検証する。
 * 認証必須エンドポイントでJWTが無効な場合、401エラーを返す。
 */
@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenFilter.class);

    @Inject
    private JwtUtil jwtUtil;

    @Inject
    private AuthenContext authenContext;

    private String cookieName;

    @PostConstruct
    public void init() {
        Config config = ConfigProvider.getConfig();
        cookieName = config.getOptionalValue("jwt.cookie-name", String.class)
                .orElse("berry-books-jwt");
        logger.info("JwtAuthenFilter initialized, cookieName: {}", cookieName);
    }

    // 認証除外エンドポイント（JWT不要）
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
        "/api/auth/login",
        "/api/auth/logout",
        "/api/auth/register",
        "/api/books",
        "/api/images",
        "/auth/login",    // コンテキストパス処理後のパス
        "/auth/logout",
        "/auth/register",
        "/books",
        "/images"
    );

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestUri = requestContext.getUriInfo().getRequestUri().getPath();
        String contextPath = requestContext.getUriInfo().getBaseUri().getPath();

        // コンテキストパスを除外したパスを取得
        String path = requestUri;
        if (contextPath != null && !contextPath.equals("/") && requestUri.startsWith(contextPath)) {
            path = requestUri.substring(contextPath.length());
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
        }

        logger.debug("[ JwtAuthenFilter ] requestUri={}, path={}", requestUri, path);

        // 公開エンドポイントの場合は認証をスキップ
        if (isPublicEndpoint(path)) {
            logger.debug("[ JwtAuthenFilter ] Public endpoint, skipping authentication");
            return;
        }

        // JWT Cookieを取得
        String jwt = extractJwtFromRequest(requestContext);

        if (jwt == null) {
            logger.warn("[ JwtAuthenFilter ] JWT Cookie not found");
            abortWithUnauthorized(requestContext, "認証が必要です", path);
            return;
        }

        // JWT検証
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("[ JwtAuthenFilter ] Invalid JWT token");
            abortWithUnauthorized(requestContext, "認証が必要です", path);
            return;
        }

        // JWT Claimsから顧客情報を取得してAuthenContextに設定
        try {
            Integer customerId = jwtUtil.getCustomerIdFromToken(jwt);
            String email = jwtUtil.getEmailFromToken(jwt);
            authenContext.setCustomerId(customerId);
            authenContext.setEmail(email);
            logger.debug("[ JwtAuthenFilter ] Authenticated: customerId={}, email={}", customerId, email);
        } catch (Exception e) {
            logger.error("[ JwtAuthenFilter ] Failed to extract claims from JWT", e);
            abortWithUnauthorized(requestContext, "認証が必要です", path);
        }
    }

    /**
     * HTTPリクエストからJWT Cookieを抽出する
     * 
     * @param requestContext リクエストコンテキスト
     * @return JWT トークン文字列（存在しない場合はnull）
     */
    private String extractJwtFromRequest(ContainerRequestContext requestContext) {
        Cookie cookie = requestContext.getCookies().get(cookieName);
        return (cookie != null) ? cookie.getValue() : null;
    }

    /**
     * 公開エンドポイントかどうかを判定する
     * 
     * @param path リクエストパス
     * @return 公開エンドポイントの場合true
     */
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    /**
     * 401 Unauthorizedレスポンスを返してリクエストを中断する
     * 
     * @param requestContext リクエストコンテキスト
     * @param message エラーメッセージ
     * @param path リクエストパス
     */
    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message, String path) {
        ErrorResponse error = new ErrorResponse(401, "Unauthorized", message, path);
        requestContext.abortWith(
            Response.status(Response.Status.UNAUTHORIZED)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build()
        );
    }
}
