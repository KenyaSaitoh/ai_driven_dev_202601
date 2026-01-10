package pro.kensait.backoffice.entity;

import jakarta.persistence.*;
import pro.kensait.backoffice.common.WorkflowType;
import pro.kensait.backoffice.common.WorkflowState;
import pro.kensait.backoffice.common.OperationType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * ワークフローエンティティ
 * 
 * テーブル: WORKFLOW
 * ワークフローの操作履歴を管理する
 * 1つのワークフローに対して複数の操作履歴行を保持
 */
@Entity
@Table(name = "WORKFLOW")
public class Workflow implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OPERATION_ID")
    private Long operationId;

    @Column(name = "WORKFLOW_ID")
    private Long workflowId;

    @Enumerated(EnumType.STRING)
    @Column(name = "WORKFLOW_TYPE", length = 30)
    private WorkflowType workflowType;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATE", length = 20)
    private WorkflowState state;

    @Column(name = "BOOK_ID")
    private Integer bookId;

    @Column(name = "BOOK_NAME", length = 200)
    private String bookName;

    @Column(name = "AUTHOR", length = 100)
    private String author;

    @Column(name = "CATEGORY_ID")
    private Integer categoryId;

    @Column(name = "PUBLISHER_ID")
    private Integer publisherId;

    @Column(name = "PRICE", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "IMAGE_URL", length = 500)
    private String imageUrl;

    @Column(name = "APPLY_REASON", length = 500)
    private String applyReason;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "OPERATION_TYPE", length = 20)
    private OperationType operationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OPERATED_BY")
    private Employee operator;

    @Column(name = "OPERATED_AT")
    private LocalDateTime operatedAt;

    @Column(name = "OPERATION_REASON", length = 500)
    private String operationReason;

    // 参照用（更新不可）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOK_ID", insertable = false, updatable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID", insertable = false, updatable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PUBLISHER_ID", insertable = false, updatable = false)
    private Publisher publisher;

    // デフォルトコンストラクタ（JPA必須）
    public Workflow() {
    }

    // コンストラクタ
    public Workflow(Long operationId, Long workflowId, WorkflowType workflowType, 
                    WorkflowState state, Integer bookId, String bookName, String author,
                    Integer categoryId, Integer publisherId, BigDecimal price, String imageUrl,
                    String applyReason, LocalDate startDate, LocalDate endDate,
                    OperationType operationType, Employee operator, LocalDateTime operatedAt,
                    String operationReason) {
        this.operationId = operationId;
        this.workflowId = workflowId;
        this.workflowType = workflowType;
        this.state = state;
        this.bookId = bookId;
        this.bookName = bookName;
        this.author = author;
        this.categoryId = categoryId;
        this.publisherId = publisherId;
        this.price = price;
        this.imageUrl = imageUrl;
        this.applyReason = applyReason;
        this.startDate = startDate;
        this.endDate = endDate;
        this.operationType = operationType;
        this.operator = operator;
        this.operatedAt = operatedAt;
        this.operationReason = operationReason;
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

    public WorkflowType getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(WorkflowType workflowType) {
        this.workflowType = workflowType;
    }

    public WorkflowState getState() {
        return state;
    }

    public void setState(WorkflowState state) {
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

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public Employee getOperator() {
        return operator;
    }

    public void setOperator(Employee operator) {
        this.operator = operator;
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

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
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

    // equals/hashCode（主キーベース）
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Workflow workflow = (Workflow) o;
        return Objects.equals(operationId, workflow.operationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId);
    }

    // toString
    @Override
    public String toString() {
        return "Workflow{" +
                "operationId=" + operationId +
                ", workflowId=" + workflowId +
                ", workflowType=" + workflowType +
                ", state=" + state +
                ", bookId=" + bookId +
                ", operationType=" + operationType +
                ", operatedAt=" + operatedAt +
                '}';
    }
}
