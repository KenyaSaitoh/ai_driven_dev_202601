package pro.kensait.berrybooks.service.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DeliveryFeeServiceTest - 配送料金計算サービスのテスト
 * 
 * テスト対象:
 * - DeliveryFeeService.calculateDeliveryFee(Integer totalPrice, String deliveryAddress)
 * 
 * テスト方針:
 * - 正常系: 購入金額と配送先住所のバリエーション
 * - 境界値テスト: 送料無料の閾値（10,000円）
 * - 沖縄県の特別配送料金
 */
@DisplayName("DeliveryFeeService Tests")
class DeliveryFeeServiceTest {
    
    private DeliveryFeeService deliveryFeeService;
    
    @BeforeEach
    void setUp() {
        deliveryFeeService = new DeliveryFeeService();
    }
    
    // 正常系テスト
    
    @Test
    @DisplayName("DFS-001: 購入金額10,000円未満の場合、標準配送料金800円")
    void testCalculateDeliveryFee_LessThanThreshold_ReturnsStandardFee() {
        // Given
        Integer totalPrice = 9999;
        String deliveryAddress = "東京都渋谷区";
        
        // When
        Integer result = deliveryFeeService.calculateDeliveryFee(totalPrice, deliveryAddress);
        
        // Then
        assertEquals(800, result, "購入金額9,999円の場合、標準配送料金800円");
    }
    
    @Test
    @DisplayName("DFS-002: 購入金額10,000円の場合、送料無料（0円）")
    void testCalculateDeliveryFee_ExactlyThreshold_ReturnsFree() {
        // Given
        Integer totalPrice = 10000;
        String deliveryAddress = "東京都渋谷区";
        
        // When
        Integer result = deliveryFeeService.calculateDeliveryFee(totalPrice, deliveryAddress);
        
        // Then
        assertEquals(0, result, "購入金額10,000円の場合、送料無料");
    }
    
    @Test
    @DisplayName("DFS-003: 購入金額10,000円以上の場合、送料無料（0円）")
    void testCalculateDeliveryFee_AboveThreshold_ReturnsFree() {
        // Given
        Integer totalPrice = 15000;
        String deliveryAddress = "東京都渋谷区";
        
        // When
        Integer result = deliveryFeeService.calculateDeliveryFee(totalPrice, deliveryAddress);
        
        // Then
        assertEquals(0, result, "購入金額15,000円の場合、送料無料");
    }
    
    @Test
    @DisplayName("DFS-004: 沖縄県の場合、特別配送料金1,500円")
    void testCalculateDeliveryFee_Okinawa_ReturnsSpecialFee() {
        // Given
        Integer totalPrice = 9999;
        String deliveryAddress = "沖縄県那覇市";
        
        // When
        Integer result = deliveryFeeService.calculateDeliveryFee(totalPrice, deliveryAddress);
        
        // Then
        assertEquals(1500, result, "沖縄県の場合、特別配送料金1,500円");
    }
    
    @Test
    @DisplayName("DFS-005: 沖縄県で購入金額10,000円以上の場合、送料無料（0円）")
    void testCalculateDeliveryFee_OkinawaAboveThreshold_ReturnsFree() {
        // Given
        Integer totalPrice = 10000;
        String deliveryAddress = "沖縄県那覇市";
        
        // When
        Integer result = deliveryFeeService.calculateDeliveryFee(totalPrice, deliveryAddress);
        
        // Then
        assertEquals(0, result, "沖縄県でも購入金額10,000円以上の場合、送料無料");
    }
    
    // 境界値テスト
    
    @Test
    @DisplayName("DFS-BV-001: 購入金額0円の場合、標準配送料金800円")
    void testCalculateDeliveryFee_Zero_ReturnsStandardFee() {
        // Given
        Integer totalPrice = 0;
        String deliveryAddress = "東京都渋谷区";
        
        // When
        Integer result = deliveryFeeService.calculateDeliveryFee(totalPrice, deliveryAddress);
        
        // Then
        assertEquals(800, result);
    }
    
    @Test
    @DisplayName("DFS-BV-002: 購入金額1円の場合、標準配送料金800円")
    void testCalculateDeliveryFee_One_ReturnsStandardFee() {
        // Given
        Integer totalPrice = 1;
        String deliveryAddress = "東京都渋谷区";
        
        // When
        Integer result = deliveryFeeService.calculateDeliveryFee(totalPrice, deliveryAddress);
        
        // Then
        assertEquals(800, result);
    }
    
    @Test
    @DisplayName("DFS-BV-003: 購入金額9,999円の場合、標準配送料金800円")
    void testCalculateDeliveryFee_OneBeforeThreshold_ReturnsStandardFee() {
        // Given
        Integer totalPrice = 9999;
        String deliveryAddress = "東京都渋谷区";
        
        // When
        Integer result = deliveryFeeService.calculateDeliveryFee(totalPrice, deliveryAddress);
        
        // Then
        assertEquals(800, result);
    }
    
    @Test
    @DisplayName("DFS-BV-004: 購入金額10,000円の場合、送料無料（0円）")
    void testCalculateDeliveryFee_Threshold_ReturnsFree() {
        // Given
        Integer totalPrice = 10000;
        String deliveryAddress = "東京都渋谷区";
        
        // When
        Integer result = deliveryFeeService.calculateDeliveryFee(totalPrice, deliveryAddress);
        
        // Then
        assertEquals(0, result);
    }
    
    @Test
    @DisplayName("DFS-BV-005: 購入金額10,001円の場合、送料無料（0円）")
    void testCalculateDeliveryFee_OneAboveThreshold_ReturnsFree() {
        // Given
        Integer totalPrice = 10001;
        String deliveryAddress = "東京都渋谷区";
        
        // When
        Integer result = deliveryFeeService.calculateDeliveryFee(totalPrice, deliveryAddress);
        
        // Then
        assertEquals(0, result);
    }
    
    // 配送先住所のバリエーション
    
    @Test
    @DisplayName("DFS-ADDR-001: 東京都の場合、標準配送料金800円")
    void testCalculateDeliveryFee_Tokyo_ReturnsStandardFee() {
        // Given
        Integer totalPrice = 9999;
        String deliveryAddress = "東京都渋谷区";
        
        // When
        Integer result = deliveryFeeService.calculateDeliveryFee(totalPrice, deliveryAddress);
        
        // Then
        assertEquals(800, result);
    }
    
    @Test
    @DisplayName("DFS-ADDR-002: 大阪府の場合、標準配送料金800円")
    void testCalculateDeliveryFee_Osaka_ReturnsStandardFee() {
        // Given
        Integer totalPrice = 9999;
        String deliveryAddress = "大阪府大阪市";
        
        // When
        Integer result = deliveryFeeService.calculateDeliveryFee(totalPrice, deliveryAddress);
        
        // Then
        assertEquals(800, result);
    }
    
    @Test
    @DisplayName("DFS-ADDR-003: 北海道の場合、標準配送料金800円")
    void testCalculateDeliveryFee_Hokkaido_ReturnsStandardFee() {
        // Given
        Integer totalPrice = 9999;
        String deliveryAddress = "北海道札幌市";
        
        // When
        Integer result = deliveryFeeService.calculateDeliveryFee(totalPrice, deliveryAddress);
        
        // Then
        assertEquals(800, result);
    }
    
    @Test
    @DisplayName("DFS-ADDR-004: 沖縄県の場合、特別配送料金1,500円")
    void testCalculateDeliveryFee_OkinawaAddress_ReturnsSpecialFee() {
        // Given
        Integer totalPrice = 9999;
        String deliveryAddress = "沖縄県那覇市";
        
        // When
        Integer result = deliveryFeeService.calculateDeliveryFee(totalPrice, deliveryAddress);
        
        // Then
        assertEquals(1500, result);
    }
}
