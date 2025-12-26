package pro.kensait.berrybooks.web.cart;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * CartSessionのユニットテストクラス
 * 
 * <p>カート管理のビジネスロジックをテストします。</p>
 */
class CartSessionTest {
    
    private CartSession cartSession;
    
    @BeforeEach
    void setUp() {
        cartSession = new CartSession();
    }
    
    /**
     * カートアイテム追加（正常系）
     * カートに新しいアイテムを追加できることを確認
     */
    @Test
    void testAddItem_NewItem() {
        // Arrange
        CartItem item = new CartItem(
            1, "Java SE ディープダイブ", "Test Publisher",
            BigDecimal.valueOf(3400), 2, 1L
        );
        
        // Act
        cartSession.addItem(item);
        
        // Assert
        assertEquals(1, cartSession.getCartItems().size());
        assertEquals(1, cartSession.getCartItems().get(0).getBookId());
        assertEquals(2, cartSession.getCartItems().get(0).getCount());
        assertEquals(BigDecimal.valueOf(6800), cartSession.getTotalPrice());
    }
    
    /**
     * 同じ書籍を複数回追加（数量加算、BR-011）
     * 同じ書籍IDのアイテムを追加した場合、数量が加算されることを確認
     */
    @Test
    void testAddItem_ExistingItem_QuantityAdded() {
        // Arrange
        CartItem item1 = new CartItem(
            1, "Java SE ディープダイブ", "Test Publisher",
            BigDecimal.valueOf(3400), 2, 1L
        );
        CartItem item2 = new CartItem(
            1, "Java SE ディープダイブ", "Test Publisher",
            BigDecimal.valueOf(3400), 3, 2L
        );
        
        // Act
        cartSession.addItem(item1);
        cartSession.addItem(item2);
        
        // Assert
        assertEquals(1, cartSession.getCartItems().size(), "同じ書籍IDのアイテムは重複しない");
        assertEquals(5, cartSession.getCartItems().get(0).getCount(), "数量が加算される（BR-011）");
        assertEquals(2L, cartSession.getCartItems().get(0).getVersion(), "バージョン番号が最新のもので上書きされる");
        assertEquals(BigDecimal.valueOf(17000), cartSession.getTotalPrice());
    }
    
    /**
     * カートアイテム削除（正常系）
     * カートからアイテムを削除できることを確認
     */
    @Test
    void testRemoveItem_Success() {
        // Arrange
        CartItem item1 = new CartItem(
            1, "Java SE ディープダイブ", "Test Publisher",
            BigDecimal.valueOf(3400), 2, 1L
        );
        CartItem item2 = new CartItem(
            2, "JVM とバイトコードの探求", "Test Publisher",
            BigDecimal.valueOf(4200), 1, 1L
        );
        cartSession.addItem(item1);
        cartSession.addItem(item2);
        
        // Act
        cartSession.removeItem(1);
        
        // Assert
        assertEquals(1, cartSession.getCartItems().size());
        assertEquals(2, cartSession.getCartItems().get(0).getBookId());
        assertEquals(BigDecimal.valueOf(4200), cartSession.getTotalPrice());
    }
    
    /**
     * 存在しないアイテムの削除（異常系）
     * 存在しないアイテムを削除しようとしてもエラーにならないことを確認
     */
    @Test
    void testRemoveItem_NonExistentItem() {
        // Arrange
        CartItem item = new CartItem(
            1, "Java SE ディープダイブ", "Test Publisher",
            BigDecimal.valueOf(3400), 2, 1L
        );
        cartSession.addItem(item);
        
        // Act
        cartSession.removeItem(999);
        
        // Assert
        assertEquals(1, cartSession.getCartItems().size());
        assertEquals(BigDecimal.valueOf(6800), cartSession.getTotalPrice());
    }
    
    /**
     * 合計金額計算（正常系、BR-013）
     * カート内のすべてのアイテムの小計を合算した合計金額が正しく計算されることを確認
     */
    @Test
    void testCalculateTotalPrice_MultipleItems() {
        // Arrange
        CartItem item1 = new CartItem(
            1, "Java SE ディープダイブ", "Test Publisher",
            BigDecimal.valueOf(3400), 2, 1L
        );
        CartItem item2 = new CartItem(
            2, "JVM とバイトコードの探求", "Test Publisher",
            BigDecimal.valueOf(4200), 3, 1L
        );
        CartItem item3 = new CartItem(
            3, "Spring Boot ベストプラクティス", "Test Publisher",
            BigDecimal.valueOf(2800), 1, 1L
        );
        
        // Act
        cartSession.addItem(item1);
        cartSession.addItem(item2);
        cartSession.addItem(item3);
        
        // Assert
        // 3400 * 2 + 4200 * 3 + 2800 * 1 = 6800 + 12600 + 2800 = 22200
        assertEquals(BigDecimal.valueOf(22200), cartSession.getTotalPrice());
    }
    
    /**
     * カートクリア（正常系）
     * カートをクリアすると全てのアイテムが削除され、合計金額がゼロになることを確認
     */
    @Test
    void testClear_Success() {
        // Arrange
        CartItem item1 = new CartItem(
            1, "Java SE ディープダイブ", "Test Publisher",
            BigDecimal.valueOf(3400), 2, 1L
        );
        CartItem item2 = new CartItem(
            2, "JVM とバイトコードの探求", "Test Publisher",
            BigDecimal.valueOf(4200), 1, 1L
        );
        cartSession.addItem(item1);
        cartSession.addItem(item2);
        
        // Act
        cartSession.clear();
        
        // Assert
        assertTrue(cartSession.isEmpty(), "カートが空になる");
        assertEquals(0, cartSession.getCartItems().size());
        assertEquals(BigDecimal.ZERO, cartSession.getTotalPrice());
        assertNull(cartSession.getDeliveryAddress());
    }
    
    /**
     * 空カートの判定（正常系）
     * カートが空かどうかを正しく判定できることを確認
     */
    @Test
    void testIsEmpty_EmptyCart() {
        // Assert
        assertTrue(cartSession.isEmpty());
    }
    
    /**
     * 空でないカートの判定（正常系）
     * カートにアイテムがある場合、isEmptyがfalseを返すことを確認
     */
    @Test
    void testIsEmpty_NotEmptyCart() {
        // Arrange
        CartItem item = new CartItem(
            1, "Java SE ディープダイブ", "Test Publisher",
            BigDecimal.valueOf(3400), 1, 1L
        );
        
        // Act
        cartSession.addItem(item);
        
        // Assert
        assertFalse(cartSession.isEmpty());
    }
    
    /**
     * nullアイテムの追加（異常系）
     * nullアイテムを追加してもエラーにならないことを確認
     */
    @Test
    void testAddItem_NullItem() {
        // Act
        cartSession.addItem(null);
        
        // Assert
        assertTrue(cartSession.isEmpty());
        assertEquals(BigDecimal.ZERO, cartSession.getTotalPrice());
    }
    
    /**
     * null書籍IDでの削除（異常系）
     * null書籍IDで削除してもエラーにならないことを確認
     */
    @Test
    void testRemoveItem_NullBookId() {
        // Arrange
        CartItem item = new CartItem(
            1, "Java SE ディープダイブ", "Test Publisher",
            BigDecimal.valueOf(3400), 1, 1L
        );
        cartSession.addItem(item);
        
        // Act
        cartSession.removeItem(null);
        
        // Assert
        assertEquals(1, cartSession.getCartItems().size());
    }
    
    /**
     * 配送先住所の設定・取得（正常系）
     * 配送先住所を正しく設定・取得できることを確認
     */
    @Test
    void testDeliveryAddress_SetAndGet() {
        // Arrange
        String address = "東京都渋谷区渋谷1-1-1";
        
        // Act
        cartSession.setDeliveryAddress(address);
        
        // Assert
        assertEquals(address, cartSession.getDeliveryAddress());
    }
    
    /**
     * 配送料金の設定・取得（正常系）
     * 配送料金を正しく設定・取得できることを確認
     */
    @Test
    void testDeliveryPrice_SetAndGet() {
        // Arrange
        BigDecimal deliveryPrice = BigDecimal.valueOf(500);
        
        // Act
        cartSession.setDeliveryPrice(deliveryPrice);
        
        // Assert
        assertEquals(deliveryPrice, cartSession.getDeliveryPrice());
    }
    
    /**
     * 複数アイテムの削除（正常系）
     * 複数のアイテムを順次削除できることを確認
     */
    @Test
    void testRemoveMultipleItems_Success() {
        // Arrange
        CartItem item1 = new CartItem(
            1, "Java SE ディープダイブ", "Test Publisher",
            BigDecimal.valueOf(3400), 2, 1L
        );
        CartItem item2 = new CartItem(
            2, "JVM とバイトコードの探求", "Test Publisher",
            BigDecimal.valueOf(4200), 1, 1L
        );
        CartItem item3 = new CartItem(
            3, "Spring Boot ベストプラクティス", "Test Publisher",
            BigDecimal.valueOf(2800), 1, 1L
        );
        cartSession.addItem(item1);
        cartSession.addItem(item2);
        cartSession.addItem(item3);
        
        // Act
        cartSession.removeItem(1);
        cartSession.removeItem(3);
        
        // Assert
        assertEquals(1, cartSession.getCartItems().size());
        assertEquals(2, cartSession.getCartItems().get(0).getBookId());
        assertEquals(BigDecimal.valueOf(4200), cartSession.getTotalPrice());
    }
}






