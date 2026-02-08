package pro.kensait.berrybooks.api.dto;

/**
 * カートアイテム情報リクエスト
 */
public record CartItemRequest(
    Integer bookId,
    String bookName,
    String publisherName,
    Integer price,
    Integer count,
    Long version
) {
}