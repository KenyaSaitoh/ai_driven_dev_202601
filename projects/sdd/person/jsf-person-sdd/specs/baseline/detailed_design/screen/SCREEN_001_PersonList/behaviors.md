# 振る舞い仕様書 - SCREEN_001_PersonList

## 1. 概要

PERSON一覧画面（SCREEN_001_PersonList）の振る舞いを定義する。

## 2. 画面の振る舞い

### 2.1 初期表示時の動作

* トリガー: ユーザーがpersonList.xhtmlにアクセス

* 動作:

```
Given: ユーザーがシステムにアクセスする
When: personList.xhtmlを表示する
Then:
  * PersonListBeanのインスタンスが作成される（@ViewScoped）
  * init()メソッドが自動的に実行される（@PostConstruct）
  * PersonService.getAllPersons()が呼び出される
  * EntityManagerがJPQLクエリを実行する: "SELECT p FROM Person p ORDER BY p.personId"
  * データベースから全PERSONレコードを取得する
  * 取得したList<Person>をpersonListフィールドに設定する
  * 画面のh:dataTableにpersonListが表示される
  * 各行にID、名前、年齢、性別、操作（編集・削除ボタン）が表示される
```

* 事後条件: 全PERSON情報が一覧表示される

### 2.2 新規追加ボタンクリック時の動作

* トリガー: ユーザーが「新規追加」ボタンをクリック

* 動作:

```
Given: ユーザーが一覧画面を表示している
When: 新規追加ボタンをクリックする
Then:
  * personInput.xhtmlに遷移する
  * URLパラメータにpersonIdは含まれない
  * PersonInputBeanのinit()メソッドが実行される
  * personIdがnullのため、新規追加モードとして初期化される
  * 入力フォームが空の状態で表示される
```

* 事後条件: PERSON入力画面（新規追加モード）が表示される

### 2.3 編集ボタンクリック時の動作

* トリガー: ユーザーが特定のPERSONの「編集」ボタンをクリック

* 動作:

```
Given: ユーザーが一覧画面を表示している
When: 編集ボタンをクリックする（例: personId=2のPERSON）
Then:
  * personInput.xhtml?personId=2に遷移する
  * URLパラメータでpersonIdが指定される
  * PersonInputBeanのinit()メソッドが実行される
  * PersonService.getPersonById(2)が呼び出される
  * EntityManager.find(Person.class, 2)で既存データを取得する
  * 取得したPersonデータがPersonInputBeanのフィールドに設定される
  * 入力フォームに既存データがプリセットされる
```

* 事後条件: PERSON入力画面（編集モード）が表示され、既存データがプリセットされる

### 2.4 削除ボタンクリック時の動作

* トリガー: ユーザーが特定のPERSONの「削除」ボタンをクリック

* 動作:

```
Given: ユーザーが一覧画面を表示している
When: 削除ボタンをクリックする（例: personId=3のPERSON）
Then:
  * JavaScriptの削除確認ダイアログが表示される: "削除してもよろしいですか？"
  * ユーザーが「OK」をクリックした場合:
    * PersonListBean.deletePerson(3)が呼び出される
    * PersonService.deletePerson(3)が実行される
    * EntityManager.find(Person.class, 3)で削除対象を取得する
    * EntityManager.remove(person)で削除する
    * トランザクションがコミットされ、DELETEが実行される
    * PersonListBean.init()が再度呼び出される
    * 全PERSONリストが再取得される
    * 画面がリロードされ、削除されたPERSONが一覧から消える
    * 成功メッセージが表示される: "PERSONを削除しました"
  * ユーザーが「キャンセル」をクリックした場合:
    * 削除処理は実行されない
    * 画面は変更されない
```

* 事後条件: 指定されたPERSONがデータベースから削除され、一覧から消える

## 3. Given-When-Thenシナリオ

### 3.1 シナリオ1: 初回アクセス時にPERSONリストを表示する

```
Given: データベースに3件のPERSONレコードが存在する
  * (1, 'Alice', 35, 'female')
  * (2, 'Bob', 20, 'male')
  * (3, 'Carol', 30, 'female')

When: ユーザーがpersonList.xhtmlにアクセスする

Then: 
  * 画面に3件のPERSONが表示される
  * 各行にID、名前、年齢、性別、操作ボタンが表示される
  * 性別は "男性" または "女性" に変換されて表示される
```

### 3.2 シナリオ2: 新規追加ボタンから入力画面に遷移する

```
Given: ユーザーが一覧画面を表示している

When: 新規追加ボタンをクリックする

Then: 
  * personInput.xhtmlに遷移する
  * 入力フォームが空の状態で表示される
  * personIdはnull（新規追加モード）
```

### 3.3 シナリオ3: 編集ボタンから入力画面に遷移し、既存データを表示する

```
Given: ユーザーが一覧画面を表示している
  And: personId=2のPERSON（Bob, 20, male）が存在する

When: personId=2のPERSONの編集ボタンをクリックする

Then: 
  * personInput.xhtml?personId=2に遷移する
  * 入力フォームにBobのデータがプリセットされる
    * 名前: "Bob"
    * 年齢: 20
    * 性別: "male"（男性が選択された状態）
  * personIdは2（編集モード）
```

### 3.4 シナリオ4: 削除ボタンでPERSONを削除する

```
Given: ユーザーが一覧画面を表示している
  And: personId=3のPERSON（Carol）が存在する

When: personId=3のPERSONの削除ボタンをクリックする
  And: 削除確認ダイアログで「OK」をクリックする

Then: 
  * personId=3のPERSONがデータベースから削除される
  * 一覧画面がリロードされる
  * Carolが一覧から消える
  * 残りのPERSON（Alice、Bob）のみが表示される
  * 成功メッセージ "PERSONを削除しました" が表示される
```

### 3.5 シナリオ5: 削除確認ダイアログでキャンセルする

```
Given: ユーザーが一覧画面を表示している
  And: personId=3のPERSON（Carol）が存在する

When: personId=3のPERSONの削除ボタンをクリックする
  And: 削除確認ダイアログで「キャンセル」をクリックする

Then: 
  * 削除処理は実行されない
  * 画面は変更されない
  * Carolは一覧に残る
```

## 4. エラーケース

### 4.1 エラーケース1: データ取得エラー

```
Given: データベース接続が失敗している

When: personList.xhtmlにアクセスする

Then: 
  * PersonService.getAllPersons()でRuntimeExceptionが発生する
  * PersonListBean.init()で例外をキャッチする
  * エラーメッセージが表示される: "データ取得に失敗しました: [エラー詳細]"
  * 空のPERSONリスト（0件）が表示される
```

### 4.2 エラーケース2: 削除処理エラー

```
Given: ユーザーが一覧画面を表示している
  And: personId=3のPERSONが存在する
  And: データベース接続が削除処理中に失敗する

When: personId=3のPERSONの削除ボタンをクリックする
  And: 削除確認ダイアログで「OK」をクリックする

Then: 
  * PersonService.deletePerson(3)でRuntimeExceptionが発生する
  * トランザクションが自動的にロールバックされる
  * PersonListBean.deletePerson()で例外をキャッチする
  * エラーメッセージが表示される: "削除処理に失敗しました: [エラー詳細]"
  * PERSONリストは変更されない（Carolは一覧に残る）
```

### 4.3 エラーケース3: 削除対象が存在しない

```
Given: ユーザーが一覧画面を表示している
  And: personId=999のPERSONは存在しない

When: personId=999のPERSONの削除ボタンをクリックする（何らかの不正操作）
  And: 削除確認ダイアログで「OK」をクリックする

Then: 
  * PersonService.deletePerson(999)が呼び出される
  * EntityManager.find(Person.class, 999)でnullが返される
  * RuntimeExceptionがスローされる: "削除対象のPERSONが見つかりませんでした: personId=999"
  * エラーメッセージが表示される: "削除処理に失敗しました: 削除対象のPERSONが見つかりませんでした: personId=999"
```

## 5. バリデーション

* 本画面には入力項目がないため、バリデーションなし

## 6. トランザクション境界

### 6.1 データ取得トランザクション

* PersonService.getAllPersons()
  * トランザクション: READ_COMMITTED（@Transactional）
  * 読み取り専用: 暗黙的に読み取り専用
  * コミットタイミング: メソッド終了時

### 6.2 削除トランザクション

* PersonService.deletePerson(personId)
  * トランザクション: READ_COMMITTED（@Transactional）
  * 書き込み: DELETE操作
  * コミットタイミング: メソッド終了時（正常終了の場合）
  * ロールバックタイミング: RuntimeException発生時

## 7. 並行アクセス

### 7.1 同時削除

```
Given: ユーザーAとユーザーBが同じ一覧画面を表示している
  And: personId=3のPERSON（Carol）が存在する

When: ユーザーAがpersonId=3のPERSONを削除する
  And: ユーザーBもpersonId=3のPERSONを削除しようとする

Then: 
  * ユーザーAの削除が成功する
  * ユーザーBの削除時、EntityManager.find(Person.class, 3)でnullが返される
  * ユーザーBにエラーメッセージが表示される: "削除対象のPERSONが見つかりませんでした: personId=3"
```

### 7.2 削除中の編集

```
Given: ユーザーAとユーザーBが同じ一覧画面を表示している
  And: personId=3のPERSON（Carol）が存在する

When: ユーザーAがpersonId=3のPERSONを削除する
  And: ユーザーBがpersonId=3のPERSONを編集しようとする

Then: 
  * ユーザーAの削除が成功する
  * ユーザーBが編集画面を開く
  * PersonService.getPersonById(3)でnullが返される
  * エラーメッセージが表示される（または編集画面でエラー）
```

## 8. パフォーマンス

### 8.1 データ取得パフォーマンス

* データベースクエリ: 1回のSELECT文
* N+1問題: なし（全データを一度に取得）
* ページネーションなし: 全件表示（将来的に追加の可能性）

### 8.2 削除処理パフォーマンス

* データベース操作:
  1. SELECT: 1回（削除対象の取得）
  2. DELETE: 1回（削除実行）
* トランザクション: 短時間のトランザクション

## 9. ログ出力

### 9.1 ログ出力タイミング

* init()メソッド実行時:
  * ログレベル: INFO
  * メッセージ: "PersonList initialized: [件数] persons loaded"

* deletePerson()メソッド実行時:
  * ログレベル: INFO
  * メッセージ: "Person deleted successfully: personId=[ID]"

* エラー発生時:
  * ログレベル: SEVERE
  * メッセージ: "Failed to delete person: personId=[ID]"
  * スタックトレースを出力

## 10. セキュリティ

### 10.1 XSS対策

* <h:outputText>: 自動的にHTMLエスケープ
* personName、age、genderの表示は安全

### 10.2 CSRF対策

* <h:form>: 自動的にCSRFトークンを生成
* jakarta.faces.ViewState hidden field

### 10.3 削除確認

* JavaScript確認ダイアログ: "削除してもよろしいですか？"
* 誤削除を防止

## 11. 参考資料

* [システム要件定義](../../basic_design/requirements.md)
* [アーキテクチャ設計書](../../basic_design/architecture_design.md)
* [機能設計書](../../basic_design/functional_design.md)
* [データモデル](../../basic_design/data_model.md)
* [システム振る舞い仕様書](../../basic_design/behaviors.md)
* [SCREEN_001_PersonList画面設計](../../basic_design/screen_design.md)
* [SCREEN_001_PersonList機能設計](../../basic_design/functional_design.md)
