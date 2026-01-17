package pro.kensait.berrybooks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * OrderDetailPK - 注文明細の複合主キー
 * 
 * 責務:
 * - 注文明細テーブルの複合主キー
 * - ORDER_TRAN_ID（注文トランザクションID）とORDER_DETAIL_ID（注文明細ID）の組み合わせ
 * 
 * アノテーション:
 * - @Embeddable: 埋め込み可能な値オブジェクト
 * 
 * 実装:
 * - Serializable: JPAの複合主キー要件
 * - equals(), hashCode(): 複合主キーの同一性判定に必要
 */
@Embeddable
public class OrderDetailPK implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Column(name = "ORDER_TRAN_ID")
    private Integer orderTranId;
    
    @Column(name = "ORDER_DETAIL_ID")
    private Integer orderDetailId;
    
    /**
     * デフォルトコンストラクタ（JPA要件）
     */
    public OrderDetailPK() {
    }
    
    /**
     * 全引数コンストラクタ
     * 
     * @param orderTranId 注文トランザクションID
     * @param orderDetailId 注文明細ID
     */
    public OrderDetailPK(Integer orderTranId, Integer orderDetailId) {
        this.orderTranId = orderTranId;
        this.orderDetailId = orderDetailId;
    }
    
    // Getters and Setters
    
    public Integer getOrderTranId() {
        return orderTranId;
    }
    
    public void setOrderTranId(Integer orderTranId) {
        this.orderTranId = orderTranId;
    }
    
    public Integer getOrderDetailId() {
        return orderDetailId;
    }
    
    public void setOrderDetailId(Integer orderDetailId) {
        this.orderDetailId = orderDetailId;
    }
    
    /**
     * 同一性判定
     * 複合主キーの同一性判定に使用
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDetailPK that = (OrderDetailPK) o;
        return Objects.equals(orderTranId, that.orderTranId) &&
               Objects.equals(orderDetailId, that.orderDetailId);
    }
    
    /**
     * ハッシュコード計算
     * 複合主キーのハッシュコード計算に使用
     */
    @Override
    public int hashCode() {
        return Objects.hash(orderTranId, orderDetailId);
    }
}
