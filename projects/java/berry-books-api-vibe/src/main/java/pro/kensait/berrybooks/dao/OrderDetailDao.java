package pro.kensait.berrybooks.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderDetailPK;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

// 注文明細テーブルへのアクセスを行うDAOクラス
@ApplicationScoped
public class OrderDetailDao {
    private static final Logger logger = LoggerFactory.getLogger(
            OrderDetailDao.class);

    @PersistenceContext(unitName = "bookstorePU")
    private EntityManager em;

    // DAOメソッド：注文明細を主キーで検索
    public OrderDetail findById(OrderDetailPK id) {
        logger.info("[ OrderDetailDao#findById ]");
        return em.find(OrderDetail.class, id);
    }

    // DAOメソッド：注文IDで注文明細リストを検索
    public List<OrderDetail> findByOrderTranId(Integer orderTranId) {
        logger.info("[ OrderDetailDao#findByOrderTranId ]");
        
        TypedQuery<OrderDetail> query = em.createQuery(
                "SELECT od FROM OrderDetail od WHERE od.orderTranId = :orderTranId",
                OrderDetail.class);
        query.setParameter("orderTranId", orderTranId);
        
        return query.getResultList();
    }

    // DAOメソッド：注文明細を保存
    public void persist(OrderDetail orderDetail) {
        logger.info("[ OrderDetailDao#persist ]");
        em.persist(orderDetail);
        // 即座にINSERTを実行してデータベースに反映
        em.flush();
    }
}


