package pro.kensait.backoffice.api.dto;

import java.math.BigDecimal;

/**
 * 書籍情報転送オブジェクト（Book Stock API用）
 * エンティティをマイクロサービス間で転送するためのDTO
 * ネストされた構造でカテゴリと出版社情報を含む
 */
public class BookTO {
    private Integer bookId;
    private String bookName;
    private String author;
    private BigDecimal price;
    private String imageUrl;
    private Integer quantity;
    private Long version;
    private CategoryInfo category;
    private PublisherInfo publisher;

    // デフォルトコンストラクタ（JSON-B用）
    public BookTO() {
    }

    // 全フィールドコンストラクタ
    public BookTO(Integer bookId, String bookName, String author,
                  BigDecimal price, String imageUrl, Integer quantity, Long version,
                  CategoryInfo category, PublisherInfo publisher) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.author = author;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.version = version;
        this.category = category;
        this.publisher = publisher;
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

    public CategoryInfo getCategory() {
        return category;
    }

    public void setCategory(CategoryInfo category) {
        this.category = category;
    }

    public PublisherInfo getPublisher() {
        return publisher;
    }

    public void setPublisher(PublisherInfo publisher) {
        this.publisher = publisher;
    }

    @Override
    public String toString() {
        return "BookTO [bookId=" + bookId + ", bookName=" + bookName + ", author=" + author + "]";
    }

    /**
     * カテゴリ情報
     */
    public static class CategoryInfo {
        private Integer categoryId;
        private String categoryName;

        public CategoryInfo() {
        }

        public CategoryInfo(Integer categoryId, String categoryName) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
        }

        public Integer getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Integer categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }
    }

    /**
     * 出版社情報
     */
    public static class PublisherInfo {
        private Integer publisherId;
        private String publisherName;

        public PublisherInfo() {
        }

        public PublisherInfo(Integer publisherId, String publisherName) {
            this.publisherId = publisherId;
            this.publisherName = publisherName;
        }

        public Integer getPublisherId() {
            return publisherId;
        }

        public void setPublisherId(Integer publisherId) {
            this.publisherId = publisherId;
        }

        public String getPublisherName() {
            return publisherName;
        }

        public void setPublisherName(String publisherName) {
            this.publisherName = publisherName;
        }
    }
}
