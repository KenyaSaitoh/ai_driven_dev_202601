# language: ja
機能: CustomerHubRestClient - 外部API連携（顧客管理）

  シナリオ: メールアドレスで顧客を検索
    前提 外部APIクライアントがモック化されている
    かつ email="test@example.com"
    かつ モック設定: GET /customers/query_email?email=...が顧客情報を返す
    もし CustomerHubRestClient.findByEmail("test@example.com")を呼び出す
    ならば 顧客情報（CustomerTO）が返される
    かつ customerId=1
    かつ email="test@example.com"

  シナリオ: 存在しないメールアドレスで検索
    前提 外部APIクライアントがモック化されている
    かつ email="notfound@example.com"
    かつ モック設定: GET /customers/query_email?email=...が404 Not Foundを返す
    もし CustomerHubRestClient.findByEmail("notfound@example.com")を呼び出す
    ならば nullが返される

  シナリオ: 新規顧客を登録
    前提 外部APIクライアントがモック化されている
    かつ 新規顧客情報（CustomerTO）が存在する
    かつ モック設定: POST /customers/が作成された顧客情報を返す
    もし CustomerHubRestClient.register(newCustomer)を呼び出す
    ならば 作成された顧客情報（CustomerTO）が返される
    かつ customerId（自動採番）が含まれている

  シナリオ: 重複したメールアドレスで登録
    前提 外部APIクライアントがモック化されている
    かつ 重複したメールアドレスの顧客情報が存在する
    かつ モック設定: POST /customers/が409 Conflictを返す
    もし CustomerHubRestClient.register(duplicateCustomer)を呼び出す
    ならば 適切な例外がスローされる
