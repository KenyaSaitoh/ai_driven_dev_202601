# language: ja
機能: OrderDetailPK - 複合主キー

  シナリオ: 同じ値のOrderDetailPKが等しい
    前提 OrderDetailPK pk1 = new OrderDetailPK(1, 1)
    かつ OrderDetailPK pk2 = new OrderDetailPK(1, 1)
    もし pk1.equals(pk2)を呼び出す
    ならば trueが返される
    かつ pk1.hashCode() == pk2.hashCode()

  シナリオ: nullとの比較
    前提 OrderDetailPK pk1 = new OrderDetailPK(1, 1)
    もし pk1.equals(null)を呼び出す
    ならば falseが返される
