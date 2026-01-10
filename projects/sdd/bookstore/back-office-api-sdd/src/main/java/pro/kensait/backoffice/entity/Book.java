package pro.kensait.backoffice.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * 書籍エンティティ
 * 
 * テーブル: BOOK + STOCK（@SecondaryTable）
 * 
 * BOOKテーブルとSTOCKテーブルを1:1で結合し、
 * 書籍情報と在庫情報を単一のエンティティで扱う
 */
@Entity
@Table(name = "BOOK")
@SecondaryTable(
    name = "STOCK",
    pkJoinColumns = @PrimaryKeyJoinColumn(name = "BOOK_ID")
)
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOK_ID")
    private Integer bookId;

    @Column(name = "BOOK_NAME", length = 200)
    private String bookName;

    @Column(name = "AUTHOR", length = 100)
    private String author;

    @Column(name = "PRICE", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "IMAGE_URL", length = 500)
    private String imageUrl;

    @Column(name = "DELETED")
    private Boolean deleted;

    // カテゴリへの多対一関連
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    // 出版社への多対一関連
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PUBLISHER_ID")
    private Publisher publisher;

    // STOCKテーブルのカラム（SecondaryTable）
    @Column(name = "QUANTITY", table = "STOCK")
    private Integer quantity;

    @Version
    @Column(name = "VERSION", table = "STOCK")
    private Long version;

    // デフォルトコンストラクタ（JPA必須）
    public Book() {
    }

    // コンストラクタ
    public Book(Integer bookId, String bookName, String author, BigDecimal price, 
                String imageUrl, Boolean deleted, Category category, Publisher publisher,
                Integer quantity, Long version) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.author = author;
        this.price = price;
        this.imageUrl = imageUrl;
        this.deleted = deleted;
        this.category = category;
        this.publisher = publisher;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
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

    // equals/hashCode（主キーベース）
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(bookId, book.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId);
    }

    // toString
    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", bookName='" + bookName + '\'' +
                ", author='" + author + '\'' +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                ", deleted=" + deleted +
                ", categoryId=" + (category != null ? category.getCategoryId() : null) +
                ", publisherId=" + (publisher != null ? publisher.getPublisherId() : null) +
                ", quantity=" + quantity +
                ", version=" + version +
                '}';
    }
}
