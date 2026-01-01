package pro.kensait.backoffice.service.workflow;

/**
 * ワークフロータイプ
 */
public enum WorkflowType {
    /** 新規作成 */
    CREATE,
    /** 削除 */
    DELETE,
    /** 価格の一時調整 */
    PRICE_TEMP_ADJUSTMENT
}

