# ユースケース: 認証（ログイン・ログアウト）

ユースケースID: auth  
バージョン: 1.0.0  
最終更新日: 2026-01-31  
ステータス: 確定

---

## 1. ユーザーストーリー

**As a** 社員（バックオフィス利用者）  
**I want to** 社員コードとパスワードでログインし、ログアウトできる  
**So that** 書籍・在庫・ワークフロー管理等の機能を利用できる

---

## 2. 受入基準

* AC1: 正しい社員コード・パスワードでログインすると JWT が HttpOnly Cookie で返り、以降のリクエストで認証される
* AC2: 誤ったパスワードや存在しない社員コードの場合は 401 Unauthorized が返る
* AC3: ログアウトで Cookie が無効化される
* AC4: JWT Payload に employeeId, employeeCode, departmentId を含む。有効期限 24 時間

---

## 3. 概要

社員情報は本システムの EMPLOYEE テーブルで管理。パスワードは BCrypt。JWT は本システムで生成・検証。

---

## 4. API仕様

| メソッド | パス | 説明 |
|---------|------|------|
| POST | /api/auth/login | ログイン（社員コード・パスワード） |
| POST | /api/auth/logout | ログアウト |

* 入力: employeeCode, password
* 出力: 200 OK + LoginResponse + Set-Cookie / 401 ErrorResponse
* 参照: [../../common/architecture_design.md](../../common/architecture_design.md), [../../common/data_model.md](../../common/data_model.md)（EMPLOYEE）

---

## 5. 参照

* [../../common/data_model.md](../../common/data_model.md) - EMPLOYEE, DEPARTMENT
* [../../common/architecture_design.md](../../common/architecture_design.md) - JWT・認証除外
* [behaviors.md](behaviors.md) - 本ユースケースの振る舞い
