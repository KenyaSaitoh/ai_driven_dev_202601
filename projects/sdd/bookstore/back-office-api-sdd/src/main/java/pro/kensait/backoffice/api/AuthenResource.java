package pro.kensait.backoffice.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.backoffice.api.dto.LoginRequest;
import pro.kensait.backoffice.api.dto.LoginResponse;
import pro.kensait.backoffice.dao.EmployeeDao;
import pro.kensait.backoffice.entity.Employee;
import pro.kensait.backoffice.security.JwtUtil;

/**
 * 認証APIリソース
 * 
 * ベースパス: /api/auth
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
     * ログイン
     * 
     * POST /api/auth/login
     * 
     * @param request ログインリクエスト
     * @return ログインレスポンス + JWT Cookie
     */
    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        logger.info("[ AuthenResource#login ] employeeCode: {}", request.employeeCode());

        try {
            // 1. 社員情報を取得
            Employee employee = employeeDao.findByCode(request.employeeCode());
            
            if (employee == null) {
                logger.warn("[ AuthenResource#login ] Employee not found: {}", request.employeeCode());
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("Unauthorized", "社員コードまたはパスワードが正しくありません"))
                        .build();
            }

            logger.debug("[ AuthenResource#login ] Employee found: {}", employee.getEmployeeCode());

            // 2. パスワード照合
            boolean passwordMatch = checkPassword(request.password(), employee.getPassword());
            
            if (!passwordMatch) {
                logger.warn("[ AuthenResource#login ] Password mismatch for employeeCode: {}", request.employeeCode());
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("Unauthorized", "社員コードまたはパスワードが正しくありません"))
                        .build();
            }

            logger.debug("[ AuthenResource#login ] Password matched");

            // 3. JWT生成
            String jwt = jwtUtil.generateToken(
                employee.getEmployeeId(),
                employee.getEmployeeCode(),
                employee.getDepartment().getDepartmentId()
            );

            logger.debug("[ AuthenResource#login ] JWT generated");

            // 4. HttpOnly Cookieを生成
            NewCookie jwtCookie = new NewCookie.Builder(jwtUtil.getCookieName())
                    .value(jwt)
                    .path("/")
                    .maxAge(jwtUtil.getExpirationSeconds())
                    .httpOnly(true)
                    .secure(false) // 開発環境ではfalse、本番環境ではtrue
                    .build();

            // 5. レスポンス生成
            LoginResponse response = new LoginResponse(
                employee.getEmployeeId(),
                employee.getEmployeeCode(),
                employee.getEmployeeName(),
                employee.getEmail(),
                employee.getJobRank(),
                employee.getDepartment().getDepartmentId(),
                employee.getDepartment().getDepartmentName()
            );

            logger.info("[ AuthenResource#login ] Login successful: {}", request.employeeCode());

            return Response.ok(response)
                    .cookie(jwtCookie)
                    .build();
        } catch (Exception e) {
            logger.error("[ AuthenResource#login ] Exception occurred", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal Server Error", "ログイン処理中にエラーが発生しました: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * ログアウト
     * 
     * POST /api/auth/logout
     * 
     * @return 空のレスポンス + Cookie削除
     */
    @POST
    @Path("/logout")
    public Response logout() {
        logger.info("[ AuthenResource#logout ]");

        // Cookie削除用のNewCookie（MaxAge=0）
        NewCookie deleteCookie = new NewCookie.Builder(jwtUtil.getCookieName())
                .value("")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();

        return Response.ok()
                .cookie(deleteCookie)
                .build();
    }

    /**
     * 現在のユーザー情報取得（未実装）
     * 
     * GET /api/auth/me
     * 
     * @return 501 Not Implemented
     */
    @GET
    @Path("/me")
    public Response getCurrentUser() {
        logger.info("[ AuthenResource#getCurrentUser ]");
        
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity(new ErrorResponse("Not Implemented", "この機能は未実装です"))
                .build();
    }

    /**
     * パスワード照合
     * 
     * BCryptハッシュまたは平文パスワードをサポート
     * 
     * @param plainPassword 平文パスワード
     * @param storedPassword 保存されているパスワード（BCryptハッシュまたは平文）
     * @return 照合結果（true: 一致、false: 不一致）
     */
    private boolean checkPassword(String plainPassword, String storedPassword) {
        // BCryptハッシュの判定（$2a$, $2b$, $2y$で始まる）
        if (storedPassword.startsWith("$2a$") || 
            storedPassword.startsWith("$2b$") || 
            storedPassword.startsWith("$2y$")) {
            // BCrypt照合
            return BCrypt.checkpw(plainPassword, storedPassword);
        } else {
            // 平文比較（開発環境のみ）
            return plainPassword.equals(storedPassword);
        }
    }

    /**
     * エラーレスポンスDTO（内部クラス）
     */
    private record ErrorResponse(String error, String message) {}
}
