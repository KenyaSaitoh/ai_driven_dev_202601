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
  Given WireMock が GET /api/books をスタブしている（書籍配列を返す）
  When GET /api/books を送る
  Then レスポンスは 200 OK
  And 書籍配列が返る

Scenario: 書籍詳細を取得する
  Given WireMock が GET /api/books/1 をスタブしている（書籍詳細を返す）
  When GET /api/books/1 を送る
  Then レスポンスは 200 OK
  And 書籍詳細が返る

Scenario: 存在しない書籍IDで詳細を取得する
  Given WireMock が GET /api/books/999 に対して 404 を返す
  When GET /api/books/999 を送る
  Then レスポンスは 404 Not Found
```

### Feature: 書籍検索

```gherkin
Scenario: カテゴリIDで検索する
  Given WireMock が GET /api/books/search/jpql?categoryId=1 をスタブしている
  When GET /api/books/search/jpql?categoryId=1 を送る
  Then レスポンスは 200 OK
  And 該当書籍配列が返る
```

---

## 3. 参照

* [userstory.md](userstory.md)
* [../../common/external_interface.md](../../common/external_interface.md)
