package pro.kensait.berrybooks.service.delivery;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeliveryFeeServiceTest {

    private DeliveryFeeService deliveryFeeService;

    @BeforeEach
    void setUp() {
        deliveryFeeService = new DeliveryFeeService();
    }

    // calculateDeliveryFeeのテスト

    @Test
    @DisplayName("通常の配送先で標準配送料金（800円）が計算されることをテストする")
    void testCalculateDeliveryFeeStandard() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String address = "東京都渋谷区";
        BigDecimal totalPrice = new BigDecimal("3000");

        // 実行フェーズ
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(address, totalPrice);

        // 検証フェーズ（出力値ベース）
        assertEquals(new BigDecimal("800"), result);
    }

    @Test
    @DisplayName("沖縄県への配送で配送料金が1700円になることをテストする")
    void testCalculateDeliveryFeeOkinawa() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String address = "沖縄県那覇市";
        BigDecimal totalPrice = new BigDecimal("3000");

        // 実行フェーズ
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(address, totalPrice);

        // 検証フェーズ（出力値ベース）
        assertEquals(new BigDecimal("1700"), result);
    }

    @Test
    @DisplayName("沖縄県で始まる住所の場合に配送料金が1700円になることをテストする")
    void testCalculateDeliveryFeeOkinawaPrefix() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String address = "沖縄県宮古島市";
        BigDecimal totalPrice = new BigDecimal("4999");

        // 実行フェーズ
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(address, totalPrice);

        // 検証フェーズ（出力値ベース）
        assertEquals(new BigDecimal("1700"), result);
    }

    @Test
    @DisplayName("購入金額が5000円以上の場合に送料無料になることをテストする")
    void testCalculateDeliveryFeeFree() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String address = "大阪府大阪市";
        BigDecimal totalPrice = new BigDecimal("5000");

        // 実行フェーズ
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(address, totalPrice);

        // 検証フェーズ（出力値ベース）
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("沖縄県でも5000円以上の購入で送料無料になることをテストする")
    void testCalculateDeliveryFeeFreeOkinawa() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String address = "沖縄県那覇市";
        BigDecimal totalPrice = new BigDecimal("5000");

        // 実行フェーズ
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(address, totalPrice);

        // 検証フェーズ（出力値ベース）
        // 5000円以上なので送料無料（沖縄県でも）
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("購入金額が送料無料基準を超える場合に送料無料になることをテストする")
    void testCalculateDeliveryFeeAboveThreshold() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String address = "東京都新宿区";
        BigDecimal totalPrice = new BigDecimal("10000");

        // 実行フェーズ
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(address, totalPrice);

        // 検証フェーズ（出力値ベース）
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("購入金額がちょうど5000円の場合に送料無料になることをテストする（境界値）")
    void testCalculateDeliveryFeeExactlyThreshold() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String address = "東京都新宿区";
        BigDecimal totalPrice = new BigDecimal("5000");

        // 実行フェーズ
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(address, totalPrice);

        // 検証フェーズ（出力値ベース）
        // 5000円ちょうどは送料無料
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("購入金額が4999円の場合に標準配送料金が適用されることをテストする（境界値）")
    void testCalculateDeliveryFeeJustBelowThreshold() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String address = "東京都新宿区";
        BigDecimal totalPrice = new BigDecimal("4999");

        // 実行フェーズ
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(address, totalPrice);

        // 検証フェーズ（出力値ベース）
        // 5000円未満なので通常配送料金
        assertEquals(new BigDecimal("800"), result);
    }

    @Test
    @DisplayName("配送先住所がnullの場合に標準配送料金が適用されることをテストする")
    void testCalculateDeliveryFeeNullAddress() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String address = null;
        BigDecimal totalPrice = new BigDecimal("3000");

        // 実行フェーズ
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(address, totalPrice);

        // 検証フェーズ（出力値ベース）
        // nullの場合は通常配送料金
        assertEquals(new BigDecimal("800"), result);
    }

    @Test
    @DisplayName("配送先住所が空文字列の場合に標準配送料金が適用されることをテストする")
    void testCalculateDeliveryFeeEmptyAddress() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String address = "";
        BigDecimal totalPrice = new BigDecimal("3000");

        // 実行フェーズ
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(address, totalPrice);

        // 検証フェーズ（出力値ベース）
        assertEquals(new BigDecimal("800"), result);
    }

    @Test
    @DisplayName("購入金額が0円の場合に標準配送料金が適用されることをテストする")
    void testCalculateDeliveryFeeZeroPrice() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String address = "東京都渋谷区";
        BigDecimal totalPrice = BigDecimal.ZERO;

        // 実行フェーズ
        BigDecimal result = deliveryFeeService.calculateDeliveryFee(address, totalPrice);

        // 検証フェーズ（出力値ベース）
        assertEquals(new BigDecimal("800"), result);
    }

    // isOkinawaのテスト

    @Test
    @DisplayName("沖縄県の住所が正しく判定されることをテストする")
    void testIsOkinawaTrue() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String address = "沖縄県那覇市";

        // 実行フェーズ
        boolean result = deliveryFeeService.isOkinawa(address);

        // 検証フェーズ（出力値ベース）
        assertTrue(result);
    }

    @Test
    @DisplayName("沖縄県以外の住所が正しく判定されることをテストする")
    void testIsOkinawaFalse() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String address = "東京都渋谷区";

        // 実行フェーズ
        boolean result = deliveryFeeService.isOkinawa(address);

        // 検証フェーズ（出力値ベース）
        assertFalse(result);
    }

    @Test
    @DisplayName("住所がnullの場合にfalseが返されることをテストする")
    void testIsOkinawaNull() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String address = null;

        // 実行フェーズ
        boolean result = deliveryFeeService.isOkinawa(address);

        // 検証フェーズ（出力値ベース）
        assertFalse(result);
    }

    @Test
    @DisplayName("住所が空文字列の場合にfalseが返されることをテストする")
    void testIsOkinawaEmpty() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String address = "";

        // 実行フェーズ
        boolean result = deliveryFeeService.isOkinawa(address);

        // 検証フェーズ（出力値ベース）
        assertFalse(result);
    }

    @Test
    @DisplayName("「沖縄」を含むが「沖縄県」で始まらない住所がfalseと判定されることをテストする")
    void testIsOkinawaPartialMatch() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String address = "福岡県沖縄町"; // 「沖縄」を含むが「沖縄県」で始まらない

        // 実行フェーズ
        boolean result = deliveryFeeService.isOkinawa(address);

        // 検証フェーズ（出力値ベース）
        assertFalse(result);
    }

    // isFreeDeliveryのテスト

    @Test
    @DisplayName("購入金額が5000円の場合に送料無料と判定されることをテストする")
    void testIsFreeDeliveryTrue() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        BigDecimal totalPrice = new BigDecimal("5000");

        // 実行フェーズ
        boolean result = deliveryFeeService.isFreeDelivery(totalPrice);

        // 検証フェーズ（出力値ベース）
        assertTrue(result);
    }

    @Test
    @DisplayName("購入金額が5000円を超える場合に送料無料と判定されることをテストする")
    void testIsFreeDeliveryTrueAboveThreshold() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        BigDecimal totalPrice = new BigDecimal("10000");

        // 実行フェーズ
        boolean result = deliveryFeeService.isFreeDelivery(totalPrice);

        // 検証フェーズ（出力値ベース）
        assertTrue(result);
    }

    @Test
    @DisplayName("購入金額が4999円の場合に送料無料でないと判定されることをテストする")
    void testIsFreeDeliveryFalse() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        BigDecimal totalPrice = new BigDecimal("4999");

        // 実行フェーズ
        boolean result = deliveryFeeService.isFreeDelivery(totalPrice);

        // 検証フェーズ（出力値ベース）
        assertFalse(result);
    }

    @Test
    @DisplayName("購入金額が0円の場合に送料無料でないと判定されることをテストする")
    void testIsFreeDeliveryFalseZero() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        BigDecimal totalPrice = BigDecimal.ZERO;

        // 実行フェーズ
        boolean result = deliveryFeeService.isFreeDelivery(totalPrice);

        // 検証フェーズ（出力値ベース）
        assertFalse(result);
    }

    @Test
    @DisplayName("購入金額が100円の場合に送料無料でないと判定されることをテストする")
    void testIsFreeDeliveryFalseSmallAmount() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        BigDecimal totalPrice = new BigDecimal("100");

        // 実行フェーズ
        boolean result = deliveryFeeService.isFreeDelivery(totalPrice);

        // 検証フェーズ（出力値ベース）
        assertFalse(result);
    }
}
