package pro.kensait.berrybooks.api.dto;

/**
 * 注文明細レスポンスDTO
 */
public record OrderDetailResponse(
    Integer orderDetailId,
    Integer bookId,
    String bookName,
    String publisherName,
    Integer price,
    Integer count
) {
}
