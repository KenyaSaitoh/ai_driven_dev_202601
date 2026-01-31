package pro.kensait.berrybooks.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// 注文明細を表すエンティティクラス
@Entity
@Table(name = "ORDER_DETAIL")
@IdClass(OrderDetailPK.class)
public class OrderDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    // 注文ID
    @Id
    @Column(name = "ORDER_TRAN_ID",
            nullable = false)
    private Integer orderTranId;

    // 注文明細ID
    @Id
    @Column(name = "ORDER_DETAIL_ID",
            nullable = false)
    private Integer orderDetailId;

    // 注文
    @ManyToOne(targetEntity = OrderTran.class)
    @JoinColumn(name = "ORDER_TRAN_ID",
            referencedColumnName = "ORDER_TRAN_ID",
            insertable = false, updatable = false) // ここがポイント！JPA教材でもちゃんと説明する
    private OrderTran orderTran;

    // 書籍ID（論理的参照のみ - back-office-apiに分離）
    @Column(name = "BOOK_ID", nullable = false)
    private Integer bookId;

    // 書籍名（非正規化 - 注文時点の情報を保持）
    @Column(name = "BOOK_NAME", length = 80, nullable = false)
    private String bookName;

    // 出版社名（非正規化 - 注文時点の情報を保持）
    @Column(name = "PUBLISHER_NAME", length = 80)
    private String publisherName;

    // 価格
    // 購入時点の価格を履歴に記録するため、あえて関連は使わず独立したフィールドにする
    @Column(name = "PRICE")
    private BigDecimal price;

    // 注文数
    @Column(name = "COUNT")
    private Integer count;

    // 引数なしのコンストラクタ
    public OrderDetail() {
    }

    // コンストラクタ
    public OrderDetail(Integer orderTranId, Integer orderDetailId, 
            Integer bookId, String bookName, String publisherName, BigDecimal price, Integer count) {
        this.orderTranId = orderTranId;
        this.orderDetailId = orderDetailId;
        this.bookId = bookId;
        this.bookName = bookName;
        this.publisherName = publisherName;
        this.price = price;
        this.count = count;
    }

    // アクセサメソッド
    public int getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(int orderDetailId) {
        this.orderDetailId = orderDetailId;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "OrderDetail [orderTranId=" + orderTranId + ", orderDetailId="
                + orderDetailId + ", orderTran=" + orderTran + ", bookId=" + bookId
                + ", bookName=" + bookName + ", price=" + price + ", count=" + count + "]";
    }
}