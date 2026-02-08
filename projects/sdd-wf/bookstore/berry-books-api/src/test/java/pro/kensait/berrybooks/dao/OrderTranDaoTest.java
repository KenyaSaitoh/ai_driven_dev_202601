package pro.kensait.berrybooks.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.berrybooks.entity.OrderTran;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrderTranDaoの単体テスト
 */
@ExtendWith(MockitoExtension.class)
class OrderTranDaoTest {
    
    @Mock
    private EntityManager em;
    
    @InjectMocks
    private OrderTranDao orderTranDao;
    
    private OrderTran testOrderTran;
    
    @BeforeEach
    void setUp() {
        // Given: テストデータを準備
        testOrderTran = new OrderTran();
        testOrderTran.setOrderDate(LocalDate.of(2026, 2, 7));
        testOrderTran.setCustomerId(1);
        testOrderTran.setTotalPrice(5400);
        testOrderTran.setDeliveryPrice(400);
        testOrderTran.setDeliveryAddress("東京都渋谷区1-1-1");
        testOrderTran.setSettlementType(2);
    }
    
    @Test
    void testInsert_Success() {
        // Given: EntityManagerがモック化されている
        
        // When: insert()を呼び出す
        OrderTran result = orderTranDao.insert(testOrderTran);
        
        // Then: persistが呼び出され、OrderTranが返される
        verify(em).persist(testOrderTran);
        assertEquals(testOrderTran, result);
    }
    
    @Test
    void testFindById_Found() {
        // Given: EntityManagerがモック化されている
        testOrderTran.setOrderTranId(1);
        when(em.find(OrderTran.class, 1)).thenReturn(testOrderTran);
        
        // When: findById()を呼び出す
        OrderTran result = orderTranDao.findById(1);
        
        // Then: 注文トランザクションが返される
        assertNotNull(result);
        assertEquals(1, result.getOrderTranId());
        assertEquals(1, result.getCustomerId());
        verify(em).find(OrderTran.class, 1);
    }
    
    @Test
    void testFindById_NotFound() {
        // Given: 存在しない注文ID
        when(em.find(OrderTran.class, 999)).thenReturn(null);
        
        // When: findById()を呼び出す
        OrderTran result = orderTranDao.findById(999);
        
        // Then: nullが返される
        assertNull(result);
        verify(em).find(OrderTran.class, 999);
    }
    
    @Test
    void testFindByCustomerId_Found() {
        // Given: 顧客ID=1の注文が複数存在する
        OrderTran order1 = new OrderTran();
        order1.setOrderTranId(1);
        order1.setCustomerId(1);
        order1.setOrderDate(LocalDate.of(2026, 2, 7));
        
        OrderTran order2 = new OrderTran();
        order2.setOrderTranId(2);
        order2.setCustomerId(1);
        order2.setOrderDate(LocalDate.of(2026, 2, 6));
        
        List<OrderTran> mockResults = Arrays.asList(order1, order2);
        
        TypedQuery<OrderTran> mockQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(OrderTran.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter("customerId", 1)).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(mockResults);
        
        // When: findByCustomerId()を呼び出す
        List<OrderTran> results = orderTranDao.findByCustomerId(1);
        
        // Then: 注文リストが注文日の降順で返される
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getOrderTranId());
        assertEquals(2, results.get(1).getOrderTranId());
        
        // And: 最初の要素が最新の注文である
        assertTrue(results.get(0).getOrderDate().isAfter(results.get(1).getOrderDate()));
        
        verify(em).createQuery(anyString(), eq(OrderTran.class));
        verify(mockQuery).setParameter("customerId", 1);
        verify(mockQuery).getResultList();
    }
    
    @Test
    void testFindByCustomerId_NotFound() {
        // Given: 存在しない顧客ID
        TypedQuery<OrderTran> mockQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(OrderTran.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter("customerId", 999)).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(Arrays.asList());
        
        // When: findByCustomerId()を呼び出す
        List<OrderTran> results = orderTranDao.findByCustomerId(999);
        
        // Then: 空リストが返される
        assertNotNull(results);
        assertTrue(results.isEmpty());
        
        verify(em).createQuery(anyString(), eq(OrderTran.class));
        verify(mockQuery).setParameter("customerId", 999);
        verify(mockQuery).getResultList();
    }
}