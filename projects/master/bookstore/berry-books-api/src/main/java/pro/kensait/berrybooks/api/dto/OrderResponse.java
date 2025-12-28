package pro.kensait.berrybooks.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 注文レスポンスDTO
 */
public record OrderResponse(
    Integer orderTranId,
    LocalDate orderDate,
    BigDecimal totalPrice,
    BigDecimal deliveryPrice,
    String deliveryAddress,
    Integer settlementType,
    List<OrderDetailResponse> orderDetails
) {
}

