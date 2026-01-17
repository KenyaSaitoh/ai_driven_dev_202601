package pro.kensait.berrybooks.api.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.common.ErrorResponse;

/**
 * OptimisticLockExceptionMapper - 楽観的ロック例外マッパー
 * 
 * 責務:
 * - 楽観的ロック競合エラーを処理
 * - 409 Conflictを返却
 * 
 * アノテーション:
 * - @Provider: JAX-RS Provider
 * 
 * 実装:
 * - ExceptionMapper<OptimisticLockException>: 楽観的ロック競合エラーをHTTPレスポンスに変換
 */
@Provider
public class OptimisticLockExceptionMapper implements ExceptionMapper<OptimisticLockException> {
    
    private static final Logger logger = LoggerFactory.getLogger(OptimisticLockExceptionMapper.class);
    
    /**
     * 楽観的ロック競合エラーをHTTPレスポンスに変換
     * 
     * 処理:
     * 1. 警告ログ出力: logger.warn("Optimistic lock conflict")
     * 2. ErrorResponse構築:
     *   - status: 409
     *   - error: "Conflict"
     *   - message: "他のユーザーが購入済みです。最新の在庫情報を確認してください。"
     *   - path: (リクエストパスは取得不可、空文字列)
     * 3. Response返却:
     *   - ステータス: 409 Conflict
     *   - Content-Type: application/json
     *   - ボディ: ErrorResponse
     * 
     * @param exception 楽観的ロック例外
     * @return Response 409 Conflict
     */
    @Override
    public Response toResponse(OptimisticLockException exception) {
        logger.warn("[ OptimisticLockExceptionMapper#toResponse ] Optimistic lock conflict");
        
        ErrorResponse errorResponse = new ErrorResponse(
            409,
            "Conflict",
            "他のユーザーが購入済みです。最新の在庫情報を確認してください。",
            ""
        );
        
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}
