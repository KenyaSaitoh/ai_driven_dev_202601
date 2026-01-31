# ユースケース: 認証 - 振る舞い仕様書

ユースケースID: auth  
バージョン: 1.0.0  
最終更新日: 2026-01-31

記法: Gherkin。

---

## 1. 概要

認証（ログイン・ログアウト）の振る舞い。結合テスト・E2Eテストの参照元。

---

## 2. テストシナリオ（Gherkin）

### Feature: ログイン

```gherkin
Scenario: 正しい社員コード・パスワードでログインする
  Given DB に社員が存在する（employeeCode="E001", password=BCryptハッシュ）
  When POST /api/auth/login に {"employeeCode":"E001","password":"password123"} を送る
  Then レスポンスは 200 OK
  And HttpOnly Cookie に JWT が設定される
  And レスポンスボディに社員情報が含まれる

Scenario: パスワードが誤っている
  Given DB に社員が存在する（employeeCode="E001"）
  When POST /api/auth/login に {"employeeCode":"E001","password":"wrong"} を送る
  Then レスポンスは 401 Unauthorized

Scenario: 社員が存在しない
  When POST /api/auth/login に {"employeeCode":"UNKNOWN","password":"password123"} を送る
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

---

## 3. 参照

* [userstory.md](userstory.md)
* [../../common/architecture_design.md](../../common/architecture_design.md)
