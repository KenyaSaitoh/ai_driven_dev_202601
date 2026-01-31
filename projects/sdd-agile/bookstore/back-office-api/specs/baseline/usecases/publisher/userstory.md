# ユースケース: 出版社一覧取得

ユースケースID: publisher  
バージョン: 1.0.0  
最終更新日: 2026-01-31  
ステータス: 確定

---

## 1. ユーザーストーリー

**As a** 社員  
**I want to** 出版社一覧を取得できる  
**So that** 書籍の出版社を参照・検索に使える

---

## 2. 受入基準

* AC1: 出版社一覧を取得できる（GET /api/publishers）。配列形式で返す

---

## 3. 概要

PUBLISHER テーブルを本システムで管理。PublisherResource → PublisherService → PublisherDao。

---

## 4. API仕様

| メソッド | パス | 説明 |
|---------|------|------|
| GET | /api/publishers | 出版社一覧 |

* 参照: [../../common/data_model.md](../../common/data_model.md)（PUBLISHER）

---

## 5. 参照

* [../../common/data_model.md](../../common/data_model.md)
* [behaviors.md](behaviors.md)
