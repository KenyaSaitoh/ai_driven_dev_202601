package pro.kensait.berrybooks.service.delivery;

import jakarta.enterprise.context.ApplicationScoped;
import pro.kensait.berrybooks.util.AddressUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配送料金計算サービス
 * 
 * ビジネスルール:
 * - 購入金額10,000円未満: 配送料800円
 * - 購入金額10,000円以上: 配送料0円（無料）
 * - 沖縄県: 特別料金（購入金額に関わらず1,500円）
 */
@ApplicationScoped
public class DeliveryFeeService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeliveryFeeService.class);
    
    // 配送料金閾値
    private static final int FREE_DELIVERY_THRESHOLD = 10000;
    
    // 標準配送料
    private static final int STANDARD_DELIVERY_FEE = 800;
    
    // 沖縄県配送料
    private static final int OKINAWA_DELIVERY_FEE = 1500;
    
    /**
     * 配送料金を計算
     * 
     * @param totalPrice 購入金額（税込）
     * @param deliveryAddress 配送先住所
     * @return 配送料金
     */
    public int calculateDeliveryFee(int totalPrice, String deliveryAddress) {
        logger.info("[ DeliveryFeeService#calculateDeliveryFee ] totalPrice={}, deliveryAddress={}", 
                    totalPrice, deliveryAddress);
        
        // 沖縄県の場合は特別料金
        if (AddressUtil.isOkinawa(deliveryAddress)) {
            logger.info("[ DeliveryFeeService#calculateDeliveryFee ] Okinawa prefecture detected, deliveryFee={}", 
                        OKINAWA_DELIVERY_FEE);
            return OKINAWA_DELIVERY_FEE;
        }
        
        // 購入金額が10,000円以上の場合は送料無料
        if (totalPrice >= FREE_DELIVERY_THRESHOLD) {
            logger.info("[ DeliveryFeeService#calculateDeliveryFee ] Free delivery (totalPrice >= {}), deliveryFee=0", 
                        FREE_DELIVERY_THRESHOLD);
            return 0;
        }
        
        // 標準配送料
        logger.info("[ DeliveryFeeService#calculateDeliveryFee ] Standard delivery fee, deliveryFee={}", 
                    STANDARD_DELIVERY_FEE);
        return STANDARD_DELIVERY_FEE;
    }
}

