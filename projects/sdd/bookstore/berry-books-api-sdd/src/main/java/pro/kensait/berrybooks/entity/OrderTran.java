package pro.kensait.berrybooks.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * ORDER_TRANテーブルのエンティティ
 * 
 * 注文トランザクション情報を管理する。
 * CUSTOMER_IDは外部キーだが、Customerエンティティとのリレーションは未実装（外部API管理）。
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
    private LocalDate orderDate;  // DATE型（日付のみ）

    @Column(name = "CUSTOMER_ID", nullable = false)
    private Integer customerId;

    @Column(name = "TOTAL_PRICE", nullable = false)
    private Integer totalPrice;

    @Column(name = "DELIVERY_PRICE", nullable = false)
    private Integer deliveryPrice;

    @Column(name = "DELIVERY_ADDRESS", length = 30, nullable = false)
    private String deliveryAddress;

    @Column(name = "SETTLEMENT_TYPE", nullable = false)
    private Integer settlementType;

    @OneToMany(mappedBy = "orderTran", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    // デフォルトコンストラクタ（JPA必須）
    public OrderTran() {
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

    // Helperメソッド：注文明細を追加
    public void addOrderDetail(OrderDetail orderDetail) {
        orderDetails.add(orderDetail);
        orderDetail.setOrderTran(this);
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
               '}';
    }
}
