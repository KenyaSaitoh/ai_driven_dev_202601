package com.example.bookstore.backoffice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 書籍エンティティ
 * BOOKテーブルとSTOCKテーブルを結合して管理
 */
@Entity
@Table(name = "BOOK")
@SecondaryTable(name = "STOCK", pkJoinColumns = @PrimaryKeyJoinColumn(name = "BOOK_ID"))
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOK_ID")
    private Integer bookId;

    @Column(name = "BOOK_NAME")
    private String bookName;

    @Column(name = "AUTHOR")
    private String author;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PUBLISHER_ID")
    private Publisher publisher;

    @Column(name = "PRICE")
    private BigDecimal price;

    @Column(name = "IMAGE_URL")
    private String imageUrl;

    @Column(name = "DELETED")
    private Boolean deleted;

    // STOCKテーブルのカラム（SecondaryTable）
    @Column(name = "QUANTITY", table = "STOCK")
    private Integer quantity;

    @Version
    @Column(name = "VERSION", table = "STOCK")
    private Long version;

    // Constructors
    public Book() {
    }

    // Getters and Setters
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
}
