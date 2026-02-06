package pro.kensait.berrybooks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * 注文明細の複合主キー
 */
@Embeddable
public class OrderDetailPK implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Column(name = "ORDER_TRAN_ID")
    private Integer orderTranId;
    
    @Column(name = "ORDER_DETAIL_ID")
    private Integer orderDetailId;
    
    public OrderDetailPK() {
    }
    
    public OrderDetailPK(Integer orderTranId, Integer orderDetailId) {
        this.orderTranId = orderTranId;
        this.orderDetailId = orderDetailId;
    }
    
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
}
