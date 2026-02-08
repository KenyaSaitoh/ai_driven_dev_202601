package pro.kensait.berrybooks.api;

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
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.security.AuthenticatedUser;
import pro.kensait.berrybooks.service.OrderService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OrderResourceの単体テスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderResource 単体テスト")
class OrderResourceTest {
    
    @Mock
    private OrderService orderService;
    
    @Mock
    private AuthenticatedUser authenticatedUser;
    
    @InjectMocks
    private OrderResource orderResource;
    
    private OrderRequest orderRequest;
    
    @BeforeEach
    void setUp() {
        // Given: テストデータの準備
        CartItemRequest item = new CartItemRequest(
            10, "Java入門", "技術評論社", 3000, 2, 1L
        );
        orderRequest = new OrderRequest(
            Arrays.asList(item),
            "東京都渋谷区1-1-1",
            2
        );
    }
    
    @Test
    @DisplayName("注文作成が成功")
    void testCreateOrder_Success() {
        // Given: モック設定
        when(authenticatedUser.getCustomerId()).thenReturn(1);
        
        OrderTran orderTran = new OrderTran();
        orderTran.setOrderTranId(1);
        orderTran.setOrderDate(LocalDate.now());
        orderTran.setCustomerId(1);
        orderTran.setTotalPrice(6400);
        orderTran.setDeliveryPrice(400);
        orderTran.setDeliveryAddress("東京都渋谷区1-1-1");
        orderTran.setSettlementType(2);
        orderTran.setOrderDetails(new ArrayList<>());
        
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(orderTran);
        
        // When: createOrder()を呼び出す
        Response response = orderResource.createOrder(orderRequest);
        
        // Then: 201 Createdが返される
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof OrderResponse);
        verify(orderService, times(1)).createOrder(any(OrderRequest.class));
    }
    
    @Test
    @DisplayName("在庫不足時に400 Bad Requestを返す")
    void testCreateOrder_OutOfStock() {
        // Given: モック設定
        when(authenticatedUser.getCustomerId()).thenReturn(1);
        when(orderService.createOrder(any(OrderRequest.class)))
            .thenThrow(new RuntimeException("在庫不足: Java入門"));
        
        // When: createOrder()を呼び出す
        Response response = orderResource.createOrder(orderRequest);
        
        // Then: 400 Bad Requestが返される
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }
    
    @Test
    @DisplayName("注文履歴を取得")
    void testGetOrderHistory() {
        // Given: モック設定
        when(authenticatedUser.getCustomerId()).thenReturn(1);
        
        OrderTran order1 = new OrderTran();
        order1.setOrderTranId(1);
        order1.setOrderDetails(new ArrayList<>());
        
        OrderTran order2 = new OrderTran();
        order2.setOrderTranId(2);
        order2.setOrderDetails(new ArrayList<>());
        
        List<OrderTran> orders = Arrays.asList(order1, order2);
        when(orderService.getOrderHistory(1)).thenReturn(orders);
        
        // When: getOrderHistory()を呼び出す
        Response response = orderResource.getOrderHistory();
        
        // Then: 200 OKが返される
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof List);
        verify(orderService, times(1)).getOrderHistory(1);
    }
    
    @Test
    @DisplayName("注文詳細を取得")
    void testGetOrderDetail_Found() {
        // Given: モック設定
        OrderTran orderTran = new OrderTran();
        orderTran.setOrderTranId(1);
        orderTran.setOrderDetails(new ArrayList<>());
        
        when(orderService.getOrderDetail(1)).thenReturn(orderTran);
        
        // When: getOrderDetail()を呼び出す
        Response response = orderResource.getOrderDetail(1);
        
        // Then: 200 OKが返される
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof OrderResponse);
        verify(orderService, times(1)).getOrderDetail(1);
    }
    
    @Test
    @DisplayName("注文が見つからない場合に404 Not Foundを返す")
    void testGetOrderDetail_NotFound() {
        // Given: モック設定
        when(orderService.getOrderDetail(999)).thenReturn(null);
        
        // When: getOrderDetail()を呼び出す
        Response response = orderResource.getOrderDetail(999);
        
        // Then: 404 Not Foundが返される
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(orderService, times(1)).getOrderDetail(999);
    }
}