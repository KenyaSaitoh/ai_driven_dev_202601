package pro.kensait.berrybooks.service.order;

/**
 * 在庫不足例外
 * 
 * 注文時に在庫が不足している場合にスローされる
 */
public class OutOfStockException extends RuntimeException {
    
    private final Integer bookId;
    private final String bookName;
    
    public OutOfStockException(Integer bookId, String bookName) {
        super("在庫が不足しています: " + bookName);
        this.bookId = bookId;
        this.bookName = bookName;
    }
    
    public Integer getBookId() {
        return bookId;
    }
    
    public String getBookName() {
        return bookName;
    }
}

