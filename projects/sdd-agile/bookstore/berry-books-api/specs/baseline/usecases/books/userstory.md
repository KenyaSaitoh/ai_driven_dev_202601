# ユースケース: 書籍一覧・検索・詳細取得

ユースケースID: books  
バージョン: 1.0.0  
最終更新日: 2026-01-31  
ステータス: 確定

---

## 1. ユーザーストーリー

**As a** 顧客  
**I want to** 書籍一覧・検索・詳細を閲覧できる  
**So that** 購入したい書籍を選べる

---

## 2. 受入基準

* AC1: 書籍一覧を取得できる（GET /api/books）。back-office-api から取得した書籍・在庫・カテゴリ・出版社情報を返す
* AC2: 書籍IDで詳細を取得できる（GET /api/books/{bookId}）。存在しなければ 404
* AC3: カテゴリID・キーワードで検索できる（GET /api/books/search/jpql または /search/criteria）
* AC4: 認証不要で利用可能（common の認証除外に含まれる）

---

## 3. 概要

本ユースケースは外部API（back-office-api）の透過的転送。berry-books-api は BookResource で BackOfficeRestClient を呼び、書籍・在庫・カテゴリ情報をそのまま返す。データ永続化は本システムでは行わない。

---

## 4. API仕様

| メソッド | パス | 説明 |
|---------|------|------|
| GET | /api/books | 書籍一覧 |
| GET | /api/books/{bookId} | 書籍詳細 |
| GET | /api/books/search/jpql?categoryId=&keyword= | JPQL検索 |
| GET | /api/books/search/criteria?categoryId=&keyword= | Criteria API検索 |

* レスポンス構造: [../../common/external_interface.md](../../common/external_interface.md)（back-office-api連携）および [../../common/openapi/](../../common/openapi/) の OpenAPI 参照

---

## 5. 参照

* [../../common/external_interface.md](../../common/external_interface.md) - back-office-api 連携
* [../../common/architecture_design.md](../../common/architecture_design.md) - 認証除外・パッケージ
* [behaviors.md](behaviors.md) - 本ユースケースの振る舞い
