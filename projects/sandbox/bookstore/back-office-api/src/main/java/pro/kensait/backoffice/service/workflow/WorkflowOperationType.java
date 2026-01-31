package pro.kensait.backoffice.service.workflow;

/**
 * ワークフロー操作タイプ
 */
public enum WorkflowOperationType {
    /** 作成 */
    CREATE("作成"),
    /** 申請 */
    APPLY("申請"),
    /** 承認 */
    APPROVE("承認"),
    /** 却下（差戻） */
    REJECT("差戻");
    
    private final String displayName;
    
    WorkflowOperationType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return this.displayName;
    }
}

