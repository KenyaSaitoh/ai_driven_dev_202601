package pro.kensait.berrybooks.service.delivery;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DeliveryFeeService - 配送料金計算サービス
 * 
 * 責務:
 * - 配送料金計算ロジック
 * 
 * アノテーション:
 * - @ApplicationScoped: CDI管理Bean（シングルトン）
 * 
 * 計算ロジック:
 * - 購入金額10,000円未満の場合、標準配送料金800円
 * - 購入金額10,000円以上の場合、送料無料（0円）
 * - 沖縄県の場合、特別配送料金1,500円
 */
@ApplicationScoped
public class DeliveryFeeService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeliveryFeeService.class);
    
    private static final int FREE_SHIPPING_THRESHOLD = 10000; // 送料無料の閾値（円）
    private static final int STANDARD_SHIPPING_FEE = 800;     // 標準配送料金（円）
    private static final int OKINAWA_SHIPPING_FEE = 1500;     // 沖縄県配送料金（円）
    
    /**
     * 配送料金を計算
     * 
     * 処理:
     * 1. 購入金額チェック:
     *   - totalPrice >= 10,000円 → 送料無料（0円）
     * 2. 配送先住所チェック:
     *   - deliveryAddress.startsWith("沖縄県") → 1,500円
     *   - それ以外 → 800円
     * 3. 配送料金を返却
     * 
     * @param totalPrice 購入金額合計（円）
     * @param deliveryAddress 配送先住所
     * @return 配送料金（円）
     */
    public Integer calculateDeliveryFee(Integer totalPrice, String deliveryAddress) {
        logger.debug("[ DeliveryFeeService#calculateDeliveryFee ] Calculating delivery fee: totalPrice={}, deliveryAddress={}", 
                    totalPrice, deliveryAddress);
        
        // 購入金額10,000円以上の場合、送料無料
        if (totalPrice >= FREE_SHIPPING_THRESHOLD) {
            logger.debug("[ DeliveryFeeService#calculateDeliveryFee ] Free shipping (totalPrice >= {})", FREE_SHIPPING_THRESHOLD);
            return 0;
        }
        
        // 沖縄県の場合、特別配送料金
        if (deliveryAddress != null && deliveryAddress.startsWith("沖縄県")) {
            logger.debug("[ DeliveryFeeService#calculateDeliveryFee ] Okinawa shipping fee: {}", OKINAWA_SHIPPING_FEE);
            return OKINAWA_SHIPPING_FEE;
        }
        
        // 標準配送料金
        logger.debug("[ DeliveryFeeService#calculateDeliveryFee ] Standard shipping fee: {}", STANDARD_SHIPPING_FEE);
        return STANDARD_SHIPPING_FEE;
    }
}
