package pro.kensait.berrybooks.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 注文リクエストDTO
 * 
 * Java Recordを使用してイミュータブルなDTOを実装
 */
public record OrderRequest(
    @NotEmpty(message = "カートアイテムは必須です")
    @Valid
    List<CartItemRequest> cartItems,
    
    @NotNull(message = "合計金額は必須です")
    Integer totalPrice,
    
    @NotNull(message = "配送料金は必須です")
    Integer deliveryPrice,
    
    @NotBlank(message = "配送先住所は必須です")
    String deliveryAddress,
    
    @NotNull(message = "決済方法は必須です")
    Integer settlementType
) {
}

