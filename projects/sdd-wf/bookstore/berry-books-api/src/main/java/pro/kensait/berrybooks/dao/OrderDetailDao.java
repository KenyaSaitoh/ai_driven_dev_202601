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
 * 
 * 注文明細のCRUD操作とクエリ実行を担当する。
 * 
 * @since 1.0.0
 */
@ApplicationScoped
public class OrderDetailDao {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderDetailDao.class);
    
    @PersistenceContext(unitName = "BerryBooksPU")
    private EntityManager em;
    
    /**
     * 注文明細を登録する
     * 
     * @param orderDetail 注文明細
     * @return 登録された注文明細
     */
    public OrderDetail insert(OrderDetail orderDetail) {
        logger.info("[ OrderDetailDao#insert ] orderTranId={}, orderDetailId={}, bookId={}", 
                orderDetail.getId().getOrderTranId(), 
                orderDetail.getId().getOrderDetailId(),
                orderDetail.getBookId());
        
        em.persist(orderDetail);
        
        return orderDetail;
    }
    
    /**
     * 注文IDで注文明細一覧を取得する
     * 
     * @param orderTranId 注文トランザクションID
     * @return 注文明細リスト（注文明細IDの昇順）
     */
    public List<OrderDetail> findByOrderTranId(Integer orderTranId) {
        logger.info("[ OrderDetailDao#findByOrderTranId ] orderTranId={}", orderTranId);
        
        String jpql = "SELECT od FROM OrderDetail od " +
                      "WHERE od.id.orderTranId = :orderTranId " +
                      "ORDER BY od.id.orderDetailId";
        TypedQuery<OrderDetail> query = em.createQuery(jpql, OrderDetail.class);
        query.setParameter("orderTranId", orderTranId);
        
        List<OrderDetail> results = query.getResultList();
        
        logger.info("[ OrderDetailDao#findByOrderTranId ] Found {} details for orderTranId={}", 
                results.size(), orderTranId);
        return results;
    }
}
