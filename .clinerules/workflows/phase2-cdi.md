# Phase 2: ビジネスロジック層のCDI化

struts-to-jsf-personプロジェクトのビジネスロジック層を、EJBからCDIに移行します。

## 作業概要

このフェーズでは以下を実施します：
1. PersonServiceの統合とCDI化
2. データアクセス方式をJDBCからJPAに変更
3. 旧EJBクラスの削除

## 詳細手順

### 1. PersonServiceの統合とCDI化

**新規作成：** `projects/java/struts-to-jsf-person/src/main/java/pro/kensait/jsf/person/PersonService.java`

**重要な構造変更：**
- インターフェースと実装クラスを統合
- JSFパッケージ（`pro.kensait.jsf.person`）に配置

**追加するアノテーション：**
- `@RequestScoped` - リクエストスコープのCDI Bean
- `@Transactional(TxType.REQUIRED)` - トランザクション管理
- `@PersistenceContext(unitName = "MyPersistenceUnit")` - EntityManager注入

**削除するアノテーション：**
- `@Stateless`（EJB関連）

**必要なimport文：**
```java
import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
```

### 2. データアクセス方式の変更

**JDBC処理をJPAに置き換え：**

**全件取得（findAll）:**
```java
public List<Person> findAll() {
    String jpql = "SELECT p FROM Person p";
    TypedQuery<Person> query = entityManager.createQuery(jpql, Person.class);
    return query.getResultList();
}
```

**主キー検索（findById）:**
```java
public Person findById(Integer personId) {
    return entityManager.find(Person.class, personId);
}
```

**登録（insert）:**
```java
public void insert(Person person) {
    entityManager.persist(person);
    // IDは自動採番される
}
```

**更新（update）:**
```java
public void update(Person person) {
    entityManager.merge(person);
}
```

**削除（delete）:**
```java
public void delete(Integer personId) {
    Person person = entityManager.find(Person.class, personId);
    if (person != null) {
        entityManager.remove(person);
    }
}
```

### 3. 例外処理の変更

**変更前：**
- SQLExceptionのcatch処理
- try-with-resources構文

**変更後：**
- PersistenceException（RuntimeException）はそのままスロー
- ビジネス例外への変換は任意

### 4. 削除するクラス

**削除対象：**
- `projects/java/struts-to-jsf-person/src/main/java/pro/kensait/struts/person/service/PersonService.java`（インターフェース）
- `projects/java/struts-to-jsf-person/src/main/java/pro/kensait/struts/person/service/PersonServiceBean.java`（EJB実装）

## 実行手順

1. 現在のPersonServiceBeanの実装を読み取る
2. 新しいJSFパッケージにPersonService.javaを作成
3. JDBCコードをJPAに置き換え
4. 旧EJBクラス（インターフェースと実装）を削除

## 注意事項

### スコープ選択
- `@RequestScoped`: リクエストごとにインスタンス生成（軽量）
- リクエスト間でステートを保持する必要がないため適切

### トランザクション管理
- `@Transactional`はServiceクラスまたはメソッドに付与
- JTAによるコンテナ管理トランザクション
- 各メソッドがトランザクション境界となる

### 依存性注入
- バッキングBeanから`@Inject`でServiceを注入
- JNDIルックアップは不要

### EntityManagerの管理
- コンテナが自動管理（close不要）
- トランザクション境界内で有効
- `@PersistenceContext`で自動注入

### パッケージ構造
- 新パッケージ: `pro.kensait.jsf.person`
- Personエンティティも後続フェーズでこのパッケージに移動予定

## 確認項目

- [ ] PersonService.javaがJSFパッケージに作成されている
- [ ] CDIアノテーション（@RequestScoped, @Transactional）が付与されている
- [ ] EntityManagerが@PersistenceContextで注入されている
- [ ] 全メソッドがJPAで実装されている
- [ ] 旧EJBクラス2つが削除されている
