# language: ja
機能: OrderTranDao - 注文トランザクション検索

  シナリオ: 顧客IDで注文履歴を取得
    前提 EntityManagerがモック化されている
    かつ モック設定: createQuery()が注文リストを返す
    かつ 顧客ID=1の注文が2件存在する
    もし OrderTranDao.findByCustomerId(1)を呼び出す
    ならば 2件の注文が返される
    かつ 注文日の降順でソートされている

  シナリオ: 注文が存在しない顧客IDで検索
    前提 EntityManagerがモック化されている
    かつ モック設定: createQuery()が空リストを返す
    もし OrderTranDao.findByCustomerId(999)を呼び出す
    ならば 空のリストが返される
    かつ 例外はスローされない
