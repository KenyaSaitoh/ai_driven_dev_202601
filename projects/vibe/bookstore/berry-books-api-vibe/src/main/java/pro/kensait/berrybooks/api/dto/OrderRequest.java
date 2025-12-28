package pro.kensait.berrybooks.api.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 注文リクエストDTO
 */
public record OrderRequest(
    @NotEmpty(message = "カート商品は必須です")
    @Valid
    List<CartItemRequest> cartItems,
    
    @NotNull(message = "合計金額は必須です")
    @Min(value = 0, message = "合計金額は0以上である必要があります")
    BigDecimal totalPrice,
    
    @NotNull(message = "配送料は必須です")
    @Min(value = 0, message = "配送料は0以上である必要があります")
    BigDecimal deliveryPrice,
    
    @NotBlank(message = "配送先住所は必須です")
    @Size(max = 200, message = "配送先住所は200文字以内で入力してください")
    String deliveryAddress,
    
    @NotNull(message = "決済方法は必須です")
    Integer settlementType
) {
}

