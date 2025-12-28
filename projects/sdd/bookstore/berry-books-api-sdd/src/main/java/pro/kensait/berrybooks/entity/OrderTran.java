package pro.kensait.berrybooks.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 注文トランザクションエンティティ
 * 
 * テーブル: ORDER_TRAN
 */
@Entity
@Table(name = "ORDER_TRAN")
public class OrderTran {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDER_TRAN_ID")
    private Integer orderTranId;
    
    @Column(name = "ORDER_DATE", nullable = false)
    private LocalDate orderDate;
    
    @Column(name = "TOTAL_PRICE", nullable = false)
    private Integer totalPrice;
    
    @Column(name = "DELIVERY_PRICE", nullable = false)
    private Integer deliveryPrice;
    
    @Column(name = "DELIVERY_ADDRESS", length = 30, nullable = false)
    private String deliveryAddress;
    
    @Column(name = "SETTLEMENT_TYPE", nullable = false)
    private Integer settlementType;
    
    // リレーションシップ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;
    
    @OneToMany(mappedBy = "orderTran", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails = new ArrayList<>();
    
    // コンストラクタ
    public OrderTran() {
    }
    
    public OrderTran(Integer orderTranId, LocalDate orderDate, Integer totalPrice, 
                    Integer deliveryPrice, String deliveryAddress, Integer settlementType) {
        this.orderTranId = orderTranId;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.deliveryPrice = deliveryPrice;
        this.deliveryAddress = deliveryAddress;
        this.settlementType = settlementType;
    }
    
    // Getter/Setter
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
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
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
                ", totalPrice=" + totalPrice +
                ", deliveryPrice=" + deliveryPrice +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", settlementType=" + settlementType +
                '}';
    }
}

