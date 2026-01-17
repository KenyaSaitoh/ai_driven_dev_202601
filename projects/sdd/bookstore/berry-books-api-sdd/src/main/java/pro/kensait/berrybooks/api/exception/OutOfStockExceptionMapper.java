package pro.kensait.berrybooks.api.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.common.ErrorResponse;

/**
 * OutOfStockExceptionMapper - 在庫不足例外マッパー
 * 
 * 責務:
 * - 在庫不足エラーを処理
 * - 409 Conflictを返却
 * 
 * アノテーション:
 * - @Provider: JAX-RS Provider
 * 
 * 実装:
 * - ExceptionMapper<OutOfStockException>: 在庫不足エラーをHTTPレスポンスに変換
 */
@Provider
public class OutOfStockExceptionMapper implements ExceptionMapper<OutOfStockException> {
    
    private static final Logger logger = LoggerFactory.getLogger(OutOfStockExceptionMapper.class);
    
    /**
     * 在庫不足エラーをHTTPレスポンスに変換
     * 
     * 処理:
     * 1. 警告ログ出力: logger.warn("Out of stock: {}", exception.getMessage())
     * 2. ErrorResponse構築:
     *   - status: 409
     *   - error: "Conflict"
     *   - message: exception.getMessage()
     *   - path: (リクエストパスは取得不可、空文字列)
     * 3. Response返却:
     *   - ステータス: 409 Conflict
     *   - Content-Type: application/json
     *   - ボディ: ErrorResponse
     * 
     * @param exception 在庫不足例外
     * @return Response 409 Conflict
     */
    @Override
    public Response toResponse(OutOfStockException exception) {
        logger.warn("[ OutOfStockExceptionMapper#toResponse ] Out of stock: {}", exception.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            409,
            "Conflict",
            exception.getMessage(),
            ""
        );
        
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}
