package pro.kensait.backoffice.api.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ワークフロー更新リクエスト
 * 
 * ワークフロー更新時のリクエストデータ（一時保存）
 */
public record WorkflowUpdateRequest(
    @NotNull(message = "updatedByは必須です")
    Long updatedBy,
    
    // 更新可能フィールド（ワークフロータイプによって異なる）
    String bookName,
    String author,
    BigDecimal price,
    String imageUrl,
    Integer categoryId,
    Integer publisherId,
    Integer bookId,
    LocalDate startDate,
    LocalDate endDate,
    String applyReason
) {}
