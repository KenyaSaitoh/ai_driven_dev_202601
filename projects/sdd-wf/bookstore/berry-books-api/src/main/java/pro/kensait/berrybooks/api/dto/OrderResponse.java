package pro.kensait.berrybooks.api.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 注文情報レスポンス
 */
public record OrderResponse(
    Integer orderTranId,
    LocalDate orderDate,
    Integer customerId,
    Integer totalPrice,
    Integer deliveryPrice,
    String deliveryAddress,
    Integer settlementType,
    List<OrderDetailResponse> orderDetails
) {
}