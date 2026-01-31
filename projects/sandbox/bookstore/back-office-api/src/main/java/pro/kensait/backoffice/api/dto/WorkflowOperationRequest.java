package pro.kensait.backoffice.api.dto;

/**
 * ワークフロー操作リクエストDTO
 * APPLY, APPROVE, REJECT操作で使用
 */
public class WorkflowOperationRequest {
    private Long operatedBy; // 操作者ID
    private String operationReason; // 操作理由（REJECT時は必須）

    // デフォルトコンストラクタ
    public WorkflowOperationRequest() {
    }

    // コンストラクタ
    public WorkflowOperationRequest(Long operatedBy, String operationReason) {
        this.operatedBy = operatedBy;
        this.operationReason = operationReason;
    }

    // Getter/Setter
    public Long getOperatedBy() {
        return operatedBy;
    }

    public void setOperatedBy(Long operatedBy) {
        this.operatedBy = operatedBy;
    }

    public String getOperationReason() {
        return operationReason;
    }

    public void setOperationReason(String operationReason) {
        this.operationReason = operationReason;
    }

    @Override
    public String toString() {
        return "WorkflowOperationRequest [operatedBy=" + operatedBy + "]";
    }
}


