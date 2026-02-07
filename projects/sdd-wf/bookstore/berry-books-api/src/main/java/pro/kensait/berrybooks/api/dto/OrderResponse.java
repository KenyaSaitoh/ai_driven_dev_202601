package pro.kensait.berrybooks.api.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 注文作成レスポンス・注文履歴レスポンスDTO
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
