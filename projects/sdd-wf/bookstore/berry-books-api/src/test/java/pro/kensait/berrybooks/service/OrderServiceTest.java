package pro.kensait.berrybooks.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.berrybooks.api.dto.CartItemRequest;
import pro.kensait.berrybooks.api.dto.OrderRequest;
import pro.kensait.berrybooks.dao.OrderDetailDao;
import pro.kensait.berrybooks.dao.OrderTranDao;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.external.BackOfficeRestClient;
import pro.kensait.berrybooks.external.dto.StockTO;
import pro.kensait.berrybooks.security.AuthenticatedUser;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * OrderServiceの単体テスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 単体テスト")
class OrderServiceTest {
    
    @Mock
    private OrderTranDao orderTranDao;
    
    @Mock
    private OrderDetailDao orderDetailDao;
    
    @Mock
    private BackOfficeRestClient backOfficeClient;
    
    @Mock
    private DeliveryFeeService deliveryFeeService;
    
    @Mock
    private AuthenticatedUser authenticatedUser;
    
    @InjectMocks
    private OrderService orderService;
    
    private OrderRequest orderRequest;
    private List<CartItemRequest> cartItems;
    
    @BeforeEach
    void setUp() {
        // Given: テストデータの準備
        CartItemRequest item1 = new CartItemRequest(
            10, "Java入門", "技術評論社", 3000, 2, 1L
        );
        CartItemRequest item2 = new CartItemRequest(
            20, "Spring Boot実践", "翔泳社", 3500, 1, 1L
        );
        cartItems = Arrays.asList(item1, item2);
        
        orderRequest = new OrderRequest(
            cartItems,
            "東京都渋谷区1-1-1",
            2 // クレジットカード
        );
    }
    
    @Test
    @DisplayName("注文を正常に作成")
    void testCreateOrder_Success() {
        // Given: モック設定
        when(authenticatedUser.getCustomerId()).thenReturn(1);

        // 在庫情報
        StockTO stock1 = new StockTO();
        stock1.setBookId(10);
        stock1.setQuantity(10);
        stock1.setVersion(1L);

        StockTO stock2 = new StockTO();
        stock2.setBookId(20);
        stock2.setQuantity(5);
        stock2.setVersion(1L);

        when(backOfficeClient.findStockById(10)).thenReturn(stock1);
        when(backOfficeClient.findStockById(20)).thenReturn(stock2);
        
        // 配送料金
        when(deliveryFeeService.calculateDeliveryFee(anyInt(), anyString())).thenReturn(400);
        
        // OrderTran作成
        OrderTran orderTran = new OrderTran();
        orderTran.setOrderTranId(1);
        when(orderTranDao.insert(any(OrderTran.class))).thenReturn(orderTran);
        
        // 在庫更新
        when(backOfficeClient.updateStock(anyInt(), anyLong(), anyInt())).thenReturn(stock1);
        
        // When: createOrder()を呼び出す
        OrderTran result = orderService.createOrder(orderRequest);
        
        // Then: 各メソッドが呼び出される
        assertNotNull(result);
        assertEquals(1, result.getOrderTranId());
        verify(orderTranDao, times(1)).insert(any(OrderTran.class));
        verify(orderDetailDao, times(2)).insert(any());
        verify(backOfficeClient, times(2)).updateStock(anyInt(), anyLong(), anyInt());
    }
    
    @Test
    @DisplayName("在庫不足時に例外をスロー")
    void testCreateOrder_OutOfStock() {
        // Given: 在庫不足のモック設定
        when(authenticatedUser.getCustomerId()).thenReturn(1);

        StockTO stock = new StockTO();
        stock.setBookId(10);
        stock.setQuantity(1); // 在庫1個
        stock.setVersion(1L);

        when(backOfficeClient.findStockById(10)).thenReturn(stock);
        
        // When & Then: 在庫不足でRuntimeExceptionがスローされる
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(orderRequest);
        });
        
        assertTrue(exception.getMessage().contains("在庫不足"));
        verify(orderTranDao, never()).insert(any());
    }
    
    @Test
    @DisplayName("注文履歴を取得")
    void testGetOrderHistory() {
        // Given: モック設定
        List<OrderTran> mockOrders = Arrays.asList(new OrderTran(), new OrderTran());
        when(orderTranDao.findByCustomerId(1)).thenReturn(mockOrders);
        
        // When: getOrderHistory()を呼び出す
        List<OrderTran> result = orderService.getOrderHistory(1);
        
        // Then: 注文履歴が返される
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderTranDao, times(1)).findByCustomerId(1);
    }
    
    @Test
    @DisplayName("注文詳細を取得")
    void testGetOrderDetail() {
        // Given: モック設定
        OrderTran mockOrder = new OrderTran();
        mockOrder.setOrderTranId(1);
        when(orderTranDao.findById(1)).thenReturn(mockOrder);
        
        // When: getOrderDetail()を呼び出す
        OrderTran result = orderService.getOrderDetail(1);
        
        // Then: 注文詳細が返される
        assertNotNull(result);
        assertEquals(1, result.getOrderTranId());
        verify(orderTranDao, times(1)).findById(1);
    }
}