# ユースケース: 書籍 - 振る舞い仕様書

ユースケースID: books  
バージョン: 1.0.0  
最終更新日: 2026-01-31

記法: Gherkin。

---

## 1. 概要

書籍一覧・詳細・検索の振る舞い。

---

## 2. テストシナリオ（Gherkin）

### Feature: 書籍一覧・詳細

```gherkin
Scenario: 書籍一覧を取得する
  Given DB に書籍が存在する
  When GET /api/books を送る
  Then レスポンスは 200 OK
  And 書籍配列が返る（在庫・カテゴリ・出版社含む）

Scenario: 書籍詳細を取得する
  Given DB に書籍ID=1 が存在する
  When GET /api/books/1 を送る
  Then レスポンスは 200 OK
  And 書籍詳細が返る

Scenario: 存在しない書籍IDで詳細を取得する
  When GET /api/books/999 を送る
  Then レスポンスは 404 Not Found
```

### Feature: 書籍検索

```gherkin
Scenario: カテゴリIDで検索する（JPQL）
  Given DB にカテゴリID=1 の書籍が存在する
  When GET /api/books/search/jpql?categoryId=1 を送る
  Then レスポンスは 200 OK
  And 該当書籍配列が返る
```

---

## 3. 参照

* [userstory.md](userstory.md)
* [../../common/data_model.md](../../common/data_model.md)
