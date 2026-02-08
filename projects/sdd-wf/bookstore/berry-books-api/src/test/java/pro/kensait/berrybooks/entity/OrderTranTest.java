package pro.kensait.berrybooks.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderTranエンティティの単体テスト
 */
class OrderTranTest {

    private OrderTran orderTran;
    private OrderDetail orderDetail;

    @BeforeEach
    void setUp() {
        // Given: OrderTranとOrderDetailを準備
        orderTran = new OrderTran();
        orderTran.setOrderDate(LocalDate.of(2026, 2, 7));
        orderTran.setCustomerId(1);
        orderTran.setTotalPrice(5400);
        orderTran.setDeliveryPrice(400);
        orderTran.setDeliveryAddress("東京都渋谷区1-1-1");
        orderTran.setSettlementType(2);

        orderDetail = new OrderDetail();
        orderDetail.setOrderTranId(1);
        orderDetail.setOrderDetailId(1);
        orderDetail.setBookId(10);
        orderDetail.setBookName("Java入門");
        orderDetail.setPublisherName("技術評論社");
        orderDetail.setPrice(3000);
        orderDetail.setCount(2);
    }

    @Test
    void testAddOrderDetail() {
        // When: addOrderDetail()を呼び出す
        orderTran.addOrderDetail(orderDetail);

        // Then: orderDetailsリストに追加され、双方向リレーションが確立される
        assertTrue(orderTran.getOrderDetails().contains(orderDetail));
        assertEquals(orderTran, orderDetail.getOrderTran());
        assertEquals(1, orderTran.getOrderDetails().size());
    }

    @Test
    void testAddMultipleOrderDetails() {
        // Given: 複数のOrderDetailを準備
        OrderDetail detail1 = new OrderDetail();
        detail1.setOrderTranId(1);
        detail1.setOrderDetailId(1);
        detail1.setBookId(10);
        detail1.setBookName("Java入門");
        detail1.setPrice(3000);
        detail1.setCount(2);

        OrderDetail detail2 = new OrderDetail();
        detail2.setOrderTranId(1);
        detail2.setOrderDetailId(2);
        detail2.setBookId(20);
        detail2.setBookName("Spring Boot実践");
        detail2.setPrice(3500);
        detail2.setCount(1);

        // When: 複数のOrderDetailを追加
        orderTran.addOrderDetail(detail1);
        orderTran.addOrderDetail(detail2);

        // Then: すべてのOrderDetailが追加され、双方向リレーションが確立される
        assertEquals(2, orderTran.getOrderDetails().size());
        assertTrue(orderTran.getOrderDetails().contains(detail1));
        assertTrue(orderTran.getOrderDetails().contains(detail2));
        assertEquals(orderTran, detail1.getOrderTran());
        assertEquals(orderTran, detail2.getOrderTran());
    }

    @Test
    void testRemoveOrderDetail() {
        // Given: OrderDetailが追加されている
        orderTran.addOrderDetail(orderDetail);

        // When: removeOrderDetail()を呼び出す
        orderTran.removeOrderDetail(orderDetail);

        // Then: orderDetailsリストから削除され、双方向リレーションが解除される
        assertFalse(orderTran.getOrderDetails().contains(orderDetail));
        assertNull(orderDetail.getOrderTran());
        assertEquals(0, orderTran.getOrderDetails().size());
    }

    @Test
    void testRemoveOrderDetail_NotAdded() {
        // Given: OrderDetailが追加されていない
        OrderDetail notAdded = new OrderDetail();
        notAdded.setOrderTranId(1);
        notAdded.setOrderDetailId(99);

        // When: 追加されていないOrderDetailを削除
        orderTran.removeOrderDetail(notAdded);

        // Then: エラーは発生せず、リストは空のまま
        assertEquals(0, orderTran.getOrderDetails().size());
    }

    @Test
    void testBidirectionalRelationship() {
        // When: addOrderDetail()を呼び出す
        orderTran.addOrderDetail(orderDetail);

        // Then: 双方向リレーションが正しく設定される
        assertEquals(orderTran, orderDetail.getOrderTran());
        assertTrue(orderTran.getOrderDetails().contains(orderDetail));
    }

    @Test
    void testGettersAndSetters() {
        // When: setterを呼び出す
        orderTran.setOrderTranId(100);
        orderTran.setOrderDate(LocalDate.of(2026, 3, 1));
        orderTran.setCustomerId(5);
        orderTran.setTotalPrice(10000);
        orderTran.setDeliveryPrice(500);
        orderTran.setDeliveryAddress("大阪府大阪市1-1-1");
        orderTran.setSettlementType(1);

        // Then: getterが正しい値を返す
        assertEquals(100, orderTran.getOrderTranId());
        assertEquals(LocalDate.of(2026, 3, 1), orderTran.getOrderDate());
        assertEquals(5, orderTran.getCustomerId());
        assertEquals(10000, orderTran.getTotalPrice());
        assertEquals(500, orderTran.getDeliveryPrice());
        assertEquals("大阪府大阪市1-1-1", orderTran.getDeliveryAddress());
        assertEquals(1, orderTran.getSettlementType());
    }
}
