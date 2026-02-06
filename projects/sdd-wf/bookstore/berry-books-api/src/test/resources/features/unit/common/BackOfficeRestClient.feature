# language: ja
機能: BackOfficeRestClient - 外部API連携（書籍・在庫）

  シナリオ: 全書籍を取得
    前提 外部APIクライアントがモック化されている
    かつ モック設定: GET /booksが書籍リストを返す
    もし BackOfficeRestClient.getAllBooks()を呼び出す
    ならば 書籍リスト（List<BookTO>）が返される
    かつ 各書籍に在庫情報が含まれている

  シナリオ: 在庫を更新（楽観的ロック成功）
    前提 外部APIクライアントがモック化されている
    かつ bookId=1, quantity=8, version=1
    かつ モック設定: PUT /stocks/1が更新後の在庫情報を返す
    もし BackOfficeRestClient.updateStock(1, 8, 1L)を呼び出す
    ならば 更新後の在庫情報（StockTO）が返される
    かつ version=2に更新されている

  シナリオ: 在庫更新時に楽観的ロック失敗
    前提 外部APIクライアントがモック化されている
    かつ bookId=1, quantity=8, version=1
    かつ モック設定: PUT /stocks/1が409 Conflictを返す
    もし BackOfficeRestClient.updateStock(1, 8, 1L)を呼び出す
    ならば OptimisticLockExceptionがスローされる
