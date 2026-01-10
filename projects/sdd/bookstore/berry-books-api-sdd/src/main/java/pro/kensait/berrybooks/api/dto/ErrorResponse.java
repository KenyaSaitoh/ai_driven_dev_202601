package pro.kensait.berrybooks.api.dto;

/**
 * 統一的なエラーレスポンスDTO
 * 
 * 全APIエンドポイントのエラーレスポンスで使用する。
 */
public record ErrorResponse(
    int status,
    String error,
    String message,
    String path
) {
}
