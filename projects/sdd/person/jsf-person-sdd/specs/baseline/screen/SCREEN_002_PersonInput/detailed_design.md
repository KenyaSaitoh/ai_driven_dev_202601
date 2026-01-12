# SCREEN_002_PersonInput PERSON入力画面 - 詳細設計書

* 画面ID: SCREEN_002_PersonInput
* 画面名: PERSON入力画面
* バージョン: 1.0.0
* 最終更新: 2026-01-12

---

## 1. パッケージ構造

### 1.1 関連パッケージ

```
pro.kensait.jsf.person
├── bean
│   └── PersonInputBean.java       # JSF Managed Bean
├── entity
│   └── Person.java                # JPA Entity
└── service
    └── PersonService.java         # CDI Service
```

### 1.2 ビューファイル

```
src/main/webapp/
└── person/
    └── input.xhtml                # Facelets XHTML
```

---

## 2. クラス設計

### 2.1 PersonInputBean（JSF Managed Bean）

* 責務: PERSON入力画面の制御、フォームデータの管理、画面遷移制御

* アノテーション:
  * `@Named("personInputBean")` - JSF Managed Bean名
  * `@ViewScoped` - ビュースコープ（画面表示中のみ有効、Forward遷移後も維持）
  * `implements Serializable` - シリアライズ対応

* フィールド:

| プロパティ名 | 型 | アノテーション | 説明 |
|------------|---|--------------|------|
| `serialVersionUID` | `long` | `private static final` | シリアライズバージョンID |
| `personService` | `PersonService` | `@Inject` | ビジネスロジック層のサービス |
| `personId` | `Integer` | - | PERSON_ID（編集モード時に設定） |
| `personName` | `String` | `@NotNull`, `@Size(min=1, max=30)` | 名前 |
| `age` | `Integer` | `@NotNull`, `@Min(0)`, `@Max(150)` | 年齢 |
| `gender` | `String` | `@NotNull` | 性別（"male"または"female"） |

* Bean Validationアノテーション詳細:

```java
@NotNull(message = "名前を入力してください")
@Size(min = 1, max = 30, message = "名前は1〜30文字で入力してください")
private String personName;

@NotNull(message = "年齢を入力してください")
@Min(value = 0, message = "年齢は0以上で入力してください")
@Max(value = 150, message = "年齢は150以下で入力してください")
private Integer age;

@NotNull(message = "性別を選択してください")
private String gender;
```

* メソッド一覧:

| メソッド名 | 戻り値 | 説明 |
|----------|-------|------|
| `init()` | `void` | 画面初期化処理（@PostConstruct） |
| `confirm()` | `String` | 確認画面へ遷移 |
| `cancel()` | `String` | 一覧画面へ戻る |
| `getPersonId()` | `Integer` | personIdのgetter |
| `setPersonId(Integer)` | `void` | personIdのsetter |
| `getPersonName()` | `String` | personNameのgetter |
| `setPersonName(String)` | `void` | personNameのsetter |
| `getAge()` | `Integer` | ageのgetter |
| `setAge(Integer)` | `void` | ageのsetter |
| `getGender()` | `String` | genderのgetter |
| `setGender(String)` | `void` | genderのsetter |

#### 2.1.1 init() - 画面初期化処理

* シグネチャ:

```java
@PostConstruct
public void init()
```

* 説明:
  * 画面表示時に自動的に実行される（@PostConstruct）
  * URLパラメータからpersonIdを取得し、編集モードか新規追加モードかを判定
  * 編集モードの場合は既存データを取得してフィールドに設定

* 処理フロー:
  1. personIdがnullかチェック
  2. personIdがnull以外の場合（編集モード）:
     * PersonService.getPersonById(personId)を呼び出す
     * 取得したPersonデータが存在する場合:
       * personName、age、genderをフィールドに設定
     * 取得したPersonデータがnullの場合:
       * FacesContext.addMessage()でエラーメッセージを追加
       * メッセージ: "指定されたPERSONが見つかりませんでした"
  3. RuntimeException発生時:
     * FacesContext.addMessage()でエラーメッセージを追加
     * メッセージ: "データ取得に失敗しました: [エラー詳細]"
  4. personIdがnullの場合（新規追加モード）:
     * 何もしない（フィールドはnullまたは空のまま）

* 実装例:

```java
@PostConstruct
public void init() {
    if (personId != null) {
        try {
            Person person = personService.getPersonById(personId);
            if (person != null) {
                this.personName = person.getPersonName();
                this.age = person.getAge();
                this.gender = person.getGender();
            } else {
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "指定されたPERSONが見つかりませんでした", null));
            }
        } catch (RuntimeException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "データ取得に失敗しました: " + e.getMessage(), null));
        }
    }
}
```

* エラーケース:
  * personIdに対応するPERSONが存在しない → エラーメッセージを表示し、フィールドは空のまま
  * データベース接続エラー → エラーメッセージを表示し、フィールドは空のまま

#### 2.1.2 confirm() - 確認画面へ遷移

* シグネチャ:

```java
public String confirm()
```

* 説明:
  * 確認画面に遷移する
  * バリデーションが成功した場合のみ実行される（JSFが自動的に検証）

* 処理フロー:
  1. "personConfirm"を返す
  2. JSFがpersonConfirm.xhtmlに遷移（Forward）
  3. PersonInputBeanは@ViewScopedのため、データは維持される
  4. PersonConfirmBeanがこのBeanを参照してデータを取得

* 実装例:

```java
public String confirm() {
    return "personConfirm";
}
```

* 戻り値: `"personConfirm"` - personConfirm.xhtmlに遷移

#### 2.1.3 cancel() - 一覧画面へ戻る

* シグネチャ:

```java
public String cancel()
```

* 説明:
  * 一覧画面に戻る
  * 入力データは破棄される

* 処理フロー:
  1. "personList?faces-redirect=true"を返す
  2. JSFがpersonList.xhtmlにリダイレクト
  3. @ViewScopedのBeanが破棄される

* 実装例:

```java
public String cancel() {
    return "personList?faces-redirect=true";
}
```

* 戻り値: `"personList?faces-redirect=true"` - personList.xhtmlにリダイレクト

* 備考:
  * `immediate="true"`属性により、バリデーションはスキップされる

#### 2.1.4 完全なクラス実装

* Java実装:

```java
package pro.kensait.jsf.person.bean;

import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import pro.kensait.jsf.person.entity.Person;
import pro.kensait.jsf.person.service.PersonService;

@Named("personInputBean")
@ViewScoped
public class PersonInputBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private PersonService personService;

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

    @PostConstruct
    public void init() {
        if (personId != null) {
            try {
                Person person = personService.getPersonById(personId);
                if (person != null) {
                    this.personName = person.getPersonName();
                    this.age = person.getAge();
                    this.gender = person.getGender();
                } else {
                    FacesContext context = FacesContext.getCurrentInstance();
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "指定されたPERSONが見つかりませんでした", null));
                }
            } catch (RuntimeException e) {
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "データ取得に失敗しました: " + e.getMessage(), null));
            }
        }
    }

    public String confirm() {
        return "personConfirm";
    }

    public String cancel() {
        return "personList?faces-redirect=true";
    }

    // Getter/Setter
    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
```

---

### 2.2 PersonService（ビジネスロジック層）

* 責務: PERSONビジネスロジックの実装、データアクセス制御

* アノテーション:
  * `@RequestScoped` - リクエストスコープ
  * `@Transactional(TxType.REQUIRED)` - トランザクション管理

* 使用するメソッド:

#### 2.2.1 getPersonById(Integer personId) - IDでPERSON取得

* シグネチャ:

```java
public Person getPersonById(Integer personId)
```

* 説明:
  * 指定されたIDのPERSONを取得する
  * 主キー検索のため高速

* 処理:
  1. EntityManager.find(Person.class, personId)を呼び出す
  2. 見つかった場合はPersonエンティティを返す
  3. 見つからない場合はnullを返す

* JPA APIメソッド:

```java
em.find(Person.class, personId)
```

* 実装例:

```java
public Person getPersonById(Integer personId) {
    return em.find(Person.class, personId);
}
```

* トランザクション: READ_COMMITTED（読み取り専用）
* 戻り値: Person（見つからない場合はnull）

---

### 2.3 Person（エンティティ）

* テーブル: `PERSON`

* 主要フィールド:

| フィールド名 | 型 | カラム名 | 制約 | 説明 |
|------------|---|---------|-----|------|
| `personId` | `Integer` | `PERSON_ID` | `PRIMARY KEY`, `IDENTITY` | 人材ID（主キー、自動採番） |
| `personName` | `String` | `PERSON_NAME` | `NOT NULL`, `length=30` | 人材名 |
| `age` | `Integer` | `AGE` | `NOT NULL` | 年齢 |
| `gender` | `String` | `GENDER` | `NOT NULL`, `length=10` | 性別（male/female） |

* アノテーション:

```java
@Entity
@Table(name = "PERSON")
public class Person implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PERSON_ID")
    private Integer personId;

    @Column(name = "PERSON_NAME", nullable = false, length = 30)
    private String personName;

    @Column(name = "AGE", nullable = false)
    private Integer age;

    @Column(name = "GENDER", nullable = false, length = 10)
    private String gender;
}
```

* リレーション: なし（単独テーブル）

---

## 3. 画面設計

### 3.1 Facelets XHTML構造

* ファイル名: `input.xhtml`
* パス: `src/main/webapp/person/input.xhtml`
* アクセスURL: `http://localhost:8080/jsf-person-sdd/person/input.xhtml`

* 主要コンポーネント:
  * `<f:metadata>` - ViewParametersの定義
  * `<f:viewParam>` - URLパラメータのマッピング
  * `<h:head>` - ページヘッダー
  * `<h:body>` - ページボディ
  * `<h:messages>` - エラーメッセージ表示
  * `<h:form>` - メインフォーム
  * `<h:inputHidden>` - personId（hidden）
  * `<h:inputText>` - テキスト入力
  * `<h:selectOneRadio>` - ラジオボタン
  * `<h:commandButton>` - ボタン

### 3.2 XHTMLレイアウト

* XHTML実装:

```xhtml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="jakarta.faces.html"
      xmlns:f="jakarta.faces.core">
<f:metadata>
    <f:viewParam name="personId" value="#{personInputBean.personId}"/>
</f:metadata>

<h:head>
    <meta charset="UTF-8"/>
    <title>PERSON入力</title>
    <h:outputStylesheet name="css/style.css"/>
</h:head>

<h:body>
    <h1>PERSON入力</h1>
    
    <h:messages globalOnly="false" showSummary="true" showDetail="false" 
                styleClass="error-messages"/>
    
    <h:form>
        <h:inputHidden value="#{personInputBean.personId}"/>
        
        <div class="form-group">
            <label for="personName">名前:</label>
            <h:inputText id="personName" value="#{personInputBean.personName}" 
                         size="30" required="true"/>
            <h:message for="personName" styleClass="error-message"/>
        </div>
        
        <div class="form-group">
            <label for="age">年齢:</label>
            <h:inputText id="age" value="#{personInputBean.age}" 
                         size="10" required="true"/>
            <h:message for="age" styleClass="error-message"/>
        </div>
        
        <div class="form-group">
            <label>性別:</label>
            <h:selectOneRadio value="#{personInputBean.gender}" required="true">
                <f:selectItem itemValue="male" itemLabel="男性"/>
                <f:selectItem itemValue="female" itemLabel="女性"/>
            </h:selectOneRadio>
            <h:message for="gender" styleClass="error-message"/>
        </div>
        
        <div class="form-group">
            <h:commandButton value="確認画面へ" action="#{personInputBean.confirm}" 
                             styleClass="button"/>
            <h:commandButton value="キャンセル" action="#{personInputBean.cancel}" 
                             immediate="true" styleClass="button cancel"/>
        </div>
    </h:form>
</h:body>
</html>
```

### 3.3 コンポーネント詳細

#### 3.3.1 ViewParameterマッピング

* 用途: URLパラメータpersonIdをBeanのpersonIdフィールドにマッピング
* 実装:

```xhtml
<f:metadata>
    <f:viewParam name="personId" value="#{personInputBean.personId}"/>
</f:metadata>
```

* 動作:
  * URL例: `/person/input.xhtml?personId=2`
  * personInputBean.setPersonId(2)が自動的に呼ばれる
  * その後、@PostConstructのinit()メソッドが実行される

#### 3.3.2 エラーメッセージ表示

* グローバルメッセージ:

```xhtml
<h:messages globalOnly="false" showSummary="true" showDetail="false" 
            styleClass="error-messages"/>
```

* フィールド固有メッセージ:

```xhtml
<h:message for="personName" styleClass="error-message"/>
```

#### 3.3.3 入力フィールド

* personName（名前）:

```xhtml
<h:inputText id="personName" value="#{personInputBean.personName}" 
             size="30" required="true"/>
```

* age（年齢）:

```xhtml
<h:inputText id="age" value="#{personInputBean.age}" 
             size="10" required="true"/>
```

* gender（性別）:

```xhtml
<h:selectOneRadio value="#{personInputBean.gender}" required="true">
    <f:selectItem itemValue="male" itemLabel="男性"/>
    <f:selectItem itemValue="female" itemLabel="女性"/>
</h:selectOneRadio>
```

#### 3.3.4 ボタン

* 確認画面へボタン:

```xhtml
<h:commandButton value="確認画面へ" action="#{personInputBean.confirm}" 
                 styleClass="button"/>
```

* 属性:
  * `immediate="false"`（デフォルト）: バリデーションを実行
  * バリデーション成功後にconfirm()メソッドが実行される

* キャンセルボタン:

```xhtml
<h:commandButton value="キャンセル" action="#{personInputBean.cancel}" 
                 immediate="true" styleClass="button cancel"/>
```

* 属性:
  * `immediate="true"`: バリデーションをスキップ
  * ユーザーが入力途中でもキャンセルできる

---

## 4. 画面遷移

### 4.1 遷移パターン

| 遷移元 | 遷移先 | 条件 | データ受け渡し |
|-------|-------|-----|--------------|
| personList.xhtml | personInput.xhtml | 新規追加ボタン | personId=null |
| personList.xhtml | personInput.xhtml | 編集ボタン | URLパラメータ（personId） |
| personInput.xhtml | personConfirm.xhtml | 確認画面へボタン（バリデーション成功） | Forward（@ViewScoped Bean維持） |
| personInput.xhtml | personList.xhtml | キャンセルボタン | Redirect（Bean破棄） |

### 4.2 遷移方式詳細

#### 4.2.1 一覧画面から入力画面への遷移

* 新規追加モード:
  * URL: `/person/input.xhtml`
  * personIdパラメータなし
  * PersonInputBeanのpersonIdはnull
  * init()メソッドはデータ取得を行わない

* 編集モード:
  * URL: `/person/input.xhtml?personId=2`
  * personIdパラメータあり
  * PersonInputBeanのpersonIdに2が設定される
  * init()メソッドでpersonId=2のデータを取得

#### 4.2.2 入力画面から確認画面への遷移

* 遷移種別: Forward（通常遷移）
* 遷移先: `personConfirm.xhtml`
* 戻り値: `"personConfirm"`

* データ受け渡し:
  * PersonInputBeanが@ViewScopedのため、Forward遷移後も維持される
  * PersonConfirmBeanがPersonInputBeanを@Injectして参照
  * personId、personName、age、genderが引き継がれる

* 実装例（PersonConfirmBean）:

```java
@Named("personConfirmBean")
@ViewScoped
public class PersonConfirmBean implements Serializable {
    @Inject
    private PersonInputBean personInputBean;
    
    // personInputBean.getPersonName()等でデータ取得
}
```

#### 4.2.3 入力画面から一覧画面への遷移

* 遷移種別: Redirect
* 遷移先: `personList.xhtml`
* 戻り値: `"personList?faces-redirect=true"`

* データ受け渡し: なし（Bean破棄）

---

## 5. バリデーション

### 5.1 Bean Validation

| フィールド | バリデーション | メッセージ |
|----------|--------------|----------|
| `personName` | `@NotNull` | "名前を入力してください" |
| `personName` | `@Size(min=1, max=30)` | "名前は1〜30文字で入力してください" |
| `age` | `@NotNull` | "年齢を入力してください" |
| `age` | `@Min(0)` | "年齢は0以上で入力してください" |
| `age` | `@Max(150)` | "年齢は150以下で入力してください" |
| `gender` | `@NotNull` | "性別を選択してください" |

### 5.2 バリデーション実行タイミング

* 確認画面へボタンクリック時:
  * JSFがBean Validationアノテーションを自動的に検証
  * Process Validationsフェーズで実行
  * エラーがある場合は、Invoke Applicationフェーズがスキップされる
  * confirm()メソッドは実行されない

* キャンセルボタンクリック時:
  * `immediate="true"`により、バリデーションはスキップされる
  * cancel()メソッドが直ちに実行される

### 5.3 バリデーションエラー時の動作

* JSFライフサイクル:
  1. Restore View
  2. Apply Request Values
  3. Process Validations（バリデーションエラー発生）
  4. Render Response（入力画面を再表示）

* エラーメッセージ表示:
  * `<h:messages>`にグローバルメッセージを表示
  * `<h:message for="fieldId">`にフィールド固有メッセージを表示

* 入力データの保持:
  * PersonInputBeanのフィールドに入力データが保持される
  * ユーザーは修正して再送信できる

### 5.4 カスタムバリデーション

* 現在のバージョンではカスタムバリデーションは不要
* Bean Validationアノテーションで要件を満たせる

---

## 6. エラーハンドリング

### 6.1 エラーシナリオ

| エラーケース | 処理 | メッセージ |
|------------|-----|----------|
| 編集対象が存在しない（personId=999） | FacesMessage追加、フィールドは空のまま表示 | "指定されたPERSONが見つかりませんでした" |
| データベース接続エラー | FacesMessage追加、フィールドは空のまま表示 | "データ取得に失敗しました: [エラー詳細]" |
| バリデーションエラー | 入力画面再表示、エラーメッセージ表示 | Bean Validationのメッセージ |
| 年齢に文字列を入力 | コンバーターエラー、入力画面再表示 | "年齢は数値で入力してください" |

### 6.2 エラーメッセージ追加

* Managed Beanからのメッセージ追加:

```java
FacesContext context = FacesContext.getCurrentInstance();
context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
    "エラーメッセージ", null));
```

* 重要度レベル:
  * `SEVERITY_ERROR`: エラー（赤色表示）
  * `SEVERITY_WARN`: 警告（黄色表示）
  * `SEVERITY_INFO`: 情報（青色表示）

### 6.3 エラーメッセージ表示

* Faceletsでの表示:

```xhtml
<h:messages globalOnly="false" showSummary="true" showDetail="false" 
            styleClass="error-messages"/>
```

* 属性:
  * `globalOnly="false"`: すべてのメッセージを表示（フィールド固有も含む）
  * `showSummary="true"`: メッセージサマリーを表示
  * `showDetail="false"`: 詳細メッセージは非表示
  * `styleClass="error-messages"`: CSSクラス

---

## 7. セッション管理

### 7.1 スコープ管理

* PersonInputBean: `@ViewScoped`
  * 画面表示中のみ有効
  * Forward遷移後も維持される（personConfirm.xhtmlに遷移後も有効）
  * Redirect遷移時は破棄される（personList.xhtmlへのリダイレクト時）

### 7.2 画面間のデータ受け渡し

* 入力画面 → 確認画面:
  * Forward遷移により、PersonInputBeanが維持される
  * PersonConfirmBeanがPersonInputBeanを@Injectして参照
  * personId、personName、age、genderが利用可能

* 実装例（PersonConfirmBeanでの参照）:

```java
@Named("personConfirmBean")
@ViewScoped
public class PersonConfirmBean implements Serializable {
    @Inject
    private PersonInputBean personInputBean;
    
    public String getPersonName() {
        return personInputBean.getPersonName();
    }
    
    public Integer getAge() {
        return personInputBean.getAge();
    }
    
    public String getGender() {
        return personInputBean.getGender();
    }
}
```

---

## 8. スタイルシート

### 8.1 CSSクラス

* `form-group`: 入力フィールドのグループ
* `button`: ボタンのスタイル
* `button cancel`: キャンセルボタンのスタイル
* `error-messages`: エラーメッセージのスタイル
* `error-message`: フィールド固有エラーメッセージのスタイル

### 8.2 CSS実装例

* ファイル: `src/main/webapp/resources/css/style.css`

```css
.form-group {
    margin-bottom: 15px;
}

.form-group label {
    display: inline-block;
    width: 100px;
    font-weight: bold;
}

.button {
    padding: 10px 20px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    margin-right: 10px;
}

.button:hover {
    background-color: #0056b3;
}

.button.cancel {
    background-color: #6c757d;
}

.button.cancel:hover {
    background-color: #545b62;
}

.error-messages {
    color: red;
    font-weight: bold;
    margin-bottom: 15px;
    padding: 10px;
    background-color: #ffe6e6;
    border: 1px solid red;
    border-radius: 4px;
}

.error-message {
    color: red;
    font-size: 0.9em;
    display: block;
    margin-top: 5px;
}
```

---

## 9. ログ出力

### 9.1 ログ出力タイミング

* init()メソッド実行時（編集モード）:
  * ログレベル: INFO
  * メッセージ: "PersonInput initialized in edit mode: personId=[ID]"
  * 実装:

```java
import java.util.logging.Logger;

private static final Logger logger = Logger.getLogger(PersonInputBean.class.getName());

@PostConstruct
public void init() {
    if (personId != null) {
        logger.info("PersonInput initialized in edit mode: personId=" + personId);
        try {
            // データ取得処理
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Failed to load person: personId=" + personId, e);
            // エラーメッセージ表示
        }
    } else {
        logger.info("PersonInput initialized in new mode");
    }
}
```

* エラー発生時:
  * ログレベル: SEVERE
  * メッセージ: "Failed to load person: personId=[ID]"
  * スタックトレースを出力

### 9.2 ロギングフレームワーク

* Jakarta EE標準のjava.util.loggingを使用
* Logger名: クラスのFully Qualified Name（pro.kensait.jsf.person.bean.PersonInputBean）

---

## 10. テスト要件

### 10.1 ユニットテスト

* 対象: `PersonInputBean`

* テストケース:
  * init()メソッド - 新規追加モード:
    * personIdがnullの場合、フィールドが初期化されることを確認
  * init()メソッド - 編集モード（正常系）:
    * personIdが2の場合、PersonService.getPersonById(2)が呼ばれることを確認
    * 取得したデータがフィールドに設定されることを確認
  * init()メソッド - 編集モード（異常系）:
    * personIdが999の場合（データが存在しない）、エラーメッセージが追加されることを確認
    * RuntimeException発生時、エラーメッセージが追加されることを確認
  * confirm()メソッド:
    * "personConfirm"が返されることを確認
  * cancel()メソッド:
    * "personList?faces-redirect=true"が返されることを確認

### 10.2 画面テスト

* 対象: PERSON入力画面

* テストケース:
  * 新規追加モードでアクセス:
    * 入力フィールドが空の状態で表示されることを確認
  * 編集モードでアクセス:
    * 既存データがプリセットされることを確認
  * バリデーションエラー:
    * 名前を空欄で送信 → エラーメッセージが表示されることを確認
    * 年齢に-1を入力 → エラーメッセージが表示されることを確認
    * 性別を未選択で送信 → エラーメッセージが表示されることを確認
  * 正常な入力:
    * 正しいデータを入力して確認画面へボタンをクリック → 確認画面に遷移することを確認
  * キャンセル:
    * キャンセルボタンをクリック → 一覧画面にリダイレクトされることを確認

---

## 11. パフォーマンス

### 11.1 データ取得パフォーマンス

* データベースクエリ: 1回のSELECT文（主キー検索）
* `em.find(Person.class, personId)`は効率的
* インデックス: PRIMARY KEY（PERSON_ID）により高速検索

### 11.2 メモリ使用量

* PersonInputBean: @ViewScopedのため、画面表示中のみメモリに保持
* Forward遷移後も維持されるが、リダイレクト時は破棄される

---

## 12. セキュリティ

### 12.1 XSS対策

* `<h:inputText>`は自動的にHTMLエスケープを行う
* 入力データは安全に処理される

### 12.2 CSRF対策

* `<h:form>`は自動的にCSRFトークンを生成
* jakarta.faces.ViewState hidden fieldによる保護

### 12.3 SQLインジェクション対策

* JPAのEntityManager.find()はパラメータバインディングを使用
* SQLインジェクションのリスクはない

---

## 13. 参考資料

* [screen_design.md](screen_design.md) - 画面設計書
* [functional_design.md](functional_design.md) - 機能設計書
* [behaviors.md](behaviors.md) - 振る舞い仕様書
* [../../system/architecture_design.md](../../system/architecture_design.md) - アーキテクチャ設計書
* [../../system/data_model.md](../../system/data_model.md) - データモデル
* [Jakarta Faces 4.0仕様](https://jakarta.ee/specifications/faces/4.0/)
* [Jakarta Bean Validation 3.0仕様](https://jakarta.ee/specifications/bean-validation/3.0/)
