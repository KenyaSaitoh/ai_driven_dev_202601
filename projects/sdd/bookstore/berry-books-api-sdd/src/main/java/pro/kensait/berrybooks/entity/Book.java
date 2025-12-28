package pro.kensait.berrybooks.entity;

import jakarta.persistence.*;

/**
 * 書籍エンティティ
 * 
 * テーブル: BOOK
 */
@Entity
@Table(name = "BOOK")
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOK_ID")
    private Integer bookId;
    
    @Column(name = "BOOK_NAME", length = 80, nullable = false)
    private String bookName;
    
    @Column(name = "AUTHOR", length = 40, nullable = false)
    private String author;
    
    @Column(name = "PRICE", nullable = false)
    private Integer price;
    
    // リレーションシップ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    private Category category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PUBLISHER_ID", nullable = false)
    private Publisher publisher;
    
    @OneToOne(mappedBy = "book", fetch = FetchType.LAZY)
    private Stock stock;
    
    // コンストラクタ
    public Book() {
    }
    
    public Book(Integer bookId, String bookName, String author, Integer price) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.author = author;
        this.price = price;
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
    
    public Integer getPrice() {
        return price;
    }
    
    public void setPrice(Integer price) {
        this.price = price;
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
    
    public Stock getStock() {
        return stock;
    }
    
    public void setStock(Stock stock) {
        this.stock = stock;
    }
    
    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", bookName='" + bookName + '\'' +
                ", author='" + author + '\'' +
                ", price=" + price +
                '}';
    }
}

