package pro.kensait.backoffice.common;

/**
 * ワークフロー状態列挙型
 * 
 * ワークフローのライフサイクル状態を定義する
 */
public enum WorkflowState {
    /**
     * 作成済み（一時保存）
     */
    CREATED,
    
    /**
     * 申請済み（承認待ち）
     */
    APPLIED,
    
    /**
     * 承認済み（完了）
     */
    APPROVED
}
