package pro.kensait.berrybooks.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderDetailPK の単体テスト
 * 
 * @since 1.0.0
 */
class OrderDetailPKTest {
    
    /**
     * Scenario: 同じ値のOrderDetailPKが等しい
     * 
     * Given: OrderDetailPK pk1 = new OrderDetailPK(1, 1)
     *        OrderDetailPK pk2 = new OrderDetailPK(1, 1)
     * When: pk1.equals(pk2)を呼び出す
     * Then: trueが返される
     *       pk1.hashCode() == pk2.hashCode()
     */
    @Test
    void testEquals_SameValues_ReturnsTrue() {
        // Given
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        OrderDetailPK pk2 = new OrderDetailPK(1, 1);
        
        // When
        boolean result = pk1.equals(pk2);
        
        // Then
        assertTrue(result, "Same values should be equal");
        assertEquals(pk1.hashCode(), pk2.hashCode(), "Hash codes should be equal");
    }
    
    /**
     * Scenario: 異なる値のOrderDetailPKが等しくない
     * 
     * Given: OrderDetailPK pk1 = new OrderDetailPK(1, 1)
     *        OrderDetailPK pk2 = new OrderDetailPK(1, 2)
     * When: pk1.equals(pk2)を呼び出す
     * Then: falseが返される
     */
    @Test
    void testEquals_DifferentValues_ReturnsFalse() {
        // Given
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        OrderDetailPK pk2 = new OrderDetailPK(1, 2);
        
        // When
        boolean result = pk1.equals(pk2);
        
        // Then
        assertFalse(result, "Different values should not be equal");
    }
    
    /**
     * Scenario: nullとの比較
     * 
     * Given: OrderDetailPK pk1 = new OrderDetailPK(1, 1)
     * When: pk1.equals(null)を呼び出す
     * Then: falseが返される
     */
    @Test
    void testEquals_Null_ReturnsFalse() {
        // Given
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        
        // When
        boolean result = pk1.equals(null);
        
        // Then
        assertFalse(result, "Equals with null should return false");
    }
    
    /**
     * Scenario: 同じインスタンスとの比較
     * 
     * Given: OrderDetailPK pk1 = new OrderDetailPK(1, 1)
     * When: pk1.equals(pk1)を呼び出す
     * Then: trueが返される
     */
    @Test
    void testEquals_SameInstance_ReturnsTrue() {
        // Given
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        
        // When
        boolean result = pk1.equals(pk1);
        
        // Then
        assertTrue(result, "Same instance should be equal");
    }
    
    /**
     * Scenario: 異なるクラスとの比較
     * 
     * Given: OrderDetailPK pk1 = new OrderDetailPK(1, 1)
     *        Object other = new Object()
     * When: pk1.equals(other)を呼び出す
     * Then: falseが返される
     */
    @Test
    void testEquals_DifferentClass_ReturnsFalse() {
        // Given
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        Object other = new Object();
        
        // When
        boolean result = pk1.equals(other);
        
        // Then
        assertFalse(result, "Different class should not be equal");
    }
    
    /**
     * Scenario: toStringメソッドのテスト
     * 
     * Given: OrderDetailPK pk1 = new OrderDetailPK(1, 1)
     * When: pk1.toString()を呼び出す
     * Then: 文字列表現が返される
     */
    @Test
    void testToString_ReturnsStringRepresentation() {
        // Given
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        
        // When
        String result = pk1.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("1"), "String representation should contain orderTranId");
    }
}
