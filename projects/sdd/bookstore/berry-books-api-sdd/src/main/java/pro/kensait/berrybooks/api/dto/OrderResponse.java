package pro.kensait.berrybooks.api.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 注文レスポンスDTO
 * 
 * 注文の詳細情報を保持する。
 */
public record OrderResponse(
    Integer orderTranId,
    Integer customerId,
    LocalDateTime orderDate,
    Integer totalPrice,
    Integer deliveryPrice,
    String deliveryAddress,
    Integer settlementType,
    List<OrderDetailResponse> orderDetails
) {}
