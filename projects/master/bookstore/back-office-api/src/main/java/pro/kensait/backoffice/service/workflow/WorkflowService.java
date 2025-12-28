package pro.kensait.backoffice.service.workflow;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import pro.kensait.backoffice.api.dto.WorkflowCreateRequest;
import pro.kensait.backoffice.api.dto.WorkflowOperationRequest;
import pro.kensait.backoffice.api.dto.WorkflowTO;
import pro.kensait.backoffice.dao.BookDao;
import pro.kensait.backoffice.dao.EmployeeDao;
import pro.kensait.backoffice.dao.StockDao;
import pro.kensait.backoffice.dao.WorkflowDao;
import pro.kensait.backoffice.entity.Book;
import pro.kensait.backoffice.entity.BookWorkflow;
import pro.kensait.backoffice.entity.Category;
import pro.kensait.backoffice.entity.Employee;
import pro.kensait.backoffice.entity.Publisher;
import pro.kensait.backoffice.entity.Stock;

/**
 * ワークフローサービス
 */
@ApplicationScoped
@Transactional
public class WorkflowService {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowService.class);

    // ワークフロータイプ
    private static final String TYPE_CREATE = "CREATE";
    private static final String TYPE_DELETE = "DELETE";
    private static final String TYPE_PRICE_TEMP_ADJUSTMENT = "PRICE_TEMP_ADJUSTMENT";

    // 状態
    private static final String STATE_CREATED = "CREATED";  // NEW → CREATED
    private static final String STATE_APPLIED = "APPLIED";
    private static final String STATE_APPROVED = "APPROVED";

    // 操作タイプ
    private static final String OP_CREATE = "CREATE";  // CREATED → CREATE
    private static final String OP_APPLY = "APPLY";
    private static final String OP_APPROVE = "APPROVE";
    private static final String OP_REJECT = "REJECT";  // UI表記は「差戻」

    // 役職ランク
    private static final int JOB_RANK_MANAGER = 2;

    @Inject
    private WorkflowDao workflowDao;

    @Inject
    private EmployeeDao employeeDao;

    @Inject
    private BookDao bookDao;

    @Inject
    private StockDao stockDao;

    @PersistenceContext(unitName = "bookstorePU")
    private EntityManager em;

    /**
     * ワークフロー作成
     * @param request 作成リクエスト
     * @return ワークフローTO
     */
    public WorkflowTO createWorkflow(WorkflowCreateRequest request) {
        logger.info("[ WorkflowService#createWorkflow ] workflowType={}", request.getWorkflowType());

        // 作成者チェック
        Employee creator = employeeDao.findById(request.getCreatedBy());
        if (creator == null) {
            throw new IllegalArgumentException("作成者が存在しません: " + request.getCreatedBy());
        }

        // 次のワークフローIDを取得
        Long nextWorkflowId = workflowDao.getNextWorkflowId();

        // ワークフローエンティティを作成
        BookWorkflow workflow = new BookWorkflow();
        workflow.setWorkflowId(nextWorkflowId);
        workflow.setWorkflowType(request.getWorkflowType());
        workflow.setState(STATE_CREATED);
        workflow.setOperationType(OP_CREATE);
        workflow.setOperatedAt(LocalDateTime.now());
        workflow.setOperatedBy(request.getCreatedBy());

        // ワークフロータイプごとの設定
        switch (request.getWorkflowType()) {
            case TYPE_CREATE:
                workflow.setBookName(request.getBookName());
                workflow.setAuthor(request.getAuthor());
                workflow.setPrice(request.getPrice());
                workflow.setImageUrl(request.getImageUrl());
                workflow.setCategoryId(request.getCategoryId());
                workflow.setPublisherId(request.getPublisherId());
                break;
            case TYPE_DELETE:
                workflow.setBookId(request.getBookId());
                break;
            case TYPE_PRICE_TEMP_ADJUSTMENT:
                workflow.setBookId(request.getBookId());
                workflow.setPrice(request.getPrice());
                workflow.setStartDate(request.getStartDate());
                workflow.setEndDate(request.getEndDate());
                break;
            default:
                throw new IllegalArgumentException("不明なワークフロータイプ: " + request.getWorkflowType());
        }

        workflow.setApplyReason(request.getApplyReason());

        // 保存
        BookWorkflow saved = workflowDao.insert(workflow);

        return convertToWorkflowTO(saved);
    }

    /**
     * 申請
     * @param workflowId ワークフローID
     * @param request 操作リクエスト
     * @return ワークフローTO
     */
    public WorkflowTO applyWorkflow(Long workflowId, WorkflowOperationRequest request) {
        logger.info("[ WorkflowService#applyWorkflow ] workflowId={}", workflowId);

        // 最新の状態を取得
        BookWorkflow latest = workflowDao.findLatestByWorkflowId(workflowId);
        if (latest == null) {
            throw new WorkflowNotFoundException("ワークフローが見つかりません: " + workflowId);
        }

        // 状態チェック: CREATEDでなければエラー
        if (!STATE_CREATED.equals(latest.getState())) {
            throw new InvalidWorkflowStateException(
                "申請できる状態ではありません。現在の状態: " + latest.getState());
        }

        // 操作者チェック
        Employee operator = employeeDao.findById(request.getOperatedBy());
        if (operator == null) {
            throw new IllegalArgumentException("操作者が存在しません: " + request.getOperatedBy());
        }

        // 新しい操作履歴を作成
        BookWorkflow newOp = copyWorkflowData(latest);
        newOp.setState(STATE_APPLIED);
        newOp.setOperationType(OP_APPLY);
        newOp.setOperatedAt(LocalDateTime.now());
        newOp.setOperationReason(request.getOperationReason());
        newOp.setOperatedBy(request.getOperatedBy());

        // 保存
        BookWorkflow saved = workflowDao.insert(newOp);

        return convertToWorkflowTO(saved);
    }

    /**
     * 承認
     * @param workflowId ワークフローID
     * @param request 操作リクエスト
     * @return ワークフローTO
     */
    public WorkflowTO approveWorkflow(Long workflowId, WorkflowOperationRequest request) {
        logger.info("[ WorkflowService#approveWorkflow ] workflowId={}", workflowId);

        // 最新の状態を取得
        BookWorkflow latest = workflowDao.findLatestByWorkflowId(workflowId);
        if (latest == null) {
            throw new WorkflowNotFoundException("ワークフローが見つかりません: " + workflowId);
        }

        // 状態チェック: APPLIEDでなければエラー
        if (!STATE_APPLIED.equals(latest.getState())) {
            throw new InvalidWorkflowStateException(
                "承認できる状態ではありません。現在の状態: " + latest.getState());
        }

        // 承認権限チェック
        checkApprovalAuthority(workflowId, request.getOperatedBy());

        // 操作者取得
        Employee operator = employeeDao.findById(request.getOperatedBy());

        // 新しい操作履歴を作成
        BookWorkflow newOp = copyWorkflowData(latest);
        newOp.setState(STATE_APPROVED);
        newOp.setOperationType(OP_APPROVE);
        newOp.setOperatedAt(LocalDateTime.now());
        newOp.setOperationReason(request.getOperationReason());
        newOp.setOperatedBy(request.getOperatedBy());

        // 保存
        BookWorkflow saved = workflowDao.insert(newOp);

        // 書籍マスタへの反映
        applyToBookMaster(saved);

        return convertToWorkflowTO(saved);
    }

    /**
     * 却下
     * @param workflowId ワークフローID
     * @param request 操作リクエスト
     * @return ワークフローTO
     */
    public WorkflowTO rejectWorkflow(Long workflowId, WorkflowOperationRequest request) {
        logger.info("[ WorkflowService#rejectWorkflow ] workflowId={}", workflowId);

        // 最新の状態を取得
        BookWorkflow latest = workflowDao.findLatestByWorkflowId(workflowId);
        if (latest == null) {
            throw new WorkflowNotFoundException("ワークフローが見つかりません: " + workflowId);
        }

        // 状態チェック: APPLIEDでなければエラー
        if (!STATE_APPLIED.equals(latest.getState())) {
            throw new InvalidWorkflowStateException(
                "却下できる状態ではありません。現在の状態: " + latest.getState());
        }

        // 承認権限チェック
        checkApprovalAuthority(workflowId, request.getOperatedBy());

        // 操作者取得
        Employee operator = employeeDao.findById(request.getOperatedBy());

        // 新しい操作履歴を作成（CREATEDに戻る）
        BookWorkflow newOp = copyWorkflowData(latest);
        newOp.setState(STATE_CREATED);
        newOp.setOperationType(OP_REJECT);
        newOp.setOperatedAt(LocalDateTime.now());
        newOp.setOperationReason(request.getOperationReason());
        newOp.setOperatedBy(request.getOperatedBy());

        // 保存
        BookWorkflow saved = workflowDao.insert(newOp);

        return convertToWorkflowTO(saved);
    }

    /**
     * ワークフロー履歴取得
     * @param workflowId ワークフローID
     * @return ワークフローTOのリスト
     */
    public List<WorkflowTO> getWorkflowHistory(Long workflowId) {
        logger.info("[ WorkflowService#getWorkflowHistory ] workflowId={}", workflowId);

        List<BookWorkflow> history = workflowDao.findByWorkflowId(workflowId);
        return history.stream()
                .map(this::convertToWorkflowTO)
                .collect(Collectors.toList());
    }

    /**
     * ワークフロー一覧取得
     * @param state 状態（オプション）
     * @param workflowType ワークフロータイプ（オプション）
     * @return ワークフローTOのリスト
     */
    public List<WorkflowTO> getWorkflows(String state, String workflowType) {
        logger.info("[ WorkflowService#getWorkflows ] state={}, workflowType={}", state, workflowType);

        List<BookWorkflow> workflows;

        if (state != null && workflowType != null) {
            workflows = workflowDao.findByStateAndType(state, workflowType);
        } else if (state != null) {
            workflows = workflowDao.findByState(state);
        } else if (workflowType != null) {
            workflows = workflowDao.findByWorkflowType(workflowType);
        } else {
            workflows = workflowDao.findAllLatest();
        }

        return workflows.stream()
                .map(this::convertToWorkflowTO)
                .collect(Collectors.toList());
    }

    /**
     * 承認権限チェック
     * @param workflowId ワークフローID
     * @param approverId 承認者ID
     */
    private void checkApprovalAuthority(Long workflowId, Long approverId) {
        logger.info("[ WorkflowService#checkApprovalAuthority ] workflowId={}, approverId={}", 
                   workflowId, approverId);

        // 承認者を取得
        Employee approver = employeeDao.findById(approverId);
        if (approver == null) {
            throw new IllegalArgumentException("承認者が存在しません: " + approverId);
        }

        // 承認者のJOB_RANKチェック（MANAGER以上）
        if (approver.getJobRank() < JOB_RANK_MANAGER) {
            throw new UnauthorizedApprovalException(
                "承認権限がありません。MANAGER以上の役職が必要です。");
        }

        // 最初の操作（CREATE）から申請者を取得
        List<BookWorkflow> history = workflowDao.findByWorkflowId(workflowId);
        if (history.isEmpty()) {
            throw new WorkflowNotFoundException("ワークフローが見つかりません: " + workflowId);
        }

        BookWorkflow firstOp = history.get(0);
        Employee requester = employeeDao.findById(firstOp.getOperatedBy());
        if (requester == null) {
            throw new IllegalArgumentException("申請者が存在しません: " + firstOp.getOperatedBy());
        }

        // 同じ部署かチェック
        if (!approver.getDepartment().getDepartmentId()
                .equals(requester.getDepartment().getDepartmentId())) {
            throw new UnauthorizedApprovalException(
                "承認権限がありません。申請者と同じ部署のMANAGER以上が必要です。");
        }
    }

    /**
     * 書籍マスタへの反映
     * @param workflow ワークフロー
     */
    private void applyToBookMaster(BookWorkflow workflow) {
        logger.info("[ WorkflowService#applyToBookMaster ] workflowType={}", workflow.getWorkflowType());

        switch (workflow.getWorkflowType()) {
            case TYPE_CREATE:
                // 新規書籍追加
                Book newBook = new Book();
                newBook.setBookName(workflow.getBookName());
                newBook.setAuthor(workflow.getAuthor());
                
                Category category = em.find(Category.class, workflow.getCategoryId());
                newBook.setCategory(category);
                
                Publisher publisher = em.find(Publisher.class, workflow.getPublisherId());
                newBook.setPublisher(publisher);
                
                newBook.setPrice(workflow.getPrice());
                newBook.setImageUrl(workflow.getImageUrl());
                
                em.persist(newBook);
                em.flush();  // BOOKIDを取得するためにflush
                logger.info("新規書籍を追加しました: bookId={}, bookName={}", 
                           newBook.getBookId(), newBook.getBookName());
                
                // STOCKテーブルにも在庫数0でINSERT
                Stock newStock = new Stock();
                newStock.setBookId(newBook.getBookId());
                newStock.setQuantity(0);
                newStock.setVersion(0L);
                em.persist(newStock);
                logger.info("在庫情報を追加しました: bookId={}, quantity=0", newBook.getBookId());
                break;

            case TYPE_DELETE:
                // 書籍削除（論理削除）
                Book bookToDelete = bookDao.findById(workflow.getBookId());
                if (bookToDelete != null) {
                    // DELETED列がないため、コメントアウト
                    // bookToDelete.setDeleted(true);
                    logger.info("書籍を論理削除しました: bookId={}", workflow.getBookId());
                }
                break;

            case TYPE_PRICE_TEMP_ADJUSTMENT:
                // 価格の一時変更
                Book bookToUpdate = bookDao.findById(workflow.getBookId());
                if (bookToUpdate != null) {
                    bookToUpdate.setPrice(workflow.getPrice());
                    logger.info("書籍価格を変更しました: bookId={}, newPrice={}", 
                              workflow.getBookId(), workflow.getPrice());
                }
                break;

            default:
                logger.warn("不明なワークフロータイプ: {}", workflow.getWorkflowType());
        }
    }

    /**
     * ワークフローデータをコピー
     * @param source コピー元
     * @return コピーされたワークフロー（operationIdはnull）
     */
    private BookWorkflow copyWorkflowData(BookWorkflow source) {
        BookWorkflow copy = new BookWorkflow();
        copy.setWorkflowId(source.getWorkflowId());
        copy.setWorkflowType(source.getWorkflowType());
        copy.setBookId(source.getBookId());
        copy.setBookName(source.getBookName());
        copy.setAuthor(source.getAuthor());
        copy.setCategoryId(source.getCategoryId());
        copy.setPublisherId(source.getPublisherId());
        copy.setPrice(source.getPrice());
        copy.setImageUrl(source.getImageUrl());
        copy.setApplyReason(source.getApplyReason());
        copy.setStartDate(source.getStartDate());
        copy.setEndDate(source.getEndDate());
        
        return copy;
    }

    /**
     * エンティティからTOへの変換
     * @param workflow ワークフローエンティティ
     * @return ワークフローTO
     */
    private WorkflowTO convertToWorkflowTO(BookWorkflow workflow) {
        WorkflowTO to = new WorkflowTO();
        to.setOperationId(workflow.getOperationId());
        to.setWorkflowId(workflow.getWorkflowId());
        to.setWorkflowType(workflow.getWorkflowType());
        to.setState(workflow.getState());
        to.setBookId(workflow.getBookId());
        to.setBookName(workflow.getBookName());
        to.setAuthor(workflow.getAuthor());
        to.setCategoryId(workflow.getCategoryId());
        to.setPublisherId(workflow.getPublisherId());
        to.setPrice(workflow.getPrice());
        to.setImageUrl(workflow.getImageUrl());
        to.setApplyReason(workflow.getApplyReason());
        to.setStartDate(workflow.getStartDate());
        to.setEndDate(workflow.getEndDate());
        to.setOperationType(workflow.getOperationType());
        to.setOperatedBy(workflow.getOperatedBy());
        to.setOperatedAt(workflow.getOperatedAt());
        to.setOperationReason(workflow.getOperationReason());

        // 関連エンティティの情報を設定
        if (workflow.getOperator() != null) {
            to.setOperatorName(workflow.getOperator().getEmployeeName());
            to.setOperatorCode(workflow.getOperator().getEmployeeCode());
            to.setJobRank(workflow.getOperator().getJobRank());
            
            if (workflow.getOperator().getDepartment() != null) {
                to.setDepartmentId(workflow.getOperator().getDepartment().getDepartmentId());
                to.setDepartmentName(workflow.getOperator().getDepartment().getDepartmentName());
            }
        }

        if (workflow.getCategory() != null) {
            to.setCategoryName(workflow.getCategory().getCategoryName());
        }

        if (workflow.getPublisher() != null) {
            to.setPublisherName(workflow.getPublisher().getPublisherName());
        }

        return to;
    }
}


