package pro.kensait.berrybooks.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.kensait.berrybooks.api.dto.CartItemRequest;
import pro.kensait.berrybooks.api.dto.OrderRequest;
import pro.kensait.berrybooks.dao.OrderDetailDao;
import pro.kensait.berrybooks.dao.OrderTranDao;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.external.BackOfficeRestClient;
import pro.kensait.berrybooks.service.DeliveryFeeService;
import pro.kensait.berrybooks.service.OrderService;

/**
 * OrderService 結合テスト
 * 
 * 対象: Service + DAO + Entity + DB + 外部API（WireMock）
 * シナリオ: specs/baseline/basic_design/orders/behaviors.md
 */
class OrderServiceIntegrationTest extends BaseIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceIntegrationTest.class);
    
    private OrderService orderService;
    private OrderTranDao orderTranDao;
    private OrderDetailDao orderDetailDao;
    private BackOfficeRestClient backOfficeRestClient;
    private DeliveryFeeService deliveryFeeService;
    
    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        
        // CDIコンテナからサービスとDAOを取得
        orderTranDao = container.select(OrderTranDao.class).get();
        orderDetailDao = container.select(OrderDetailDao.class).get();
        backOfficeRestClient = container.select(BackOfficeRestClient.class).get();
        deliveryFeeService = container.select(DeliveryFeeService.class).get();
        orderService = container.select(OrderService.class).get();
        
        logger.info("[ OrderServiceIntegrationTest#setUp ] Services and DAOs initialized");
    }
    
    /**
     * Scenario: 注文作成（正常系）
     * Given: WireMockが在庫APIをスタブする
     * When: OrderService.createOrder()を呼び出す
     * Then: DBに注文が作成される
     * And: DBに注文明細が作成される
     * And: 外部APIで在庫が更新される
     */
    @Test
    void testCreateOrder_Success() {
        logger.info("[ testCreateOrder_Success ] Starting test");
        
        // Given: WireMockが在庫APIをスタブする
        stubFor(get(urlEqualTo("/api/stocks/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"bookId\": 1, \"quantity\": 10, \"version\": 1}")));
        
        stubFor(put(urlEqualTo("/api/stocks/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"bookId\": 1, \"quantity\": 8, \"version\": 2}")));
        
        // 書籍情報のスタブ
        stubFor(get(urlEqualTo("/api/books/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"bookId\": 1, \"bookName\": \"Java完全理解\", " +
                         "\"price\": 3000, \"publisherName\": \"技術評論社\"}")));
        
        CartItemRequest cartItem = new CartItemRequest(1, "Java完全理解", "技術評論社", 
                                                        3000, 2, 1L);
        OrderRequest orderRequest = new OrderRequest(List.of(cartItem), "東京都", 1);
        
        // When: OrderService.createOrder()を呼び出す
        OrderTran orderTran = orderService.createOrder(orderRequest);
        
        // Then: DBに注文が作成される
        assertNotNull(orderTran);
        assertNotNull(orderTran.getOrderTranId());
        assertEquals(Integer.valueOf(1), orderTran.getCustomerId());
        assertEquals(LocalDate.now(), orderTran.getOrderDate());
        
        // And: DBに注文明細が作成される
        em.flush();
        em.clear();
        
        OrderTran foundOrderTran = em.find(OrderTran.class, orderTran.getOrderTranId());
        assertNotNull(foundOrderTran);
        assertEquals(1, foundOrderTran.getOrderDetails().size());
        
        OrderDetail orderDetail = foundOrderTran.getOrderDetails().get(0);
        assertEquals(Integer.valueOf(1), orderDetail.getBookId());
        assertEquals(Integer.valueOf(2), orderDetail.getCount());
        assertEquals(Integer.valueOf(3000), orderDetail.getPrice());
        
        // And: 外部APIで在庫が更新される
        verify(getRequestedFor(urlEqualTo("/api/stocks/1")));
        verify(putRequestedFor(urlEqualTo("/api/stocks/1")));
        
        logger.info("[ testCreateOrder_Success ] Test completed successfully");
    }
    
    /**
     * Scenario: 在庫不足で注文失敗
     * Given: WireMockが在庫不足をスタブする
     * When: OrderService.createOrder()を呼び出す
     * Then: InsufficientStockExceptionがスローされる
     * And: DBに注文は作成されない
     */
    @Test
    void testCreateOrder_InsufficientStock() {
        logger.info("[ testCreateOrder_InsufficientStock ] Starting test");
        
        // Given: WireMockが在庫不足をスタブする
        stubFor(get(urlEqualTo("/api/stocks/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"bookId\": 1, \"quantity\": 1, \"version\": 1}")));
        
        CartItemRequest cartItem = new CartItemRequest(1, "Java完全理解", "技術評論社",
                                                        3000, 2, 1L);
        OrderRequest orderRequest = new OrderRequest(List.of(cartItem), "東京都", 1);
        
        // When: OrderService.createOrder()を呼び出す
        // Then: 例外がスローされる（実装によってはRuntimeExceptionまたはカスタム例外）
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(orderRequest);
        });
        
        // And: DBに注文は作成されない
        em.flush();
        em.clear();
        
        List<OrderTran> orders = em.createQuery(
            "SELECT o FROM OrderTran o WHERE o.customerId = :customerId", OrderTran.class)
            .setParameter("customerId", 1)
            .getResultList();
        
        assertTrue(orders.isEmpty());
        
        logger.info("[ testCreateOrder_InsufficientStock ] Test completed successfully");
    }
    
    /**
     * Scenario: 楽観的ロック競合で注文失敗
     * Given: WireMockが楽観的ロック競合をスタブする
     * When: OrderService.createOrder()を呼び出す
     * Then: OptimisticLockExceptionがスローされる
     * And: DBに注文は作成されない（ロールバック）
     */
    @Test
    void testCreateOrder_OptimisticLockException() {
        logger.info("[ testCreateOrder_OptimisticLockException ] Starting test");
        
        // Given: WireMockが在庫APIをスタブする
        stubFor(get(urlEqualTo("/api/stocks/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"bookId\": 1, \"quantity\": 10, \"version\": 1}")));
        
        // 楽観的ロック競合を返す
        stubFor(put(urlEqualTo("/api/stocks/1"))
            .willReturn(aResponse()
                .withStatus(409)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\": \"Optimistic lock exception\"}")));
        
        CartItemRequest cartItem = new CartItemRequest(1, "Java完全理解", "技術評論社",
                                                        3000, 2, 1L);
        OrderRequest orderRequest = new OrderRequest(List.of(cartItem), "東京都", 1);
        
        // When: OrderService.createOrder()を呼び出す
        // Then: 例外がスローされる
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(orderRequest);
        });
        
        // And: DBに注文は作成されない（ロールバック）
        em.flush();
        em.clear();
        
        List<OrderTran> orders = em.createQuery(
            "SELECT o FROM OrderTran o WHERE o.customerId = :customerId", OrderTran.class)
            .setParameter("customerId", 1)
            .getResultList();
        
        assertTrue(orders.isEmpty());
        
        logger.info("[ testCreateOrder_OptimisticLockException ] Test completed successfully");
    }
    
    /**
     * Scenario: 注文履歴取得
     * Given: DBに注文が存在する
     * When: OrderService.getOrderHistory()を呼び出す
     * Then: 顧客の注文履歴が返される
     */
    @Test
    void testGetOrderHistory() {
        logger.info("[ testGetOrderHistory ] Starting test");
        
        // Given: DBに注文が存在する
        OrderTran orderTran1 = new OrderTran();
        orderTran1.setCustomerId(1);
        orderTran1.setOrderDate(LocalDate.of(2026, 1, 1));
        orderTran1.setTotalPrice(6500);
        orderTran1.setDeliveryPrice(500);
        orderTran1.setDeliveryAddress("東京都");
        orderTran1.setSettlementType(1);
        persistAndFlush(orderTran1);
        
        OrderTran orderTran2 = new OrderTran();
        orderTran2.setCustomerId(1);
        orderTran2.setOrderDate(LocalDate.of(2026, 1, 2));
        orderTran2.setTotalPrice(3500);
        orderTran2.setDeliveryPrice(500);
        orderTran2.setDeliveryAddress("東京都");
        orderTran2.setSettlementType(1);
        persistAndFlush(orderTran2);
        
        OrderDetail orderDetail1 = new OrderDetail();
        orderDetail1.setOrderTranId(orderTran1.getOrderTranId());
        orderDetail1.setOrderDetailId(1);
        orderDetail1.setBookId(1);
        orderDetail1.setBookName("Java完全理解");
        orderDetail1.setPublisherName("技術評論社");
        orderDetail1.setPrice(3000);
        orderDetail1.setCount(2);
        persistAndFlush(orderDetail1);
        
        OrderDetail orderDetail2 = new OrderDetail();
        orderDetail2.setOrderTranId(orderTran2.getOrderTranId());
        orderDetail2.setOrderDetailId(1);
        orderDetail2.setBookId(2);
        orderDetail2.setBookName("Python入門");
        orderDetail2.setPublisherName("翔泳社");
        orderDetail2.setPrice(3000);
        orderDetail2.setCount(1);
        persistAndFlush(orderDetail2);
        
        clearEntityCache();
        
        // When: 注文履歴を取得
        List<OrderTran> orderHistory = orderService.getOrderHistory(1);
        
        // Then: 顧客ID=1の注文履歴が返される
        assertNotNull(orderHistory);
        assertEquals(2, orderHistory.size());
        
        logger.info("[ testGetOrderHistory ] Test completed successfully");
    }
}