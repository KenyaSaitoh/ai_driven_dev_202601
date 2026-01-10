package pro.kensait.backoffice.api.dto;

import jakarta.validation.constraints.NotNull;

/**
 * ワークフロー操作リクエスト
 * 
 * ワークフロー操作（申請、承認、却下）時のリクエストデータ
 */
public record WorkflowOperationRequest(
    @NotNull(message = "operatedByは必須です")
    Long operatedBy,
    
    String operationReason
) {}
