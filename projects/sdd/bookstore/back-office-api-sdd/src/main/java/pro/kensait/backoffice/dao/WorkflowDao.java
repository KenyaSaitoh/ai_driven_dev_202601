package pro.kensait.backoffice.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pro.kensait.backoffice.entity.Workflow;
import pro.kensait.backoffice.common.WorkflowState;
import pro.kensait.backoffice.common.WorkflowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ワークフローデータアクセスオブジェクト
 * 
 * ワークフロー履歴のCRUD操作を提供する
 */
@ApplicationScoped
public class WorkflowDao {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowDao.class);

    @PersistenceContext
    private EntityManager em;

    /**
     * 操作IDでワークフロー操作履歴を取得
     * 
     * @param operationId 操作ID
     * @return ワークフローエンティティ、存在しない場合はnull
     */
    public Workflow findById(Long operationId) {
        logger.debug("findById called with operationId: {}", operationId);
        return em.find(Workflow.class, operationId);
    }

    /**
     * ワークフローIDの最新操作履歴を取得
     * 
     * @param workflowId ワークフローID
     * @return 最新のワークフローエンティティ、存在しない場合はnull
     */
    public Workflow findLatestByWorkflowId(Long workflowId) {
        logger.debug("findLatestByWorkflowId called with workflowId: {}", workflowId);
        
        String jpql = "SELECT w FROM Workflow w " +
                     "JOIN FETCH w.operator e " +
                     "JOIN FETCH e.department " +
                     "WHERE w.workflowId = :workflowId " +
                     "ORDER BY w.operationId DESC";
        
        TypedQuery<Workflow> query = em.createQuery(jpql, Workflow.class);
        query.setParameter("workflowId", workflowId);
        query.setMaxResults(1);
        
        try {
            Workflow workflow = query.getSingleResult();
            logger.debug("Latest workflow found: operationId={}, state={}", 
                        workflow.getOperationId(), workflow.getState());
            return workflow;
        } catch (NoResultException e) {
            logger.debug("Workflow not found with workflowId: {}", workflowId);
            return null;
        }
    }

    /**
     * ワークフローIDの全操作履歴を取得（時系列順）
     * 
     * @param workflowId ワークフローID
     * @return ワークフロー操作履歴のリスト
     */
    public List<Workflow> findHistoryByWorkflowId(Long workflowId) {
        logger.debug("findHistoryByWorkflowId called with workflowId: {}", workflowId);
        
        String jpql = "SELECT w FROM Workflow w " +
                     "JOIN FETCH w.operator e " +
                     "WHERE w.workflowId = :workflowId " +
                     "ORDER BY w.operatedAt ASC, w.operationId ASC";
        
        TypedQuery<Workflow> query = em.createQuery(jpql, Workflow.class);
        query.setParameter("workflowId", workflowId);
        
        List<Workflow> results = query.getResultList();
        logger.debug("Found {} workflow history records", results.size());
        return results;
    }

    /**
     * 全ワークフローの最新状態を取得
     * 
     * @return ワークフローのリスト（各ワークフローIDの最新操作履歴のみ）
     */
    public List<Workflow> findAllLatest() {
        logger.debug("findAllLatest called");
        
        // 各ワークフローIDの最大操作IDを持つ行のみを取得
        String jpql = "SELECT w FROM Workflow w " +
                     "JOIN FETCH w.operator e " +
                     "WHERE w.operationId IN (" +
                     "  SELECT MAX(w2.operationId) FROM Workflow w2 " +
                     "  GROUP BY w2.workflowId" +
                     ") " +
                     "ORDER BY w.operatedAt DESC";
        
        TypedQuery<Workflow> query = em.createQuery(jpql, Workflow.class);
        
        List<Workflow> results = query.getResultList();
        logger.debug("Found {} workflows", results.size());
        return results;
    }

    /**
     * 状態でフィルタしたワークフロー一覧を取得
     * 
     * @param state 状態
     * @return ワークフローのリスト
     */
    public List<Workflow> findAllLatestByState(WorkflowState state) {
        logger.debug("findAllLatestByState called with state: {}", state);
        
        String jpql = "SELECT w FROM Workflow w " +
                     "JOIN FETCH w.operator e " +
                     "WHERE w.operationId IN (" +
                     "  SELECT MAX(w2.operationId) FROM Workflow w2 " +
                     "  WHERE w2.state = :state " +
                     "  GROUP BY w2.workflowId" +
                     ") " +
                     "ORDER BY w.operatedAt DESC";
        
        TypedQuery<Workflow> query = em.createQuery(jpql, Workflow.class);
        query.setParameter("state", state);
        
        List<Workflow> results = query.getResultList();
        logger.debug("Found {} workflows with state {}", results.size(), state);
        return results;
    }

    /**
     * ワークフロータイプでフィルタしたワークフロー一覧を取得
     * 
     * @param workflowType ワークフロータイプ
     * @return ワークフローのリスト
     */
    public List<Workflow> findAllLatestByType(WorkflowType workflowType) {
        logger.debug("findAllLatestByType called with type: {}", workflowType);
        
        String jpql = "SELECT w FROM Workflow w " +
                     "JOIN FETCH w.operator e " +
                     "WHERE w.operationId IN (" +
                     "  SELECT MAX(w2.operationId) FROM Workflow w2 " +
                     "  WHERE w2.workflowType = :workflowType " +
                     "  GROUP BY w2.workflowId" +
                     ") " +
                     "ORDER BY w.operatedAt DESC";
        
        TypedQuery<Workflow> query = em.createQuery(jpql, Workflow.class);
        query.setParameter("workflowType", workflowType);
        
        List<Workflow> results = query.getResultList();
        logger.debug("Found {} workflows with type {}", results.size(), workflowType);
        return results;
    }

    /**
     * 社員IDでフィルタしたワークフロー一覧を取得（作成者または操作者）
     * 
     * @param employeeId 社員ID
     * @return ワークフローのリスト
     */
    public List<Workflow> findAllLatestByEmployeeId(Long employeeId) {
        logger.debug("findAllLatestByEmployeeId called with employeeId: {}", employeeId);
        
        String jpql = "SELECT w FROM Workflow w " +
                     "JOIN FETCH w.operator e " +
                     "WHERE w.workflowId IN (" +
                     "  SELECT DISTINCT w2.workflowId FROM Workflow w2 " +
                     "  WHERE w2.operator.employeeId = :employeeId" +
                     ") " +
                     "AND w.operationId IN (" +
                     "  SELECT MAX(w3.operationId) FROM Workflow w3 " +
                     "  GROUP BY w3.workflowId" +
                     ") " +
                     "ORDER BY w.operatedAt DESC";
        
        TypedQuery<Workflow> query = em.createQuery(jpql, Workflow.class);
        query.setParameter("employeeId", employeeId);
        
        List<Workflow> results = query.getResultList();
        logger.debug("Found {} workflows for employeeId {}", results.size(), employeeId);
        return results;
    }

    /**
     * 次のワークフローIDを生成
     * 
     * @return 次のワークフローID
     */
    public Long generateWorkflowId() {
        String jpql = "SELECT COALESCE(MAX(w.workflowId), 0) + 1 FROM Workflow w";
        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        Long nextId = query.getSingleResult();
        logger.debug("Generated workflowId: {}", nextId);
        return nextId;
    }

    /**
     * ワークフロー操作履歴を保存（INSERT）
     * 
     * @param workflow ワークフローエンティティ
     */
    public void insert(Workflow workflow) {
        logger.info("Inserting workflow: workflowId={}, type={}, state={}, operation={}", 
                   workflow.getWorkflowId(), workflow.getWorkflowType(), 
                   workflow.getState(), workflow.getOperationType());
        em.persist(workflow);
    }

    /**
     * ワークフロー操作履歴を更新（UPDATE）
     * 
     * @param workflow ワークフローエンティティ
     * @return 更新後のワークフローエンティティ
     */
    public Workflow update(Workflow workflow) {
        logger.info("Updating workflow: operationId={}", workflow.getOperationId());
        return em.merge(workflow);
    }

    /**
     * ワークフロー操作履歴を削除（DELETE）
     * 
     * @param operationId 操作ID
     */
    public void delete(Long operationId) {
        logger.info("Deleting workflow operation with ID: {}", operationId);
        Workflow workflow = findById(operationId);
        if (workflow != null) {
            em.remove(workflow);
        }
    }
}
