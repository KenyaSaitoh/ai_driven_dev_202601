package pro.kensait.berrybooks.external.dto;

/**
 * StockTO - 在庫情報転送オブジェクト
 * 
 * 責務:
 * - back-office-apiとの在庫情報のやり取り
 * - イミュータブルなデータ転送オブジェクト
 * 
 * 構造種別:
 * - Java Record（イミュータブル）
 * 
 * フィールド:
 * - bookId: 書籍ID
 * - bookName: 書籍名
 * - quantity: 在庫数
 * - version: バージョン番号（楽観的ロック用）
 */
public record StockTO(
    Integer bookId,
    String bookName,
    Integer quantity,
    Long version
) {}
