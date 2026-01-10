package pro.kensait.berrybooks.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pro.kensait.berrybooks.entity.OrderTran;

/**
 * ORDER_TRANテーブルのDAOクラス
 * 
 * 注文トランザクションデータのCRUD操作を提供する。
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
     * @return 登録された注文トランザクション（ORDER_TRAN_IDが設定される）
     */
    public OrderTran insert(OrderTran orderTran) {
        logger.info("[ OrderTranDao#insert ] customerId={}, totalPrice={}", 
                    orderTran.getCustomerId(), orderTran.getTotalPrice());
        em.persist(orderTran);
        em.flush(); // IDENTITYで自動採番されたIDを即座に取得
        logger.info("[ OrderTranDao#insert ] orderTranId={}", orderTran.getOrderTranId());
        return orderTran;
    }

    /**
     * 注文トランザクションIDで検索する
     * 
     * @param orderTranId 注文トランザクションID
     * @return 注文トランザクション（存在しない場合はnull）
     */
    public OrderTran findById(Integer orderTranId) {
        logger.info("[ OrderTranDao#findById ] orderTranId={}", orderTranId);
        return em.find(OrderTran.class, orderTranId);
    }

    /**
     * 顧客IDで注文トランザクションを検索する（注文日降順）
     * 
     * @param customerId 顧客ID
     * @return 注文トランザクションのリスト
     */
    public List<OrderTran> findByCustomerId(Integer customerId) {
        logger.info("[ OrderTranDao#findByCustomerId ] customerId={}", customerId);
        return em.createQuery(
            "SELECT ot FROM OrderTran ot WHERE ot.customerId = :customerId ORDER BY ot.orderDate DESC", 
            OrderTran.class)
            .setParameter("customerId", customerId)
            .getResultList();
    }
}
