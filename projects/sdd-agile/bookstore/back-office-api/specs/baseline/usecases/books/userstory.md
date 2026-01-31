# ユースケース: 書籍一覧・検索・詳細

ユースケースID: books  
バージョン: 1.0.0  
最終更新日: 2026-01-31  
ステータス: 確定

---

## 1. ユーザーストーリー

**As a** 社員  
**I want to** 書籍一覧・検索・詳細を取得できる  
**So that** 書籍マスタを参照・管理できる

---

## 2. 受入基準

* AC1: 書籍一覧を取得できる（GET /api/books）。在庫・カテゴリ・出版社情報を含む
* AC2: 書籍IDで詳細を取得できる（GET /api/books/{bookId}）。存在しなければ 404
* AC3: カテゴリID・キーワードで検索できる（GET /api/books/search/jpql および /search/criteria）
* AC4: カテゴリ一覧を取得できる（GET /api/categories または 書籍API 経由で参照）

---

## 3. 概要

BOOK, STOCK, CATEGORY, PUBLISHER を本システムで管理。Book は @SecondaryTable で STOCK と結合。JPQL 検索と Criteria API 検索の両方を提供。

---

## 4. API仕様

| メソッド | パス | 説明 |
|---------|------|------|
| GET | /api/books | 書籍一覧 |
| GET | /api/books/{bookId} | 書籍詳細 |
| GET | /api/books/search/jpql?categoryId=&keyword= | JPQL検索 |
| GET | /api/books/search/criteria?categoryId=&keyword= | Criteria API検索 |

* 参照: [../../common/data_model.md](../../common/data_model.md)（BOOK, STOCK, CATEGORY, PUBLISHER）

---

## 5. 参照

* [../../common/data_model.md](../../common/data_model.md)
* [../../common/architecture_design.md](../../common/architecture_design.md)
* [behaviors.md](behaviors.md)
