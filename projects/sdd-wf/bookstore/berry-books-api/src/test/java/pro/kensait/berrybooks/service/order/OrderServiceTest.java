package pro.kensait.berrybooks.service.order;

import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.berrybooks.api.dto.CartItemRequest;
import pro.kensait.berrybooks.api.dto.OrderRequest;
import pro.kensait.berrybooks.common.exception.OutOfStockException;
import pro.kensait.berrybooks.dao.OrderDetailDao;
import pro.kensait.berrybooks.dao.OrderTranDao;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.external.BackOfficeRestClient;
import pro.kensait.berrybooks.external.dto.StockTO;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * OrderService の単体テスト
 * 
 * テスト対象: 注文処理ビジネスロジック
 * モック対象: OrderTranDao, OrderDetailDao, BackOfficeRestClient, DeliveryFeeService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService のテスト")
class OrderServiceTest {
    
    @Mock
    private OrderTranDao orderTranDao;
    
    @Mock
    private OrderDetailDao orderDetailDao;
    
    @Mock
    private BackOfficeRestClient backOfficeClient;
    
    @Mock
    private DeliveryFeeService deliveryFeeService;
    
    @InjectMocks
    private OrderService orderService;
    
    private OrderRequest orderRequest;
    private Integer customerId;
    
    @BeforeEach
    void setUp() {
        // Given: テストデータを準備
        customerId = 1;
        
        CartItemRequest cartItem = new CartItemRequest(
                1, 
                "Java完全理解", 
                "技術評論社", 
                3200, 
                2, 
                1L
        );
        
        orderRequest = new OrderRequest(
                List.of(cartItem),
                7200,
                800,
                "東京都渋谷区1-2-3",
                1
        );
    }
    
    @Test
    @DisplayName("正常系: 注文作成が成功する")
    void testCreateOrder_Success() {
        // Given: 在庫情報と注文データをモック化
        StockTO stock = new StockTO(1, "Java完全理解", 10, 1L);
        when(backOfficeClient.findStockById(1)).thenReturn(stock);
        when(backOfficeClient.updateStock(eq(1), eq(8), eq(1L))).thenReturn(stock);
        
        OrderTran savedOrderTran = new OrderTran();
        savedOrderTran.setOrderTranId(1);
        when(orderTranDao.insert(any(OrderTran.class))).thenReturn(savedOrderTran);
        
        OrderDetail savedOrderDetail = new OrderDetail();
        when(orderDetailDao.insert(any(OrderDetail.class))).thenReturn(savedOrderDetail);
        
        // When: createOrder()を呼び出す
        OrderTran result = orderService.createOrder(orderRequest, customerId);
        
        // Then: 注文が正常に作成される
        assertNotNull(result);
        assertEquals(1, result.getOrderTranId());
        
        // 在庫確認・更新が呼び出される
        verify(backOfficeClient, times(1)).findStockById(1);
        verify(backOfficeClient, times(1)).updateStock(eq(1), eq(8), eq(1L));
        
        // 注文トランザクション・注文明細が作成される
        verify(orderTranDao, times(1)).insert(any(OrderTran.class));
        verify(orderDetailDao, times(1)).insert(any(OrderDetail.class));
    }
    
    @Test
    @DisplayName("異常系: 在庫不足エラー")
    void testCreateOrder_OutOfStock() {
        // Given: 在庫情報（在庫1、注文数2 → 不足）
        StockTO stock = new StockTO(1, "Java完全理解", 1, 1L);
        when(backOfficeClient.findStockById(1)).thenReturn(stock);
        
        // When & Then: OutOfStockExceptionがスローされる
        assertThrows(OutOfStockException.class, () -> {
            orderService.createOrder(orderRequest, customerId);
        });
        
        // 在庫確認は呼び出されるが、在庫更新は呼び出されない
        verify(backOfficeClient, times(1)).findStockById(1);
        verify(backOfficeClient, never()).updateStock(anyInt(), anyInt(), anyLong());
        
        // 注文トランザクション・注文明細は作成されない
        verify(orderTranDao, never()).insert(any(OrderTran.class));
        verify(orderDetailDao, never()).insert(any(OrderDetail.class));
    }
    
    @Test
    @DisplayName("異常系: 楽観的ロック競合エラー")
    void testCreateOrder_OptimisticLockException() {
        // Given: 在庫情報、在庫更新で409 Conflictがスローされる
        StockTO stock = new StockTO(1, "Java完全理解", 10, 1L);
        when(backOfficeClient.findStockById(1)).thenReturn(stock);
        
        Response mockResponse = Response.status(409).build();
        WebApplicationException webEx = new WebApplicationException(mockResponse);
        when(backOfficeClient.updateStock(eq(1), eq(8), eq(1L))).thenThrow(webEx);
        
        // When & Then: OptimisticLockExceptionがスローされる
        assertThrows(OptimisticLockException.class, () -> {
            orderService.createOrder(orderRequest, customerId);
        });
        
        // 在庫確認・更新は呼び出される
        verify(backOfficeClient, times(1)).findStockById(1);
        verify(backOfficeClient, times(1)).updateStock(eq(1), eq(8), eq(1L));
        
        // 注文トランザクション・注文明細は作成されない
        verify(orderTranDao, never()).insert(any(OrderTran.class));
        verify(orderDetailDao, never()).insert(any(OrderDetail.class));
    }
    
    @Test
    @DisplayName("異常系: 複数カートアイテム、一部在庫不足")
    void testCreateOrder_MultipleItems_PartialOutOfStock() {
        // Given: カートアイテム1（在庫充分）、カートアイテム2（在庫不足）
        CartItemRequest cartItem1 = new CartItemRequest(
                1, "Java完全理解", "技術評論社", 3200, 2, 1L
        );
        CartItemRequest cartItem2 = new CartItemRequest(
                2, "Python入門", "翔泳社", 2800, 5, 1L
        );
        
        OrderRequest multiItemRequest = new OrderRequest(
                List.of(cartItem1, cartItem2),
                10000,
                800,
                "東京都渋谷区1-2-3",
                1
        );
        
        StockTO stock1 = new StockTO(1, "Java完全理解", 10, 1L);
        StockTO stock2 = new StockTO(2, "Python入門", 3, 1L); // 在庫不足
        
        when(backOfficeClient.findStockById(1)).thenReturn(stock1);
        when(backOfficeClient.findStockById(2)).thenReturn(stock2);
        
        // When & Then: OutOfStockExceptionがスローされる
        assertThrows(OutOfStockException.class, () -> {
            orderService.createOrder(multiItemRequest, customerId);
        });
        
        // 在庫確認は両方呼び出されるが、在庫更新は呼び出されない
        verify(backOfficeClient, times(1)).findStockById(1);
        verify(backOfficeClient, times(1)).findStockById(2);
        verify(backOfficeClient, never()).updateStock(anyInt(), anyInt(), anyLong());
        
        // 注文トランザクション・注文明細は作成されない
        verify(orderTranDao, never()).insert(any(OrderTran.class));
        verify(orderDetailDao, never()).insert(any(OrderDetail.class));
    }
    
    @Test
    @DisplayName("正常系: 注文履歴を取得する")
    void testGetOrderHistory_Success() {
        // Given: 顧客IDで注文履歴を取得
        OrderTran orderTran1 = new OrderTran();
        orderTran1.setOrderTranId(1);
        
        OrderTran orderTran2 = new OrderTran();
        orderTran2.setOrderTranId(2);
        
        when(orderTranDao.findByCustomerId(customerId)).thenReturn(List.of(orderTran1, orderTran2));
        
        // When: getOrderHistory()を呼び出す
        List<OrderTran> result = orderService.getOrderHistory(customerId);
        
        // Then: 注文履歴が返される
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getOrderTranId());
        assertEquals(2, result.get(1).getOrderTranId());
        
        // OrderTranDao.findByCustomerId()が呼び出される
        verify(orderTranDao, times(1)).findByCustomerId(customerId);
    }
    
    @Test
    @DisplayName("正常系: 注文詳細を取得する")
    void testGetOrderById_Success() {
        // Given: 注文IDで注文詳細を取得
        OrderTran orderTran = new OrderTran();
        orderTran.setOrderTranId(1);
        orderTran.setOrderDetails(List.of(new OrderDetail()));
        
        when(orderTranDao.findById(1)).thenReturn(Optional.of(orderTran));
        
        // When: getOrderById()を呼び出す
        OrderTran result = orderService.getOrderById(1);
        
        // Then: 注文詳細が返される
        assertNotNull(result);
        assertEquals(1, result.getOrderTranId());
        assertEquals(1, result.getOrderDetails().size());
        
        // OrderTranDao.findById()が呼び出される
        verify(orderTranDao, times(1)).findById(1);
    }
    
    @Test
    @DisplayName("異常系: 存在しない注文IDを指定 → nullが返される")
    void testGetOrderById_NotFound() {
        // Given: 存在しない注文ID
        when(orderTranDao.findById(999)).thenReturn(Optional.empty());
        
        // When: getOrderById()を呼び出す
        OrderTran result = orderService.getOrderById(999);
        
        // Then: nullが返される
        assertNull(result);
        
        // OrderTranDao.findById()が呼び出される
        verify(orderTranDao, times(1)).findById(999);
    }
}
