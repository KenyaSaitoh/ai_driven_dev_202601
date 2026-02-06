# language: ja
機能: OrderDetailDao - 注文明細検索

  シナリオ: 注文IDで注文明細一覧を取得
    前提 EntityManagerがモック化されている
    かつ モック設定: createQuery()が注文明細リストを返す
    かつ 注文ID=1の注文明細が3件存在する
    もし OrderDetailDao.findByOrderTranId(1)を呼び出す
    ならば 3件の注文明細が返される
    かつ 注文明細IDの昇順でソートされている
