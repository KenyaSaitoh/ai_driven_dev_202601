# ユースケース: 認証 - 振る舞い仕様書

ユースケースID: auth  
バージョン: 1.0.0  
最終更新日: 2026-01-31

記法: Gherkin（Feature, Scenario, Given, When, Then）。agent_skills/jakarta-ee-api-agile/principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照。

---

## 1. 概要

認証（ログイン・ログアウト・登録・現在ユーザー取得）の振る舞いとテストシナリオ。結合テスト・E2Eテストの参照元。

---

## 2. テストシナリオ（Gherkin）

### Feature: ログイン

```gherkin
Scenario: 正しいメール・パスワードでログインする
  Given customer-hub-api に顧客が存在する（email="alice@gmail.com", password=BCryptハッシュ）
  When POST /api/auth/login に {"email":"alice@gmail.com","password":"password123"} を送る
  Then レスポンスは 200 OK
  And HttpOnly Cookie に JWT が設定される
  And レスポンスボディに顧客情報が含まれる

Scenario: パスワードが誤っている
  Given customer-hub-api に顧客が存在する（email="alice@gmail.com"）
  When POST /api/auth/login に {"email":"alice@gmail.com","password":"wrong"} を送る
  Then レスポンスは 401 Unauthorized

Scenario: 顧客が存在しない
  Given customer-hub-api に該当顧客が存在しない
  When POST /api/auth/login に {"email":"notfound@example.com","password":"password123"} を送る
  Then レスポンスは 401 Unauthorized
```

### Feature: ログアウト

```gherkin
Scenario: ログアウトで Cookie が無効化される
  Given 認証済み（JWT Cookie が設定されている）
  When POST /api/auth/logout を送る
  Then レスポンスは 200 OK
  And JWT Cookie が削除または無効化される
```

### Feature: 新規登録

```gherkin
Scenario: 新規顧客を登録する
  Given メールアドレスが未登録である
  When POST /api/auth/register に顧客情報を送る
  Then レスポンスは 200 OK
  And customer-hub-api に顧客が作成される
  And レスポンスに作成された顧客情報が含まれる

Scenario: メールアドレスが重複している
  Given customer-hub-api に同じメールの顧客が既に存在する
  When POST /api/auth/register にそのメールで登録する
  Then レスポンスは 409 Conflict
```

---

## 3. 受入基準との対応

| 受入基準 | 対応シナリオ |
|---------|-------------|
| AC1 | Scenario: 正しいメール・パスワードでログインする |
| AC2 | Scenario: パスワードが誤っている / 顧客が存在しない |
| AC3 | Scenario: ログアウトで Cookie が無効化される |
| AC4 | Scenario: 新規顧客を登録する / メールアドレスが重複している |
| AC5 | GET /api/auth/me の Scenario（詳細は上記と同様の形式で追加可能） |

---

## 4. 参照

* [userstory.md](userstory.md) - ユーザーストーリー・受入基準
* [../../common/architecture_design.md](../../common/architecture_design.md) - 共通アーキテクチャ
* [../../common/external_interface.md](../../common/external_interface.md) - customer-hub-api 連携
