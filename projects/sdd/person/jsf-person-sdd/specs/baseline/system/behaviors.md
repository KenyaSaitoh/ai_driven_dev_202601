# システム振る舞い仕様書

## 1. 概要

本ドキュメントは、人材管理システム（JSF Person）の全体的な振る舞いを定義する。主要な業務フロー、バリデーション、エラーハンドリングを記述する。

## 2. 主要な業務フロー

### 2.1 PERSON一覧表示フロー

* 目的: 全PERSON情報を一覧表示する

* トリガー: ユーザーがpersonList.xhtmlにアクセス

* 事前条件: データベースにPERSONテーブルが存在する

* フロー:

```
Given: ユーザーがシステムにアクセスする
When: personList.xhtmlを表示する
Then:
  * PersonListBeanのinit()メソッドが自動的に実行される（@PostConstruct）
  * PersonServiceのgetAllPersons()メソッドを呼び出す
  * EntityManagerがJPQLクエリ "SELECT p FROM Person p ORDER BY p.personId" を実行する
  * データベースから全PERSONレコードを取得する
  * 取得したList<Person>をPersonListBeanのpersonListフィールドに設定する
  * 画面のh:dataTableにpersonListが表示される
  * 各PERSONに対して編集ボタンと削除ボタンが表示される
```

* 事後条件: 全PERSON情報が一覧表示される

### 2.2 PERSON新規追加フロー

* 目的: 新しいPERSON情報をデータベースに登録する

* トリガー: ユーザーが一覧画面の「新規追加」ボタンをクリック

* 事前条件: なし

* フロー:

```
Given: ユーザーが一覧画面を表示している
When: 新規追加ボタンをクリックする
Then:
  * personInput.xhtmlに遷移する
  * PersonInputBeanのinit()メソッドが実行される
  * personIdがnullのため、フィールドは初期化される（空または null）

Given: ユーザーが入力画面を表示している
When: 名前、年齢、性別を入力し、「確認画面へ」ボタンをクリックする
Then:
  * PersonInputBeanのconfirm()メソッドが実行される
  * 入力データがPersonInputBeanのフィールドに保持される
  * personConfirm.xhtmlに遷移する

Given: ユーザーが確認画面を表示している
When: 入力内容を確認し、「登録」ボタンをクリックする
Then:
  * PersonConfirmBeanのsave()メソッドが実行される
  * Personオブジェクトを作成し、入力データを設定する
  * PersonServiceのaddPerson(person)メソッドを呼び出す
  * EntityManagerのpersist(person)メソッドでデータベースに登録する
  * トランザクションがコミットされ、INSERTが実行される
  * PERSON_IDはデータベースが自動採番する
  * personList.xhtmlにリダイレクトする
  * 一覧画面に新しいPERSONが表示される
```

* 事後条件: 新しいPERSONがデータベースに登録される

### 2.3 PERSON編集フロー

* 目的: 既存のPERSON情報を更新する

* トリガー: ユーザーが一覧画面の「編集」ボタンをクリック

* 事前条件: 編集対象のPERSONがデータベースに存在する

* フロー:

```
Given: ユーザーが一覧画面を表示している
When: 編集ボタンをクリックする（personIdを指定）
Then:
  * personInput.xhtml?personId=xxxに遷移する
  * PersonInputBeanのinit()メソッドが実行される
  * URLパラメータからpersonIdを取得する
  * PersonServiceのgetPersonById(personId)メソッドを呼び出す
  * EntityManagerのfind(Person.class, personId)で既存データを取得する
  * 取得したPersonデータをPersonInputBeanのフィールドに設定する
  * 入力フォームに既存データがプリセットされる

Given: ユーザーが入力画面（編集モード）を表示している
When: 名前、年齢、性別を編集し、「確認画面へ」ボタンをクリックする
Then:
  * PersonInputBeanのconfirm()メソッドが実行される
  * 編集データがPersonInputBeanのフィールドに保持される
  * personConfirm.xhtmlに遷移する

Given: ユーザーが確認画面を表示している
When: 編集内容を確認し、「登録」ボタンをクリックする
Then:
  * PersonConfirmBeanのsave()メソッドが実行される
  * Personオブジェクトを作成し、編集データを設定する（personIdを含む）
  * PersonServiceのupdatePerson(person)メソッドを呼び出す
  * EntityManagerのmerge(person)メソッドでデータベースを更新する
  * トランザクションがコミットされ、UPDATEが実行される
  * personList.xhtmlにリダイレクトする
  * 一覧画面に更新されたPERSONが表示される
```

* 事後条件: 既存のPERSONがデータベースで更新される

### 2.4 PERSON削除フロー

* 目的: 指定されたPERSON情報をデータベースから削除する
* トリガー: ユーザーが一覧画面の「削除」ボタンをクリック
* 事前条件: 削除対象のPERSONがデータベースに存在する
* フロー:

```
Given: ユーザーが一覧画面を表示している
When: 削除ボタンをクリックする（personIdを指定）
Then:
  * JavaScriptの削除確認ダイアログが表示される
  * ユーザーが「OK」をクリックした場合のみ、削除処理が実行される

Given: ユーザーが削除確認ダイアログで「OK」をクリックした
When: 削除処理が実行される
Then:
  * PersonListBeanのdeletePerson(personId)メソッドが実行される
  * PersonServiceのdeletePerson(personId)メソッドを呼び出す
  * EntityManagerのfind(Person.class, personId)で削除対象を取得する
  * EntityManagerのremove(person)メソッドで削除する
  * トランザクションがコミットされ、DELETEが実行される
  * PersonListBeanのinit()メソッドを再度呼び出し、リストを更新する
  * 一覧画面に削除後のPERSONリストが表示される
```

* 事後条件: 指定されたPERSONがデータベースから削除される

### 2.5 キャンセル・戻るフロー

* 目的: 入力や確認をキャンセルして前の画面に戻る

#### 2.5.1 入力画面からキャンセル

```
Given: ユーザーが入力画面を表示している
When: キャンセルボタンをクリックする
Then:
  * PersonInputBeanのcancel()メソッドが実行される
  * personList.xhtmlにリダイレクトする
  * 入力データは破棄される
  * 一覧画面が表示される
```

#### 2.5.2 確認画面から戻る

```
Given: ユーザーが確認画面を表示している
When: 戻るボタンをクリックする
Then:
  * PersonConfirmBeanのback()メソッドが実行される
  * JavaScriptのhistory.back()が実行される
  * ブラウザ履歴を使用して入力画面に戻る
  * 入力データは保持される（@ViewScopedのため）
  * 入力画面が再表示される
```

## 3. バリデーション

### 3.1 入力検証ルール

#### 3.1.1 personName（名前）

* 必須チェック:
  * ルール: 空文字列またはnullは許可しない
  * 実装: @NotNull、@Size(min = 1)
  * エラーメッセージ: "名前を入力してください"

* 最大長チェック:
  * ルール: 30文字以内
  * 実装: @Size(max = 30)
  * エラーメッセージ: "名前は30文字以内で入力してください"

#### 3.1.2 age（年齢）

* 必須チェック:
  * ルール: nullは許可しない
  * 実装: @NotNull
  * エラーメッセージ: "年齢を入力してください"

* 数値型チェック:
  * ルール: 整数型（Integer）
  * 実装: JSFのコンバーターが自動的に変換
  * エラーメッセージ: "年齢は数値で入力してください"

* 最小値チェック:
  * ルール: 0以上
  * 実装: @Min(0)
  * エラーメッセージ: "年齢は0以上で入力してください"

* 最大値チェック:
  * ルール: 150以下
  * 実装: @Max(150)
  * エラーメッセージ: "年齢は150以下で入力してください"

#### 3.1.3 gender（性別）

* 必須チェック:
  * ルール: nullは許可しない
  * 実装: @NotNull
  * エラーメッセージ: "性別を選択してください"

* 値チェック:
  * ルール: "male" または "female" のみ許可
  * 実装: ラジオボタンによる選択（不正な値は入力できない）
  * UIレベルで制約を保証

### 3.2 バリデーション実行タイミング

* クライアント側バリデーション:
  * HTML5のrequired属性を使用（オプション）
  * JavaScriptによる即時検証（オプション）

* サーバー側バリデーション:
  * 確認画面へボタンをクリックした時点でBean Validationが実行される
  * JSFがBean Validationアノテーションを自動的に検証する
  * エラーがある場合は入力画面にエラーメッセージを表示する

### 3.3 バリデーションエラー時の動作

```
Given: ユーザーが入力画面で不正なデータを入力している
When: 確認画面へボタンをクリックする
Then:
  * JSFがBean Validationアノテーションを検証する
  * バリデーションエラーが検出される
  * PersonInputBeanのconfirm()メソッドは実行されない
  * 入力画面が再表示される
  * <h:messages>コンポーネントにエラーメッセージが表示される
  * 入力データは保持される（再入力不要）
  * ユーザーはエラーを修正して再度送信できる
```

### 3.4 Bean Validationアノテーション

* PersonInputBeanのフィールドに適用:

```java
@Named("personInputBean")
@ViewScoped
public class PersonInputBean implements Serializable {
    private Integer personId;

    @NotNull(message = "名前を入力してください")
    @Size(min = 1, max = 30, message = "名前は1〜30文字で入力してください")
    private String personName;

    @NotNull(message = "年齢を入力してください")
    @Min(value = 0, message = "年齢は0以上で入力してください")
    @Max(value = 150, message = "年齢は150以下で入力してください")
    private Integer age;

    @NotNull(message = "性別を選択してください")
    private String gender;
}
```

## 4. エラーハンドリング

### 4.1 エラーの種類

#### 4.1.1 バリデーションエラー

* 原因: ユーザー入力が検証ルールに違反
* 対応:
  * 入力画面を再表示
  * <h:messages>にエラーメッセージを表示
  * 入力データは保持される

#### 4.1.2 データベースエラー

* 原因: データベース接続エラー、SQLエラー、制約違反
* 対応:
  * PersonServiceで例外をキャッチ
  * RuntimeExceptionでラップして上位層に伝播
  * トランザクションは自動的にロールバック
  * エラーページに遷移（または同じページにエラーメッセージを表示）

#### 4.1.3 システムエラー

* 原因: 予期しない例外（NullPointerException、IllegalStateException等）
* 対応:
  * アプリケーションサーバーのエラーハンドラーがキャッチ
  * エラーログを出力
  * エラーページに遷移

### 4.2 例外処理パターン

#### 4.2.1 Service層での例外処理

```java
@RequestScoped
@Transactional
public class PersonService {
    public void addPerson(Person person) {
        try {
            em.persist(person);
        } catch (PersistenceException e) {
            // ログ出力
            logger.severe("Failed to add person: " + e.getMessage());
            // RuntimeExceptionでラップ
            throw new RuntimeException("PERSON登録に失敗しました", e);
        }
    }
}
```

#### 4.2.2 Managed Beanでの例外処理

```java
public String save() {
    try {
        personService.addPerson(person);
        return "personList?faces-redirect=true";
    } catch (RuntimeException e) {
        // エラーメッセージを表示
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "エラー", "登録処理に失敗しました: " + e.getMessage()));
        return null; // 同じページを再表示
    }
}
```

### 4.3 エラーメッセージ表示

* <h:messages>コンポーネント:
  * personInput.xhtmlとpersonConfirm.xhtmlに配置
  * globalOnly="false": すべてのメッセージを表示
  * showSummary="true": メッセージサマリーを表示
  * showDetail="false": 詳細メッセージは非表示

```xml
<h:messages globalOnly="false" showSummary="true" showDetail="false" 
            styleClass="error-messages"/>
```

### 4.4 トランザクションロールバック

* 自動ロールバック:
  * RuntimeExceptionが発生した場合、@Transactionalが自動的にロールバック
  * データベースの整合性が保たれる

* ロールバックシナリオ:

```
Given: ユーザーがPERSONを追加しようとしている
When: PersonServiceのaddPerson()メソッドでPersistenceExceptionが発生する
Then:
  * トランザクションが自動的にロールバックされる
  * データベースに不完全なデータは保存されない
  * RuntimeExceptionが上位層に伝播する
  * エラーメッセージが表示される
```

## 5. 特殊な振る舞い

### 5.1 自動採番（PERSON_ID）

* 動作:
  * 新規追加時、ユーザーはPERSON_IDを入力しない
  * PersonオブジェクトのpersonIdフィールドはnull
  * EntityManagerのpersist()を実行すると、HSQLDBが自動的にIDを生成
  * トランザクションコミット後、Personオブジェクトに生成されたIDが設定される

* 実装:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "PERSON_ID")
private Integer personId;
```

### 5.2 性別の表示変換

* データベース値: "male"、"female"
* 画面表示値: "男性"、"女性"

* 実装方法:
  * 一覧画面と確認画面で条件分岐を使用
  * <h:outputText>のレンダリング条件で表示を切り替え

```xml
<h:outputText value="男性" rendered="#{person.gender == 'male'}"/>
<h:outputText value="女性" rendered="#{person.gender == 'female'}"/>
```

* または、カスタムコンバーターを実装:

```java
@FacesConverter("genderConverter")
public class GenderConverter implements Converter<String> {
    @Override
    public String getAsObject(FacesContext context, UIComponent component, String value) {
        if ("男性".equals(value)) return "male";
        if ("女性".equals(value)) return "female";
        return value;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, String value) {
        if ("male".equals(value)) return "男性";
        if ("female".equals(value)) return "女性";
        return value;
    }
}
```

### 5.3 削除確認ダイアログ

* 動作:
  * 削除ボタンクリック時にJavaScriptのconfirm()ダイアログを表示
  * ユーザーが「OK」をクリックした場合のみ削除処理を実行
  * ユーザーが「キャンセル」をクリックした場合は削除しない

* 実装:

```xml
<h:commandButton value="削除" 
                 action="#{personListBean.deletePerson(person.personId)}"
                 onclick="return confirm('削除してもよろしいですか？');">
    <f:ajax execute="@form" render="@all"/>
</h:commandButton>
```

### 5.4 画面間データ引き継ぎ

* 入力画面 → 確認画面:
  * @ViewScopedのBeanがデータを保持
  * PersonInputBeanのフィールドがPersonConfirmBeanに引き継がれる
  * Flash Scopeを使用する場合もある

* 確認画面 → 一覧画面:
  * リダイレクト遷移のためデータ引き継ぎなし
  * 一覧画面でデータベースから再取得

## 6. 並行アクセス

### 6.1 楽観的ロック

* 現在のバージョンでは実装しない
* 将来的に@Versionアノテーションを使用した楽観的ロックを追加する可能性がある

### 6.2 悲観的ロック

* 現在のバージョンでは実装しない
* 必要に応じてEntityManagerのロックメソッドを使用

### 6.3 同時更新の扱い

* 現状: 後勝ち（Last Write Wins）
* 同じPERSONを複数ユーザーが編集した場合、最後にコミットされた変更が有効になる
* 将来的に楽観的ロックで対応する可能性がある

## 7. セッション管理

### 7.1 スコープの使用

* @ViewScoped:
  * PersonListBean、PersonInputBean、PersonConfirmBeanに使用
  * 画面単位のライフサイクル
  * 画面がアクティブな間、Beanのデータが保持される

* @RequestScoped:
  * PersonServiceに使用
  * リクエスト単位のライフサイクル
  * ステートレス、トランザクション境界

### 7.2 セッションタイムアウト

* デフォルトのセッションタイムアウト: 30分
* web.xmlで設定可能:

```xml
<session-config>
    <session-timeout>30</session-timeout>
</session-config>
```

* タイムアウト時の動作:
  * @ViewScopedのBeanが破棄される
  * ユーザーが操作を続けるとViewExpiredExceptionが発生
  * エラーページに遷移またはログイン画面にリダイレクト

## 8. ログ出力

### 8.1 ログ出力箇所

* PersonServiceの主要メソッド:
  * addPerson()、updatePerson()、deletePerson()
  * ログレベル: INFO

* 例外発生時:
  * すべての例外をログに記録
  * ログレベル: SEVERE
  * スタックトレースを出力

### 8.2 ログフォーマット

```
[2026-01-12 10:30:45] INFO: Person added successfully: personId=4
[2026-01-12 10:35:20] INFO: Person updated successfully: personId=2
[2026-01-12 10:40:10] INFO: Person deleted successfully: personId=3
[2026-01-12 10:45:00] SEVERE: Failed to add person: SQL exception
```

## 9. パフォーマンス考慮事項

### 9.1 データ取得の最適化

* 一覧表示:
  * 全PERSONをORDER BY PERSON_IDで取得
  * 現状はページネーションなし（将来的に追加の可能性）

* 編集モード:
  * findById()で1件のみ取得
  * 効率的なクエリ

### 9.2 トランザクション最適化

* トランザクション境界: Serviceクラスのメソッド
* 短いトランザクション: CRUD操作のみ
* 長時間のトランザクションは避ける

## 10. 参考資料

* [システム要件定義](requirements.md) - システム要件
* [アーキテクチャ設計書](architecture_design.md) - システム全体のアーキテクチャ
* [機能設計書](functional_design.md) - 画面遷移とコンポーネント設計
* [データモデル](data_model.md) - データベーススキーマの詳細
* [SCREEN_001_PersonList仕様](../screen/SCREEN_001_PersonList/behaviors.md)
* [SCREEN_002_PersonInput仕様](../screen/SCREEN_002_PersonInput/behaviors.md)
* [SCREEN_003_PersonConfirm仕様](../screen/SCREEN_003_PersonConfirm/behaviors.md)
