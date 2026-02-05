# 共通ドメイン - 結合テスト仕様書

プロジェクトID: back-office-api  
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

**テスト対象外:**
* API層（Resource、JAX-RS）

---

## 2. 認証機能のシナリオ

### 2.1 EmployeeService - 社員認証

#### Feature: 社員認証（パスワード検証）

```gherkin
Feature: 社員認証
  社員コードとパスワードで認証を行う

  Scenario: 正しいパスワードで認証成功
    Given DBに社員が存在する:
      | employeeCode | password                    | departmentId |
      | EMP001       | $2a$10$...（BCryptハッシュ） | 1            |
    When EmployeeService.authenticate("EMP001", "password123")を呼び出す
    Then 認証が成功する
    And Employeeエンティティが返される

  Scenario: パスワード不一致で認証失敗
    Given DBに社員が存在する:
      | employeeCode | password            | departmentId |
      | EMP001       | $2a$10$...         | 1            |
    When EmployeeService.authenticate("EMP001", "wrongpassword")を呼び出す
    Then AuthenticationExceptionがスローされる

  Scenario: 存在しない社員コードで認証失敗
    Given DBに社員が存在しない
    When EmployeeService.authenticate("NONEXIST", "password123")を呼び出す
    Then AuthenticationExceptionがスローされる
```

---

## 3. Dao層のデータアクセスシナリオ

### 3.1 BookDao - 書籍検索

#### Feature: カテゴリで書籍を検索

```gherkin
Feature: 書籍検索
  カテゴリIDとキーワードで書籍を検索する

  Scenario: カテゴリで書籍を検索
    Given DBに以下の書籍が存在する:
      | bookId | bookName      | categoryId |
      | 1      | Java完全理解   | 1          |
      | 2      | Spring入門     | 1          |
      | 3      | 文学作品       | 2          |
    When BookDao.searchBooks(categoryId=1, keyword=null)を呼び出す
    Then カテゴリID=1の書籍2件が返される:
      | bookName      |
      | Java完全理解   |
      | Spring入門     |

  Scenario: キーワードで書籍を検索
    Given DBに書籍が存在する:
      | bookId | bookName          | categoryId |
      | 1      | Java完全理解       | 1          |
      | 2      | JavaScript入門     | 1          |
    When BookDao.searchBooks(categoryId=null, keyword="Java")を呼び出す
    Then "Java"を含む書籍2件が返される
```

### 3.2 StockDao - 在庫管理

#### Feature: 在庫数を更新（楽観的ロック）

```gherkin
Feature: 在庫更新
  在庫数を更新する（楽観的ロック対応）

  Scenario: 在庫数を更新（正常系）
    Given DBに在庫が存在する:
      | bookId | quantity | version |
      | 1      | 10       | 1       |
    When StockDao.updateStock(bookId=1, quantity=15, version=1)を呼び出す
    Then DBの在庫が更新される:
      | bookId | quantity | version |
      | 1      | 15       | 2       |

  Scenario: 楽観的ロック競合検知
    Given DBに在庫が存在する:
      | bookId | quantity | version |
      | 1      | 10       | 2       |
    When StockDao.updateStock(bookId=1, quantity=15, version=1)を呼び出す（古いバージョン）
    Then OptimisticLockExceptionがスローされる
    And DBの在庫は更新されない
```

---

## 4. エンティティのリレーションテスト

### 4.1 Book - Category - Publisher のリレーション

#### Feature: 書籍詳細取得（リレーション含む）

```gherkin
Feature: 書籍詳細取得
  書籍詳細を取得（カテゴリ、出版社、在庫を含む）

  Scenario: 書籍詳細を取得
    Given DBに書籍とリレーションデータが存在する:
      | bookId | bookName      | categoryId | publisherId |
      | 1      | Java完全理解   | 1          | 1           |
    And Category(id=1, name="プログラミング")が存在する
    And Publisher(id=1, name="技術評論社")が存在する
    And Stock(bookId=1, quantity=10)が存在する
    When BookDao.findById(bookId=1)を呼び出す
    Then 書籍詳細が取得される:
      | bookName      | categoryName      | publisherName | quantity |
      | Java完全理解   | プログラミング      | 技術評論社     | 10       |
```

---

## 5. 参考資料

* [functional_design.md](functional_design.md) - 共通機能設計書
* [data_model.md](data_model.md) - データモデル仕様書
