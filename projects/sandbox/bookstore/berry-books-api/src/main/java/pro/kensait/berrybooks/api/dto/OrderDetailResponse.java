package pro.kensait.berrybooks.api.dto;

import java.math.BigDecimal;

/**
 * 注文明細レスポンスDTO
 */
public record OrderDetailResponse(
    Integer orderDetailId,
    Integer bookId,
    String bookName,
    String publisherName,
    BigDecimal price,
    Integer count
) {
}

