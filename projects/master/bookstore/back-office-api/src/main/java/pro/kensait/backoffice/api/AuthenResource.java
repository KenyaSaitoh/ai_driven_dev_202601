package pro.kensait.backoffice.api;

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
import pro.kensait.backoffice.api.dto.ErrorResponse;
import pro.kensait.backoffice.api.dto.LoginRequest;
import pro.kensait.backoffice.api.dto.LoginResponse;
import pro.kensait.backoffice.dao.EmployeeDao;
import pro.kensait.backoffice.entity.Employee;
import pro.kensait.backoffice.security.JwtUtil;

/**
 * 認証API リソースクラス（社員用）
 */
@Path("/auth")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthenResource {
    private static final Logger logger = LoggerFactory.getLogger(AuthenResource.class);

    @Inject
    private EmployeeDao employeeDao;

    @Inject
    private JwtUtil jwtUtil;

    /**
     * ログイン（社員コード + パスワード）
     */
    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        logger.info("[ AuthenResource#login ] employeeCode: {}", request.employeeCode());

        try {
            // 社員情報を取得
            Employee employee = employeeDao.findByCode(request.employeeCode());

            if (employee == null) {
                logger.warn("[ AuthenResource#login ] Employee not found: {}", request.employeeCode());
                ErrorResponse errorResponse = new ErrorResponse(
                        "Unauthorized",
                        "社員コードまたはパスワードが正しくありません"
                );
                return Response.status(Response.Status.UNAUTHORIZED)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(errorResponse)
                        .build();
            }

            // パスワード照合（BCryptハッシュまたは平文パスワードをサポート）
            boolean passwordMatch;
            String storedPassword = employee.getPassword();
            
            // BCryptハッシュかどうかを判定（$2a$, $2b$, $2y$ で始まる）
            if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                // BCryptハッシュの場合
                passwordMatch = BCrypt.checkpw(request.password(), storedPassword);
            } else {
                // 平文パスワードの場合（開発環境用）
                passwordMatch = request.password().equals(storedPassword);
            }
            
            if (!passwordMatch) {
                logger.warn("[ AuthenResource#login ] Password mismatch for employeeCode: {}", request.employeeCode());
                ErrorResponse errorResponse = new ErrorResponse(
                        "Unauthorized",
                        "社員コードまたはパスワードが正しくありません"
                );
                return Response.status(Response.Status.UNAUTHORIZED)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(errorResponse)
                        .build();
            }

            // JWT生成
            Long departmentId = employee.getDepartment() != null ? 
                    employee.getDepartment().getDepartmentId() : null;
            String jwt = jwtUtil.generateToken(
                    employee.getEmployeeId(), 
                    employee.getEmployeeCode(),
                    departmentId
            );

            // HttpOnly Cookieを生成
            NewCookie cookie = new NewCookie.Builder(jwtUtil.getCookieName())
                    .value(jwt)
                    .path("/")
                    .maxAge(jwtUtil.getExpirationSeconds())
                    .httpOnly(true)
                    .secure(false)  // 開発環境ではfalse、本番環境ではtrue
                    .build();

            // レスポンス生成
            String departmentName = employee.getDepartment() != null ? 
                    employee.getDepartment().getDepartmentName() : null;
            LoginResponse response = new LoginResponse(
                    employee.getEmployeeId(),
                    employee.getEmployeeCode(),
                    employee.getEmployeeName(),
                    employee.getEmail(),
                    employee.getJobRank(),
                    departmentId,
                    departmentName
            );

            return Response.ok(response)
                    .cookie(cookie)
                    .build();

        } catch (Exception e) {
            logger.error("[ AuthenResource#login ] Unexpected error", e);
            ErrorResponse errorResponse = new ErrorResponse(
                    "Internal Server Error",
                    "ログイン処理中にエラーが発生しました"
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
     * 現在のログインユーザー情報取得
     * TODO: JWT認証フィルタ実装後に有効化
     */
    @GET
    @Path("/me")
    public Response getCurrentUser() {
        logger.info("[ AuthenResource#getCurrentUser ]");

        // TODO: JWT認証フィルタから社員IDを取得する実装を追加
        ErrorResponse errorResponse = new ErrorResponse(
                "Not Implemented",
                "この機能は未実装です"
        );
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity(errorResponse)
                .build();
    }
}

