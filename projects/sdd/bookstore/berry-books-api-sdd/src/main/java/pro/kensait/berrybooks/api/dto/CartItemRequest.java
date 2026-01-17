package pro.kensait.berrybooks.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * CartItemRequest - カートアイテムリクエストDTO
 * 
 * 責務:
 * - カートアイテムリクエストデータの転送
 * - イミュータブルなデータ転送オブジェクト
 * 
 * 構造種別:
 * - Java Record（イミュータブル）
 * 
 * フィールド:
 * - bookId: 書籍ID
 * - bookName: 書籍名（スナップショット用）
 * - publisherName: 出版社名（スナップショット用）
 * - price: 価格（スナップショット用）
 * - count: 注文数
 * - version: バージョン番号（楽観的ロック用）
 */
public record CartItemRequest(
    @NotNull(message = "書籍IDは必須です")
    Integer bookId,
    
    @NotBlank(message = "書籍名は必須です")
    String bookName,
    
    @NotBlank(message = "出版社名は必須です")
    String publisherName,
    
    @NotNull(message = "価格は必須です")
    @Positive(message = "価格は正の数である必要があります")
    Integer price,
    
    @NotNull(message = "注文数は必須です")
    @Min(value = 1, message = "注文数は1以上である必要があります")
    Integer count,
    
    @NotNull(message = "バージョンは必須です")
    Long version
) {}
