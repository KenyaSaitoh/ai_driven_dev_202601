package pro.kensait.berrybooks.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderDetailPK単体テスト")
class OrderDetailPKTest {
    
    @Test
    @DisplayName("同じ値のOrderDetailPKが等しい（equals/hashCode）")
    void testEquals_SameValues() {
        // Given: 同じ値を持つOrderDetailPK
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        OrderDetailPK pk2 = new OrderDetailPK(1, 1);
        
        // When/Then: 検証
        assertTrue(pk1.equals(pk2));
        assertEquals(pk1.hashCode(), pk2.hashCode());
    }
    
    @Test
    @DisplayName("異なる値のOrderDetailPKが等しくない（equals）")
    void testEquals_DifferentValues() {
        // Given: 異なる値を持つOrderDetailPK
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        OrderDetailPK pk2 = new OrderDetailPK(1, 2);
        OrderDetailPK pk3 = new OrderDetailPK(2, 1);
        
        // When/Then: 検証
        assertFalse(pk1.equals(pk2));
        assertFalse(pk1.equals(pk3));
        assertFalse(pk2.equals(pk3));
    }
    
    @Test
    @DisplayName("nullとの比較（境界値）")
    void testEquals_Null() {
        // Given: OrderDetailPKとnull
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        
        // When/Then: 検証
        assertFalse(pk1.equals(null));
    }
    
    @Test
    @DisplayName("自分自身との比較（境界値）")
    void testEquals_Self() {
        // Given: OrderDetailPK
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        
        // When/Then: 検証
        assertTrue(pk1.equals(pk1));
    }
    
    @Test
    @DisplayName("異なるクラスのオブジェクトとの比較（境界値）")
    void testEquals_DifferentClass() {
        // Given: OrderDetailPKと異なるクラスのオブジェクト
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        String differentObject = "not a OrderDetailPK";
        
        // When/Then: 検証
        assertFalse(pk1.equals(differentObject));
    }
    
    @Test
    @DisplayName("OrderDetailPKの生成とゲッター（正常系）")
    void testConstructorAndGetters() {
        // Given/When: OrderDetailPKの生成
        OrderDetailPK pk = new OrderDetailPK(1, 2);
        
        // Then: 検証
        assertEquals(1, pk.getOrderTranId());
        assertEquals(2, pk.getOrderDetailId());
    }
    
    @Test
    @DisplayName("OrderDetailPKのセッター（正常系）")
    void testSetters() {
        // Given: OrderDetailPKの生成
        OrderDetailPK pk = new OrderDetailPK();
        
        // When: セッター呼び出し
        pk.setOrderTranId(10);
        pk.setOrderDetailId(20);
        
        // Then: 検証
        assertEquals(10, pk.getOrderTranId());
        assertEquals(20, pk.getOrderDetailId());
    }
}
