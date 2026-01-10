package pro.kensait.backoffice.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pro.kensait.backoffice.api.dto.*;
import pro.kensait.backoffice.common.*;
import pro.kensait.backoffice.dao.*;
import pro.kensait.backoffice.entity.*;
import pro.kensait.backoffice.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ワークフローサービス
 * 
 * ワークフローのビジネスロジックを提供する
 */
@ApplicationScoped
public class WorkflowService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowService.class);

    @Inject
    private WorkflowDao workflowDao;

    @Inject
    private EmployeeDao employeeDao;

    @Inject
    private BookDao bookDao;

    @Inject
    private DepartmentDao departmentDao;

    /**
     * ワークフロー作成
     * 
     * @param request 作成リクエスト
     * @return ワークフローTO
     */
    @Transactional
    public WorkflowTO createWorkflow(WorkflowCreateRequest request) {
        logger.info("[WorkflowService#createWorkflow] Creating workflow: type={}, createdBy={}", 
                   request.workflowType(), request.createdBy());

        // 社員情報を取得
        Employee operator = employeeDao.findById(request.createdBy());
        if (operator == null) {
            throw new IllegalArgumentException("社員が見つかりません: employeeId=" + request.createdBy());
        }

        // 新しいワークフローIDを生成
        Long workflowId = workflowDao.generateWorkflowId();

        // ワークフローエンティティを作成
        Workflow workflow = new Workflow();
        workflow.setWorkflowId(workflowId);
        workflow.setWorkflowType(WorkflowType.valueOf(request.workflowType()));
        workflow.setState(WorkflowState.CREATED);
        workflow.setBookId(request.bookId());
        workflow.setBookName(request.bookName());
        workflow.setAuthor(request.author());
        workflow.setCategoryId(request.categoryId());
        workflow.setPublisherId(request.publisherId());
        workflow.setPrice(request.price());
        workflow.setImageUrl(request.imageUrl());
        workflow.setApplyReason(request.applyReason());
        workflow.setStartDate(request.startDate());
        workflow.setEndDate(request.endDate());
        workflow.setOperationType(OperationType.CREATE);
        workflow.setOperator(operator);
        workflow.setOperatedAt(LocalDateTime.now());

        // 保存
        workflowDao.insert(workflow);

        logger.info("[WorkflowService#createWorkflow] Workflow created: workflowId={}, operationId={}", 
                   workflowId, workflow.getOperationId());

        return convertToTO(workflow);
    }

    /**
     * ワークフロー更新
     * 
     * @param workflowId ワークフローID
     * @param request 更新リクエスト
     * @return ワークフローTO
     */
    @Transactional
    public WorkflowTO updateWorkflow(Long workflowId, WorkflowUpdateRequest request) {
        logger.info("[WorkflowService#updateWorkflow] Updating workflow: workflowId={}, updatedBy={}", 
                   workflowId, request.updatedBy());

        // 最新のワークフロー状態を取得
        Workflow latest = workflowDao.findLatestByWorkflowId(workflowId);
        if (latest == null) {
            throw new WorkflowNotFoundException(workflowId);
        }

        // 状態チェック: CREATEDのみ更新可能
        if (latest.getState() != WorkflowState.CREATED) {
            throw new InvalidWorkflowStateException(latest.getState(), "UPDATE");
        }

        // 社員情報を取得
        Employee operator = employeeDao.findById(request.updatedBy());
        if (operator == null) {
            throw new IllegalArgumentException("社員が見つかりません: employeeId=" + request.updatedBy());
        }

        // 新しい操作履歴を作成（更新は新規INSERT）
        Workflow workflow = new Workflow();
        workflow.setWorkflowId(workflowId);
        workflow.setWorkflowType(latest.getWorkflowType());
        workflow.setState(WorkflowState.CREATED);
        workflow.setBookId(request.bookId() != null ? request.bookId() : latest.getBookId());
        workflow.setBookName(request.bookName() != null ? request.bookName() : latest.getBookName());
        workflow.setAuthor(request.author() != null ? request.author() : latest.getAuthor());
        workflow.setCategoryId(request.categoryId() != null ? request.categoryId() : latest.getCategoryId());
        workflow.setPublisherId(request.publisherId() != null ? request.publisherId() : latest.getPublisherId());
        workflow.setPrice(request.price() != null ? request.price() : latest.getPrice());
        workflow.setImageUrl(request.imageUrl() != null ? request.imageUrl() : latest.getImageUrl());
        workflow.setApplyReason(request.applyReason() != null ? request.applyReason() : latest.getApplyReason());
        workflow.setStartDate(request.startDate() != null ? request.startDate() : latest.getStartDate());
        workflow.setEndDate(request.endDate() != null ? request.endDate() : latest.getEndDate());
        workflow.setOperationType(OperationType.CREATE);
        workflow.setOperator(operator);
        workflow.setOperatedAt(LocalDateTime.now());

        // 保存
        workflowDao.insert(workflow);

        logger.info("[WorkflowService#updateWorkflow] Workflow updated: workflowId={}, operationId={}", 
                   workflowId, workflow.getOperationId());

        return convertToTO(workflow);
    }

    /**
     * ワークフロー申請
     * 
     * @param workflowId ワークフローID
     * @param request 操作リクエスト
     * @return ワークフローTO
     */
    @Transactional
    public WorkflowTO applyWorkflow(Long workflowId, WorkflowOperationRequest request) {
        logger.info("[WorkflowService#applyWorkflow] Applying workflow: workflowId={}, operatedBy={}", 
                   workflowId, request.operatedBy());

        // 最新のワークフロー状態を取得
        Workflow latest = workflowDao.findLatestByWorkflowId(workflowId);
        if (latest == null) {
            throw new WorkflowNotFoundException(workflowId);
        }

        // 状態チェック: CREATEDのみ申請可能
        if (latest.getState() != WorkflowState.CREATED) {
            throw new InvalidWorkflowStateException(latest.getState(), "APPLY");
        }

        // 社員情報を取得
        Employee operator = employeeDao.findById(request.operatedBy());
        if (operator == null) {
            throw new IllegalArgumentException("社員が見つかりません: employeeId=" + request.operatedBy());
        }

        // 新しい操作履歴を作成
        Workflow workflow = createOperationHistory(latest, WorkflowState.APPLIED, 
                                                   OperationType.APPLY, operator, request.operationReason());

        // 保存
        workflowDao.insert(workflow);

        logger.info("[WorkflowService#applyWorkflow] Workflow applied: workflowId={}, operationId={}", 
                   workflowId, workflow.getOperationId());

        return convertToTO(workflow);
    }

    /**
     * ワークフロー承認
     * 
     * @param workflowId ワークフローID
     * @param request 操作リクエスト
     * @return ワークフローTO
     */
    @Transactional
    public WorkflowTO approveWorkflow(Long workflowId, WorkflowOperationRequest request) {
        logger.info("[WorkflowService#approveWorkflow] Approving workflow: workflowId={}, operatedBy={}", 
                   workflowId, request.operatedBy());

        // 最新のワークフロー状態を取得
        Workflow latest = workflowDao.findLatestByWorkflowId(workflowId);
        if (latest == null) {
            throw new WorkflowNotFoundException(workflowId);
        }

        // 状態チェック: APPLIEDのみ承認可能
        if (latest.getState() != WorkflowState.APPLIED) {
            logger.warn("[WorkflowService#approveWorkflow] Invalid state transition: workflowId={}, currentState={}", 
                       workflowId, latest.getState());
            throw new InvalidWorkflowStateException(latest.getState(), "APPROVE");
        }

        // 承認者情報を取得
        Employee approver = employeeDao.findById(request.operatedBy());
        if (approver == null) {
            throw new IllegalArgumentException("社員が見つかりません: employeeId=" + request.operatedBy());
        }

        // 承認権限チェック
        checkApprovalAuthority(approver, latest);

        // 新しい操作履歴を作成
        Workflow workflow = createOperationHistory(latest, WorkflowState.APPROVED, 
                                                   OperationType.APPROVE, approver, request.operationReason());

        // 保存
        workflowDao.insert(workflow);

        // 書籍マスタに反映
        applyToBookMaster(workflow);

        logger.info("[WorkflowService#approveWorkflow] Workflow approved and applied to book master: workflowId={}", 
                   workflowId);

        return convertToTO(workflow);
    }

    /**
     * ワークフロー却下
     * 
     * @param workflowId ワークフローID
     * @param request 操作リクエスト
     * @return ワークフローTO
     */
    @Transactional
    public WorkflowTO rejectWorkflow(Long workflowId, WorkflowOperationRequest request) {
        logger.info("[WorkflowService#rejectWorkflow] Rejecting workflow: workflowId={}, operatedBy={}", 
                   workflowId, request.operatedBy());

        // 最新のワークフロー状態を取得
        Workflow latest = workflowDao.findLatestByWorkflowId(workflowId);
        if (latest == null) {
            throw new WorkflowNotFoundException(workflowId);
        }

        // 状態チェック: APPLIEDのみ却下可能
        if (latest.getState() != WorkflowState.APPLIED) {
            throw new InvalidWorkflowStateException(latest.getState(), "REJECT");
        }

        // 社員情報を取得
        Employee operator = employeeDao.findById(request.operatedBy());
        if (operator == null) {
            throw new IllegalArgumentException("社員が見つかりません: employeeId=" + request.operatedBy());
        }

        // 新しい操作履歴を作成（却下後はCREATED状態に戻る）
        Workflow workflow = createOperationHistory(latest, WorkflowState.CREATED, 
                                                   OperationType.REJECT, operator, request.operationReason());

        // 保存
        workflowDao.insert(workflow);

        logger.info("[WorkflowService#rejectWorkflow] Workflow rejected: workflowId={}, operationId={}", 
                   workflowId, workflow.getOperationId());

        return convertToTO(workflow);
    }

    /**
     * ワークフロー一覧取得
     * 
     * @param state 状態（オプション）
     * @param workflowType ワークフロータイプ（オプション）
     * @param employeeId 社員ID（オプション）
     * @return ワークフローTOのリスト
     */
    public List<WorkflowTO> getWorkflows(String state, String workflowType, Long employeeId) {
        logger.debug("[WorkflowService#getWorkflows] Getting workflows: state={}, type={}, employeeId={}", 
                    state, workflowType, employeeId);

        List<Workflow> workflows;

        if (employeeId != null) {
            workflows = workflowDao.findAllLatestByEmployeeId(employeeId);
        } else if (state != null) {
            WorkflowState stateEnum = WorkflowState.valueOf(state);
            workflows = workflowDao.findAllLatestByState(stateEnum);
        } else if (workflowType != null) {
            WorkflowType typeEnum = WorkflowType.valueOf(workflowType);
            workflows = workflowDao.findAllLatestByType(typeEnum);
        } else {
            workflows = workflowDao.findAllLatest();
        }

        logger.debug("[WorkflowService#getWorkflows] Found {} workflows", workflows.size());

        return workflows.stream()
                        .map(this::convertToTO)
                        .collect(Collectors.toList());
    }

    /**
     * ワークフロー履歴取得
     * 
     * @param workflowId ワークフローID
     * @return ワークフローTOのリスト
     */
    public List<WorkflowTO> getWorkflowHistory(Long workflowId) {
        logger.debug("[WorkflowService#getWorkflowHistory] Getting workflow history: workflowId={}", workflowId);

        List<Workflow> history = workflowDao.findHistoryByWorkflowId(workflowId);

        if (history.isEmpty()) {
            throw new WorkflowNotFoundException(workflowId);
        }

        logger.debug("[WorkflowService#getWorkflowHistory] Found {} history records", history.size());

        return history.stream()
                      .map(this::convertToTO)
                      .collect(Collectors.toList());
    }

    /**
     * 承認権限チェック
     * 
     * @param approver 承認者
     * @param workflow ワークフロー
     * @throws UnauthorizedApprovalException 承認権限がない場合
     */
    private void checkApprovalAuthority(Employee approver, Workflow workflow) {
        logger.debug("[WorkflowService#checkApprovalAuthority] Checking approval authority: approverId={}, jobRank={}", 
                    approver.getEmployeeId(), approver.getJobRank());

        // 職務ランクを取得
        JobRankType jobRank;
        try {
            jobRank = JobRankType.fromRank(approver.getJobRank());
        } catch (IllegalArgumentException e) {
            logger.warn("[WorkflowService#checkApprovalAuthority] Invalid job rank: {}", approver.getJobRank());
            throw new UnauthorizedApprovalException(approver.getEmployeeId(), "不正な職務ランク");
        }

        // マネージャー以上かチェック
        if (!jobRank.isManagerOrAbove()) {
            logger.warn("[WorkflowService#checkApprovalAuthority] Unauthorized approval attempt: employeeId={}, jobRank={}", 
                       approver.getEmployeeId(), jobRank);
            throw new UnauthorizedApprovalException(approver.getEmployeeId(), "承認権限がありません（マネージャー以上が必要）");
        }

        // ディレクターは全部署承認可能
        if (jobRank.isDirector()) {
            logger.debug("[WorkflowService#checkApprovalAuthority] Director approval authorized");
            return;
        }

        // マネージャーは同一部署のみ承認可能
        // 申請者の部署を取得
        Employee applicant = workflow.getOperator();
        if (applicant.getDepartment() == null || approver.getDepartment() == null) {
            logger.warn("[WorkflowService#checkApprovalAuthority] Department information missing");
            throw new UnauthorizedApprovalException(approver.getEmployeeId(), "部署情報が不足しています");
        }

        Long applicantDeptId = applicant.getDepartment().getDepartmentId();
        Long approverDeptId = approver.getDepartment().getDepartmentId();

        if (!applicantDeptId.equals(approverDeptId)) {
            logger.warn("[WorkflowService#checkApprovalAuthority] Manager cannot approve different department: " +
                       "approverDept={}, applicantDept={}", approverDeptId, applicantDeptId);
            throw new UnauthorizedApprovalException(approver.getEmployeeId(), 
                                                   "マネージャーは同一部署のワークフローのみ承認可能です");
        }

        logger.debug("[WorkflowService#checkApprovalAuthority] Manager approval authorized (same department)");
    }

    /**
     * 書籍マスタに反映
     * 
     * @param workflow ワークフロー
     */
    private void applyToBookMaster(Workflow workflow) {
        logger.info("[WorkflowService#applyToBookMaster] Applying to book master: workflowId={}, type={}", 
                   workflow.getWorkflowId(), workflow.getWorkflowType());

        switch (workflow.getWorkflowType()) {
            case ADD_NEW_BOOK:
                addNewBook(workflow);
                break;
            case REMOVE_BOOK:
                removeBook(workflow);
                break;
            case ADJUST_BOOK_PRICE:
                adjustBookPrice(workflow);
                break;
            default:
                throw new IllegalArgumentException("Unknown workflow type: " + workflow.getWorkflowType());
        }
    }

    /**
     * 新規書籍追加
     * 
     * @param workflow ワークフロー
     */
    private void addNewBook(Workflow workflow) {
        logger.info("[WorkflowService#addNewBook] Adding new book: bookName={}", workflow.getBookName());

        Book book = new Book();
        book.setBookName(workflow.getBookName());
        book.setAuthor(workflow.getAuthor());
        book.setPrice(workflow.getPrice());
        book.setImageUrl(workflow.getImageUrl());
        book.setDeleted(false);

        // Category設定
        if (workflow.getCategoryId() != null) {
            Category category = new Category();
            category.setCategoryId(workflow.getCategoryId());
            book.setCategory(category);
        }

        // Publisher設定
        if (workflow.getPublisherId() != null) {
            Publisher publisher = new Publisher();
            publisher.setPublisherId(workflow.getPublisherId());
            book.setPublisher(publisher);
        }

        // 初期在庫設定
        book.setQuantity(0);

        bookDao.insert(book);

        logger.info("[WorkflowService#addNewBook] Book added: bookId={}", book.getBookId());
    }

    /**
     * 書籍削除（論理削除）
     * 
     * @param workflow ワークフロー
     */
    private void removeBook(Workflow workflow) {
        logger.info("[WorkflowService#removeBook] Removing book: bookId={}", workflow.getBookId());

        Book book = bookDao.findById(workflow.getBookId());
        if (book == null) {
            throw new IllegalArgumentException("書籍が見つかりません: bookId=" + workflow.getBookId());
        }

        book.setDeleted(true);
        bookDao.update(book);

        logger.info("[WorkflowService#removeBook] Book removed (logical delete): bookId={}", workflow.getBookId());
    }

    /**
     * 書籍価格改定
     * 
     * @param workflow ワークフロー
     */
    private void adjustBookPrice(Workflow workflow) {
        logger.info("[WorkflowService#adjustBookPrice] Adjusting book price: bookId={}, newPrice={}", 
                   workflow.getBookId(), workflow.getPrice());

        Book book = bookDao.findById(workflow.getBookId());
        if (book == null) {
            throw new IllegalArgumentException("書籍が見つかりません: bookId=" + workflow.getBookId());
        }

        book.setPrice(workflow.getPrice());
        bookDao.update(book);

        logger.info("[WorkflowService#adjustBookPrice] Book price adjusted: bookId={}, price={}", 
                   workflow.getBookId(), workflow.getPrice());
    }

    /**
     * 操作履歴を作成（前の状態をコピー）
     * 
     * @param previous 前の操作履歴
     * @param newState 新しい状態
     * @param operationType 操作タイプ
     * @param operator 操作者
     * @param operationReason 操作理由
     * @return 新しい操作履歴
     */
    private Workflow createOperationHistory(Workflow previous, WorkflowState newState, 
                                           OperationType operationType, Employee operator, 
                                           String operationReason) {
        Workflow workflow = new Workflow();
        workflow.setWorkflowId(previous.getWorkflowId());
        workflow.setWorkflowType(previous.getWorkflowType());
        workflow.setState(newState);
        workflow.setBookId(previous.getBookId());
        workflow.setBookName(previous.getBookName());
        workflow.setAuthor(previous.getAuthor());
        workflow.setCategoryId(previous.getCategoryId());
        workflow.setPublisherId(previous.getPublisherId());
        workflow.setPrice(previous.getPrice());
        workflow.setImageUrl(previous.getImageUrl());
        workflow.setApplyReason(previous.getApplyReason());
        workflow.setStartDate(previous.getStartDate());
        workflow.setEndDate(previous.getEndDate());
        workflow.setOperationType(operationType);
        workflow.setOperator(operator);
        workflow.setOperatedAt(LocalDateTime.now());
        workflow.setOperationReason(operationReason);
        return workflow;
    }

    /**
     * WorkflowエンティティをWorkflowTOに変換
     * 
     * @param workflow ワークフローエンティティ
     * @return ワークフローTO
     */
    private WorkflowTO convertToTO(Workflow workflow) {
        return new WorkflowTO(
            workflow.getOperationId(),
            workflow.getWorkflowId(),
            workflow.getWorkflowType() != null ? workflow.getWorkflowType().name() : null,
            workflow.getState() != null ? workflow.getState().name() : null,
            workflow.getBookId(),
            workflow.getBookName(),
            workflow.getAuthor(),
            workflow.getCategoryId(),
            workflow.getPublisherId(),
            workflow.getPrice(),
            workflow.getImageUrl(),
            workflow.getApplyReason(),
            workflow.getStartDate(),
            workflow.getEndDate(),
            workflow.getOperationType() != null ? workflow.getOperationType().name() : null,
            workflow.getOperator() != null ? workflow.getOperator().getEmployeeId() : null,
            workflow.getOperator() != null ? workflow.getOperator().getEmployeeName() : null,
            workflow.getOperator() != null ? workflow.getOperator().getEmployeeCode() : null,
            workflow.getOperatedAt(),
            workflow.getOperationReason()
        );
    }
}
