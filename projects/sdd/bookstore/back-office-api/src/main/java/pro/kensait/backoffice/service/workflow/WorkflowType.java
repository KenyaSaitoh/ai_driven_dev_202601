package pro.kensait.backoffice.service.workflow;

/**
 * ワークフロータイプ
 */
public enum WorkflowType {
    /** 新規書籍の追加 */
    ADD_NEW_BOOK("新規書籍の追加"),
    /** 既存書籍の削除 */
    REMOVE_BOOK("既存書籍の削除"),
    /** 書籍価格の改定 */
    ADJUST_BOOK_PRICE("書籍価格の改定");
    
    private final String displayName;
    
    WorkflowType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return this.displayName;
    }
}

