@unit @books_proxy
Feature: 書籍API連携ドメイン（単体テスト）
  BookResourceとCategoryResourceの外部API連携テスト

  Scenario: 書籍一覧を正常に取得
    Given BackOfficeRestClientがモック化されている
    And findAllBooks()が書籍リストを返す
    When BookResource.getAllBooks()を呼び出す
    Then HTTPステータス200 OKが返される
    And レスポンスボディにList<BookTO>が含まれる
    And BackOfficeRestClient.findAllBooks()が1回呼ばれる

  Scenario: 外部API呼び出し失敗時
    Given BackOfficeRestClientがモック化されている
    And findAllBooks()がRuntimeExceptionをスローする
    When BookResource.getAllBooks()を呼び出す
    Then RuntimeExceptionがスローされる

  Scenario: 書籍詳細を正常に取得
    Given BackOfficeRestClientがモック化されている
    And findBookById(1)が書籍情報を返す
    When BookResource.getBookById(1)を呼び出す
    Then HTTPステータス200 OKが返される
    And レスポンスボディにBookTOが含まれる
    And bookIdが1である

  Scenario: 書籍が存在しない場合
    Given BackOfficeRestClientがモック化されている
    And findBookById(999)がWebApplicationException（404）をスローする
    When BookResource.getBookById(999)を呼び出す
    Then WebApplicationExceptionがスローされる
    And HTTPステータス404 Not Foundが返される

  Scenario: カテゴリIDで書籍を検索
    Given BackOfficeRestClientがモック化されている
    And searchBooksJpql(1, null)が該当書籍リストを返す
    When BookResource.searchBooksJpql(categoryId=1, keyword=null)を呼び出す
    Then HTTPステータス200 OKが返される
    And レスポンスボディに該当するList<BookTO>が含まれる
    And すべての書籍のcategoryIdが1である

  Scenario: キーワードで書籍を検索
    Given BackOfficeRestClientがモック化されている
    And searchBooksJpql(null, "Java")がキーワードに一致する書籍リストを返す
    When BookResource.searchBooksJpql(categoryId=null, keyword="Java")を呼び出す
    Then HTTPステータス200 OKが返される
    And レスポンスボディにList<BookTO>が含まれる
    And 書籍名に"Java"が含まれる

  Scenario: 該当書籍がない場合
    Given BackOfficeRestClientがモック化されている
    And searchBooksJpql(null, "存在しないキーワード")が空のリストを返す
    When BookResource.searchBooksJpql(categoryId=null, keyword="存在しないキーワード")を呼び出す
    Then HTTPステータス200 OKが返される
    And レスポンスボディに空のリストが含まれる

  Scenario: カテゴリIDとキーワードの両方で検索
    Given BackOfficeRestClientがモック化されている
    And searchBooksCriteria(1, "Java")が両条件に一致する書籍リストを返す
    When BookResource.searchBooksCriteria(categoryId=1, keyword="Java")を呼び出す
    Then HTTPステータス200 OKが返される
    And レスポンスボディにList<BookTO>が含まれる
    And すべての書籍のcategoryIdが1である
    And すべての書籍名に"Java"が含まれる

  Scenario: パラメータなしで検索
    Given BackOfficeRestClientがモック化されている
    And searchBooksCriteria(null, null)が全書籍リストを返す
    When BookResource.searchBooksCriteria(categoryId=null, keyword=null)を呼び出す
    Then HTTPステータス200 OKが返される
    And レスポンスボディに全書籍のList<BookTO>が含まれる

  Scenario: カテゴリ一覧を正常に取得
    Given BackOfficeRestClientがモック化されている
    And findAllCategories()がカテゴリリストを返す
    When CategoryResource.getAllCategories()を呼び出す
    Then HTTPステータス200 OKが返される
    And レスポンスボディにMap<String, Integer>が含まれる
    And キーがカテゴリ名、値がカテゴリIDである

  Scenario: カテゴリ一覧取得で外部API呼び出し失敗時
    Given BackOfficeRestClientがモック化されている
    And findAllCategories()がRuntimeExceptionをスローする
    When CategoryResource.getAllCategories()を呼び出す
    Then RuntimeExceptionがスローされる
