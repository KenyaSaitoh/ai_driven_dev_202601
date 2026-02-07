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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderTranDao 結合テスト
 * 
 * テスト対象: DAO + Entity + DB
 * 
 * シナリオ:
 * * 注文履歴の取得
 * * 注文詳細の取得（明細含む）
 */
class OrderTranDaoIntegrationTest extends BaseIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderTranDaoIntegrationTest.class);
    
    /**
     * Scenario: 注文履歴を取得
     * 
     * Given: DBに以下の注文が存在する
     * When: customerId=1で検索する
     * Then: 顧客ID=1の注文2件が返される（注文日の降順）
     */
    @Test
    void testFindByCustomerId_Success() {
        logger.info("[ OrderTranDaoIntegrationTest#testFindByCustomerId_Success ] START");
        
        // Given: DBに注文データを投入
        OrderTran order1 = new OrderTran();
        order1.setOrderDate(LocalDate.of(2026, 1, 1));
        order1.setCustomerId(1);
        order1.setTotalPrice(3000);
        order1.setDeliveryPrice(500);
        order1.setDeliveryAddress("東京都渋谷区1-1-1");
        order1.setSettlementType(1);
        persistAndFlush(order1);
        
        OrderTran order2 = new OrderTran();
        order2.setOrderDate(LocalDate.of(2026, 1, 2));
        order2.setCustomerId(1);
        order2.setTotalPrice(2000);
        order2.setDeliveryPrice(500);
        order2.setDeliveryAddress("東京都渋谷区1-1-1");
        order2.setSettlementType(2);
        persistAndFlush(order2);
        
        OrderTran order3 = new OrderTran();
        order3.setOrderDate(LocalDate.of(2026, 1, 3));
        order3.setCustomerId(2); // 別の顧客
        order3.setTotalPrice(1500);
        order3.setDeliveryPrice(500);
        order3.setDeliveryAddress("大阪府大阪市2-2-2");
        order3.setSettlementType(1);
        persistAndFlush(order3);
        
        clearEntityCache();
        
        // When: JPQL で顧客IDで検索
        String jpql = "SELECT o FROM OrderTran o WHERE o.customerId = :customerId ORDER BY o.orderDate DESC";
        List<OrderTran> results = em.createQuery(jpql, OrderTran.class)
                .setParameter("customerId", 1)
                .setMaxResults(100)
                .getResultList();
        
        // Then: 顧客ID=1の注文2件が返される（注文日の降順）
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(LocalDate.of(2026, 1, 2), results.get(0).getOrderDate());
        assertEquals(LocalDate.of(2026, 1, 1), results.get(1).getOrderDate());
        assertEquals(1, results.get(0).getCustomerId());
        assertEquals(1, results.get(1).getCustomerId());
        
        logger.info("[ OrderTranDaoIntegrationTest#testFindByCustomerId_Success ] PASS");
    }
    
    /**
     * Scenario: 注文詳細を取得
     * 
     * Given: DBに注文とリレーションデータが存在する
     * When: orderTranId=1で検索する
     * Then: 注文詳細が取得される（注文明細2件を含む）
     */
    @Test
    void testFindById_WithOrderDetails() {
        logger.info("[ OrderTranDaoIntegrationTest#testFindById_WithOrderDetails ] START");
        
        // Given: DBに注文と注文明細を投入
        OrderTran orderTran = new OrderTran();
        orderTran.setOrderDate(LocalDate.of(2026, 1, 1));
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
        
        // When: JPQL で注文IDで検索（JOIN FETCH で明細も取得）
        String jpql = "SELECT o FROM OrderTran o LEFT JOIN FETCH o.orderDetails WHERE o.orderTranId = :orderTranId";
        List<OrderTran> results = em.createQuery(jpql, OrderTran.class)
                .setParameter("orderTranId", orderTran.getOrderTranId())
                .getResultList();
        
        // Then: 注文詳細が取得される（明細2件を含む）
        assertFalse(results.isEmpty());
        OrderTran foundOrder = results.get(0);
        
        assertEquals(orderTran.getOrderTranId(), foundOrder.getOrderTranId());
        assertEquals(1, foundOrder.getCustomerId());
        assertEquals(5000, foundOrder.getTotalPrice());
        
        // 注文明細の検証
        assertNotNull(foundOrder.getOrderDetails());
        assertEquals(2, foundOrder.getOrderDetails().size());
        
        OrderDetail foundDetail1 = foundOrder.getOrderDetails().get(0);
        assertEquals(1, foundDetail1.getBookId());
        assertEquals("Java完全理解", foundDetail1.getBookName());
        assertEquals(2, foundDetail1.getCount());
        
        OrderDetail foundDetail2 = foundOrder.getOrderDetails().get(1);
        assertEquals(2, foundDetail2.getBookId());
        assertEquals("Spring Boot入門", foundDetail2.getBookName());
        assertEquals(1, foundDetail2.getCount());
        
        logger.info("[ OrderTranDaoIntegrationTest#testFindById_WithOrderDetails ] PASS");
    }
    
    /**
     * Scenario: 存在しない注文IDで検索
     * 
     * Given: DBに注文が存在しない
     * When: orderTranId=999で検索する
     * Then: 空のリストが返される
     */
    @Test
    void testFindById_NotFound() {
        logger.info("[ OrderTranDaoIntegrationTest#testFindById_NotFound ] START");
        
        // Given: DBにデータなし
        
        // When: 存在しない注文IDで検索
        String jpql = "SELECT o FROM OrderTran o LEFT JOIN FETCH o.orderDetails WHERE o.orderTranId = :orderTranId";
        List<OrderTran> results = em.createQuery(jpql, OrderTran.class)
                .setParameter("orderTranId", 999)
                .getResultList();
        
        // Then: 空のリストが返される
        assertTrue(results.isEmpty());
        
        logger.info("[ OrderTranDaoIntegrationTest#testFindById_NotFound ] PASS");
    }
    
    /**
     * Scenario: 注文を作成
     * 
     * Given: 注文データ
     * When: INSERT する
     * Then: DBに注文が作成される
     */
    @Test
    void testInsert_Success() {
        logger.info("[ OrderTranDaoIntegrationTest#testInsert_Success ] START");
        
        // Given: 注文データ
        OrderTran orderTran = new OrderTran();
        orderTran.setOrderDate(LocalDate.now());
        orderTran.setCustomerId(1);
        orderTran.setTotalPrice(3000);
        orderTran.setDeliveryPrice(500);
        orderTran.setDeliveryAddress("東京都渋谷区1-1-1");
        orderTran.setSettlementType(1);
        
        // When: INSERT
        persistAndFlush(orderTran);
        
        // Then: orderTranIdが自動採番される
        assertNotNull(orderTran.getOrderTranId());
        assertTrue(orderTran.getOrderTranId() > 0);
        
        // DBから再取得して検証
        clearEntityCache();
        OrderTran foundOrder = em.find(OrderTran.class, orderTran.getOrderTranId());
        
        assertNotNull(foundOrder);
        assertEquals(orderTran.getOrderTranId(), foundOrder.getOrderTranId());
        assertEquals(1, foundOrder.getCustomerId());
        assertEquals(3000, foundOrder.getTotalPrice());
        assertEquals(500, foundOrder.getDeliveryPrice());
        assertEquals("東京都渋谷区1-1-1", foundOrder.getDeliveryAddress());
        assertEquals(1, foundOrder.getSettlementType());
        
        logger.info("[ OrderTranDaoIntegrationTest#testInsert_Success ] PASS");
    }
}
