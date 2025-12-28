package pro.kensait.berrybooks.entity;

import jakarta.persistence.*;

/**
 * 注文明細エンティティ
 * 
 * テーブル: ORDER_DETAIL
 * 複合主キー: (ORDER_TRAN_ID, ORDER_DETAIL_ID)
 */
@Entity
@Table(name = "ORDER_DETAIL")
@IdClass(OrderDetailPK.class)
public class OrderDetail {
    
    @Id
    @Column(name = "ORDER_TRAN_ID")
    private Integer orderTranId;
    
    @Id
    @Column(name = "ORDER_DETAIL_ID")
    private Integer orderDetailId;
    
    @Column(name = "PRICE", nullable = false)
    private Integer price;
    
    @Column(name = "COUNT", nullable = false)
    private Integer count;
    
    // リレーションシップ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_TRAN_ID", insertable = false, updatable = false)
    private OrderTran orderTran;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOK_ID", nullable = false)
    private Book book;
    
    // コンストラクタ
    public OrderDetail() {
    }
    
    public OrderDetail(Integer orderTranId, Integer orderDetailId, Integer price, Integer count) {
        this.orderTranId = orderTranId;
        this.orderDetailId = orderDetailId;
        this.price = price;
        this.count = count;
    }
    
    // Getter/Setter
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
    
    public Book getBook() {
        return book;
    }
    
    public void setBook(Book book) {
        this.book = book;
    }
    
    @Override
    public String toString() {
        return "OrderDetail{" +
                "orderTranId=" + orderTranId +
                ", orderDetailId=" + orderDetailId +
                ", price=" + price +
                ", count=" + count +
                '}';
    }
}

