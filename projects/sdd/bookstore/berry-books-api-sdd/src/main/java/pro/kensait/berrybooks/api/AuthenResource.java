package pro.kensait.berrybooks.api;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import pro.kensait.berrybooks.api.dto.ErrorResponse;
import pro.kensait.berrybooks.api.dto.LoginRequest;
import pro.kensait.berrybooks.api.dto.LoginResponse;
import pro.kensait.berrybooks.api.dto.RegisterRequest;
import pro.kensait.berrybooks.external.CustomerHubRestClient;
import pro.kensait.berrybooks.external.dto.CustomerTO;
import pro.kensait.berrybooks.security.JwtUtil;
import pro.kensait.berrybooks.security.AuthenContext;
import pro.kensait.berrybooks.service.exception.EmailAlreadyExistsException;
import pro.kensait.berrybooks.util.AddressUtil;

/**
 * 認証APIリソース
 * 
 * ログイン、ログアウト、新規登録、ユーザー情報取得を提供する。
 */
@Path("/auth")
@ApplicationScoped
public class AuthenResource {

    private static final Logger logger = LoggerFactory.getLogger(AuthenResource.class);

    @Inject
    private CustomerHubRestClient customerHubClient;

    @Inject
    private JwtUtil jwtUtil;

    @Inject
    private AuthenContext authenContext;

    private String cookieName;
    private Long expirationMs;

    @PostConstruct
    public void init() {
        Config config = ConfigProvider.getConfig();
        cookieName = config.getOptionalValue("jwt.cookie-name", String.class)
                .orElse("berry-books-jwt");
        expirationMs = config.getOptionalValue("jwt.expiration-ms", Long.class)
                .orElse(86400000L);
        logger.info("AuthenResource initialized, cookieName: {}, expirationMs: {}", 
                cookieName, expirationMs);
    }

    /**
     * ログイン
     * 
     * @param request ログインリクエスト
     * @return ログインレスポンス + JWT Cookie
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@Valid LoginRequest request) {
        logger.info("[ AuthenResource#login ] email={}", request.email());

        // 顧客情報を取得（外部API）
        CustomerTO customer = customerHubClient.findByEmail(request.email());
        
        if (customer == null) {
            logger.warn("[ AuthenResource#login ] Customer not found: email={}", request.email());
            ErrorResponse error = new ErrorResponse(
                401,
                "Unauthorized",
                "メールアドレスまたはパスワードが正しくありません",
                "/api/auth/login"
            );
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(error)
                    .build();
        }

        // パスワード照合（BCryptまたは平文）
        boolean passwordMatch = verifyPassword(request.password(), customer.password());
        
        if (!passwordMatch) {
            logger.warn("[ AuthenResource#login ] Password mismatch: email={}", request.email());
            ErrorResponse error = new ErrorResponse(
                401,
                "Unauthorized",
                "メールアドレスまたはパスワードが正しくありません",
                "/api/auth/login"
            );
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(error)
                    .build();
        }

        // JWT生成
        String jwt = jwtUtil.generateToken(customer.customerId(), customer.email());

        // JWT Cookieを設定
        NewCookie jwtCookie = new NewCookie.Builder(cookieName)
                .value(jwt)
                .path("/")
                .maxAge((int) (expirationMs / 1000)) // 秒単位
                .httpOnly(true)
                .secure(false) // 開発環境: false、本番環境: true
                .build();

        // レスポンス作成
        LoginResponse response = new LoginResponse(
            customer.customerId(),
            customer.customerName(),
            customer.email(),
            customer.birthday(),
            customer.address()
        );

        logger.info("[ AuthenResource#login ] Login successful: customerId={}", customer.customerId());

        return Response.ok(response)
                .cookie(jwtCookie)
                .build();
    }

    /**
     * ログアウト
     * 
     * @return 200 OK
     */
    @POST
    @Path("/logout")
    public Response logout() {
        logger.info("[ AuthenResource#logout ]");

        // JWT Cookie削除（MaxAge=0）
        NewCookie jwtCookie = new NewCookie.Builder(cookieName)
                .value("")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(false)
                .build();

        return Response.ok()
                .cookie(jwtCookie)
                .build();
    }

    /**
     * 新規登録
     * 
     * @param request 新規登録リクエスト
     * @return ログインレスポンス + JWT Cookie
     */
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(@Valid RegisterRequest request) {
        logger.info("[ AuthenResource#register ] email={}", request.email());

        // 住所バリデーション（都道府県名から始まるか）
        if (request.address() != null && !request.address().isEmpty()) {
            if (!AddressUtil.startsWithValidPrefecture(request.address())) {
                logger.warn("[ AuthenResource#register ] Invalid address: {}", request.address());
                ErrorResponse error = new ErrorResponse(
                    400,
                    "Bad Request",
                    "住所は都道府県名から始めてください",
                    "/api/auth/register"
                );
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(error)
                        .build();
            }
        }

        // パスワードをBCryptでハッシュ化
        String hashedPassword = BCrypt.hashpw(request.password(), BCrypt.gensalt());

        // 顧客登録（外部API）
        CustomerTO customerTO = new CustomerTO(
            null, // customerId（自動採番）
            request.customerName(),
            hashedPassword,
            request.email(),
            request.birthday(),
            request.address()
        );

        try {
            CustomerTO registeredCustomer = customerHubClient.register(customerTO);
            
            // JWT生成
            String jwt = jwtUtil.generateToken(registeredCustomer.customerId(), registeredCustomer.email());

            // JWT Cookieを設定
            NewCookie jwtCookie = new NewCookie.Builder(cookieName)
                    .value(jwt)
                    .path("/")
                    .maxAge((int) (expirationMs / 1000))
                    .httpOnly(true)
                    .secure(false)
                    .build();

            // レスポンス作成
            LoginResponse response = new LoginResponse(
                registeredCustomer.customerId(),
                registeredCustomer.customerName(),
                registeredCustomer.email(),
                registeredCustomer.birthday(),
                registeredCustomer.address()
            );

            logger.info("[ AuthenResource#register ] Registration successful: customerId={}", registeredCustomer.customerId());

            return Response.ok(response)
                    .cookie(jwtCookie)
                    .build();

        } catch (EmailAlreadyExistsException e) {
            logger.warn("[ AuthenResource#register ] Email already exists: {}", request.email());
            ErrorResponse error = new ErrorResponse(
                409,
                "Conflict",
                e.getMessage(),
                "/api/auth/register"
            );
            return Response.status(Response.Status.CONFLICT)
                    .entity(error)
                    .build();
        }
    }

    /**
     * 現在のログインユーザー情報取得
     * 
     * @return ログインレスポンス
     */
    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentUser() {
        Integer customerId = authenContext.getCustomerId();
        logger.info("[ AuthenResource#getCurrentUser ] customerId={}", customerId);

        // 顧客情報を取得（外部API）
        CustomerTO customer = customerHubClient.findById(customerId);

        if (customer == null) {
            logger.warn("[ AuthenResource#getCurrentUser ] Customer not found: customerId={}", customerId);
            ErrorResponse error = new ErrorResponse(
                404,
                "Not Found",
                "顧客が見つかりません",
                "/api/auth/me"
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
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
     * パスワード照合（BCryptまたは平文）
     * 
     * @param plainPassword 平文パスワード
     * @param storedPassword 保存されたパスワード（BCryptハッシュまたは平文）
     * @return パスワードが一致する場合true
     */
    private boolean verifyPassword(String plainPassword, String storedPassword) {
        // BCryptハッシュかどうかを判定（$2a$で始まる）
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            // BCrypt照合
            return BCrypt.checkpw(plainPassword, storedPassword);
        } else {
            // 平文比較（開発環境用）
            logger.debug("Using plain text password comparison");
            return plainPassword.equals(storedPassword);
        }
    }
}
