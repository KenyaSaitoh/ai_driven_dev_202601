package pro.kensait.berrybooks.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DeliveryFeeServiceの単体テスト
 */
@DisplayName("DeliveryFeeService 単体テスト")
class DeliveryFeeServiceTest {
    
    private DeliveryFeeService deliveryFeeService;
    
    @BeforeEach
    void setUp() {
        deliveryFeeService = new DeliveryFeeService();
    }
    
    @Test
    @DisplayName("購入金額が5000円以上で配送料無料")
    void testCalculateDeliveryFee_FreeShipping() {
        // Given: 購入金額=5000円、配送先住所="東京都渋谷区1-1-1"
        Integer totalAmount = 5000;
        String deliveryAddress = "東京都渋谷区1-1-1";
        
        // When: calculateDeliveryFee()を呼び出す
        Integer deliveryFee = deliveryFeeService.calculateDeliveryFee(totalAmount, deliveryAddress);
        
        // Then: 配送料金=0円が返される
        assertEquals(0, deliveryFee);
    }
    
    @Test
    @DisplayName("購入金額が5001円で配送料無料")
    void testCalculateDeliveryFee_FreeShipping_Over5000() {
        // Given: 購入金額=5001円（5000円以上）
        Integer totalAmount = 5001;
        String deliveryAddress = "大阪府大阪市1-1-1";
        
        // When: calculateDeliveryFee()を呼び出す
        Integer deliveryFee = deliveryFeeService.calculateDeliveryFee(totalAmount, deliveryAddress);
        
        // Then: 配送料金=0円が返される
        assertEquals(0, deliveryFee);
    }
    
    @Test
    @DisplayName("配送先が沖縄県の場合は800円")
    void testCalculateDeliveryFee_Okinawa() {
        // Given: 購入金額=3000円、配送先住所="沖縄県那覇市1-1-1"
        Integer totalAmount = 3000;
        String deliveryAddress = "沖縄県那覇市1-1-1";
        
        // When: calculateDeliveryFee()を呼び出す
        Integer deliveryFee = deliveryFeeService.calculateDeliveryFee(totalAmount, deliveryAddress);
        
        // Then: 配送料金=800円が返される
        assertEquals(800, deliveryFee);
    }
    
    @Test
    @DisplayName("その他の地域は400円")
    void testCalculateDeliveryFee_Standard() {
        // Given: 購入金額=3000円、配送先住所="東京都渋谷区1-1-1"
        Integer totalAmount = 3000;
        String deliveryAddress = "東京都渋谷区1-1-1";
        
        // When: calculateDeliveryFee()を呼び出す
        Integer deliveryFee = deliveryFeeService.calculateDeliveryFee(totalAmount, deliveryAddress);
        
        // Then: 配送料金=400円が返される
        assertEquals(400, deliveryFee);
    }
    
    @Test
    @DisplayName("配送先住所がnullの場合は標準料金400円")
    void testCalculateDeliveryFee_NullAddress() {
        // Given: 購入金額=3000円、配送先住所=null
        Integer totalAmount = 3000;
        String deliveryAddress = null;
        
        // When: calculateDeliveryFee()を呼び出す
        Integer deliveryFee = deliveryFeeService.calculateDeliveryFee(totalAmount, deliveryAddress);
        
        // Then: 配送料金=400円が返される
        assertEquals(400, deliveryFee);
    }
    
    @Test
    @DisplayName("購入金額が4999円で沖縄県以外は400円")
    void testCalculateDeliveryFee_JustBelow5000() {
        // Given: 購入金額=4999円（5000円未満）
        Integer totalAmount = 4999;
        String deliveryAddress = "北海道札幌市1-1-1";
        
        // When: calculateDeliveryFee()を呼び出す
        Integer deliveryFee = deliveryFeeService.calculateDeliveryFee(totalAmount, deliveryAddress);
        
        // Then: 配送料金=400円が返される
        assertEquals(400, deliveryFee);
    }
}