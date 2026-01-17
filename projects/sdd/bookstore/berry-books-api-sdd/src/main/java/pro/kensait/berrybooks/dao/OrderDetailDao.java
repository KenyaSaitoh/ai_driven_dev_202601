package pro.kensait.berrybooks.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderDetailPK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * OrderDetailDao - 注文明細DAO
 * 
 * 責務:
 * - 注文明細のCRUD操作
 * - JPQL実行
 * - エンティティライフサイクル管理
 * 
 * アノテーション:
 * - @ApplicationScoped: CDI管理Bean（シングルトン）
 */
@ApplicationScoped
public class OrderDetailDao {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderDetailDao.class);
    
    @Inject
    private EntityManager em;
    
    /**
     * 注文明細を登録
     * 
     * 処理:
     * 1. em.persist(orderDetail) - エンティティを永続化
     * 2. 永続化されたOrderDetailを返却
     * 
     * @param orderDetail 登録する注文明細
     * @return 永続化されたOrderDetail
     */
    public OrderDetail insert(OrderDetail orderDetail) {
        logger.debug("[ OrderDetailDao#insert ] Inserting OrderDetail for orderTranId={}, orderDetailId={}", 
                    orderDetail.getId().getOrderTranId(), orderDetail.getId().getOrderDetailId());
        em.persist(orderDetail);
        logger.debug("[ OrderDetailDao#insert ] OrderDetail inserted successfully");
        return orderDetail;
    }
    
    /**
     * 注文トランザクションIDで明細を検索
     * 
     * JPQL:
     * SELECT d FROM OrderDetail d WHERE d.id.orderTranId = :orderTranId ORDER BY d.id.orderDetailId
     * 
     * 処理:
     * 1. TypedQueryを作成
     * 2. パラメータorderTranIdを設定
     * 3. getResultList()で全件取得
     * 4. 明細ID昇順でソート
     * 
     * @param orderTranId 注文トランザクションID
     * @return List<OrderDetail>（0件の場合は空リスト）
     */
    public List<OrderDetail> findByOrderTranId(Integer orderTranId) {
        logger.debug("[ OrderDetailDao#findByOrderTranId ] Finding OrderDetails by orderTranId={}", orderTranId);
        String jpql = "SELECT d FROM OrderDetail d WHERE d.id.orderTranId = :orderTranId ORDER BY d.id.orderDetailId";
        TypedQuery<OrderDetail> query = em.createQuery(jpql, OrderDetail.class);
        query.setParameter("orderTranId", orderTranId);
        List<OrderDetail> result = query.getResultList();
        logger.debug("[ OrderDetailDao#findByOrderTranId ] Found {} OrderDetails for orderTranId={}", result.size(), orderTranId);
        return result;
    }
    
    /**
     * 複合主キーで注文明細を検索
     * 
     * 処理:
     * 1. em.find(OrderDetail.class, id) - 主キー検索
     * 2. 検索結果を返却（存在しない場合はnull）
     * 
     * @param id 複合主キー（OrderDetailPK）
     * @return OrderDetail（存在しない場合はnull）
     */
    public OrderDetail findById(OrderDetailPK id) {
        logger.debug("[ OrderDetailDao#findById ] Finding OrderDetail by orderTranId={}, orderDetailId={}", 
                    id.getOrderTranId(), id.getOrderDetailId());
        OrderDetail orderDetail = em.find(OrderDetail.class, id);
        if (orderDetail == null) {
            logger.warn("[ OrderDetailDao#findById ] OrderDetail not found, orderTranId={}, orderDetailId={}", 
                       id.getOrderTranId(), id.getOrderDetailId());
        }
        return orderDetail;
    }
}
