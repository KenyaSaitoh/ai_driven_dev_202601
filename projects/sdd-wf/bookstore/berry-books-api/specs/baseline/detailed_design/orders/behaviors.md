# orders - 振る舞い仕様書（単体テスト用）

ドメイン名: orders  
バージョン: 1.0.0  
最終更新日: 2026-02-07

記法: シナリオは Gherkin 記法で記述する（Feature, Scenario, Given, When, Then, And, But）。principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照すること。

---

## 1. 概要

本文書は、ordersドメインの単体テスト用の振る舞い、テストシナリオ、受入基準を記述する。

テスト対象:
* OrderResource（JAX-RS）
* OrderService（ビジネスロジック）
* OrderTranDao（データアクセス）
* OrderDetailDao（データアクセス）
* DeliveryFeeService（配送料金計算）

単体テストの範囲:
* ドメイン粒度内の機能をテスト
* ドメイン内のコンポーネント間は実際の連携をテスト可能
* ドメイン外の依存（BackOfficeRestClient、EntityManager等）はモック化
* 結合テストシナリオは ../../basic_design/orders/behaviors.md を参照すること
* E2Eテストシナリオは ../../requirements/behaviors.md を参照すること

関連ドキュメント:
* [detailed_design.md](detailed_design.md) - 詳細設計書
* [../../basic_design/orders/functional_design.md](../../basic_design/orders/functional_design.md) - ドメイン機能設計書
* [../../basic_design/orders/behaviors.md](../../basic_design/orders/behaviors.md) - ドメイン振る舞い仕様書（結合テスト用）

---

## 2. テストシナリオ

### 2.1 OrderResource - 注文作成（正常系）

#### Feature: 注文作成API

注文作成APIは、認証されたユーザーが書籍を注文できる機能である。

#### Scenario: 正常な注文作成

* Given（前提条件）:
  * ユーザーがログインしている（AuthenContext.customerId = 1）
  * OrderServiceがモック化されている
  * OrderServiceの戻り値として正常なOrderTranオブジェクトが設定されている

* When（操作）:
  * POST /api/orders に有効なOrderRequestを送信する

* Then（期待結果）:
  * HTTPステータス: 201 Created
  * レスポンスボディ: OrderResponse（orderTranId, orderDate, customerId, totalPrice, deliveryPrice, deliveryAddress, settlementType, orderDetails）
  * OrderService.createOrder()が1回呼び出される

* And（追加の検証）:
  * customerId = 1（AuthenContextから取得）
  * レスポンスのContent-Type: application/json

#### テストデータ
* 入力:
  ```json
  {
    "cartItems": [
      {
        "bookId": 1,
        "bookName": "Java完全理解",
        "publisherName": "技術評論社",
        "price": 3200,
        "count": 2,
        "version": 1
      }
    ],
    "totalPrice": 7200,
    "deliveryPrice": 800,
    "deliveryAddress": "東京都渋谷区1-2-3",
    "settlementType": 1
  }
  ```
* 期待される出力:
  ```json
  {
    "orderTranId": 1,
    "orderDate": "2026-02-07",
    "customerId": 1,
    "totalPrice": 7200,
    "deliveryPrice": 800,
    "deliveryAddress": "東京都渋谷区1-2-3",
    "settlementType": 1,
    "orderDetails": [
      {
        "orderDetailId": 1,
        "bookId": 1,
        "bookName": "Java完全理解",
        "publisherName": "技術評論社",
        "price": 3200,
        "count": 2
      }
    ]
  }
  ```

---

### 2.2 OrderResource - 注文作成（異常系: 在庫不足）

#### Feature: 注文作成API

#### Scenario: 在庫不足エラー

* Given（前提条件）:
  * ユーザーがログインしている（AuthenContext.customerId = 1）
  * OrderServiceがモック化されている
  * OrderService.createOrder()がOutOfStockExceptionをスローするように設定

* When（操作）:
  * POST /api/orders に有効なOrderRequestを送信する

* Then（期待結果）:
  * HTTPステータス: 409 Conflict
  * レスポンスボディ: ErrorResponse（status=409, error="Conflict", message="在庫不足", path="/api/orders"）

---

### 2.3 OrderResource - 注文作成（異常系: 楽観的ロック競合）

#### Feature: 注文作成API

#### Scenario: 楽観的ロック競合エラー

* Given（前提条件）:
  * ユーザーがログインしている（AuthenContext.customerId = 1）
  * OrderServiceがモック化されている
  * OrderService.createOrder()がOptimisticLockExceptionをスローするように設定

* When（操作）:
  * POST /api/orders に有効なOrderRequestを送信する

* Then（期待結果）:
  * HTTPステータス: 409 Conflict
  * レスポンスボディ: ErrorResponse（status=409, error="Conflict", message="データが他のユーザーによって更新されました", path="/api/orders"）

---

### 2.4 OrderResource - 注文作成（異常系: 未認証）

#### Feature: 注文作成API

#### Scenario: 未認証ユーザーの注文試行

* Given（前提条件）:
  * ユーザーがログインしていない（AuthenContext.customerId = null）

* When（操作）:
  * POST /api/orders に有効なOrderRequestを送信する

* Then（期待結果）:
  * HTTPステータス: 401 Unauthorized
  * レスポンスボディ: ErrorResponse（status=401, error="Unauthorized", message="認証が必要です"）

---

### 2.5 OrderResource - 注文作成（異常系: バリデーションエラー）

#### Feature: 注文作成API

#### Scenario: 不正なリクエストデータ

* Given（前提条件）:
  * ユーザーがログインしている（AuthenContext.customerId = 1）
  * OrderRequestのcartItemsが空リスト

* When（操作）:
  * POST /api/orders に不正なOrderRequestを送信する

* Then（期待結果）:
  * HTTPステータス: 400 Bad Request
  * レスポンスボディ: ErrorResponse（status=400, error="Bad Request", message="cartItemsは必須です"）

---

### 2.6 OrderResource - 注文履歴取得（正常系）

#### Feature: 注文履歴取得API

#### Scenario: ログインユーザーの注文履歴を取得

* Given（前提条件）:
  * ユーザーがログインしている（AuthenContext.customerId = 1）
  * OrderServiceがモック化されている
  * OrderService.getOrderHistory(1)が注文リストを返す

* When（操作）:
  * GET /api/orders/history を送信する

* Then（期待結果）:
  * HTTPステータス: 200 OK
  * レスポンスボディ: List<OrderResponse>（注文履歴）
  * OrderService.getOrderHistory(1)が1回呼び出される

* And（追加の検証）:
  * customerId = 1（AuthenContextから取得）
  * レスポンスのContent-Type: application/json

#### テストデータ
* 期待される出力:
  ```json
  [
    {
      "orderTranId": 1,
      "orderDate": "2026-02-06",
      "customerId": 1,
      "totalPrice": 7200,
      "deliveryPrice": 800,
      "deliveryAddress": "東京都渋谷区1-2-3",
      "settlementType": 1,
      "orderDetails": [...]
    },
    {
      "orderTranId": 2,
      "orderDate": "2026-02-05",
      "customerId": 1,
      "totalPrice": 5400,
      "deliveryPrice": 0,
      "deliveryAddress": "東京都新宿区4-5-6",
      "settlementType": 2,
      "orderDetails": [...]
    }
  ]
  ```

---

### 2.7 OrderResource - 注文履歴取得（異常系: 未認証）

#### Feature: 注文履歴取得API

#### Scenario: 未認証ユーザーの注文履歴取得試行

* Given（前提条件）:
  * ユーザーがログインしていない（AuthenContext.customerId = null）

* When（操作）:
  * GET /api/orders/history を送信する

* Then（期待結果）:
  * HTTPステータス: 401 Unauthorized
  * レスポンスボディ: ErrorResponse（status=401, error="Unauthorized", message="認証が必要です"）

---

### 2.8 OrderResource - 注文詳細取得（正常系）

#### Feature: 注文詳細取得API

#### Scenario: 注文詳細を取得

* Given（前提条件）:
  * OrderServiceがモック化されている
  * OrderService.getOrderById(1)が注文情報を返す

* When（操作）:
  * GET /api/orders/1 を送信する

* Then（期待結果）:
  * HTTPステータス: 200 OK
  * レスポンスボディ: OrderResponse（注文詳細、注文明細を含む）
  * OrderService.getOrderById(1)が1回呼び出される

---

### 2.9 OrderResource - 注文詳細取得（異常系: 存在しない注文ID）

#### Feature: 注文詳細取得API

#### Scenario: 存在しない注文IDを指定

* Given（前提条件）:
  * OrderServiceがモック化されている
  * OrderService.getOrderById(999)がnullを返す

* When（操作）:
  * GET /api/orders/999 を送信する

* Then（期待結果）:
  * HTTPステータス: 404 Not Found
  * レスポンスボディ: ErrorResponse（status=404, error="Not Found", message="注文が見つかりません"）

---

### 2.10 OrderService - 注文作成（正常系）

#### Feature: 注文処理

#### Scenario: 正常な注文処理（在庫確認・更新、注文作成、注文明細作成）

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * DeliveryFeeServiceがモック化されている
  * OrderTranDaoがモック化されている
  * OrderDetailDaoがモック化されている
  * 在庫情報: bookId=1, quantity=10, version=1
  * BackOfficeRestClient.findStockById(1)がStockTO（quantity=10, version=1）を返す
  * BackOfficeRestClient.updateStock(1, 1, 8)が成功する（新在庫: quantity=8, version=2）
  * OrderTranDao.insert()が成功する（orderTranId=1が採番される）
  * OrderDetailDao.insert()が成功する

* When（操作）:
  * OrderService.createOrder(orderRequest, customerId=1)を呼び出す

* Then（期待結果）:
  * 戻り値: OrderTran（orderTranId=1, customerId=1, ...）
  * BackOfficeRestClient.findStockById(1)が1回呼び出される
  * BackOfficeRestClient.updateStock(1, 1, 8)が1回呼び出される
  * OrderTranDao.insert()が1回呼び出される
  * OrderDetailDao.insert()が1回呼び出される（カートアイテム数分）

* And（追加の検証）:
  * トランザクションがコミットされる
  * 例外がスローされない

---

### 2.11 OrderService - 注文作成（異常系: 在庫不足）

#### Feature: 注文処理

#### Scenario: 在庫不足エラー

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * 在庫情報: bookId=1, quantity=1, version=1
  * BackOfficeRestClient.findStockById(1)がStockTO（quantity=1, version=1）を返す
  * 注文数: 2

* When（操作）:
  * OrderService.createOrder(orderRequest, customerId=1)を呼び出す

* Then（期待結果）:
  * 例外: OutOfStockException
  * BackOfficeRestClient.findStockById(1)が1回呼び出される
  * BackOfficeRestClient.updateStock()が呼び出されない（在庫チェック段階でエラー）
  * OrderTranDao.insert()が呼び出されない
  * OrderDetailDao.insert()が呼び出されない

* And（追加の検証）:
  * トランザクションがロールバックされる

---

### 2.12 OrderService - 注文作成（異常系: 楽観的ロック競合）

#### Feature: 注文処理

#### Scenario: 楽観的ロック競合エラー

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * 在庫情報: bookId=1, quantity=10, version=1
  * BackOfficeRestClient.findStockById(1)がStockTO（quantity=10, version=1）を返す
  * BackOfficeRestClient.updateStock(1, 1, 8)が409 Conflict（OptimisticLockException）をスローする

* When（操作）:
  * OrderService.createOrder(orderRequest, customerId=1)を呼び出す

* Then（期待結果）:
  * 例外: OptimisticLockException
  * BackOfficeRestClient.findStockById(1)が1回呼び出される
  * BackOfficeRestClient.updateStock(1, 1, 8)が1回呼び出される
  * OrderTranDao.insert()が呼び出されない
  * OrderDetailDao.insert()が呼び出されない

* And（追加の検証）:
  * トランザクションがロールバックされる

---

### 2.13 OrderService - 注文作成（異常系: 複数カートアイテム、一部在庫不足）

#### Feature: 注文処理

#### Scenario: 複数カートアイテム、1件目は在庫充分、2件目は在庫不足

* Given（前提条件）:
  * BackOfficeRestClientがモック化されている
  * カートアイテム1: bookId=1, quantity=2, 在庫=10
  * カートアイテム2: bookId=2, quantity=5, 在庫=3（不足）
  * BackOfficeRestClient.findStockById(1)がStockTO（quantity=10）を返す
  * BackOfficeRestClient.findStockById(2)がStockTO（quantity=3）を返す

* When（操作）:
  * OrderService.createOrder(orderRequest, customerId=1)を呼び出す

* Then（期待結果）:
  * 例外: OutOfStockException
  * BackOfficeRestClient.findStockById(1)が1回呼び出される
  * BackOfficeRestClient.findStockById(2)が1回呼び出される
  * BackOfficeRestClient.updateStock()が呼び出されない（在庫チェック段階でエラー）
  * OrderTranDao.insert()が呼び出されない
  * OrderDetailDao.insert()が呼び出されない

* And（追加の検証）:
  * トランザクションがロールバックされる

---

### 2.14 OrderService - 注文履歴取得（正常系）

#### Feature: 注文履歴取得

#### Scenario: 顧客IDで注文履歴を取得

* Given（前提条件）:
  * OrderTranDaoがモック化されている
  * OrderTranDao.findByCustomerId(1)が注文リストを返す（2件）

* When（操作）:
  * OrderService.getOrderHistory(customerId=1)を呼び出す

* Then（期待結果）:
  * 戻り値: List<OrderTran>（2件）
  * OrderTranDao.findByCustomerId(1)が1回呼び出される

* And（追加の検証）:
  * 注文リストは注文日の降順で並んでいる

---

### 2.15 OrderService - 注文詳細取得（正常系）

#### Feature: 注文詳細取得

#### Scenario: 注文IDで注文詳細を取得

* Given（前提条件）:
  * OrderTranDaoがモック化されている
  * OrderTranDao.findById(1)がOrderTranを返す（注文明細を含む）

* When（操作）:
  * OrderService.getOrderById(tranId=1)を呼び出す

* Then（期待結果）:
  * 戻り値: OrderTran（注文明細を含む）
  * OrderTranDao.findById(1)が1回呼び出される

---

### 2.16 OrderService - 注文詳細取得（異常系: 存在しない注文ID）

#### Feature: 注文詳細取得

#### Scenario: 存在しない注文IDを指定

* Given（前提条件）:
  * OrderTranDaoがモック化されている
  * OrderTranDao.findById(999)がnullを返す

* When（操作）:
  * OrderService.getOrderById(tranId=999)を呼び出す

* Then（期待結果）:
  * 戻り値: null

---

### 2.17 DeliveryFeeService - 配送料金計算（正常系: 5000円未満）

#### Feature: 配送料金計算

#### Scenario: 注文金額が5000円未満、その他の地域

* Given（前提条件）:
  * 注文金額: 4000円
  * 配送先住所: "東京都渋谷区1-2-3"

* When（操作）:
  * DeliveryFeeService.calculateDeliveryFee(address, totalPrice)を呼び出す

* Then（期待結果）:
  * 戻り値: 800（円）

---

### 2.18 DeliveryFeeService - 配送料金計算（正常系: 5000円以上）

#### Feature: 配送料金計算

#### Scenario: 注文金額が5000円以上、送料無料

* Given（前提条件）:
  * 注文金額: 5000円
  * 配送先住所: "東京都渋谷区1-2-3"

* When（操作）:
  * DeliveryFeeService.calculateDeliveryFee(address, totalPrice)を呼び出す

* Then（期待結果）:
  * 戻り値: 0（円）

---

### 2.19 DeliveryFeeService - 配送料金計算（正常系: 北海道）

#### Feature: 配送料金計算

#### Scenario: 注文金額が5000円未満、北海道

* Given（前提条件）:
  * 注文金額: 4000円
  * 配送先住所: "北海道札幌市1-2-3"

* When（操作）:
  * DeliveryFeeService.calculateDeliveryFee(address, totalPrice)を呼び出す

* Then（期待結果）:
  * 戻り値: 1500（円）

---

### 2.20 DeliveryFeeService - 配送料金計算（正常系: 沖縄）

#### Feature: 配送料金計算

#### Scenario: 注文金額が5000円未満、沖縄

* Given（前提条件）:
  * 注文金額: 4000円
  * 配送先住所: "沖縄県那覇市1-2-3"

* When（操作）:
  * DeliveryFeeService.calculateDeliveryFee(address, totalPrice)を呼び出す

* Then（期待結果）:
  * 戻り値: 1500（円）

---

### 2.21 DeliveryFeeService - 配送料金計算（境界値: 5000円ちょうど）

#### Feature: 配送料金計算

#### Scenario: 注文金額が5000円ちょうど

* Given（前提条件）:
  * 注文金額: 5000円
  * 配送先住所: "東京都渋谷区1-2-3"

* When（操作）:
  * DeliveryFeeService.calculateDeliveryFee(address, totalPrice)を呼び出す

* Then（期待結果）:
  * 戻り値: 0（円）

#### テストデータ（境界値）
* 最小値: 0円 → 配送料800円
* 境界値: 4999円 → 配送料800円
* 境界値: 5000円 → 配送料0円
* 最大値: Integer.MAX_VALUE → 配送料0円

---

### 2.22 OrderTranDao - 注文作成（正常系）

#### Feature: 注文データ永続化

#### Scenario: 注文トランザクションを作成

* Given（前提条件）:
  * EntityManagerがモック化されている
  * OrderTranオブジェクトが準備されている（orderTranId=null）

* When（操作）:
  * OrderTranDao.insert(orderTran)を呼び出す

* Then（期待結果）:
  * 戻り値: OrderTran（orderTranId=1が採番される）
  * EntityManager.persist()が1回呼び出される

---

### 2.23 OrderTranDao - 注文検索（正常系: 注文ID）

#### Feature: 注文データ検索

#### Scenario: 注文IDで注文を検索

* Given（前提条件）:
  * EntityManagerがモック化されている
  * TypedQueryがモック化されている
  * JPQL: "SELECT o FROM OrderTran o LEFT JOIN FETCH o.orderDetails WHERE o.orderTranId = :orderTranId"
  * TypedQuery.getSingleResult()がOrderTranを返す

* When（操作）:
  * OrderTranDao.findById(orderTranId=1)を呼び出す

* Then（期待結果）:
  * 戻り値: OrderTran（注文明細を含む）
  * EntityManager.createQuery()が1回呼び出される
  * TypedQuery.setParameter("orderTranId", 1)が1回呼び出される
  * TypedQuery.getSingleResult()が1回呼び出される

---

### 2.24 OrderTranDao - 注文検索（異常系: 存在しない注文ID）

#### Feature: 注文データ検索

#### Scenario: 存在しない注文IDを指定

* Given（前提条件）:
  * EntityManagerがモック化されている
  * TypedQueryがモック化されている
  * TypedQuery.getSingleResult()がNoResultExceptionをスローする

* When（操作）:
  * OrderTranDao.findById(orderTranId=999)を呼び出す

* Then（期待結果）:
  * 戻り値: null
  * NoResultExceptionがキャッチされる

---

### 2.25 OrderTranDao - 注文履歴検索（正常系: 顧客ID）

#### Feature: 注文データ検索

#### Scenario: 顧客IDで注文履歴を検索

* Given（前提条件）:
  * EntityManagerがモック化されている
  * TypedQueryがモック化されている
  * JPQL: "SELECT o FROM OrderTran o WHERE o.customerId = :customerId ORDER BY o.orderDate DESC"
  * TypedQuery.getResultList()が注文リスト（2件）を返す

* When（操作）:
  * OrderTranDao.findByCustomerId(customerId=1)を呼び出す

* Then（期待結果）:
  * 戻り値: List<OrderTran>（2件、注文日の降順）
  * EntityManager.createQuery()が1回呼び出される
  * TypedQuery.setParameter("customerId", 1)が1回呼び出される
  * TypedQuery.setMaxResults(100)が1回呼び出される
  * TypedQuery.getResultList()が1回呼び出される

---

### 2.26 OrderDetailDao - 注文明細作成（正常系）

#### Feature: 注文明細データ永続化

#### Scenario: 注文明細を作成

* Given（前提条件）:
  * EntityManagerがモック化されている
  * OrderDetailオブジェクトが準備されている

* When（操作）:
  * OrderDetailDao.insert(orderDetail)を呼び出す

* Then（期待結果）:
  * 戻り値: OrderDetail
  * EntityManager.persist()が1回呼び出される

---

### 2.27 OrderDetailDao - 注文明細検索（正常系: 複合主キー）

#### Feature: 注文明細データ検索

#### Scenario: 複合主キーで注文明細を検索

* Given（前提条件）:
  * EntityManagerがモック化されている
  * TypedQueryがモック化されている
  * JPQL: "SELECT d FROM OrderDetail d WHERE d.orderDetailPK.orderTranId = :orderTranId AND d.orderDetailPK.orderDetailId = :orderDetailId"
  * TypedQuery.getSingleResult()がOrderDetailを返す

* When（操作）:
  * OrderDetailDao.findById(orderDetailPK)を呼び出す

* Then（期待結果）:
  * 戻り値: OrderDetail
  * EntityManager.createQuery()が1回呼び出される
  * TypedQuery.setParameter("orderTranId", 1)が1回呼び出される
  * TypedQuery.setParameter("orderDetailId", 1)が1回呼び出される
  * TypedQuery.getSingleResult()が1回呼び出される

---

### 2.28 OrderDetailDao - 注文明細検索（異常系: 存在しない複合主キー）

#### Feature: 注文明細データ検索

#### Scenario: 存在しない複合主キーを指定

* Given（前提条件）:
  * EntityManagerがモック化されている
  * TypedQueryがモック化されている
  * TypedQuery.getSingleResult()がNoResultExceptionをスローする

* When（操作）:
  * OrderDetailDao.findById(orderDetailPK)を呼び出す

* Then（期待結果）:
  * 戻り値: null
  * NoResultExceptionがキャッチされる

---

## 3. モック化の方針

### 3.1 ドメイン内の依存関係
* OrderService → OrderTranDao: テストによってはモック化（単体テスト）、実連携も可能（統合的な単体テスト）
* OrderService → OrderDetailDao: テストによってはモック化（単体テスト）、実連携も可能（統合的な単体テスト）
* OrderService → DeliveryFeeService: テストによってはモック化、実連携も可能

### 3.2 ドメイン外の依存関係
* BackOfficeRestClient → モック化必須（外部API連携）
* EntityManager → モック化必須（データベース操作）
* AuthenContext → モック化またはスタブ（@RequestScoped）

---

## 4. カバレッジ目標

* ステートメントカバレッジ: 80%以上
* ブランチカバレッジ: 70%以上

---

## 5. 受入基準

### 5.1 機能要件
* すべての正常系テストが成功する
* すべての異常系テストが成功する
* すべての境界値テストが成功する

### 5.2 品質要件
* カバレッジ目標を達成する
* テストコードにコメントが適切に記載されている
* テストケースが独立している（テスト間の依存関係がない）

---

## 6. 参考資料

* [detailed_design.md](detailed_design.md) - 詳細設計書
* [../../basic_design/orders/functional_design.md](../../basic_design/orders/functional_design.md) - ドメイン機能設計書
* [../../basic_design/orders/behaviors.md](../../basic_design/orders/behaviors.md) - ドメイン振る舞い仕様書（結合テスト用）
* [../../requirements/behaviors.md](../../requirements/behaviors.md) - システム振る舞い仕様書（E2Eテスト用）
