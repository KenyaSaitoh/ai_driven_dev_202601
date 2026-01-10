package pro.kensait.berrybooks.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * ORDER_DETAILテーブルの複合主キークラス
 * 
 * 複合主キー: (ORDER_TRAN_ID, ORDER_DETAIL_ID)
 * - ORDER_TRAN_ID: 注文トランザクションID
 * - ORDER_DETAIL_ID: 注文明細ID（注文内で連番）
 */
@Embeddable
public class OrderDetailPK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "ORDER_TRAN_ID")
    private Integer orderTranId;

    @Column(name = "ORDER_DETAIL_ID")
    private Integer orderDetailId;

    // デフォルトコンストラクタ（JPA必須）
    public OrderDetailPK() {
    }

    public OrderDetailPK(Integer orderTranId, Integer orderDetailId) {
        this.orderTranId = orderTranId;
        this.orderDetailId = orderDetailId;
    }

    // Getters and Setters
    public Integer getOrderTranId() {
        return orderTranId;
    }

    public void setOrderTranId(Integer orderTranId) {
        this.orderTranId = orderTranId;
    }

    public Integer getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(Integer orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    // equals and hashCode（複合主キーに必須）
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDetailPK that = (OrderDetailPK) o;
        return Objects.equals(orderTranId, that.orderTranId) &&
               Objects.equals(orderDetailId, that.orderDetailId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderTranId, orderDetailId);
    }

    @Override
    public String toString() {
        return "OrderDetailPK{" +
               "orderTranId=" + orderTranId +
               ", orderDetailId=" + orderDetailId +
               '}';
    }
}
