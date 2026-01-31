package pro.kensait.berrybooks.service.order;

import java.math.BigDecimal;
import java.time.LocalDate;

// 注文サマリーを保持するDTOクラス（注文履歴の一覧表示用、Recordとして定義）
public record OrderSummaryTO(
        // 注文取引ID
        Integer orderTranId,
        // 注文日
        LocalDate orderDate,
        // 明細数
        Long itemCount,
        // 注文金額合計
        BigDecimal totalPrice) {
    
    // JPQLの COUNT() は Long を返すため、Long を受け取るコンストラクタが必要（自動生成される）
}

