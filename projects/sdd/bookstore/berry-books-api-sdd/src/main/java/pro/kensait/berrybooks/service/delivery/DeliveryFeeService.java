package pro.kensait.berrybooks.service.delivery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import pro.kensait.berrybooks.util.AddressUtil;

/**
 * 配送料金計算サービス
 * 
 * 配送料金の計算ロジックを提供する。
 */
@ApplicationScoped
public class DeliveryFeeService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryFeeService.class);

    // 配送料金定数
    private static final int FREE_SHIPPING_THRESHOLD = 10000;  // 送料無料の閾値
    private static final int STANDARD_DELIVERY_FEE = 800;      // 標準配送料
    private static final int OKINAWA_DELIVERY_FEE = 1500;      // 沖縄県配送料

    /**
     * 配送料金を計算する
     * 
     * @param totalPrice 購入金額
     * @param address 配送先住所
     * @return 配送料金
     */
    public Integer calculateDeliveryFee(Integer totalPrice, String address) {
        logger.debug("[ DeliveryFeeService#calculateDeliveryFee ] totalPrice={}, address={}", 
                totalPrice, address);

        // 沖縄県の場合は特別料金
        if (AddressUtil.startsWithPrefecture(address, "沖縄県")) {
            logger.info("[ DeliveryFeeService#calculateDeliveryFee ] Okinawa delivery fee: {}", 
                    OKINAWA_DELIVERY_FEE);
            return OKINAWA_DELIVERY_FEE;
        }

        // 購入金額が10,000円以上の場合は送料無料
        if (totalPrice >= FREE_SHIPPING_THRESHOLD) {
            logger.info("[ DeliveryFeeService#calculateDeliveryFee ] Free shipping (totalPrice >= {})", 
                    FREE_SHIPPING_THRESHOLD);
            return 0;
        }

        // それ以外は標準配送料
        logger.info("[ DeliveryFeeService#calculateDeliveryFee ] Standard delivery fee: {}", 
                STANDARD_DELIVERY_FEE);
        return STANDARD_DELIVERY_FEE;
    }
}
