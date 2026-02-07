package pro.kensait.berrybooks.service.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DeliveryFeeService の単体テスト
 * 
 * テスト対象: 配送料金計算ロジック
 */
@DisplayName("DeliveryFeeService のテスト")
class DeliveryFeeServiceTest {
    
    private DeliveryFeeService deliveryFeeService;
    
    @BeforeEach
    void setUp() {
        // Given: DeliveryFeeServiceのインスタンスを準備
        deliveryFeeService = new DeliveryFeeService();
    }
    
    @Test
    @DisplayName("正常系: 注文金額が5000円未満、その他の地域 → 配送料800円")
    void testCalculateDeliveryFee_StandardRegion_Under5000() {
        // Given: 注文金額4000円、配送先住所「東京都渋谷区1-2-3」
        int totalPrice = 4000;
        String address = "東京都渋谷区1-2-3";
        
        // When: calculateDeliveryFee()を呼び出す
        int deliveryFee = deliveryFeeService.calculateDeliveryFee(address, totalPrice);
        
        // Then: 配送料800円が返される
        assertEquals(800, deliveryFee);
    }
    
    @Test
    @DisplayName("正常系: 注文金額が5000円以上 → 送料無料")
    void testCalculateDeliveryFee_FreeShipping() {
        // Given: 注文金額5000円、配送先住所「東京都渋谷区1-2-3」
        int totalPrice = 5000;
        String address = "東京都渋谷区1-2-3";
        
        // When: calculateDeliveryFee()を呼び出す
        int deliveryFee = deliveryFeeService.calculateDeliveryFee(address, totalPrice);
        
        // Then: 配送料0円が返される
        assertEquals(0, deliveryFee);
    }
    
    @Test
    @DisplayName("正常系: 注文金額が5000円以上（10000円） → 送料無料")
    void testCalculateDeliveryFee_FreeShipping_HighPrice() {
        // Given: 注文金額10000円、配送先住所「東京都渋谷区1-2-3」
        int totalPrice = 10000;
        String address = "東京都渋谷区1-2-3";
        
        // When: calculateDeliveryFee()を呼び出す
        int deliveryFee = deliveryFeeService.calculateDeliveryFee(address, totalPrice);
        
        // Then: 配送料0円が返される
        assertEquals(0, deliveryFee);
    }
    
    @Test
    @DisplayName("正常系: 注文金額が5000円未満、北海道 → 配送料1500円")
    void testCalculateDeliveryFee_Hokkaido_Under5000() {
        // Given: 注文金額4000円、配送先住所「北海道札幌市1-2-3」
        int totalPrice = 4000;
        String address = "北海道札幌市1-2-3";
        
        // When: calculateDeliveryFee()を呼び出す
        int deliveryFee = deliveryFeeService.calculateDeliveryFee(address, totalPrice);
        
        // Then: 配送料1500円が返される
        assertEquals(1500, deliveryFee);
    }
    
    @Test
    @DisplayName("正常系: 注文金額が5000円未満、沖縄 → 配送料1500円")
    void testCalculateDeliveryFee_Okinawa_Under5000() {
        // Given: 注文金額4000円、配送先住所「沖縄県那覇市1-2-3」
        int totalPrice = 4000;
        String address = "沖縄県那覇市1-2-3";
        
        // When: calculateDeliveryFee()を呼び出す
        int deliveryFee = deliveryFeeService.calculateDeliveryFee(address, totalPrice);
        
        // Then: 配送料1500円が返される
        assertEquals(1500, deliveryFee);
    }
    
    @Test
    @DisplayName("境界値: 注文金額が5000円ちょうど → 送料無料")
    void testCalculateDeliveryFee_BoundaryValue_Exactly5000() {
        // Given: 注文金額5000円、配送先住所「東京都渋谷区1-2-3」
        int totalPrice = 5000;
        String address = "東京都渋谷区1-2-3";
        
        // When: calculateDeliveryFee()を呼び出す
        int deliveryFee = deliveryFeeService.calculateDeliveryFee(address, totalPrice);
        
        // Then: 配送料0円が返される
        assertEquals(0, deliveryFee);
    }
    
    @Test
    @DisplayName("境界値: 注文金額が4999円 → 配送料800円")
    void testCalculateDeliveryFee_BoundaryValue_4999() {
        // Given: 注文金額4999円、配送先住所「東京都渋谷区1-2-3」
        int totalPrice = 4999;
        String address = "東京都渋谷区1-2-3";
        
        // When: calculateDeliveryFee()を呼び出す
        int deliveryFee = deliveryFeeService.calculateDeliveryFee(address, totalPrice);
        
        // Then: 配送料800円が返される
        assertEquals(800, deliveryFee);
    }
    
    @Test
    @DisplayName("境界値: 注文金額が0円 → 配送料800円")
    void testCalculateDeliveryFee_BoundaryValue_Zero() {
        // Given: 注文金額0円、配送先住所「東京都渋谷区1-2-3」
        int totalPrice = 0;
        String address = "東京都渋谷区1-2-3";
        
        // When: calculateDeliveryFee()を呼び出す
        int deliveryFee = deliveryFeeService.calculateDeliveryFee(address, totalPrice);
        
        // Then: 配送料800円が返される
        assertEquals(800, deliveryFee);
    }
    
    @Test
    @DisplayName("正常系: 北海道、5000円以上 → 送料無料（遠隔地でも5000円以上は無料）")
    void testCalculateDeliveryFee_Hokkaido_FreeShipping() {
        // Given: 注文金額5000円、配送先住所「北海道札幌市1-2-3」
        int totalPrice = 5000;
        String address = "北海道札幌市1-2-3";
        
        // When: calculateDeliveryFee()を呼び出す
        int deliveryFee = deliveryFeeService.calculateDeliveryFee(address, totalPrice);
        
        // Then: 配送料0円が返される（5000円以上は無料）
        assertEquals(0, deliveryFee);
    }
    
    @Test
    @DisplayName("正常系: 沖縄、5000円以上 → 送料無料（遠隔地でも5000円以上は無料）")
    void testCalculateDeliveryFee_Okinawa_FreeShipping() {
        // Given: 注文金額5000円、配送先住所「沖縄県那覇市1-2-3」
        int totalPrice = 5000;
        String address = "沖縄県那覇市1-2-3";
        
        // When: calculateDeliveryFee()を呼び出す
        int deliveryFee = deliveryFeeService.calculateDeliveryFee(address, totalPrice);
        
        // Then: 配送料0円が返される（5000円以上は無料）
        assertEquals(0, deliveryFee);
    }
}
