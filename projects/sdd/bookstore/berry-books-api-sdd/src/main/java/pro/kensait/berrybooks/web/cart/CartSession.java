package pro.kensait.berrybooks.web.cart;

import jakarta.enterprise.context.SessionScoped;
import pro.kensait.berrybooks.service.order.CartItem;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * カートセッション管理クラス
 * 
 * セッションスコープでカート情報を管理
 */
@SessionScoped
public class CartSession implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private List<CartItem> cartItems = new ArrayList<>();
    private String deliveryAddress;
    private BigDecimal deliveryPrice = BigDecimal.ZERO;
    
    /**
     * カートアイテムを追加
     * 同じ書籍IDのアイテムが既に存在する場合は数量を加算（BR-011）
     * 
     * @param item カートアイテム
     */
    public void addItem(CartItem item) {
        if (item == null) {
            return;
        }
        
        // 既存アイテムを探す
        CartItem existingItem = cartItems.stream()
                .filter(ci -> ci.getBookId().equals(item.getBookId()))
                .findFirst()
                .orElse(null);
        
        if (existingItem != null) {
            // 既存アイテムの数量を加算
            existingItem.setCount(existingItem.getCount() + item.getCount());
            // バージョン番号を最新のもので上書き
            existingItem.setVersion(item.getVersion());
        } else {
            // 新規アイテムを追加
            cartItems.add(item);
        }
    }
    
    /**
     * カートアイテムを削除
     * 
     * @param bookId 書籍ID
     */
    public void removeItem(Integer bookId) {
        if (bookId == null) {
            return;
        }
        
        cartItems.removeIf(item -> item.getBookId().equals(bookId));
    }
    
    /**
     * カートアイテムリストを取得
     * 
     * @return カートアイテムリスト
     */
    public List<CartItem> getCartItems() {
        return cartItems;
    }
    
    /**
     * 合計金額を計算（BR-013）
     * 
     * @return 合計金額
     */
    public BigDecimal getTotalPrice() {
        return cartItems.stream()
                .map(item -> BigDecimal.valueOf(item.getPrice()).multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * カートが空かどうかを判定
     * 
     * @return 空の場合true
     */
    public boolean isEmpty() {
        return cartItems.isEmpty();
    }
    
    /**
     * カートをクリア
     */
    public void clear() {
        cartItems.clear();
        deliveryAddress = null;
        deliveryPrice = BigDecimal.ZERO;
    }
    
    /**
     * 配送先住所を取得
     * 
     * @return 配送先住所
     */
    public String getDeliveryAddress() {
        return deliveryAddress;
    }
    
    /**
     * 配送先住所を設定
     * 
     * @param deliveryAddress 配送先住所
     */
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
    
    /**
     * 配送料金を取得
     * 
     * @return 配送料金
     */
    public BigDecimal getDeliveryPrice() {
        return deliveryPrice;
    }
    
    /**
     * 配送料金を設定
     * 
     * @param deliveryPrice 配送料金
     */
    public void setDeliveryPrice(BigDecimal deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }
}

