package pro.kensait.backoffice.service.workflow;

/**
 * ワークフロー操作タイプ
 */
public enum WorkflowOperationType {
    /** 作成 */
    CREATE,
    /** 更新（一時保存） */
    UPDATE,
    /** 申請 */
    APPLY,
    /** 承認 */
    APPROVE,
    /** 却下（差戻） */
    REJECT
}

