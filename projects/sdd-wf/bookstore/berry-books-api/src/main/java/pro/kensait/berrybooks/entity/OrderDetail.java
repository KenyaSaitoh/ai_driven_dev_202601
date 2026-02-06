package pro.kensait.berrybooks.entity;

import jakarta.persistence.*;

/**
 * 注文明細エンティティ（スナップショットパターン）
 */
@Entity
@Table(name = "ORDER_DETAIL")
public class OrderDetail {
    
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
    
    public OrderDetail() {
    }
    
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
}
