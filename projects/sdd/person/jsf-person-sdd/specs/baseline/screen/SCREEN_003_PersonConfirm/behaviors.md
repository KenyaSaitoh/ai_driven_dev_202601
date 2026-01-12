# 振る舞い仕様書 - SCREEN_003_PersonConfirm

## 1. 概要

PERSON確認画面（SCREEN_003_PersonConfirm）の振る舞いを定義する。

## 2. 画面の振る舞い

### 2.1 初期表示時の動作

* トリガー: ユーザーが入力画面の「確認画面へ」ボタンをクリックし、personConfirm.xhtmlにアクセス

* 動作:

```
Given: ユーザーが入力画面でデータを入力している
  And: バリデーションが成功している
When: 確認画面へボタンをクリックする
Then:
  * personConfirm.xhtmlに遷移する
  * PersonConfirmBeanのインスタンスが作成される（@ViewScoped）
  * PersonInputBeanが注入される（@Inject）
  * init()メソッドが自動的に実行される（@PostConstruct）
  * PersonInputBeanから入力データを取得する:
    * personId = personInputBean.getPersonId()
    * personName = personInputBean.getPersonName()
    * age = personInputBean.getAge()
    * gender = personInputBean.getGender()
  * 画面に確認情報が表示される:
    * 名前: [入力された名前]
    * 年齢: [入力された年齢]
    * 性別: [男性または女性]
```

* 事後条件: 確認画面に入力データが表示される

### 2.2 登録ボタンクリック時の動作（新規追加）

* トリガー: ユーザーが確認画面の「登録」ボタンをクリック（新規追加モード）

* 動作:

```
Given: ユーザーが確認画面を表示している（新規追加モード、personIdはnull）
  And: 名前 "David"、年齢 25、性別 "male" を確認している
When: 登録ボタンをクリックする
Then:
  * PersonConfirmBean.save()メソッドが実行される
  * Personオブジェクトを作成する:
    * personId = null
    * personName = "David"
    * age = 25
    * gender = "male"
  * personIdがnullのため、新規追加モードとして処理される
  * PersonService.addPerson(person)が呼び出される
  * EntityManager.persist(person)でPersistence Contextに追加される
  * トランザクションがコミットされ、INSERTが実行される
  * データベースがPERSON_IDを自動採番する
  * personList.xhtmlにリダイレクトする
  * 一覧画面に新しいPERSONが表示される
```

* 事後条件: 新しいPERSONがデータベースに登録され、一覧画面に表示される

### 2.3 登録ボタンクリック時の動作（更新）

* トリガー: ユーザーが確認画面の「登録」ボタンをクリック（編集モード）

* 動作:

```
Given: ユーザーが確認画面を表示している（編集モード、personId=2）
  And: 名前 "Bob Updated"、年齢 21、性別 "male" を確認している
When: 登録ボタンをクリックする
Then:
  * PersonConfirmBean.save()メソッドが実行される
  * Personオブジェクトを作成する:
    * personId = 2
    * personName = "Bob Updated"
    * age = 21
    * gender = "male"
  * personIdが2のため、更新モードとして処理される
  * PersonService.updatePerson(person)が呼び出される
  * EntityManager.merge(person)でエンティティを更新する
  * トランザクションがコミットされ、UPDATEが実行される
  * データベースのpersonId=2のレコードが更新される
  * personList.xhtmlにリダイレクトする
  * 一覧画面に更新されたPERSONが表示される
```

* 事後条件: 既存のPERSONがデータベースで更新され、一覧画面に反映される

### 2.4 戻るボタンクリック時の動作

* トリガー: ユーザーが確認画面の「戻る」ボタンをクリック

* 動作:

```
Given: ユーザーが確認画面を表示している
When: 戻るボタンをクリックする
Then:
  * JavaScriptのhistory.back()が実行される
  * ブラウザ履歴を使用して入力画面に戻る
  * PersonInputBeanはまだ@ViewScopedとして有効
  * 入力データは保持される（再入力不要）
  * 入力画面が再表示される
```

* 事後条件: 入力画面に戻り、入力データが保持される

## 3. Given-When-Thenシナリオ

### 3.1 シナリオ1: 新規追加モードで登録する

```
Given: ユーザーが入力画面で名前 "Eve"、年齢 28、性別 "female" を入力した
  And: 確認画面に遷移した
When: 登録ボタンをクリックする
Then: 
  * データベースに新しいPERSONが追加される:
    * PERSON_ID: [自動採番]
    * PERSON_NAME: "Eve"
    * AGE: 28
    * GENDER: "female"
  * 一覧画面にリダイレクトする
  * 一覧画面に "Eve" が表示される
```

### 3.2 シナリオ2: 編集モードで更新する

```
Given: ユーザーがpersonId=2のPERSON（Bob, 20, male）を編集している
  And: 入力画面で年齢を 20 → 21 に変更した
  And: 確認画面に遷移した
When: 登録ボタンをクリックする
Then: 
  * データベースのpersonId=2のレコードが更新される:
    * PERSON_ID: 2（変更なし）
    * PERSON_NAME: "Bob"（変更なし）
    * AGE: 21（更新）
    * GENDER: "male"（変更なし）
  * 一覧画面にリダイレクトする
  * 一覧画面でBobの年齢が 21 に変更されている
```

### 3.3 シナリオ3: 戻るボタンで入力画面に戻る

```
Given: ユーザーが確認画面を表示している
  And: 名前 "Frank"、年齢 30、性別 "male" を確認している
When: 戻るボタンをクリックする
Then: 
  * 入力画面に戻る
  * 入力フィールドに "Frank"、30、"male" がプリセットされている
  * ユーザーは修正できる
```

### 3.4 シナリオ4: 性別の表示変換

```
Given: ユーザーが確認画面を表示している
  And: genderフィールドが "male" である
When: 画面を表示する
Then: 
  * 性別欄に "男性" と表示される（"male" ではなく）

Given: genderフィールドが "female" である
Then: 
  * 性別欄に "女性" と表示される（"female" ではなく）
```

## 4. エラーケース

### 4.1 エラーケース1: 登録処理エラー（新規追加）

```
Given: ユーザーが確認画面を表示している（新規追加モード）
  And: データベース接続が失敗している
When: 登録ボタンをクリックする
Then: 
  * PersonService.addPerson(person)でRuntimeExceptionが発生する
  * トランザクションが自動的にロールバックされる
  * PersonConfirmBean.save()で例外をキャッチする
  * エラーメッセージが表示される: "登録処理に失敗しました: [エラー詳細]"
  * 確認画面が再表示される（リダイレクトしない）
  * データベースには追加されない
```

### 4.2 エラーケース2: 更新処理エラー

```
Given: ユーザーが確認画面を表示している（編集モード、personId=2）
  And: データベース接続が失敗している
When: 登録ボタンをクリックする
Then: 
  * PersonService.updatePerson(person)でRuntimeExceptionが発生する
  * トランザクションが自動的にロールバックされる
  * PersonConfirmBean.save()で例外をキャッチする
  * エラーメッセージが表示される: "登録処理に失敗しました: [エラー詳細]"
  * 確認画面が再表示される（リダイレクトしない）
  * データベースは更新されない
```

### 4.3 エラーケース3: 制約違反エラー

```
Given: ユーザーが確認画面を表示している
  And: データベースに制約違反が発生する（例: 外部キー制約、CHECK制約）
When: 登録ボタンをクリックする
Then: 
  * EntityManagerでPersistenceExceptionが発生する
  * トランザクションが自動的にロールバックされる
  * RuntimeExceptionでラップされて上位層に伝播する
  * エラーメッセージが表示される: "登録処理に失敗しました: [制約違反詳細]"
  * 確認画面が再表示される
```

## 5. バリデーション

* 本画面ではバリデーションなし（入力画面で既に実行済み）

## 6. トランザクション境界

### 6.1 登録トランザクション

* PersonService.addPerson(person)
  * トランザクション: READ_COMMITTED（@Transactional）
  * 書き込み: INSERT操作
  * コミットタイミング: メソッド終了時（正常終了の場合）
  * ロールバックタイミング: RuntimeException発生時

### 6.2 更新トランザクション

* PersonService.updatePerson(person)
  * トランザクション: READ_COMMITTED（@Transactional）
  * 書き込み: UPDATE操作
  * コミットタイミング: メソッド終了時（正常終了の場合）
  * ロールバックタイミング: RuntimeException発生時

## 7. 並行アクセス

### 7.1 同時追加

```
Given: ユーザーAとユーザーBが同時にPERSONを追加している
When: 両方が登録ボタンをクリックする
Then: 
  * 両方のトランザクションが独立して実行される
  * 両方のPERSONがデータベースに追加される
  * PERSON_IDは異なる値が自動採番される
```

### 7.2 同時更新（競合）

```
Given: ユーザーAとユーザーBが同じPERSON（personId=2）を編集している
  And: ユーザーAが年齢を 20 → 21 に変更
  And: ユーザーBが名前を "Bob" → "Bob Updated" に変更
When: ユーザーAが先に登録ボタンをクリックする
  And: その後、ユーザーBが登録ボタンをクリックする
Then: 
  * ユーザーAの更新が成功する（年齢: 21）
  * ユーザーBの更新が成功する（名前: "Bob Updated"）
  * 最終的な状態: (2, "Bob Updated", 21, "male")
  * ユーザーAの年齢変更はユーザーBの更新で上書きされない（Last Write Wins）
  * 楽観的ロックは現在未実装（将来的に追加の可能性）
```

## 8. パフォーマンス

### 8.1 登録・更新パフォーマンス

* データベース操作:
  * 新規追加: 1回のINSERT文
  * 更新: 1回のUPDATE文
* トランザクション: 短時間のトランザクション

## 9. ログ出力

### 9.1 ログ出力タイミング

* save()メソッド実行時（新規追加）:
  * ログレベル: INFO
  * メッセージ: "Person added successfully"

* save()メソッド実行時（更新）:
  * ログレベル: INFO
  * メッセージ: "Person updated successfully: personId=[ID]"

* エラー発生時:
  * ログレベル: SEVERE
  * メッセージ: "Failed to save person: [エラー詳細]"
  * スタックトレースを出力

## 10. セキュリティ

### 10.1 XSS対策

* <h:outputText>: 自動的にHTMLエスケープ
* personName、age、genderの表示は安全

### 10.2 CSRF対策

* <h:form>: 自動的にCSRFトークンを生成
* jakarta.faces.ViewState hidden field

## 11. 参考資料

* [システム要件定義](../../system/requirements.md)
* [アーキテクチャ設計書](../../system/architecture_design.md)
* [機能設計書](../../system/functional_design.md)
* [データモデル](../../system/data_model.md)
* [システム振る舞い仕様書](../../system/behaviors.md)
* [SCREEN_003_PersonConfirm画面設計](screen_design.md)
* [SCREEN_003_PersonConfirm機能設計](functional_design.md)
