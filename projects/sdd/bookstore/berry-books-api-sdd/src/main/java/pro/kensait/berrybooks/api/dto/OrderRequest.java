package pro.kensait.berrybooks.api.dto;

import jakarta.validation.constraints.*;

import java.util.List;

/**
 * OrderRequest - 注文リクエストDTO
 * 
 * 責務:
 * - 注文リクエストデータの転送
 * - イミュータブルなデータ転送オブジェクト
 * 
 * 構造種別:
 * - Java Record（イミュータブル）
 * 
 * フィールド:
 * - cartItems: カート項目リスト
 * - totalPrice: 合計金額（配送料を含む）
 * - deliveryPrice: 配送料金
 * - deliveryAddress: 配送先住所
 * - settlementType: 決済方法（1:銀行振込, 2:クレジットカード, 3:着払い）
 */
public record OrderRequest(
    @NotNull(message = "カート項目は必須です")
    @NotEmpty(message = "カート項目は空にできません")
    List<CartItemRequest> cartItems,
    
    @NotNull(message = "合計金額は必須です")
    @Positive(message = "合計金額は正の数である必要があります")
    Integer totalPrice,
    
    @NotNull(message = "配送料金は必須です")
    @PositiveOrZero(message = "配送料金は0以上である必要があります")
    Integer deliveryPrice,
    
    @NotBlank(message = "配送先住所は必須です")
    String deliveryAddress,
    
    @NotNull(message = "決済方法は必須です")
    Integer settlementType
) {}
