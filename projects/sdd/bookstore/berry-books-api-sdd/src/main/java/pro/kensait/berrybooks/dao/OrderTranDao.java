package pro.kensait.berrybooks.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.service.order.OrderHistoryTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 注文トランザクションデータアクセスクラス
 */
@ApplicationScoped
public class OrderTranDao {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderTranDao.class);
    
    @PersistenceContext(unitName = "BerryBooksPU")
    private EntityManager em;
    
    /**
     * 注文トランザクションを登録
     * 
     * @param orderTran 注文トランザクション
     * @return 登録された注文トランザクション
     */
    public OrderTran insert(OrderTran orderTran) {
        logger.info("[ OrderTranDao#insert ] Inserting order transaction");
        em.persist(orderTran);
        em.flush(); // ID採番を確実に実行
        logger.info("[ OrderTranDao#insert ] Order transaction inserted: orderTranId={}", 
                   orderTran.getOrderTranId());
        return orderTran;
    }
    
    /**
     * 注文トランザクションIDで検索
     * 
     * @param orderTranId 注文トランザクションID
     * @return OrderTran（見つからない場合はnull）
     */
    public OrderTran findById(Integer orderTranId) {
        logger.info("[ OrderTranDao#findById ] orderTranId={}", orderTranId);
        
        TypedQuery<OrderTran> query = em.createQuery(
            "SELECT ot FROM OrderTran ot " +
            "LEFT JOIN FETCH ot.orderDetails od " +
            "LEFT JOIN FETCH od.book b " +
            "LEFT JOIN FETCH b.publisher " +
            "WHERE ot.orderTranId = :orderTranId",
            OrderTran.class
        );
        query.setParameter("orderTranId", orderTranId);
        
        List<OrderTran> orders = query.getResultList();
        if (orders.isEmpty()) {
            logger.info("[ OrderTranDao#findById ] Order not found: {}", orderTranId);
            return null;
        }
        
        return orders.get(0);
    }
    
    /**
     * 顧客IDで注文履歴を取得（非正規化DTO）
     * 
     * 1注文明細=1レコードで返す
     * 
     * @param customerId 顧客ID
     * @return 注文履歴リスト
     */
    public List<OrderHistoryTO> getOrderHistory(Integer customerId) {
        logger.info("[ OrderTranDao#getOrderHistory ] customerId={}", customerId);
        
        TypedQuery<OrderHistoryTO> query = em.createQuery(
            "SELECT new pro.kensait.berrybooks.service.order.OrderHistoryTO(" +
            "ot.orderDate, ot.orderTranId, od.orderDetailId, " +
            "b.bookName, p.publisherName, od.price, od.count) " +
            "FROM OrderTran ot " +
            "JOIN ot.orderDetails od " +
            "JOIN od.book b " +
            "JOIN b.publisher p " +
            "WHERE ot.customer.customerId = :customerId " +
            "ORDER BY ot.orderDate DESC, ot.orderTranId, od.orderDetailId",
            OrderHistoryTO.class
        );
        query.setParameter("customerId", customerId);
        
        List<OrderHistoryTO> history = query.getResultList();
        logger.info("[ OrderTranDao#getOrderHistory ] Found {} order history records", history.size());
        
        return history;
    }
}

