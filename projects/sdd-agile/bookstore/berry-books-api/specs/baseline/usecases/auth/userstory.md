# ユースケース: 認証（ログイン・ログアウト・登録）

ユースケースID: auth  
バージョン: 1.0.0  
最終更新日: 2026-01-31  
ステータス: 確定

---

## 1. ユーザーストーリー

**As a** 顧客（エンドユーザー）  
**I want to** メールアドレスとパスワードでログインし、ログアウト・新規登録ができる  
**So that** 注文や注文履歴など認証が必要な機能を利用できる

---

## 2. 受入基準

* AC1: 正しいメール・パスワードでログインすると JWT が HttpOnly Cookie で返り、以降のリクエストで認証される
* AC2: 誤ったパスワードや存在しないメールの場合は 401 Unauthorized が返る
* AC3: ログアウトで Cookie が無効化される
* AC4: 新規登録で顧客情報を customer-hub-api に登録し、メール重複時は 409 Conflict を返す
* AC5: 認証済みユーザーが現在の顧客情報を取得できる（GET /api/auth/me 相当）

---

## 3. 概要

berry-books-api は JWT を生成・検証し、顧客情報は customer-hub-api から取得する。パスワード照合は本システムで実施（BCrypt）。認証除外パスは common/architecture_design に記載。

---

## 4. API仕様

| メソッド | パス | 説明 | 認証 |
|---------|------|------|------|
| POST | /api/auth/login | ログイン（メール・パスワード） | 不要 |
| POST | /api/auth/logout | ログアウト | 不要 |
| POST | /api/auth/register | 新規登録 | 不要 |
| GET | /api/auth/me | 現在の顧客情報取得 | 必要 |

* リクエスト/レスポンス: common/architecture_design および common/external_interface（customer-hub-api）を参照。

---

## 5. ビジネスルール

* BR-AUTH-001: パスワードは BCrypt（cost=10）で照合。customer-hub-api から取得した顧客の password フィールドと比較
* BR-AUTH-002: JWT 有効期限は 24 時間。HttpOnly Cookie で管理
* BR-AUTH-003: 開発環境では平文パスワード認証を許容するオプションを記載可能（本番では不可）

---

## 6. 参照

* [../../common/data_model.md](../../common/data_model.md) - 共通データモデル（CUSTOMER は外部API管理）
* [../../common/external_interface.md](../../common/external_interface.md) - customer-hub-api 連携
* [../../common/architecture_design.md](../../common/architecture_design.md) - 認証除外エンドポイント・JWT
* [behaviors.md](behaviors.md) - 本ユースケースの振る舞い
