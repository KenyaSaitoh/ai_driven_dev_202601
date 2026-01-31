package pro.kensait.backoffice.service.workflow;

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
import pro.kensait.backoffice.api.dto.WorkflowUpdateRequest;
import pro.kensait.backoffice.dao.BookDao;
import pro.kensait.backoffice.dao.EmployeeDao;
import pro.kensait.backoffice.dao.WorkflowDao;
import pro.kensait.backoffice.entity.Book;
import pro.kensait.backoffice.entity.Workflow;
import pro.kensait.backoffice.entity.Category;
import pro.kensait.backoffice.entity.Employee;
import pro.kensait.backoffice.entity.Publisher;

/**
 * ワークフローサービス
 */
@ApplicationScoped
@Transactional
public class WorkflowService {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowService.class);

    @Inject
    private WorkflowDao workflowDao;

    @Inject
    private EmployeeDao employeeDao;

    @Inject
    private BookDao bookDao;

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

        // ワークフロータイプの変換
        WorkflowType workflowType = WorkflowType.valueOf(request.getWorkflowType());

        // 次のワークフローIDを取得
        Long nextWorkflowId = workflowDao.getNextWorkflowId();

        // ワークフローエンティティを作成
        Workflow workflow = new Workflow();
        workflow.setWorkflowId(nextWorkflowId);
        workflow.setWorkflowType(workflowType.name());
        workflow.setState(WorkflowStateType.CREATED.name());
        workflow.setOperationType(WorkflowOperationType.CREATE.name());
        workflow.setOperatedAt(LocalDateTime.now());
        workflow.setOperatedBy(request.getCreatedBy());

        // ワークフロータイプごとの設定
        switch (workflowType) {
            case ADD_NEW_BOOK:
                workflow.setBookName(request.getBookName());
                workflow.setAuthor(request.getAuthor());
                workflow.setPrice(request.getPrice());
                workflow.setImageUrl(request.getImageUrl());
                workflow.setCategoryId(request.getCategoryId());
                workflow.setPublisherId(request.getPublisherId());
                break;
            case REMOVE_BOOK:
                workflow.setBookId(request.getBookId());
                break;
            case ADJUST_BOOK_PRICE:
                workflow.setBookId(request.getBookId());
                workflow.setPrice(request.getPrice());
                workflow.setStartDate(request.getStartDate());
                workflow.setEndDate(request.getEndDate());
                break;
            default:
                throw new IllegalArgumentException("不明なワークフロータイプ: " + workflowType);
        }

        workflow.setApplyReason(request.getApplyReason());

        // 保存
        Workflow saved = workflowDao.insert(workflow);

        return convertToWorkflowTO(saved);
    }

    /**
     * ワークフロー更新（一時保存）
     * @param workflowId ワークフローID
     * @param request 更新リクエスト
     * @return ワークフローTO
     */
    public WorkflowTO updateWorkflow(Long workflowId, WorkflowUpdateRequest request) {
        logger.info("[ WorkflowService#updateWorkflow ] workflowId={}", workflowId);

        // 最新の状態を取得
        Workflow latest = workflowDao.findLatestByWorkflowId(workflowId);
        if (latest == null) {
            throw new WorkflowNotFoundException("ワークフローが見つかりません: " + workflowId);
        }

        // 状態チェック: CREATEDでなければエラー
        if (!WorkflowStateType.CREATED.name().equals(latest.getState())) {
            throw new InvalidWorkflowStateException(
                "更新できる状態ではありません。現在の状態: " + latest.getState());
        }

        // 更新者チェック
        Employee updater = employeeDao.findById(request.getUpdatedBy());
        if (updater == null) {
            throw new IllegalArgumentException("更新者が存在しません: " + request.getUpdatedBy());
        }

        // ワークフロータイプの取得
        WorkflowType workflowType = WorkflowType.valueOf(latest.getWorkflowType());

        // 既存のCREATEレコードを直接更新（新規INSERT不要）
        // 操作日時のみ更新（操作者は作成者のまま変更しない）
        latest.setOperatedAt(LocalDateTime.now());

        // ワークフロータイプごとの更新内容を反映
        switch (workflowType) {
            case ADD_NEW_BOOK:
                if (request.getBookName() != null) {
                    latest.setBookName(request.getBookName());
                }
                if (request.getAuthor() != null) {
                    latest.setAuthor(request.getAuthor());
                }
                if (request.getPrice() != null) {
                    latest.setPrice(request.getPrice());
                }
                if (request.getImageUrl() != null) {
                    latest.setImageUrl(request.getImageUrl());
                }
                if (request.getCategoryId() != null) {
                    latest.setCategoryId(request.getCategoryId());
                }
                if (request.getPublisherId() != null) {
                    latest.setPublisherId(request.getPublisherId());
                }
                break;
            case REMOVE_BOOK:
                if (request.getBookId() != null) {
                    latest.setBookId(request.getBookId());
                }
                break;
            case ADJUST_BOOK_PRICE:
                if (request.getBookId() != null) {
                    latest.setBookId(request.getBookId());
                }
                if (request.getPrice() != null) {
                    latest.setPrice(request.getPrice());
                }
                if (request.getStartDate() != null) {
                    latest.setStartDate(request.getStartDate());
                }
                if (request.getEndDate() != null) {
                    latest.setEndDate(request.getEndDate());
                }
                break;
            default:
                throw new IllegalArgumentException("不明なワークフロータイプ: " + workflowType);
        }

        // 申請理由の更新
        if (request.getApplyReason() != null) {
            latest.setApplyReason(request.getApplyReason());
        }

        // EntityManagerが管理しているため、変更は自動的にUPDATEされる
        // 明示的なflushで即座に反映
        em.flush();

        return convertToWorkflowTO(latest);
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
        Workflow latest = workflowDao.findLatestByWorkflowId(workflowId);
        if (latest == null) {
            throw new WorkflowNotFoundException("ワークフローが見つかりません: " + workflowId);
        }

        // 状態チェック: CREATEDでなければエラー
        if (!WorkflowStateType.CREATED.name().equals(latest.getState())) {
            throw new InvalidWorkflowStateException(
                "申請できる状態ではありません。現在の状態: " + latest.getState());
        }

        // 操作者チェック
        Employee operator = employeeDao.findById(request.getOperatedBy());
        if (operator == null) {
            throw new IllegalArgumentException("操作者が存在しません: " + request.getOperatedBy());
        }

        // 新しい操作履歴を作成
        Workflow newOp = copyWorkflowData(latest);
        newOp.setState(WorkflowStateType.APPLIED.name());
        newOp.setOperationType(WorkflowOperationType.APPLY.name());
        newOp.setOperatedAt(LocalDateTime.now());
        newOp.setOperationReason(request.getOperationReason());
        newOp.setOperatedBy(request.getOperatedBy());

        // 保存
        Workflow saved = workflowDao.insert(newOp);

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
        Workflow latest = workflowDao.findLatestByWorkflowId(workflowId);
        if (latest == null) {
            throw new WorkflowNotFoundException("ワークフローが見つかりません: " + workflowId);
        }

        // 状態チェック: APPLIEDでなければエラー
        if (!WorkflowStateType.APPLIED.name().equals(latest.getState())) {
            throw new InvalidWorkflowStateException(
                "承認できる状態ではありません。現在の状態: " + latest.getState());
        }

        // 承認権限チェック
        checkApprovalAuthority(workflowId, request.getOperatedBy());

        // 操作者取得と存在チェック
        Employee operator = employeeDao.findById(request.getOperatedBy());
        if (operator == null) {
            throw new IllegalArgumentException("操作者が存在しません: " + request.getOperatedBy());
        }

        // 新しい操作履歴を作成
        Workflow newOp = copyWorkflowData(latest);
        newOp.setState(WorkflowStateType.APPROVED.name());
        newOp.setOperationType(WorkflowOperationType.APPROVE.name());
        newOp.setOperatedAt(LocalDateTime.now());
        newOp.setOperationReason(request.getOperationReason());
        newOp.setOperatedBy(request.getOperatedBy());

        // 保存
        Workflow saved = workflowDao.insert(newOp);

        // 書籍マスタへの反映
        applyToBookMaster(saved);

        return convertToWorkflowTO(saved);
    }

    /**
     * 却下（差戻）
     * @param workflowId ワークフローID
     * @param request 操作リクエスト
     * @return ワークフローTO
     */
    public WorkflowTO rejectWorkflow(Long workflowId, WorkflowOperationRequest request) {
        logger.info("[ WorkflowService#rejectWorkflow ] workflowId={}", workflowId);

        // 最新の状態を取得
        Workflow latest = workflowDao.findLatestByWorkflowId(workflowId);
        if (latest == null) {
            throw new WorkflowNotFoundException("ワークフローが見つかりません: " + workflowId);
        }

        // 状態チェック: APPLIEDでなければエラー
        if (!WorkflowStateType.APPLIED.name().equals(latest.getState())) {
            throw new InvalidWorkflowStateException(
                "却下できる状態ではありません。現在の状態: " + latest.getState());
        }

        // 承認権限チェック
        checkApprovalAuthority(workflowId, request.getOperatedBy());

        // 操作者取得と存在チェック
        Employee operator = employeeDao.findById(request.getOperatedBy());
        if (operator == null) {
            throw new IllegalArgumentException("操作者が存在しません: " + request.getOperatedBy());
        }

        // 新しい操作履歴を作成（CREATEDに戻る）
        Workflow newOp = copyWorkflowData(latest);
        newOp.setState(WorkflowStateType.CREATED.name());
        newOp.setOperationType(WorkflowOperationType.REJECT.name());
        newOp.setOperatedAt(LocalDateTime.now());
        newOp.setOperationReason(request.getOperationReason());
        newOp.setOperatedBy(request.getOperatedBy());

        // 保存
        Workflow saved = workflowDao.insert(newOp);

        return convertToWorkflowTO(saved);
    }

    /**
     * ワークフロー履歴取得
     * @param workflowId ワークフローID
     * @return ワークフローTOのリスト
     */
    public List<WorkflowTO> getWorkflowHistory(Long workflowId) {
        logger.info("[ WorkflowService#getWorkflowHistory ] workflowId={}", workflowId);

        List<Workflow> history = workflowDao.findByWorkflowId(workflowId);
        return history.stream()
                .map(this::convertToWorkflowTO)
                .collect(Collectors.toList());
    }

    /**
     * ワークフロー一覧取得
     * @param state 状態（オプション）
     * @param workflowType ワークフロータイプ（オプション）
     * @param employeeId ログイン中の社員ID（CREATED状態のフィルタリング用）
     * @return ワークフローTOのリスト
     */
    public List<WorkflowTO> getWorkflows(String state, String workflowType, Long employeeId) {
        logger.info("[ WorkflowService#getWorkflows ] state={}, workflowType={}, employeeId={}", 
                    state, workflowType, employeeId);

        List<Workflow> workflows;

        if (state != null && workflowType != null) {
            workflows = workflowDao.findByStateAndType(state, workflowType);
        } else if (state != null) {
            workflows = workflowDao.findByState(state);
        } else if (workflowType != null) {
            workflows = workflowDao.findByWorkflowType(workflowType);
        } else {
            workflows = workflowDao.findAllLatest();
        }

        // CREATED状態のワークフローは作成者本人のみ閲覧可能
        // APPLIED状態のワークフローは承認権限がある人のみ閲覧可能
        if (employeeId != null) {
            Employee employee = employeeDao.findById(employeeId);
            workflows = workflows.stream()
                    .filter(w -> {
                        // CREATED状態: 作成者本人のみ
                        if (WorkflowStateType.CREATED.name().equals(w.getState())) {
                            return w.getOperatedBy().equals(employeeId);
                        }
                        // APPLIED状態: 作成者本人または承認権限がある人
                        else if (WorkflowStateType.APPLIED.name().equals(w.getState())) {
                            // 作成者（CREATE操作を行った人）を取得
                            List<Workflow> history = workflowDao.findByWorkflowId(w.getWorkflowId());
                            if (!history.isEmpty()) {
                                Workflow firstOp = history.get(0);
                                Long creatorId = firstOp.getOperatedBy();
                                
                                // 自分が作成者の場合は表示
                                if (creatorId.equals(employeeId)) {
                                    return true;
                                }
                                
                                // 承認権限があるかチェック
                                return hasApprovalAuthority(employee, creatorId);
                            }
                            return false;
                        }
                        // APPROVED状態: すべて表示
                        return true;
                    })
                    .collect(Collectors.toList());
        }

        return workflows.stream()
                .map(this::convertToWorkflowTO)
                .collect(Collectors.toList());
    }

    /**
     * 承認権限があるかチェック（例外を投げない版）
     * @param approver 承認者
     * @param creatorId 作成者ID
     * @return 承認権限がある場合true
     */
    private boolean hasApprovalAuthority(Employee approver, Long creatorId) {
        if (approver == null) {
            return false;
        }

        // 承認者の職務ランクチェック（MANAGER以上）
        JobRankType approverJobRank;
        try {
            approverJobRank = JobRankType.fromRank(approver.getJobRank());
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (!approverJobRank.isAtLeast(JobRankType.MANAGER)) {
            return false;
        }

        // DIRECTORは全部署の明細を参照可能
        if (approverJobRank == JobRankType.DIRECTOR) {
            return true;
        }

        // 作成者を取得
        Employee creator = employeeDao.findById(creatorId);
        if (creator == null) {
            return false;
        }

        // 同じ部署かチェック（MANAGER向け）
        if (approver.getDepartment() == null || creator.getDepartment() == null) {
            return false;
        }

        return approver.getDepartment().getDepartmentId()
                .equals(creator.getDepartment().getDepartmentId());
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

        // 承認者の職務ランクをenumに変換
        JobRankType approverJobRank;
        try {
            approverJobRank = JobRankType.fromRank(approver.getJobRank());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("承認者の職務ランクが不正です: " + approver.getJobRank());
        }

        // 承認者の職務ランクチェック（MANAGER以上）
        if (!approverJobRank.isAtLeast(JobRankType.MANAGER)) {
            throw new UnauthorizedApprovalException(
                "承認権限がありません。MANAGER以上の職務ランクが必要です。");
        }

        // DIRECTORは全部署の明細を参照・承認可能
        if (approverJobRank == JobRankType.DIRECTOR) {
            return;
        }

        // 最初の操作（CREATE）から申請者を取得
        List<Workflow> history = workflowDao.findByWorkflowId(workflowId);
        if (history.isEmpty()) {
            throw new WorkflowNotFoundException("ワークフローが見つかりません: " + workflowId);
        }

        Workflow firstOp = history.get(0);
        Employee requester = employeeDao.findById(firstOp.getOperatedBy());
        if (requester == null) {
            throw new IllegalArgumentException("申請者が存在しません: " + firstOp.getOperatedBy());
        }

        // 同じ部署かチェック（MANAGER向け）
        if (!approver.getDepartment().getDepartmentId()
                .equals(requester.getDepartment().getDepartmentId())) {
            throw new UnauthorizedApprovalException(
                "承認権限がありません。申請者と同じ部署のMANAGER以上の職務ランクが必要です。");
        }
    }

    /**
     * 書籍マスタへの反映
     * @param workflow ワークフロー
     */
    private void applyToBookMaster(Workflow workflow) {
        logger.info("[ WorkflowService#applyToBookMaster ] workflowType={}", workflow.getWorkflowType());

        WorkflowType workflowType = WorkflowType.valueOf(workflow.getWorkflowType());

        switch (workflowType) {
            case ADD_NEW_BOOK:
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
                newBook.setDeleted(false);
                
                // 在庫情報も設定（SecondaryTableなので同時にINSERTされる）
                newBook.setQuantity(0);
                newBook.setVersion(0L);
                
                em.persist(newBook);
                em.flush();  // BOOKIDを取得するためにflush
                logger.info("新規書籍と在庫情報を追加しました: bookId={}, bookName={}, quantity=0", 
                           newBook.getBookId(), newBook.getBookName());
                break;

            case REMOVE_BOOK:
                // 書籍削除（論理削除）
                Book bookToDelete = bookDao.findById(workflow.getBookId());
                if (bookToDelete != null) {
                    bookToDelete.setDeleted(true);
                    logger.info("書籍を論理削除しました: bookId={}", workflow.getBookId());
                }
                break;

            case ADJUST_BOOK_PRICE:
                // 価格の一時変更
                Book bookToUpdate = bookDao.findById(workflow.getBookId());
                if (bookToUpdate != null) {
                    bookToUpdate.setPrice(workflow.getPrice());
                    logger.info("書籍価格を変更しました: bookId={}, newPrice={}", 
                              workflow.getBookId(), workflow.getPrice());
                }
                break;

            default:
                logger.warn("不明なワークフロータイプ: {}", workflowType);
        }
    }

    /**
     * ワークフローデータをコピー
     * @param source コピー元
     * @return コピーされたワークフロー（operationIdはnull）
     */
    private Workflow copyWorkflowData(Workflow source) {
        Workflow copy = new Workflow();
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
    private WorkflowTO convertToWorkflowTO(Workflow workflow) {
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

        // bookIdがあるがbookNameがない場合、書籍情報を取得
        if (workflow.getBookId() != null && workflow.getBookName() == null) {
            Book book = bookDao.findById(workflow.getBookId());
            if (book != null) {
                to.setBookName(book.getBookName());
            }
        }

        // 価格改定の場合、元の価格を設定
        if (WorkflowType.ADJUST_BOOK_PRICE.name().equals(workflow.getWorkflowType()) 
                && workflow.getBookId() != null) {
            Book book = bookDao.findById(workflow.getBookId());
            if (book != null) {
                to.setOriginalPrice(book.getPrice());
            }
        }

        // 関連エンティティの情報を設定
        if (workflow.getOperator() != null) {
            to.setOperatorName(workflow.getOperator().getEmployeeName());
            to.setOperatorCode(workflow.getOperator().getEmployeeCode());
            to.setJobRank(workflow.getOperator().getJobRank());
            
            if (workflow.getOperator().getDepartment() != null) {
                to.setDepartmentId(workflow.getOperator().getDepartment().getDepartmentId());
                to.setDepartmentName(workflow.getOperator().getDepartment().getDepartmentName());
            }
        } else if (workflow.getOperatedBy() != null) {
            // operatorがロードされていない場合、operatedByから取得
            Employee operator = employeeDao.findById(workflow.getOperatedBy());
            if (operator != null) {
                to.setOperatorName(operator.getEmployeeName());
                to.setOperatorCode(operator.getEmployeeCode());
                to.setJobRank(operator.getJobRank());
                
                if (operator.getDepartment() != null) {
                    to.setDepartmentId(operator.getDepartment().getDepartmentId());
                    to.setDepartmentName(operator.getDepartment().getDepartmentName());
                }
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


