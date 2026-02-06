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
 */
@ApplicationScoped
public class OrderTranDao {
    private static final Logger logger = LoggerFactory.getLogger(OrderTranDao.class);
    
    @PersistenceContext(unitName = "BerryBooksPU")
    private EntityManager em;
    
    /**
     * 注文トランザクションを登録
     */
    public OrderTran insert(OrderTran orderTran) {
        logger.info("[ OrderTranDao#insert ] Inserting order transaction");
        em.persist(orderTran);
        em.flush();
        logger.info("[ OrderTranDao#insert ] Order transaction inserted: orderTranId={}", orderTran.getOrderTranId());
        return orderTran;
    }
    
    /**
     * 注文IDで注文トランザクションを取得
     */
    public Optional<OrderTran> findById(Integer orderTranId) {
        logger.info("[ OrderTranDao#findById ] Finding order transaction: orderTranId={}", orderTranId);
        
        String jpql = "SELECT o FROM OrderTran o " +
                      "LEFT JOIN FETCH o.orderDetails " +
                      "WHERE o.orderTranId = :orderTranId";
        
        TypedQuery<OrderTran> query = em.createQuery(jpql, OrderTran.class);
        query.setParameter("orderTranId", orderTranId);
        
        List<OrderTran> results = query.getResultList();
        
        if (results.isEmpty()) {
            logger.info("[ OrderTranDao#findById ] Order transaction not found: orderTranId={}", orderTranId);
            return Optional.empty();
        }
        
        logger.info("[ OrderTranDao#findById ] Order transaction found: orderTranId={}", orderTranId);
        return Optional.of(results.get(0));
    }
    
    /**
     * 顧客IDで注文履歴を取得
     */
    public List<OrderTran> findByCustomerId(Integer customerId) {
        logger.info("[ OrderTranDao#findByCustomerId ] Finding order history: customerId={}", customerId);
        
        String jpql = "SELECT o FROM OrderTran o " +
                      "LEFT JOIN FETCH o.orderDetails " +
                      "WHERE o.customerId = :customerId " +
                      "ORDER BY o.orderDate DESC";
        
        TypedQuery<OrderTran> query = em.createQuery(jpql, OrderTran.class);
        query.setParameter("customerId", customerId);
        
        List<OrderTran> results = query.getResultList();
        logger.info("[ OrderTranDao#findByCustomerId ] Found {} orders for customerId={}", results.size(), customerId);
        
        return results;
    }
}
