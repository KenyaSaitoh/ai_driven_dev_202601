package pro.kensait.backoffice.api.dto;

/**
 * 出版社情報転送オブジェクト
 */
public record PublisherTO(
    Integer publisherId,
    String publisherName
) {
}

