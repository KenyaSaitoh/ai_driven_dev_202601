package pro.kensait.berrybooks.entity;

import jakarta.persistence.*;

/**
 * OrderDetail - 注文明細エンティティ
 * 
 * 責務:
 * - 注文明細情報の管理
 * - テーブル: ORDER_DETAIL
 * 
 * 重要な設計ポイント:
 * - 複合主キー: @EmbeddedIdを使用
 * - BOOK_ID: 論理参照のみ（外部キー制約なし、back-office-apiが書籍データを管理）
 * - スナップショットパターン: BOOK_NAME、PUBLISHER_NAME、PRICEを保存（注文時点の値）
 * - @MapsId("orderTranId"): 複合主キーの一部をリレーションシップから取得
 */
@Entity
@Table(name = "ORDER_DETAIL")
public class OrderDetail {
    
    @EmbeddedId
    private OrderDetailPK id;
    
    @MapsId("orderTranId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_TRAN_ID")
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
     * デフォルトコンストラクタ（JPA要件）
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
}
