package pro.kensait.berrybooks.external.dto;

/**
 * 在庫情報転送用DTO（Transfer Object）
 * 
 * back-office-apiとのデータ転送に使用する。
 */
public record StockTO(
    Integer bookId,
    Integer quantity,
    Long version
) {
}
