package pro.kensait.berrybooks.external.dto;

/**
 * 在庫情報転送オブジェクト（外部APIレスポンス）
 */
public record StockTO(
    Integer bookId,
    String bookName,
    Integer quantity,
    Long version
) {}
