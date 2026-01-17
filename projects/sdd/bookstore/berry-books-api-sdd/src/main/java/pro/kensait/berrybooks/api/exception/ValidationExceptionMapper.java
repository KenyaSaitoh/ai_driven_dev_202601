package pro.kensait.berrybooks.api.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.common.ErrorResponse;

import java.util.stream.Collectors;

/**
 * ValidationExceptionMapper - バリデーション例外マッパー
 * 
 * 責務:
 * - Bean Validationエラーを処理
 * - 400 Bad Requestを返却
 * 
 * アノテーション:
 * - @Provider: JAX-RS Provider
 * 
 * 実装:
 * - ExceptionMapper<ConstraintViolationException>: バリデーションエラーをHTTPレスポンスに変換
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionMapper.class);
    
    /**
     * バリデーションエラーをHTTPレスポンスに変換
     * 
     * 処理:
     * 1. 制約違反メッセージ収集:
     *   - exception.getConstraintViolations() - 制約違反セット取得
     *   - 各制約違反のメッセージを連結（改行区切り）
     * 2. 警告ログ出力: logger.warn("Validation error: {}", message)
     * 3. ErrorResponse構築:
     *   - status: 400
     *   - error: "Bad Request"
     *   - message: 制約違反メッセージ
     *   - path: (リクエストパスは取得不可、空文字列)
     * 4. Response返却:
     *   - ステータス: 400 Bad Request
     *   - Content-Type: application/json
     *   - ボディ: ErrorResponse
     * 
     * @param exception バリデーション例外
     * @return Response 400 Bad Request
     */
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        logger.warn("[ ValidationExceptionMapper#toResponse ] Validation error: {}", message);
        
        ErrorResponse errorResponse = new ErrorResponse(
            400,
            "Bad Request",
            message,
            ""
        );
        
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}
