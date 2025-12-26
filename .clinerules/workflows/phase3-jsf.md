# Phase 3: プレゼンテーション層のJSF化

struts-to-jsf-personプロジェクトのプレゼンテーション層を、StrutsからJSFに移行します。

## 作業概要

このフェーズでは以下を実施します：
1. PersonTableBean（一覧画面用）の作成
2. PersonInputBean（入力画面用）の作成
3. PersonUpdateBean（更新処理用）の作成
4. Personエンティティの移動
5. 旧Strutsクラスの削除

## 詳細手順

### 1. PersonTableBean（一覧画面用）の作成

**新規作成：** `projects/java/struts-to-jsf-person/src/main/java/pro/kensait/jsf/person/PersonTableBean.java`

**役割：** `PersonListAction`と`PersonDeleteAction`の統合置き換え

**クラス設計：**
- `@Named("personTable")` - EL式でのアクセス名
- `@ViewScoped` - 画面表示中のスコープ
- `implements Serializable` - ViewScoped必須要件

**プロパティ：**
- `List<Person> personList` - 一覧表示データ
- getter/setter必須

**依存性注入：**
- `@Inject PersonService personService`

**主要メソッド：**
```java
@PostConstruct
void postConstruct() {
    // 初期化時にデータ取得
    personList = personService.findAll();
}

String removePerson(Integer personId) {
    // 削除処理
    personService.delete(personId);
    // リストを再取得
    personList = personService.findAll();
    // 自画面リロード（nullを返す）
    return null;
}

String editPerson(Integer personId) {
    // 編集対象データを取得
    Person person = personService.findById(personId);
    // Flash Scopeに設定
    FacesContext.getCurrentInstance()
        .getExternalContext()
        .getFlash()
        .put("person", person);
    // 入力画面へ遷移
    return "PersonInputPage?faces-redirect=true";
}
```

**必要なimport文：**
```java
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;
```

### 2. PersonInputBean（入力画面用）の作成

**新規作成：** `projects/java/struts-to-jsf-person/src/main/java/pro/kensait/jsf/person/PersonInputBean.java`

**役割：** `PersonInputAction`と`PersonForm`の統合置き換え

**クラス設計：**
- `@Named("personInput")`
- `@ViewScoped`
- `implements Serializable`

**プロパティ（画面入力項目）：**
- `Integer personId` - 編集時のID
- `String personName` - 名前
- `Integer age` - 年齢（型をIntegerに変更）
- `String gender` - 性別
- すべてgetter/setter必須

**依存性注入：**
- `@Inject PersonService personService`

**主要メソッド：**
```java
@PostConstruct
void postConstruct() {
    // Flash Scopeから編集データ取得
    Person person = (Person) FacesContext.getCurrentInstance()
        .getExternalContext()
        .getFlash()
        .get("person");
    
    if (person != null) {
        // 編集モード：フィールドに設定
        this.personId = person.getPersonId();
        this.personName = person.getPersonName();
        this.age = person.getAge();
        this.gender = person.getGender();
    }
    // 新規モード：何もしない（フィールドはnullのまま）
}

String confirm() {
    // Personオブジェクト生成
    Person person = new Person();
    person.setPersonId(this.personId);
    person.setPersonName(this.personName);
    person.setAge(this.age);
    person.setGender(this.gender);
    
    // Flash Scopeに設定
    FacesContext.getCurrentInstance()
        .getExternalContext()
        .getFlash()
        .put("person", person);
    
    // 確認画面へ遷移
    return "PersonConfirmPage?faces-redirect=true";
}

String cancel() {
    // 一覧画面へ戻る
    return "PersonTablePage?faces-redirect=true";
}
```

### 3. PersonUpdateBean（更新処理用）の作成

**新規作成：** `projects/java/struts-to-jsf-person/src/main/java/pro/kensait/jsf/person/PersonUpdateBean.java`

**役割：** `PersonUpdateAction`と`PersonConfirmAction`の統合置き換え

**クラス設計：**
- `@Named("personUpdate")`
- `@ViewScoped`
- `implements Serializable`

**プロパティ：**
- `Person person` - 登録/更新対象データ
- getter/setter必須

**依存性注入：**
- `@Inject PersonService personService`

**主要メソッド：**
```java
@PostConstruct
void postConstruct() {
    // Flash Scopeからデータ取得
    person = (Person) FacesContext.getCurrentInstance()
        .getExternalContext()
        .getFlash()
        .get("person");
}

String register() {
    if (person.getPersonId() == null) {
        // 新規登録
        personService.insert(person);
    } else {
        // 更新
        personService.update(person);
    }
    
    // 一覧画面へリダイレクト
    return "PersonTablePage?faces-redirect=true";
}

String back() {
    // Flash Scopeに再設定（入力画面で復元するため）
    FacesContext.getCurrentInstance()
        .getExternalContext()
        .getFlash()
        .put("person", person);
    
    // 入力画面へ戻る
    return "PersonInputPage?faces-redirect=true";
}
```

### 4. Personエンティティの移動

**移動元：** `projects/java/struts-to-jsf-person/src/main/java/pro/kensait/struts/person/model/Person.java`
**移動先：** `projects/java/struts-to-jsf-person/src/main/java/pro/kensait/jsf/person/Person.java`

**重要：**
- Phase 1で追加したJPAアノテーションを保持したまま移動
- persistence.xmlのエンティティクラス指定も更新が必要（Phase 5で実施）

### 5. 削除するクラス

**削除対象：**
- `projects/java/struts-to-jsf-person/src/main/java/pro/kensait/struts/person/action/PersonListAction.java`
- `projects/java/struts-to-jsf-person/src/main/java/pro/kensait/struts/person/action/PersonInputAction.java`
- `projects/java/struts-to-jsf-person/src/main/java/pro/kensait/struts/person/action/PersonConfirmAction.java`
- `projects/java/struts-to-jsf-person/src/main/java/pro/kensait/struts/person/action/PersonUpdateAction.java`
- `projects/java/struts-to-jsf-person/src/main/java/pro/kensait/struts/person/action/PersonDeleteAction.java`
- `projects/java/struts-to-jsf-person/src/main/java/pro/kensait/struts/person/form/PersonForm.java`

## 実行手順

1. PersonTableBean.javaを作成
2. PersonInputBean.javaを作成
3. PersonUpdateBean.javaを作成
4. Person.javaをJSFパッケージに移動
5. 旧Strutsクラス（Action、Form）を削除

## 注意事項

### スコープ選択
- `@ViewScoped` - 画面単位でのデータ保持、Ajax対応
- 画面表示中はBeanインスタンスが保持される
- ページ遷移時には破棄される

### Flash Scope
- 画面間でのデータ受け渡しに使用
- リダイレクト後も一度だけ有効
- `getFlash().put(key, value)`と`getFlash().get(key)`

### 画面遷移
- アクションメソッドは遷移先ビューID（拡張子なし）を返す
- リダイレクトは`"viewId?faces-redirect=true"`
- nullを返すと自画面リロード

### 型変換
- ActionFormのString型 → 適切な型（Integer等）
- JSFが自動で型変換処理を実施
- 入力値の検証も自動実施

### 依存性注入
- JNDIルックアップ不要
- `@Inject`でServiceを注入
- CDIコンテナが依存性を解決

### Serializableの実装
- ViewScopedではSerializable実装が必須
- セッションへのシリアライズに対応

## 確認項目

- [ ] PersonTableBean.javaが作成されている
- [ ] PersonInputBean.javaが作成されている
- [ ] PersonUpdateBean.javaが作成されている
- [ ] 各BeanにCDIアノテーションが付与されている
- [ ] Person.javaがJSFパッケージに移動している
- [ ] 旧Strutsクラス6つが削除されている
