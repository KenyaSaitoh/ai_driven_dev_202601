package pro.kensait.berrybooks.common;

/**
 * 統一的なエラーレスポンス形式
 */
public record ErrorResponse(
    int status,
    String error,
    String message,
    String path
) {}
