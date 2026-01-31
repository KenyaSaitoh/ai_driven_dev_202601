# ユースケース: カテゴリ一覧取得

ユースケースID: category  
バージョン: 1.0.0  
最終更新日: 2026-01-31  
ステータス: 確定

---

## 1. ユーザーストーリー

**As a** 社員  
**I want to** カテゴリ一覧を取得できる  
**So that** 書籍のカテゴリを参照・検索に使える

---

## 2. 受入基準

* AC1: カテゴリ一覧を取得できる（GET /api/categories）。配列またはマップ形式で返す（プロジェクト仕様に従う）

---

## 3. 概要

CATEGORY テーブルを本システムで管理。CategoryResource が CategoryService → CategoryDao で取得。

---

## 4. API仕様

| メソッド | パス | 説明 |
|---------|------|------|
| GET | /api/categories | カテゴリ一覧 |

* 参照: [../../common/data_model.md](../../common/data_model.md)（CATEGORY）

---

## 5. 参照

* [../../common/data_model.md](../../common/data_model.md)
* [behaviors.md](behaviors.md)
