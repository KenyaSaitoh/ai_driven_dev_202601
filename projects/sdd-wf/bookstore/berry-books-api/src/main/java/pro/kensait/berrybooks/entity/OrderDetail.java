package pro.kensait.berrybooks.entity;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * 注文明細エンティティ（スナップショットパターン）
 * 
 * 注文時点の書籍情報（書籍名、出版社名、価格）を保存し、
 * 書籍マスタの変更の影響を受けないようにする。
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "ORDER_DETAIL")
public class OrderDetail implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    private OrderDetailPK id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orderTranId")
    @JoinColumn(name = "ORDER_TRAN_ID", nullable = false)
    private OrderTran orderTran;
    
    @Column(name = "BOOK_ID", nullable = false)
    private Integer bookId;
    
    @Column(name = "BOOK_NAME", nullable = false, length = 100)
    private String bookName;
    
    @Column(name = "PUBLISHER_NAME", nullable = false, length = 50)
    private String publisherName;
    
    @Column(name = "PRICE", nullable = false)
    private Integer price;
    
    @Column(name = "COUNT", nullable = false)
    private Integer count;
    
    /**
     * デフォルトコンストラクタ
     */
    public OrderDetail() {
    }
    
    // Getters and Setters
    
    public OrderDetailPK getId() {
        return id;
    }
    
    public void setId(OrderDetailPK id) {
        this.id = id;
    }
    
    public OrderTran getOrderTran() {
        return orderTran;
    }
    
    public void setOrderTran(OrderTran orderTran) {
        this.orderTran = orderTran;
    }
    
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
    
    @Override
    public String toString() {
        return "OrderDetail{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", bookName='" + bookName + '\'' +
                ", publisherName='" + publisherName + '\'' +
                ", price=" + price +
                ", count=" + count +
                '}';
    }
}
