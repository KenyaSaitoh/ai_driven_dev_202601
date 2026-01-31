package pro.kensait.berrybooks.service.order;

// 在庫不足時にスローされる例外クラス
public class OutOfStockException extends RuntimeException {
    private Integer bookId;
    private String bookName;

    public OutOfStockException() {
        super();
    }

    public OutOfStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public OutOfStockException(String message) {
        super(message);
    }

    public OutOfStockException(Throwable cause) {
        super(cause);
    }

    public OutOfStockException(Integer bookId, String bookName, String message) {
        super(message);
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