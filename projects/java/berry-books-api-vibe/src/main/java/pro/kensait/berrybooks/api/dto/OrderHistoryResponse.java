package pro.kensait.berrybooks.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 注文履歴レスポンスDTO
 */
public record OrderHistoryResponse(
    LocalDate orderDate,
    Integer tranId,
    Integer detailId,
    String bookName,
    String publisherName,
    BigDecimal price,
    Integer count
) {
}

