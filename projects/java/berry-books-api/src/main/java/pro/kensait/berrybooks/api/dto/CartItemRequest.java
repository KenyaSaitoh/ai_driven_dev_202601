package pro.kensait.berrybooks.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * カート項目リクエストDTO
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
    BigDecimal price,
    
    @NotNull(message = "数量は必須です")
    @Min(value = 1, message = "数量は1以上である必要があります")
    Integer count,
    
    @NotNull(message = "バージョンは必須です")
    Integer version
) {
}

