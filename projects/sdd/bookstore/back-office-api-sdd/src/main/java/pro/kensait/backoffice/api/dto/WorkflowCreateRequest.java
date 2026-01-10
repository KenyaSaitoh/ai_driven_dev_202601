package pro.kensait.backoffice.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ワークフロー作成リクエスト
 * 
 * 新規ワークフロー作成時のリクエストデータ
 */
public record WorkflowCreateRequest(
    @NotBlank(message = "workflowTypeは必須です")
    String workflowType,
    
    @NotNull(message = "createdByは必須です")
    Long createdBy,
    
    // ADD_NEW_BOOK用フィールド
    String bookName,
    String author,
    BigDecimal price,
    String imageUrl,
    Integer categoryId,
    Integer publisherId,
    
    // REMOVE_BOOK用フィールド
    Integer bookId,
    
    // ADJUST_BOOK_PRICE用フィールド
    LocalDate startDate,
    LocalDate endDate,
    
    // 共通フィールド
    String applyReason
) {}
