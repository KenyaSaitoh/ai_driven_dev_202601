# 書籍API連携ドメイン - 機能設計書

プロジェクトID: berry-books-api  
ドメイン: books_proxy  
バージョン: 1.0.0  
最終更新日: 2026-02-05

---

## 1. 概要

本ドキュメントは、書籍API連携ドメインの機能を定義する。書籍情報はback-office-api経由で取得する。

* 実装方式: 外部API呼び出し（プロキシ転送）
* 書籍情報の永続化: なし（すべて外部APIから取得）

---

## 2. 機能一覧

### 2.1 書籍API

| 機能ID | 機能名 | 説明 |
|--------|--------|------|
| API_002 | 書籍API | back-office-apiから書籍情報を取得 |

---

## 3. API詳細設計

### 3.1 書籍一覧取得

#### 3.1.1 エンドポイント

* メソッド: GET
* パス: `/api/books`
* 認証: 不要

#### 3.1.2 入力

なし

#### 3.1.3 処理フロー

1. back-office-api の `/api/books` を呼び出し
2. レスポンスをそのまま返却

#### 3.1.4 出力

* 成功（200 OK）: List<BookTO>
* エラー（500 Internal Server Error）: ErrorResponse

---

（以下、書籍詳細取得、書籍検索等の詳細設計を記載してください）

---

## 4. 外部API仕様

* 外部API: back-office-api
* エンドポイント: `http://localhost:8080/back-office-api/api/books`
* 詳細は `../common/external_interface.md` を参照

---

## 5. 参考資料

* [../common/architecture_design.md](../common/architecture_design.md) - アーキテクチャ設計書
* [../common/external_interface.md](../common/external_interface.md) - 外部インターフェース仕様書
* [behaviors.md](behaviors.md) - 書籍API連携ドメインの振る舞い仕様書（結合テスト用）
