package pro.kensait.berrybooks.service.order;

import java.math.BigDecimal;
import java.time.LocalDate;

// 注文履歴を保持するDTOクラス（Recordとして定義）
public record OrderHistoryTO (
        // 注文日
        LocalDate orderDate,
        // 注文ID
        Integer tranId,
        // 注文明細ID
        Integer detailId,
        // 書籍名
        String bookName,
        // 出版社名
        String publisherName,
        // 価格
        BigDecimal price,
        // 個数
        Integer count) {
}