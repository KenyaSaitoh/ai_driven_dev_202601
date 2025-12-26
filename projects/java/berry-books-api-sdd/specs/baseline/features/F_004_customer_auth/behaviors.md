# F-004: 顧客管理・認証 - 振る舞い仕様書

**機能ID:** F-004  
**機能名:** 顧客管理・認証  
**バージョン:** 1.1.1  
**最終更新日:** 2025-12-21

**変更履歴:**
- v1.1.1 (2025-12-21): バグ修正 - Scenario 6追加（新規登録後の認証状態確認）、登録後の自動ログイン処理を明記
- v1.1.0 (2025-12-16): 初版

---

## 概要

このドキュメントは、顧客管理・認証機能の外形的な振る舞いを定義する。

**関連ドキュメント:**
- [functional_design.md](functional_design.md) - 機能設計
- [screen_design.md](screen_design.md) - 画面設計
- [../../system/requirements.md](../../system/requirements.md) - システム要件定義書

---

## 画面マッピング

| 画面ID | 画面名 | ファイル名 | 認証要否 |
|--------|--------|-----------|---------|
| SC-001 | ログイン画面 | index.xhtml | 不要 |
| SC-002 | 新規登録画面 | customerInput.xhtml | 不要 |
| SC-003 | 登録完了画面 | customerOutput.xhtml | 不要 |

---

## シナリオ一覧

### Scenario 1: 新規顧客登録（正常系）

```gherkin
Given ユーザーがログイン画面（index.xhtml）にアクセスしている
When ユーザーが「新規登録」リンクをクリックする
Then 新規登録画面（customerInput.xhtml）に遷移する
When ユーザーが以下の情報を入力フォームに入力する
  | フィールド | 値 |
  | 顧客名 | Alice |
  | メールアドレス | alice@gmail.com |
  | パスワード | password123 |
  | 生年月日 | 1990-01-01 |
  | 住所 | 東京都渋谷区神南1-1-1 |
And 「登録」ボタンをクリックする
Then CustomerBean.register()が呼び出される
And CustomerService.registerCustomer()が呼び出される
And CustomerRestClient.getCustomerByEmail("alice@gmail.com")でREST API呼び出しが行われる
And 重複がないことを確認（404 Not Found）
And CustomerTOが作成される
And CustomerRestClient.registerCustomer(customerTO)でREST API経由でデータベースに保存される
And CustomerBean.customerフィールドに登録済み顧客情報が設定される
And LoginBean.setLoggedIn(true)が呼び出され、ログイン状態が設定される
And 登録完了画面（customerOutput.xhtml）に遷移する
And 登録された顧客情報が表示される
And ユーザーは認証済み状態となり、書籍選択ページなど保護ページにアクセス可能
```

**受入基準:**
- [ ] 全ての必須フィールドが入力されている場合のみ登録可能
- [ ] メールアドレスが一意性チェックされる（BR-030）
- [ ] パスワードが平文で保存される（BR-031、学習用のみ）
- [ ] 登録後、顧客IDが自動採番される
- [ ] 登録成功後、自動的にログイン状態となる（LoginBean.loggedIn=true）
- [ ] 登録完了画面から書籍選択ページへ遷移可能

---

### Scenario 2: メールアドレス重複エラー（異常系）

```gherkin
Given データベースのCUSTOMERテーブルに以下のレコードが存在する
  | email | customerName |
  | alice@gmail.com | Alice |
When ユーザーが新規登録画面（customerInput.xhtml）で同じメールアドレス「alice@gmail.com」を入力する
And 「登録」ボタンをクリックする
Then CustomerService.registerCustomer()内でCustomerRestClient.getCustomerByEmail()が実行される
And REST APIから既存の顧客レコードが返される（200 OK）
And EmailAlreadyExistsExceptionがスローされる（409 Conflictを検出）
And CustomerBeanで例外がキャッチされる
And FacesMessageが追加される: 「BIZ-001: メールアドレスが既に登録されています」
And エラーメッセージが新規登録画面に表示される
And アカウントは作成されない
```

**受入基準:**
- [ ] メールアドレスの一意性制約（BR-030）が検証される
- [ ] EmailAlreadyExistsExceptionが適切にハンドリングされる
- [ ] エラーメッセージ「BIZ-001」が表示される
- [ ] ユーザーが再入力できる

---

### Scenario 3: ログイン（正常系）

```gherkin
Given データベースに以下の顧客レコードが存在する
  | customerId | email | password | customerName |
  | 1 | alice@gmail.com | password123 | Alice |
When ユーザーがログイン画面（index.xhtml）でメールアドレス「alice@gmail.com」を入力する
And パスワードフィールドに「password123」を入力する
And 「ログイン」ボタンをクリックする
Then LoginBean.processLogin()が呼び出される
And CustomerService.authenticate("alice@gmail.com", "password123")が呼び出される
And CustomerRestClient.getCustomerByEmail()でREST API経由でCustomerTOを取得
And パスワードが一致することを確認（平文比較、BR-031）
And CustomerエンティティがCustomerBeanに保存される（@SessionScoped）
And ログインが成功する
And 書籍検索画面（bookSearch.xhtml）に遷移する
```

**受入基準:**
- [ ] 正しい認証情報でログインできる
- [ ] Customerエンティティがセッションスコープで保持される（BR-034）
- [ ] ログイン後は認証が必要なページにアクセスできる
- [ ] セッションタイムアウトは60分（BR-032）

---

### Scenario 4: ログイン失敗（異常系）

```gherkin
Given データベースに顧客「alice@gmail.com」（password: "password123"）が存在する
When ユーザーがログイン画面（index.xhtml）でメールアドレス「alice@gmail.com」を入力する
And パスワードフィールドに誤ったパスワード「wrongpassword」を入力する
And 「ログイン」ボタンをクリックする
Then LoginBean.processLogin()が呼び出される
And CustomerService.authenticate()でパスワードが一致しない
And nullが返される
And FacesMessageが追加される: 「BIZ-002: メールアドレスまたはパスワードが正しくありません」
And エラーメッセージがログイン画面に表示される
And ログインは成功しない
```

**受入基準:**
- [ ] 誤ったパスワードでログインできない
- [ ] エラーメッセージ「BIZ-002」が表示される
- [ ] セキュリティのため、メールアドレスとパスワードのどちらが誤っているか特定しない

---

### Scenario 5: 未ログインユーザーのアクセス制限

```gherkin
Given ユーザーがログインしていない（セッションにCustomerBeanが存在しない）
When ユーザーがブラウザで直接URL「/bookSearch.xhtml」にアクセスしようとする
Then AuthenticationFilterが起動する
And リクエストURIが「bookSearch.xhtml」であることを検出
And publicPagesリストをチェック（index.xhtml, customerInput.xhtml, customerOutput.xhtml）
And 「bookSearch.xhtml」が公開ページではないことを確認
And セッションからCustomerBeanを取得
And CustomerBeanが存在しないまたはCustomerBean.isLoggedIn()がfalseであることを確認
And response.sendRedirect("/berry-books/index.xhtml")が実行される
And ログイン画面（index.xhtml）にリダイレクトされる
```

**受入基準:**
- [ ] 未認証ユーザーは保護ページにアクセスできない（BR-034）
- [ ] AuthenticationFilter（@WebFilter）が全てのxhtmlページで動作する
- [ ] 公開ページ（BR-033）は認証なしでアクセスできる
- [ ] リダイレクト後のURLが正しい

---

### Scenario 6: 新規登録後の認証状態確認

```gherkin
Given ユーザーがログイン画面（index.xhtml）にアクセスしている
When ユーザーが新規顧客登録を完了する（Scenario 1と同様）
Then 登録完了画面（customerOutput.xhtml）が表示される
And CustomerBean.customerに登録済み顧客情報が保持されている
And LoginBean.isLoggedIn()がtrueを返す
When ユーザーが登録完了画面で「書籍の選択ページへ」リンクをクリックする
Then 書籍選択画面（bookSelect.xhtml）に遷移する
And AuthenticationFilterが動作する
And LoginBean.isLoggedIn()がtrueを返すため、認証チェックを通過する
And bookSelect.xhtmlが正常に表示される
And ログイン画面にリダイレクトされない
```

**受入基準:**
- [ ] 新規登録後、LoginBean.loggedInフラグがtrueに設定される
- [ ] 登録完了画面から保護ページ（書籍選択画面など）に直接遷移できる
- [ ] AuthenticationFilterが新規登録後のユーザーを認証済みとして扱う
- [ ] ログイン画面にリダイレクトされずにページ遷移が完了する

---

### Scenario 7: ログアウト

```gherkin
Given ユーザーがログイン済みの状態（CustomerBeanにCustomerが存在）
When ユーザーが書籍検索画面（bookSearch.xhtml）のナビゲーションメニューで「ログアウト」リンクをクリックする
Then LoginBean.logout()が呼び出される
And CustomerBean.logout()が呼び出される
And CustomerBeanのcustomerフィールドがnullに設定される
And CartSession.clear()が呼び出される
And HttpSession.invalidate()が実行される
And セッションが無効化される
And ログイン画面（index.xhtml）に遷移する
```

**受入基準:**
- [ ] ログアウト後、セッションが完全に無効化される
- [ ] カート内容がクリアされる
- [ ] ログアウト後、保護ページにアクセスするとログイン画面にリダイレクトされる
- [ ] ブラウザの「戻る」ボタンを押してもログイン画面にリダイレクトされる

---

## ビジネスルール参照

| ルールID | 説明 | 影響するシナリオ |
|---------|-------------|-----------------|
| BR-030 | メールアドレスは一意（重複不可） | Scenario 2 |
| BR-031 | パスワードは平文保存（学習用のみ、本番環境では非推奨） | Scenario 1, 3 |
| BR-032 | セッションタイムアウト: 60分 | Scenario 3 |
| BR-033 | 公開ページ: ログイン画面、新規登録画面、登録完了画面 | Scenario 5 |
| BR-034 | 公開ページ以外は認証必須 | Scenario 3, 5, 6 |

---

## テスト実行チェックリスト

- [ ] Scenario 1: 新規顧客登録（正常系）
- [ ] Scenario 2: メールアドレス重複エラー（異常系）
- [ ] Scenario 3: ログイン（正常系）
- [ ] Scenario 4: ログイン失敗（異常系）
- [ ] Scenario 5: 未ログインユーザーのアクセス制限
- [ ] Scenario 6: 新規登録後の認証状態確認
- [ ] Scenario 7: ログアウト

