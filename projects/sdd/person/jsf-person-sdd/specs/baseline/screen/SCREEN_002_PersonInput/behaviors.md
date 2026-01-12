# 振る舞い仕様書 - SCREEN_002_PersonInput

## 1. 概要

PERSON入力画面（SCREEN_002_PersonInput）の振る舞いを定義する。

## 2. 画面の振る舞い

### 2.1 初期表示時の動作（新規追加モード）

* トリガー: ユーザーが一覧画面の「新規追加」ボタンをクリックし、personInput.xhtmlにアクセス

* 動作:

```
Given: ユーザーが一覧画面を表示している
When: 新規追加ボタンをクリックする
Then:
  * personInput.xhtmlに遷移する
  * PersonInputBeanのインスタンスが作成される（@ViewScoped）
  * URLパラメータにpersonIdは含まれない
  * <f:viewParam>でpersonIdフィールドはnullのまま
  * init()メソッドが自動的に実行される（@PostConstruct）
  * personIdがnullのため、フィールドは初期化される（空またはnull）
  * 入力フォームが空の状態で表示される
```

* 事後条件: 入力フォームが空の状態で表示される（新規追加モード）

### 2.2 初期表示時の動作（編集モード）

* トリガー: ユーザーが一覧画面の「編集」ボタンをクリックし、personInput.xhtml?personId=xxxにアクセス

* 動作:

```
Given: ユーザーが一覧画面を表示している
  And: personId=2のPERSON（Bob, 20, male）が存在する
When: personId=2のPERSONの編集ボタンをクリックする
Then:
  * personInput.xhtml?personId=2に遷移する
  * PersonInputBeanのインスタンスが作成される（@ViewScoped）
  * <f:viewParam>でpersonIdフィールドに2が設定される
  * init()メソッドが自動的に実行される（@PostConstruct）
  * personIdが2のため、編集モードとして処理される
  * PersonService.getPersonById(2)が呼び出される
  * EntityManager.find(Person.class, 2)で既存データを取得する
  * 取得したPersonデータがPersonInputBeanのフィールドに設定される:
    * personName = "Bob"
    * age = 20
    * gender = "male"
  * 入力フォームに既存データがプリセットされる
```

* 事後条件: 入力フォームに既存データがプリセットされる（編集モード）

### 2.3 確認画面へボタンクリック時の動作（バリデーション成功）

* トリガー: ユーザーが入力画面で入力し、「確認画面へ」ボタンをクリック

* 動作:

```
Given: ユーザーが入力画面を表示している
  And: 名前、年齢、性別を正しく入力している
When: 確認画面へボタンをクリックする
Then:
  * JSFがBean Validationアノテーションを検証する
  * バリデーションが成功する（すべてのフィールドが有効）
  * PersonInputBean.confirm()メソッドが実行される
  * personConfirm.xhtmlに遷移する
  * 入力データはPersonInputBeanのフィールドに保持される（@ViewScoped）
  * PersonConfirmBeanがこのデータを参照する
```

* 事後条件: PERSON確認画面が表示され、入力データが引き継がれる

### 2.4 確認画面へボタンクリック時の動作（バリデーションエラー）

* トリガー: ユーザーが入力画面で不正なデータを入力し、「確認画面へ」ボタンをクリック

* 動作:

```
Given: ユーザーが入力画面を表示している
  And: 名前を空欄のまま、または年齢に不正な値を入力している
When: 確認画面へボタンをクリックする
Then:
  * JSFがBean Validationアノテーションを検証する
  * バリデーションエラーが検出される
  * PersonInputBean.confirm()メソッドは実行されない
  * 入力画面が再表示される
  * <h:messages>コンポーネントにエラーメッセージが表示される:
    * "名前を入力してください"
    * "年齢は0以上で入力してください"
    * "性別を選択してください"
  * 入力データは保持される（再入力不要）
  * ユーザーはエラーを修正して再度送信できる
```

* 事後条件: 入力画面が再表示され、エラーメッセージが表示される

### 2.5 キャンセルボタンクリック時の動作

* トリガー: ユーザーが入力画面で「キャンセル」ボタンをクリック

* 動作:

```
Given: ユーザーが入力画面を表示している
  And: 何らかのデータを入力している（または入力していない）
When: キャンセルボタンをクリックする
Then:
  * PersonInputBean.cancel()メソッドが実行される
  * personList.xhtmlにリダイレクトする
  * 入力データは破棄される（@ViewScopedのBeanが破棄される）
  * 一覧画面が表示される
  * バリデーションは実行されない（immediate="true"）
```

* 事後条件: 一覧画面が表示され、入力データは破棄される

## 3. Given-When-Thenシナリオ

### 3.1 シナリオ1: 新規追加モードで入力画面を開く

```
Given: ユーザーが一覧画面を表示している
When: 新規追加ボタンをクリックする
Then: 
  * 入力画面が表示される
  * すべての入力フィールドが空の状態
  * personIdはnull
```

### 3.2 シナリオ2: 編集モードで入力画面を開く

```
Given: ユーザーが一覧画面を表示している
  And: personId=2のPERSON（Bob, 20, male）が存在する
When: personId=2のPERSONの編集ボタンをクリックする
Then: 
  * 入力画面が表示される
  * 名前フィールドに "Bob" がプリセットされる
  * 年齢フィールドに 20 がプリセットされる
  * 性別ラジオボタンで "男性" が選択された状態
  * personIdは2
```

### 3.3 シナリオ3: 正しいデータを入力して確認画面に遷移する

```
Given: ユーザーが入力画面を表示している（新規追加モード）
When: 名前に "David"、年齢に 25、性別に "male" を入力する
  And: 確認画面へボタンをクリックする
Then: 
  * バリデーションが成功する
  * 確認画面に遷移する
  * 確認画面に入力データが表示される:
    * 名前: "David"
    * 年齢: 25
    * 性別: "男性"
```

### 3.4 シナリオ4: バリデーションエラーが発生する

```
Given: ユーザーが入力画面を表示している（新規追加モード）
When: 名前を空欄のまま、年齢に -5、性別を未選択のまま確認画面へボタンをクリックする
Then: 
  * バリデーションエラーが発生する
  * 入力画面が再表示される
  * エラーメッセージが表示される:
    * "名前を入力してください"
    * "年齢は0以上で入力してください"
    * "性別を選択してください"
  * 入力データは保持される（年齢: -5）
```

### 3.5 シナリオ5: キャンセルボタンで一覧画面に戻る

```
Given: ユーザーが入力画面を表示している
  And: 名前に "Eve" を入力している
When: キャンセルボタンをクリックする
Then: 
  * 一覧画面にリダイレクトする
  * 入力データ（Eve）は破棄される
  * 一覧画面には変更が反映されない
```

## 4. エラーケース

### 4.1 エラーケース1: 編集対象が存在しない

```
Given: personId=999のPERSONは存在しない
When: personInput.xhtml?personId=999にアクセスする（何らかの不正操作）
Then: 
  * PersonInputBeanのinit()メソッドが実行される
  * PersonService.getPersonById(999)が呼び出される
  * EntityManager.find(Person.class, 999)でnullが返される
  * エラーメッセージが表示される: "指定されたPERSONが見つかりませんでした"
  * 入力フォームは空の状態で表示される
```

### 4.2 エラーケース2: データ取得エラー

```
Given: データベース接続が失敗している
When: personInput.xhtml?personId=2にアクセスする
Then: 
  * PersonService.getPersonById(2)でRuntimeExceptionが発生する
  * PersonInputBean.init()で例外をキャッチする
  * エラーメッセージが表示される: "データ取得に失敗しました: [エラー詳細]"
  * 入力フォームは空の状態で表示される
```

### 4.3 エラーケース3: 年齢に文字列を入力

```
Given: ユーザーが入力画面を表示している
When: 年齢フィールドに "abc" を入力し、確認画面へボタンをクリックする
Then: 
  * JSFのコンバーターがエラーを検出する
  * 入力画面が再表示される
  * エラーメッセージが表示される: "年齢は数値で入力してください"（コンバーターメッセージ）
```

## 5. バリデーション詳細

### 5.1 personNameバリデーション

* 空文字列:

```
Given: 名前フィールドが空の状態
When: 確認画面へボタンをクリックする
Then: エラーメッセージ "名前を入力してください" が表示される
```

* 31文字入力:

```
Given: 名前フィールドに31文字を入力
When: 確認画面へボタンをクリックする
Then: エラーメッセージ "名前は1〜30文字で入力してください" が表示される
```

### 5.2 ageバリデーション

* null:

```
Given: 年齢フィールドが空の状態
When: 確認画面へボタンをクリックする
Then: エラーメッセージ "年齢を入力してください" が表示される
```

* -1入力:

```
Given: 年齢フィールドに -1 を入力
When: 確認画面へボタンをクリックする
Then: エラーメッセージ "年齢は0以上で入力してください" が表示される
```

* 151入力:

```
Given: 年齢フィールドに 151 を入力
When: 確認画面へボタンをクリックする
Then: エラーメッセージ "年齢は150以下で入力してください" が表示される
```

### 5.3 genderバリデーション

* 未選択:

```
Given: 性別ラジオボタンが未選択の状態
When: 確認画面へボタンをクリックする
Then: エラーメッセージ "性別を選択してください" が表示される
```

## 6. トランザクション境界

### 6.1 データ取得トランザクション

* PersonService.getPersonById(personId)
  * トランザクション: READ_COMMITTED（@Transactional）
  * 読み取り専用: 暗黙的に読み取り専用
  * コミットタイミング: メソッド終了時

## 7. 並行アクセス

### 7.1 同時編集

```
Given: ユーザーAとユーザーBが同じPERSON（personId=2）を編集している
When: ユーザーAが編集画面を開く
  And: ユーザーBも編集画面を開く
Then: 
  * 両方のユーザーが同じ既存データを取得する
  * 後にどちらかが更新を完了すると、もう一方の変更は上書きされる（Last Write Wins）
  * 楽観的ロックは現在未実装（将来的に追加の可能性）
```

## 8. パフォーマンス

### 8.1 データ取得パフォーマンス

* データベースクエリ: 1回のSELECT文（主キー検索）
* em.find(Person.class, personId)は効率的

## 9. ログ出力

### 9.1 ログ出力タイミング

* init()メソッド実行時（編集モード）:
  * ログレベル: INFO
  * メッセージ: "PersonInput initialized in edit mode: personId=[ID]"

* エラー発生時:
  * ログレベル: SEVERE
  * メッセージ: "Failed to load person: personId=[ID]"
  * スタックトレースを出力

## 10. セキュリティ

### 10.1 XSS対策

* <h:inputText>: 自動的にHTMLエスケープ
* 入力データは安全に処理される

### 10.2 CSRF対策

* <h:form>: 自動的にCSRFトークンを生成
* jakarta.faces.ViewState hidden field

## 11. 参考資料

* [システム要件定義](../../system/requirements.md)
* [アーキテクチャ設計書](../../system/architecture_design.md)
* [機能設計書](../../system/functional_design.md)
* [データモデル](../../system/data_model.md)
* [システム振る舞い仕様書](../../system/behaviors.md)
* [SCREEN_002_PersonInput画面設計](screen_design.md)
* [SCREEN_002_PersonInput機能設計](functional_design.md)
