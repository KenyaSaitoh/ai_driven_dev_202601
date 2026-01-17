package pro.kensait.berrybooks.service.order;

import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.berrybooks.api.dto.CartItemRequest;
import pro.kensait.berrybooks.api.dto.OrderRequest;
import pro.kensait.berrybooks.api.exception.OutOfStockException;
import pro.kensait.berrybooks.dao.OrderDetailDao;
import pro.kensait.berrybooks.dao.OrderTranDao;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.external.BackOfficeRestClient;
import pro.kensait.berrybooks.external.dto.StockTO;
import pro.kensait.berrybooks.service.delivery.DeliveryFeeService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrderServiceTest - 注文処理サービスのテスト
 * 
 * テスト対象:
 * - OrderService.orderBooks(Long customerId, OrderRequest request)
 * 
 * モック対象:
 * - OrderTranDao
 * - OrderDetailDao
 * - BackOfficeRestClient
 * - DeliveryFeeService
 * 
 * テスト方針:
 * - 正常系: 注文作成成功
 * - 異常系: 在庫不足、楽観的ロック競合
 * - メソッド呼び出し検証
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
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
    
    // テストデータ
    private Long customerId;
    private OrderRequest orderRequest;
    private CartItemRequest cartItem;
    private StockTO stockTO;
    private OrderTran orderTran;
    
    @BeforeEach
    void setUp() {
        customerId = 1L;
        
        // カートアイテム作成
        cartItem = new CartItemRequest(
            1,
            "Java完全理解",
            "技術評論社",
            3200,
            2,
            1L
        );
        
        // 注文リクエスト作成
        orderRequest = new OrderRequest(
            List.of(cartItem),
            7200,
            800,
            "東京都渋谷区1-2-3",
            1
        );
        
        // 在庫情報作成
        stockTO = new StockTO(1, "Java完全理解", 10, 1L);
        
        // 注文トランザクション作成
        orderTran = new OrderTran();
        orderTran.setOrderTranId(1);
        orderTran.setCustomerId(1);
        orderTran.setTotalPrice(7200);
        orderTran.setDeliveryPrice(800);
        orderTran.setDeliveryAddress("東京都渋谷区1-2-3");
        orderTran.setSettlementType(1);
    }
    
    // 正常系テスト
    
    @Test
    @DisplayName("OS-001: 注文作成成功（1冊）")
    void testOrderBooks_Success_SingleBook() {
        // Given
        when(backOfficeClient.findStockById(1)).thenReturn(stockTO);
        when(backOfficeClient.updateStock(eq(1), eq(1L), eq(8))).thenReturn(stockTO);
        when(deliveryFeeService.calculateDeliveryFee(anyInt(), anyString())).thenReturn(800);
        when(orderTranDao.insert(any(OrderTran.class))).thenReturn(orderTran);
        when(orderDetailDao.insert(any(OrderDetail.class))).thenReturn(new OrderDetail());
        
        // When
        OrderTran result = orderService.orderBooks(customerId, orderRequest);
        
        // Then
        assertNotNull(result, "注文トランザクションが作成される");
        verify(backOfficeClient, times(1)).findStockById(1);
        verify(backOfficeClient, times(1)).updateStock(1, 1L, 8);
        verify(deliveryFeeService, times(1)).calculateDeliveryFee(anyInt(), eq("東京都渋谷区1-2-3"));
        verify(orderTranDao, times(1)).insert(any(OrderTran.class));
        verify(orderDetailDao, times(1)).insert(any(OrderDetail.class));
    }
    
    @Test
    @DisplayName("OS-002: 注文作成成功（複数冊）")
    void testOrderBooks_Success_MultipleBooks() {
        // Given
        CartItemRequest cartItem2 = new CartItemRequest(2, "Python入門", "技術評論社", 2800, 1, 1L);
        OrderRequest multiItemRequest = new OrderRequest(
            List.of(cartItem, cartItem2),
            9200,
            800,
            "東京都渋谷区1-2-3",
            1
        );
        StockTO stockTO2 = new StockTO(2, "Python入門", 5, 1L);
        
        when(backOfficeClient.findStockById(1)).thenReturn(stockTO);
        when(backOfficeClient.findStockById(2)).thenReturn(stockTO2);
        when(backOfficeClient.updateStock(eq(1), eq(1L), eq(8))).thenReturn(stockTO);
        when(backOfficeClient.updateStock(eq(2), eq(1L), eq(4))).thenReturn(stockTO2);
        when(deliveryFeeService.calculateDeliveryFee(anyInt(), anyString())).thenReturn(800);
        when(orderTranDao.insert(any(OrderTran.class))).thenReturn(orderTran);
        when(orderDetailDao.insert(any(OrderDetail.class))).thenReturn(new OrderDetail());
        
        // When
        OrderTran result = orderService.orderBooks(customerId, multiItemRequest);
        
        // Then
        assertNotNull(result, "注文トランザクションが作成される");
        verify(backOfficeClient, times(2)).findStockById(anyInt());
        verify(backOfficeClient, times(2)).updateStock(anyInt(), anyLong(), anyInt());
        verify(orderDetailDao, times(2)).insert(any(OrderDetail.class));
    }
    
    @Test
    @DisplayName("OS-003: 配送料金が正しく計算される")
    void testOrderBooks_DeliveryFeeCalculated() {
        // Given
        when(backOfficeClient.findStockById(1)).thenReturn(stockTO);
        when(backOfficeClient.updateStock(anyInt(), anyLong(), anyInt())).thenReturn(stockTO);
        when(deliveryFeeService.calculateDeliveryFee(6400, "東京都渋谷区1-2-3")).thenReturn(800);
        when(orderTranDao.insert(any(OrderTran.class))).thenReturn(orderTran);
        when(orderDetailDao.insert(any(OrderDetail.class))).thenReturn(new OrderDetail());
        
        // When
        orderService.orderBooks(customerId, orderRequest);
        
        // Then
        verify(deliveryFeeService, times(1)).calculateDeliveryFee(6400, "東京都渋谷区1-2-3");
    }
    
    // 異常系テスト（在庫不足）
    
    @Test
    @DisplayName("OS-E-001: 在庫不足エラー")
    void testOrderBooks_OutOfStock() {
        // Given - 在庫数=1、注文数=2
        StockTO lowStock = new StockTO(1, "Java完全理解", 1, 1L);
        when(backOfficeClient.findStockById(1)).thenReturn(lowStock);
        
        // When & Then
        OutOfStockException exception = assertThrows(OutOfStockException.class, () -> {
            orderService.orderBooks(customerId, orderRequest);
        });
        
        assertTrue(exception.getMessage().contains("在庫が不足しています"));
        verify(backOfficeClient, times(1)).findStockById(1);
        verify(backOfficeClient, never()).updateStock(anyInt(), anyLong(), anyInt());
        verify(orderTranDao, never()).insert(any(OrderTran.class));
    }
    
    @Test
    @DisplayName("OS-E-003: 在庫情報が存在しない")
    void testOrderBooks_StockNotFound() {
        // Given
        when(backOfficeClient.findStockById(1)).thenReturn(null);
        
        // When & Then
        OutOfStockException exception = assertThrows(OutOfStockException.class, () -> {
            orderService.orderBooks(customerId, orderRequest);
        });
        
        assertTrue(exception.getMessage().contains("在庫情報が見つかりません"));
        verify(backOfficeClient, times(1)).findStockById(1);
        verify(backOfficeClient, never()).updateStock(anyInt(), anyLong(), anyInt());
    }
    
    // 異常系テスト（楽観的ロック競合）
    
    @Test
    @DisplayName("OS-E-004: 楽観的ロック競合エラー")
    void testOrderBooks_OptimisticLockException() {
        // Given
        when(backOfficeClient.findStockById(1)).thenReturn(stockTO);
        when(backOfficeClient.updateStock(eq(1), eq(1L), eq(8)))
            .thenThrow(new OptimisticLockException("他のユーザーが購入済みです"));
        
        // When & Then
        assertThrows(OptimisticLockException.class, () -> {
            orderService.orderBooks(customerId, orderRequest);
        });
        
        verify(backOfficeClient, times(1)).findStockById(1);
        verify(backOfficeClient, times(1)).updateStock(1, 1L, 8);
        verify(orderTranDao, never()).insert(any(OrderTran.class));
    }
    
    // DAOメソッド呼び出し検証
    
    @Test
    @DisplayName("OS-DAO-001: OrderTranDao.insert()が呼ばれる")
    void testOrderBooks_OrderTranDaoInsertCalled() {
        // Given
        when(backOfficeClient.findStockById(anyInt())).thenReturn(stockTO);
        when(backOfficeClient.updateStock(anyInt(), anyLong(), anyInt())).thenReturn(stockTO);
        when(deliveryFeeService.calculateDeliveryFee(anyInt(), anyString())).thenReturn(800);
        when(orderTranDao.insert(any(OrderTran.class))).thenReturn(orderTran);
        when(orderDetailDao.insert(any(OrderDetail.class))).thenReturn(new OrderDetail());
        
        // When
        orderService.orderBooks(customerId, orderRequest);
        
        // Then
        verify(orderTranDao, times(1)).insert(any(OrderTran.class));
    }
    
    @Test
    @DisplayName("OS-DAO-002: OrderDetailDao.insert()が呼ばれる（1冊）")
    void testOrderBooks_OrderDetailDaoInsertCalled_SingleBook() {
        // Given
        when(backOfficeClient.findStockById(anyInt())).thenReturn(stockTO);
        when(backOfficeClient.updateStock(anyInt(), anyLong(), anyInt())).thenReturn(stockTO);
        when(deliveryFeeService.calculateDeliveryFee(anyInt(), anyString())).thenReturn(800);
        when(orderTranDao.insert(any(OrderTran.class))).thenReturn(orderTran);
        when(orderDetailDao.insert(any(OrderDetail.class))).thenReturn(new OrderDetail());
        
        // When
        orderService.orderBooks(customerId, orderRequest);
        
        // Then
        verify(orderDetailDao, times(1)).insert(any(OrderDetail.class));
    }
    
    // BackOfficeRestClient呼び出し検証
    
    @Test
    @DisplayName("OS-EXT-001: findStockById()が呼ばれる（1冊）")
    void testOrderBooks_FindStockByIdCalled_SingleBook() {
        // Given
        when(backOfficeClient.findStockById(1)).thenReturn(stockTO);
        when(backOfficeClient.updateStock(anyInt(), anyLong(), anyInt())).thenReturn(stockTO);
        when(deliveryFeeService.calculateDeliveryFee(anyInt(), anyString())).thenReturn(800);
        when(orderTranDao.insert(any(OrderTran.class))).thenReturn(orderTran);
        when(orderDetailDao.insert(any(OrderDetail.class))).thenReturn(new OrderDetail());
        
        // When
        orderService.orderBooks(customerId, orderRequest);
        
        // Then
        verify(backOfficeClient, times(1)).findStockById(1);
    }
    
    @Test
    @DisplayName("OS-EXT-003: updateStock()が呼ばれる（1冊）")
    void testOrderBooks_UpdateStockCalled_SingleBook() {
        // Given
        when(backOfficeClient.findStockById(1)).thenReturn(stockTO);
        when(backOfficeClient.updateStock(1, 1L, 8)).thenReturn(stockTO);
        when(deliveryFeeService.calculateDeliveryFee(anyInt(), anyString())).thenReturn(800);
        when(orderTranDao.insert(any(OrderTran.class))).thenReturn(orderTran);
        when(orderDetailDao.insert(any(OrderDetail.class))).thenReturn(new OrderDetail());
        
        // When
        orderService.orderBooks(customerId, orderRequest);
        
        // Then
        verify(backOfficeClient, times(1)).updateStock(1, 1L, 8);
    }
}
