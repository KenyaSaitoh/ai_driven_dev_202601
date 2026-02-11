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
  Given WireMockがcustomer-hub-apiをスタブする:
    | Method | Path              | Response                                               |
    | POST   | /api/auth/login   | 200 OK, {"customerId":1,"email":"alice@gmail.com"}   |
  
  And このAPIではDBに顧客データを保持しない:
    検証:
      - 顧客データは customer-hub-api（外部API）が管理
      - berry-books-api のDBには顧客テーブルは存在しない
  
  When POST /api/auth/login に {"email":"alice@gmail.com","password":"password123"} を送る
  
  Then レスポンスは 200 OK
  And HttpOnly Cookie に JWT が設定される
  And レスポンスボディに顧客情報が含まれる:
    | customerId | email              |
    | 1          | alice@gmail.com    |
  
  And DBの状態は変化しない:
    検証:
      - 認証はステートレス（JWTトークン方式）
      - DBにセッション情報は保存しない
      - 外部API呼び出しのみでDB更新なし

Scenario: パスワードが誤っている
  Given WireMockがcustomer-hub-apiをスタブする:
    | Method | Path            | Response           |
    | POST   | /api/auth/login | 401 Unauthorized   |
  
  When POST /api/auth/login に {"email":"alice@gmail.com","password":"wrong"} を送る
  
  Then レスポンスは 401 Unauthorized
  
  And DBの状態は変化しない:
    検証:
      - 認証エラーのため、DB操作は行われない

Scenario: 顧客が存在しない
  Given WireMockがcustomer-hub-apiをスタブする:
    | Method | Path            | Response           |
    | POST   | /api/auth/login | 404 Not Found      |
  
  When POST /api/auth/login に {"email":"notfound@example.com","password":"password123"} を送る
  
  Then レスポンスは 401 Unauthorized
  
  And DBの状態は変化しない:
    検証:
      - 顧客が存在しないため、DB操作は行われない
```

### Feature: ログアウト

```gherkin
Scenario: ログアウトで Cookie が無効化される
  Given 認証済み（JWT Cookie が設定されている）
  
  When POST /api/auth/logout を送る
  
  Then レスポンスは 200 OK
  And JWT Cookie が削除または無効化される
  
  And DBの状態は変化しない:
    検証:
      - ログアウトはステートレス（Cookieクリアのみ）
      - DBにセッション情報は保存していないため、DB操作は行われない
```

### Feature: 新規登録

```gherkin
Scenario: 新規顧客を登録する
  Given WireMockがcustomer-hub-apiをスタブする:
    | Method | Path               | Response                                          |
    | POST   | /api/customers     | 201 Created, {"customerId":1,"email":"new@example.com"} |
  
  When POST /api/auth/register に顧客情報を送る:
    | email           | password     | name      |
    | new@example.com | password123  | 新規ユーザー |
  
  Then レスポンスは 200 OK
  And レスポンスに作成された顧客情報が含まれる:
    | customerId | email           |
    | 1          | new@example.com |
  
  And DBの状態は変化しない:
    検証:
      - 顧客データは customer-hub-api（外部API）が管理
      - berry-books-api のDBには顧客テーブルは存在しない
      - 外部API呼び出しのみでDB更新なし

Scenario: メールアドレスが重複している
  Given WireMockがcustomer-hub-apiをスタブする:
    | Method | Path           | Response        |
    | POST   | /api/customers | 409 Conflict    |
  
  When POST /api/auth/register にそのメールで登録する:
    | email              | password    |
    | existing@example.com | password123 |
  
  Then レスポンスは 409 Conflict
  
  And DBの状態は変化しない:
    検証:
      - メールアドレス重複のため、顧客は作成されない
      - DB操作は行われない
```

---

## 3. DBUnitデータセット対応表

| シナリオ | 初期データセット | 期待データセット | 検証テーブル | 備考 |
|---------|----------------|----------------|------------|------|
| 正しいメール・パスワードでログインする | （なし） | （なし） | （なし） | 外部API連携のみ、DB操作なし |
| パスワードが誤っている | （なし） | （なし） | （なし） | 外部API連携のみ、DB操作なし |
| 顧客が存在しない | （なし） | （なし） | （なし） | 外部API連携のみ、DB操作なし |
| ログアウト | （なし） | （なし） | （なし） | ステートレス、DB操作なし |
| 新規顧客を登録する | （なし） | （なし） | （なし） | 外部API連携のみ、DB操作なし |
| メールアドレスが重複している | （なし） | （なし） | （なし） | 外部API連携のみ、DB操作なし |

**注意:**
* このユースケースは外部API（customer-hub-api）連携がメインであり、berry-books-api のDBは操作しない
* テストは WireMock によるスタブ化と、JWT/Cookieの検証が中心
* DBUnitは使用しない（DB操作がないため）

---

## 4. 受入基準との対応

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
