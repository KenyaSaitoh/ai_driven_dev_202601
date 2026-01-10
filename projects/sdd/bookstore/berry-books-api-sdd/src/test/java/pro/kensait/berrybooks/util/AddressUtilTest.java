package pro.kensait.berrybooks.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * AddressUtilの単体テスト
 */
class AddressUtilTest {

    @Test
    @DisplayName("住所が東京都から始まる場合、trueを返す")
    void testStartsWithValidPrefecture_Tokyo() {
        // Given
        String address = "東京都渋谷区1-2-3";
        
        // When
        boolean result = AddressUtil.startsWithValidPrefecture(address);
        
        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("住所が大阪府から始まる場合、trueを返す")
    void testStartsWithValidPrefecture_Osaka() {
        // Given
        String address = "大阪府大阪市北区1-1-1";
        
        // When
        boolean result = AddressUtil.startsWithValidPrefecture(address);
        
        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("住所が北海道から始まる場合、trueを返す")
    void testStartsWithValidPrefecture_Hokkaido() {
        // Given
        String address = "北海道札幌市中央区1-1-1";
        
        // When
        boolean result = AddressUtil.startsWithValidPrefecture(address);
        
        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("住所が沖縄県から始まる場合、trueを返す")
    void testStartsWithValidPrefecture_Okinawa() {
        // Given
        String address = "沖縄県那覇市1-1-1";
        
        // When
        boolean result = AddressUtil.startsWithValidPrefecture(address);
        
        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("住所が都道府県名から始まらない場合、falseを返す")
    void testStartsWithValidPrefecture_Invalid() {
        // Given
        String address = "渋谷区1-2-3";
        
        // When
        boolean result = AddressUtil.startsWithValidPrefecture(address);
        
        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("住所がnullの場合、falseを返す")
    void testStartsWithValidPrefecture_Null() {
        // Given
        String address = null;
        
        // When
        boolean result = AddressUtil.startsWithValidPrefecture(address);
        
        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("住所が空文字列の場合、falseを返す")
    void testStartsWithValidPrefecture_Empty() {
        // Given
        String address = "";
        
        // When
        boolean result = AddressUtil.startsWithValidPrefecture(address);
        
        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("住所が都道府県名と部分一致する場合、falseを返す（京都が京都府の一部）")
    void testStartsWithValidPrefecture_PartialMatch() {
        // Given
        String address = "京都市中京区1-1-1";
        
        // When
        boolean result = AddressUtil.startsWithValidPrefecture(address);
        
        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("すべての都道府県名が正しく検証される")
    void testStartsWithValidPrefecture_AllPrefectures() {
        // Given
        String[] prefectures = {
            "北海道", "青森県", "岩手県", "宮城県", "秋田県", "山形県", "福島県",
            "茨城県", "栃木県", "群馬県", "埼玉県", "千葉県", "東京都", "神奈川県",
            "新潟県", "富山県", "石川県", "福井県", "山梨県", "長野県", "岐阜県",
            "静岡県", "愛知県", "三重県", "滋賀県", "京都府", "大阪府", "兵庫県",
            "奈良県", "和歌山県", "鳥取県", "島根県", "岡山県", "広島県", "山口県",
            "徳島県", "香川県", "愛媛県", "高知県", "福岡県", "佐賀県", "長崎県",
            "熊本県", "大分県", "宮崎県", "鹿児島県", "沖縄県"
        };
        
        // When & Then
        for (String prefecture : prefectures) {
            String address = prefecture + "テスト市1-1-1";
            boolean result = AddressUtil.startsWithValidPrefecture(address);
            assertTrue(result, prefecture + "で始まる住所の検証に失敗しました");
        }
    }
}
