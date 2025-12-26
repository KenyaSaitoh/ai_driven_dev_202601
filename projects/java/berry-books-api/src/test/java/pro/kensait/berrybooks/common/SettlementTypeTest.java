package pro.kensait.berrybooks.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SettlementTypeTest {

    // getCodeのテスト

    @Test
    @DisplayName("銀行振り込みのコードが1であることをテストする")
    void testGetCodeBankTransfer() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        SettlementType type = SettlementType.BANK_TRANSFER;

        // 実行フェーズ
        Integer code = type.getCode();

        // 検証フェーズ（出力値ベース）
        assertEquals(1, code);
    }

    @Test
    @DisplayName("クレジットカードのコードが2であることをテストする")
    void testGetCodeCreditCard() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        SettlementType type = SettlementType.CREDIT_CARD;

        // 実行フェーズ
        Integer code = type.getCode();

        // 検証フェーズ（出力値ベース）
        assertEquals(2, code);
    }

    @Test
    @DisplayName("着払いのコードが3であることをテストする")
    void testGetCodeCashOnDelivery() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        SettlementType type = SettlementType.CASH_ON_DELIVERY;

        // 実行フェーズ
        Integer code = type.getCode();

        // 検証フェーズ（出力値ベース）
        assertEquals(3, code);
    }

    // getDisplayNameのテスト

    @Test
    @DisplayName("銀行振り込みの表示名が正しく取得できることをテストする")
    void testGetDisplayNameBankTransfer() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        SettlementType type = SettlementType.BANK_TRANSFER;

        // 実行フェーズ
        String displayName = type.getDisplayName();

        // 検証フェーズ（出力値ベース）
        assertEquals("銀行振り込み", displayName);
    }

    @Test
    @DisplayName("クレジットカードの表示名が正しく取得できることをテストする")
    void testGetDisplayNameCreditCard() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        SettlementType type = SettlementType.CREDIT_CARD;

        // 実行フェーズ
        String displayName = type.getDisplayName();

        // 検証フェーズ（出力値ベース）
        assertEquals("クレジットカード", displayName);
    }

    @Test
    @DisplayName("着払いの表示名が正しく取得できることをテストする")
    void testGetDisplayNameCashOnDelivery() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        SettlementType type = SettlementType.CASH_ON_DELIVERY;

        // 実行フェーズ
        String displayName = type.getDisplayName();

        // 検証フェーズ（出力値ベース）
        assertEquals("着払い", displayName);
    }

    // fromCodeのテスト

    @Test
    @DisplayName("コード1から銀行振り込みのEnumが取得できることをテストする")
    void testFromCodeBankTransfer() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer code = 1;

        // 実行フェーズ
        SettlementType type = SettlementType.fromCode(code);

        // 検証フェーズ（出力値ベース）
        assertEquals(SettlementType.BANK_TRANSFER, type);
    }

    @Test
    @DisplayName("コード2からクレジットカードのEnumが取得できることをテストする")
    void testFromCodeCreditCard() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer code = 2;

        // 実行フェーズ
        SettlementType type = SettlementType.fromCode(code);

        // 検証フェーズ（出力値ベース）
        assertEquals(SettlementType.CREDIT_CARD, type);
    }

    @Test
    @DisplayName("コード3から着払いのEnumが取得できることをテストする")
    void testFromCodeCashOnDelivery() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer code = 3;

        // 実行フェーズ
        SettlementType type = SettlementType.fromCode(code);

        // 検証フェーズ（出力値ベース）
        assertEquals(SettlementType.CASH_ON_DELIVERY, type);
    }

    @Test
    @DisplayName("nullコードからnullが返されることをテストする")
    void testFromCodeNull() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer code = null;

        // 実行フェーズ
        SettlementType type = SettlementType.fromCode(code);

        // 検証フェーズ（出力値ベース）
        assertNull(type);
    }

    @Test
    @DisplayName("不正なコードでIllegalArgumentExceptionがスローされることをテストする")
    void testFromCodeInvalid() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer code = 999;

        // 実行フェーズと検証フェーズ
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SettlementType.fromCode(code));

        // 検証フェーズ（例外メッセージ）
        assertTrue(exception.getMessage().contains("Invalid settlement type code"));
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    @DisplayName("コード0でIllegalArgumentExceptionがスローされることをテストする")
    void testFromCodeZero() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer code = 0;

        // 実行フェーズと検証フェーズ
        assertThrows(IllegalArgumentException.class,
                () -> SettlementType.fromCode(code));
    }

    // getDisplayNameByCodeのテスト

    @Test
    @DisplayName("コード1から銀行振り込みの表示名が取得できることをテストする")
    void testGetDisplayNameByCodeBankTransfer() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer code = 1;

        // 実行フェーズ
        String displayName = SettlementType.getDisplayNameByCode(code);

        // 検証フェーズ（出力値ベース）
        assertEquals("銀行振り込み", displayName);
    }

    @Test
    @DisplayName("コード2からクレジットカードの表示名が取得できることをテストする")
    void testGetDisplayNameByCodeCreditCard() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer code = 2;

        // 実行フェーズ
        String displayName = SettlementType.getDisplayNameByCode(code);

        // 検証フェーズ（出力値ベース）
        assertEquals("クレジットカード", displayName);
    }

    @Test
    @DisplayName("コード3から着払いの表示名が取得できることをテストする")
    void testGetDisplayNameByCodeCashOnDelivery() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer code = 3;

        // 実行フェーズ
        String displayName = SettlementType.getDisplayNameByCode(code);

        // 検証フェーズ（出力値ベース）
        assertEquals("着払い", displayName);
    }

    @Test
    @DisplayName("nullコードから未選択が返されることをテストする")
    void testGetDisplayNameByCodeNull() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer code = null;

        // 実行フェーズ
        String displayName = SettlementType.getDisplayNameByCode(code);

        // 検証フェーズ（出力値ベース）
        assertEquals("未選択", displayName);
    }

    @Test
    @DisplayName("不正なコードから不明が返されることをテストする")
    void testGetDisplayNameByCodeInvalid() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer code = 999;

        // 実行フェーズ
        String displayName = SettlementType.getDisplayNameByCode(code);

        // 検証フェーズ（出力値ベース）
        assertEquals("不明", displayName);
    }

    // getAllCodesのテスト

    @Test
    @DisplayName("全ての決済方法コードが取得できることをテストする")
    void testGetAllCodes() {
        // 実行フェーズ
        Integer[] codes = SettlementType.getAllCodes();

        // 検証フェーズ（出力値ベース）
        assertNotNull(codes);
        assertEquals(3, codes.length);
        assertEquals(1, codes[0]);
        assertEquals(2, codes[1]);
        assertEquals(3, codes[2]);
    }

    @Test
    @DisplayName("values()で全てのEnum値が取得できることをテストする")
    void testValues() {
        // 実行フェーズ
        SettlementType[] types = SettlementType.values();

        // 検証フェーズ（出力値ベース）
        assertNotNull(types);
        assertEquals(3, types.length);
        assertEquals(SettlementType.BANK_TRANSFER, types[0]);
        assertEquals(SettlementType.CREDIT_CARD, types[1]);
        assertEquals(SettlementType.CASH_ON_DELIVERY, types[2]);
    }

    @Test
    @DisplayName("valueOf()で名前からEnum値が取得できることをテストする")
    void testValueOf() {
        // 実行フェーズ
        SettlementType type = SettlementType.valueOf("BANK_TRANSFER");

        // 検証フェーズ（出力値ベース）
        assertEquals(SettlementType.BANK_TRANSFER, type);
        assertEquals(1, type.getCode());
        assertEquals("銀行振り込み", type.getDisplayName());
    }
}

