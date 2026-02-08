package pro.kensait.berrybooks.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderDetailPKの単体テスト
 */
class OrderDetailPKTest {
    
    @Test
    void testEquals_SameValues() {
        // Given: 同じ値の複合主キー
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        OrderDetailPK pk2 = new OrderDetailPK(1, 1);
        
        // When & Then: equalsがtrueを返す
        assertEquals(pk1, pk2);
        assertTrue(pk1.equals(pk2));
    }
    
    @Test
    void testEquals_DifferentValues() {
        // Given: 異なる値の複合主キー
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        OrderDetailPK pk2 = new OrderDetailPK(1, 2);
        
        // When & Then: equalsがfalseを返す
        assertNotEquals(pk1, pk2);
        assertFalse(pk1.equals(pk2));
    }
    
    @Test
    void testEquals_SameObject() {
        // Given: 同一オブジェクト
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        
        // When & Then: equalsがtrueを返す
        assertEquals(pk1, pk1);
        assertTrue(pk1.equals(pk1));
    }
    
    @Test
    void testEquals_Null() {
        // Given: 複合主キーとnull
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        
        // When & Then: equalsがfalseを返す
        assertFalse(pk1.equals(null));
    }
    
    @Test
    void testEquals_DifferentClass() {
        // Given: 複合主キーと異なるクラスのオブジェクト
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        String other = "not a pk";
        
        // When & Then: equalsがfalseを返す
        assertFalse(pk1.equals(other));
    }
    
    @Test
    void testHashCode_SameValues() {
        // Given: 同じ値の複合主キー
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        OrderDetailPK pk2 = new OrderDetailPK(1, 1);
        
        // When & Then: 同じハッシュコードを返す
        assertEquals(pk1.hashCode(), pk2.hashCode());
    }
    
    @Test
    void testHashCode_DifferentValues() {
        // Given: 異なる値の複合主キー
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        OrderDetailPK pk2 = new OrderDetailPK(1, 2);
        
        // When & Then: 異なるハッシュコードを返す（通常は）
        // 注: ハッシュコードは必ずしも異なるとは限らないが、通常は異なる
        assertNotEquals(pk1.hashCode(), pk2.hashCode());
    }
    
    @Test
    void testGettersAndSetters() {
        // Given: 複合主キー
        OrderDetailPK pk = new OrderDetailPK();
        
        // When: setterを呼び出す
        pk.setOrderTranId(10);
        pk.setOrderDetailId(20);
        
        // Then: getterが正しい値を返す
        assertEquals(10, pk.getOrderTranId());
        assertEquals(20, pk.getOrderDetailId());
    }
    
    @Test
    void testConstructor_WithParameters() {
        // When: パラメータ付きコンストラクタで生成
        OrderDetailPK pk = new OrderDetailPK(5, 15);
        
        // Then: 値が正しく設定される
        assertEquals(5, pk.getOrderTranId());
        assertEquals(15, pk.getOrderDetailId());
    }
    
    @Test
    void testConstructor_NoParameters() {
        // When: デフォルトコンストラクタで生成
        OrderDetailPK pk = new OrderDetailPK();
        
        // Then: 値はnull
        assertNull(pk.getOrderTranId());
        assertNull(pk.getOrderDetailId());
    }
}