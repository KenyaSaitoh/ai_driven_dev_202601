package pro.kensait.berrybooks.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.entity.OrderTran;

import java.util.List;
import java.util.Optional;

/**
 * 注文トランザクションDAO
 * 
 * 注文トランザクションのCRUD操作とクエリ実行を担当する。
 * 
 * @since 1.0.0
 */
@ApplicationScoped
public class OrderTranDao {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderTranDao.class);
    
    @PersistenceContext(unitName = "BerryBooksPU")
    private EntityManager em;
    
    /**
     * 注文トランザクションを登録する
     * 
     * @param orderTran 注文トランザクション
     * @return 登録された注文トランザクション
     */
    public OrderTran insert(OrderTran orderTran) {
        logger.info("[ OrderTranDao#insert ] customerId={}, totalPrice={}", 
                orderTran.getCustomerId(), orderTran.getTotalPrice());
        
        em.persist(orderTran);
        em.flush(); // 自動採番されたIDを即座に取得
        
        logger.info("[ OrderTranDao#insert ] orderTranId={}", orderTran.getOrderTranId());
        return orderTran;
    }
    
    /**
     * 注文IDで注文トランザクションを取得する
     * 
     * @param orderTranId 注文トランザクションID
     * @return 注文トランザクション（存在しない場合はOptional.empty()）
     */
    public Optional<OrderTran> findById(Integer orderTranId) {
        logger.info("[ OrderTranDao#findById ] orderTranId={}", orderTranId);
        
        String jpql = "SELECT o FROM OrderTran o LEFT JOIN FETCH o.orderDetails WHERE o.orderTranId = :orderTranId";
        TypedQuery<OrderTran> query = em.createQuery(jpql, OrderTran.class);
        query.setParameter("orderTranId", orderTranId);
        
        List<OrderTran> results = query.getResultList();
        
        if (results.isEmpty()) {
            logger.warn("[ OrderTranDao#findById ] OrderTran not found: orderTranId={}", orderTranId);
            return Optional.empty();
        }
        
        logger.info("[ OrderTranDao#findById ] Found orderTranId={}", orderTranId);
        return Optional.of(results.get(0));
    }
    
    /**
     * 顧客IDで注文履歴を取得する
     * 
     * @param customerId 顧客ID
     * @return 注文履歴リスト（注文日の降順）
     */
    public List<OrderTran> findByCustomerId(Integer customerId) {
        logger.info("[ OrderTranDao#findByCustomerId ] customerId={}", customerId);
        
        String jpql = "SELECT o FROM OrderTran o LEFT JOIN FETCH o.orderDetails " +
                      "WHERE o.customerId = :customerId ORDER BY o.orderDate DESC";
        TypedQuery<OrderTran> query = em.createQuery(jpql, OrderTran.class);
        query.setParameter("customerId", customerId);
        
        List<OrderTran> results = query.getResultList();
        
        logger.info("[ OrderTranDao#findByCustomerId ] Found {} orders for customerId={}", 
                results.size(), customerId);
        return results;
    }
}
