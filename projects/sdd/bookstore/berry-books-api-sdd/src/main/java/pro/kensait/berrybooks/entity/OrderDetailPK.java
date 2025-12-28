package pro.kensait.berrybooks.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * 注文明細複合主キークラス
 * 
 * 複合主キー: (ORDER_TRAN_ID, ORDER_DETAIL_ID)
 */
public class OrderDetailPK implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer orderTranId;
    private Integer orderDetailId;
    
    // コンストラクタ
    public OrderDetailPK() {
    }
    
    public OrderDetailPK(Integer orderTranId, Integer orderDetailId) {
        this.orderTranId = orderTranId;
        this.orderDetailId = orderDetailId;
    }
    
    // Getter/Setter
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
    
    // equals/hashCode（複合主キーに必須）
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

