package pro.kensait.backoffice.api.dto;

/**
 * 在庫転送オブジェクト
 * 
 * 在庫情報のレスポンスDTO
 */
public record StockTO(
    Integer bookId,
    String bookName,
    Integer quantity,
    Long version
) {}
