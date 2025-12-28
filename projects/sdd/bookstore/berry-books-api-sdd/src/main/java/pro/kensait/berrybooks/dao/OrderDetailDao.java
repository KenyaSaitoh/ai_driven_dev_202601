package pro.kensait.berrybooks.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderDetailPK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 注文明細データアクセスクラス
 */
@ApplicationScoped
public class OrderDetailDao {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderDetailDao.class);
    
    @PersistenceContext(unitName = "BerryBooksPU")
    private EntityManager em;
    
    /**
     * 注文明細を登録
     * 
     * @param orderDetail 注文明細
     */
    public void insert(OrderDetail orderDetail) {
        logger.info("[ OrderDetailDao#insert ] Inserting order detail: orderTranId={}, orderDetailId={}", 
                   orderDetail.getOrderTranId(), orderDetail.getOrderDetailId());
        em.persist(orderDetail);
    }
    
    /**
     * 注文明細を検索（複合主キー）
     * 
     * @param orderTranId 注文トランザクションID
     * @param orderDetailId 注文明細ID
     * @return OrderDetail（見つからない場合はnull）
     */
    public OrderDetail findById(Integer orderTranId, Integer orderDetailId) {
        logger.info("[ OrderDetailDao#findById ] orderTranId={}, orderDetailId={}", 
                   orderTranId, orderDetailId);
        
        OrderDetailPK pk = new OrderDetailPK(orderTranId, orderDetailId);
        OrderDetail orderDetail = em.find(OrderDetail.class, pk);
        
        if (orderDetail == null) {
            logger.info("[ OrderDetailDao#findById ] Order detail not found");
        }
        
        return orderDetail;
    }
    
    /**
     * 注文トランザクションIDで注文明細リストを取得
     * 
     * @param orderTranId 注文トランザクションID
     * @return 注文明細リスト
     */
    public List<OrderDetail> findByOrderTranId(Integer orderTranId) {
        logger.info("[ OrderDetailDao#findByOrderTranId ] orderTranId={}", orderTranId);
        
        TypedQuery<OrderDetail> query = em.createQuery(
            "SELECT od FROM OrderDetail od " +
            "LEFT JOIN FETCH od.book b " +
            "LEFT JOIN FETCH b.publisher " +
            "WHERE od.orderTranId = :orderTranId " +
            "ORDER BY od.orderDetailId",
            OrderDetail.class
        );
        query.setParameter("orderTranId", orderTranId);
        
        List<OrderDetail> details = query.getResultList();
        logger.info("[ OrderDetailDao#findByOrderTranId ] Found {} order details", details.size());
        
        return details;
    }
}

