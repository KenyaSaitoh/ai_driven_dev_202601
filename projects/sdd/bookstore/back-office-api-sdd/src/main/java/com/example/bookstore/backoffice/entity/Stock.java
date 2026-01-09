package com.example.bookstore.backoffice.entity;

import jakarta.persistence.*;

/**
 * 在庫エンティティ
 * 楽観的ロック対応（@Version）
 */
@Entity
@Table(name = "STOCK")
public class Stock {

    @Id
    @Column(name = "BOOK_ID")
    private Integer bookId;

    @Column(name = "QUANTITY")
    private Integer quantity;

    @Version
    @Column(name = "VERSION")
    private Long version;

    // Constructors
    public Stock() {
    }

    // Getters and Setters
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
}
