# 機能設計書 - SCREEN_003_PersonConfirm

## 1. 概要

PERSON確認画面（SCREEN_003_PersonConfirm）の機能設計を定義する。

## 2. Managed Bean設計

### 2.1 PersonConfirmBean

* パッケージ: pro.kensait.jsf.person.bean
* クラス名: PersonConfirmBean
* アノテーション:
  * @Named("personConfirmBean")
  * @ViewScoped
  * implements Serializable

#### 2.1.1 フィールド

* personService: PersonService
  * 型: PersonService
  * アノテーション: @Inject
  * 説明: ビジネスロジック層のPersonServiceを注入

* personInputBean: PersonInputBean
  * 型: PersonInputBean
  * アノテーション: @Inject
  * 説明: PERSON入力画面のBeanを注入し、入力データを参照

* personId: Integer
  * 型: Integer
  * 説明: PERSON_ID（編集モードの場合に設定）
  * 初期値: PersonInputBeanから取得

* personName: String
  * 型: String
  * 説明: 名前
  * 初期値: PersonInputBeanから取得

* age: Integer
  * 型: Integer
  * 説明: 年齢
  * 初期値: PersonInputBeanから取得

* gender: String
  * 型: String
  * 説明: 性別（"male" または "female"）
  * 初期値: PersonInputBeanから取得

#### 2.1.2 メソッド

* init(): void
  * アノテーション: @PostConstruct
  * 説明: 画面初期表示時に自動的に実行される
  * 処理:
    1. PersonInputBeanから入力データを取得
    2. フィールドに設定

```java
@PostConstruct
public void init() {
    this.personId = personInputBean.getPersonId();
    this.personName = personInputBean.getPersonName();
    this.age = personInputBean.getAge();
    this.gender = personInputBean.getGender();
}
```

* save(): String
  * 戻り値: String（画面遷移先）
  * 説明: PERSON情報をデータベースに登録または更新する
  * 処理:
    1. Personオブジェクトを作成
    2. フィールドの値をPersonオブジェクトに設定
    3. personIdがnullの場合（新規追加）:
       * personService.addPerson(person)を呼び出す
    4. personIdがnull以外の場合（更新）:
       * personService.updatePerson(person)を呼び出す
    5. 成功メッセージを設定（オプション）
    6. personList.xhtmlにリダイレクトする

```java
public String save() {
    try {
        Person person = new Person();
        person.setPersonId(personId);
        person.setPersonName(personName);
        person.setAge(age);
        person.setGender(gender);
        
        if (personId == null) {
            personService.addPerson(person);
        } else {
            personService.updatePerson(person);
        }
        
        return "personList?faces-redirect=true";
    } catch (RuntimeException e) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "エラー", "登録処理に失敗しました: " + e.getMessage()));
        return null; // 同じページを再表示
    }
}
```

* back(): void
  * 戻り値: void
  * 説明: 入力画面に戻る（JavaScriptで実装）
  * 処理: このメソッドは実際には呼ばれない（history.back()をJavaScriptで実行）

```java
public void back() {
    // このメソッドは実際には呼ばれない
    // history.back()をJavaScriptで実行するため
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

import pro.kensait.jsf.person.entity.Person;
import pro.kensait.jsf.person.service.PersonService;

@Named("personConfirmBean")
@ViewScoped
public class PersonConfirmBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private PersonService personService;

    @Inject
    private PersonInputBean personInputBean;

    private Integer personId;
    private String personName;
    private Integer age;
    private String gender;

    @PostConstruct
    public void init() {
        this.personId = personInputBean.getPersonId();
        this.personName = personInputBean.getPersonName();
        this.age = personInputBean.getAge();
        this.gender = personInputBean.getGender();
    }

    public String save() {
        try {
            Person person = new Person();
            person.setPersonId(personId);
            person.setPersonName(personName);
            person.setAge(age);
            person.setGender(gender);
            
            if (personId == null) {
                personService.addPerson(person);
            } else {
                personService.updatePerson(person);
            }
            
            return "personList?faces-redirect=true";
        } catch (RuntimeException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "エラー", "登録処理に失敗しました: " + e.getMessage()));
            return null;
        }
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

* addPerson(Person person): void
  * 引数: person（新しいPersonオブジェクト）
  * 戻り値: void
  * 説明: 新しいPERSONをデータベースに追加する
  * トランザクション: READ_COMMITTED（@Transactionalにより自動管理）
  * JPA APIメソッド: em.persist(person)

```java
public void addPerson(Person person) {
    em.persist(person);
}
```

* updatePerson(Person person): void
  * 引数: person（更新するPersonオブジェクト）
  * 戻り値: void
  * 説明: 既存のPERSONをデータベースで更新する
  * トランザクション: READ_COMMITTED（@Transactionalにより自動管理）
  * JPA APIメソッド: em.merge(person)

```java
public void updatePerson(Person person) {
    em.merge(person);
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

### 4.2 データ登録

* 新規PERSON追加:

```java
em.persist(person);
```

* 説明:
  * 新しいエンティティをPersistence Contextに追加
  * トランザクションコミット時にINSERTが実行される
  * personIdはデータベースが自動採番

### 4.3 データ更新

* 既存PERSON更新:

```java
em.merge(person);
```

* 説明:
  * 既存のエンティティを更新
  * トランザクションコミット時にUPDATEが実行される

## 5. ビュー設計

### 5.1 personConfirm.xhtml

* ビュー技術: Facelets XHTML
* 名前空間:
  * xmlns:h="jakarta.faces.html"
  * xmlns:f="jakarta.faces.core"

### 5.2 主要コンポーネント

* <h:head>: ページヘッダー

```xml
<h:head>
    <meta charset="UTF-8"/>
    <title>PERSON確認</title>
    <h:outputStylesheet name="css/style.css"/>
</h:head>
```

* <h:body>: ページボディ

```xml
<h:body>
    <h1>PERSON確認</h1>
    
    <h:messages globalOnly="false" showSummary="true" showDetail="false" 
                styleClass="error-messages"/>
    
    <!-- 確認情報表示 -->
    <!-- フォーム -->
</h:body>
```

* 確認情報表示:

```xml
<div class="info-group">
    <label>名前:</label>
    <h:outputText value="#{personConfirmBean.personName}"/>
</div>

<div class="info-group">
    <label>年齢:</label>
    <h:outputText value="#{personConfirmBean.age}"/>
</div>

<div class="info-group">
    <label>性別:</label>
    <h:outputText value="男性" rendered="#{personConfirmBean.gender == 'male'}"/>
    <h:outputText value="女性" rendered="#{personConfirmBean.gender == 'female'}"/>
</div>
```

* フォーム:

```xml
<h:form>
    <h:inputHidden value="#{personConfirmBean.personId}"/>
    <h:inputHidden value="#{personConfirmBean.personName}"/>
    <h:inputHidden value="#{personConfirmBean.age}"/>
    <h:inputHidden value="#{personConfirmBean.gender}"/>
    
    <div class="info-group">
        <h:commandButton value="登録" action="#{personConfirmBean.save}" 
                         styleClass="button"/>
        <h:button value="戻る" onclick="history.back(); return false;" 
                  styleClass="button back"/>
    </div>
</h:form>
```

## 6. 画面遷移設計

### 6.1 遷移先

* personList.xhtml（PERSON一覧画面）
  * 登録ボタンから遷移
  * リダイレクト遷移
  * 戻り値: "personList?faces-redirect=true"

### 6.2 遷移方式

* 登録ボタン: PersonConfirmBean.save()メソッドの戻り値
* 戻るボタン: JavaScriptのhistory.back()

## 7. トランザクション設計

* PersonServiceのaddPerson()、updatePerson()メソッドはトランザクション境界
* @Transactionalアノテーションにより自動管理
* RuntimeExceptionが発生した場合、自動的にロールバック

## 8. エラーハンドリング設計

### 8.1 登録・更新エラー

* PersonService.addPerson()またはupdatePerson()でRuntimeExceptionが発生
* エラーメッセージを表示し、確認画面を再表示
* トランザクションは自動的にロールバック

## 9. 参考資料

* [システム要件定義](../../system/requirements.md)
* [アーキテクチャ設計書](../../system/architecture_design.md)
* [機能設計書](../../system/functional_design.md)
* [データモデル](../../system/data_model.md)
* [SCREEN_003_PersonConfirm画面設計](screen_design.md)
* [SCREEN_003_PersonConfirm振る舞い仕様](behaviors.md)
