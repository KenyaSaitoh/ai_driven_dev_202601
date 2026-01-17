package pro.kensait.berrybooks.api.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.common.ErrorResponse;

/**
 * GenericExceptionMapper - 一般例外マッパー
 * 
 * 責務:
 * - 一般的な例外を処理
 * - 500 Internal Server Errorを返却
 * 
 * アノテーション:
 * - @Provider: JAX-RS Provider
 * 
 * 実装:
 * - ExceptionMapper<Exception>: 一般例外をHTTPレスポンスに変換
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    
    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionMapper.class);
    
    /**
     * 例外をHTTPレスポンスに変換
     * 
     * 処理:
     * 1. エラーログ出力: logger.error("Unexpected error", exception)
     * 2. ErrorResponse構築:
     *   - status: 500
     *   - error: "Internal Server Error"
     *   - message: exception.getMessage()
     *   - path: (リクエストパスは取得不可、空文字列)
     * 3. Response返却:
     *   - ステータス: 500 Internal Server Error
     *   - Content-Type: application/json
     *   - ボディ: ErrorResponse
     * 
     * @param exception 例外
     * @return Response 500 Internal Server Error
     */
    @Override
    public Response toResponse(Exception exception) {
        logger.error("[ GenericExceptionMapper#toResponse ] Unexpected error", exception);
        
        ErrorResponse errorResponse = new ErrorResponse(
            500,
            "Internal Server Error",
            exception.getMessage() != null ? exception.getMessage() : "予期しないエラーが発生しました",
            ""
        );
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}
