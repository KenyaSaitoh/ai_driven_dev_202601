package pro.kensait.berrybooks.external.dto;

/**
 * CategoryTO - カテゴリ情報転送オブジェクト
 * 
 * 責務:
 * - back-office-apiとのカテゴリ情報のやり取り
 * - イミュータブルなデータ転送オブジェクト
 * 
 * 構造種別:
 * - Java Record（イミュータブル）
 * 
 * フィールド:
 * - categoryId: カテゴリID
 * - categoryName: カテゴリ名
 */
public record CategoryTO(
    Integer categoryId,
    String categoryName
) {}
