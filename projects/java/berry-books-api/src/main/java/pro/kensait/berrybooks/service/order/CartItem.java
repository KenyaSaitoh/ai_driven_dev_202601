package pro.kensait.berrybooks.service.order;

import java.math.BigDecimal;

/**
 * カート内の書籍を保持するクラス（DTO化：セッションレス）
 */
public class CartItem {
    // 書籍ID
    private Integer bookId;
    // 書籍名
    private String bookName;
    // 出版社名
    private String publisherName;
    // 価格
    private BigDecimal price;
    // 個数
    private Integer count;
    // 削除フラグ（API版では不要だが互換性のため保持）
    private boolean remove;
    // バージョン（楽観的ロック用）
    private Long version;

    // 引数のないコンストラクタ
    public CartItem() {
    }

    // すべてのフィールドをパラメータに持つコンストラクタ
    public CartItem(Integer bookId, String bookName, String publisherName,
            BigDecimal price, Integer count, boolean remove) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.publisherName = publisherName;
        this.price = price;
        this.count = count;
        this.remove = remove;
    }

    // バージョン付きコンストラクタ
    public CartItem(Integer bookId, String bookName, String publisherName,
            BigDecimal price, Integer count, boolean remove, Long version) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.publisherName = publisherName;
        this.price = price;
        this.count = count;
        this.remove = remove;
        this.version = version;
    }

    // アクセサメソッド
    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public boolean isRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // 小計を計算するメソッド
    public BigDecimal getSubtotal() {
        if (price == null || count == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(count));
    }

    @Override
    public String toString() {
        return "CartItem [bookId=" + bookId + ", bookName=" + bookName
                + ", publisherName=" + publisherName + ", price=" + price + ", count="
                + count + ", remove=" + remove + ", version=" + version + "]";
    }
}

