package pro.kensait.berrybooks.api;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.api.dto.*;
import pro.kensait.berrybooks.external.CustomerRestClient;
import pro.kensait.berrybooks.external.dto.CustomerTO;
import pro.kensait.berrybooks.security.JwtUtil;
import pro.kensait.berrybooks.security.SecuredResource;
import pro.kensait.berrybooks.util.AddressUtil;

import java.time.LocalDate;

/**
 * 認証API Resource
 * 
 * ベースパス: /api/auth
 * 
 * エンドポイント:
 * - POST /login: ログイン（JWT Cookie発行）
 * - POST /logout: ログアウト（Cookie削除）
 * - POST /register: 新規登録
 * - GET /me: 現在のログインユーザー情報取得（認証必須）
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthResource.class);
    
    @Inject
    private CustomerRestClient customerRestClient;
    
    @Inject
    private JwtUtil jwtUtil;
    
    @Inject
    private SecuredResource securedResource;
    
    @Inject
    @ConfigProperty(name = "jwt.cookie-name", defaultValue = "berry-books-jwt")
    private String cookieName;
    
    @Inject
    @ConfigProperty(name = "jwt.expiration-ms", defaultValue = "86400000")
    private Long expirationMs;
    
    /**
     * ログイン
     * 
     * @param request ログインリクエスト
     * @return ログインレスポンス（JWT Cookieを含む）
     */
    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        logger.info("[ AuthResource#login ] email={}", request.email());
        
        // メールアドレスで顧客を検索
        CustomerTO customer = customerRestClient.findByEmail(request.email());
        if (customer == null) {
            logger.warn("[ AuthResource#login ] Customer not found: {}", request.email());
            ErrorResponse errorResponse = new ErrorResponse(
                401,
                "Unauthorized",
                "メールアドレスまたはパスワードが正しくありません",
                "/api/auth/login"
            );
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(errorResponse)
                    .build();
        }
        
        // パスワード検証（BCryptハッシュまたは平文）
        if (!verifyPassword(request.password(), customer.password())) {
            logger.warn("[ AuthResource#login ] Password mismatch for: {}", request.email());
            ErrorResponse errorResponse = new ErrorResponse(
                401,
                "Unauthorized",
                "メールアドレスまたはパスワードが正しくありません",
                "/api/auth/login"
            );
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(errorResponse)
                    .build();
        }
        
        // JWT生成
        String token = jwtUtil.generateToken(customer.customerId(), customer.email());
        
        // レスポンス作成
        LoginResponse response = new LoginResponse(
            customer.customerId(),
            customer.customerName(),
            customer.email(),
            customer.birthday(),
            customer.address()
        );
        
        // JWT Cookieを設定
        NewCookie jwtCookie = new NewCookie.Builder(cookieName)
                .value(token)
                .path("/")
                .maxAge((int) (expirationMs / 1000)) // 秒単位
                .httpOnly(true)
                .build();
        
        logger.info("[ AuthResource#login ] Login successful: customerId={}", customer.customerId());
        
        return Response.ok(response)
                .cookie(jwtCookie)
                .build();
    }
    
    /**
     * ログアウト
     * 
     * @return 空レスポンス（JWT Cookie削除）
     */
    @POST
    @Path("/logout")
    public Response logout() {
        logger.info("[ AuthResource#logout ] Logging out");
        
        // JWT Cookieを削除（MaxAge=0）
        NewCookie jwtCookie = new NewCookie.Builder(cookieName)
                .value("")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();
        
        return Response.ok("{}")
                .cookie(jwtCookie)
                .build();
    }
    
    /**
     * 新規登録
     * 
     * @param request 登録リクエスト
     * @return 登録レスポンス（JWT Cookieを含む）
     */
    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request) {
        logger.info("[ AuthResource#register ] email={}", request.email());
        
        // 住所検証（都道府県名から始まるか）
        if (request.address() != null && !request.address().isEmpty()) {
            if (!AddressUtil.startsWithValidPrefecture(request.address())) {
                logger.warn("[ AuthResource#register ] Invalid address prefix: {}", request.address());
                ErrorResponse errorResponse = new ErrorResponse(
                    400,
                    "Bad Request",
                    "住所は都道府県名から始めてください",
                    "/api/auth/register"
                );
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse)
                        .build();
            }
        }
        
        // パスワードハッシュ化（BCrypt）
        String hashedPassword = hashPassword(request.password());
        
        // 顧客情報を作成
        CustomerTO customerTO = new CustomerTO(
            null, // customerIdは自動採番
            request.customerName(),
            hashedPassword,
            request.email(),
            request.birthday(),
            request.address()
        );
        
        // 外部APIで顧客登録
        CustomerTO createdCustomer = customerRestClient.register(customerTO);
        if (createdCustomer == null) {
            logger.warn("[ AuthResource#register ] Email already exists: {}", request.email());
            ErrorResponse errorResponse = new ErrorResponse(
                409,
                "Conflict",
                "指定されたメールアドレスは既に登録されています",
                "/api/auth/register"
            );
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorResponse)
                    .build();
        }
        
        // JWT生成（自動ログイン）
        String token = jwtUtil.generateToken(createdCustomer.customerId(), createdCustomer.email());
        
        // レスポンス作成
        LoginResponse response = new LoginResponse(
            createdCustomer.customerId(),
            createdCustomer.customerName(),
            createdCustomer.email(),
            createdCustomer.birthday(),
            createdCustomer.address()
        );
        
        // JWT Cookieを設定
        NewCookie jwtCookie = new NewCookie.Builder(cookieName)
                .value(token)
                .path("/")
                .maxAge((int) (expirationMs / 1000)) // 秒単位
                .httpOnly(true)
                .build();
        
        logger.info("[ AuthResource#register ] Registration successful: customerId={}", createdCustomer.customerId());
        
        return Response.ok(response)
                .cookie(jwtCookie)
                .build();
    }
    
    /**
     * 現在のログインユーザー情報取得（認証必須）
     * 
     * @return ユーザー情報
     */
    @GET
    @Path("/me")
    public Response getCurrentUser() {
        logger.info("[ AuthResource#getCurrentUser ] customerId={}", securedResource.getCustomerId());
        
        // SecuredResourceから顧客IDを取得（JwtAuthenticationFilterで設定済み）
        Integer customerId = securedResource.getCustomerId();
        if (customerId == null) {
            logger.warn("[ AuthResource#getCurrentUser ] Customer ID not found in secured resource");
            ErrorResponse errorResponse = new ErrorResponse(
                401,
                "Unauthorized",
                "認証が必要です",
                "/api/auth/me"
            );
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(errorResponse)
                    .build();
        }
        
        // 外部APIで顧客情報を取得
        CustomerTO customer = customerRestClient.findById(customerId);
        if (customer == null) {
            logger.warn("[ AuthResource#getCurrentUser ] Customer not found: {}", customerId);
            ErrorResponse errorResponse = new ErrorResponse(
                404,
                "Not Found",
                "顧客が見つかりません",
                "/api/auth/me"
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorResponse)
                    .build();
        }
        
        // レスポンス作成
        LoginResponse response = new LoginResponse(
            customer.customerId(),
            customer.customerName(),
            customer.email(),
            customer.birthday(),
            customer.address()
        );
        
        return Response.ok(response).build();
    }
    
    /**
     * パスワード検証（BCryptハッシュまたは平文）
     * 
     * @param plainPassword 平文パスワード
     * @param storedPassword 保存されたパスワード（BCryptハッシュまたは平文）
     * @return 一致する場合true
     */
    private boolean verifyPassword(String plainPassword, String storedPassword) {
        // BCryptハッシュの場合（$2a$, $2b$, $2y$で始まる）
        if (storedPassword.startsWith("$2a$") || 
            storedPassword.startsWith("$2b$") || 
            storedPassword.startsWith("$2y$")) {
            return org.mindrot.jbcrypt.BCrypt.checkpw(plainPassword, storedPassword);
        }
        
        // 平文の場合（開発環境用）
        return plainPassword.equals(storedPassword);
    }
    
    /**
     * パスワードハッシュ化（BCrypt）
     * 
     * @param plainPassword 平文パスワード
     * @return BCryptハッシュ
     */
    private String hashPassword(String plainPassword) {
        return org.mindrot.jbcrypt.BCrypt.hashpw(plainPassword, org.mindrot.jbcrypt.BCrypt.gensalt());
    }
}

