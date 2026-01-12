# 機能設計書 - SCREEN_002_PersonInput

## 1. 概要

PERSON入力画面（SCREEN_002_PersonInput）の機能設計を定義する。

## 2. Managed Bean設計

### 2.1 PersonInputBean

* パッケージ: pro.kensait.jsf.person.bean
* クラス名: PersonInputBean
* アノテーション:
  * @Named("personInputBean")
  * @ViewScoped
  * implements Serializable

#### 2.1.1 フィールド

* personService: PersonService
  * 型: PersonService
  * アノテーション: @Inject
  * 説明: ビジネスロジック層のPersonServiceを注入

* personId: Integer
  * 型: Integer
  * 説明: PERSON_ID（編集モードの場合に設定）
  * 初期値: null（新規追加モード）

* personName: String
  * 型: String
  * アノテーション:
    * @NotNull(message = "名前を入力してください")
    * @Size(min = 1, max = 30, message = "名前は1〜30文字で入力してください")
  * 説明: 名前

* age: Integer
  * 型: Integer
  * アノテーション:
    * @NotNull(message = "年齢を入力してください")
    * @Min(value = 0, message = "年齢は0以上で入力してください")
    * @Max(value = 150, message = "年齢は150以下で入力してください")
  * 説明: 年齢

* gender: String
  * 型: String
  * アノテーション:
    * @NotNull(message = "性別を選択してください")
  * 説明: 性別（"male" または "female"）

#### 2.1.2 メソッド

* init(): void
  * アノテーション: @PostConstruct
  * 説明: 画面初期表示時に自動的に実行される
  * 処理:
    1. URLパラメータからpersonIdを取得（<f:viewParam>経由）
    2. personIdがnull以外の場合（編集モード）:
       * personService.getPersonById(personId)を呼び出して既存データを取得
       * 取得したPersonデータをフィールドに設定
    3. personIdがnullの場合（新規追加モード）:
       * フィールドを初期化（nullまたは空文字列）

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
                    "エラー", "指定されたPERSONが見つかりませんでした"));
            }
        } catch (RuntimeException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "エラー", "データ取得に失敗しました: " + e.getMessage()));
        }
    }
}
```

* confirm(): String
  * 戻り値: String（画面遷移先）
  * 説明: 確認画面に遷移する
  * 処理:
    1. "personConfirm"を返す（personConfirm.xhtmlに遷移）
    2. 入力データはPersonInputBeanのフィールドに保持される（@ViewScoped）
    3. PersonConfirmBeanがこのデータを参照する

```java
public String confirm() {
    return "personConfirm";
}
```

* cancel(): String
  * 戻り値: String（画面遷移先）
  * 説明: 一覧画面に戻る
  * 処理:
    1. "personList?faces-redirect=true"を返す（リダイレクト遷移）
    2. 入力データは破棄される

```java
public String cancel() {
    return "personList?faces-redirect=true";
}
```

#### 2.1.3 完全なクラス実装例

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
                        "エラー", "指定されたPERSONが見つかりませんでした"));
                }
            } catch (RuntimeException e) {
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "エラー", "データ取得に失敗しました: " + e.getMessage()));
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

## 3. Service設計

### 3.1 PersonService

* パッケージ: pro.kensait.jsf.person.service
* クラス名: PersonService
* アノテーション:
  * @RequestScoped
  * @Transactional

#### 3.1.1 使用するメソッド

* getPersonById(Integer personId): Person
  * 引数: personId（PERSON_ID）
  * 戻り値: Person（見つからない場合はnull）
  * 説明: 指定されたIDのPERSONを取得する
  * トランザクション: READ_COMMITTED
  * JPA APIメソッド: em.find(Person.class, personId)

```java
public Person getPersonById(Integer personId) {
    return em.find(Person.class, personId);
}
```

## 4. データアクセス設計

### 4.1 EntityManager使用

* Persistence Context: personPU
* EntityManagerのインジェクション:

```java
@PersistenceContext
private EntityManager em;
```

### 4.2 データ取得

* 既存データの取得:

```java
Person person = em.find(Person.class, personId);
```

* 説明:
  * 主キーでエンティティを検索
  * 該当レコードがない場合はnullを返す

## 5. ビュー設計

### 5.1 personInput.xhtml

* ビュー技術: Facelets XHTML
* 名前空間:
  * xmlns:h="jakarta.faces.html"
  * xmlns:f="jakarta.faces.core"

### 5.2 主要コンポーネント

* <f:metadata>: ViewParametersの定義

```xml
<f:metadata>
    <f:viewParam name="personId" value="#{personInputBean.personId}"/>
</f:metadata>
```

* <h:head>: ページヘッダー

```xml
<h:head>
    <meta charset="UTF-8"/>
    <title>PERSON入力</title>
    <h:outputStylesheet name="css/style.css"/>
</h:head>
```

* <h:body>: ページボディ

```xml
<h:body>
    <h1>PERSON入力</h1>
    
    <h:messages globalOnly="false" showSummary="true" showDetail="false" 
                styleClass="error-messages"/>
    
    <h:form>
        <!-- 入力フィールド -->
        <!-- ボタン -->
    </h:form>
</h:body>
```

* 入力フォーム:

```xml
<h:form>
    <h:inputHidden value="#{personInputBean.personId}"/>
    
    <div class="form-group">
        <label for="personName">名前:</label>
        <h:inputText id="personName" value="#{personInputBean.personName}" size="30"/>
    </div>
    
    <div class="form-group">
        <label for="age">年齢:</label>
        <h:inputText id="age" value="#{personInputBean.age}" size="10"/>
    </div>
    
    <div class="form-group">
        <label>性別:</label>
        <h:selectOneRadio value="#{personInputBean.gender}">
            <f:selectItem itemValue="male" itemLabel="男性"/>
            <f:selectItem itemValue="female" itemLabel="女性"/>
        </h:selectOneRadio>
    </div>
    
    <div class="form-group">
        <h:commandButton value="確認画面へ" action="#{personInputBean.confirm}" 
                         styleClass="button"/>
        <h:commandButton value="キャンセル" action="#{personInputBean.cancel}" 
                         immediate="true" styleClass="button cancel"/>
    </div>
</h:form>
```

## 6. 画面遷移設計

### 6.1 遷移先

* personConfirm.xhtml（PERSON確認画面）
  * 確認画面へボタンから遷移
  * 通常遷移（Forward）
  * 入力データはPersonInputBeanのフィールドに保持される

* personList.xhtml（PERSON一覧画面）
  * キャンセルボタンから遷移
  * リダイレクト遷移
  * 入力データは破棄される

### 6.2 遷移方式

* 確認画面へボタン: 戻り値 "personConfirm"
* キャンセルボタン: 戻り値 "personList?faces-redirect=true"

## 7. バリデーション設計

### 7.1 Bean Validationアノテーション

* PersonInputBeanのフィールドにアノテーションを付加
* JSFが自動的にバリデーションを実行

### 7.2 バリデーション実行タイミング

* 確認画面へボタンクリック時
* immediate="false"（デフォルト）のため、バリデーションが実行される

### 7.3 エラーメッセージ表示

* <h:messages>コンポーネント
* FacesContext.addMessage()でメッセージを追加

## 8. エラーハンドリング設計

### 8.1 データ取得エラー

* PersonService.getPersonById()でRuntimeExceptionが発生
* エラーメッセージを表示し、フィールドは初期化される

### 8.2 存在しないPERSON

* em.find()でnullが返される
* エラーメッセージを表示: "指定されたPERSONが見つかりませんでした"

## 9. 参考資料

* [システム要件定義](../../system/requirements.md)
* [アーキテクチャ設計書](../../system/architecture_design.md)
* [機能設計書](../../system/functional_design.md)
* [データモデル](../../system/data_model.md)
* [SCREEN_002_PersonInput画面設計](screen_design.md)
* [SCREEN_002_PersonInput振る舞い仕様](behaviors.md)
