# SCREEN_001_PersonList PERSON一覧画面 - 詳細設計書

* 画面ID: SCREEN_001_PersonList
* 画面名: PERSON一覧画面
* バージョン: 1.0.0
* 最終更新: 2026-01-12

---

## 1. パッケージ構造

### 1.1 関連パッケージ

```
pro.kensait.jsf.person/
├── bean
│   └── PersonListBean.java       # JSF Managed Bean
├── entity
│   └── Person.java                # JPA Entity（共通機能で実装済み）
└── service
    └── PersonService.java         # CDI Service（共通機能で実装済み）
```

### 1.2 実装対象クラス

* PersonListBean: この画面で新規に実装
* PersonService: 共通機能タスクで実装済み（getAllPersons()、deletePerson()メソッドを使用）
* Person: 共通機能タスクで実装済み（JPA Entity）

---

## 2. クラス設計

### 2.1 PersonListBean（JSF Managed Bean）

* 責務: PERSON一覧画面の制御、PERSONリストの表示、削除機能の提供

* アノテーション:
  * `@Named("personListBean")` - JSF Managed Bean名
  * `@ViewScoped` - ビュースコープ（画面表示中のみ有効）
  * `implements Serializable` - シリアライズ可能

* パッケージ: pro.kensait.jsf.person.bean

* プロパティ:

| プロパティ名 | 型 | 説明 |
|------------|---|------|
| `personService` | `PersonService` | ビジネスロジック層のサービス（@Inject） |
| `personList` | `List<Person>` | 画面に表示するPERSONリスト |

* アクションメソッド:

#### init() - 画面初期表示処理

* シグネチャ:

```java
@PostConstruct
public void init()
```

* 処理フロー:
  1. personService.getAllPersons()を呼び出して全PERSONを取得
  2. 取得したList<Person>をpersonListフィールドに設定
  3. エラー発生時は例外をキャッチし、エラーメッセージを表示
  4. エラー時はpersonListに空のリストを設定

* 戻り値: なし（void）

* エラーケース:
  * RuntimeException発生時 → エラーメッセージ "データ取得に失敗しました: [エラー詳細]" を表示し、空のリストを設定

* Java実装例:

```java
@PostConstruct
public void init() {
    try {
        personList = personService.getAllPersons();
    } catch (RuntimeException e) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "エラー", "データ取得に失敗しました: " + e.getMessage()));
        personList = new ArrayList<>();
    }
}
```

#### getPersonList() - PERSONリスト取得

* シグネチャ:

```java
public List<Person> getPersonList()
```

* 処理フロー:
  1. personListフィールドを返す

* 戻り値: `List<Person>` - PERSONリスト

* ビューからのアクセス: `#{personListBean.personList}`

#### deletePerson(Integer personId) - PERSON削除

* シグネチャ:

```java
public String deletePerson(Integer personId)
```

* 処理フロー:
  1. personService.deletePerson(personId)を呼び出して削除
  2. 削除成功後、init()メソッドを再度呼び出してリストを更新
  3. 成功メッセージ "PERSONを削除しました" を表示
  4. nullを返して同じページをリロード

* 戻り値: `null` - 同じページをリロード

* エラーケース:
  * RuntimeException発生時 → エラーメッセージ "削除処理に失敗しました: [エラー詳細]" を表示し、nullを返す

* Java実装例:

```java
public String deletePerson(Integer personId) {
    try {
        personService.deletePerson(personId);
        init(); // リストを再取得
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
            "成功", "PERSONを削除しました"));
        return null;
    } catch (RuntimeException e) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "エラー", "削除処理に失敗しました: " + e.getMessage()));
        return null;
    }
}
```

#### 完全なクラス実装

* Java:

```java
package pro.kensait.jsf.person.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import pro.kensait.jsf.person.entity.Person;
import pro.kensait.jsf.person.service.PersonService;

@Named("personListBean")
@ViewScoped
public class PersonListBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private PersonService personService;

    private List<Person> personList;

    @PostConstruct
    public void init() {
        try {
            personList = personService.getAllPersons();
        } catch (RuntimeException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "エラー", "データ取得に失敗しました: " + e.getMessage()));
            personList = new ArrayList<>();
        }
    }

    public List<Person> getPersonList() {
        return personList;
    }

    public String deletePerson(Integer personId) {
        try {
            personService.deletePerson(personId);
            init();
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "成功", "PERSONを削除しました"));
            return null;
        } catch (RuntimeException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "エラー", "削除処理に失敗しました: " + e.getMessage()));
            return null;
        }
    }
}
```

---

### 2.2 PersonService（ビジネスロジック層）

* 責務: PERSONビジネスロジックの実装、トランザクション管理

* アノテーション:
  * `@RequestScoped`
  * `@Transactional(TxType.REQUIRED)`

* パッケージ: pro.kensait.jsf.person.service

* 注意: PersonServiceは共通機能タスクで実装済み。この画面では以下のメソッドを使用する。

* 主要メソッド:

#### getAllPersons()

* シグネチャ:

```java
public List<Person> getAllPersons()
```

* 処理:
  1. JPQLクエリを実行してすべてのPersonエンティティを取得
  2. personIdの昇順でソート

* JPQL:

```sql
SELECT p FROM Person p ORDER BY p.personId
```

* 戻り値: `List<Person>` - 全PERSONリスト

* トランザクション: READ_COMMITTED（@Transactionalにより自動管理）

#### deletePerson(Integer personId)

* シグネチャ:

```java
public void deletePerson(Integer personId)
```

* 処理:
  1. em.find(Person.class, personId)で削除対象を取得
  2. 見つかった場合はem.remove(person)で削除
  3. 見つからなかった場合はRuntimeExceptionをスロー

* Java実装例:

```java
public void deletePerson(Integer personId) {
    Person person = em.find(Person.class, personId);
    if (person != null) {
        em.remove(person);
    } else {
        throw new RuntimeException("削除対象のPERSONが見つかりませんでした: personId=" + personId);
    }
}
```

* トランザクション: READ_COMMITTED（@Transactionalにより自動管理）
* コミット: メソッド正常終了時
* ロールバック: RuntimeException発生時

---

### 2.3 Person（エンティティ）

* テーブル: `PERSON`

* 主要フィールド:

| フィールド名 | 型 | カラム名 | 制約 | 説明 |
|------------|---|---------|-----|------|
| `personId` | `Integer` | `PERSON_ID` | `PRIMARY KEY, AUTO INCREMENT` | 人材ID |
| `personName` | `String` | `PERSON_NAME` | `NOT NULL, VARCHAR(30)` | 人材名 |
| `age` | `Integer` | `AGE` | `NOT NULL` | 年齢 |
| `gender` | `String` | `GENDER` | `NOT NULL, VARCHAR(10)` | 性別（male/female） |

* アノテーション:

```java
@Entity
@Table(name = "PERSON")
```

* 主キー:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "PERSON_ID")
private Integer personId;
```

* 注意: Personエンティティは共通機能タスクで実装済み。

---

## 3. 画面設計

### 3.1 Facelets XHTML構造

* ファイル名: `personList.xhtml`
* 配置先: `src/main/webapp/personList.xhtml`

* 主要コンポーネント:
  * `<h:head>` - ページヘッダー（タイトル、スタイルシート）
  * `<h:body>` - ページボディ
  * `<h:messages>` - エラーメッセージ表示エリア
  * `<h:link>` - 新規追加ボタン
  * `<h:form>` - メインフォーム
  * `<h:dataTable>` - PERSONリスト表示テーブル
  * `<h:commandButton>` - 削除ボタン

* レイアウト:

```xhtml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="jakarta.faces.html"
      xmlns:f="jakarta.faces.core"
      xmlns:ui="jakarta.faces.facelets">

<h:head>
    <meta charset="UTF-8"/>
    <title>PERSON一覧</title>
    <h:outputStylesheet name="css/style.css"/>
</h:head>

<h:body>
    <h1>PERSON一覧</h1>
    
    <!-- エラーメッセージ表示エリア -->
    <h:messages globalOnly="false" showSummary="true" showDetail="false" 
                styleClass="error-messages"/>
    
    <!-- 新規追加ボタン -->
    <h:link outcome="personInput" value="新規追加" styleClass="button-link add"/>
    
    <!-- PERSONリスト表示テーブル -->
    <h:form>
        <h:dataTable value="#{personListBean.personList}" var="person" styleClass="person-table">
            <!-- IDカラム -->
            <h:column>
                <f:facet name="header">ID</f:facet>
                <h:outputText value="#{person.personId}"/>
            </h:column>
            
            <!-- 名前カラム -->
            <h:column>
                <f:facet name="header">名前</f:facet>
                <h:outputText value="#{person.personName}"/>
            </h:column>
            
            <!-- 年齢カラム -->
            <h:column>
                <f:facet name="header">年齢</f:facet>
                <h:outputText value="#{person.age}"/>
            </h:column>
            
            <!-- 性別カラム -->
            <h:column>
                <f:facet name="header">性別</f:facet>
                <h:outputText value="男性" rendered="#{person.gender == 'male'}"/>
                <h:outputText value="女性" rendered="#{person.gender == 'female'}"/>
            </h:column>
            
            <!-- 操作カラム -->
            <h:column>
                <f:facet name="header">操作</f:facet>
                <!-- 編集ボタン -->
                <h:link outcome="personInput" value="編集" styleClass="button-link">
                    <f:param name="personId" value="#{person.personId}"/>
                </h:link>
                <!-- 削除ボタン -->
                <h:commandButton value="削除" 
                                 action="#{personListBean.deletePerson(person.personId)}"
                                 onclick="return confirm('削除してもよろしいですか？');"
                                 styleClass="button-link delete">
                    <f:ajax execute="@form" render="@all"/>
                </h:commandButton>
            </h:column>
        </h:dataTable>
    </h:form>
</h:body>
</html>
```

### 3.2 コンポーネント詳細

#### 新規追加ボタン

* コンポーネント: `<h:link>`
* outcome: `personInput`（PERSON入力画面に遷移）
* スタイルクラス: `button-link add`

```xhtml
<h:link outcome="personInput" value="新規追加" styleClass="button-link add"/>
```

#### 編集ボタン

* コンポーネント: `<h:link>`
* outcome: `personInput`（PERSON入力画面に遷移）
* パラメータ: `personId`（編集対象のID）
* スタイルクラス: `button-link`

```xhtml
<h:link outcome="personInput" value="編集" styleClass="button-link">
    <f:param name="personId" value="#{person.personId}"/>
</h:link>
```

#### 削除ボタン

* コンポーネント: `<h:commandButton>`
* action: `#{personListBean.deletePerson(person.personId)}`
* onclick: JavaScript確認ダイアログ（`confirm('削除してもよろしいですか？')`）
* Ajax: `<f:ajax execute="@form" render="@all"/>`（部分更新）
* スタイルクラス: `button-link delete`

```xhtml
<h:commandButton value="削除" 
                 action="#{personListBean.deletePerson(person.personId)}"
                 onclick="return confirm('削除してもよろしいですか？');"
                 styleClass="button-link delete">
    <f:ajax execute="@form" render="@all"/>
</h:commandButton>
```

#### 性別表示

* 変換ロジック: `rendered`属性で条件分岐
  * "male" → "男性"
  * "female" → "女性"

```xhtml
<h:outputText value="男性" rendered="#{person.gender == 'male'}"/>
<h:outputText value="女性" rendered="#{person.gender == 'female'}"/>
```

---

## 4. 画面遷移

### 4.1 遷移パターン

| 遷移元 | 遷移先 | 条件 | データ受け渡し |
|-------|-------|-----|--------------|
| personList.xhtml | personInput.xhtml | 新規追加ボタンをクリック | なし（新規追加モード） |
| personList.xhtml | personInput.xhtml?personId=xxx | 編集ボタンをクリック | URLパラメータ（personId） |
| personList.xhtml | personList.xhtml | 削除ボタンをクリック | なし（同じページをリロード） |

### 4.2 遷移方式

* 新規追加ボタン:
  * `<h:link outcome="personInput">`による通常遷移
  * URLパラメータなし

* 編集ボタン:
  * `<h:link outcome="personInput">`による通常遷移
  * `<f:param name="personId" value="#{person.personId}"/>`でURLパラメータを渡す

* 削除ボタン:
  * `action="#{personListBean.deletePerson(person.personId)}"`
  * 戻り値は`null`（同じページをリロード）
  * Ajax部分更新（`<f:ajax execute="@form" render="@all"/>`）

### 4.3 画面遷移図

```
personList.xhtml
    ├─→ personInput.xhtml（新規追加）
    ├─→ personInput.xhtml?personId=xxx（編集）
    └─→ personList.xhtml（削除後リロード）
```

---

## 5. バリデーション

### 5.1 入力バリデーション

* 本画面には入力項目がないため、バリデーションなし

---

## 6. エラーハンドリング

### 6.1 エラーシナリオ

| エラーケース | 処理 | メッセージ |
|------------|-----|----------|
| データ取得エラー | PersonService.getAllPersons()でRuntimeException発生 | "データ取得に失敗しました: [エラー詳細]" |
| 削除処理エラー | PersonService.deletePerson()でRuntimeException発生 | "削除処理に失敗しました: [エラー詳細]" |
| 削除対象不在 | em.find()でnullが返される | "削除対象のPERSONが見つかりませんでした: personId=[ID]" |

### 6.2 エラーメッセージ表示

* Faceletsでの表示:

```xhtml
<h:messages globalOnly="false" showSummary="true" showDetail="false" 
            styleClass="error-messages"/>
```

* Managed Beanからのメッセージ追加:

```java
FacesContext context = FacesContext.getCurrentInstance();
context.addMessage(null, 
    new FacesMessage(FacesMessage.SEVERITY_ERROR, "エラー", "エラーメッセージ"));
```

* 成功メッセージ:

```java
FacesContext context = FacesContext.getCurrentInstance();
context.addMessage(null, 
    new FacesMessage(FacesMessage.SEVERITY_INFO, "成功", "PERSONを削除しました"));
```

### 6.3 エラーハンドリングフロー

* データ取得エラー:
  1. PersonListBean.init()でRuntimeExceptionをキャッチ
  2. エラーメッセージを表示
  3. personListに空のリストを設定

* 削除処理エラー:
  1. PersonListBean.deletePerson()でRuntimeExceptionをキャッチ
  2. トランザクションが自動的にロールバック
  3. エラーメッセージを表示
  4. nullを返して画面はそのまま

---

## 7. トランザクション設計

### 7.1 トランザクション境界

* PersonService.getAllPersons():
  * トランザクション: READ_COMMITTED
  * 読み取り専用
  * コミット: メソッド終了時

* PersonService.deletePerson(Integer personId):
  * トランザクション: READ_COMMITTED
  * 書き込み: DELETE操作
  * コミット: メソッド正常終了時
  * ロールバック: RuntimeException発生時

### 7.2 トランザクション管理

* @Transactionalアノテーションによる宣言的トランザクション管理
* JTA（Jakarta Transactions）による自動管理
* RuntimeException発生時は自動ロールバック

---

## 8. セキュリティ設計

### 8.1 XSS対策

* `<h:outputText>`は自動的にHTMLエスケープを行う
* personName、age、genderの表示は安全

### 8.2 CSRF対策

* `<h:form>`は自動的にCSRFトークンを生成
* `jakarta.faces.ViewState` hidden fieldによる保護

### 8.3 削除確認

* JavaScript確認ダイアログ: "削除してもよろしいですか？"
* 誤削除を防止

---

## 9. パフォーマンス設計

### 9.1 データ取得最適化

* データベースクエリ: 1回のSELECT文
* N+1問題: なし（全データを一度に取得）
* ページネーション: なし（全件表示）

### 9.2 Ajax部分更新

* 削除ボタンクリック時にAjaxで部分更新
* `<f:ajax execute="@form" render="@all"/>`
* 全画面をリフレッシュせず、必要な部分のみ更新

---

## 10. スタイルシート設計

### 10.1 CSSクラス

* `button-link`: ボタン風リンクのスタイル
* `button-link add`: 新規追加ボタンのスタイル（緑色背景）
* `button-link delete`: 削除ボタンのスタイル（赤色背景）
* `error-messages`: エラーメッセージのスタイル
* `person-table`: PERSONリスト表のスタイル

### 10.2 CSS実装例

* CSSファイル: `src/main/webapp/resources/css/style.css`

```css
.button-link {
    display: inline-block;
    padding: 8px 16px;
    background-color: #007bff;
    color: white;
    text-decoration: none;
    border-radius: 4px;
    border: none;
    cursor: pointer;
}

.button-link.add {
    background-color: #28a745;
}

.button-link.delete {
    background-color: #dc3545;
}

.error-messages {
    color: red;
    font-weight: bold;
    margin-bottom: 10px;
}

.person-table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 20px;
}

.person-table th, .person-table td {
    border: 1px solid #ddd;
    padding: 8px;
    text-align: left;
}

.person-table th {
    background-color: #f2f2f2;
}
```

---

## 11. テスト要件

### 11.1 ユニットテスト

* 対象: `PersonListBean`

* テストケース:
  * init()メソッドのテスト:
    * 正常系: personService.getAllPersons()が正常に動作する場合、personListが設定される
    * 異常系: personService.getAllPersons()がRuntimeExceptionをスローする場合、エラーメッセージが表示され、personListが空のリストになる
  
  * deletePerson()メソッドのテスト:
    * 正常系: personService.deletePerson()が正常に動作する場合、削除が成功し、リストが更新される
    * 異常系: personService.deletePerson()がRuntimeExceptionをスローする場合、エラーメッセージが表示される

### 11.2 画面テスト

* 対象: personList.xhtml

* テストケース:
  * 初期表示テスト:
    * データベースに3件のPERSONが存在する場合、3件が表示される
    * 性別が "male" の場合、"男性" と表示される
    * 性別が "female" の場合、"女性" と表示される
  
  * 新規追加ボタンテスト:
    * 新規追加ボタンをクリックした場合、personInput.xhtmlに遷移する
  
  * 編集ボタンテスト:
    * 編集ボタンをクリックした場合、personInput.xhtml?personId=xxxに遷移する
  
  * 削除ボタンテスト:
    * 削除ボタンをクリックした場合、削除確認ダイアログが表示される
    * 削除確認ダイアログで「OK」をクリックした場合、PERSONが削除される
    * 削除確認ダイアログで「キャンセル」をクリックした場合、PERSONは削除されない

### 11.3 統合テスト

* テストケース:
  * PERSON削除後にリストが更新される
  * データ取得エラー時にエラーメッセージが表示される
  * 削除処理エラー時にエラーメッセージが表示される

---

## 12. ログ出力設計

### 12.1 ログ出力タイミング

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

### 12.2 ログ実装例

* Java:

```java
import java.util.logging.Level;
import java.util.logging.Logger;

private static final Logger logger = Logger.getLogger(PersonListBean.class.getName());

@PostConstruct
public void init() {
    try {
        personList = personService.getAllPersons();
        logger.log(Level.INFO, "PersonList initialized: {0} persons loaded", personList.size());
    } catch (RuntimeException e) {
        logger.log(Level.SEVERE, "Failed to load person list", e);
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "エラー", "データ取得に失敗しました: " + e.getMessage()));
        personList = new ArrayList<>();
    }
}

public String deletePerson(Integer personId) {
    try {
        personService.deletePerson(personId);
        logger.log(Level.INFO, "Person deleted successfully: personId={0}", personId);
        init();
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
            "成功", "PERSONを削除しました"));
        return null;
    } catch (RuntimeException e) {
        logger.log(Level.SEVERE, "Failed to delete person: personId=" + personId, e);
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "エラー", "削除処理に失敗しました: " + e.getMessage()));
        return null;
    }
}
```

---

## 13. 並行アクセス設計

### 13.1 同時削除

* シナリオ: ユーザーAとユーザーBが同じPERSONを同時に削除しようとする
* 対応:
  * ユーザーAの削除が成功する
  * ユーザーBの削除時、em.find()でnullが返される
  * ユーザーBにエラーメッセージが表示される: "削除対象のPERSONが見つかりませんでした: personId=[ID]"

### 13.2 削除中の編集

* シナリオ: ユーザーAがPERSONを削除し、ユーザーBが同じPERSONを編集しようとする
* 対応:
  * ユーザーAの削除が成功する
  * ユーザーBが編集画面を開く
  * PersonService.getPersonById()でnullが返される
  * エラーメッセージが表示される

---

## 14. 実装チェックリスト

### 仕様理解の確認

* [x] 画面の目的と機能を理解している
* [x] 表示項目と入力項目を把握している
* [x] ボタンと画面遷移を把握している
* [x] ビジネスルールを理解している
* [x] エラーケースを把握している

### パッケージ構造の確認

* [x] ベースパッケージを確認した: pro.kensait.jsf.person
* [x] 命名規則を確認した: PersonListBean
* [x] 実装が必要なクラスをリストアップした: PersonListBean

### Managed Bean設計の確認

* [x] Bean名とスコープを確認した: personListBean、@ViewScoped
* [x] プロパティ一覧を確認した: personService、personList
* [x] アクションメソッド一覧を確認した: init()、getPersonList()、deletePerson()
* [x] URLパラメータでのデータ受け渡しを確認した

### データモデルの確認

* [x] エンティティのテーブル定義を確認した: PERSON
* [x] フィールド、型、制約を確認した
* [x] リレーションを確認した: なし

---

## 15. 参考資料

* [screen_design.md](screen_design.md) - 画面設計書
* [functional_design.md](functional_design.md) - 機能設計書
* [behaviors.md](behaviors.md) - 振る舞い仕様書
* [architecture_design.md](../../system/architecture_design.md) - アーキテクチャ設計書
* [data_model.md](../../system/data_model.md) - データモデル設計書
* [Jakarta Faces 4.0仕様](https://jakarta.ee/specifications/faces/4.0/)
* [Jakarta Persistence 3.1仕様](https://jakarta.ee/specifications/persistence/3.1/)
