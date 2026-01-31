# Service層以下 - 結合テスト仕様書

システム名: 書店バックオフィスAPI  
バージョン: 2.0.0  
最終更新日: 2025-01-18

---

## 1. 概要

本文書は、書店バックオフィスAPIシステムのService層以下（Service + DAO + Entity + DB）の結合テスト仕様を記述する。API層（Resource）は含まず、ビジネスロジック層とデータアクセス層の連携をテストする。

**テスト対象:**
- Service層のビジネスロジック
- DAO層のデータアクセス
- Entity（JPA）のマッピング
- 実際のDB操作（メモリDB）
- 外部API呼び出し（WireMockでスタブ化）

**テスト対象外:**
- API層（Resource、JAX-RS）
- HTTPリクエスト/レスポンス
- 認証・認可（JWT、Cookie）

---

## 2. Service層のビジネスロジックシナリオ

### 2.1 BookService - 書籍検索

#### シナリオ: カテゴリで書籍を検索

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| DBに以下の書籍が存在:<br/>- bookId=1, bookName="Java完全理解", categoryId=1<br/>- bookId=2, bookName="Spring入門", categoryId=1<br/>- bookId=3, bookName="文学作品", categoryId=2 | BookService.searchBooks(categoryId=1, keyword=null) | カテゴリID=1の書籍2件が取得される<br/>- "Java完全理解"<br/>- "Spring入門" |

#### シナリオ: キーワードで書籍を検索

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| DBに書籍が存在:<br/>- bookId=1, bookName="Java完全理解"<br/>- bookId=2, bookName="JavaScript入門" | BookService.searchBooks(categoryId=null, keyword="Java") | "Java"を含む書籍2件が取得される |

#### シナリオ: 書籍詳細を取得（カテゴリ・出版社・在庫を含む）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| DBに書籍とリレーションデータが存在:<br/>- bookId=1, categoryId=1, publisherId=1<br/>- Category(id=1, name="プログラミング")<br/>- Publisher(id=1, name="技術評論社")<br/>- Stock(bookId=1, quantity=10) | BookService.getBookDetail(bookId=1) | 書籍詳細が取得され、以下が含まれる:<br/>- book.bookName="Java完全理解"<br/>- book.category.categoryName="プログラミング"<br/>- book.publisher.publisherName="技術評論社"<br/>- book.stock.quantity=10 |

### 2.2 StockService - 在庫管理

#### シナリオ: 在庫数を更新（正常系）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| DBに在庫が存在:<br/>- Stock(bookId=1, quantity=10, version=1) | StockService.updateStock(bookId=1, quantity=15, version=1) | DBの在庫が更新される:<br/>- quantity=15<br/>- version=2（楽観的ロックバージョンアップ） |

#### シナリオ: 楽観的ロック競合検知

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| DBに在庫が存在:<br/>- Stock(bookId=1, quantity=10, version=2) | StockService.updateStock(bookId=1, quantity=15, version=1)<br/>（古いバージョンで更新試行） | OptimisticLockExceptionがスローされる<br/>DBの在庫は更新されない |

#### シナリオ: 外部在庫API連携（WireMockスタブ）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが以下をスタブ:<br/>- PUT /api/stocks/1<br/>- Response: {quantity: 15, version: 2} | StockService.syncWithExternalStock(bookId=1) | 外部APIが呼ばれる<br/>レスポンスに基づきDBが更新される |

### 2.3 EmployeeService - 社員管理

#### シナリオ: 社員認証（パスワード検証）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| DBに社員が存在:<br/>- employeeCode="EMP001"<br/>- password="$2a$10$..." (BCrypt) | EmployeeService.authenticate(code="EMP001", password="password123") | 認証成功<br/>Employeeエンティティが返される |

#### シナリオ: 認証失敗（パスワード不一致）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| DBに社員が存在:<br/>- employeeCode="EMP001"<br/>- password="$2a$10$..." | EmployeeService.authenticate(code="EMP001", password="wrongpassword") | AuthenticationExceptionがスローされる |

### 2.4 WorkflowService - ワークフロー管理

#### シナリオ: ワークフロー作成

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| 社員ID=1（一般社員）が存在 | WorkflowService.createWorkflow(workflowType="ADD_NEW_BOOK", operatedBy=1) | Workflowエンティティが作成される:<br/>- state="DRAFT"<br/>- operatedBy=1 |

#### シナリオ: ワークフロー申請

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Workflow(id=1, state="DRAFT") | WorkflowService.applyWorkflow(workflowId=1, operatedBy=1) | 状態が更新される:<br/>- state="APPLIED"<br/>- appliedAt=現在時刻 |

#### シナリオ: ワークフロー承認（管理職のみ）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Workflow(id=1, state="APPLIED")<br/>社員ID=2（管理職、role="MANAGER"） | WorkflowService.approveWorkflow(workflowId=1, operatedBy=2) | 状態が更新される:<br/>- state="APPROVED"<br/>- approvedAt=現在時刻<br/>書籍マスタに反映される |

#### シナリオ: 承認権限チェック（一般社員は承認不可）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Workflow(id=1, state="APPLIED")<br/>社員ID=1（一般社員、role="ASSOCIATE"） | WorkflowService.approveWorkflow(workflowId=1, operatedBy=1) | ForbiddenExceptionがスローされる<br/>状態は変更されない |

---

## 3. DAO層のデータアクセスシナリオ

### 3.1 BookDao - JPQL動的クエリ

#### シナリオ: 条件付き検索（JPQL）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| DBに書籍が複数存在 | BookDao.searchByJpql(categoryId=1, keyword="Java") | JPQLクエリが正しく生成・実行される:<br/>"SELECT b FROM Book b WHERE b.categoryId = :categoryId AND b.bookName LIKE :keyword" |

### 3.2 StockDao - 楽観的ロック

#### シナリオ: @Versionフィールドによる楽観的ロック

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Stock(id=1, quantity=10, version=1) | StockDao.update(stock) でquantity=15に変更<br/>ただし、別トランザクションで既にversion=2に更新済み | OptimisticLockExceptionがスローされる |

### 3.3 WorkflowDao - Criteria API

#### シナリオ: 複雑な条件検索（Criteria API）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Workflow複数件が存在:<br/>- state="APPLIED", "APPROVED", "REJECTED" | WorkflowDao.searchByCriteria(state="APPLIED", operatedBy=1) | Criteria APIで動的クエリが生成され、条件に合致するWorkflowが取得される |

---

## 4. トランザクション管理シナリオ

### 4.1 トランザクションロールバック

#### シナリオ: 例外発生時のロールバック

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Book(id=1, quantity=10) | WorkflowService.approveWorkflow() 内で:<br/>1. Workflowのstate更新<br/>2. Bookの作成<br/>3. 例外発生（ValidationException） | トランザクション全体がロールバックされる:<br/>- Workflowのstateは変更されない<br/>- Bookは作成されない |

### 4.2 複数エンティティの整合性

#### シナリオ: 関連エンティティの同時更新

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Workflow(id=1, state="APPLIED")<br/>Book(id=1) 存在 | WorkflowService.approveWorkflow(workflowId=1) で:<br/>1. Workflow.state="APPROVED"<br/>2. Book作成<br/>3. WorkflowDetail更新 | すべての更新が1トランザクションでコミットされる |

---

## 5. 外部API連携シナリオ（WireMock）

### 5.1 在庫API呼び出し

#### シナリオ: 外部在庫APIへのPUTリクエスト

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが以下をスタブ:<br/>- PUT /api/stocks/1<br/>- Request Body: {quantity: 15, version: 1}<br/>- Response: 200 OK, {quantity: 15, version: 2} | StockService.updateStock(bookId=1, quantity=15, version=1) | 外部APIが呼ばれる:<br/>- URL: http://localhost:8089/api/stocks/1<br/>- Method: PUT<br/>- Body: {quantity: 15, version: 1}<br/>レスポンスが正しく処理される |

#### シナリオ: 外部API呼び出しエラー（400 Bad Request）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが以下をスタブ:<br/>- PUT /api/stocks/1<br/>- Response: 400 Bad Request, {message: "在庫不足"} | StockService.updateStock(bookId=1, quantity=100, version=1) | ExternalApiExceptionがスローされる<br/>エラーメッセージ: "在庫不足" |

#### シナリオ: 外部APIタイムアウト

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが5秒遅延を設定:<br/>- PUT /api/stocks/1<br/>- Fixed Delay: 5000ms | StockService.updateStock(bookId=1, quantity=15, version=1) | TimeoutExceptionがスローされる<br/>（タイムアウト設定: 3秒） |

---

## 6. バリデーションシナリオ

### 6.1 Bean Validation

#### シナリオ: エンティティのバリデーション

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Book(bookName="", price=-100) | BookService.createBook(book) | ConstraintViolationExceptionがスローされる:<br/>- bookName: "must not be blank"<br/>- price: "must be greater than or equal to 0" |

### 6.2 ビジネスルールバリデーション

#### シナリオ: 在庫数の妥当性チェック

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Stock(bookId=1, quantity=10) | StockService.updateStock(bookId=1, quantity=-5, version=1) | BusinessRuleViolationExceptionがスローされる:<br/>"在庫数は0以上である必要があります" |

---

## 7. エンティティリレーションシナリオ

### 7.1 OneToMany / ManyToOne

#### シナリオ: Book → Category (ManyToOne)

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Book(id=1, categoryId=1)<br/>Category(id=1, name="プログラミング") | BookDao.findById(1) | Bookエンティティが取得され、<br/>book.category.categoryName="プログラミング"<br/>（Lazy Loadingでカテゴリも取得） |

#### シナリオ: Category → Books (OneToMany)

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Category(id=1)<br/>Book(id=1, categoryId=1)<br/>Book(id=2, categoryId=1) | CategoryDao.findById(1) | Categoryエンティティが取得され、<br/>category.books.size() == 2<br/>（Lazy Loadingで書籍リストも取得） |

---

## 8. 性能関連シナリオ

### 8.1 N+1問題の回避

#### シナリオ: JOIN FETCHによる一括取得

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Book 100件、各Bookに1つのCategory | BookDao.findAllWithCategory()<br/>（JOIN FETCH b.category） | SQLクエリが1回のみ実行される:<br/>"SELECT b, c FROM Book b JOIN FETCH b.category c"<br/>N+1問題が発生しない |

### 8.2 ページネーション

#### シナリオ: 大量データの分割取得

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Book 1000件が存在 | BookDao.findAll(page=0, size=20) | 20件のみ取得される<br/>OFFSET 0 LIMIT 20 |

---

## 9. テストデータ準備

各テストケースでは、以下の方針でテストデータを準備する：

1. **@BeforeEach で初期データ投入**
   - EntityManagerを使用してエンティティを永続化
   - em.flush()で強制的にDBに反映

2. **@AfterEach でロールバック**
   - トランザクションロールバックで自動クリーンアップ
   - 次のテストへの影響を防ぐ

3. **テストデータの一意性**
   - UUIDやタイムスタンプを使用して一意なデータを生成
   - テスト間の干渉を防ぐ

---

## 10. WireMockスタブ設定例

### 10.1 在庫API（成功）

```java
@BeforeEach
void setupWireMock() {
    stubFor(put(urlEqualTo("/api/stocks/1"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"bookId\": 1, \"quantity\": 15, \"version\": 2}")));
}
```

### 10.2 在庫API（エラー）

```java
stubFor(put(urlEqualTo("/api/stocks/1"))
    .willReturn(aResponse()
        .withStatus(400)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"message\": \"在庫不足\"}")));
```

---

## 11. テスト実行コマンド

```bash
# 結合テストのみ実行
./gradlew :back-office-api-sdd:integrationTest

# 単体テスト→結合テスト→E2Eテストの順に実行
./gradlew :back-office-api-sdd:test integrationTest e2eTest
```

---

## 12. 参考情報

- **requirements/behaviors.md**: E2Eテスト用の受入基準（API層を含む全体フロー）
- **basic_design/functional_design.md**: 機能設計書
- **basic_design/data_model.md**: データモデル仕様書
- **agent_skills/jakarta-ee-api-base/instructions/it_generation.md**: 結合テスト生成指示書
