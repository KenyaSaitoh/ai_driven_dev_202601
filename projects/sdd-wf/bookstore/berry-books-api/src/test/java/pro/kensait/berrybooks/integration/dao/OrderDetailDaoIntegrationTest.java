package pro.kensait.berrybooks.integration.dao;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderDetailPK;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.integration.BaseIntegrationTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderDetailDao 結合テスト
 * 
 * テスト対象: DAO + Entity + DB
 * 
 * シナリオ:
 * * 注文明細の作成
 * * 注文IDで注文明細一覧を取得
 */
class OrderDetailDaoIntegrationTest extends BaseIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderDetailDaoIntegrationTest.class);
    
    /**
     * Scenario: 注文明細を作成
     * 
     * Given: 注文が存在する
     * When: 注文明細を INSERT する
     * Then: DBに注文明細が作成される
     */
    @Test
    void testInsert_Success() {
        logger.info("[ OrderDetailDaoIntegrationTest#testInsert_Success ] START");
        
        // Given: 注文を作成
        OrderTran orderTran = new OrderTran();
        orderTran.setOrderDate(LocalDate.now());
        orderTran.setCustomerId(1);
        orderTran.setTotalPrice(3000);
        orderTran.setDeliveryPrice(500);
        orderTran.setDeliveryAddress("東京都渋谷区1-1-1");
        orderTran.setSettlementType(1);
        persistAndFlush(orderTran);
        
        // When: 注文明細を作成
        OrderDetailPK pk = new OrderDetailPK(orderTran.getOrderTranId(), 1);
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setId(pk);
        orderDetail.setOrderTran(orderTran);
        orderDetail.setBookId(1);
        orderDetail.setBookName("Java完全理解");
        orderDetail.setPublisherName("技術評論社");
        orderDetail.setPrice(2000);
        orderDetail.setCount(2);
        persistAndFlush(orderDetail);
        
        // Then: DBから再取得して検証
        clearEntityCache();
        OrderDetail foundDetail = em.find(OrderDetail.class, pk);
        
        assertNotNull(foundDetail);
        assertEquals(orderTran.getOrderTranId(), foundDetail.getId().getOrderTranId());
        assertEquals(1, foundDetail.getId().getOrderDetailId());
        assertEquals(1, foundDetail.getBookId());
        assertEquals("Java完全理解", foundDetail.getBookName());
        assertEquals("技術評論社", foundDetail.getPublisherName());
        assertEquals(2000, foundDetail.getPrice());
        assertEquals(2, foundDetail.getCount());
        
        logger.info("[ OrderDetailDaoIntegrationTest#testInsert_Success ] PASS");
    }
    
    /**
     * Scenario: 注文IDで注文明細一覧を取得
     * 
     * Given: 注文と注文明細が存在する
     * When: orderTranId=1で検索する
     * Then: 注文明細リストが返される（注文明細IDの昇順）
     */
    @Test
    void testFindByOrderTranId_Success() {
        logger.info("[ OrderDetailDaoIntegrationTest#testFindByOrderTranId_Success ] START");
        
        // Given: 注文を作成
        OrderTran orderTran = new OrderTran();
        orderTran.setOrderDate(LocalDate.now());
        orderTran.setCustomerId(1);
        orderTran.setTotalPrice(5000);
        orderTran.setDeliveryPrice(500);
        orderTran.setDeliveryAddress("東京都渋谷区1-1-1");
        orderTran.setSettlementType(1);
        persistAndFlush(orderTran);
        
        // 注文明細1
        OrderDetailPK pk1 = new OrderDetailPK(orderTran.getOrderTranId(), 1);
        OrderDetail detail1 = new OrderDetail();
        detail1.setId(pk1);
        detail1.setOrderTran(orderTran);
        detail1.setBookId(1);
        detail1.setBookName("Java完全理解");
        detail1.setPublisherName("技術評論社");
        detail1.setPrice(2000);
        detail1.setCount(2);
        persistAndFlush(detail1);
        
        // 注文明細2
        OrderDetailPK pk2 = new OrderDetailPK(orderTran.getOrderTranId(), 2);
        OrderDetail detail2 = new OrderDetail();
        detail2.setId(pk2);
        detail2.setOrderTran(orderTran);
        detail2.setBookId(2);
        detail2.setBookName("Spring Boot入門");
        detail2.setPublisherName("翔泳社");
        detail2.setPrice(1000);
        detail2.setCount(1);
        persistAndFlush(detail2);
        
        clearEntityCache();
        
        // When: JPQL で注文IDで検索
        String jpql = "SELECT od FROM OrderDetail od " +
                      "WHERE od.id.orderTranId = :orderTranId " +
                      "ORDER BY od.id.orderDetailId";
        List<OrderDetail> results = em.createQuery(jpql, OrderDetail.class)
                .setParameter("orderTranId", orderTran.getOrderTranId())
                .getResultList();
        
        // Then: 注文明細リストが返される（注文明細IDの昇順）
        assertNotNull(results);
        assertEquals(2, results.size());
        
        OrderDetail foundDetail1 = results.get(0);
        assertEquals(1, foundDetail1.getId().getOrderDetailId());
        assertEquals(1, foundDetail1.getBookId());
        assertEquals("Java完全理解", foundDetail1.getBookName());
        assertEquals(2, foundDetail1.getCount());
        
        OrderDetail foundDetail2 = results.get(1);
        assertEquals(2, foundDetail2.getId().getOrderDetailId());
        assertEquals(2, foundDetail2.getBookId());
        assertEquals("Spring Boot入門", foundDetail2.getBookName());
        assertEquals(1, foundDetail2.getCount());
        
        logger.info("[ OrderDetailDaoIntegrationTest#testFindByOrderTranId_Success ] PASS");
    }
    
    /**
     * Scenario: 存在しない注文IDで検索
     * 
     * Given: DBに注文明細が存在しない
     * When: orderTranId=999で検索する
     * Then: 空のリストが返される
     */
    @Test
    void testFindByOrderTranId_NotFound() {
        logger.info("[ OrderDetailDaoIntegrationTest#testFindByOrderTranId_NotFound ] START");
        
        // Given: DBにデータなし
        
        // When: 存在しない注文IDで検索
        String jpql = "SELECT od FROM OrderDetail od " +
                      "WHERE od.id.orderTranId = :orderTranId " +
                      "ORDER BY od.id.orderDetailId";
        List<OrderDetail> results = em.createQuery(jpql, OrderDetail.class)
                .setParameter("orderTranId", 999)
                .getResultList();
        
        // Then: 空のリストが返される
        assertTrue(results.isEmpty());
        
        logger.info("[ OrderDetailDaoIntegrationTest#testFindByOrderTranId_NotFound ] PASS");
    }
    
    /**
     * Scenario: 複数の注文の明細を検索
     * 
     * Given: 複数の注文と注文明細が存在する
     * When: それぞれの注文IDで検索する
     * Then: 各注文の明細のみが返される
     */
    @Test
    void testFindByOrderTranId_MultipleOrders() {
        logger.info("[ OrderDetailDaoIntegrationTest#testFindByOrderTranId_MultipleOrders ] START");
        
        // Given: 注文1を作成
        OrderTran order1 = new OrderTran();
        order1.setOrderDate(LocalDate.now());
        order1.setCustomerId(1);
        order1.setTotalPrice(3000);
        order1.setDeliveryPrice(500);
        order1.setDeliveryAddress("東京都渋谷区1-1-1");
        order1.setSettlementType(1);
        persistAndFlush(order1);
        
        OrderDetailPK pk1_1 = new OrderDetailPK(order1.getOrderTranId(), 1);
        OrderDetail detail1_1 = new OrderDetail();
        detail1_1.setId(pk1_1);
        detail1_1.setOrderTran(order1);
        detail1_1.setBookId(1);
        detail1_1.setBookName("Java完全理解");
        detail1_1.setPublisherName("技術評論社");
        detail1_1.setPrice(2000);
        detail1_1.setCount(2);
        persistAndFlush(detail1_1);
        
        // Given: 注文2を作成
        OrderTran order2 = new OrderTran();
        order2.setOrderDate(LocalDate.now());
        order2.setCustomerId(1);
        order2.setTotalPrice(2000);
        order2.setDeliveryPrice(500);
        order2.setDeliveryAddress("東京都渋谷区1-1-1");
        order2.setSettlementType(1);
        persistAndFlush(order2);
        
        OrderDetailPK pk2_1 = new OrderDetailPK(order2.getOrderTranId(), 1);
        OrderDetail detail2_1 = new OrderDetail();
        detail2_1.setId(pk2_1);
        detail2_1.setOrderTran(order2);
        detail2_1.setBookId(2);
        detail2_1.setBookName("Spring Boot入門");
        detail2_1.setPublisherName("翔泳社");
        detail2_1.setPrice(1500);
        detail2_1.setCount(1);
        persistAndFlush(detail2_1);
        
        clearEntityCache();
        
        // When: 注文1の明細を検索
        String jpql = "SELECT od FROM OrderDetail od " +
                      "WHERE od.id.orderTranId = :orderTranId " +
                      "ORDER BY od.id.orderDetailId";
        List<OrderDetail> results1 = em.createQuery(jpql, OrderDetail.class)
                .setParameter("orderTranId", order1.getOrderTranId())
                .getResultList();
        
        // Then: 注文1の明細のみが返される
        assertEquals(1, results1.size());
        assertEquals(1, results1.get(0).getBookId());
        assertEquals("Java完全理解", results1.get(0).getBookName());
        
        // When: 注文2の明細を検索
        List<OrderDetail> results2 = em.createQuery(jpql, OrderDetail.class)
                .setParameter("orderTranId", order2.getOrderTranId())
                .getResultList();
        
        // Then: 注文2の明細のみが返される
        assertEquals(1, results2.size());
        assertEquals(2, results2.get(0).getBookId());
        assertEquals("Spring Boot入門", results2.get(0).getBookName());
        
        logger.info("[ OrderDetailDaoIntegrationTest#testFindByOrderTranId_MultipleOrders ] PASS");
    }
}
