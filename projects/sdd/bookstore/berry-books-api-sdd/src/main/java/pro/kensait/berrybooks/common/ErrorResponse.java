package pro.kensait.berrybooks.common;

/**
 * ErrorResponse - 統一的なエラーレスポンス形式
 * 
 * 責務:
 * - エラーレスポンスの標準フォーマット
 * - Exception Mapperで使用
 * 
 * 構造種別:
 * - Java Record（イミュータブル）
 * 
 * フィールド:
 * - status: HTTPステータスコード
 * - error: エラー種別
 * - message: エラーメッセージ
 * - path: リクエストパス
 * 
 * 使用例:
 * {
 *   "status": 409,
 *   "error": "Conflict",
 *   "message": "在庫が不足しています: Java完全理解",
 *   "path": "/api/orders"
 * }
 */
public record ErrorResponse(
    int status,
    String error,
    String message,
    String path
) {}
