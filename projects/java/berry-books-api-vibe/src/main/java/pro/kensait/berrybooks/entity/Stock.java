package pro.kensait.berrybooks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

// 在庫情報を表すエンティティクラス
@Entity
@Table(name = "STOCK")
public class Stock {
    // 書籍ID
    @Id
    @Column(name = "BOOK_ID")
    private Integer bookId;

    // 在庫数
    @Column(name = "QUANTITY")
    private Integer quantity;

    // バージョン（楽観的ロック用）
    @Version
    @Column(name = "VERSION")
    private Long version;

    //  引数なしのコンストラクタ
    public Stock() {
    }

    // コンストラクタ
    public Stock(Integer bookId, Integer quantity, Long version) {
        this.bookId = bookId;
        this.quantity = quantity;
        this.version = version;
    }

    // アクセサメソッド
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
        return "Stock [bookId=" + bookId + ", quantity=" + quantity + ", version="
                + version + "]";
    }
}