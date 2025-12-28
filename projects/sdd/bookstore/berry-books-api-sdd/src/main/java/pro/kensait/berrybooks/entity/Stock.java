package pro.kensait.berrybooks.entity;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;

/**
 * 在庫エンティティ（楽観的ロック対応）
 * 
 * テーブル: STOCK
 */
@Entity
@Table(name = "STOCK")
public class Stock {
    
    @Id
    @Column(name = "BOOK_ID")
    private Integer bookId;
    
    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity;
    
    @Version
    @Column(name = "VERSION", nullable = false)
    private Long version;
    
    // リレーションシップ
    @OneToOne
    @JoinColumn(name = "BOOK_ID")
    @MapsId
    @JsonbTransient
    private Book book;
    
    // コンストラクタ
    public Stock() {
    }
    
    public Stock(Integer bookId, Integer quantity, Long version) {
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
    
    public Book getBook() {
        return book;
    }
    
    public void setBook(Book book) {
        this.book = book;
    }
    
    @Override
    public String toString() {
        return "Stock{" +
                "bookId=" + bookId +
                ", quantity=" + quantity +
                ", version=" + version +
                '}';
    }
}

