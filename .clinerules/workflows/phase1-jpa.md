# Phase 1: データモデル層のJPA化

struts-to-jsf-personプロジェクトのデータモデル層を、JDBCからJPAに移行します。

## 作業概要

このフェーズでは以下を実施します：
1. Personクラスのエンティティ化
2. persistence.xmlの作成
3. PersonDaoの削除

## 詳細手順

### 1. Personクラスのエンティティ化

**対象ファイル：** `projects/java/struts-to-jsf-person/src/main/java/pro/kensait/struts/person/model/Person.java`

この段階では、まだStrutsパッケージ内のファイルを修正します（後続フェーズでJSFパッケージに移行）。

**追加するアノテーション：**

**クラスレベル：**
- `@Entity` - JPAエンティティ
- `@Table(name = "PERSON")` - テーブルマッピング

**フィールドレベル：**
- **personId:**
  - `@Id` - 主キー
  - `@GeneratedValue(strategy = GenerationType.IDENTITY)` - 自動採番
  - `@Column(name = "PERSON_ID")`

- **personName:**
  - `@Column(name = "PERSON_NAME", nullable = false, length = 50)`

- **age:**
  - `@Column(name = "AGE", nullable = false)`

- **gender:**
  - `@Column(name = "GENDER", nullable = false, length = 10)`

**追加要素：**
- `implements Serializable` - セッション保存対応
- 引数なしコンストラクタ（JPA必須）

**必要なimport文：**
```java
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import java.io.Serializable;
```

### 2. persistence.xmlの作成

**新規作成：** `projects/java/struts-to-jsf-person/src/main/resources/META-INF/persistence.xml`

**設定内容：**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
                                 https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"
             version="3.0">
    
    <persistence-unit name="MyPersistenceUnit" transaction-type="JTA">
        <jta-data-source>jdbc/HsqldbDS</jta-data-source>
        
        <!-- エンティティクラスの指定 -->
        <class>pro.kensait.struts.person.model.Person</class>
        
        <properties>
            <!-- スキーマ自動生成：none（既存DBスキーマを使用） -->
            <property name="jakarta.persistence.schema-generation.database.action" value="none"/>
            
            <!-- SQL実行ログ出力（開発用） -->
            <property name="eclipselink.logging.level.sql" value="FINE"/>
            <property name="eclipselink.logging.parameters" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

### 3. PersonDaoの削除

**削除対象：** `projects/java/struts-to-jsf-person/src/main/java/pro/kensait/struts/person/dao/PersonDao.java`

**理由：**
- JPAのEntityManagerで置き換えるため不要
- JDBC処理は完全にJPAに移行

## 実行手順

1. まず現在のPersonクラスを読み取り、JPAアノテーションを追加
2. persistence.xmlを新規作成
3. PersonDaoを削除

## 注意事項

- パッケージは現時点ではまだ `pro.kensait.struts.person` を使用
- JSFパッケージへの移行は後続フェーズで実施
- DataSource（JNDI名: `jdbc/HsqldbDS`）の設定は後のフェーズで対応

## 確認項目

- [ ] Personクラスに@Entityアノテーションが付与されている
- [ ] 全フィールドに適切な@Columnアノテーションが設定されている
- [ ] persistence.xmlが正しく作成されている
- [ ] PersonDaoが削除されている
