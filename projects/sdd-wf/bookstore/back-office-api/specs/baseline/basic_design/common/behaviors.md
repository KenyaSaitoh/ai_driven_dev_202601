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
    Given DBに以下の社員が存在する:
      テーブル: EMPLOYEE
      件数: 1件
      データセット: /datasets/common/initial-employee.xml
      データ:
        | EMPLOYEE_ID | EMPLOYEE_CODE | PASSWORD                      | DEPARTMENT_ID | NAME   |
        | 1           | EMP001        | $2a$10$...（BCryptハッシュ）    | 1             | 山田太郎 |
    
    When EmployeeService.authenticate("EMP001", "password123")を呼び出す
    
    Then 認証が成功する
    And Employeeエンティティが返される:
      | employeeId | employeeCode | departmentId |
      | 1          | EMP001       | 1            |
    
    And DBの状態は変化しない:
      テーブル: EMPLOYEE
      件数: 1件（変更なし）
      検証:
        - 認証処理はREAD操作のみ、DBは更新されない

  Scenario: パスワード不一致で認証失敗
    Given DBに以下の社員が存在する:
      テーブル: EMPLOYEE
      件数: 1件
      データセット: /datasets/common/initial-employee.xml
      データ:
        | EMPLOYEE_ID | EMPLOYEE_CODE | PASSWORD    | DEPARTMENT_ID |
        | 1           | EMP001        | $2a$10$...  | 1             |
    
    When EmployeeService.authenticate("EMP001", "wrongpassword")を呼び出す
    
    Then AuthenticationExceptionがスローされる
    
    And DBの状態は変化しない:
      テーブル: EMPLOYEE
      件数: 1件（変更なし）
      検証:
        - 認証エラーのため、DB操作は行われない

  Scenario: 存在しない社員コードで認証失敗
    Given DBに社員が存在しない:
      テーブル: EMPLOYEE
      件数: 0件
    
    When EmployeeService.authenticate("NONEXIST", "password123")を呼び出す
    
    Then AuthenticationExceptionがスローされる
    
    And DBの状態は変化しない:
      テーブル: EMPLOYEE
      件数: 0件（変更なし）
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
      テーブル: BOOK
      件数: 3件
      データセット: /datasets/common/initial-books-by-category.xml
      データ:
        | BOOK_ID | BOOK_NAME     | CATEGORY_ID | PRICE |
        | 1       | Java完全理解  | 1           | 3000  |
        | 2       | Spring入門    | 1           | 2500  |
        | 3       | 文学作品      | 2           | 2000  |
    
    When BookDao.searchBooks(categoryId=1, keyword=null)を呼び出す
    
    Then カテゴリID=1の書籍2件が返される:
      データ:
        | bookName     |
        | Java完全理解 |
        | Spring入門   |
    
    And DBの状態は変化しない:
      テーブル: BOOK
      件数: 3件（変更なし）
      検証:
        - READ操作のため、DBは更新されない

  Scenario: キーワードで書籍を検索
    Given DBに以下の書籍が存在する:
      テーブル: BOOK
      件数: 2件
      データセット: /datasets/common/initial-books-for-keyword-search.xml
      データ:
        | BOOK_ID | BOOK_NAME        | CATEGORY_ID |
        | 1       | Java完全理解     | 1           |
        | 2       | JavaScript入門   | 1           |
    
    When BookDao.searchBooks(categoryId=null, keyword="Java")を呼び出す
    
    Then "Java"を含む書籍2件が返される:
      データ:
        | bookName        |
        | Java完全理解    |
        | JavaScript入門  |
    
    And DBの状態は変化しない:
      テーブル: BOOK
      件数: 2件（変更なし）
```

### 3.2 StockDao - 在庫管理

#### Feature: 在庫数を更新（楽観的ロック）

```gherkin
Feature: 在庫更新
  在庫数を更新する（楽観的ロック対応）

  Scenario: 在庫数を更新（正常系）
    Given DBに以下の在庫が存在する:
      テーブル: STOCK
      件数: 1件
      データセット: /datasets/common/initial-stock-before-update.xml
      データ:
        | BOOK_ID | QUANTITY | VERSION |
        | 1       | 10       | 1       |
    
    When StockDao.updateStock(bookId=1, quantity=15, version=1)を呼び出す
    
    Then DBの在庫テーブルは以下になる:
      テーブル: STOCK
      件数: 1件（変更なし）
      データセット: /datasets/common/expected-stock-updated.xml
      データ:
        | BOOK_ID | QUANTITY | VERSION |
        | 1       | 15       | 2       |
      検証:
        - QUANTITY が 10 から 15 に更新される
        - VERSION が 1 から 2 にインクリメントされる

  Scenario: 楽観的ロック競合検知
    Given DBに以下の在庫が存在する:
      テーブル: STOCK
      件数: 1件
      データセット: /datasets/common/initial-stock-version-conflict.xml
      データ:
        | BOOK_ID | QUANTITY | VERSION |
        | 1       | 10       | 2       |
    
    When StockDao.updateStock(bookId=1, quantity=15, version=1)を呼び出す（古いバージョン）
    
    Then OptimisticLockExceptionがスローされる
    
    And DBの在庫テーブルは変化しない:
      テーブル: STOCK
      件数: 1件（変更なし）
      データ:
        | BOOK_ID | QUANTITY | VERSION |
        | 1       | 10       | 2       |
      検証:
        - 在庫は更新されない
        - VERSION は 2 のまま変化しない
```

---

## 4. エンティティのリレーションテスト

### 4.1 Book - Category - Publisher のリレーション

#### Feature: 書籍詳細取得（リレーション含む）

```gherkin
Feature: 書籍詳細取得
  書籍詳細を取得（カテゴリ、出版社、在庫を含む）

  Scenario: 書籍詳細を取得
    Given DBに以下の書籍が存在する:
      テーブル: BOOK
      件数: 1件
      データセット: /datasets/common/initial-book-detail.xml
      データ:
        | BOOK_ID | BOOK_NAME    | CATEGORY_ID | PUBLISHER_ID | PRICE |
        | 1       | Java完全理解 | 1           | 1            | 3000  |
    
    And DBに以下のカテゴリが存在する:
      テーブル: CATEGORY
      件数: 1件
      データ:
        | CATEGORY_ID | CATEGORY_NAME    |
        | 1           | プログラミング   |
    
    And DBに以下の出版社が存在する:
      テーブル: PUBLISHER
      件数: 1件
      データ:
        | PUBLISHER_ID | PUBLISHER_NAME |
        | 1            | 技術評論社     |
    
    And DBに以下の在庫が存在する:
      テーブル: STOCK
      件数: 1件
      データ:
        | BOOK_ID | QUANTITY | VERSION |
        | 1       | 10       | 1       |
    
    When BookDao.findById(bookId=1)を呼び出す
    
    Then 書籍詳細が取得される:
      データ:
        | bookName     | categoryName   | publisherName | quantity |
        | Java完全理解 | プログラミング | 技術評論社    | 10       |
      検証:
        - BOOK, CATEGORY, PUBLISHER, STOCK のリレーションが正しく取得される
    
    And DBの状態は変化しない:
      テーブル: BOOK, CATEGORY, PUBLISHER, STOCK
      検証:
        - READ操作のため、DBは更新されない
```

---

## 5. DBUnitデータセット対応表

| シナリオ | 初期データセット | 期待データセット | 検証テーブル |
|---------|----------------|----------------|------------|
| 正しいパスワードで認証成功 | `/datasets/common/initial-employee.xml` | （変更なし） | EMPLOYEE |
| パスワード不一致で認証失敗 | `/datasets/common/initial-employee.xml` | （変更なし） | EMPLOYEE |
| 存在しない社員コードで認証失敗 | （空） | （変更なし） | EMPLOYEE |
| カテゴリで書籍を検索 | `/datasets/common/initial-books-by-category.xml` | （変更なし） | BOOK |
| キーワードで書籍を検索 | `/datasets/common/initial-books-for-keyword-search.xml` | （変更なし） | BOOK |
| 在庫数を更新（正常系） | `/datasets/common/initial-stock-before-update.xml` | `/datasets/common/expected-stock-updated.xml` | STOCK |
| 楽観的ロック競合検知 | `/datasets/common/initial-stock-version-conflict.xml` | （変更なし） | STOCK |
| 書籍詳細を取得 | `/datasets/common/initial-book-detail.xml` | （変更なし） | BOOK<br>CATEGORY<br>PUBLISHER<br>STOCK |

---

## 6. 参考資料

* [functional_design.md](functional_design.md) - 共通機能設計書
* [data_model.md](data_model.md) - データモデル仕様書
* DBUnit公式ドキュメント: http://dbunit.sourceforge.net/
