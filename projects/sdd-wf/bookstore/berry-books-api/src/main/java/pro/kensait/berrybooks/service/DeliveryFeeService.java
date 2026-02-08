package pro.kensait.berrybooks.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配送料金計算サービス
 */
@ApplicationScoped
public class DeliveryFeeService {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryFeeService.class);
    
    private static final int FREE_SHIPPING_THRESHOLD = 5000;
    private static final int OKINAWA_FEE = 800;
    private static final int STANDARD_FEE = 400;
    
    /**
     * 配送料金を計算する
     * 
     * @param totalAmount 購入金額
     * @param deliveryAddress 配送先住所
     * @return 配送料金（円）
     */
    public Integer calculateDeliveryFee(Integer totalAmount, String deliveryAddress) {
        logger.info("[ DeliveryFeeService#calculateDeliveryFee ] totalAmount={}, deliveryAddress={}", 
            totalAmount, deliveryAddress);
        
        // 購入金額が5000円以上の場合は配送料無料
        if (totalAmount >= FREE_SHIPPING_THRESHOLD) {
            logger.debug("[ DeliveryFeeService#calculateDeliveryFee ] Free shipping (totalAmount >= {})", 
                FREE_SHIPPING_THRESHOLD);
            return 0;
        }
        
        // 配送先が沖縄県の場合は800円
        if (deliveryAddress != null && deliveryAddress.contains("沖縄")) {
            logger.debug("[ DeliveryFeeService#calculateDeliveryFee ] Okinawa delivery fee: {}", OKINAWA_FEE);
            return OKINAWA_FEE;
        }
        
        // その他の場合は400円
        logger.debug("[ DeliveryFeeService#calculateDeliveryFee ] Standard delivery fee: {}", STANDARD_FEE);
        return STANDARD_FEE;
    }
}