package pro.kensait.berrybooks.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import pro.kensait.berrybooks.entity.OrderTran;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * OrderTranDao - 注文トランザクションDAO
 * 
 * 責務:
 * - 注文トランザクションのCRUD操作
 * - JPQL実行
 * - エンティティライフサイクル管理
 * 
 * アノテーション:
 * - @ApplicationScoped: CDI管理Bean（シングルトン）
 */
@ApplicationScoped
public class OrderTranDao {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderTranDao.class);
    
    @Inject
    private EntityManager em;
    
    /**
     * 注文トランザクションを登録
     * 
     * 処理:
     * 1. em.persist(orderTran) - エンティティを永続化
     * 2. em.flush() - 即座にINSERT文を実行（IDを取得）
     * 3. 永続化されたOrderTranを返却
     * 
     * @param orderTran 登録する注文トランザクション
     * @return 永続化されたOrderTran（orderTranIdが自動採番済み）
     */
    public OrderTran insert(OrderTran orderTran) {
        logger.info("[ OrderTranDao#insert ] Inserting OrderTran for customerId={}", orderTran.getCustomerId());
        em.persist(orderTran);
        em.flush();
        logger.info("[ OrderTranDao#insert ] OrderTran inserted successfully, orderTranId={}", orderTran.getOrderTranId());
        return orderTran;
    }
    
    /**
     * 注文トランザクションIDで検索
     * 
     * 処理:
     * 1. em.find(OrderTran.class, orderTranId) - 主キー検索
     * 2. 検索結果を返却（存在しない場合はnull）
     * 
     * @param orderTranId 注文トランザクションID
     * @return OrderTran（存在しない場合はnull）
     */
    public OrderTran findById(Integer orderTranId) {
        logger.debug("[ OrderTranDao#findById ] Finding OrderTran by orderTranId={}", orderTranId);
        OrderTran orderTran = em.find(OrderTran.class, orderTranId);
        if (orderTran == null) {
            logger.warn("[ OrderTranDao#findById ] OrderTran not found, orderTranId={}", orderTranId);
        }
        return orderTran;
    }
    
    /**
     * 顧客IDで注文履歴を検索
     * 
     * JPQL:
     * SELECT o FROM OrderTran o WHERE o.customerId = :customerId ORDER BY o.orderDate DESC
     * 
     * 処理:
     * 1. TypedQueryを作成
     * 2. パラメータcustomerIdを設定
     * 3. getResultList()で全件取得
     * 4. 注文日降順でソート
     * 
     * @param customerId 顧客ID
     * @return List<OrderTran>（0件の場合は空リスト）
     */
    public List<OrderTran> findByCustomerId(Integer customerId) {
        logger.debug("[ OrderTranDao#findByCustomerId ] Finding OrderTrans by customerId={}", customerId);
        String jpql = "SELECT o FROM OrderTran o WHERE o.customerId = :customerId ORDER BY o.orderDate DESC";
        TypedQuery<OrderTran> query = em.createQuery(jpql, OrderTran.class);
        query.setParameter("customerId", customerId);
        List<OrderTran> result = query.getResultList();
        logger.info("[ OrderTranDao#findByCustomerId ] Found {} OrderTrans for customerId={}", result.size(), customerId);
        return result;
    }
}
