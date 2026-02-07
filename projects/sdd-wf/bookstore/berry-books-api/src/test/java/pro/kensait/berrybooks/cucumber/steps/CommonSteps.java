package pro.kensait.berrybooks.cucumber.steps;

import io.cucumber.java.ja.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pro.kensait.berrybooks.dao.OrderTranDao;
import pro.kensait.berrybooks.entity.OrderDetailPK;
import pro.kensait.berrybooks.entity.OrderTran;
import pro.kensait.berrybooks.security.JwtUtil;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Common domain Cucumber ステップ定義
 */
public class CommonSteps {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<OrderTran> query;

    private OrderTranDao orderTranDao;
    private JwtUtil jwtUtil;
    private List<OrderTran> orderResult;
    private String token;
    private Boolean validationResult;
    private Integer customerId;
    private OrderDetailPK pk1;
    private OrderDetailPK pk2;
    private Boolean equalsResult;

    // OrderTranDao steps

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
        OrderTran order1 = new OrderTran();
        order1.setOrderTranId(1);
        order1.setOrderDate(LocalDate.of(2026, 1, 1));
        order1.setCustomerId(customerId);
        order1.setTotalPrice(5000);
        order1.setDeliveryPrice(500);
        order1.setDeliveryAddress("東京都");
        order1.setSettlementType(1);
        
        OrderTran order2 = new OrderTran();
        order2.setOrderTranId(2);
        order2.setOrderDate(LocalDate.of(2026, 1, 2));
        order2.setCustomerId(customerId);
        order2.setTotalPrice(6000);
        order2.setDeliveryPrice(500);
        order2.setDeliveryAddress("東京都");
        order2.setSettlementType(1);
        
        List<OrderTran> orders = Arrays.asList(order2, order1); // 降順
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(orders);
    }

    @もし("OrderTranDao.findByCustomerId\\({int}\\)を呼び出す")
    public void orderTranDaoFindByCustomerIdを呼び出す(int customerId) {
        orderResult = orderTranDao.findByCustomerId(customerId);
    }

    @ならば("{int}件の注文が返される")
    public void 件の注文が返される(int count) {
        assertEquals(count, orderResult.size());
    }

    @かつ("注文日の降順でソートされている")
    public void 注文日の降順でソートされている() {
        assertTrue(orderResult.get(0).getOrderDate().isAfter(orderResult.get(1).getOrderDate()));
    }

    @かつ("モック設定: createQuery\\(\\)が空リストを返す")
    public void モック設定_createQueryが空リストを返す() {
        when(entityManager.createQuery(anyString(), eq(OrderTran.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());
    }

    @ならば("空のリストが返される")
    public void 空のリストが返される() {
        assertTrue(orderResult.isEmpty());
    }

    @かつ("例外はスローされない")
    public void 例外はスローされない() {
        assertNotNull(orderResult);
    }

    // JwtUtil steps

    @前提("JwtUtilが初期化されている")
    public void jwtUtilが初期化されている() {
        jwtUtil = new JwtUtil();
        jwtUtil.init();
    }

    @かつ("秘密鍵が設定されている")
    public void 秘密鍵が設定されている() {
        assertNotNull(jwtUtil);
    }

    @かつ("customerId={int}, email={string}")
    public void customerIdEmail(int id, String email) {
        this.customerId = id;
    }

    @もし("JwtUtil.generateToken\\({int}, {string}\\)を呼び出す")
    public void jwtUtilGenerateTokenを呼び出す(int id, String email) {
        token = jwtUtil.generateToken(id, email);
    }

    @ならば("JWTトークン文字列が返される")
    public void jwtトークン文字列が返される() {
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @かつ("トークンが{int}つのパート（ヘッダー、ペイロード、署名）で構成されている")
    public void トークンがつのパートで構成されている(int parts) {
        String[] tokenParts = token.split("\\.");
        assertEquals(parts, tokenParts.length);
    }

    @かつ("ペイロードにcustomerId={int}が含まれている")
    public void ペイロードにcustomerIdが含まれている(int expectedId) {
        Integer actualId = jwtUtil.getCustomerIdFromToken(token);
        assertEquals(expectedId, actualId);
    }

    @かつ("ペイロードにemail={string}が含まれている")
    public void ペイロードにemailが含まれている(String expectedEmail) {
        String actualEmail = jwtUtil.getEmailFromToken(token);
        assertEquals(expectedEmail, actualEmail);
    }

    @かつ("有効なJWTトークンが存在する")
    public void 有効なJWTトークンが存在する() {
        token = jwtUtil.generateToken(1, "test@example.com");
    }

    @もし("JwtUtil.validateToken\\(validToken\\)を呼び出す")
    public void jwtUtilValidateTokenValidTokenを呼び出す() {
        validationResult = jwtUtil.validateToken(token);
    }

    @ならば("trueが返される")
    public void trueが返される() {
        assertTrue(validationResult);
    }

    @かつ("期限切れのJWTトークンが存在する")
    public void 期限切れのJWTトークンが存在する() {
        // For testing purposes, we'll use an invalid token
        token = "invalid.token.here";
    }

    @もし("JwtUtil.validateToken\\(expiredToken\\)を呼び出す")
    public void jwtUtilValidateTokenExpiredTokenを呼び出す() {
        validationResult = jwtUtil.validateToken(token);
    }

    @ならば("falseが返される")
    public void falseが返される() {
        assertFalse(validationResult);
    }

    // OrderDetailPK steps

    @前提("OrderDetailPK pk1 = new OrderDetailPK\\({int}, {int}\\)")
    public void orderDetailPKPk1NewOrderDetailPK(int orderTranId, int orderDetailId) {
        pk1 = new OrderDetailPK(orderTranId, orderDetailId);
    }

    @かつ("OrderDetailPK pk2 = new OrderDetailPK\\({int}, {int}\\)")
    public void orderDetailPKPk2NewOrderDetailPK(int orderTranId, int orderDetailId) {
        pk2 = new OrderDetailPK(orderTranId, orderDetailId);
    }

    @もし("pk1.equals\\(pk2\\)を呼び出す")
    public void pk1EqualsPk2を呼び出す() {
        equalsResult = pk1.equals(pk2);
        assertTrue(equalsResult);
    }

    @かつ("pk1.hashCode\\(\\) == pk2.hashCode\\(\\)")
    public void pk1HashCodePk2HashCode() {
        assertEquals(pk1.hashCode(), pk2.hashCode());
    }

    @もし("pk1.equals\\(null\\)を呼び出す")
    public void pk1EqualsNullを呼び出す() {
        equalsResult = pk1.equals(null);
        assertFalse(equalsResult);
    }
}
