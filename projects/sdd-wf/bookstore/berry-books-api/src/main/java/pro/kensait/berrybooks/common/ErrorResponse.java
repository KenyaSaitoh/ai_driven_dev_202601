package pro.kensait.berrybooks.common;

/**
 * エラーレスポンス
 * 
 * 統一的なエラーレスポンス形式を提供する。
 * 
 * @param status HTTPステータスコード
 * @param error エラー種別
 * @param message エラーメッセージ
 * @param path リクエストパス
 * 
 * @since 1.0.0
 */
public record ErrorResponse(
    int status,
    String error,
    String message,
    String path
) {
}
