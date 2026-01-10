package pro.kensait.backoffice.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ワークフロー転送オブジェクト
 * 
 * ワークフロー操作履歴のレスポンスデータ
 */
public record WorkflowTO(
    Long operationId,
    Long workflowId,
    String workflowType,
    String state,
    Integer bookId,
    String bookName,
    String author,
    Integer categoryId,
    Integer publisherId,
    BigDecimal price,
    String imageUrl,
    String applyReason,
    LocalDate startDate,
    LocalDate endDate,
    String operationType,
    Long operatedBy,
    String operatorName,
    String operatorCode,
    LocalDateTime operatedAt,
    String operationReason
) {}
