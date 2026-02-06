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

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrderTranDao の単体テスト
 * 
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class OrderTranDaoTest {
    
    @Mock
    private EntityManager em;
    
    @Mock
    private TypedQuery<OrderTran> query;
    
    @InjectMocks
    private OrderTranDao orderTranDao;
    
    @BeforeEach
    void setUp() throws Exception {
        // EntityManagerをDaoにインジェクション
        Field emField = OrderTranDao.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(orderTranDao, em);
    }
    
    /**
     * Scenario: 注文トランザクションを登録する
     * 
     * Given: OrderTranが存在する
     * When: insert()を呼び出す
     * Then: EntityManager.persist()が呼び出される
     *       EntityManager.flush()が呼び出される
     *       登録されたOrderTranが返される
     */
    @Test
    void testInsert_Success() {
        // Given
        OrderTran orderTran = new OrderTran();
        orderTran.setCustomerId(1);
        orderTran.setTotalPrice(5000);
        orderTran.setDeliveryPrice(500);
        orderTran.setOrderDate(LocalDate.now());
        
        // When
        OrderTran result = orderTranDao.insert(orderTran);
        
        // Then
        assertNotNull(result);
        verify(em, times(1)).persist(orderTran);
        verify(em, times(1)).flush();
    }
    
    /**
     * Scenario: 注文IDで注文トランザクションを取得する
     * 
     * Given: EntityManagerがモック化されている
     *        モック設定: createQuery()が注文を返す
     *        orderTranId=1の注文が存在する
     * When: findById(1)を呼び出す
     * Then: Optional<OrderTran>が返される
     *       注文が取得される
     */
    @Test
    void testFindById_Found() {
        // Given
        Integer orderTranId = 1;
        OrderTran orderTran = new OrderTran();
        orderTran.setOrderTranId(orderTranId);
        orderTran.setCustomerId(1);
        
        when(em.createQuery(anyString(), eq(OrderTran.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(orderTran));
        
        // When
        Optional<OrderTran> result = orderTranDao.findById(orderTranId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(orderTranId, result.get().getOrderTranId());
        verify(em, times(1)).createQuery(anyString(), eq(OrderTran.class));
    }
    
    /**
     * Scenario: 存在しない注文IDで検索
     * 
     * Given: EntityManagerがモック化されている
     *        モック設定: createQuery()が空リストを返す
     * When: findById(999)を呼び出す
     * Then: Optional.empty()が返される
     */
    @Test
    void testFindById_NotFound() {
        // Given
        Integer orderTranId = 999;
        
        when(em.createQuery(anyString(), eq(OrderTran.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());
        
        // When
        Optional<OrderTran> result = orderTranDao.findById(orderTranId);
        
        // Then
        assertFalse(result.isPresent());
    }
    
    /**
     * Scenario: 顧客IDで注文履歴を取得
     * 
     * Given: EntityManagerがモック化されている
     *        モック設定: createQuery()が注文リストを返す
     *        顧客ID=1の注文が2件存在する
     * When: findByCustomerId(1)を呼び出す
     * Then: 2件の注文が返される
     *       注文日の降順でソートされている
     */
    @Test
    void testFindByCustomerId_Success() {
        // Given
        Integer customerId = 1;
        
        OrderTran order1 = new OrderTran();
        order1.setOrderTranId(1);
        order1.setCustomerId(customerId);
        order1.setOrderDate(LocalDate.of(2026, 1, 1));
        
        OrderTran order2 = new OrderTran();
        order2.setOrderTranId(2);
        order2.setCustomerId(customerId);
        order2.setOrderDate(LocalDate.of(2026, 1, 2));
        
        List<OrderTran> orders = Arrays.asList(order2, order1); // 降順
        
        when(em.createQuery(anyString(), eq(OrderTran.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(orders);
        
        // When
        List<OrderTran> result = orderTranDao.findByCustomerId(customerId);
        
        // Then
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getOrderTranId()); // 新しい注文が先
        assertEquals(1, result.get(1).getOrderTranId());
    }
    
    /**
     * Scenario: 注文が存在しない顧客IDで検索
     * 
     * Given: EntityManagerがモック化されている
     *        モック設定: createQuery()が空リストを返す
     * When: findByCustomerId(999)を呼び出す
     * Then: 空のリストが返される
     *       例外はスローされない
     */
    @Test
    void testFindByCustomerId_Empty() {
        // Given
        Integer customerId = 999;
        
        when(em.createQuery(anyString(), eq(OrderTran.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());
        
        // When
        List<OrderTran> result = orderTranDao.findByCustomerId(customerId);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
