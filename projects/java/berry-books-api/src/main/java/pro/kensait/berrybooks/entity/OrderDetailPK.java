package pro.kensait.berrybooks.entity;

import java.io.Serializable;

// 注文明細の複合主キークラス
public class OrderDetailPK implements Serializable {
    // 注文ID
    private Integer orderTranId;

    // 注文明細ID
    private Integer orderDetailId;

    // 引数なしのコンストラクタ
    public OrderDetailPK() {
    }

    // コンストラクタ
    public OrderDetailPK(int orderTranId, int orderDetailId) {
        this.orderTranId = orderTranId;
        this.orderDetailId = orderDetailId;
    }

    // アクセサメソッド
    public Integer getOrderTranId() {
        return orderTranId;
    }

    public Integer getOrderDetailId() {
        return orderDetailId;
    }

    // 一意性を保証するために、必ずequalsメソッドをオーバーライドする
    public boolean equals(Object obj) {
        return ((obj instanceof OrderDetailPK) &&
                orderTranId == (((OrderDetailPK)obj).getOrderTranId()) &&
                orderDetailId == (((OrderDetailPK)obj).getOrderDetailId()));
    }

    // equalsメソッドに合わせて、hashcodeメソッドもオーバーライドする
    public int hashCode() {
        return orderTranId + orderDetailId;
    }
}