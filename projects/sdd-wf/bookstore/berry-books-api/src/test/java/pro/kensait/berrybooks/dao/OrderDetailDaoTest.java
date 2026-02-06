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
import pro.kensait.berrybooks.entity.OrderDetailPK;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrderDetailDao の単体テスト
 * 
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class OrderDetailDaoTest {
    
    @Mock
    private EntityManager em;
    
    @Mock
    private TypedQuery<OrderDetail> query;
    
    @InjectMocks
    private OrderDetailDao orderDetailDao;
    
    @BeforeEach
    void setUp() throws Exception {
        // EntityManagerをDaoにインジェクション
        Field emField = OrderDetailDao.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(orderDetailDao, em);
    }
    
    /**
     * Scenario: 注文明細を登録する
     * 
     * Given: OrderDetailが存在する
     * When: insert()を呼び出す
     * Then: EntityManager.persist()が呼び出される
     *       登録されたOrderDetailが返される
     */
    @Test
    void testInsert_Success() {
        // Given
        OrderDetailPK id = new OrderDetailPK(1, 1);
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setId(id);
        orderDetail.setBookId(1);
        orderDetail.setBookName("テスト書籍");
        orderDetail.setPrice(3000);
        orderDetail.setCount(2);
        
        // When
        OrderDetail result = orderDetailDao.insert(orderDetail);
        
        // Then
        assertNotNull(result);
        verify(em, times(1)).persist(orderDetail);
    }
    
    /**
     * Scenario: 注文IDで注文明細一覧を取得
     * 
     * Given: EntityManagerがモック化されている
     *        モック設定: createQuery()が注文明細リストを返す
     *        注文ID=1の注文明細が3件存在する
     * When: findByOrderTranId(1)を呼び出す
     * Then: 3件の注文明細が返される
     *       注文明細IDの昇順でソートされている
     */
    @Test
    void testFindByOrderTranId_Success() {
        // Given
        Integer orderTranId = 1;
        
        OrderDetail detail1 = new OrderDetail();
        detail1.setId(new OrderDetailPK(orderTranId, 1));
        detail1.setBookId(1);
        detail1.setBookName("書籍1");
        
        OrderDetail detail2 = new OrderDetail();
        detail2.setId(new OrderDetailPK(orderTranId, 2));
        detail2.setBookId(2);
        detail2.setBookName("書籍2");
        
        OrderDetail detail3 = new OrderDetail();
        detail3.setId(new OrderDetailPK(orderTranId, 3));
        detail3.setBookId(3);
        detail3.setBookName("書籍3");
        
        List<OrderDetail> details = Arrays.asList(detail1, detail2, detail3);
        
        when(em.createQuery(anyString(), eq(OrderDetail.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(details);
        
        // When
        List<OrderDetail> result = orderDetailDao.findByOrderTranId(orderTranId);
        
        // Then
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getId().getOrderDetailId());
        assertEquals(2, result.get(1).getId().getOrderDetailId());
        assertEquals(3, result.get(2).getId().getOrderDetailId());
    }
    
    /**
     * Scenario: 注文明細が存在しない注文IDで検索
     * 
     * Given: EntityManagerがモック化されている
     *        モック設定: createQuery()が空リストを返す
     * When: findByOrderTranId(999)を呼び出す
     * Then: 空のリストが返される
     *       例外はスローされない
     */
    @Test
    void testFindByOrderTranId_Empty() {
        // Given
        Integer orderTranId = 999;
        
        when(em.createQuery(anyString(), eq(OrderDetail.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());
        
        // When
        List<OrderDetail> result = orderDetailDao.findByOrderTranId(orderTranId);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
