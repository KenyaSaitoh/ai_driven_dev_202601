package pro.kensait.berrybooks.service.order;

/**
 * 在庫不足を表す業務例外クラス
 * 注文確定時に注文数が在庫数を超えた場合にスローされる
 */
public class OutOfStockException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    // 在庫不足の書籍ID
    private final Integer bookId;
    
    // 在庫不足の書籍名
    private final String bookName;
    
    /**
     * コンストラクタ
     * @param bookId 在庫不足の書籍ID
     * @param bookName 在庫不足の書籍名
     * @param message エラーメッセージ
     */
    public OutOfStockException(Integer bookId, String bookName, String message) {
        super(message);
        this.bookId = bookId;
        this.bookName = bookName;
    }
    
    /**
     * 書籍IDを取得
     * @return 書籍ID
     */
    public Integer getBookId() {
        return bookId;
    }
    
    /**
     * 書籍名を取得
     * @return 書籍名
     */
    public String getBookName() {
        return bookName;
    }
}


