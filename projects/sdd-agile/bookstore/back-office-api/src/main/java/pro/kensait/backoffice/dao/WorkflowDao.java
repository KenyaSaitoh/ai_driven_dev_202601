package pro.kensait.backoffice.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pro.kensait.backoffice.entity.Workflow;

/**
 * ワークフローテーブルへのアクセスを行うDAOクラス
 */
@ApplicationScoped
public class WorkflowDao {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowDao.class);

    @PersistenceContext(unitName = "bookstorePU")
    private EntityManager em;

    /**
     * 操作履歴を挿入
     * @param workflow ワークフローエンティティ
     * @return 永続化されたワークフローエンティティ
     */
    public Workflow insert(Workflow workflow) {
        logger.info("[ WorkflowDao#insert ] workflowId={}, operationType={}", 
                   workflow.getWorkflowId(), workflow.getOperationType());
        em.persist(workflow);
        em.flush();
        return workflow;
    }

    /**
     * 操作IDで検索
     * @param operationId 操作ID
     * @return ワークフローエンティティ
     */
    public Workflow findByOperationId(Long operationId) {
        logger.info("[ WorkflowDao#findByOperationId ] operationId={}", operationId);
        return em.find(Workflow.class, operationId);
    }

    /**
     * ワークフローIDで全履歴取得
     * @param workflowId ワークフローID
     * @return ワークフローエンティティのリスト（操作日時昇順）
     */
    public List<Workflow> findByWorkflowId(Long workflowId) {
        logger.info("[ WorkflowDao#findByWorkflowId ] workflowId={}", workflowId);
        
        TypedQuery<Workflow> query = em.createQuery(
                "SELECT w FROM Workflow w WHERE w.workflowId = :workflowId ORDER BY w.operatedAt ASC",
                Workflow.class);
        query.setParameter("workflowId", workflowId);
        return query.getResultList();
    }

    /**
     * 最新の操作取得（ワークフローIDで）
     * @param workflowId ワークフローID
     * @return 最新のワークフローエンティティ
     */
    public Workflow findLatestByWorkflowId(Long workflowId) {
        logger.info("[ WorkflowDao#findLatestByWorkflowId ] workflowId={}", workflowId);
        
        TypedQuery<Workflow> query = em.createQuery(
                "SELECT w FROM Workflow w WHERE w.workflowId = :workflowId " +
                "ORDER BY w.operatedAt DESC",
                Workflow.class);
        query.setParameter("workflowId", workflowId);
        query.setMaxResults(1);
        
        List<Workflow> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 状態で検索（最新の操作のみ）
     * @param state 状態 (NEW, APPLIED, APPROVED)
     * @return ワークフローエンティティのリスト
     */
    public List<Workflow> findByState(String state) {
        logger.info("[ WorkflowDao#findByState ] state={}", state);
        
        // サブクエリで各ワークフローの最新操作IDを取得し、それに一致するレコードを取得
        TypedQuery<Workflow> query = em.createQuery(
                "SELECT w FROM Workflow w WHERE w.operationId IN (" +
                "  SELECT MAX(w2.operationId) FROM Workflow w2 GROUP BY w2.workflowId" +
                ") AND w.state = :state " +
                "ORDER BY w.operatedAt DESC",
                Workflow.class);
        query.setParameter("state", state);
        return query.getResultList();
    }

    /**
     * ワークフロータイプで検索（最新の操作のみ）
     * @param workflowType ワークフロータイプ (ADD_NEW_BOOK, REMOVE_BOOK, ADJUST_BOOK_PRICE)
     * @return ワークフローエンティティのリスト
     */
    public List<Workflow> findByWorkflowType(String workflowType) {
        logger.info("[ WorkflowDao#findByWorkflowType ] workflowType={}", workflowType);
        
        TypedQuery<Workflow> query = em.createQuery(
                "SELECT w FROM Workflow w WHERE w.operationId IN (" +
                "  SELECT MAX(w2.operationId) FROM Workflow w2 GROUP BY w2.workflowId" +
                ") AND w.workflowType = :workflowType " +
                "ORDER BY w.operatedAt DESC",
                Workflow.class);
        query.setParameter("workflowType", workflowType);
        return query.getResultList();
    }

    /**
     * 状態とワークフロータイプで検索（最新の操作のみ）
     * @param state 状態
     * @param workflowType ワークフロータイプ
     * @return ワークフローエンティティのリスト
     */
    public List<Workflow> findByStateAndType(String state, String workflowType) {
        logger.info("[ WorkflowDao#findByStateAndType ] state={}, workflowType={}", state, workflowType);
        
        TypedQuery<Workflow> query = em.createQuery(
                "SELECT w FROM Workflow w WHERE w.operationId IN (" +
                "  SELECT MAX(w2.operationId) FROM Workflow w2 GROUP BY w2.workflowId" +
                ") AND w.state = :state AND w.workflowType = :workflowType " +
                "ORDER BY w.operatedAt DESC",
                Workflow.class);
        query.setParameter("state", state);
        query.setParameter("workflowType", workflowType);
        return query.getResultList();
    }

    /**
     * 全ワークフローの最新状態を取得
     * @return ワークフローエンティティのリスト
     */
    public List<Workflow> findAllLatest() {
        logger.info("[ WorkflowDao#findAllLatest ]");
        
        TypedQuery<Workflow> query = em.createQuery(
                "SELECT w FROM Workflow w WHERE w.operationId IN (" +
                "  SELECT MAX(w2.operationId) FROM Workflow w2 GROUP BY w2.workflowId" +
                ") ORDER BY w.operatedAt DESC",
                Workflow.class);
        return query.getResultList();
    }

    /**
     * 次のワークフローIDを取得
     * @return 次のワークフローID
     */
    public Long getNextWorkflowId() {
        logger.info("[ WorkflowDao#getNextWorkflowId ]");
        
        TypedQuery<Long> query = em.createQuery(
                "SELECT COALESCE(MAX(w.workflowId), 0L) + 1 FROM Workflow w", Long.class);
        return query.getSingleResult();
    }
}

