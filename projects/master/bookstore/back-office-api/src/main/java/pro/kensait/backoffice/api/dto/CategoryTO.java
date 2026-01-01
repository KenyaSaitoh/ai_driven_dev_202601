package pro.kensait.backoffice.api.dto;

/**
 * カテゴリ情報転送オブジェクト
 */
public record CategoryTO(
    Integer categoryId,
    String categoryName
) {
}

