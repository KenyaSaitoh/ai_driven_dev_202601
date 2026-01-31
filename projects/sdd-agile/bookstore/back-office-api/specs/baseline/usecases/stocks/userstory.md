# ユースケース: 在庫一覧・取得・更新

ユースケースID: stocks  
バージョン: 1.0.0  
最終更新日: 2026-01-31  
ステータス: 確定

---

## 1. ユーザーストーリー

**As a** 社員  
**I want to** 在庫一覧・在庫詳細を取得し、在庫数を更新できる  
**So that** 在庫を正確に管理できる

---

## 2. 受入基準

* AC1: 在庫一覧を取得できる（GET /api/stocks）
* AC2: 書籍IDで在庫情報を取得できる（GET /api/stocks/{bookId}）。存在しなければ 404
* AC3: 在庫数を更新できる（PUT /api/stocks/{bookId}）。リクエストに quantity, version を含む。楽観的ロック（@Version）で競合時は 409 Conflict

---

## 3. 概要

STOCK テーブルを本システムで管理。楽観的ロック（VERSION）で更新。StockResource → StockService → StockDao。

---

## 4. API仕様

| メソッド | パス | 説明 |
|---------|------|------|
| GET | /api/stocks | 在庫一覧 |
| GET | /api/stocks/{bookId} | 在庫詳細 |
| PUT | /api/stocks/{bookId} | 在庫更新（body: quantity, version） |

* 楽観的ロック: 更新時に version を送信。DB の version と一致しない場合は 409 Conflict（OptimisticLockExceptionMapper）
* 参照: [../../common/data_model.md](../../common/data_model.md)（STOCK）, [../../common/architecture_design.md](../../common/architecture_design.md)（楽観的ロック）

---

## 5. 参照

* [../../common/data_model.md](../../common/data_model.md)
* [../../common/architecture_design.md](../../common/architecture_design.md)
* [behaviors.md](behaviors.md)
