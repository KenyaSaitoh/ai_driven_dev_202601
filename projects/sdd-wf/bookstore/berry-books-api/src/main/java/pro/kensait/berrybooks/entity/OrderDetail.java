package pro.kensait.berrybooks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * 注文明細エンティティ（スナップショットパターン適用）
 */
@Entity
@Table(name = "ORDER_DETAIL")
@IdClass(OrderDetailPK.class)
public class OrderDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "ORDER_TRAN_ID")
    private Integer orderTranId;
    
    @Id
    @Column(name = "ORDER_DETAIL_ID")
    private Integer orderDetailId;
    
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
    
    @ManyToOne
    @JoinColumn(name = "ORDER_TRAN_ID", insertable = false, updatable = false)
    private OrderTran orderTran;
    
    public OrderDetail() {
    }
    
    public Integer getOrderTranId() {
        return orderTranId;
    }
    
    public void setOrderTranId(Integer orderTranId) {
        this.orderTranId = orderTranId;
    }
    
    public Integer getOrderDetailId() {
        return orderDetailId;
    }
    
    public void setOrderDetailId(Integer orderDetailId) {
        this.orderDetailId = orderDetailId;
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
    
    public OrderTran getOrderTran() {
        return orderTran;
    }
    
    public void setOrderTran(OrderTran orderTran) {
        this.orderTran = orderTran;
    }
}