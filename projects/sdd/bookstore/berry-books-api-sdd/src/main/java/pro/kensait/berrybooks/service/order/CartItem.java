package pro.kensait.berrybooks.service.order;

/**
 * カートアイテム（POJO）
 * 
 * サービス層で使用するカート情報
 */
public class CartItem {
    
    private Integer bookId;
    private String bookName;
    private String publisherName;
    private Integer price;
    private Integer count;
    private Long version; // 楽観的ロック用
    
    // コンストラクタ
    public CartItem() {
    }
    
    public CartItem(Integer bookId, String bookName, String publisherName, 
                   Integer price, Integer count, Long version) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.publisherName = publisherName;
        this.price = price;
        this.count = count;
        this.version = version;
    }
    
    // Getter/Setter
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
    
    public Integer getPrice() {
        return price;
    }
    
    public void setPrice(Integer price) {
        this.price = price;
    }
    
    public Integer getCount() {
        return count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
}

