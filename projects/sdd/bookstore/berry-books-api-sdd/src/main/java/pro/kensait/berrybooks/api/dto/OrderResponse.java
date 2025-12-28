package pro.kensait.berrybooks.api.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 注文レスポンスDTO
 * 
 * Java Recordを使用してイミュータブルなDTOを実装
 */
public record OrderResponse(
    Integer orderTranId,
    LocalDate orderDate,
    Integer totalPrice,
    Integer deliveryPrice,
    String deliveryAddress,
    Integer settlementType,
    List<OrderDetailResponse> orderDetails
) {
}

