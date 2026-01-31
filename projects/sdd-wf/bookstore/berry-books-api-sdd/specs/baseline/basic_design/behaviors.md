# Service層以下 - 結合テスト仕様書

プロジェクトID: berry-books-api  
バージョン: 2.0.0  
最終更新日: 2025-01-18  
ステータス: Service層結合テスト仕様確定

---

## 1. 概要

本文書は、berry-books-api REST APIのService層以下（Service + DAO + Entity + DB）の結合テスト仕様を記述する。API層（Resource）は含まず、ビジネスロジック層とデータアクセス層の連携をテストする。

**テスト対象:**
- Service層のビジネスロジック
- DAO層のデータアクセス（存在する場合）
- Entity（JPA）のマッピング
- 実際のDB操作（メモリDB）
- 外部API呼び出し（WireMockでスタブ化）

**テスト対象外:**
- API層（Resource、JAX-RS）
- HTTPリクエスト/レスポンス
- 認証・認可（JWT、Cookie）

---

## 2. Service層のビジネスロジックシナリオ

### 2.1 AuthService - 認証サービス

#### シナリオ: 顧客認証（BCryptパスワード検証）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| DBに顧客が存在:<br/>- email="alice@gmail.com"<br/>- password="$2a$10$..." (BCrypt) | AuthService.authenticate(email="alice@gmail.com", password="password123") | 認証成功<br/>Customerエンティティが返される |

#### シナリオ: 認証失敗（パスワード不一致）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| DBに顧客が存在:<br/>- email="alice@gmail.com"<br/>- password="$2a$10$..." | AuthService.authenticate(email="alice@gmail.com", password="wrongpassword") | AuthenticationExceptionがスローされる |

#### シナリオ: 認証失敗（顧客が存在しない）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| DBに顧客が存在しない | AuthService.authenticate(email="notfound@example.com", password="password123") | AuthenticationExceptionがスローされる |

#### シナリオ: 平文パスワード認証（開発環境用）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| DBに顧客が存在:<br/>- email="bob@gmail.com"<br/>- password="password" (平文) | AuthService.authenticate(email="bob@gmail.com", password="password") | 認証成功<br/>平文比較で照合される |

### 2.2 BookService - 書籍サービス

#### シナリオ: 外部書籍APIから書籍情報を取得（WireMock）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが以下をスタブ:<br/>- GET /api/books/1<br/>- Response: {bookId: 1, bookName: "Java完全理解", price: 3200} | BookService.getBookFromBackOffice(bookId=1) | 外部APIが呼ばれる<br/>書籍情報が取得される |

#### シナリオ: 複数書籍検索（外部API）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが以下をスタブ:<br/>- GET /api/books/search?categoryId=1<br/>- Response: [{bookId: 1, ...}, {bookId: 2, ...}] | BookService.searchBooks(categoryId=1, keyword=null) | 外部APIが呼ばれる<br/>書籍リストが取得される |

#### シナリオ: 外部API呼び出しエラー（404 Not Found）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが以下をスタブ:<br/>- GET /api/books/999<br/>- Response: 404 Not Found | BookService.getBookFromBackOffice(bookId=999) | ExternalApiExceptionがスローされる<br/>"書籍が見つかりません" |

### 2.3 OrderService - 注文サービス

#### シナリオ: 注文作成（外部在庫API連携）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| 顧客ID=1が存在<br/>WireMockが以下をスタブ:<br/>- PUT /api/stocks/1<br/>- Response: {quantity: 8, version: 1} | OrderService.createOrder(customerId=1, items=[{bookId:1, quantity:2}]) | 1. OrderTranエンティティが作成される<br/>2. OrderDetailエンティティが作成される<br/>3. 外部在庫APIが呼ばれる<br/>4. OrderTranのstateが"CONFIRMED"になる |

#### シナリオ: 注文作成（在庫不足エラー）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが以下をスタブ:<br/>- PUT /api/stocks/1<br/>- Response: 400 Bad Request, {message: "在庫不足"} | OrderService.createOrder(customerId=1, items=[{bookId:1, quantity:100}]) | OutOfStockExceptionがスローされる<br/>OrderTranは作成されない（ロールバック） |

#### シナリオ: 注文一覧取得（顧客別）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| DBに以下の注文が存在:<br/>- OrderTran(id=1, customerId=1)<br/>- OrderTran(id=2, customerId=1)<br/>- OrderTran(id=3, customerId=2) | OrderService.findOrdersByCustomer(customerId=1) | 顧客ID=1の注文2件が取得される |

#### シナリオ: 注文詳細取得（OrderDetail含む）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| OrderTran(id=1, customerId=1)<br/>OrderDetail(id=1, tranId=1, bookId=1, quantity=2)<br/>OrderDetail(id=2, tranId=1, bookId=2, quantity=1) | OrderService.getOrderDetail(tranId=1) | OrderTranエンティティが取得され、<br/>orderTran.details.size() == 2<br/>（Lazy Loadingで詳細も取得） |

### 2.4 ImageService - 画像サービス

#### シナリオ: 外部画像APIから画像情報を取得

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが以下をスタブ:<br/>- GET /api/images/book/1<br/>- Response: {imageId: 1, url: "https://...", contentType: "image/jpeg"} | ImageService.getBookImage(bookId=1) | 外部APIが呼ばれる<br/>画像情報が取得される |

#### シナリオ: 外部画像API（画像が存在しない）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが以下をスタブ:<br/>- GET /api/images/book/999<br/>- Response: 404 Not Found | ImageService.getBookImage(bookId=999) | nullが返される<br/>（画像がない場合はnullを許容） |

---

## 3. トランザクション管理シナリオ

### 3.1 トランザクションロールバック（注文作成失敗）

#### シナリオ: 外部API呼び出し失敗時のロールバック

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが在庫API失敗を返す:<br/>- PUT /api/stocks/1<br/>- Response: 500 Internal Server Error | OrderService.createOrder(customerId=1, items=[{bookId:1, quantity:2}]) 内で:<br/>1. OrderTran作成<br/>2. OrderDetail作成<br/>3. 外部API呼び出し → 失敗 | トランザクション全体がロールバックされる:<br/>- OrderTranは作成されない<br/>- OrderDetailも作成されない |

### 3.2 複数エンティティの整合性

#### シナリオ: 注文作成時の関連エンティティ更新

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| 顧客ID=1が存在<br/>外部在庫APIが成功 | OrderService.createOrder(customerId=1, items=[{bookId:1, quantity:2}, {bookId:2, quantity:1}]) | 1トランザクションで以下が作成される:<br/>- OrderTran 1件<br/>- OrderDetail 2件<br/>すべてコミットされる |

---

## 4. 外部API連携シナリオ（WireMock）

### 4.1 back-office-api 連携

#### シナリオ: 書籍API（GET /api/books/:id）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが以下をスタブ:<br/>- GET /api/books/1<br/>- Response: 200 OK, {bookId: 1, bookName: "Java完全理解", price: 3200, categoryId: 1} | BookService.getBookFromBackOffice(bookId=1) | 外部APIが呼ばれる:<br/>- URL: http://localhost:8089/api/books/1<br/>- Method: GET<br/>レスポンスが正しくパースされる |

#### シナリオ: カテゴリAPI（GET /api/categories）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが以下をスタブ:<br/>- GET /api/categories<br/>- Response: [{categoryId: 1, categoryName: "プログラミング"}, ...] | BookService.getAllCategories() | 外部APIが呼ばれる<br/>カテゴリリストが取得される |

#### シナリオ: 在庫API（PUT /api/stocks/:id）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが以下をスタブ:<br/>- PUT /api/stocks/1<br/>- Request Body: {quantity: 8, version: 1}<br/>- Response: 200 OK, {quantity: 8, version: 2} | OrderService.updateStock(bookId=1, quantity=8, version=1) | 外部APIが呼ばれる:<br/>- URL: http://localhost:8089/api/stocks/1<br/>- Method: PUT<br/>- Body: {quantity: 8, version: 1} |

#### シナリオ: 出版社API（GET /api/publishers）

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが以下をスタブ:<br/>- GET /api/publishers<br/>- Response: [{publisherId: 1, publisherName: "技術評論社"}, ...] | BookService.getAllPublishers() | 外部APIが呼ばれる<br/>出版社リストが取得される |

### 4.2 外部APIエラーハンドリング

#### シナリオ: 外部APIタイムアウト

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが5秒遅延を設定:<br/>- GET /api/books/1<br/>- Fixed Delay: 5000ms | BookService.getBookFromBackOffice(bookId=1) | TimeoutExceptionがスローされる<br/>（タイムアウト設定: 3秒） |

#### シナリオ: 外部API 401 Unauthorized

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが以下をスタブ:<br/>- GET /api/books/1<br/>- Response: 401 Unauthorized | BookService.getBookFromBackOffice(bookId=1) | UnauthorizedExceptionがスローされる |

---

## 5. エンティティリレーションシナリオ

### 5.1 OrderTran → OrderDetail (OneToMany)

#### シナリオ: 注文詳細の遅延ロード

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| OrderTran(id=1)<br/>OrderDetail(id=1, tranId=1)<br/>OrderDetail(id=2, tranId=1) | OrderService.getOrderDetail(tranId=1) で:<br/>orderTran.getDetails() にアクセス | Lazy Loadingで詳細が取得される:<br/>orderTran.details.size() == 2 |

#### シナリオ: Cascade.ALL による自動削除

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| OrderTran(id=1)<br/>OrderDetail(id=1, tranId=1)<br/>OrderDetail(id=2, tranId=1) | OrderService.deleteOrder(tranId=1) | OrderTranが削除されると、<br/>関連するOrderDetailも自動削除される |

---

## 6. バリデーションシナリオ

### 6.1 Bean Validation

#### シナリオ: OrderTran のバリデーション

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| OrderTran(customerId=null, totalPrice=-100) | OrderService.createOrder(orderTran) | ConstraintViolationExceptionがスローされる:<br/>- customerId: "must not be null"<br/>- totalPrice: "must be greater than or equal to 0" |

#### シナリオ: OrderDetail のバリデーション

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| OrderDetail(bookId=null, quantity=0) | 注文詳細を作成 | ConstraintViolationExceptionがスローされる:<br/>- bookId: "must not be null"<br/>- quantity: "must be greater than 0" |

### 6.2 ビジネスルールバリデーション

#### シナリオ: 注文金額の妥当性チェック

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| 書籍A(price=3200) × 2 = 6400円<br/>配送料800円 | OrderService.createOrder() で:<br/>totalPrice=5000を指定 | BusinessRuleViolationExceptionがスローされる:<br/>"注文金額が正しくありません" |

---

## 7. 性能関連シナリオ

### 7.1 N+1問題の回避

#### シナリオ: JOIN FETCHによる一括取得

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| OrderTran 100件、各OrderTranに複数のOrderDetail | OrderService.findAllOrders() で:<br/>JOIN FETCH使用 | SQLクエリが最小限で実行される:<br/>"SELECT ot, od FROM OrderTran ot LEFT JOIN FETCH ot.details od"<br/>N+1問題が発生しない |

---

## 8. テストデータ準備

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

## 9. WireMockスタブ設定例

### 9.1 書籍API（成功）

```java
@BeforeEach
void setupWireMock() {
    // 書籍API
    stubFor(get(urlEqualTo("/api/books/1"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"bookId\": 1, \"bookName\": \"Java完全理解\", \"price\": 3200}")));
    
    // 在庫API
    stubFor(put(urlEqualTo("/api/stocks/1"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"bookId\": 1, \"quantity\": 8, \"version\": 2}")));
}
```

### 9.2 在庫API（エラー）

```java
stubFor(put(urlEqualTo("/api/stocks/1"))
    .willReturn(aResponse()
        .withStatus(400)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"message\": \"在庫不足\"}")));
```

---

## 10. テスト実行コマンド

```bash
# 結合テストのみ実行
./gradlew :berry-books-api-sdd:integrationTest

# 単体テスト→結合テスト→E2Eテストの順に実行
./gradlew :berry-books-api-sdd:test integrationTest e2eTest
```

---

## 11. 参考情報

- **requirements/behaviors.md**: E2Eテスト用の受入基準（API層を含む全体フロー）
- **basic_design/functional_design.md**: 機能設計書
- **basic_design/data_model.md**: データモデル仕様書
- **basic_design/external_interface.md**: 外部API仕様書
- **agent_skills/jakarta-ee-api-base/instructions/it_generation.md**: 結合テスト生成指示書
