package pro.kensait.berrybooks.service.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import pro.kensait.berrybooks.web.cart.CartItem;

// 注文を保持するDTOクラス（Recordとして定義）
public record OrderTO (
        // 顧客ID
        Integer customerId,
        // 注文日
        LocalDate orderDate,
        // カートアイテムのリスト
        List<CartItem> cartItems,
        // 注文金額合計
        BigDecimal totalPrice,
        // 配送料金
        BigDecimal deliveryPrice,
        // 配送先住所
        String deliveryAddress,
        // 決済方法
        Integer settlementType) {
}