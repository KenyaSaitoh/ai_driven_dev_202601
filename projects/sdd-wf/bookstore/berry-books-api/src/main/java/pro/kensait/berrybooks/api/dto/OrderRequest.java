package pro.kensait.berrybooks.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 注文作成リクエストDTO
 */
public record OrderRequest(
    @NotNull(message = "カートアイテムは必須です")
    @NotEmpty(message = "カートアイテムは空にできません")
    @Valid
    List<CartItemRequest> cartItems,
    
    @NotNull(message = "注文金額合計は必須です")
    @Min(value = 0, message = "注文金額合計は0以上である必要があります")
    Integer totalPrice,
    
    @NotNull(message = "配送料金は必須です")
    @Min(value = 0, message = "配送料金は0以上である必要があります")
    Integer deliveryPrice,
    
    @NotBlank(message = "配送先住所は必須です")
    @Size(max = 120, message = "配送先住所は120文字以内で入力してください")
    String deliveryAddress,
    
    @NotNull(message = "決済方法は必須です")
    @Min(value = 1, message = "決済方法は1以上である必要があります")
    @Max(value = 3, message = "決済方法は3以下である必要があります")
    Integer settlementType
) {
}
