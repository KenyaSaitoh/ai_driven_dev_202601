package pro.kensait.berrybooks.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.berrybooks.common.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

/**
 * JwtAuthenFilter - JWT認証フィルター
 * 
 * 責務:
 * - JWT認証フィルター処理
 * - リクエストの認証処理
 * - AuthenContextへの認証情報設定
 * 
 * アノテーション:
 * - @Provider: JAX-RS Provider
 * - @Priority(Priorities.AUTHENTICATION): 認証フィルターの優先度
 * 
 * 実装:
 * - ContainerRequestFilter: JAX-RS標準のリクエストフィルター
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenFilter implements ContainerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenFilter.class);
    
    /**
     * 公開エンドポイント（認証不要）
     */
    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
        "auth/login",
        "auth/logout",
        "auth/register",
        "books",
        "images",
        "/api/auth/login",
        "/api/auth/logout",
        "/api/auth/register",
        "/api/books",
        "/api/images"
    );
    
    /**
     * 認証必須エンドポイント
     */
    private static final Set<String> SECURED_ENDPOINTS = Set.of(
        "orders",
        "auth/me",
        "/api/orders",
        "/api/auth/me"
    );
    
    @Inject
    private JwtUtil jwtUtil;
    
    @Inject
    private AuthenContext authenContext;
    
    @Context
    private HttpServletRequest httpServletRequest;
    
    /**
     * リクエストの認証処理
     * 
     * 処理フロー:
     * 1. リクエストパス取得: requestContext.getUriInfo().getPath()
     * 2. 公開エンドポイント判定: isPublicPath(requestUri) → 認証スキップ
     * 3. JWT Cookie抽出: jwtUtil.extractJwtFromRequest(httpServletRequest)
     * 4. JWT検証: jwtUtil.validateToken(jwt)
     * 5. 検証成功時:
     *   - jwtUtil.getUserIdFromToken(jwt) - 顧客ID取得
     *   - jwtUtil.getEmailFromToken(jwt) - メールアドレス取得
     *   - authenContext.setCustomerId(customerId) - 認証コンテキストに設定
     *   - authenContext.setEmail(email) - 認証コンテキストに設定
     * 6. 検証失敗時（認証必須パスの場合）:
     *   - 401 Unauthorizedレスポンスを返却
     *   - requestContext.abortWith(Response.status(401)...)
     * 
     * @param requestContext リクエストコンテキスト
     * @throws IOException IO例外
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestUri = requestContext.getUriInfo().getPath();
        logger.debug("[ JwtAuthenFilter#filter ] Processing request: {}", requestUri);
        
        // 公開エンドポイント判定
        if (isPublicPath(requestUri)) {
            logger.debug("[ JwtAuthenFilter#filter ] Public path, skipping authentication: {}", requestUri);
            return;
        }
        
        try {
            // JWT Cookie抽出
            String jwt = jwtUtil.extractJwtFromRequest(httpServletRequest);
            
            if (jwt != null && jwtUtil.validateToken(jwt)) {
                // JWTからユーザー情報を取得
                Long customerId = jwtUtil.getUserIdFromToken(jwt);
                String email = jwtUtil.getEmailFromToken(jwt);
                
                // AuthenContextに認証情報を設定
                authenContext.setCustomerId(customerId);
                authenContext.setEmail(email);
                
                logger.debug("[ JwtAuthenFilter#filter ] Authentication successful, customerId={}, email={}", customerId, email);
            } else {
                // JWT認証必須のパスで未認証の場合はエラー
                if (isSecuredPath(requestUri)) {
                    logger.warn("[ JwtAuthenFilter#filter ] Authentication required but no valid JWT found: {}", requestUri);
                    ErrorResponse errorResponse = new ErrorResponse(
                        401,
                        "Unauthorized",
                        "認証が必要です",
                        requestUri
                    );
                    requestContext.abortWith(
                        Response.status(Response.Status.UNAUTHORIZED)
                                .type(MediaType.APPLICATION_JSON)
                                .entity(errorResponse)
                                .build()
                    );
                }
            }
        } catch (Exception e) {
            logger.error("[ JwtAuthenFilter#filter ] Authentication error: {}", e.getMessage(), e);
            if (isSecuredPath(requestUri)) {
                ErrorResponse errorResponse = new ErrorResponse(
                    401,
                    "Unauthorized",
                    "認証エラー",
                    requestUri
                );
                requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(errorResponse)
                            .build()
                );
            }
        }
    }
    
    /**
     * 公開パス（認証不要）かどうかを判定
     * 
     * 処理:
     * 1. PUBLIC_ENDPOINTSの各パターンとパスを比較
     * 2. パスがいずれかのパターンで始まる場合、trueを返却
     * 3. それ以外の場合、falseを返却
     * 
     * @param path リクエストパス
     * @return 公開パス=true、それ以外=false
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(path::startsWith);
    }
    
    /**
     * 認証必須パスかどうかを判定
     * 
     * 処理:
     * 1. SECURED_ENDPOINTSの各パターンとパスを比較
     * 2. パスがいずれかのパターンで始まる場合、trueを返却
     * 3. それ以外の場合、falseを返却
     * 
     * @param path リクエストパス
     * @return 認証必須パス=true、それ以外=false
     */
    private boolean isSecuredPath(String path) {
        return SECURED_ENDPOINTS.stream()
                .anyMatch(path::startsWith);
    }
}
