package pro.kensait.berrybooks.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderTran - 注文トランザクションエンティティ
 * 
 * 責務:
 * - 注文トランザクション情報の管理
 * - テーブル: ORDER_TRAN
 * 
 * 重要な設計ポイント:
 * - CUSTOMER_ID: 論理参照のみ（外部キー制約なし、customer-hub-apiが顧客データを管理）
 * - @GeneratedValue(strategy = GenerationType.IDENTITY): データベースの自動採番機能を使用
 * - orderDetails: 双方向関連（注文から明細へのナビゲーション）
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
    
    @OneToMany(mappedBy = "orderTran", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();
    
    /**
     * デフォルトコンストラクタ（JPA要件）
     */
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
}
