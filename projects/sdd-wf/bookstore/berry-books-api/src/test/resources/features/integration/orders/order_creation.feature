# language: ja
機能: 注文作成（結合テスト）
  在庫を確認して注文を作成する

  シナリオ: 注文作成（正常系）
    前提 WireMockが在庫APIをスタブする:
      | Method | Path           | Response                    |
      | GET    | /api/stocks/1  | {quantity: 10, version: 1}  |
      | PUT    | /api/stocks/1  | {quantity: 8, version: 2}   |
    もし OrderServiceで注文を作成する（customerId=1, items=[{bookId: 1, quantity: 2}]）
    ならば DBに注文が作成される
    かつ DBに注文明細が作成される
    かつ 外部APIで在庫が更新される

  シナリオ: 在庫不足で注文失敗
    前提 WireMockが在庫APIをスタブする:
      | Method | Path           | Response                   |
      | GET    | /api/stocks/1  | {quantity: 1, version: 1}  |
    もし OrderServiceで注文を作成する（customerId=1, items=[{bookId: 1, quantity: 2}]）
    ならば OutOfStockExceptionがスローされる
    かつ DBに注文は作成されない

  シナリオ: 楽観的ロック競合で注文失敗
    前提 WireMockが在庫APIをスタブする:
      | Method | Path           | Response                    |
      | GET    | /api/stocks/1  | {quantity: 10, version: 1}  |
      | PUT    | /api/stocks/1  | 409エラー（楽観的ロック競合） |
    もし OrderServiceで注文を作成する（customerId=1, items=[{bookId: 1, quantity: 2}]）
    ならば OptimisticLockExceptionがスローされる
    かつ DBに注文は作成されない（ロールバック）
