package pro.kensait.berrybooks.service.order;

import java.util.List;

/**
 * 注文転送オブジェクト（POJO）
 * 
 * サービス層で使用する注文情報
 */
public class OrderTO {
    
    private Integer customerId;
    private List<CartItem> cartItems;
    private Integer totalPrice;
    private Integer deliveryPrice;
    private String deliveryAddress;
    private Integer settlementType;
    
    // コンストラクタ
    public OrderTO() {
    }
    
    public OrderTO(Integer customerId, List<CartItem> cartItems, Integer totalPrice, 
                  Integer deliveryPrice, String deliveryAddress, Integer settlementType) {
        this.customerId = customerId;
        this.cartItems = cartItems;
        this.totalPrice = totalPrice;
        this.deliveryPrice = deliveryPrice;
        this.deliveryAddress = deliveryAddress;
        this.settlementType = settlementType;
    }
    
    // Getter/Setter
    public Integer getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }
    
    public List<CartItem> getCartItems() {
        return cartItems;
    }
    
    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
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
}

