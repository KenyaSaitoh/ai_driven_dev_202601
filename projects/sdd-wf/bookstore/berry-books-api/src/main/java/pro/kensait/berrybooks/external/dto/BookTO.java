package pro.kensait.berrybooks.external.dto;

/**
 * 書籍情報転送オブジェクト（外部APIレスポンス）
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
) {}
