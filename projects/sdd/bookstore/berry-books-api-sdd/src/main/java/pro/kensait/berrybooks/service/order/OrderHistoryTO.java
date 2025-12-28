package pro.kensait.berrybooks.service.order;

import java.time.LocalDate;

/**
 * 注文履歴DTO（非正規化）
 * 
 * 1注文明細=1レコードで返す
 * Java Recordを使用してイミュータブルなDTOを実装
 */
public record OrderHistoryTO(
    LocalDate orderDate,
    Integer orderTranId,
    Integer orderDetailId,
    String bookName,
    String publisherName,
    Integer price,
    Integer count
) {
}

