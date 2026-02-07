package pro.kensait.berrybooks.api;

import jakarta.persistence.OptimisticLockException;
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
import pro.kensait.berrybooks.api.dto.OrderResponse;
import pro.kensait.berrybooks.common.ErrorResponse;
import pro.kensait.berrybooks.common.exception.OutOfStockException;
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderDetailPK;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.security.AuthenInfo;
import pro.kensait.berrybooks.service.order.OrderService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * OrderResource の単体テスト
 * 
 * テスト対象: 注文APIエンドポイント
 * モック対象: OrderService, AuthenInfo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderResource のテスト")
class OrderResourceTest {
    
    @Mock
    private OrderService orderService;
    
    @Mock
    private AuthenInfo authenInfo;
    
    @InjectMocks
    private OrderResource orderResource;
    
    private OrderRequest orderRequest;
    private OrderTran mockOrderTran;
    
    @BeforeEach
    void setUp() {
        // Given: テストデータを準備
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
        
        // モックのOrderTranを準備
        mockOrderTran = new OrderTran();
        mockOrderTran.setOrderTranId(1);
        mockOrderTran.setOrderDate(LocalDate.of(2026, 2, 7));
        mockOrderTran.setCustomerId(1);
        mockOrderTran.setTotalPrice(7200);
        mockOrderTran.setDeliveryPrice(800);
        mockOrderTran.setDeliveryAddress("東京都渋谷区1-2-3");
        mockOrderTran.setSettlementType(1);
        
        OrderDetailPK pk = new OrderDetailPK(1, 1);
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setId(pk);
        orderDetail.setOrderTran(mockOrderTran);
        orderDetail.setBookId(1);
        orderDetail.setBookName("Java完全理解");
        orderDetail.setPublisherName("技術評論社");
        orderDetail.setPrice(3200);
        orderDetail.setCount(2);
        
        mockOrderTran.setOrderDetails(List.of(orderDetail));
    }
    
    @Test
    @DisplayName("正常系: 注文作成が成功する（認証済み）")
    void testCreateOrder_Success() {
        // Given: ユーザーがログイン済み、OrderServiceが正常なOrderTranを返す
        when(authenInfo.getCustomerId()).thenReturn(1);
        when(orderService.createOrder(any(OrderRequest.class), eq(1))).thenReturn(mockOrderTran);
        
        // When: createOrder()を呼び出す
        Response response = orderResource.createOrder(orderRequest);
        
        // Then: 201 Createdが返される
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        
        // レスポンスボディがOrderResponseである
        OrderResponse orderResponse = (OrderResponse) response.getEntity();
        assertNotNull(orderResponse);
        assertEquals(1, orderResponse.orderTranId());
        assertEquals(LocalDate.of(2026, 2, 7), orderResponse.orderDate());
        assertEquals(1, orderResponse.customerId());
        assertEquals(7200, orderResponse.totalPrice());
        assertEquals(800, orderResponse.deliveryPrice());
        assertEquals("東京都渋谷区1-2-3", orderResponse.deliveryAddress());
        assertEquals(1, orderResponse.settlementType());
        assertEquals(1, orderResponse.orderDetails().size());
        
        // OrderService.createOrder()が1回呼び出される
        verify(orderService, times(1)).createOrder(any(OrderRequest.class), eq(1));
    }
    
    @Test
    @DisplayName("異常系: 未認証ユーザーの注文試行 → 401 Unauthorized")
    void testCreateOrder_Unauthorized() {
        // Given: ユーザーがログインしていない（customerIdがnull）
        when(authenInfo.getCustomerId()).thenReturn(null);
        
        // When: createOrder()を呼び出す
        Response response = orderResource.createOrder(orderRequest);
        
        // Then: 401 Unauthorizedが返される
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        
        // レスポンスボディがErrorResponseである
        ErrorResponse error = (ErrorResponse) response.getEntity();
        assertNotNull(error);
        assertEquals(401, error.status());
        assertEquals("Unauthorized", error.error());
        assertEquals("認証が必要です", error.message());
        
        // OrderService.createOrder()は呼び出されない
        verify(orderService, never()).createOrder(any(OrderRequest.class), anyInt());
    }
    
    @Test
    @DisplayName("異常系: 在庫不足エラー → 409 Conflict")
    void testCreateOrder_OutOfStock() {
        // Given: ユーザーがログイン済み、OrderServiceがOutOfStockExceptionをスロー
        when(authenInfo.getCustomerId()).thenReturn(1);
        when(orderService.createOrder(any(OrderRequest.class), eq(1)))
                .thenThrow(new OutOfStockException("在庫不足"));
        
        // When & Then: OutOfStockExceptionがスローされる
        assertThrows(OutOfStockException.class, () -> {
            orderResource.createOrder(orderRequest);
        });
        
        // OrderService.createOrder()が1回呼び出される
        verify(orderService, times(1)).createOrder(any(OrderRequest.class), eq(1));
    }
    
    @Test
    @DisplayName("異常系: 楽観的ロック競合エラー → 409 Conflict")
    void testCreateOrder_OptimisticLockException() {
        // Given: ユーザーがログイン済み、OrderServiceがOptimisticLockExceptionをスロー
        when(authenInfo.getCustomerId()).thenReturn(1);
        when(orderService.createOrder(any(OrderRequest.class), eq(1)))
                .thenThrow(new OptimisticLockException("データが他のユーザーによって更新されました"));
        
        // When & Then: OptimisticLockExceptionがスローされる
        assertThrows(OptimisticLockException.class, () -> {
            orderResource.createOrder(orderRequest);
        });
        
        // OrderService.createOrder()が1回呼び出される
        verify(orderService, times(1)).createOrder(any(OrderRequest.class), eq(1));
    }
    
    @Test
    @DisplayName("正常系: 注文履歴を取得する（認証済み）")
    void testGetOrderHistory_Success() {
        // Given: ユーザーがログイン済み、OrderServiceが注文リストを返す
        when(authenInfo.getCustomerId()).thenReturn(1);
        
        OrderTran orderTran1 = new OrderTran();
        orderTran1.setOrderTranId(1);
        orderTran1.setOrderDate(LocalDate.of(2026, 2, 6));
        orderTran1.setCustomerId(1);
        orderTran1.setTotalPrice(7200);
        orderTran1.setDeliveryPrice(800);
        orderTran1.setDeliveryAddress("東京都渋谷区1-2-3");
        orderTran1.setSettlementType(1);
        orderTran1.setOrderDetails(List.of());
        
        OrderTran orderTran2 = new OrderTran();
        orderTran2.setOrderTranId(2);
        orderTran2.setOrderDate(LocalDate.of(2026, 2, 5));
        orderTran2.setCustomerId(1);
        orderTran2.setTotalPrice(5400);
        orderTran2.setDeliveryPrice(0);
        orderTran2.setDeliveryAddress("東京都新宿区4-5-6");
        orderTran2.setSettlementType(2);
        orderTran2.setOrderDetails(List.of());
        
        when(orderService.getOrderHistory(1)).thenReturn(List.of(orderTran1, orderTran2));
        
        // When: getOrderHistory()を呼び出す
        Response response = orderResource.getOrderHistory();
        
        // Then: 200 OKが返される
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        // レスポンスボディがList<OrderResponse>である
        @SuppressWarnings("unchecked")
        List<OrderResponse> orderResponses = (List<OrderResponse>) response.getEntity();
        assertNotNull(orderResponses);
        assertEquals(2, orderResponses.size());
        assertEquals(1, orderResponses.get(0).orderTranId());
        assertEquals(2, orderResponses.get(1).orderTranId());
        
        // OrderService.getOrderHistory()が1回呼び出される
        verify(orderService, times(1)).getOrderHistory(1);
    }
    
    @Test
    @DisplayName("異常系: 未認証ユーザーの注文履歴取得試行 → 401 Unauthorized")
    void testGetOrderHistory_Unauthorized() {
        // Given: ユーザーがログインしていない（customerIdがnull）
        when(authenInfo.getCustomerId()).thenReturn(null);
        
        // When: getOrderHistory()を呼び出す
        Response response = orderResource.getOrderHistory();
        
        // Then: 401 Unauthorizedが返される
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        
        // レスポンスボディがErrorResponseである
        ErrorResponse error = (ErrorResponse) response.getEntity();
        assertNotNull(error);
        assertEquals(401, error.status());
        assertEquals("Unauthorized", error.error());
        
        // OrderService.getOrderHistory()は呼び出されない
        verify(orderService, never()).getOrderHistory(anyInt());
    }
    
    @Test
    @DisplayName("正常系: 注文詳細を取得する（認証不要）")
    void testGetOrderById_Success() {
        // Given: OrderServiceが注文情報を返す
        when(orderService.getOrderById(1)).thenReturn(mockOrderTran);
        
        // When: getOrderById()を呼び出す
        Response response = orderResource.getOrderById(1);
        
        // Then: 200 OKが返される
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        // レスポンスボディがOrderResponseである
        OrderResponse orderResponse = (OrderResponse) response.getEntity();
        assertNotNull(orderResponse);
        assertEquals(1, orderResponse.orderTranId());
        assertEquals(1, orderResponse.orderDetails().size());
        
        // OrderService.getOrderById()が1回呼び出される
        verify(orderService, times(1)).getOrderById(1);
    }
    
    @Test
    @DisplayName("異常系: 存在しない注文IDを指定 → 404 Not Found")
    void testGetOrderById_NotFound() {
        // Given: OrderServiceがnullを返す
        when(orderService.getOrderById(999)).thenReturn(null);
        
        // When: getOrderById()を呼び出す
        Response response = orderResource.getOrderById(999);
        
        // Then: 404 Not Foundが返される
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        
        // レスポンスボディがErrorResponseである
        ErrorResponse error = (ErrorResponse) response.getEntity();
        assertNotNull(error);
        assertEquals(404, error.status());
        assertEquals("Not Found", error.error());
        assertEquals("注文が見つかりません", error.message());
        
        // OrderService.getOrderById()が1回呼び出される
        verify(orderService, times(1)).getOrderById(999);
    }
    
    @Test
    @DisplayName("正常系: 注文明細を取得する（認証不要）")
    void testGetOrderDetailById_Success() {
        // Given: OrderServiceが注文情報を返す
        when(orderService.getOrderById(1)).thenReturn(mockOrderTran);
        
        // When: getOrderDetailById()を呼び出す
        Response response = orderResource.getOrderDetailById(1, 1);
        
        // Then: 200 OKが返される
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        // OrderService.getOrderById()が1回呼び出される
        verify(orderService, times(1)).getOrderById(1);
    }
    
    @Test
    @DisplayName("異常系: 存在しない注文IDを指定（注文明細取得） → 404 Not Found")
    void testGetOrderDetailById_OrderNotFound() {
        // Given: OrderServiceがnullを返す
        when(orderService.getOrderById(999)).thenReturn(null);
        
        // When: getOrderDetailById()を呼び出す
        Response response = orderResource.getOrderDetailById(999, 1);
        
        // Then: 404 Not Foundが返される
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        
        // レスポンスボディがErrorResponseである
        ErrorResponse error = (ErrorResponse) response.getEntity();
        assertNotNull(error);
        assertEquals(404, error.status());
        assertEquals("Not Found", error.error());
        assertEquals("注文が見つかりません", error.message());
        
        // OrderService.getOrderById()が1回呼び出される
        verify(orderService, times(1)).getOrderById(999);
    }
    
    @Test
    @DisplayName("異常系: 存在しない注文明細IDを指定 → 404 Not Found")
    void testGetOrderDetailById_DetailNotFound() {
        // Given: OrderServiceが注文情報を返すが、指定された注文明細IDが存在しない
        when(orderService.getOrderById(1)).thenReturn(mockOrderTran);
        
        // When: getOrderDetailById()を呼び出す（存在しない注文明細ID）
        Response response = orderResource.getOrderDetailById(1, 999);
        
        // Then: 404 Not Foundが返される
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        
        // レスポンスボディがErrorResponseである
        ErrorResponse error = (ErrorResponse) response.getEntity();
        assertNotNull(error);
        assertEquals(404, error.status());
        assertEquals("Not Found", error.error());
        assertEquals("注文明細が見つかりません", error.message());
        
        // OrderService.getOrderById()が1回呼び出される
        verify(orderService, times(1)).getOrderById(1);
    }
}
