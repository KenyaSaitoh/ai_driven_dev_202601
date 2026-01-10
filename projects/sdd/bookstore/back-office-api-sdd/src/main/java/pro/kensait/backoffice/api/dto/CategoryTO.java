package pro.kensait.backoffice.api.dto;

/**
 * カテゴリ転送オブジェクト
 * 
 * カテゴリ情報のレスポンスDTO
 */
public record CategoryTO(
    Integer categoryId,
    String categoryName
) {}
