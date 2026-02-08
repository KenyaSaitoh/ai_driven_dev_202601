package pro.kensait.berrybooks.api.dto;

import java.util.List;

/**
 * 注文作成リクエスト
 */
public record OrderRequest(
    List<CartItemRequest> cartItems,
    String deliveryAddress,
    Integer settlementType
) {
}