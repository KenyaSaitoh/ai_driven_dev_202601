package pro.kensait.berrybooks.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderDetailPK;

/**
 * ORDER_DETAILテーブルのDAOクラス
 * 
 * 注文明細データのCRUD操作を提供する。
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
                    orderDetail.getOrderTranId(), orderDetail.getOrderDetailId(), orderDetail.getBookId());
        em.persist(orderDetail);
        return orderDetail;
    }

    /**
     * 注文トランザクションIDで注文明細を検索する
     * 
     * @param orderTranId 注文トランザクションID
     * @return 注文明細のリスト
     */
    public List<OrderDetail> findByOrderTranId(Integer orderTranId) {
        logger.info("[ OrderDetailDao#findByOrderTranId ] orderTranId={}", orderTranId);
        return em.createQuery(
            "SELECT od FROM OrderDetail od WHERE od.orderTranId = :orderTranId ORDER BY od.orderDetailId", 
            OrderDetail.class)
            .setParameter("orderTranId", orderTranId)
            .getResultList();
    }

    /**
     * 複合主キーで注文明細を検索する
     * 
     * @param orderDetailPK 複合主キー
     * @return 注文明細（存在しない場合はnull）
     */
    public OrderDetail findByOrderDetailPK(OrderDetailPK orderDetailPK) {
        logger.info("[ OrderDetailDao#findByOrderDetailPK ] orderTranId={}, orderDetailId={}", 
                    orderDetailPK.getOrderTranId(), orderDetailPK.getOrderDetailId());
        return em.find(OrderDetail.class, orderDetailPK);
    }
}
