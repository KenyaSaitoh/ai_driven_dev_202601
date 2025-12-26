# F-001: 書籍検索・閲覧

**担当者:** 担当者A（1名）  
**推奨スキル:** JSF、JPA、CDI  
**想定工数:** 12時間  
**依存タスク:** common_tasks.md

---

## 概要

本タスクは、書籍検索・閲覧機能を実装します。カテゴリやキーワードで書籍を検索し、検索結果を一覧表示します。カバー画像の表示にも対応します。

---

## タスクリスト

### セクション1: データアクセス層

- [X] **T_F001_001**: BookDaoの作成
  - **目的**: 書籍エンティティのCRUD操作を実装する
  - **対象**: `pro.kensait.berrybooks.dao.BookDao`（DAOクラス）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_001_book_search/functional_design.md) の「6.3 データアクセス層」
  - **注意事項**: 
    - `@ApplicationScoped`を付与
    - `@PersistenceContext`で`EntityManager`をインジェクト
    - メソッド: `findAll()`（全書籍取得、書籍ID昇順）
    - メソッド: `findById(Integer bookId)`（書籍ID検索）
    - メソッド: `findByKeyword(String keyword)`（書籍名または著者でLIKE検索）
    - メソッド: `findByCategoryAndKeyword(Integer categoryId, String keyword)`（カテゴリとキーワードで検索）
    - クエリ実行時に在庫情報も`JOIN FETCH`で取得（N+1問題回避）

---

### セクション2: ビジネスロジック層

- [X] **T_F001_002**: BookServiceの作成
  - **目的**: 書籍検索のビジネスロジックを実装する
  - **対象**: `pro.kensait.berrybooks.service.book.BookService`（サービスクラス）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_001_book_search/functional_design.md) の「6.2 ビジネスロジック層」
  - **注意事項**: 
    - `@ApplicationScoped`を付与
    - `@Inject`で`BookDao`をインジェクト
    - メソッド: `searchBook(SearchParam searchParam)`
      - カテゴリIDが指定されている場合: `findByCategoryAndKeyword()`を呼び出し
      - カテゴリIDが未指定の場合: キーワードのみで`findByKeyword()`を呼び出し
      - キーワードも未指定の場合: `findAll()`を呼び出し
    - メソッド: `findBookById(Integer bookId)`（書籍詳細取得）
    - ログ出力: `INFO  [ BookService#searchBook ] categoryId={}, keyword={}`

---

### セクション3: プレゼンテーション層（DTO）

- [X] **T_F001_003**: SearchParamクラスの作成
  - **目的**: 書籍検索パラメータを保持するDTOクラスを実装する
  - **対象**: `pro.kensait.berrybooks.web.book.SearchParam`（DTOクラス）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_001_book_search/functional_design.md) の「6.1 プレゼンテーション層」
  - **注意事項**: 
    - フィールド: `categoryId`（Integer）、`keyword`（String）
    - getter/setterを実装
    - `Serializable`を実装

---

### セクション4: プレゼンテーション層（Managed Bean）

- [X] **T_F001_004**: BookSearchBeanの作成
  - **目的**: 書籍検索画面のコントローラーを実装する
  - **対象**: `pro.kensait.berrybooks.web.book.BookSearchBean`（Managed Bean）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_001_book_search/functional_design.md) の「6.1 プレゼンテーション層」
  - **注意事項**: 
    - `@Named`、`@ViewScoped`を付与
    - `Serializable`を実装
    - `@Inject`で`BookService`、`CategoryService`、`CartSession`をインジェクト
    - フィールド: `searchParam`（SearchParam）、`bookList`（List<Book>）、`categoryList`（List<Category>）
    - `@PostConstruct`メソッド: カテゴリリストを取得
    - メソッド: `search()`（書籍検索を実行、結果を`bookList`に設定）
    - メソッド: `addToCart(Book book)`（カートに追加、`CartSession.addItem()`を呼び出し）
    - ログ出力: `INFO  [ BookSearchBean#search ]`

---

### セクション5: ビュー層（XHTML）

- [X] **T_F001_005**: bookSearch.xhtmlの作成
  - **目的**: 書籍検索画面のビューを実装する
  - **対象**: `src/main/webapp/bookSearch.xhtml`（Facelets XHTML）
  - **参照SPEC**: [screen_design.md](../specs/baseline/features/F_001_book_search/screen_design.md) の「1. 書籍検索画面」
  - **注意事項**: 
    - `<h:form>`で検索フォームを作成
    - `<h:selectOneMenu>`でカテゴリ選択（`categoryList`をソース）
    - `<h:inputText>`でキーワード入力
    - `<h:commandButton>`で検索ボタン（`#{bookSearchBean.search}`アクション）
    - ヘッダーにナビゲーションメニュー（書籍検索、カート、注文履歴、ログアウト）を配置
    - CSS: `resources/css/style.css`を使用

- [X] **T_F001_006**: bookSelect.xhtmlの作成
  - **目的**: 検索結果画面のビューを実装する
  - **対象**: `src/main/webapp/bookSelect.xhtml`（Facelets XHTML）
  - **参照SPEC**: 
    - [screen_design.md](../specs/baseline/features/F_001_book_search/screen_design.md) の「2. 検索結果画面」
    - [functional_design.md](../specs/baseline/features/F_001_book_search/functional_design.md) の「3. ビジネスルール」（BR-005, BR-006, BR-007）
  - **注意事項**: 
    - `<h:dataTable>`で検索結果を表示（`#{bookSearchBean.bookList}`をソース）
    - 各行に書籍情報（カバー画像、書籍名、著者、出版社、価格、在庫状況）を表示
    - カバー画像: `<h:graphicImage value="resources/covers/#{book.imageFileName}"/>`（`book.imageFileName`は`bookName + ".jpg"`を返す）
    - 在庫0の場合: 「在庫なし」と表示、「カートに追加」ボタンを無効化
    - 在庫ありの場合: 「カートに追加」ボタンを表示（`#{bookSearchBean.addToCart(book)}`アクション）
    - 検索結果が0件の場合: 「該当する書籍がありません」メッセージ表示

---

### セクション6: テスト

- [X] **T_F001_007**: BookServiceの単体テストの作成
  - **目的**: BookServiceのビジネスロジックをテストする
  - **対象**: `src/test/java/.../service/book/BookServiceTest.java`（JUnit 5テスト）
  - **参照SPEC**: 
    - [constitution.md](../memory/constitution.md) の「原則3: テスト駆動品質」
    - [functional_design.md](../specs/baseline/features/F_001_book_search/functional_design.md) の「6.2 ビジネスロジック層」
  - **注意事項**: 
    - Mockitoで`BookDao`をモック
    - テストケース: 
      - カテゴリとキーワードで検索（正常系）
      - キーワードのみで検索（正常系）
      - 全書籍取得（正常系）
      - 検索結果0件（正常系）

- [X] **T_F001_008**: BookDaoの結合テストの作成
  - **目的**: BookDaoのクエリ実行をテストする
  - **対象**: `src/test/java/.../dao/BookDaoTest.java`（JUnit 5テスト）
  - **参照SPEC**: 
    - [constitution.md](../memory/constitution.md) の「原則3: テスト駆動品質」
    - [functional_design.md](../specs/baseline/features/F_001_book_search/functional_design.md) の「6.3 データアクセス層」
  - **注意事項**: 
    - インメモリデータベース（HSQLDB）を使用
    - テストケース: 
      - 全書籍取得（正常系）
      - 書籍ID検索（正常系）
      - キーワード検索（正常系、LIKE検索）
      - カテゴリとキーワード検索（正常系）

---

## 並行実行可能なタスク

以下のタスクは並行して実行できます：

- T_F001_001（BookDao）とT_F001_002（BookService）は順次実行（依存関係あり）
- T_F001_003（SearchParam）は他タスクと並行可能
- T_F001_004（BookSearchBean）はT_F001_002、T_F001_003完了後
- T_F001_005、T_F001_006（ビュー）はT_F001_004完了後
- T_F001_007、T_F001_008（テスト）は対応するクラス完成後

---

## 完了条件

- [X] BookDao、BookService、BookSearchBean、SearchParamが実装されている
- [X] bookSearch.xhtml、bookSelect.xhtmlが実装されている
- [X] カバー画像が正しく表示される（画像ファイル名生成ロジック: `bookName + ".jpg"`）
- [X] 在庫0の書籍は「在庫なし」と表示され、カート追加ボタンが無効化されている
- [X] 単体テストと結合テストが実装され、カバレッジ80%以上
- [X] プロジェクトがビルドでき、エラーがない
- [ ] 手動テスト: 書籍検索が正常に動作する

---

## 次のステップ

F-001完了後、他の機能タスクと並行して作業を進めてください。全機能完了後、[integration_tasks.md](integration_tasks.md) に進んでください。

