package pro.kensait.backoffice.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ワークフロー情報転送オブジェクト
 * エンティティをマイクロサービス間で転送するためのDTO
 * ネスト構造でEmployee, Department, Book情報を含む
 */
public class WorkflowTO {
    private Long operationId;
    private Long workflowId;
    private String workflowType;
    private String state;
    private Integer bookId;
    private String bookName;
    private String author;
    private Integer categoryId;
    private String categoryName;
    private Integer publisherId;
    private String publisherName;
    private BigDecimal price;
    private String imageUrl;
    private String applyReason;
    private LocalDate startDate;
    private LocalDate endDate;
    private String operationType;
    private Long operatedBy;
    private String operatorName;
    private String operatorCode;
    private Long departmentId;
    private String departmentName;
    private Integer jobRank;
    private LocalDateTime operatedAt;
    private String operationReason;

    // デフォルトコンストラクタ
    public WorkflowTO() {
    }

    // Getter/Setter
    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public Long getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public Long getOperatedBy() {
        return operatedBy;
    }

    public void setOperatedBy(Long operatedBy) {
        this.operatedBy = operatedBy;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getOperatorCode() {
        return operatorCode;
    }

    public void setOperatorCode(String operatorCode) {
        this.operatorCode = operatorCode;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Integer getJobRank() {
        return jobRank;
    }

    public void setJobRank(Integer jobRank) {
        this.jobRank = jobRank;
    }

    public LocalDateTime getOperatedAt() {
        return operatedAt;
    }

    public void setOperatedAt(LocalDateTime operatedAt) {
        this.operatedAt = operatedAt;
    }

    public String getOperationReason() {
        return operationReason;
    }

    public void setOperationReason(String operationReason) {
        this.operationReason = operationReason;
    }

    @Override
    public String toString() {
        return "WorkflowTO [operationId=" + operationId + ", workflowId=" + workflowId 
                + ", workflowType=" + workflowType + ", state=" + state 
                + ", operationType=" + operationType + "]";
    }
}


