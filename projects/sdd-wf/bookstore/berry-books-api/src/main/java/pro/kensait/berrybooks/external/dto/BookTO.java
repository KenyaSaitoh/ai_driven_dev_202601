package pro.kensait.berrybooks.external.dto;

/**
 * 書籍情報転送オブジェクト
 * 
 * back-office-apiからの書籍・在庫情報を転送する。
 * 
 * @param bookId 書籍ID
 * @param bookName 書籍名
 * @param author 著者
 * @param categoryId カテゴリID
 * @param categoryName カテゴリ名
 * @param publisherId 出版社ID
 * @param publisherName 出版社名
 * @param price 価格
 * @param quantity 在庫数
 * @param version 楽観的ロックバージョン
 * 
 * @since 1.0.0
 */
public record BookTO(
    Integer bookId,
    String bookName,
    String author,
    Integer categoryId,
    String categoryName,
    Integer publisherId,
    String publisherName,
    Integer price,
    Integer quantity,
    Long version
) {
}
