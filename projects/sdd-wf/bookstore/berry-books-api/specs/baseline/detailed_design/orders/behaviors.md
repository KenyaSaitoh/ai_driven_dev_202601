# orders - 振る舞い仕様書（単体テスト用）

ドメイン名: orders  
バージョン: 1.0.0  
最終更新日: 2026-02-07

記法: シナリオは Gherkin 記法で記述する（Feature, Scenario, Given, When, Then, And, But）。principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照すること。

---

## 1. 概要

本文書は、ordersドメインの単体テスト用の振る舞い、テストシナリオ、受入基準を記述する。

テスト対象:
* OrderTranDao（注文トランザクションDAO）
* OrderDetailDao（注文明細DAO）
* OrderTran（注文トランザクションエンティティ）
* OrderDetail（注文明細エンティティ）
* OrderDetailPK（注文明細複合主キー）
* DeliveryFeeService（配送料金計算サービス）
* OrderService（注文ビジネスロジックサービス）
* OrderResource（注文REST APIリソース）

単体テストの範囲:
* ドメイン粒度内の機能をテスト
* EntityManagerはモック化
* 結合テストシナリオは ../../basic_design/orders/behaviors.md を参照すること
* E2Eテストシナリオは ../../requirements/behaviors.md を参照すること

関連ドキュメント:
* [detailed_design.md](detailed_design.md) - 詳細設計書
* [../../basic_design/orders/functional_design.md](../../basic_design/orders/functional_design.md) - ドメイン機能設計書
* [../../basic_design/orders/behaviors.md](../../basic_design/orders/behaviors.md) - ドメイン振る舞い仕様書（結合テスト用）

---

## 2. テストシナリオ

### 2.1 OrderTranDao - 注文トランザクション登録

#### Feature: 注文トランザクションの登録

#### Scenario: 注文トランザクションを正常に登録

* Given（前提条件）:
  * EntityManagerがモック化されている
  * 注文トランザクションが準備されている（注文日、顧客ID、合計金額、配送料金、配送先住所、決済方法）

* When（操作）:
  * OrderTranDao.insert(orderTran)を呼び出す

* Then（期待結果）:
  * EntityManager.persist()が呼び出される
  * 登録された注文トランザクションが返される

#### テストデータ

* 入力:
  ```
  orderDate: 2026-02-07
  customerId: 1
  totalPrice: 5400
  deliveryPrice: 400
  deliveryAddress: "東京都渋谷区1-1-1"
  settlementType: 2 (クレジットカード)
  ```

---

### 2.2 OrderTranDao - 注文IDで検索

#### Feature: 注文IDで注文トランザクションを検索

#### Scenario: 注文が見つかる

* Given（前提条件）:
  * EntityManagerがモック化されている
  * 注文ID=1の注文トランザクションが存在する

* When（操作）:
  * OrderTranDao.findById(1)を呼び出す

* Then（期待結果）:
  * 注文トランザクションが返される
  * 注文ID=1である
  * EntityManager.find()が呼び出される

#### Scenario: 注文が見つからない

* Given（前提条件）:
  * EntityManagerがモック化されている
  * 注文ID=999の注文トランザクションが存在しない

* When（操作）:
  * OrderTranDao.findById(999)を呼び出す

* Then（期待結果）:
  * nullが返される
  * EntityManager.find()が呼び出される

---

### 2.3 OrderTranDao - 顧客IDで注文履歴を検索

#### Feature: 顧客IDで注文履歴を検索

#### Scenario: 顧客の注文履歴が見つかる（複数件、降順）

* Given（前提条件）:
  * EntityManagerがモック化されている
  * 顧客ID=1の注文が複数存在する:
    * 注文ID=1（注文日: 2026-02-07）
    * 注文ID=2（注文日: 2026-02-06）
  * モック設定: JPQLクエリが注文日の降順でリストを返す

* When（操作）:
  * OrderTranDao.findByCustomerId(1)を呼び出す

* Then（期待結果）:
  * 注文トランザクションのリストが返される
  * リストのサイズは2である
  * 最初の要素の注文日が2番目の要素より新しい（降順）
  * EntityManager.createQuery()が呼び出される

* And（追加の検証）:
  * JPQLクエリのパラメータcustomerId=1が設定される
  * ORDER BY o.orderDate DESC が適用される

#### Scenario: 顧客の注文履歴が見つからない

* Given（前提条件）:
  * EntityManagerがモック化されている
  * 顧客ID=999の注文が存在しない
  * モック設定: JPQLクエリが空リストを返す

* When（操作）:
  * OrderTranDao.findByCustomerId(999)を呼び出す

* Then（期待結果）:
  * 空のリストが返される
  * EntityManager.createQuery()が呼び出される

---

### 2.4 OrderDetailDao - 注文明細登録

#### Feature: 注文明細の登録（スナップショットパターン）

#### Scenario: 注文明細を正常に登録

* Given（前提条件）:
  * EntityManagerがモック化されている
  * 注文明細が準備されている（注文ID、明細ID、書籍ID、書籍名、出版社名、価格、数量）

* When（操作）:
  * OrderDetailDao.insert(orderDetail)を呼び出す

* Then（期待結果）:
  * EntityManager.persist()が呼び出される
  * 登録された注文明細が返される

* And（追加の検証）:
  * スナップショット値（書籍名、出版社名、価格）が保持される

#### テストデータ

* 入力:
  ```
  orderTranId: 1
  orderDetailId: 1
  bookId: 10
  bookName: "Java入門"
  publisherName: "技術評論社"
  price: 3000
  count: 2
  ```

#### Scenario: スナップショット値が保持される

* Given（前提条件）:
  * EntityManagerがモック化されている
  * スナップショット値が設定された注文明細が準備されている

* When（操作）:
  * OrderDetailDao.insert(orderDetail)を呼び出す

* Then（期待結果）:
  * 書籍名="Java入門"が保持される
  * 出版社名="技術評論社"が保持される
  * 価格=3000が保持される
  * EntityManager.persist()が呼び出される

---

### 2.5 OrderDetailDao - 注文IDで注文明細一覧を検索

#### Feature: 注文IDで注文明細一覧を検索

#### Scenario: 注文明細が見つかる（複数件、明細ID昇順）

* Given（前提条件）:
  * EntityManagerがモック化されている
  * 注文ID=1の注文明細が複数存在する:
    * 明細ID=1（書籍ID=10、書籍名="Java入門"）
    * 明細ID=2（書籍ID=20、書籍名="Spring Boot実践"）
  * モック設定: JPQLクエリが明細IDの昇順でリストを返す

* When（操作）:
  * OrderDetailDao.findByOrderTranId(1)を呼び出す

* Then（期待結果）:
  * 注文明細のリストが返される
  * リストのサイズは2である
  * 最初の要素の明細ID=1、2番目の要素の明細ID=2（昇順）
  * EntityManager.createQuery()が呼び出される

* And（追加の検証）:
  * JPQLクエリのパラメータorderTranId=1が設定される
  * ORDER BY od.orderDetailId が適用される

#### Scenario: 注文明細が見つからない

* Given（前提条件）:
  * EntityManagerがモック化されている
  * 注文ID=999の注文明細が存在しない
  * モック設定: JPQLクエリが空リストを返す

* When（操作）:
  * OrderDetailDao.findByOrderTranId(999)を呼び出す

* Then（期待結果）:
  * 空のリストが返される
  * EntityManager.createQuery()が呼び出される

---

### 2.6 OrderTran - エンティティの双方向リレーション

#### Feature: 注文トランザクションと注文明細の双方向リレーション

#### Scenario: 注文明細を追加する

* Given（前提条件）:
  * OrderTranエンティティが準備されている
  * OrderDetailエンティティが準備されている

* When（操作）:
  * OrderTran.addOrderDetail(orderDetail)を呼び出す

* Then（期待結果）:
  * OrderTran.orderDetailsリストに注文明細が追加される
  * OrderDetail.orderTranに親の注文トランザクションが設定される
  * 双方向リレーションが確立される

#### Scenario: 注文明細を削除する

* Given（前提条件）:
  * OrderTranエンティティが準備されている
  * OrderDetailエンティティが注文トランザクションに追加されている

* When（操作）:
  * OrderTran.removeOrderDetail(orderDetail)を呼び出す

* Then（期待結果）:
  * OrderTran.orderDetailsリストから注文明細が削除される
  * OrderDetail.orderTranがnullに設定される
  * 双方向リレーションが解除される

---

### 2.7 OrderDetailPK - 複合主キーの同値性

#### Feature: 複合主キーの同値性判定

#### Scenario: 同じ値を持つ複合主キーは等しい

* Given（前提条件）:
  * OrderDetailPK(orderTranId=1, orderDetailId=1)が準備されている
  * OrderDetailPK(orderTranId=1, orderDetailId=1)がもう一つ準備されている

* When（操作）:
  * equals()メソッドで比較する

* Then（期待結果）:
  * trueが返される
  * hashCode()が同じ値を返す

#### Scenario: 異なる値を持つ複合主キーは等しくない

* Given（前提条件）:
  * OrderDetailPK(orderTranId=1, orderDetailId=1)が準備されている
  * OrderDetailPK(orderTranId=1, orderDetailId=2)が準備されている

* When（操作）:
  * equals()メソッドで比較する

* Then（期待結果）:
  * falseが返される

---

### 2.8 DeliveryFeeService - 配送料金計算

#### Feature: 配送料金の計算

#### Scenario: 購入金額が5000円以上で配送料無料

* Given（前提条件）:
  * 購入金額=5000円
  * 配送先住所="東京都渋谷区1-1-1"

* When（操作）:
  * DeliveryFeeService.calculateDeliveryFee(5000, "東京都渋谷区1-1-1")を呼び出す

* Then（期待結果）:
  * 配送料金=0円が返される

#### Scenario: 配送先が沖縄県の場合は800円

* Given（前提条件）:
  * 購入金額=3000円
  * 配送先住所="沖縄県那覇市1-1-1"

* When（操作）:
  * DeliveryFeeService.calculateDeliveryFee(3000, "沖縄県那覇市1-1-1")を呼び出す

* Then（期待結果）:
  * 配送料金=800円が返される

#### Scenario: その他の地域は400円

* Given（前提条件）:
  * 購入金額=3000円
  * 配送先住所="東京都渋谷区1-1-1"

* When（操作）:
  * DeliveryFeeService.calculateDeliveryFee(3000, "東京都渋谷区1-1-1")を呼び出す

* Then（期待結果）:
  * 配送料金=400円が返される

---

### 2.9 OrderService - 注文作成

#### Feature: 注文の作成

#### Scenario: 注文を正常に作成

* Given（前提条件）:
  * OrderTranDao、OrderDetailDao、BackOfficeRestClient、DeliveryFeeService、AuthenticatedUserがモック化されている
  * 在庫が十分にある（backOfficeClient.findStockById()が在庫情報を返す）
  * 配送料金が計算される（deliveryFeeService.calculateDeliveryFee()が400を返す）
  * 認証済みユーザー情報がある（authenticatedUser.getCustomerId()が1を返す）

* When（操作）:
  * OrderService.createOrder(orderRequest)を呼び出す

* Then（期待結果）:
  * orderTranDao.insert()が呼び出される
  * orderDetailDao.insert()が注文明細数分呼び出される
  * backOfficeClient.updateStock()が在庫更新のために呼び出される
  * 作成されたOrderTranが返される

#### Scenario: 在庫不足時に例外をスロー

* Given（前提条件）:
  * モック設定: backOfficeClient.findStockById()が在庫数=5を返す
  * 注文数=10（在庫より多い）

* When（操作）:
  * OrderService.createOrder(orderRequest)を呼び出す

* Then（期待結果）:
  * RuntimeExceptionがスローされる
  * エラーメッセージに"在庫不足"が含まれる
  * orderTranDao.insert()は呼び出されない

---

### 2.10 OrderService - 注文履歴取得

#### Feature: 注文履歴の取得

#### Scenario: 顧客の注文履歴を取得

* Given（前提条件）:
  * OrderTranDaoがモック化されている
  * 顧客ID=1の注文履歴が存在する

* When（操作）:
  * OrderService.getOrderHistory(1)を呼び出す

* Then（期待結果）:
  * orderTranDao.findByCustomerId(1)が呼び出される
  * 注文履歴のリストが返される

---

### 2.11 OrderResource - 注文作成API

#### Feature: 注文作成APIエンドポイント

#### Scenario: 注文作成が成功

* Given（前提条件）:
  * OrderServiceがモック化されている
  * AuthenticatedUserがモック化されている
  * orderService.createOrder()が正常にOrderTranを返す

* When（操作）:
  * OrderResource.createOrder(orderRequest)を呼び出す

* Then（期待結果）:
  * HTTPステータス201 Createdが返される
  * レスポンスボディにOrderResponseが含まれる
  * orderService.createOrder()が1回呼び出される

#### Scenario: 在庫不足時に400 Bad Requestを返す

* Given（前提条件）:
  * OrderServiceがモック化されている
  * orderService.createOrder()が"在庫不足"を含むRuntimeExceptionをスローする

* When（操作）:
  * OrderResource.createOrder(orderRequest)を呼び出す

* Then（期待結果）:
  * HTTPステータス400 Bad Requestが返される
  * レスポンスボディにErrorResponseが含まれる
  * エラーメッセージに"在庫不足"が含まれる

---

### 2.12 OrderResource - 注文履歴取得API

#### Feature: 注文履歴取得APIエンドポイント

#### Scenario: 注文履歴を取得

* Given（前提条件）:
  * OrderServiceがモック化されている
  * AuthenticatedUserがモック化されている
  * authenticatedUser.getCustomerId()が1を返す
  * orderService.getOrderHistory(1)が注文リストを返す

* When（操作）:
  * OrderResource.getOrderHistory()を呼び出す

* Then（期待結果）:
  * HTTPステータス200 OKが返される
  * レスポンスボディにList<OrderResponse>が含まれる
  * orderService.getOrderHistory()が1回呼び出される

---

### 2.13 OrderResource - 注文詳細取得API

#### Feature: 注文詳細取得APIエンドポイント

#### Scenario: 注文詳細を取得

* Given（前提条件）:
  * OrderServiceがモック化されている
  * orderService.getOrderDetail(1)が注文情報を返す

* When（操作）:
  * OrderResource.getOrderDetail(1)を呼び出す

* Then（期待結果）:
  * HTTPステータス200 OKが返される
  * レスポンスボディにOrderResponseが含まれる
  * orderService.getOrderDetail(1)が1回呼び出される

#### Scenario: 注文が見つからない場合に404 Not Foundを返す

* Given（前提条件）:
  * OrderServiceがモック化されている
  * orderService.getOrderDetail(999)がnullを返す

* When（操作）:
  * OrderResource.getOrderDetail(999)を呼び出す

* Then（期待結果）:
  * HTTPステータス404 Not Foundが返される
  * レスポンスボディにErrorResponseが含まれる
  * エラーメッセージに"注文が見つかりません"が含まれる

---

## 3. モック化の方針

### 3.1 ドメイン内の依存関係

* OrderTranエンティティ、OrderDetailエンティティ、OrderDetailPK → モック不要（実際のオブジェクトを使用）

### 3.2 ドメイン外の依存関係

* EntityManager → モック化
* TypedQuery → モック化
* BackOfficeRestClient → モック化（OrderServiceテスト時）
* DeliveryFeeService → モック化（OrderServiceテスト時）
* AuthenticatedUser → モック化（OrderService/OrderResourceテスト時）
* OrderService → モック化（OrderResourceテスト時）
* OrderTranDao → モック化（OrderServiceテスト時）
* OrderDetailDao → モック化（OrderServiceテスト時）

---

## 4. カバレッジ目標

* ステートメントカバレッジ: 80%以上
* ブランチカバレッジ: 70%以上

---

## 5. 受入基準

### 5.1 機能要件

* [ ] すべての正常系テストが成功する
* [ ] すべての異常系テストが成功する
* [ ] 境界値テストが成功する（該当する場合）
* [ ] スナップショットパターンの動作が検証される
* [ ] 双方向リレーションの動作が検証される
* [ ] 複合主キーの同値性が検証される

### 5.2 品質要件

* [ ] カバレッジ目標を達成する
* [ ] テストコードにコメントが適切に記載されている
* [ ] テストケースが独立している（テスト間の依存関係がない）
* [ ] モック化が適切に行われている

---

## 6. 参考資料

* [detailed_design.md](detailed_design.md) - 詳細設計書
* [../../basic_design/orders/functional_design.md](../../basic_design/orders/functional_design.md) - ドメイン機能設計書
* [../../basic_design/orders/behaviors.md](../../basic_design/orders/behaviors.md) - ドメイン振る舞い仕様書（結合テスト用）
* [../../requirements/behaviors.md](../../requirements/behaviors.md) - システム振る舞い仕様書（E2Eテスト用）