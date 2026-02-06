package pro.kensait.berrybooks.cucumber.steps;

import io.cucumber.java.ja.*;
import pro.kensait.berrybooks.entity.OrderDetailPK;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderDetailPK Cucumber ステップ定義
 */
public class OrderDetailPKSteps {

    private OrderDetailPK pk1;
    private OrderDetailPK pk2;
    private Boolean equalsResult;

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
    }

    @ならば("pk1.hashCode\\(\\) == pk2.hashCode\\(\\)")
    public void pk1HashCodePk2HashCode() {
        assertEquals(pk1.hashCode(), pk2.hashCode());
    }

    @もし("pk1.equals\\(null\\)を呼び出す")
    public void pk1EqualsNullを呼び出す() {
        equalsResult = pk1.equals(null);
    }
}
