package pro.kensait.berrybooks.api.dto;

/**
 * 注文明細レスポンスDTO
 * 
 * 注文明細の情報を保持する。
 */
public record OrderDetailResponse(
    Integer orderDetailId,  // 明細番号（lineNo）
    Integer bookId,
    String bookName,
    String publisherName,
    Integer price,
    Integer count
) {}
