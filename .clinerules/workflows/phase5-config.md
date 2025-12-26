# Phase 5: 設定ファイルとクリーンアップ

struts-to-jsf-personプロジェクトの設定ファイルを、StrutsからJSFに移行し、旧ファイルをクリーンアップします。

## 作業概要

このフェーズでは以下を実施します：
1. web.xmlの変更（StrutsからJSFへ）
2. beans.xmlの作成（CDI有効化）
3. persistence.xmlの更新（エンティティクラスパス変更）
4. 旧設定ファイルの削除
5. 旧パッケージディレクトリの削除確認

## 詳細手順

### 1. web.xmlの変更

**対象ファイル：** `projects/java/struts-to-jsf-person/src/main/webapp/WEB-INF/web.xml`

**削除する設定：**
- Struts ActionServletの全定義
- Strutsのservlet-mapping
- Strutsタグライブラリ設定

**追加する設定：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                             https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">
    
    <display-name>struts-to-jsf-person</display-name>
    
    <!-- JSF Servlet -->
    <servlet>
        <servlet-name>Faces Servlet</servlet-name>
        <servlet-class>jakarta.faces.webapp.FacesServlet</servlet-class>
        <load-on-startup>1</load-on-startup>
    </servlet>
    
    <!-- JSF Servlet Mapping -->
    <servlet-mapping>
        <servlet-name>Faces Servlet</servlet-name>
        <url-pattern>/faces/*</url-pattern>
    </servlet-mapping>
    
    <!-- Welcome File -->
    <welcome-file-list>
        <welcome-file>faces/PersonTablePage.xhtml</welcome-file>
    </welcome-file-list>
    
    <!-- DataSource設定（維持） -->
    <resource-ref>
        <res-ref-name>jdbc/HsqldbDS</res-ref-name>
        <res-type>javax.sql.DataSource</res-type>
        <res-auth>Container</res-auth>
    </resource-ref>
</web-app>
```

**重要なポイント：**
- Jakarta EE 10のスキーマに更新
- FacesServletの登録（load-on-startup=1）
- URL pattern: `/faces/*`（明示的なマッピング）
- Welcome fileにPersonTablePage.xhtmlを指定
- DataSource設定は維持（JNDI名: jdbc/HsqldbDS）

### 2. beans.xmlの作成

**新規作成：** `projects/java/struts-to-jsf-person/src/main/webapp/WEB-INF/beans.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                           https://jakarta.ee/xml/ns/jakartaee/beans_4_0.xsd"
       version="4.0"
       bean-discovery-mode="all">
</beans>
```

**役割：**
- CDI（Contexts and Dependency Injection）の有効化
- bean-discovery-mode="all"でアノテーション付きBeanをすべてスキャン

**bean-discovery-modeの選択肢：**
- `all` - すべてのクラスをBeanとして扱う
- `annotated` - アノテーション付きクラスのみ（推奨）
- `none` - CDI無効化

### 3. persistence.xmlの更新

**対象ファイル：** `projects/java/struts-to-jsf-person/src/main/resources/META-INF/persistence.xml`

**変更内容：**
- エンティティクラスのパッケージ名を更新
- `pro.kensait.struts.person.model.Person` → `pro.kensait.jsf.person.Person`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
                                 https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"
             version="3.0">
    
    <persistence-unit name="MyPersistenceUnit" transaction-type="JTA">
        <jta-data-source>jdbc/HsqldbDS</jta-data-source>
        
        <!-- エンティティクラスの指定（更新） -->
        <class>pro.kensait.jsf.person.Person</class>
        
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

### 4. 削除する設定ファイル

**削除対象：**
- `projects/java/struts-to-jsf-person/src/main/resources/META-INF/ejb-jar.xml` - EJB設定
- `projects/java/struts-to-jsf-person/src/main/webapp/WEB-INF/struts-config.xml` - Struts設定
- `projects/java/struts-to-jsf-person/src/main/resources/ApplicationResources.properties` - Strutsメッセージリソース（使用していない場合）

### 5. プロジェクトの最終クリーンアップ

**削除確認項目：**

**Javaクラス（すべて削除済みのはず）：**
- [ ] `action/` パッケージ全体
- [ ] `form/PersonForm.java`
- [ ] `dao/PersonDao.java`
- [ ] `service/PersonService.java`（インターフェース）
- [ ] `service/PersonServiceBean.java`（EJB実装）
- [ ] `model/Person.java`（Strutsパッケージ側）

**ビューファイル（すべて削除済みのはず）：**
- [ ] `index.jsp`
- [ ] `personList.jsp`
- [ ] `personInput.jsp`
- [ ] `personConfirm.jsp`

**設定ファイル：**
- [ ] `struts-config.xml`
- [ ] `ejb-jar.xml`
- [ ] `ApplicationResources.properties`（使用している場合は残す）

**パッケージ構造の確認：**
- 旧パッケージ `pro.kensait.struts.person` 以下がすべて削除されていることを確認
- 新パッケージ `pro.kensait.jsf.person` のみが残っていることを確認

## 実行手順

1. web.xmlを完全に書き換え
2. beans.xmlを新規作成
3. persistence.xmlのエンティティクラスパスを更新
4. 旧設定ファイル（3つ）を削除
5. 旧パッケージディレクトリの削除確認

## 最終的なファイル構造

```
projects/java/struts-to-jsf-person/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── pro/kensait/jsf/person/
│   │   │       ├── Person.java              # @Entity
│   │   │       ├── PersonService.java       # @RequestScoped, @Transactional
│   │   │       ├── PersonTableBean.java     # @Named, @ViewScoped
│   │   │       ├── PersonInputBean.java     # @Named, @ViewScoped
│   │   │       └── PersonUpdateBean.java    # @Named, @ViewScoped
│   │   ├── resources/
│   │   │   └── META-INF/
│   │   │       └── persistence.xml          # JPA設定
│   │   └── webapp/
│   │       ├── PersonTablePage.xhtml        # 一覧画面
│   │       ├── PersonInputPage.xhtml        # 入力画面
│   │       ├── PersonConfirmPage.xhtml      # 確認画面
│   │       ├── css/
│   │       │   └── style.css
│   │       └── WEB-INF/
│   │           ├── web.xml                  # JSF Servlet設定
│   │           └── beans.xml                # CDI設定
│   └── test/
│       └── java/
└── sql/
    └── hsqldb/
        ├── 1_PERSON_DROP.sql
        ├── 2_PERSON_DDL.sql
        └── 3_PERSON_DML.sql
```

## 注意事項

### アプリケーションサーバーの変更
- TomEE 8（Jakarta EE 9） → Payara Server 6（Jakarta EE 10）
- Jakarta EE 10完全対応
- 名前空間が`javax.*`から`jakarta.*`に完全移行

### データソース設定
- JNDI名は変更しない（`jdbc/HsqldbDS`）
- Payara Server側でデータソース登録が必要：
  1. 接続プールの作成
  2. JDBCリソースの作成（JNDI名: jdbc/HsqldbDS）
  3. HSQLDBドライバJARの配置

### デプロイメント
1. WARファイルのビルド（Gradleまたはmaven）
2. Payara Serverへのデプロイ
3. コンテキストルートの確認
4. アプリケーションURLの確認

### 動作確認項目
- [ ] 一覧画面の表示
- [ ] 新規登録機能
- [ ] 編集機能
- [ ] 削除機能
- [ ] トランザクション動作
- [ ] Flash Scopeによる画面間データ受け渡し
- [ ] バリデーション動作
- [ ] CSS適用状況

### ログ確認
- Payara Serverのログ（`payara6/glassfish/domains/domain1/logs/server.log`）
- SQL実行ログ（persistence.xmlの設定による）
- アプリケーションログ

## トラブルシューティング

### よくある問題

**1. CDI Beanが見つからない**
- beans.xmlが正しく配置されているか確認
- @Namedアノテーションが正しく付与されているか確認
- bean-discovery-modeの設定を確認

**2. EntityManagerがnull**
- persistence.xmlが正しく配置されているか確認
- @PersistenceContextの指定が正しいか確認
- DataSourceが正しく設定されているか確認

**3. 画面が表示されない**
- web.xmlのFacesServlet設定を確認
- URLパターン（/faces/*）でアクセスしているか確認
- XHTMLファイルのパスが正しいか確認

**4. トランザクションがコミットされない**
- @Transactionalアノテーションが付与されているか確認
- JTAデータソースが使用されているか確認

## 確認項目

- [ ] web.xmlがJSF用に更新されている
- [ ] beans.xmlが作成されている
- [ ] persistence.xmlのエンティティクラスパスが更新されている
- [ ] 旧設定ファイル3つが削除されている
- [ ] 旧パッケージ（pro.kensait.struts.person）が削除されている
- [ ] アプリケーションが正常にデプロイできる
- [ ] 全機能が正常に動作する
