# 画面設計書

画面グループ名: person_management
バージョン: 1.0.0
最終更新日: 2026-02-08
ステータス: 基本設計

---

## 1. 概要

本ドキュメントは、person_management画面グループの全画面の設計を定義する。画面レイアウト、入力項目、バリデーションルール、ボタンとアクションを記述する。

### 1.1 対象システム

* システム名: Person Management System
* 技術スタック: Jakarta EE 10、Jakarta Faces (JSF) 4.0

### 1.2 画面一覧

| 画面ID | 画面名 | URL | 目的 |
|--------|--------|-----|------|
| SCREEN_001 | PERSON一覧 | /personList.xhtml | 全PERSON情報を一覧表示 |
| SCREEN_002 | PERSON入力 | /personInput.xhtml | PERSON情報の入力・編集 |
| SCREEN_003 | PERSON確認 | /personConfirm.xhtml | 入力内容の確認と登録 |

---

# SCREEN_001: PERSON一覧

## 1. 画面概要

* 画面ID: SCREEN_001_PersonList
* 画面名: PERSON一覧
* URL: /personList.xhtml
* 目的: 登録されている全PERSON情報を一覧表示し、新規追加、編集、削除の操作を提供する

## 2. 画面レイアウト

### 2.1 画面構成

```
+------------------------------------------------------------------+
|                      PERSON一覧                                   |
+------------------------------------------------------------------+
| [新規追加]                                                         |
+------------------------------------------------------------------+
| ID  | 名前    | 年齢 | 性別 | 操作                                 |
+------------------------------------------------------------------+
| 1   | Alice   | 35   | 女性 | [編集] [削除]                        |
| 2   | Bob     | 20   | 男性 | [編集] [削除]                        |
| 3   | Carol   | 30   | 女性 | [編集] [削除]                        |
+------------------------------------------------------------------+
```

### 2.2 画面要素

* ページヘッダー
  * タイトル: "PERSON一覧"（H1見出し）

* 新規追加リンク
  * 種類: リンク
  * ラベル: "新規追加"
  * 機能: PERSON入力画面に遷移する（新規追加モード）
  * スタイル: button-link, add

* PERSON一覧テーブル
  * 種類: データテーブル
  * カラム: ID、名前、年齢、性別、操作
  * ソート順: ID昇順
  * 機能: 登録されている全PERSONを表示する

## 3. 表示データ

### 3.1 表示項目

* personList: List<Person>
  * データソース: PersonService.getAllPersons()
  * ソート順: PERSON_ID 昇順
  * 表示件数: 全件

### 3.2 各項目の表示データ

* ID（PERSON_ID）
  * 表示形式: 整数
  * 例: "1"、"2"、"3"

* 名前（PERSON_NAME）
  * 表示形式: 文字列
  * 最大長: 30文字
  * 例: "Alice"、"Bob"、"Carol"

* 年齢（AGE）
  * 表示形式: 整数
  * 例: "35"、"20"、"30"

* 性別（GENDER）
  * データベース値: "male"、"female"
  * 表示値: "男性"、"女性"
  * 変換ロジック:
    * "male" → "男性"
    * "female" → "女性"

## 4. 入力項目とバリデーション

この画面には入力項目はありません。

## 5. ボタンとアクション

### 5.1 新規追加リンク

* ボタンラベル: "新規追加"
* クリック時の動作:
  * PERSON入力画面に遷移する
  * フォームは空の状態で表示される（新規追加モード）

* 実装:

```xml
<h:link outcome="personInput" value="新規追加" styleClass="button-link add" />
```

### 5.2 編集リンク

* ボタンラベル: "編集"
* クリック時の動作:
  * PERSON入力画面に遷移する
  * URLパラメータでpersonIdを渡す
  * 既存データを表示する（編集モード）

* 実装:

```xml
<h:link outcome="personInput" value="編集" styleClass="button-link">
    <f:param name="personId" value="#{person.personId}" />
</h:link>
```

### 5.3 削除リンク

* ボタンラベル: "削除"
* クリック時の動作:
  * JavaScriptで削除確認ダイアログを表示する
  * OKをクリックした場合、指定IDのPERSONを削除する
  * PERSON一覧画面にリダイレクトする

* 実装:

```xml
<h:link outcome="personDelete" value="削除" styleClass="button-link delete" onclick="return confirm('削除してもよろしいですか？');">
    <f:param name="personId" value="#{person.personId}" />
</h:link>
```

または

```xml
<h:commandLink action="#{personListBean.deletePerson(person.personId)}" value="削除" styleClass="button-link delete" onclick="return confirm('削除してもよろしいですか？');" />
```

## 6. エラーメッセージとバリデーション

### 6.1 バリデーションエラー

この画面にはバリデーションエラーはありません。

### 6.2 ビジネスルールエラー

* データ取得エラー: "データの取得に失敗しました"
  * 条件: PersonService.getAllPersons()でエラーが発生した場合
  * 表示位置: ページヘッダー（<h:messages>）

* 削除エラー: "削除に失敗しました"
  * 条件: PersonService.deletePerson()でエラーが発生した場合
  * 表示位置: ページヘッダー（<h:messages>）

## 7. 画面の振る舞い

### 7.1 初期表示

* PersonService.getAllPersons()を呼び出して全PERSONを取得する
* 取得したPERSONリストをPERSON_ID昇順でソートする
* 一覧テーブルに表示する

### 7.2 ボタンクリック時

* 新規追加: PERSON入力画面に遷移する
* 編集: PERSON入力画面に遷移する（personId付き）
* 削除: 削除確認ダイアログを表示し、OKをクリックした場合に削除実行

### 7.3 画面遷移

* PERSON一覧 → PERSON入力: 新規追加リンクまたは編集リンクをクリック
* PERSON一覧 → PERSON一覧: 削除リンクをクリック（削除後にリダイレクト）

---

# SCREEN_002: PERSON入力

## 1. 画面概要

* 画面ID: SCREEN_002_PersonInput
* 画面名: PERSON入力
* URL: /personInput.xhtml
* 目的: PERSON情報を入力または編集するためのフォームを提供する

## 2. 画面レイアウト

### 2.1 画面構成

```
+------------------------------------------------------------------+
|                      PERSON入力                                   |
+------------------------------------------------------------------+
| 名前: [________________________________]                         |
+------------------------------------------------------------------+
| 年齢: [__________]                                               |
+------------------------------------------------------------------+
| 性別: ( ) 男性  ( ) 女性                                          |
+------------------------------------------------------------------+
| [確認画面へ] [キャンセル]                                          |
+------------------------------------------------------------------+
```

### 2.2 画面要素

* ページヘッダー
  * タイトル: "PERSON入力"（H1見出し）

* 名前入力フィールド
  * 種類: テキストフィールド
  * ラベル: "名前:"
  * 機能: 人材名を入力する
  * サイズ: 30文字

* 年齢入力フィールド
  * 種類: テキストフィールド
  * ラベル: "年齢:"
  * 機能: 年齢を入力する
  * サイズ: 10文字

* 性別選択ラジオボタン
  * 種類: ラジオボタン
  * ラベル: "性別:"
  * 選択肢: "男性"（male）、"女性"（female）
  * 機能: 性別を選択する

## 3. 表示データ

### 3.1 新規追加モード（personId パラメータなし）

* フォームは空の状態で表示される
* PERSON_IDはnull

### 3.2 編集モード（personId パラメータあり）

* URLパラメータからPERSON_IDを取得する
* PersonService.getPersonById(personId)で既存データを取得する
* 取得したPERSON情報をフォームに設定して表示する

## 4. 入力項目とバリデーション

### 4.1 名前（personName）

* フィールド名: personName
* ラベル: "名前:"
* 入力タイプ: テキスト
* バリデーション:
  * 必須: Yes
  * 最小長: 1文字
  * 最大長: 30文字
  * 形式: なし
  * エラーメッセージ: "名前は必須です"、"名前は30文字以内で入力してください"

* 実装:

```xml
<h:outputLabel for="personName" value="名前:" />
<h:inputText id="personName" value="#{personInputBean.personName}" size="30">
    <f:validateLength minimum="1" maximum="30" />
</h:inputText>
<h:message for="personName" styleClass="error" />
```

または Bean Validation:

```java
@NotBlank(message = "名前は必須です")
@Size(max = 30, message = "名前は30文字以内で入力してください")
private String personName;
```

### 4.2 年齢（age）

* フィールド名: age
* ラベル: "年齢:"
* 入力タイプ: 数値
* バリデーション:
  * 必須: Yes
  * 最小値: 0
  * 最大値: なし（整数値）
  * 形式: 整数
  * エラーメッセージ: "年齢は必須です"、"年齢は数値で入力してください"

* 実装:

```xml
<h:outputLabel for="age" value="年齢:" />
<h:inputText id="age" value="#{personInputBean.age}" size="10" />
<h:message for="age" styleClass="error" />
```

または Bean Validation:

```java
@NotNull(message = "年齢は必須です")
@Min(value = 0, message = "年齢は0以上で入力してください")
private Integer age;
```

### 4.3 性別（gender）

* フィールド名: gender
* ラベル: "性別:"
* 入力タイプ: ラジオボタン
* バリデーション:
  * 必須: Yes
  * 選択肢: "male"（男性）、"female"（女性）
  * エラーメッセージ: "性別は必須です"

* 実装:

```xml
<h:outputLabel for="gender" value="性別:" />
<h:selectOneRadio id="gender" value="#{personInputBean.gender}">
    <f:selectItem itemValue="male" itemLabel="男性" />
    <f:selectItem itemValue="female" itemLabel="女性" />
</h:selectOneRadio>
<h:message for="gender" styleClass="error" />
```

または

```xml
<h:outputLabel for="gender" value="性別:" />
<h:inputText type="radio" id="genderMale" value="#{personInputBean.gender}" />男性
<h:inputText type="radio" id="genderFemale" value="#{personInputBean.gender}" styleClass="gender-radio" />女性
```

または Bean Validation:

```java
@NotBlank(message = "性別は必須です")
private String gender;
```

## 5. ボタンとアクション

### 5.1 確認画面へボタン

* ボタンラベル: "確認画面へ"
* クリック時の動作:
  * 入力内容をバリデーションする
  * バリデーション成功時、PERSON確認画面に遷移する
  * バリデーション失敗時、エラーメッセージを表示して画面にとどまる

* 実装:

```xml
<h:commandButton value="確認画面へ" action="#{personInputBean.confirm()}" styleClass="button" />
```

### 5.2 キャンセルボタン

* ボタンラベル: "キャンセル"
* クリック時の動作:
  * 入力内容を破棄する
  * PERSON一覧画面に遷移する

* 実装:

```xml
<h:button value="キャンセル" outcome="personList" styleClass="button cancel" />
```

または

```xml
<h:button value="キャンセル" onclick="location.href='personList.xhtml'" styleClass="button cancel" />
```

## 6. エラーメッセージとバリデーション

### 6.1 バリデーションエラー

* 名前: "名前は必須です"、"名前は30文字以内で入力してください"
  * 条件: 空白またはnull、または30文字を超える
  * 表示位置: フィールドの下

* 年齢: "年齢は必須です"、"年齢は数値で入力してください"
  * 条件: 空白またはnull、または数値以外
  * 表示位置: フィールドの下

* 性別: "性別は必須です"
  * 条件: 選択されていない
  * 表示位置: フィールドの下

### 6.2 ビジネスルールエラー

* データ取得エラー（編集モード）: "データの取得に失敗しました"
  * 条件: PersonService.getPersonById()でエラーが発生した場合
  * 表示位置: ページヘッダー（<h:messages>）

## 7. 画面の振る舞い

### 7.1 初期表示

* 新規追加モード: フォームを空の状態で表示する
* 編集モード: URLパラメータからpersonIdを取得し、既存データをフォームに設定する

### 7.2 ボタンクリック時

* 確認画面へ: バリデーション成功時にPERSON確認画面に遷移する
* キャンセル: PERSON一覧画面に遷移する

### 7.3 画面遷移

* PERSON入力 → PERSON確認: 確認画面へボタンをクリック
* PERSON入力 → PERSON一覧: キャンセルボタンをクリック

---

# SCREEN_003: PERSON確認

## 1. 画面概要

* 画面ID: SCREEN_003_PersonConfirm
* 画面名: PERSON確認
* URL: /personConfirm.xhtml
* 目的: 入力したPERSON情報を確認し、登録または戻る操作を提供する

## 2. 画面レイアウト

### 2.1 画面構成

```
+------------------------------------------------------------------+
|                      PERSON確認                                   |
+------------------------------------------------------------------+
| 名前: Alice                                                       |
+------------------------------------------------------------------+
| 年齢: 35                                                          |
+------------------------------------------------------------------+
| 性別: 女性                                                         |
+------------------------------------------------------------------+
| [登録] [戻る]                                                      |
+------------------------------------------------------------------+
```

### 2.2 画面要素

* ページヘッダー
  * タイトル: "PERSON確認"（H1見出し）

* 名前表示
  * ラベル: "名前:"
  * 値: 入力した名前

* 年齢表示
  * ラベル: "年齢:"
  * 値: 入力した年齢

* 性別表示
  * ラベル: "性別:"
  * 値: 入力した性別（日本語表記: "男性" または "女性"）

## 3. 表示データ

### 3.1 表示項目

* personForm: PersonForm型（Session ScopeまたはFlash Scopeから取得）
  * データソース: PERSON入力画面から引き継がれたフォームデータ

### 3.2 各項目の表示データ

* 名前（personName）
  * 表示形式: 文字列
  * 例: "Alice"

* 年齢（age）
  * 表示形式: 整数
  * 例: "35"

* 性別（gender）
  * データベース値: "male"、"female"
  * 表示値: "男性"、"女性"
  * 変換ロジック:
    * "male" → "男性"
    * "female" → "女性"

## 4. 入力項目とバリデーション

この画面には入力項目はありません。ただし、登録時にフォームデータを送信するためにhiddenフィールドを使用します。

* 実装:

```xml
<h:form>
    <h:inputHidden value="#{personConfirmBean.personId}" />
    <h:inputHidden value="#{personConfirmBean.personName}" />
    <h:inputHidden value="#{personConfirmBean.age}" />
    <h:inputHidden value="#{personConfirmBean.gender}" />
    <!-- ボタン -->
</h:form>
```

## 5. ボタンとアクション

### 5.1 登録ボタン

* ボタンラベル: "登録"
* クリック時の動作:
  * フォームデータをPersonエンティティに変換する
  * PERSON_IDがnullの場合は新規追加、PERSON_IDがある場合は更新を実行する
  * PersonServiceを使用してデータベースに保存する
  * PERSON一覧画面にリダイレクトする

* 実装:

```xml
<h:commandButton value="登録" action="#{personConfirmBean.register()}" styleClass="button" />
```

### 5.2 戻るボタン

* ボタンラベル: "戻る"
* クリック時の動作:
  * JavaScript history.back()を使用してPERSON入力画面に戻る
  * 入力内容は保持される

* 実装:

```xml
<h:button value="戻る" onclick="history.back();" styleClass="button back" />
```

## 6. エラーメッセージとバリデーション

### 6.1 バリデーションエラー

この画面にはバリデーションエラーはありません。

### 6.2 ビジネスルールエラー

* 登録エラー: "登録に失敗しました"
  * 条件: PersonService.addPerson()またはPersonService.updatePerson()でエラーが発生した場合
  * 表示位置: ページヘッダー（<h:messages>）

* データベースエラー: "データベースエラーが発生しました"
  * 条件: データベースアクセス時にエラーが発生した場合
  * 表示位置: ページヘッダー（<h:messages>）

## 7. 画面の振る舞い

### 7.1 初期表示

* Session ScopeまたはFlash ScopeからPersonFormデータを取得する
* 取得したデータを確認画面に表示する
* 性別の値（"male" / "female"）を日本語（"男性" / "女性"）に変換して表示する

### 7.2 ボタンクリック時

* 登録: PERSONを登録または更新し、PERSON一覧画面にリダイレクトする
* 戻る: JavaScript history.back()でPERSON入力画面に戻る

### 7.3 画面遷移

* PERSON確認 → PERSON一覧: 登録ボタンをクリック（リダイレクト）
* PERSON確認 → PERSON入力: 戻るボタンをクリック（history.back()）

---

## 参考資料

* [functional_design.md](functional_design.md) - 機能設計書（画面一覧、画面遷移）
* [behaviors.md](behaviors.md) - 振る舞い仕様書（E2Eテスト用）
* [../common/architecture_design.md](../common/architecture_design.md) - アーキテクチャ設計書
