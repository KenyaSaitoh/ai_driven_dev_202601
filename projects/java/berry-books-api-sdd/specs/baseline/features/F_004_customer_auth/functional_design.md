# F-004: 顧客管理・認証 - 機能設計書

**機能ID:** F-004  
**機能名:** 顧客管理・認証  
**バージョン:** 1.1.1  
**最終更新日:** 2025-12-21

**変更履歴:**
- v1.1.1 (2025-12-21): バグ修正 - 新規顧客登録後にログイン状態を自動設定するよう修正
- v1.1.0 (2025-12-16): 初版

---

## 1. 概要

本文書は、顧客管理・認証機能の詳細設計を記述します。

**関連ドキュメント:**
- [../../system/requirements.md](../../system/requirements.md) - システム要件定義書
- [../../system/architecture_design.md](../../system/architecture_design.md) - アーキテクチャ設計書
- [behaviors.md](behaviors.md) - 振る舞い仕様書（Acceptance Criteria）
- [screen_design.md](screen_design.md) - 画面設計書

---

## 2. ユーザーストーリー

```
As a 新規顧客
I want to アカウントを登録してログインする
So that 書籍を購入し、注文履歴を管理できる
```

---

## 3. ビジネスルール

| ルールID | 説明 |
|---------|-------------|
| BR-030 | メールアドレスは一意（重複不可） |
| BR-031 | パスワードは平文保存（学習用のみ、本番環境では非推奨） |
| BR-032 | セッションタイムアウト: 60分 |
| BR-033 | 公開ページ: ログイン画面、新規登録画面、登録完了画面 |
| BR-034 | 公開ページ以外は認証必須 |

---

## 4. 機能フロー

### 4.1 新規登録フロー

1. ログイン画面で「新規登録」リンクをクリック
2. 新規登録画面に遷移
3. 顧客情報を入力（氏名、メールアドレス、パスワード、生年月日、住所）
4. 「登録」ボタンをクリック
5. システムがメールアドレスの重複をチェック
6. システムが顧客情報を登録
7. **システムが自動的にログイン状態を設定（LoginBean.setLoggedIn(true)）**
8. 登録完了画面に遷移
9. 登録完了画面から書籍選択ページへアクセス可能（認証済み状態）

### 4.2 ログインフロー

1. ログイン画面でメールアドレスとパスワードを入力
2. 「ログイン」ボタンをクリック
3. システムが認証情報を確認
4. 認証成功の場合、書籍検索画面に遷移
5. 認証失敗の場合、エラーメッセージを表示

### 4.3 ログアウトフロー

1. 「ログアウト」リンクをクリック
2. システムがセッションをクリア
3. ログイン画面に遷移

---

## 5. クラス設計

### 5.1 プレゼンテーション層

**CustomerBean**
- **責務**: ログイン中の顧客情報を保持、新規顧客登録処理
- **タイプ**: @SessionScoped Bean
- **依存関係**:
  - `CustomerService` - 顧客管理サービス
  - `LoginBean` - ログイン状態管理（新規登録後のログイン状態設定に使用）
- **フィールド**: 
  - `customer` - ログイン中の顧客エンティティ
  - `customerName`, `email`, `password`, `birthday`, `address` - 新規登録フォーム入力値
- **主要メソッド**: 
  - `getCustomer()` - 顧客情報を取得
  - `setCustomer(Customer)` - 顧客情報を設定
  - `register()` - 新規顧客登録処理（登録成功後、LoginBean.setLoggedIn(true)を呼び出してログイン状態を設定）

**LoginBean**
- **責務**: ログイン処理のコントローラーとログイン状態の管理
- **タイプ**: @SessionScoped Bean
- **依存関係**:
  - `CustomerService` - 認証サービス
  - `CustomerBean` - 顧客情報保持
- **フィールド**: 
  - `email` - メールアドレス
  - `password` - パスワード
  - `loggedIn` - ログイン済みフラグ
- **主要メソッド**: 
  - `processLogin()` - ログイン処理（認証成功時、loggedIn=trueを設定）
  - `processLogout()` - ログアウト処理
  - `isLoggedIn()` - ログイン状態確認
  - `setLoggedIn(boolean)` - ログイン状態設定（新規登録後の自動ログインに使用）

### 5.2 ビジネスロジック層

**CustomerService**
- **責務**: 顧客管理のビジネスロジック
- **タイプ**: @ApplicationScoped
- **主要メソッド**: 
  - `register(Customer)` - 顧客を登録
  - `authenticate(String email, String password)` - 認証
  - `findByEmail(String)` - メールアドレスで検索

### 5.3 REST APIクライアント層

**CustomerRestClient**
- **責務**: berry-books-rest APIへのREST API呼び出し
- **タイプ**: @ApplicationScoped
- **主要メソッド**: 
  - `getCustomer(Integer customerId)` - 顧客を取得（GET /customers/{customerId}）
  - `getCustomerByEmail(String email)` - メールアドレスで検索（GET /customers/query_email?email={email}）
  - `registerCustomer(CustomerTO customerTO)` - 顧客を登録（POST /customers/）
  - エラーハンドリング（404 Not Found, 409 Conflict, 500 Internal Server Error）

---

## 6. 認証フィルター

**AuthenticationFilter**
- **責務**: 未認証ユーザーのアクセス制限
- **タイプ**: @WebFilter（アノテーションベース）
- **動作**: 
  - 公開ページ（BR-033）以外へのアクセスをチェック
  - LoginBeanをCDIでインジェクトし、`isLoggedIn()`でログイン状態を確認
  - 未ログインの場合、ログイン画面にリダイレクト（BR-034）
  - セッションタイムアウト（BR-032）を管理

---

## 7. 例外・エラー処理

| シナリオ | 期待される動作 |
|----------|-------------------|
| メールアドレス重複 | EmailAlreadyExistsException、エラー表示 |
| ログイン失敗 | エラーメッセージ表示 |
| セッションタイムアウト | ログイン画面にリダイレクト |
| 直接URL入力（未ログイン） | ログイン画面にリダイレクト |
| 新規登録成功 | CustomerBean.customerに顧客情報を保存、LoginBean.loggedIn=trueを設定 |

---

## 8. 受入基準

詳細は [behaviors.md](behaviors.md) を参照

