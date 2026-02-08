package pro.kensait.berrybooks.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pro.kensait.berrybooks.entity.OrderDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * OrderDetailエンティティのDAO
 */
@ApplicationScoped
public class OrderDetailDao {
    private static final Logger logger = LoggerFactory.getLogger(OrderDetailDao.class);
    
    @PersistenceContext(unitName = "BerryBooksPU")
    private EntityManager em;
    
    /**
     * 注文明細を登録する
     * 
     * @param orderDetail 登録する注文明細
     * @return 登録された注文明細
     */
    public OrderDetail insert(OrderDetail orderDetail) {
        logger.info("[ OrderDetailDao#insert ] orderTranId={}, orderDetailId={}, bookId={}, bookName={}", 
            orderDetail.getOrderTranId(), orderDetail.getOrderDetailId(), 
            orderDetail.getBookId(), orderDetail.getBookName());
        em.persist(orderDetail);
        return orderDetail;
    }
    
    /**
     * 注文IDで注文明細一覧を検索する
     * 
     * @param orderTranId 注文ID
     * @return 注文明細のリスト
     */
    public List<OrderDetail> findByOrderTranId(Integer orderTranId) {
        logger.info("[ OrderDetailDao#findByOrderTranId ] orderTranId={}", orderTranId);
        
        String jpql = "SELECT od FROM OrderDetail od WHERE od.orderTranId = :orderTranId ORDER BY od.orderDetailId";
        TypedQuery<OrderDetail> query = em.createQuery(jpql, OrderDetail.class);
        query.setParameter("orderTranId", orderTranId);
        
        List<OrderDetail> results = query.getResultList();
        logger.debug("[ OrderDetailDao#findByOrderTranId ] Found {} order details", results.size());
        
        return results;
    }
}