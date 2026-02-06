package pro.kensait.berrybooks.external.dto;

/**
 * 在庫情報転送オブジェクト
 * 
 * back-office-apiからの在庫情報を転送する。
 * 
 * @param bookId 書籍ID
 * @param bookName 書籍名
 * @param quantity 在庫数
 * @param version 楽観的ロックバージョン
 * 
 * @since 1.0.0
 */
public record StockTO(
    Integer bookId,
    String bookName,
    Integer quantity,
    Long version
) {
}
