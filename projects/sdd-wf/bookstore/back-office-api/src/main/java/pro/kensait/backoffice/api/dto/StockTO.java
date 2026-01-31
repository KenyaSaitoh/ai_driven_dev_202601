package pro.kensait.backoffice.api.dto;

/**
 * 在庫情報転送オブジェクト（Book Stock API用）
 */
public class StockTO {
    private Integer bookId;
    private Integer quantity;
    private Long version;

    // デフォルトコンストラクタ（JSON-B用）
    public StockTO() {
    }

    // 全フィールドコンストラクタ
    public StockTO(Integer bookId, Integer quantity, Long version) {
        this.bookId = bookId;
        this.quantity = quantity;
        this.version = version;
    }

    // Getter/Setter
    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "StockTO [bookId=" + bookId + ", quantity=" + quantity + ", version=" + version + "]";
    }
}

