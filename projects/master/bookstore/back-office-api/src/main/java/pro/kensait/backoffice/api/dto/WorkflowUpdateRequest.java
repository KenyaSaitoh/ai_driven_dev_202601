package pro.kensait.backoffice.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ワークフロー更新リクエストDTO
 */
public class WorkflowUpdateRequest {
    private Integer bookId; // DELETE, PRICE_TEMP_ADJUSTMENTの場合に必要
    private String bookName; // CREATEの場合に必要
    private String author; // CREATEの場合に必要
    private Integer categoryId; // CREATEの場合に必要
    private Integer publisherId; // CREATEの場合に必要
    private BigDecimal price; // CREATE, PRICE_TEMP_ADJUSTMENTの場合に必要
    private String imageUrl; // CREATEの場合
    private String applyReason; // 申請理由
    private LocalDate startDate; // PRICE_TEMP_ADJUSTMENTの場合に必要
    private LocalDate endDate; // PRICE_TEMP_ADJUSTMENTの場合に必要
    private Long updatedBy; // 更新者ID

    // デフォルトコンストラクタ
    public WorkflowUpdateRequest() {
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

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Integer publisherId) {
        this.publisherId = publisherId;
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

    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "WorkflowUpdateRequest [updatedBy=" + updatedBy + "]";
    }
}

