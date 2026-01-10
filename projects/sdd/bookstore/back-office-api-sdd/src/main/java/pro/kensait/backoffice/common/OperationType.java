package pro.kensait.backoffice.common;

/**
 * ワークフロー操作種別列挙型
 * 
 * ワークフローに対する操作の種類を定義する
 */
public enum OperationType {
    /**
     * 作成
     */
    CREATE,
    
    /**
     * 申請
     */
    APPLY,
    
    /**
     * 承認
     */
    APPROVE,
    
    /**
     * 却下
     */
    REJECT
}
