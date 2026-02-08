package pro.kensait.berrybooks.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderDetailエンティティの単体テスト
 */
class OrderDetailTest {

    private OrderDetail orderDetail;
    private OrderTran orderTran;

    @BeforeEach
    void setUp() {
        // Given: OrderDetailを準備（スナップショットパターン）
        orderDetail = new OrderDetail();
        orderDetail.setOrderTranId(1);
        orderDetail.setOrderDetailId(1);
        orderDetail.setBookId(10);
        orderDetail.setBookName("Java入門");
        orderDetail.setPublisherName("技術評論社");
        orderDetail.setPrice(3000);
        orderDetail.setCount(2);

        // Given: OrderTranを準備
        orderTran = new OrderTran();
        orderTran.setOrderTranId(1);
        orderTran.setOrderDate(LocalDate.of(2026, 2, 7));
        orderTran.setCustomerId(1);
        orderTran.setTotalPrice(5400);
        orderTran.setDeliveryPrice(400);
        orderTran.setDeliveryAddress("東京都渋谷区1-1-1");
        orderTran.setSettlementType(2);
    }

    @Test
    void testGettersAndSetters() {
        // When: setterを呼び出す
        orderDetail.setOrderTranId(10);
        orderDetail.setOrderDetailId(20);
        orderDetail.setBookId(100);
        orderDetail.setBookName("Spring Boot実践");
        orderDetail.setPublisherName("翔泳社");
        orderDetail.setPrice(3500);
        orderDetail.setCount(3);

        // Then: getterが正しい値を返す
        assertEquals(10, orderDetail.getOrderTranId());
        assertEquals(20, orderDetail.getOrderDetailId());
        assertEquals(100, orderDetail.getBookId());
        assertEquals("Spring Boot実践", orderDetail.getBookName());
        assertEquals("翔泳社", orderDetail.getPublisherName());
        assertEquals(3500, orderDetail.getPrice());
        assertEquals(3, orderDetail.getCount());
    }

    @Test
    void testSnapshotFields() {
        // Then: スナップショット値が保持される
        assertEquals("Java入門", orderDetail.getBookName());
        assertEquals("技術評論社", orderDetail.getPublisherName());
        assertEquals(3000, orderDetail.getPrice());
    }

    @Test
    void testManyToOneRelationship() {
        // When: OrderTranを設定
        orderDetail.setOrderTran(orderTran);

        // Then: リレーションシップが正しく設定される
        assertEquals(orderTran, orderDetail.getOrderTran());
        assertEquals(1, orderDetail.getOrderTran().getOrderTranId());
    }

    @Test
    void testNullOrderTran() {
        // When: OrderTranをnullに設定
        orderDetail.setOrderTran(null);

        // Then: nullが設定される
        assertNull(orderDetail.getOrderTran());
    }

    @Test
    void testSnapshotPreservation() {
        // Given: スナップショット値を変更
        orderDetail.setBookName("変更後の書籍名");
        orderDetail.setPublisherName("変更後の出版社");
        orderDetail.setPrice(9999);

        // Then: 変更が保持される（スナップショットは不変ではなく、明示的に設定できる）
        assertEquals("変更後の書籍名", orderDetail.getBookName());
        assertEquals("変更後の出版社", orderDetail.getPublisherName());
        assertEquals(9999, orderDetail.getPrice());
    }

    @Test
    void testComplexCompositeKey() {
        // When: 複合主キーの両方のフィールドを設定
        orderDetail.setOrderTranId(100);
        orderDetail.setOrderDetailId(200);

        // Then: 両方のフィールドが正しく設定される
        assertEquals(100, orderDetail.getOrderTranId());
        assertEquals(200, orderDetail.getOrderDetailId());
    }
}
