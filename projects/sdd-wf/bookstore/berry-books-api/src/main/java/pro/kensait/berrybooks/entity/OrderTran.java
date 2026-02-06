package pro.kensait.berrybooks.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 注文トランザクションエンティティ
 * 
 * 注文の基本情報（注文日、顧客ID、合計金額、配送先等）を管理する。
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "ORDER_TRAN")
public class OrderTran implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDER_TRAN_ID")
    private Integer orderTranId;
    
    @Column(name = "ORDER_DATE", nullable = false)
    private LocalDate orderDate;
    
    @Column(name = "CUSTOMER_ID", nullable = false)
    private Integer customerId;
    
    @Column(name = "TOTAL_PRICE", nullable = false)
    private Integer totalPrice;
    
    @Column(name = "DELIVERY_PRICE", nullable = false)
    private Integer deliveryPrice;
    
    @Column(name = "DELIVERY_ADDRESS", nullable = false, length = 30)
    private String deliveryAddress;
    
    @Column(name = "SETTLEMENT_TYPE", nullable = false)
    private Integer settlementType;
    
    @OneToMany(mappedBy = "orderTran", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails = new ArrayList<>();
    
    /**
     * デフォルトコンストラクタ
     */
    public OrderTran() {
    }
    
    /**
     * 注文明細を追加する
     * 
     * @param orderDetail 注文明細
     */
    public void addOrderDetail(OrderDetail orderDetail) {
        orderDetails.add(orderDetail);
        orderDetail.setOrderTran(this);
    }
    
    /**
     * 注文明細を削除する
     * 
     * @param orderDetail 注文明細
     */
    public void removeOrderDetail(OrderDetail orderDetail) {
        orderDetails.remove(orderDetail);
        orderDetail.setOrderTran(null);
    }
    
    // Getters and Setters
    
    public Integer getOrderTranId() {
        return orderTranId;
    }
    
    public void setOrderTranId(Integer orderTranId) {
        this.orderTranId = orderTranId;
    }
    
    public LocalDate getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }
    
    public Integer getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }
    
    public Integer getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public Integer getDeliveryPrice() {
        return deliveryPrice;
    }
    
    public void setDeliveryPrice(Integer deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }
    
    public String getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
    
    public Integer getSettlementType() {
        return settlementType;
    }
    
    public void setSettlementType(Integer settlementType) {
        this.settlementType = settlementType;
    }
    
    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }
    
    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }
    
    @Override
    public String toString() {
        return "OrderTran{" +
                "orderTranId=" + orderTranId +
                ", orderDate=" + orderDate +
                ", customerId=" + customerId +
                ", totalPrice=" + totalPrice +
                ", deliveryPrice=" + deliveryPrice +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", settlementType=" + settlementType +
                ", orderDetailsCount=" + (orderDetails != null ? orderDetails.size() : 0) +
                '}';
    }
}
