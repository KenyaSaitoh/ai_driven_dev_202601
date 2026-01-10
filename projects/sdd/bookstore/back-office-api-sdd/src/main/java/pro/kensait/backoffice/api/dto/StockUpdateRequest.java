package pro.kensait.backoffice.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 在庫更新リクエスト
 * 
 * 在庫更新のリクエストDTO（楽観的ロック対応）
 */
public record StockUpdateRequest(
    @NotNull(message = "在庫数は必須です")
    @Min(value = 0, message = "在庫数は0以上である必要があります")
    Integer quantity,
    
    @NotNull(message = "バージョンは必須です")
    Long version
) {}
