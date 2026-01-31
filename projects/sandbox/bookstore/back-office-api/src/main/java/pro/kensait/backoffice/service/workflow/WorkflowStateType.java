package pro.kensait.backoffice.service.workflow;

/**
 * ワークフロー状態タイプ
 */
public enum WorkflowStateType {
    /** 作成済み */
    CREATED("作成済み"),
    /** 申請済み */
    APPLIED("申請中"),
    /** 承認済み */
    APPROVED("承認済み");
    
    private final String displayName;
    
    WorkflowStateType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return this.displayName;
    }
}

