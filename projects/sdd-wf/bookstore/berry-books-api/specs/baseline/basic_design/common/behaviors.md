# 共通ドメイン - 結合テスト仕様書

プロジェクトID: berry-books-api  
ドメイン: common（共通ドメイン）  
バージョン: 1.0.0  
最終更新日: 2026-02-05

---

## 1. 概要

本文書は、共通ドメインのService層以下（Service + DAO + Entity + DB）の結合テスト仕様を記述する。

**テスト対象:**
* Service層のビジネスロジック
* DAO層のデータアクセス
* Entity（JPA）のマッピング
* 実際のDB操作（メモリDB）
* 認証機能（JWT生成・検証）
* 外部API呼び出し（WireMockでスタブ化）

**テスト対象外:**
* API層（Resource、JAX-RS）

---

## 2. 認証機能のシナリオ

### 2.1 CustomerService - 顧客認証（外部API連携）

#### Feature: 顧客認証

```gherkin
Feature: 顧客認証
  顧客IDとパスワードで認証を行う（外部API経由）

  Scenario: 正しいパスワードで認証成功
    Given WireMockが顧客情報APIをスタブする:
      | customerId | email            | name        |
      | 1          | test@example.com | テストユーザー |
    When CustomerService.authenticate(customerId=1, password="password123")を呼び出す
    Then 認証が成功する
    And Customerオブジェクトが返される

  Scenario: パスワード不一致で認証失敗
    Given WireMockが顧客情報APIをスタブする
    When CustomerService.authenticate(customerId=1, password="wrongpassword")を呼び出す
    Then AuthenticationExceptionがスローされる
```

---

## 3. Dao層のデータアクセスシナリオ

### 3.1 OrderDao - 注文検索

#### Feature: 注文履歴取得

```gherkin
Feature: 注文履歴取得
  顧客IDで注文履歴を取得する

  Scenario: 注文履歴を取得
    Given DBに以下の注文が存在する:
      | orderId | customerId | orderDate  |
      | 1       | 1          | 2026-01-01 |
      | 2       | 1          | 2026-01-02 |
    And 注文明細が存在する:
      | orderItemId | orderId | bookId | quantity |
      | 1           | 1       | 1      | 2        |
      | 2           | 2       | 2      | 1        |
    When OrderDao.findByCustomerId(customerId=1)を呼び出す
    Then 顧客ID=1の注文2件が返される
```

---

## 4. エンティティのリレーションテスト

### 4.1 Order - OrderItem のリレーション

#### Feature: 注文詳細取得（明細含む）

```gherkin
Feature: 注文詳細取得
  注文詳細を取得（注文明細を含む）

  Scenario: 注文詳細を取得
    Given DBに注文とリレーションデータが存在する:
      | orderId | customerId | orderDate  |
      | 1       | 1          | 2026-01-01 |
    And 注文明細が存在する:
      | orderItemId | orderId | bookId | quantity |
      | 1           | 1       | 1      | 2        |
      | 2           | 1       | 2      | 1        |
    When OrderDao.findById(orderId=1)を呼び出す
    Then 注文詳細が取得される:
      | orderId | customerId | orderItemCount |
      | 1       | 1          | 2              |
```

---

## 5. 参考資料

* [functional_design.md](functional_design.md) - 共通機能設計書
* [data_model.md](data_model.md) - データモデル仕様書
