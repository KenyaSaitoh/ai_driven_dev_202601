package pro.kensait.berrybooks.cucumber.steps;

import io.cucumber.java.ja.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pro.kensait.berrybooks.dao.OrderTranDao;
import pro.kensait.berrybooks.entity.OrderTran;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrderTranDao Cucumber ステップ定義
 */
public class OrderTranDaoSteps {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<OrderTran> query;

    private OrderTranDao orderTranDao;
    private List<OrderTran> result;

    @前提("EntityManagerがモック化されている")
    public void entityManagerがモック化されている() {
        MockitoAnnotations.openMocks(this);
        orderTranDao = new OrderTranDao();
        // Use reflection to inject mock EntityManager
        try {
            java.lang.reflect.Field field = OrderTranDao.class.getDeclaredField("em");
            field.setAccessible(true);
            field.set(orderTranDao, entityManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @かつ("モック設定: createQuery\\(\\)が注文リストを返す")
    public void モック設定_createQueryが注文リストを返す() {
        when(entityManager.createQuery(anyString(), eq(OrderTran.class))).thenReturn(query);
    }

    @かつ("顧客ID={int}の注文が{int}件存在する")
    public void 顧客IDの注文が件存在する(int customerId, int count) {
        OrderTran order1 = new OrderTran(LocalDate.of(2026, 1, 1), customerId, 5000, 500, "東京都", 1);
        order1.setOrderTranId(1);
        
        OrderTran order2 = new OrderTran(LocalDate.of(2026, 1, 2), customerId, 6000, 500, "東京都", 1);
        order2.setOrderTranId(2);
        
        List<OrderTran> orders = Arrays.asList(order2, order1); // 降順
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(orders);
    }

    @もし("OrderTranDao.findByCustomerId\\({int}\\)を呼び出す")
    public void orderTranDaoFindByCustomerIdを呼び出す(int customerId) {
        result = orderTranDao.findByCustomerId(customerId);
    }

    @ならば("{int}件の注文が返される")
    public void 件の注文が返される(int count) {
        assertEquals(count, result.size());
    }

    @かつ("注文日の降順でソートされている")
    public void 注文日の降順でソートされている() {
        assertTrue(result.get(0).getOrderDate().isAfter(result.get(1).getOrderDate()));
    }

    @かつ("モック設定: createQuery\\(\\)が空リストを返す")
    public void モック設定_createQueryが空リストを返す() {
        when(entityManager.createQuery(anyString(), eq(OrderTran.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());
    }

    @ならば("空のリストが返される")
    public void 空のリストが返される() {
        assertTrue(result.isEmpty());
    }

    @かつ("例外はスローされない")
    public void 例外はスローされない() {
        // No exception thrown during execution
        assertNotNull(result);
    }
}
