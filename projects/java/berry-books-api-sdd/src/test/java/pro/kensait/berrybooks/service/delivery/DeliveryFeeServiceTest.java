package pro.kensait.berrybooks.service.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DeliveryFeeServiceのユニットテストクラス
 * 
 * <p>配送料金計算ロジックをテストします。</p>
 */
class DeliveryFeeServiceTest {
    
    private DeliveryFeeService deliveryFeeService;
    
    @BeforeEach
    void setUp() {
        deliveryFeeService = new DeliveryFeeService();
    }
    
    /**
     * 通常配送料金（購入金額4999円、東京都）→ 800円
     */
    @Test
    void testCalculateDeliveryFee_Standard_Tokyo() {
        // Arrange
        String deliveryAddress = "東京都渋谷区神南1-1-1";
        BigDecimal totalPrice = new BigDecimal("4999");
        
        // Act
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(deliveryAddress, totalPrice);
        
        // Assert
        assertEquals(new BigDecimal("800"), result, 
                    "購入金額4999円、東京都の配送料金は800円であるべき");
    }
    
    /**
     * 送料無料（購入金額5000円、東京都）→ 0円
     */
    @Test
    void testCalculateDeliveryFee_Free_Exactly5000() {
        // Arrange
        String deliveryAddress = "東京都渋谷区神南1-1-1";
        BigDecimal totalPrice = new BigDecimal("5000");
        
        // Act
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(deliveryAddress, totalPrice);
        
        // Assert
        assertEquals(BigDecimal.ZERO, result, 
                    "購入金額5000円の配送料金は0円（送料無料）であるべき");
    }
    
    /**
     * 沖縄県配送料金（購入金額3000円、沖縄県）→ 1700円
     */
    @Test
    void testCalculateDeliveryFee_Okinawa_3000Yen() {
        // Arrange
        String deliveryAddress = "沖縄県那覇市おもろまち1-1-1";
        BigDecimal totalPrice = new BigDecimal("3000");
        
        // Act
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(deliveryAddress, totalPrice);
        
        // Assert
        assertEquals(new BigDecimal("1700"), result, 
                    "購入金額3000円、沖縄県の配送料金は1700円であるべき");
    }
    
    /**
     * 沖縄県送料無料（購入金額5000円、沖縄県）→ 0円（送料無料優先）
     */
    @Test
    void testCalculateDeliveryFee_Okinawa_Free() {
        // Arrange
        String deliveryAddress = "沖縄県那覇市おもろまち1-1-1";
        BigDecimal totalPrice = new BigDecimal("5000");
        
        // Act
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(deliveryAddress, totalPrice);
        
        // Assert
        assertEquals(BigDecimal.ZERO, result, 
                    "購入金額5000円の配送料金は沖縄県でも0円（送料無料優先）であるべき");
    }
    
    /**
     * 送料無料（購入金額5001円、大阪府）→ 0円
     */
    @Test
    void testCalculateDeliveryFee_Free_Over5000() {
        // Arrange
        String deliveryAddress = "大阪府大阪市北区梅田1-1-1";
        BigDecimal totalPrice = new BigDecimal("5001");
        
        // Act
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(deliveryAddress, totalPrice);
        
        // Assert
        assertEquals(BigDecimal.ZERO, result, 
                    "購入金額5001円の配送料金は0円（送料無料）であるべき");
    }
    
    /**
     * 通常配送料金（購入金額1000円、神奈川県）→ 800円
     */
    @Test
    void testCalculateDeliveryFee_Standard_Kanagawa() {
        // Arrange
        String deliveryAddress = "神奈川県横浜市西区みなとみらい1-1-1";
        BigDecimal totalPrice = new BigDecimal("1000");
        
        // Act
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(deliveryAddress, totalPrice);
        
        // Assert
        assertEquals(new BigDecimal("800"), result, 
                    "購入金額1000円、神奈川県の配送料金は800円であるべき");
    }
    
    /**
     * 通常配送料金（購入金額100円、北海道）→ 800円
     */
    @Test
    void testCalculateDeliveryFee_Standard_Hokkaido() {
        // Arrange
        String deliveryAddress = "北海道札幌市中央区北1条西1丁目";
        BigDecimal totalPrice = new BigDecimal("100");
        
        // Act
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(deliveryAddress, totalPrice);
        
        // Assert
        assertEquals(new BigDecimal("800"), result, 
                    "購入金額100円、北海道の配送料金は800円であるべき");
    }
    
    /**
     * 沖縄県配送料金（購入金額4999円、沖縄県）→ 1700円
     */
    @Test
    void testCalculateDeliveryFee_Okinawa_4999Yen() {
        // Arrange
        String deliveryAddress = "沖縄県石垣市登野城1-1";
        BigDecimal totalPrice = new BigDecimal("4999");
        
        // Act
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(deliveryAddress, totalPrice);
        
        // Assert
        assertEquals(new BigDecimal("1700"), result, 
                    "購入金額4999円、沖縄県の配送料金は1700円であるべき");
    }
    
    /**
     * 送料無料（購入金額10000円、福岡県）→ 0円
     */
    @Test
    void testCalculateDeliveryFee_Free_10000Yen() {
        // Arrange
        String deliveryAddress = "福岡県福岡市博多区博多駅1-1";
        BigDecimal totalPrice = new BigDecimal("10000");
        
        // Act
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(deliveryAddress, totalPrice);
        
        // Assert
        assertEquals(BigDecimal.ZERO, result, 
                    "購入金額10000円の配送料金は0円（送料無料）であるべき");
    }
    
    /**
     * isOkinawa メソッドのテスト: 沖縄県住所
     */
    @Test
    void testIsOkinawa_OkinawaPrefecture() {
        // Arrange
        String deliveryAddress = "沖縄県那覇市おもろまち1-1-1";
        
        // Act
        boolean result = deliveryFeeService.isOkinawa(deliveryAddress);
        
        // Assert
        assertTrue(result, "沖縄県で始まる住所はtrueであるべき");
    }
    
    /**
     * isOkinawa メソッドのテスト: 東京都住所
     */
    @Test
    void testIsOkinawa_TokyoPrefecture() {
        // Arrange
        String deliveryAddress = "東京都渋谷区神南1-1-1";
        
        // Act
        boolean result = deliveryFeeService.isOkinawa(deliveryAddress);
        
        // Assert
        assertFalse(result, "東京都で始まる住所はfalseであるべき");
    }
    
    /**
     * isOkinawa メソッドのテスト: null住所
     */
    @Test
    void testIsOkinawa_NullAddress() {
        // Arrange
        String deliveryAddress = null;
        
        // Act
        boolean result = deliveryFeeService.isOkinawa(deliveryAddress);
        
        // Assert
        assertFalse(result, "nullの住所はfalseであるべき");
    }
    
    /**
     * isFreeDelivery メソッドのテスト: 送料無料対象
     */
    @Test
    void testIsFreeDelivery_Eligible() {
        // Arrange
        BigDecimal totalPrice = new BigDecimal("5000");
        
        // Act
        boolean result = deliveryFeeService.isFreeDelivery(totalPrice);
        
        // Assert
        assertTrue(result, "5000円以上はtrueであるべき");
    }
    
    /**
     * isFreeDelivery メソッドのテスト: 送料無料対象外
     */
    @Test
    void testIsFreeDelivery_NotEligible() {
        // Arrange
        BigDecimal totalPrice = new BigDecimal("4999");
        
        // Act
        boolean result = deliveryFeeService.isFreeDelivery(totalPrice);
        
        // Assert
        assertFalse(result, "4999円はfalseであるべき");
    }
}
