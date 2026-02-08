package pro.kensait.berrybooks.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.berrybooks.entity.OrderDetail;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrderDetailDaoの単体テスト
 */
@ExtendWith(MockitoExtension.class)
class OrderDetailDaoTest {
    
    @Mock
    private EntityManager em;
    
    @InjectMocks
    private OrderDetailDao orderDetailDao;
    
    private OrderDetail testOrderDetail;
    
    @BeforeEach
    void setUp() {
        // Given: テストデータを準備（スナップショットパターン）
        testOrderDetail = new OrderDetail();
        testOrderDetail.setOrderTranId(1);
        testOrderDetail.setOrderDetailId(1);
        testOrderDetail.setBookId(10);
        testOrderDetail.setBookName("Java入門");
        testOrderDetail.setPublisherName("技術評論社");
        testOrderDetail.setPrice(3000);
        testOrderDetail.setCount(2);
    }
    
    @Test
    void testInsert_Success() {
        // Given: EntityManagerがモック化されている
        
        // When: insert()を呼び出す
        OrderDetail result = orderDetailDao.insert(testOrderDetail);
        
        // Then: persistが呼び出され、スナップショット値が保持される
        verify(em).persist(testOrderDetail);
        assertEquals(testOrderDetail, result);
        assertEquals("Java入門", result.getBookName());
        assertEquals("技術評論社", result.getPublisherName());
        assertEquals(3000, result.getPrice());
    }
    
    @Test
    void testFindByOrderTranId_Found() {
        // Given: 注文ID=1の明細が複数存在する
        OrderDetail detail1 = new OrderDetail();
        detail1.setOrderTranId(1);
        detail1.setOrderDetailId(1);
        detail1.setBookId(10);
        detail1.setBookName("Java入門");
        detail1.setPublisherName("技術評論社");
        detail1.setPrice(3000);
        detail1.setCount(2);
        
        OrderDetail detail2 = new OrderDetail();
        detail2.setOrderTranId(1);
        detail2.setOrderDetailId(2);
        detail2.setBookId(20);
        detail2.setBookName("Spring Boot実践");
        detail2.setPublisherName("翔泳社");
        detail2.setPrice(3500);
        detail2.setCount(1);
        
        List<OrderDetail> mockResults = Arrays.asList(detail1, detail2);
        
        TypedQuery<OrderDetail> mockQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(OrderDetail.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter("orderTranId", 1)).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(mockResults);
        
        // When: findByOrderTranId()を呼び出す
        List<OrderDetail> results = orderDetailDao.findByOrderTranId(1);
        
        // Then: 注文明細リストが返される
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getOrderDetailId());
        assertEquals(2, results.get(1).getOrderDetailId());
        
        verify(em).createQuery(anyString(), eq(OrderDetail.class));
        verify(mockQuery).setParameter("orderTranId", 1);
        verify(mockQuery).getResultList();
    }
    
    @Test
    void testFindByOrderTranId_NotFound() {
        // Given: 存在しない注文ID
        TypedQuery<OrderDetail> mockQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(OrderDetail.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter("orderTranId", 999)).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(Arrays.asList());
        
        // When: findByOrderTranId()を呼び出す
        List<OrderDetail> results = orderDetailDao.findByOrderTranId(999);
        
        // Then: 空リストが返される
        assertNotNull(results);
        assertTrue(results.isEmpty());
        
        verify(em).createQuery(anyString(), eq(OrderDetail.class));
        verify(mockQuery).setParameter("orderTranId", 999);
        verify(mockQuery).getResultList();
    }
    
    @Test
    void testInsert_SnapshotPreserved() {
        // Given: スナップショット値が設定されたOrderDetail
        
        // When: insert()を呼び出す
        OrderDetail result = orderDetailDao.insert(testOrderDetail);
        
        // Then: スナップショット値が保持される
        assertEquals("Java入門", result.getBookName());
        assertEquals("技術評論社", result.getPublisherName());
        assertEquals(3000, result.getPrice());
        verify(em).persist(testOrderDetail);
    }
}