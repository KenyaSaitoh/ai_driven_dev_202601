package pro.kensait.berrybooks.service.order;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配送料金計算サービス
 * 
 * ビジネスルール:
 * - 注文金額が5000円以上の場合は送料無料
 * - 北海道・沖縄の場合は1500円
 * - その他の地域は800円
 */
@ApplicationScoped
public class DeliveryFeeService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeliveryFeeService.class);
    
    private static final int FREE_SHIPPING_THRESHOLD = 5000;
    private static final int STANDARD_FEE = 800;
    private static final int REMOTE_AREA_FEE = 1500;
    
    /**
     * 配送料金を計算する
     * 
     * @param address 配送先住所
     * @param totalPrice 注文金額合計
     * @return 配送料金（円）
     */
    public int calculateDeliveryFee(String address, int totalPrice) {
        logger.info("[ DeliveryFeeService#calculateDeliveryFee ] address={}, totalPrice={}", 
                    address, totalPrice);
        
        // 5000円以上は送料無料
        if (totalPrice >= FREE_SHIPPING_THRESHOLD) {
            logger.info("[ DeliveryFeeService#calculateDeliveryFee ] Free shipping (totalPrice >= {})", 
                        FREE_SHIPPING_THRESHOLD);
            return 0;
        }
        
        // 北海道・沖縄は1500円
        if (address.startsWith("北海道") || address.startsWith("沖縄")) {
            logger.info("[ DeliveryFeeService#calculateDeliveryFee ] Remote area fee: {}", 
                        REMOTE_AREA_FEE);
            return REMOTE_AREA_FEE;
        }
        
        // その他の地域は800円
        logger.info("[ DeliveryFeeService#calculateDeliveryFee ] Standard fee: {}", 
                    STANDARD_FEE);
        return STANDARD_FEE;
    }
}
