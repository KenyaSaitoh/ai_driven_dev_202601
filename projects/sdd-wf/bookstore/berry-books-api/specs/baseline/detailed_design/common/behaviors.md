# common - 振る舞い仕様書（単体テスト用）

ドメイン名: common（共通ドメイン）  
バージョン: 1.0.0  
最終更新日: 2026-02-06

記法: シナリオは Gherkin 記法で記述する（Feature, Scenario, Given, When, Then, And, But）。principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照すること。

---

## 1. 概要

本文書は、commonドメインの単体テスト用の振る舞い、テストシナリオ、受入基準を記述する。

**テスト対象**:
* Entity（OrderTran、OrderDetail、OrderDetailPK）
* DAO（OrderTranDao、OrderDetailDao）
* Security（JwtUtil、JwtAuthenFilter、AuthenInfo）
* 外部API連携（BackOfficeRestClient、CustomerHubRestClient）
* Exception Mapper

**単体テストの範囲**:
* commonドメイン内の機能をテスト
* commonドメイン内のコンポーネント間は実際の連携をテスト可能
* commonドメイン外の依存（EntityManager、外部API等）はモック化
* 結合テストシナリオは ../../basic_design/common/behaviors.md を参照すること
* E2Eテストシナリオは ../../requirements/behaviors.md を参照すること

**関連ドキュメント**:
* [detailed_design.md](detailed_design.md) - 詳細設計書
* [../../basic_design/common/functional_design.md](../../basic_design/common/functional_design.md) - 共通機能設計書
* [../../basic_design/common/behaviors.md](../../basic_design/common/behaviors.md) - 共通機能振る舞い仕様書（結合テスト用）

---

## 2. テストシナリオ

### 2.1 OrderTranDao - 注文検索（正常系）

#### Feature: 注文トランザクション検索

#### Scenario: 顧客IDで注文履歴を取得

* Given（前提条件）:
  * EntityManagerがモック化されている
  * モック設定: `createQuery()`が注文リストを返す
  * 顧客ID=1の注文が2件存在する

* When（操作）:
  * `OrderTranDao.findByCustomerId(1)`を呼び出す

* Then（期待結果）:
  * 2件の注文が返される
  * 注文日の降順でソートされている

#### テストデータ
* 入力:
  ```
  customerId: 1
  ```
* 期待される出力:
  ```
  List<OrderTran> size: 2
  orderTranId: [2, 1]
  orderDate: [2026-01-02, 2026-01-01]
  ```

---

### 2.2 OrderTranDao - 注文検索（境界値：注文なし）

#### Feature: 注文トランザクション検索

#### Scenario: 注文が存在しない顧客IDで検索

* Given（前提条件）:
  * EntityManagerがモック化されている
  * モック設定: `createQuery()`が空リストを返す

* When（操作）:
  * `OrderTranDao.findByCustomerId(999)`を呼び出す

* Then（期待結果）:
  * 空のリストが返される
  * 例外はスローされない

---

### 2.3 OrderDetailDao - 注文明細検索（正常系）

#### Feature: 注文明細検索

#### Scenario: 注文IDで注文明細一覧を取得

* Given（前提条件）:
  * EntityManagerがモック化されている
  * モック設定: `createQuery()`が注文明細リストを返す
  * 注文ID=1の注文明細が3件存在する

* When（操作）:
  * `OrderDetailDao.findByOrderTranId(1)`を呼び出す

* Then（期待結果）:
  * 3件の注文明細が返される
  * 注文明細IDの昇順でソートされている

#### テストデータ
* 入力:
  ```
  orderTranId: 1
  ```
* 期待される出力:
  ```
  List<OrderDetail> size: 3
  orderDetailId: [1, 2, 3]
  ```

---

### 2.4 JwtUtil - JWT生成（正常系）

#### Feature: JWT生成

#### Scenario: 顧客IDとメールアドレスからJWTを生成

* Given（前提条件）:
  * JwtUtilが初期化されている
  * 秘密鍵が設定されている
  * customerId=1, email="test@example.com"

* When（操作）:
  * `JwtUtil.generateToken(1, "test@example.com")`を呼び出す

* Then（期待結果）:
  * JWTトークン文字列が返される
  * トークンが3つのパート（ヘッダー、ペイロード、署名）で構成されている
  * ペイロードにcustomerId=1が含まれている
  * ペイロードにemail="test@example.com"が含まれている

---

### 2.5 JwtUtil - JWT検証（正常系）

#### Feature: JWT検証

#### Scenario: 有効なJWTトークンを検証

* Given（前提条件）:
  * JwtUtilが初期化されている
  * 有効なJWTトークンが存在する

* When（操作）:
  * `JwtUtil.validateToken(validToken)`を呼び出す

* Then（期待結果）:
  * `true`が返される

---

### 2.6 JwtUtil - JWT検証（異常系：期限切れ）

#### Feature: JWT検証

#### Scenario: 期限切れのJWTトークンを検証

* Given（前提条件）:
  * JwtUtilが初期化されている
  * 期限切れのJWTトークンが存在する

* When（操作）:
  * `JwtUtil.validateToken(expiredToken)`を呼び出す

* Then（期待結果）:
  * `false`が返される
  * 例外はスローされない

---

### 2.7 JwtUtil - JWT検証（異常系：不正な署名）

#### Feature: JWT検証

#### Scenario: 不正な署名のJWTトークンを検証

* Given（前提条件）:
  * JwtUtilが初期化されている
  * 不正な署名のJWTトークンが存在する

* When（操作）:
  * `JwtUtil.validateToken(invalidToken)`を呼び出す

* Then（期待結果）:
  * `false`が返される
  * 例外はスローされない

---

### 2.8 JwtUtil - Claims抽出（正常系）

#### Feature: Claims抽出

#### Scenario: JWTトークンから顧客IDを抽出

* Given（前提条件）:
  * JwtUtilが初期化されている
  * 有効なJWTトークン（customerId=1含む）が存在する

* When（操作）:
  * `JwtUtil.getCustomerIdFromToken(token)`を呼び出す

* Then（期待結果）:
  * `1`が返される

---

### 2.9 JwtAuthenFilter - 認証フィルター（正常系：認証成功）

#### Feature: 認証フィルター

#### Scenario: 有効なJWTトークンで認証成功

* Given（前提条件）:
  * JwtUtilがモック化されている
  * AuthenInfoが注入されている
  * リクエストに有効なJWTトークン（Cookie）が含まれている
  * モック設定: `JwtUtil.validateToken()`が`true`を返す
  * モック設定: `JwtUtil.getCustomerIdFromToken()`が`1`を返す
  * モック設定: `JwtUtil.getEmailFromToken()`が`"test@example.com"`を返す

* When（操作）:
  * `JwtAuthenFilter.doFilter(request, response, chain)`を呼び出す

* Then（期待結果）:
  * `AuthenInfo.customerId`が`1`に設定される
  * `AuthenInfo.email`が`"test@example.com"`に設定される
  * `chain.doFilter()`が呼び出される

---

### 2.10 JwtAuthenFilter - 認証フィルター（異常系：トークンなし）

#### Feature: 認証フィルター

#### Scenario: JWTトークンが存在しない

* Given（前提条件）:
  * JwtUtilがモック化されている
  * リクエストにJWTトークン（Cookie）が含まれていない
  * リクエストパス: `/api/orders`（認証必須）

* When（操作）:
  * `JwtAuthenFilter.doFilter(request, response, chain)`を呼び出す

* Then（期待結果）:
  * HTTPステータス401（Unauthorized）が返される
  * `chain.doFilter()`が呼び出されない

---

### 2.11 JwtAuthenFilter - 認証フィルター（正常系：除外パス）

#### Feature: 認証フィルター

#### Scenario: 認証除外パスへのアクセス

* Given（前提条件）:
  * JwtUtilがモック化されている
  * リクエストパス: `/api/auth/login`（認証除外）
  * リクエストにJWTトークンが含まれていない

* When（操作）:
  * `JwtAuthenFilter.doFilter(request, response, chain)`を呼び出す

* Then（期待結果）:
  * 認証処理がスキップされる
  * `chain.doFilter()`が呼び出される

---

### 2.12 BackOfficeRestClient - 書籍一覧取得（正常系）

#### Feature: 外部API連携（書籍一覧取得）

#### Scenario: 全書籍を取得

* Given（前提条件）:
  * 外部APIクライアントがモック化されている
  * モック設定: `GET /books`が書籍リストを返す

* When（操作）:
  * `BackOfficeRestClient.getAllBooks()`を呼び出す

* Then（期待結果）:
  * 書籍リスト（List<BookTO>）が返される
  * 各書籍に在庫情報が含まれている

---

### 2.13 BackOfficeRestClient - 在庫更新（正常系：楽観的ロック成功）

#### Feature: 外部API連携（在庫更新）

#### Scenario: 在庫を更新（楽観的ロック成功）

* Given（前提条件）:
  * 外部APIクライアントがモック化されている
  * bookId=1, quantity=8, version=1
  * モック設定: `PUT /stocks/1`が更新後の在庫情報を返す

* When（操作）:
  * `BackOfficeRestClient.updateStock(1, 8, 1L)`を呼び出す

* Then（期待結果）:
  * 更新後の在庫情報（StockTO）が返される
  * version=2に更新されている

---

### 2.14 BackOfficeRestClient - 在庫更新（異常系：楽観的ロック失敗）

#### Feature: 外部API連携（在庫更新）

#### Scenario: 在庫更新時に楽観的ロック失敗

* Given（前提条件）:
  * 外部APIクライアントがモック化されている
  * bookId=1, quantity=8, version=1
  * モック設定: `PUT /stocks/1`が409 Conflictを返す

* When（操作）:
  * `BackOfficeRestClient.updateStock(1, 8, 1L)`を呼び出す

* Then（期待結果）:
  * `OptimisticLockException`がスローされる

---

### 2.15 CustomerHubRestClient - 顧客検索（正常系：メールアドレス）

#### Feature: 外部API連携（顧客検索）

#### Scenario: メールアドレスで顧客を検索

* Given（前提条件）:
  * 外部APIクライアントがモック化されている
  * email="test@example.com"
  * モック設定: `GET /customers/query_email?email=...`が顧客情報を返す

* When（操作）:
  * `CustomerHubRestClient.findByEmail("test@example.com")`を呼び出す

* Then（期待結果）:
  * 顧客情報（CustomerTO）が返される
  * customerId=1
  * email="test@example.com"

---

### 2.16 CustomerHubRestClient - 顧客検索（異常系：顧客なし）

#### Feature: 外部API連携（顧客検索）

#### Scenario: 存在しないメールアドレスで検索

* Given（前提条件）:
  * 外部APIクライアントがモック化されている
  * email="notfound@example.com"
  * モック設定: `GET /customers/query_email?email=...`が404 Not Foundを返す

* When（操作）:
  * `CustomerHubRestClient.findByEmail("notfound@example.com")`を呼び出す

* Then（期待結果）:
  * `null`が返される（または適切な例外がスローされる）

---

### 2.17 CustomerHubRestClient - 顧客登録（正常系）

#### Feature: 外部API連携（顧客登録）

#### Scenario: 新規顧客を登録

* Given（前提条件）:
  * 外部APIクライアントがモック化されている
  * 新規顧客情報（CustomerTO）が存在する
  * モック設定: `POST /customers/`が作成された顧客情報を返す

* When（操作）:
  * `CustomerHubRestClient.register(newCustomer)`を呼び出す

* Then（期待結果）:
  * 作成された顧客情報（CustomerTO）が返される
  * customerId（自動採番）が含まれている

---

### 2.18 CustomerHubRestClient - 顧客登録（異常系：メール重複）

#### Feature: 外部API連携（顧客登録）

#### Scenario: 重複したメールアドレスで登録

* Given（前提条件）:
  * 外部APIクライアントがモック化されている
  * 重複したメールアドレスの顧客情報が存在する
  * モック設定: `POST /customers/`が409 Conflictを返す

* When（操作）:
  * `CustomerHubRestClient.register(duplicateCustomer)`を呼び出す

* Then（期待結果）:
  * 適切な例外がスローされる（ConflictException等）

---

### 2.19 OutOfStockExceptionMapper - 例外マッピング（在庫不足）

#### Feature: 例外マッピング

#### Scenario: OutOfStockExceptionを409 Conflictにマッピング

* Given（前提条件）:
  * OutOfStockExceptionがスローされる
  * message="在庫が不足しています"

* When（操作）:
  * `OutOfStockExceptionMapper.toResponse(exception)`を呼び出す

* Then（期待結果）:
  * HTTPステータス409（Conflict）が返される
  * ErrorResponseボディが含まれる:
    - status: 409
    - error: "Conflict"
    - message: "在庫が不足しています"

---

### 2.20 OptimisticLockExceptionMapper - 例外マッピング（楽観的ロック失敗）

#### Feature: 例外マッピング

#### Scenario: OptimisticLockExceptionを409 Conflictにマッピング

* Given（前提条件）:
  * OptimisticLockExceptionがスローされる

* When（操作）:
  * `OptimisticLockExceptionMapper.toResponse(exception)`を呼び出す

* Then（期待結果）:
  * HTTPステータス409（Conflict）が返される
  * ErrorResponseボディが含まれる:
    - status: 409
    - error: "Conflict"
    - message: "データが他のユーザーによって更新されました。再度お試しください。"

---

### 2.21 ValidationExceptionMapper - 例外マッピング（バリデーションエラー）

#### Feature: 例外マッピング

#### Scenario: ConstraintViolationExceptionを400 Bad Requestにマッピング

* Given（前提条件）:
  * ConstraintViolationExceptionがスローされる
  * 違反: "メールアドレスは必須です"

* When（操作）:
  * `ValidationExceptionMapper.toResponse(exception)`を呼び出す

* Then（期待結果）:
  * HTTPステータス400（Bad Request）が返される
  * ErrorResponseボディが含まれる:
    - status: 400
    - error: "Bad Request"
    - message: バリデーションエラーメッセージ

---

### 2.22 OrderDetailPK - 複合主キー（equals/hashCode）

#### Feature: 複合主キー

#### Scenario: 同じ値のOrderDetailPKが等しい

* Given（前提条件）:
  * OrderDetailPK pk1 = new OrderDetailPK(1, 1)
  * OrderDetailPK pk2 = new OrderDetailPK(1, 1)

* When（操作）:
  * `pk1.equals(pk2)`を呼び出す

* Then（期待結果）:
  * `true`が返される
  * `pk1.hashCode() == pk2.hashCode()`

---

### 2.23 OrderDetailPK - 複合主キー（境界値：null）

#### Feature: 複合主キー

#### Scenario: nullとの比較

* Given（前提条件）:
  * OrderDetailPK pk1 = new OrderDetailPK(1, 1)

* When（操作）:
  * `pk1.equals(null)`を呼び出す

* Then（期待結果）:
  * `false`が返される

---

## 3. モック化の方針

### 3.1 commonドメイン内の依存関係
* JwtUtil、AuthenInfo → モック不要（実際の連携をテスト）
* OrderTranDao、OrderDetailDao → モック不要（実際の連携をテスト）

### 3.2 commonドメイン外の依存関係
* EntityManager → モック化
* 外部APIクライアント（BackOfficeRestClient、CustomerHubRestClient）→ モック化（WireMockまたはMockito）
* HttpServletRequest、HttpServletResponse → モック化

---

## 4. カバレッジ目標

* ステートメントカバレッジ: 80%以上
* ブランチカバレッジ: 70%以上

---

## 5. 受入基準

### 5.1 機能要件
* [ ] すべての正常系テストが成功する
* [ ] すべての異常系テストが成功する
* [ ] すべての境界値テストが成功する
* [ ] JWT生成・検証が正しく動作する
* [ ] 外部API連携が正しく動作する（モック環境）
* [ ] 例外マッピングが正しく動作する

### 5.2 品質要件
* [ ] カバレッジ目標を達成する
* [ ] テストコードにコメントが適切に記載されている
* [ ] テストケースが独立している（テスト間の依存関係がない）
* [ ] モック化が適切に行われている

---

## 6. 参考資料

* [detailed_design.md](detailed_design.md) - 詳細設計書
* [../../basic_design/common/functional_design.md](../../basic_design/common/functional_design.md) - 共通機能設計書
* [../../basic_design/common/behaviors.md](../../basic_design/common/behaviors.md) - 共通機能振る舞い仕様書（結合テスト用）
* [../../requirements/behaviors.md](../../requirements/behaviors.md) - システム振る舞い仕様書（E2Eテスト用）
