package pro.kensait.berrybooks.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pro.kensait.berrybooks.entity.OrderTran;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * OrderTranエンティティのDAO
 */
@ApplicationScoped
public class OrderTranDao {
    private static final Logger logger = LoggerFactory.getLogger(OrderTranDao.class);
    
    @PersistenceContext(unitName = "BerryBooksPU")
    private EntityManager em;
    
    /**
     * 注文トランザクションを登録する
     * 
     * @param orderTran 登録する注文トランザクション
     * @return 登録された注文トランザクション
     */
    public OrderTran insert(OrderTran orderTran) {
        logger.info("[ OrderTranDao#insert ] orderDate={}, customerId={}, totalPrice={}", 
            orderTran.getOrderDate(), orderTran.getCustomerId(), orderTran.getTotalPrice());
        em.persist(orderTran);
        return orderTran;
    }
    
    /**
     * 注文IDで注文トランザクションを検索する
     * 
     * @param orderTranId 注文ID
     * @return 注文トランザクション（存在しない場合はnull）
     */
    public OrderTran findById(Integer orderTranId) {
        logger.debug("[ OrderTranDao#findById ] orderTranId={}", orderTranId);
        return em.find(OrderTran.class, orderTranId);
    }
    
    /**
     * 顧客IDで注文履歴を検索する（新しい順）
     * 
     * @param customerId 顧客ID
     * @return 注文トランザクションのリスト
     */
    public List<OrderTran> findByCustomerId(Integer customerId) {
        logger.info("[ OrderTranDao#findByCustomerId ] customerId={}", customerId);
        
        String jpql = "SELECT o FROM OrderTran o WHERE o.customerId = :customerId ORDER BY o.orderDate DESC";
        TypedQuery<OrderTran> query = em.createQuery(jpql, OrderTran.class);
        query.setParameter("customerId", customerId);
        
        List<OrderTran> results = query.getResultList();
        logger.debug("[ OrderTranDao#findByCustomerId ] Found {} orders", results.size());
        
        return results;
    }
}