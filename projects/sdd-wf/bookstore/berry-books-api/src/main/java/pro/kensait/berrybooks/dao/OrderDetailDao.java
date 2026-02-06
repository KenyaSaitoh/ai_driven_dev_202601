package pro.kensait.berrybooks.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.entity.OrderDetail;

import java.util.List;

/**
 * 注文明細DAO
 */
@ApplicationScoped
public class OrderDetailDao {
    private static final Logger logger = LoggerFactory.getLogger(OrderDetailDao.class);
    
    @PersistenceContext(unitName = "BerryBooksPU")
    private EntityManager em;
    
    /**
     * 注文明細を登録
     */
    public OrderDetail insert(OrderDetail orderDetail) {
        logger.info("[ OrderDetailDao#insert ] Inserting order detail: orderTranId={}, orderDetailId={}", 
                    orderDetail.getId().getOrderTranId(), 
                    orderDetail.getId().getOrderDetailId());
        em.persist(orderDetail);
        logger.info("[ OrderDetailDao#insert ] Order detail inserted");
        return orderDetail;
    }
    
    /**
     * 注文IDで注文明細一覧を取得
     */
    public List<OrderDetail> findByOrderTranId(Integer orderTranId) {
        logger.info("[ OrderDetailDao#findByOrderTranId ] Finding order details: orderTranId={}", orderTranId);
        
        String jpql = "SELECT od FROM OrderDetail od " +
                      "WHERE od.id.orderTranId = :orderTranId " +
                      "ORDER BY od.id.orderDetailId";
        
        TypedQuery<OrderDetail> query = em.createQuery(jpql, OrderDetail.class);
        query.setParameter("orderTranId", orderTranId);
        
        List<OrderDetail> results = query.getResultList();
        logger.info("[ OrderDetailDao#findByOrderTranId ] Found {} order details", results.size());
        
        return results;
    }
}
