package pro.kensait.berrybooks.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AddressUtilのユニットテスト
 * 
 * 住所検証ロジックのテスト
 */
@DisplayName("AddressUtil Test")
class AddressUtilTest {
    
    @ParameterizedTest
    @ValueSource(strings = {
        "東京都渋谷区1-2-3",
        "大阪府大阪市北区1-1-1",
        "北海道札幌市中央区",
        "沖縄県那覇市",
        "福岡県福岡市博多区"
    })
    @DisplayName("都道府県名から始まる住所は検証成功")
    void testStartsWithValidPrefecture_ValidAddress(String address) {
        // When
        boolean result = AddressUtil.startsWithValidPrefecture(address);
        
        // Then
        assertTrue(result);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "渋谷区1-2-3",
        "大阪市北区1-1-1",
        "札幌市中央区",
        "123-456 Invalid Address",
        ""
    })
    @DisplayName("都道府県名から始まらない住所は検証失敗")
    void testStartsWithValidPrefecture_InvalidAddress(String address) {
        // When
        boolean result = AddressUtil.startsWithValidPrefecture(address);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    @DisplayName("null住所は検証失敗")
    void testStartsWithValidPrefecture_NullAddress() {
        // When
        boolean result = AddressUtil.startsWithValidPrefecture(null);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    @DisplayName("住所から都道府県名を抽出できる")
    void testExtractPrefecture() {
        // Given
        String address = "東京都渋谷区1-2-3";
        
        // When
        String prefecture = AddressUtil.extractPrefecture(address);
        
        // Then
        assertEquals("東京都", prefecture);
    }
    
    @Test
    @DisplayName("都道府県名がない住所からはnullを返す")
    void testExtractPrefecture_InvalidAddress() {
        // Given
        String address = "渋谷区1-2-3";
        
        // When
        String prefecture = AddressUtil.extractPrefecture(address);
        
        // Then
        assertNull(prefecture);
    }
    
    @Test
    @DisplayName("沖縄県を判定できる")
    void testIsOkinawa_OkinawaAddress() {
        // Given
        String address = "沖縄県那覇市";
        
        // When
        boolean result = AddressUtil.isOkinawa(address);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    @DisplayName("沖縄県以外は判定失敗")
    void testIsOkinawa_NonOkinawaAddress() {
        // Given
        String address = "東京都渋谷区1-2-3";
        
        // When
        boolean result = AddressUtil.isOkinawa(address);
        
        // Then
        assertFalse(result);
    }
}

