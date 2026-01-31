package pro.kensait.backoffice.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ワークフロー作成リクエストDTO
 */
public class WorkflowCreateRequest {
    private String workflowType; // ADD_NEW_BOOK, REMOVE_BOOK, ADJUST_BOOK_PRICE
    private Integer bookId; // REMOVE_BOOK, ADJUST_BOOK_PRICEの場合に必要
    private String bookName; // ADD_NEW_BOOKの場合に必要
    private String author; // ADD_NEW_BOOKの場合に必要
    private Integer categoryId; // ADD_NEW_BOOKの場合に必要
    private Integer publisherId; // ADD_NEW_BOOKの場合に必要
    private BigDecimal price; // ADD_NEW_BOOK, ADJUST_BOOK_PRICEの場合に必要
    private String imageUrl; // ADD_NEW_BOOKの場合
    private String applyReason; // 申請理由
    private LocalDate startDate; // ADJUST_BOOK_PRICEの場合に必要
    private LocalDate endDate; // ADJUST_BOOK_PRICEの場合に必要
    private Long createdBy; // 作成者ID

    // デフォルトコンストラクタ
    public WorkflowCreateRequest() {
    }

    // Getter/Setter
    public String getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
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

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "WorkflowCreateRequest [workflowType=" + workflowType + ", createdBy=" + createdBy + "]";
    }
}


