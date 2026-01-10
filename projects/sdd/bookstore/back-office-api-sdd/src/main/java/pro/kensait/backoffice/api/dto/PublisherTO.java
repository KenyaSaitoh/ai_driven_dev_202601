package pro.kensait.backoffice.api.dto;

/**
 * 出版社転送オブジェクト
 * 
 * 出版社情報のレスポンスDTO
 */
public record PublisherTO(
    Integer publisherId,
    String publisherName
) {}
