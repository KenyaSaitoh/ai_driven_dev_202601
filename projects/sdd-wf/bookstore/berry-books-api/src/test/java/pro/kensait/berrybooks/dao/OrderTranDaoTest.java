package pro.kensait.berrybooks.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.berrybooks.entity.OrderTran;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderTranDao単体テスト")
class OrderTranDaoTest {
    
    @Mock
    private EntityManager em;
    
    @Mock
    private TypedQuery<OrderTran> query;
    
    @InjectMocks
    private OrderTranDao orderTranDao;
    
    private OrderTran testOrderTran1;
    private OrderTran testOrderTran2;
    
    @BeforeEach
    void setUp() {
        // Given: テストデータの準備
        testOrderTran1 = new OrderTran();
        testOrderTran1.setOrderTranId(1);
        testOrderTran1.setOrderDate(LocalDate.of(2026, 1, 1));
        testOrderTran1.setCustomerId(1);
        testOrderTran1.setTotalPrice(5000);
        testOrderTran1.setDeliveryPrice(500);
        testOrderTran1.setDeliveryAddress("東京都");
        testOrderTran1.setSettlementType(1);
        
        testOrderTran2 = new OrderTran();
        testOrderTran2.setOrderTranId(2);
        testOrderTran2.setOrderDate(LocalDate.of(2026, 1, 2));
        testOrderTran2.setCustomerId(1);
        testOrderTran2.setTotalPrice(8000);
        testOrderTran2.setDeliveryPrice(500);
        testOrderTran2.setDeliveryAddress("大阪府");
        testOrderTran2.setSettlementType(2);
    }
    
    @Test
    @DisplayName("顧客IDで注文履歴を取得（正常系）")
    void testFindByCustomerId_Success() {
        // Given: モック設定
        List<OrderTran> expectedOrders = new ArrayList<>();
        expectedOrders.add(testOrderTran2);  // 新しい注文が先
        expectedOrders.add(testOrderTran1);
        
        when(em.createQuery(anyString(), eq(OrderTran.class))).thenReturn(query);
        when(query.setParameter(eq("customerId"), eq(1))).thenReturn(query);
        when(query.getResultList()).thenReturn(expectedOrders);
        
        // When: メソッド呼び出し
        List<OrderTran> result = orderTranDao.findByCustomerId(1);
        
        // Then: 検証
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getOrderTranId());  // 降順
        assertEquals(1, result.get(1).getOrderTranId());
        
        verify(em).createQuery(anyString(), eq(OrderTran.class));
        verify(query).setParameter("customerId", 1);
        verify(query).getResultList();
    }
    
    @Test
    @DisplayName("注文が存在しない顧客IDで検索（境界値）")
    void testFindByCustomerId_NoOrders() {
        // Given: モック設定（空リスト）
        when(em.createQuery(anyString(), eq(OrderTran.class))).thenReturn(query);
        when(query.setParameter(eq("customerId"), eq(999))).thenReturn(query);
        when(query.getResultList()).thenReturn(new ArrayList<>());
        
        // When: メソッド呼び出し
        List<OrderTran> result = orderTranDao.findByCustomerId(999);
        
        // Then: 検証
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(em).createQuery(anyString(), eq(OrderTran.class));
        verify(query).setParameter("customerId", 999);
        verify(query).getResultList();
    }
    
    @Test
    @DisplayName("注文IDで注文トランザクションを取得（正常系）")
    void testFindById_Success() {
        // Given: モック設定
        List<OrderTran> resultList = new ArrayList<>();
        resultList.add(testOrderTran1);
        
        when(em.createQuery(anyString(), eq(OrderTran.class))).thenReturn(query);
        when(query.setParameter(eq("orderTranId"), eq(1))).thenReturn(query);
        when(query.getResultList()).thenReturn(resultList);
        
        // When: メソッド呼び出し
        Optional<OrderTran> result = orderTranDao.findById(1);
        
        // Then: 検証
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getOrderTranId());
        assertEquals(5000, result.get().getTotalPrice());
        
        verify(em).createQuery(anyString(), eq(OrderTran.class));
        verify(query).setParameter("orderTranId", 1);
        verify(query).getResultList();
    }
    
    @Test
    @DisplayName("存在しない注文IDで検索（境界値）")
    void testFindById_NotFound() {
        // Given: モック設定（空リスト）
        when(em.createQuery(anyString(), eq(OrderTran.class))).thenReturn(query);
        when(query.setParameter(eq("orderTranId"), eq(999))).thenReturn(query);
        when(query.getResultList()).thenReturn(new ArrayList<>());
        
        // When: メソッド呼び出し
        Optional<OrderTran> result = orderTranDao.findById(999);
        
        // Then: 検証
        assertFalse(result.isPresent());
        
        verify(em).createQuery(anyString(), eq(OrderTran.class));
        verify(query).setParameter("orderTranId", 999);
        verify(query).getResultList();
    }
    
    @Test
    @DisplayName("注文トランザクションを登録（正常系）")
    void testInsert_Success() {
        // Given: モック設定
        doNothing().when(em).persist(any(OrderTran.class));
        doNothing().when(em).flush();
        
        // When: メソッド呼び出し
        OrderTran result = orderTranDao.insert(testOrderTran1);
        
        // Then: 検証
        assertNotNull(result);
        assertEquals(testOrderTran1, result);
        
        verify(em).persist(testOrderTran1);
        verify(em).flush();
    }
}
