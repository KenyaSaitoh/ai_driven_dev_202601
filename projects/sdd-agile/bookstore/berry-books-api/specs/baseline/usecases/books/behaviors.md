# ユースケース: 書籍 - 振る舞い仕様書

ユースケースID: books  
バージョン: 1.0.0  
最終更新日: 2026-01-31

記法: Gherkin。principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照。

---

## 1. 概要

書籍一覧・詳細・検索の振る舞い。結合テストでは WireMock で back-office-api をスタブ化する。

---

## 2. テストシナリオ（Gherkin）

### Feature: 書籍一覧・詳細

```gherkin
Scenario: 書籍一覧を取得する
  Given WireMockがback-office-apiをスタブする:
    | Method | Path       | Response                                                |
    | GET    | /api/books | [{"bookId":1,"bookName":"Java入門","price":3000}]      |
  
  And このAPIではDBに書籍データを保持しない:
    検証:
      - 書籍データは back-office-api（外部API）が管理
      - berry-books-api のDBには書籍テーブルは存在しない
  
  When GET /api/books を送る
  
  Then レスポンスは 200 OK
  And 書籍配列が返る:
    件数: 1件
    データ:
      | bookId | bookName | price |
      | 1      | Java入門 | 3000  |
  
  And DBの状態は変化しない:
    検証:
      - 外部API連携のみで、DB操作は行われない

Scenario: 書籍詳細を取得する
  Given WireMockがback-office-apiをスタブする:
    | Method | Path          | Response                                                              |
    | GET    | /api/books/1  | {"bookId":1,"bookName":"Java入門","price":3000,"categoryName":"技術"} |
  
  When GET /api/books/1 を送る
  
  Then レスポンスは 200 OK
  And 書籍詳細が返る:
    | bookId | bookName | price | categoryName |
    | 1      | Java入門 | 3000  | 技術         |
  
  And DBの状態は変化しない:
    検証:
      - 外部API連携のみで、DB操作は行われない

Scenario: 存在しない書籍IDで詳細を取得する
  Given WireMockがback-office-apiをスタブする:
    | Method | Path            | Response      |
    | GET    | /api/books/999  | 404 Not Found |
  
  When GET /api/books/999 を送る
  
  Then レスポンスは 404 Not Found
  
  And DBの状態は変化しない:
    検証:
      - 外部API連携のみで、DB操作は行われない
```

### Feature: 書籍検索

```gherkin
Scenario: カテゴリIDで検索する
  Given WireMockがback-office-apiをスタブする:
    | Method | Path                                | Response                                           |
    | GET    | /api/books/search/jpql?categoryId=1 | [{"bookId":1,"bookName":"Java入門","categoryId":1}] |
  
  When GET /api/books/search/jpql?categoryId=1 を送る
  
  Then レスポンスは 200 OK
  And 該当書籍配列が返る:
    件数: 1件
    データ:
      | bookId | bookName | categoryId |
      | 1      | Java入門 | 1          |
  
  And DBの状態は変化しない:
    検証:
      - 外部API連携のみで、DB操作は行われない
```

---

## 3. DBUnitデータセット対応表

| シナリオ | 初期データセット | 期待データセット | 検証テーブル | 備考 |
|---------|----------------|----------------|------------|------|
| 書籍一覧を取得する | （なし） | （なし） | （なし） | 外部API連携のみ、DB操作なし |
| 書籍詳細を取得する | （なし） | （なし） | （なし） | 外部API連携のみ、DB操作なし |
| 存在しない書籍IDで詳細を取得する | （なし） | （なし） | （なし） | 外部API連携のみ、DB操作なし |
| カテゴリIDで検索する | （なし） | （なし） | （なし） | 外部API連携のみ、DB操作なし |

**注意:**
* このユースケースは外部API（back-office-api）連携がメインであり、berry-books-api のDBは操作しない
* 書籍データは back-office-api が管理
* テストは WireMock によるスタブ化が中心
* DBUnitは使用しない（DB操作がないため）

---

## 4. 参照

* [userstory.md](userstory.md)
* [../../common/external_interface.md](../../common/external_interface.md)
* WireMock公式ドキュメント: https://wiremock.org/
