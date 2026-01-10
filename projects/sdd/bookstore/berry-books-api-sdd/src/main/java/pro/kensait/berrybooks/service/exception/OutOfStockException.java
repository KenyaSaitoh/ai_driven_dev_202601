package pro.kensait.berrybooks.service.exception;

/**
 * 在庫不足例外
 * 
 * 注文数が在庫数を超える場合にスローされる。
 */
public class OutOfStockException extends RuntimeException {

    private static final long serialVersionUID = 1L;

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
