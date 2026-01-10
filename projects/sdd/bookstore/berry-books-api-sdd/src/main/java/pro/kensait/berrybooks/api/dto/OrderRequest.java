package pro.kensait.berrybooks.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 注文リクエストDTO
 * 
 * 注文作成時のリクエスト情報を保持する。
 */
public record OrderRequest(
    @NotNull(message = "カートアイテムは必須です")
    @Size(min = 1, message = "カートアイテムは1つ以上必要です")
    @Valid
    List<CartItemRequest> cartItems,
    
    @NotNull(message = "合計金額は必須です")
    @Min(value = 0, message = "合計金額は0以上である必要があります")
    Integer totalPrice,
    
    @NotNull(message = "配送料は必須です")
    @Min(value = 0, message = "配送料は0以上である必要があります")
    Integer deliveryPrice,
    
    @NotBlank(message = "配送先住所は必須です")
    String deliveryAddress,
    
    @NotNull(message = "決済方法は必須です")
    Integer settlementType  // 1: クレジットカード, 2: 代金引換
) {}
