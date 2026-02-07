# language: ja
機能: 書籍API連携（結合テスト）
  外部API（back-office-api）から書籍一覧を取得する

  シナリオ: 書籍一覧を取得（WireMockスタブ）
    前提 WireMockが以下をスタブする:
      | Method | Path       | Response                                       |
      | GET    | /api/books | [{"bookId": 1, "bookName": "Java完全理解"}] |
    もし BackOfficeRestClientでgetAllBooks()を呼び出す
    ならば 外部APIが呼ばれる
    かつ 書籍一覧が返される

  シナリオ: 外部API呼び出し失敗
    前提 WireMockが以下をスタブする:
      | Method | Path       | Response      |
      | GET    | /api/books | 500エラー      |
    もし BackOfficeRestClientでgetAllBooks()を呼び出す
    ならば ExternalApiExceptionがスローされる
