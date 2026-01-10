package pro.kensait.berrybooks.api.dto;

import java.time.LocalDateTime;

/**
 * 注文履歴レスポンスDTO（非正規化）
 * 
 * 注文履歴の一覧表示用に非正規化されたDTO。
 */
public record OrderHistoryResponse(
    LocalDateTime orderDate,
    Integer orderTranId,
    Integer orderDetailId,
    String bookName,
    String publisherName,
    Integer price,
    Integer count
) {}
