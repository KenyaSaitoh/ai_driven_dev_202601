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
import pro.kensait.berrybooks.entity.OrderDetail;
import pro.kensait.berrybooks.entity.OrderDetailPK;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderDetailDao単体テスト")
class OrderDetailDaoTest {
    
    @Mock
    private EntityManager em;
    
    @Mock
    private TypedQuery<OrderDetail> query;
    
    @InjectMocks
    private OrderDetailDao orderDetailDao;
    
    private OrderDetail testDetail1;
    private OrderDetail testDetail2;
    private OrderDetail testDetail3;
    
    @BeforeEach
    void setUp() {
        // Given: テストデータの準備
        testDetail1 = new OrderDetail();
        testDetail1.setId(new OrderDetailPK(1, 1));
        testDetail1.setBookId(1);
        testDetail1.setBookName("Java入門");
        testDetail1.setPublisherName("技術評論社");
        testDetail1.setPrice(3000);
        testDetail1.setCount(1);
        
        testDetail2 = new OrderDetail();
        testDetail2.setId(new OrderDetailPK(1, 2));
        testDetail2.setBookId(2);
        testDetail2.setBookName("Spring入門");
        testDetail2.setPublisherName("翔泳社");
        testDetail2.setPrice(3500);
        testDetail2.setCount(2);
        
        testDetail3 = new OrderDetail();
        testDetail3.setId(new OrderDetailPK(1, 3));
        testDetail3.setBookId(3);
        testDetail3.setBookName("Kotlin実践");
        testDetail3.setPublisherName("オライリー");
        testDetail3.setPrice(4000);
        testDetail3.setCount(1);
    }
    
    @Test
    @DisplayName("注文IDで注文明細一覧を取得（正常系）")
    void testFindByOrderTranId_Success() {
        // Given: モック設定
        List<OrderDetail> expectedDetails = new ArrayList<>();
        expectedDetails.add(testDetail1);
        expectedDetails.add(testDetail2);
        expectedDetails.add(testDetail3);
        
        when(em.createQuery(anyString(), eq(OrderDetail.class))).thenReturn(query);
        when(query.setParameter(eq("orderTranId"), eq(1))).thenReturn(query);
        when(query.getResultList()).thenReturn(expectedDetails);
        
        // When: メソッド呼び出し
        List<OrderDetail> result = orderDetailDao.findByOrderTranId(1);
        
        // Then: 検証
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getId().getOrderDetailId());
        assertEquals(2, result.get(1).getId().getOrderDetailId());
        assertEquals(3, result.get(2).getId().getOrderDetailId());
        
        verify(em).createQuery(anyString(), eq(OrderDetail.class));
        verify(query).setParameter("orderTranId", 1);
        verify(query).getResultList();
    }
    
    @Test
    @DisplayName("注文明細が存在しない注文IDで検索（境界値）")
    void testFindByOrderTranId_NoDetails() {
        // Given: モック設定（空リスト）
        when(em.createQuery(anyString(), eq(OrderDetail.class))).thenReturn(query);
        when(query.setParameter(eq("orderTranId"), eq(999))).thenReturn(query);
        when(query.getResultList()).thenReturn(new ArrayList<>());
        
        // When: メソッド呼び出し
        List<OrderDetail> result = orderDetailDao.findByOrderTranId(999);
        
        // Then: 検証
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(em).createQuery(anyString(), eq(OrderDetail.class));
        verify(query).setParameter("orderTranId", 999);
        verify(query).getResultList();
    }
    
    @Test
    @DisplayName("注文明細を登録（正常系）")
    void testInsert_Success() {
        // Given: モック設定
        doNothing().when(em).persist(any(OrderDetail.class));
        
        // When: メソッド呼び出し
        OrderDetail result = orderDetailDao.insert(testDetail1);
        
        // Then: 検証
        assertNotNull(result);
        assertEquals(testDetail1, result);
        
        verify(em).persist(testDetail1);
    }
}
