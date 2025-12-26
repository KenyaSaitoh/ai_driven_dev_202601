package pro.kensait.berrybooks.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.Table;

// 書籍を表すエンティティクラス
@Entity
@Table(name = "BOOK")
@SecondaryTable(name = "STOCK",
        pkJoinColumns = @PrimaryKeyJoinColumn(name = "BOOK_ID"))
public class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    // 書籍ID
    @Id
    @Column(name = "BOOK_ID")
    private Integer bookId;

    // 書籍名
    @Column(name = "BOOK_NAME")
    private String bookName;

    // 著者
    @Column(name = "AUTHOR")
    private String author;

    // カテゴリ
    @ManyToOne(targetEntity = Category.class)
    @JoinColumn(name = "CATEGORY_ID",
            referencedColumnName = "CATEGORY_ID")
    private Category category;

    // 出版社
    @ManyToOne(targetEntity = Publisher.class)
    @JoinColumn(name = "PUBLISHER_ID",
            referencedColumnName = "PUBLISHER_ID")
    private Publisher publisher;

    // 価格
    @Column(name = "PRICE")
    private BigDecimal price;

    // 在庫数
    @Column(table = "STOCK", name = "QUANTITY")
    private Integer quantity;

    // 引数なしのコンストラクタ
    public Book() {
    }

    // コンストラクタ
    public Book(Integer bookId, String bookName, String author, Category category, 
            Publisher publisher, BigDecimal price, Integer quantity) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.author = author;
        this.category = category;
        this.publisher = publisher;
        this.price = price;
        this.quantity = quantity;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Book [bookId=" + bookId + ", bookName=" + bookName + ", author=" + author
                + ", category=" + category + ", publisher=" + publisher + ", price="
                + price + ", quantity=" + quantity + "]";
    }
}