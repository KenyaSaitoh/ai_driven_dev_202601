package pro.kensait.berrybooks.external.dto;

/**
 * PublisherTO - 出版社情報転送オブジェクト
 * 
 * 責務:
 * - back-office-apiとの出版社情報のやり取り
 * - イミュータブルなデータ転送オブジェクト
 * 
 * 構造種別:
 * - Java Record（イミュータブル）
 * 
 * フィールド:
 * - publisherId: 出版社ID
 * - publisherName: 出版社名
 */
public record PublisherTO(
    Integer publisherId,
    String publisherName
) {}
