# 書籍API タスク

**担当者:** 担当者B（1名）  
**推奨スキル:** JAX-RS、JPA、JPQL、Criteria API  
**想定工数:** 6時間  
**依存タスク:** [common_tasks.md](common_tasks.md)

---

## タスクリスト

### T_API002_001: [P] BookTOの作成

- **目的**: 書籍情報のデータ転送オブジェクトを作成する
- **対象**: BookTO.java（DTOクラス）
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「4.1 BookTO」
- **注意事項**: 
  - bookId、bookName、author、price、imageUrl、quantity、versionを含める
  - CategoryInfo（ネスト構造）を含める
  - PublisherInfo（ネスト構造）を含める

---

### T_API002_002: [P] BookServiceの作成

- **目的**: 書籍管理のビジネスロジッククラスを作成する
- **対象**: BookService.java（Serviceクラス）
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.2 Business Logic Layer」
  - [functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3. エンドポイント詳細」
- **注意事項**: 
  - `@ApplicationScoped`でCDI管理する
  - `@Transactional`でトランザクション境界を制御する
  - getBooksAll()、getBook()、searchBook()、searchBookWithCriteria()メソッドを実装する
  - BookDaoとBookDaoCriteriaを使用する

---

### T_API002_003: [P] CategoryServiceの作成

- **目的**: カテゴリ管理のビジネスロジッククラスを作成する
- **対象**: CategoryService.java（Serviceクラス）
- **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.2 Business Logic Layer」
- **注意事項**: 
  - `@ApplicationScoped`でCDI管理する
  - getCategoryMap()メソッドを実装する（Map<String, Integer>形式）
  - CategoryDaoを使用する

---

### T_API002_004: [P] BookResourceの作成

- **目的**: 書籍APIのエンドポイントを実装するResourceクラスを作成する
- **対象**: BookResource.java（JAX-RS Resourceクラス）
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「2. エンドポイント一覧」
  - [functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3. エンドポイント詳細」
- **注意事項**: 
  - `@Path("/books")`でベースパスを設定する
  - getAllBooks()、getBookById()、searchBooks()、searchBooksJpql()、searchBooksCriteria()、getAllCategories()メソッドを実装する

---

### T_API002_005: getAllBooks()メソッドの実装

- **目的**: 書籍一覧取得機能を実装する
- **対象**: BookResource#getAllBooks()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.1 書籍一覧取得」
- **注意事項**: 
  - BookServiceのgetBooksAll()を呼び出す
  - BookエンティティをBookTOに変換する
  - 論理削除された書籍（deleted=true）は除外される
  - ログ出力（INFO）を行う

---

### T_API002_006: getBookById()メソッドの実装

- **目的**: 書籍詳細取得機能を実装する
- **対象**: BookResource#getBookById()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.2 書籍詳細取得」
- **注意事項**: 
  - パスパラメータから書籍IDを取得する
  - BookServiceのgetBook()を呼び出す
  - 書籍が見つからない場合は404 Not Foundを返す
  - 論理削除された書籍も取得可能（詳細取得のみ）
  - ログ出力（INFO、WARN）を行う

---

### T_API002_007: searchBooksJpql()メソッドの実装

- **目的**: 書籍検索機能を実装する（JPQL使用）
- **対象**: BookResource#searchBooksJpql()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.4 書籍検索（JPQL）」
- **注意事項**: 
  - クエリパラメータからcategoryIdとkeywordを取得する
  - 検索条件に応じてBookServiceのメソッドを呼び分ける
    - categoryId + keyword → searchBook(categoryId, keyword)
    - categoryIdのみ → searchBook(categoryId)
    - keywordのみ → searchBook(keyword)
    - 条件なし → getBooksAll()
  - categoryId=0は全カテゴリとして扱う
  - keywordは部分一致検索（LIKE '%keyword%'）
  - ログ出力（INFO）を行う

---

### T_API002_008: searchBooksCriteria()メソッドの実装

- **目的**: 書籍検索機能を実装する（Criteria API使用）
- **対象**: BookResource#searchBooksCriteria()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.5 書籍検索（Criteria API）」
- **注意事項**: 
  - **重要**: Criteria APIで動的クエリを構築する
  - クエリパラメータからcategoryIdとkeywordを取得する
  - BookServiceのsearchBookWithCriteria()を呼び出す
  - カテゴリIDとキーワードの有無に応じて動的に条件を追加する
  - タイプセーフなクエリ構築を行う
  - ログ出力（INFO）を行う

---

### T_API002_009: searchBooks()メソッドの実装（デフォルト）

- **目的**: 書籍検索機能を実装する（デフォルト: JPQL使用）
- **対象**: BookResource#searchBooks()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.3 書籍検索（デフォルト）」
- **注意事項**: 
  - 内部的にsearchBooksJpql()を呼び出す
  - デフォルトの検索エンドポイントとして提供する

---

### T_API002_010: getAllCategories()メソッドの実装

- **目的**: カテゴリ一覧取得機能を実装する（Map形式）
- **対象**: BookResource#getAllCategories()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「3.6 カテゴリ一覧取得（書籍API経由）」
- **注意事項**: 
  - CategoryServiceのgetCategoryMap()を呼び出す
  - Map<String, Integer>形式でレスポンスを返す（カテゴリ名 → カテゴリID）
  - 後方互換性のために提供される（将来的には/api/categoriesの使用を推奨）
  - ログ出力（INFO）を行う

---

### T_API002_011: [P] 書籍API単体テストの作成

- **目的**: 書籍APIの単体テストを作成する
- **対象**: BookResourceTest.java（テストクラス）
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「8. テスト仕様」
  - [functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「11. 動的振る舞い」
- **注意事項**: 
  - 正常系: 全件取得 → 200 OK + 全書籍リスト
  - 正常系: 詳細取得（存在） → 200 OK + 書籍詳細
  - 正常系: 検索（カテゴリ+キーワード） → 200 OK + フィルタされた書籍リスト
  - 正常系: 検索（カテゴリのみ） → 200 OK + カテゴリ1の書籍リスト
  - 正常系: 検索（キーワードのみ） → 200 OK + キーワードマッチの書籍リスト
  - 正常系: 検索（条件なし） → 200 OK + 全書籍リスト
  - 正常系: カテゴリ一覧 → 200 OK + カテゴリMap
  - 異常系: 詳細取得（存在しない） → 404 Not Found
  - 異常系: 不正なID形式 → 400 Bad Request
  - JPQL vs Criteria API比較テスト: 同一条件で同じ結果が返ることを確認

---

## API実装完了チェックリスト

- [ ] BookTOが作成された
- [ ] BookServiceが作成された
  - [ ] getBooksAll()が実装された
  - [ ] getBook()が実装された
  - [ ] searchBook()（複数オーバーロード）が実装された
  - [ ] searchBookWithCriteria()が実装された
- [ ] CategoryServiceが作成された
  - [ ] getCategoryMap()が実装された
- [ ] BookResourceが作成された
- [ ] getAllBooks()メソッドが実装された
  - [ ] 論理削除された書籍の除外
  - [ ] ログ出力が実装された
- [ ] getBookById()メソッドが実装された
  - [ ] 404エラーハンドリング
  - [ ] ログ出力が実装された
- [ ] searchBooksJpql()メソッドが実装された
  - [ ] 検索条件の分岐処理
  - [ ] JPQL検索の実装
  - [ ] ログ出力が実装された
- [ ] searchBooksCriteria()メソッドが実装された
  - [ ] Criteria API動的クエリ構築
  - [ ] ログ出力が実装された
- [ ] searchBooks()メソッドが実装された（デフォルト）
- [ ] getAllCategories()メソッドが実装された
  - [ ] Map形式のレスポンス
  - [ ] ログ出力が実装された
- [ ] 書籍API単体テストが作成された
  - [ ] 正常系テストが実装された
  - [ ] 異常系テストが実装された
  - [ ] JPQL vs Criteria API比較テストが実装された

---

## 次のステップ

書籍APIが完了したら、他のAPI実装タスクと並行して進めることができます：
- [認証API](API_001_auth.md)
- [カテゴリAPI](API_003_categories.md)
- [出版社API](API_004_publishers.md)
- [在庫API](API_005_stocks.md)
- [ワークフローAPI](API_006_workflows.md)

すべてのAPI実装が完了したら、[結合テスト](integration_tasks.md)に進んでください。
