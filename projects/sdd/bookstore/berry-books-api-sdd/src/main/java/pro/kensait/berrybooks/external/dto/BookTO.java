package pro.kensait.berrybooks.external.dto;

/**
 * BookTO - 書籍情報転送オブジェクト
 * 
 * 責務:
 * - back-office-apiとの書籍情報のやり取り
 * - イミュータブルなデータ転送オブジェクト
 * 
 * 構造種別:
 * - Java Record（イミュータブル）
 * 
 * フィールド:
 * - bookId: 書籍ID
 * - bookName: 書籍名
 * - author: 著者
 * - category: カテゴリ情報（ネストされたオブジェクト）
 * - publisher: 出版社情報（ネストされたオブジェクト）
 * - price: 価格（円）
 * - quantity: 在庫数
 * - version: バージョン番号（楽観的ロック用）
 * 
 * 注意:
 * - @SecondaryTableにより、BOOKテーブルとSTOCKテーブルを結合
 * - 在庫情報（quantity, version）がフラットな構造で含まれる
 */
public record BookTO(
    Integer bookId,
    String bookName,
    String author,
    CategoryTO category,
    PublisherTO publisher,
    Integer price,
    Integer quantity,
    Long version
) {}
