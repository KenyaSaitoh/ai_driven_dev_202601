# 機能設計書 - SCREEN_001_PersonList

## 1. 概要

PERSON一覧画面（SCREEN_001_PersonList）の機能設計を定義する。

## 2. Managed Bean設計

### 2.1 PersonListBean

* パッケージ: pro.kensait.jsf.person.bean
* クラス名: PersonListBean
* アノテーション:
  * @Named("personListBean")
  * @ViewScoped
  * implements Serializable

#### 2.1.1 フィールド

* personService: PersonService
  * 型: PersonService
  * アノテーション: @Inject
  * 説明: ビジネスロジック層のPersonServiceを注入

* personList: List<Person>
  * 型: List<Person>
  * 説明: 画面に表示するPERSONリスト
  * 初期値: null（init()メソッドで設定）

#### 2.1.2 メソッド

* init(): void
  * アノテーション: @PostConstruct
  * 説明: 画面初期表示時に自動的に実行される
  * 処理:
    1. personService.getAllPersons()を呼び出して全PERSONを取得
    2. 取得したList<Person>をpersonListフィールドに設定
  * 例外処理:
    * RuntimeExceptionが発生した場合、エラーメッセージを表示

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

* getPersonList(): List<Person>
  * 戻り値: List<Person>
  * 説明: personListフィールドを返す
  * ビューからのアクセス: #{personListBean.personList}

```java
public List<Person> getPersonList() {
    return personList;
}
```

* deletePerson(Integer personId): String
  * 引数: personId（削除対象のPERSON_ID）
  * 戻り値: String（画面遷移先、nullの場合は同じページをリロード）
  * 説明: 指定されたPERSONを削除し、リストを再取得する
  * 処理:
    1. personService.deletePerson(personId)を呼び出して削除
    2. 削除成功後、init()メソッドを再度呼び出してリストを更新
    3. nullを返して同じページをリロード
  * 例外処理:
    * RuntimeExceptionが発生した場合、エラーメッセージを表示し、nullを返す

```java
public String deletePerson(Integer personId) {
    try {
        personService.deletePerson(personId);
        init(); // リストを再取得
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
            "成功", "PERSONを削除しました"));
        return null; // 同じページをリロード
    } catch (RuntimeException e) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "エラー", "削除処理に失敗しました: " + e.getMessage()));
        return null;
    }
}
```

#### 2.1.3 完全なクラス実装例

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

## 3. Service設計

### 3.1 PersonService

* パッケージ: pro.kensait.jsf.person.service
* クラス名: PersonService
* アノテーション:
  * @RequestScoped
  * @Transactional

#### 3.1.1 使用するメソッド

* getAllPersons(): List<Person>
  * 戻り値: List<Person>
  * 説明: 全PERSONを取得する
  * トランザクション: READ_COMMITTED
  * JPQLクエリ: "SELECT p FROM Person p ORDER BY p.personId"

```java
public List<Person> getAllPersons() {
    return em.createQuery("SELECT p FROM Person p ORDER BY p.personId", Person.class)
             .getResultList();
}
```

* deletePerson(Integer personId): void
  * 引数: personId（削除対象のPERSON_ID）
  * 戻り値: void
  * 説明: 指定されたPERSONを削除する
  * トランザクション: READ_COMMITTED（@Transactionalにより自動管理）
  * 処理:
    1. em.find(Person.class, personId)で削除対象を取得
    2. 見つかった場合はem.remove(person)で削除
    3. 見つからなかった場合は何もしない

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

## 4. データアクセス設計

### 4.1 EntityManager使用

* Persistence Context: personPU
* EntityManagerのインジェクション:

```java
@PersistenceContext
private EntityManager em;
```

### 4.2 JPQLクエリ

* 全PERSON取得クエリ:

```sql
SELECT p FROM Person p ORDER BY p.personId
```

* 説明:
  * Person エンティティの全レコードを取得
  * personIdの昇順でソート
  * 結果: List<Person>

### 4.3 削除処理

* 削除対象の取得:

```java
Person person = em.find(Person.class, personId);
```

* 削除実行:

```java
em.remove(person);
```

* トランザクションコミット時にDELETEが実行される

## 5. ビュー設計

### 5.1 personList.xhtml

* ビュー技術: Facelets XHTML
* 名前空間:
  * xmlns:h="jakarta.faces.html"
  * xmlns:f="jakarta.faces.core"
  * xmlns:ui="jakarta.faces.facelets"

### 5.2 主要コンポーネント

* <h:head>: ページヘッダー

```xml
<h:head>
    <meta charset="UTF-8"/>
    <title>PERSON一覧</title>
    <h:outputStylesheet name="css/style.css"/>
</h:head>
```

* <h:body>: ページボディ

```xml
<h:body>
    <h1>PERSON一覧</h1>
    
    <h:messages globalOnly="false" showSummary="true" showDetail="false" 
                styleClass="error-messages"/>
    
    <!-- 新規追加ボタン -->
    <!-- PERSONリストテーブル -->
</h:body>
```

* 新規追加ボタン:

```xml
<h:link outcome="personInput" value="新規追加" styleClass="button-link add"/>
```

* PERSONリストテーブル:

```xml
<h:form>
    <h:dataTable value="#{personListBean.personList}" var="person" styleClass="person-table">
        <h:column>
            <f:facet name="header">ID</f:facet>
            #{person.personId}
        </h:column>
        
        <h:column>
            <f:facet name="header">名前</f:facet>
            #{person.personName}
        </h:column>
        
        <h:column>
            <f:facet name="header">年齢</f:facet>
            #{person.age}
        </h:column>
        
        <h:column>
            <f:facet name="header">性別</f:facet>
            <h:outputText value="男性" rendered="#{person.gender == 'male'}"/>
            <h:outputText value="女性" rendered="#{person.gender == 'female'}"/>
        </h:column>
        
        <h:column>
            <f:facet name="header">操作</f:facet>
            <h:link outcome="personInput" value="編集" styleClass="button-link">
                <f:param name="personId" value="#{person.personId}"/>
            </h:link>
            <h:commandButton value="削除" 
                             action="#{personListBean.deletePerson(person.personId)}"
                             onclick="return confirm('削除してもよろしいですか？');"
                             styleClass="button-link delete">
                <f:ajax execute="@form" render="@all"/>
            </h:commandButton>
        </h:column>
    </h:dataTable>
</h:form>
```

## 6. 画面遷移設計

### 6.1 遷移先

* personInput.xhtml（PERSON入力画面）
  * 新規追加ボタンから遷移: personIdなし
  * 編集ボタンから遷移: personIdを指定

### 6.2 遷移方式

* 新規追加ボタン: <h:link>による通常遷移
* 編集ボタン: <h:link>による通常遷移（personIdをURLパラメータで渡す）
* 削除ボタン: 同じページをリロード（nullを返す）

## 7. トランザクション設計

* PersonServiceのdeletePerson()メソッドはトランザクション境界
* @Transactionalアノテーションにより自動管理
* RuntimeExceptionが発生した場合、自動的にロールバック

## 8. エラーハンドリング設計

### 8.1 エラーメッセージ表示

* FacesContext.addMessage()を使用
* <h:messages>コンポーネントで表示

### 8.2 エラーケース

* データ取得エラー:
  * PersonService.getAllPersons()でRuntimeExceptionが発生
  * エラーメッセージを表示し、空のリストを表示

* 削除エラー:
  * PersonService.deletePerson()でRuntimeExceptionが発生
  * エラーメッセージを表示し、リストは変更されない

## 9. パフォーマンス設計

### 9.1 データ取得最適化

* 全PERSONを一度に取得（1回のクエリ）
* ページネーションなし（将来的に追加の可能性）

### 9.2 Ajax部分更新

* 削除ボタンクリック時にAjaxで部分更新
* <f:ajax execute="@form" render="@all"/>

## 10. 参考資料

* [システム要件定義](../../system/requirements.md)
* [アーキテクチャ設計書](../../system/architecture_design.md)
* [機能設計書](../../system/functional_design.md)
* [データモデル](../../system/data_model.md)
* [SCREEN_001_PersonList画面設計](screen_design.md)
* [SCREEN_001_PersonList振る舞い仕様](behaviors.md)
