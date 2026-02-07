package pro.kensait.berrybooks.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * カートアイテムDTO（注文明細1件分）
 * 
 * スナップショットパターン: 注文時点の書籍情報を保持
 */
public record CartItemRequest(
    @NotNull(message = "書籍IDは必須です")
    Integer bookId,
    
    @NotBlank(message = "書籍名は必須です")
    String bookName,
    
    @NotBlank(message = "出版社名は必須です")
    String publisherName,
    
    @NotNull(message = "価格は必須です")
    @Min(value = 0, message = "価格は0以上である必要があります")
    Integer price,
    
    @NotNull(message = "注文数は必須です")
    @Min(value = 1, message = "注文数は1以上である必要があります")
    Integer count,
    
    @NotNull(message = "バージョン番号は必須です")
    Long version
) {
}
