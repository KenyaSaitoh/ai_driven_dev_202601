package pro.kensait.berrybooks.api;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import pro.kensait.berrybooks.external.dto.CustomerTO;
import pro.kensait.berrybooks.external.CustomerHubRestClient;
import pro.kensait.berrybooks.security.JwtUtil;
import pro.kensait.berrybooks.security.AuthenContext;
import pro.kensait.berrybooks.service.customer.EmailAlreadyExistsException;
import pro.kensait.berrybooks.util.AddressUtil;

/**
 * 認証API リソースクラス
 */
@Path("/auth")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthenResource {
    private static final Logger logger = LoggerFactory.getLogger(AuthenResource.class);

    @Inject
    private CustomerHubRestClient customerHubClient;

    @Inject
    private JwtUtil jwtUtil;

    @Inject
    private AuthenContext authenContext;

    /**
     * ログイン
     */
    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        logger.info("[ AuthenResource#login ] email: {}", request.email());

        try {
            // 顧客情報を取得
            CustomerTO customer = customerHubClient.findByEmail(request.email());

            if (customer == null) {
                logger.warn("[ AuthenResource#login ] Customer not found: {}", request.email());
                ErrorResponse errorResponse = new ErrorResponse(
                        Response.Status.UNAUTHORIZED.getStatusCode(),
                        "Unauthorized",
                        "メールアドレスまたはパスワードが正しくありません",
                        "/api/auth/login"
                );
                return Response.status(Response.Status.UNAUTHORIZED)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(errorResponse)
                        .build();
            }

            // パスワード照合（BCryptハッシュまたは平文パスワードをサポート）
            boolean passwordMatch;
            String storedPassword = customer.password();
            
            // BCryptハッシュかどうかを判定（$2a$, $2b$, $2y$ で始まる）
            if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                // BCryptハッシュの場合
                passwordMatch = BCrypt.checkpw(request.password(), storedPassword);
            } else {
                // 平文パスワードの場合（開発環境用）
                passwordMatch = request.password().equals(storedPassword);
            }
            
            if (!passwordMatch) {
                logger.warn("[ AuthenResource#login ] Password mismatch for email: {}", request.email());
                ErrorResponse errorResponse = new ErrorResponse(
                        Response.Status.UNAUTHORIZED.getStatusCode(),
                        "Unauthorized",
                        "メールアドレスまたはパスワードが正しくありません",
                        "/api/auth/login"
                );
                return Response.status(Response.Status.UNAUTHORIZED)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(errorResponse)
                        .build();
            }

            // JWT生成
            String jwt = jwtUtil.generateToken(customer.customerId(), customer.email());

            // HttpOnly Cookieを生成
            NewCookie cookie = new NewCookie.Builder(jwtUtil.getCookieName())
                    .value(jwt)
                    .path("/")
                    .maxAge(jwtUtil.getExpirationSeconds())
                    .httpOnly(true)
                    .secure(false)  // 開発環境ではfalse、本番環境ではtrue
                    .build();

            // レスポンス生成
            LoginResponse response = new LoginResponse(
                    customer.customerId(),
                    customer.customerName(),
                    customer.email(),
                    customer.birthday(),
                    customer.address()
            );

            return Response.ok(response)
                    .cookie(cookie)
                    .build();

        } catch (Exception e) {
            logger.error("[ AuthenResource#login ] Unexpected error", e);
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Internal Server Error",
                    "ログイン処理中にエラーが発生しました",
                    "/api/auth/login"
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorResponse)
                    .build();
        }
    }

    /**
     * ログアウト
     */
    @POST
    @Path("/logout")
    public Response logout() {
        logger.info("[ AuthenResource#logout ]");

        // Cookieを削除（maxAge=0）
        NewCookie cookie = new NewCookie.Builder(jwtUtil.getCookieName())
                .value("")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(false)
                .build();

        return Response.ok()
                .cookie(cookie)
                .build();
    }

    /**
     * 新規登録
     */
    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request) {
        logger.info("[ AuthenResource#register ] email: {}", request.email());

        // 住所のバリデーション（都道府県チェック）
        // 住所が入力されている場合のみチェック（任意項目）
        if (request.address() != null && !request.address().isBlank()) {
            if (!AddressUtil.startsWithValidPrefecture(request.address())) {
                logger.warn("[ AuthenResource#register ] Invalid address: {}", request.address());
                ErrorResponse errorResponse = new ErrorResponse(
                        Response.Status.BAD_REQUEST.getStatusCode(),
                        "Bad Request",
                        "住所は都道府県名から始めてください",
                        "/api/auth/register"
                );
                return Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(errorResponse)
                        .build();
            }
        }

        try {
            // パスワードをハッシュ化
            String hashedPassword = BCrypt.hashpw(request.password(), BCrypt.gensalt());

            // 顧客DTO生成
            CustomerTO customerTO = new CustomerTO(
                    null,  // customerId は新規登録時はnull
                    request.customerName(),
                    hashedPassword,
                    request.email(),
                    request.birthday(),
                    request.address()
            );

            // 顧客登録（外部API経由）
            CustomerTO createdCustomer = customerHubClient.register(customerTO);

            // JWT生成
            String jwt = jwtUtil.generateToken(createdCustomer.customerId(), createdCustomer.email());

            // HttpOnly Cookieを生成
            NewCookie cookie = new NewCookie.Builder(jwtUtil.getCookieName())
                    .value(jwt)
                    .path("/")
                    .maxAge(jwtUtil.getExpirationSeconds())
                    .httpOnly(true)
                    .secure(false)
                    .build();

            // レスポンス生成
            LoginResponse response = new LoginResponse(
                    createdCustomer.customerId(),
                    createdCustomer.customerName(),
                    createdCustomer.email(),
                    createdCustomer.birthday(),
                    createdCustomer.address()
            );

            return Response.ok(response)
                    .cookie(cookie)
                    .build();

        } catch (EmailAlreadyExistsException e) {
            logger.warn("[ AuthenResource#register ] Customer already exists: {}", request.email());
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.CONFLICT.getStatusCode(),
                    "Conflict",
                    "指定されたメールアドレスは既に登録されています",
                    "/api/auth/register"
            );
            return Response.status(Response.Status.CONFLICT)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorResponse)
                    .build();
        } catch (Exception e) {
            logger.error("[ AuthenResource#register ] Unexpected error", e);
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Internal Server Error",
                    "登録処理中にエラーが発生しました",
                    "/api/auth/register"
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorResponse)
                    .build();
        }
    }

    /**
     * 現在のログインユーザー情報取得
     */
    @GET
    @Path("/me")
    public Response getCurrentUser() {
        logger.info("[ AuthenResource#getCurrentUser ]");

        // AuthenContextから顧客IDを取得（JWT認証必須）
        if (!authenContext.isAuthenticated()) {
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.UNAUTHORIZED.getStatusCode(),
                    "Unauthorized",
                    "認証が必要です",
                    "/api/auth/me"
            );
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(errorResponse)
                    .build();
        }

        Integer customerId = authenContext.getCustomerId();

        // 顧客情報を取得
        CustomerTO customer = customerHubClient.findById(customerId);
        if (customer == null) {
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.NOT_FOUND.getStatusCode(),
                    "Not Found",
                    "顧客が見つかりません",
                    "/api/auth/me"
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(errorResponse)
                    .build();
        }

        LoginResponse response = new LoginResponse(
                customer.customerId(),
                customer.customerName(),
                customer.email(),
                customer.birthday(),
                customer.address()
        );

        return Response.ok(response).build();
    }
}

