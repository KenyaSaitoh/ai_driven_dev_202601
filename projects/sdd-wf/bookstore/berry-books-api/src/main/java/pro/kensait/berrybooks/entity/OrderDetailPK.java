package pro.kensait.berrybooks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * 注文明細の複合主キー
 * 
 * @since 1.0.0
 */
@Embeddable
public class OrderDetailPK implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Column(name = "ORDER_TRAN_ID")
    private Integer orderTranId;
    
    @Column(name = "ORDER_DETAIL_ID")
    private Integer orderDetailId;
    
    /**
     * デフォルトコンストラクタ
     */
    public OrderDetailPK() {
    }
    
    /**
     * コンストラクタ
     * 
     * @param orderTranId 注文トランザクションID
     * @param orderDetailId 注文明細ID
     */
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
