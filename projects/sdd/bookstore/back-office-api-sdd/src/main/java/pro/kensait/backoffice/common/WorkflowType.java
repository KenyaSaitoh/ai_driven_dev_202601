package pro.kensait.backoffice.common;

/**
 * ワークフロータイプ列挙型
 * 
 * 書籍マスタ変更のワークフロータイプを定義する
 */
public enum WorkflowType {
    /**
     * 新規書籍追加
     */
    ADD_NEW_BOOK,
    
    /**
     * 既存書籍削除
     */
    REMOVE_BOOK,
    
    /**
     * 書籍価格改定
     */
    ADJUST_BOOK_PRICE
}
