package pro.kensait.backoffice.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * ワークフローエンティティ（履歴形式）
 * WORKFLOW テーブルに対応
 * 1つのワークフローに対して複数の操作履歴行を持つ
 */
@Entity
@Table(name = "WORKFLOW")
public class Workflow implements Serializable {
    private static final long serialVersionUID = 1L;

    // 操作ID（主キー）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OPERATION_ID")
    private Long operationId;

    // ワークフローID（同じワークフローで共通）
    @Column(name = "WORKFLOW_ID")
    private Long workflowId;

    // ワークフロー種別 (ADD_NEW_BOOK, REMOVE_BOOK, ADJUST_BOOK_PRICE)
    @Column(name = "WORKFLOW_TYPE")
    private String workflowType;

    // 状態 (CREATED, APPLIED, APPROVED)
    @Column(name = "STATE")
    private String state;

    // 対象書籍ID
    @Column(name = "BOOK_ID")
    private Integer bookId;

    // 対象書籍（参照）
    @ManyToOne(targetEntity = Book.class)
    @JoinColumn(name = "BOOK_ID", referencedColumnName = "BOOK_ID", insertable = false, updatable = false)
    private Book book;

    // 書籍名（CREATE用）
    @Column(name = "BOOK_NAME")
    private String bookName;

    // 著者（CREATE用）
    @Column(name = "AUTHOR")
    private String author;

    // カテゴリID
    @Column(name = "CATEGORY_ID")
    private Integer categoryId;

    // カテゴリ（参照）
    @ManyToOne(targetEntity = Category.class)
    @JoinColumn(name = "CATEGORY_ID", referencedColumnName = "CATEGORY_ID", insertable = false, updatable = false)
    private Category category;

    // 出版社ID
    @Column(name = "PUBLISHER_ID")
    private Integer publisherId;

    // 出版社（参照）
    @ManyToOne(targetEntity = Publisher.class)
    @JoinColumn(name = "PUBLISHER_ID", referencedColumnName = "PUBLISHER_ID", insertable = false, updatable = false)
    private Publisher publisher;

    // 価格
    @Column(name = "PRICE")
    private BigDecimal price;

    // 画像URL
    @Column(name = "IMAGE_URL")
    private String imageUrl;

    // 申請理由
    @Column(name = "APPLY_REASON")
    private String applyReason;

    // 適用開始日
    @Column(name = "START_DATE")
    private LocalDate startDate;

    // 適用終了日
    @Column(name = "END_DATE")
    private LocalDate endDate;

    // 操作タイプ (CREATE, APPLY, APPROVE, REJECT)
    @Column(name = "OPERATION_TYPE")
    private String operationType;

    // 操作者ID
    @Column(name = "OPERATED_BY")
    private Long operatedBy;

    // 操作者（参照）
    @ManyToOne(targetEntity = Employee.class)
    @JoinColumn(name = "OPERATED_BY", referencedColumnName = "EMPLOYEE_ID", insertable = false, updatable = false)
    private Employee operator;

    // 操作日時
    @Column(name = "OPERATED_AT")
    private LocalDateTime operatedAt;

    // 操作理由（主にREJECTの場合）
    @Column(name = "OPERATION_REASON")
    private String operationReason;

    // デフォルトコンストラクタ
    public Workflow() {
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

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Integer getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Integer publisherId) {
        this.publisherId = publisherId;
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

    @Override
    public String toString() {
        return "Workflow [operationId=" + operationId + ", workflowId=" + workflowId 
                + ", workflowType=" + workflowType + ", state=" + state 
                + ", operationType=" + operationType + "]";
    }
}

