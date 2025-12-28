package pro.kensait.berrybooks.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * カートアイテムリクエストDTO
 * 
 * Java Recordを使用してイミュータブルなDTOを実装
 */
public record CartItemRequest(
    @NotNull(message = "書籍IDは必須です")
    Integer bookId,
    
    @NotBlank(message = "書籍名は必須です")
    String bookName,
    
    @NotBlank(message = "出版社名は必須です")
    String publisherName,
    
    @NotNull(message = "価格は必須です")
    Integer price,
    
    @NotNull(message = "数量は必須です")
    Integer count,
    
    @NotNull(message = "バージョンは必須です")
    Long version
) {
}

