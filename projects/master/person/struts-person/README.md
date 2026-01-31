# struts-person プロジェクト

## 📖 概要

レガシーなStruts 1.3.10フレームワークを使用した人材管理システムです。
データベースアクセスには旧来型のDAOクラス（データソース使用）、
ビジネスロジックにはステートレスセッションBean（EJB）を採用しています。

**本プロジェクトはソースコードの提供のみを目的としています。**  
アプリケーションサーバーやデータベースのセットアップ・起動手順は含みません。ビルドされたWARを、利用者が任意のJava EE 8対応サーバーにデプロイして利用してください。

## 🛠 ビルド

### 前提条件

- JDK 21以上
- Gradle 8.x以上

### WARのビルド

```bash
./gradlew :struts-person:war
```

成果物は `struts-person/build/libs/struts-person.war` に出力されます。

## 🎯 プロジェクト構成

```
projects/master/person/struts-person/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── dev/
│   │   │       └── berry/
│   │   │           ├── model/          # ビジネスエンティティ
│   │   │           │   └── Person.java
│   │   │           ├── dao/            # データアクセス層
│   │   │           │   └── PersonDao.java
│   │   │           ├── service/        # ビジネスロジック層（EJB）
│   │   │           │   ├── PersonService.java
│   │   │           │   └── PersonServiceBean.java
│   │   │           └── struts/         # プレゼンテーション層
│   │   │               ├── form/
│   │   │               │   └── PersonForm.java
│   │   │               └── action/
│   │   │                   ├── PersonListAction.java
│   │   │                   ├── PersonInputAction.java
│   │   │                   ├── PersonConfirmAction.java
│   │   │                   ├── PersonUpdateAction.java
│   │   │                   └── PersonDeleteAction.java
│   │   ├── resources/
│   │   │   ├── ApplicationResources.properties
│   │   │   └── META-INF/
│   │   │       └── ejb-jar.xml         # EJB設定
│   │   └── webapp/
│   │       ├── css/
│   │       │   └── style.css
│   │       ├── index.jsp
│   │       ├── personList.jsp          # Strutsタグライブラリ使用
│   │       ├── personInput.jsp        # Strutsタグライブラリ使用
│   │       ├── personConfirm.jsp      # Strutsタグライブラリ使用
│   │       └── WEB-INF/
│   │           ├── web.xml
│   │           └── struts-config.xml   # Struts設定
│   └── test/
├── sql/
│   └── hsqldb/                         # SQLスクリプト
└── build/
    └── libs/
        └── struts-person.war
```

## 🔧 使用している技術

- **Java EE 8** (Servlet 4.0, JSP 2.3, EJB 3.2)
- **Apache Struts 1.3.10** (レガシーフレームワーク)
  - Strutsタグライブラリ（`<logic:iterate>`, `<bean:write>`, `<html:form>`等）
- **EJB 3.2** (Stateless Session Bean)
- **JDBC** (データソース経由)
- **HSQLDB 2.7.x**（接続先はデプロイ先のデータソース設定に依存）

## 📝 デプロイ時に必要な設定（参考）

本アプリはJNDIデータソース `jdbc/HsqldbDS` を参照します。Java EE 8対応のアプリケーションサーバーにデプロイする場合、サーバー側で同JNDI名のDataSourceを定義し、接続先DB（例: HSQLDBの `testdb`）を用意してください。

- **JNDI名**: `jdbc/HsqldbDS`
- **想定DB**: HSQLDB `testdb`（ユーザー `SA`、パスワードなし、TCP例: `localhost:9001`）
- テーブル作成用SQLは `sql/hsqldb/` を参照してください。

`WEB-INF/web.xml` の `resource-ref` はすでに設定済みです。

## 🔍 主な機能

1. **PERSON一覧表示** (`/personList.do`)
   - データベースから全PERSON情報を取得して表示
   - Strutsタグ `<logic:iterate>` と `<bean:write>` を使用

2. **PERSON追加** (`/personInput.do` → `/personConfirm.do` → `/personUpdate.do`)
   - 入力画面 → 確認画面 → 登録処理
   - Strutsタグ `<html:form>` と `<html:text>` を使用

3. **PERSON編集** (`/personInput.do?personId=xxx` → `/personConfirm.do` → `/personUpdate.do`)
   - 入力画面（既存データ表示） → 確認画面 → 更新処理

4. **PERSON削除** (`/personDelete.do?personId=xxx`)
   - 指定IDのPERSONを削除

## 📚 技術的な特徴

### Struts 1.3.10の特徴

- **ActionServlet**: フロントコントローラーパターン
- **Action**: ビジネスロジックの呼び出し
- **ActionForm**: フォームデータの保持
- **struts-config.xml**: マッピング設定
- **Strutsタグライブラリ**: JSPでの動的コンテンツ表示
  - `<logic:iterate>`: コレクションのループ処理
  - `<bean:write>`: プロパティ値の出力
  - `<html:form>`: フォーム生成（自動的にActionFormとバインド）
  - `<html:text>`: テキスト入力フィールド

### EJBの利用

- **@Stateless**: ステートレスセッションBean
- **JNDIルックアップ**: EJB取得（Struts ActionではJNDIを使用）
- トランザクション管理（コンテナ管理）

> **Note:** Struts 1.xのActionクラスでは`@EJB`インジェクションが機能しないため、
> JNDIルックアップ（`InitialContext.lookup()`）を使用してEJBを取得します。

### DAOパターン

- **DataSource**: JNDIルックアップによる取得
- **JDBC**: PreparedStatementを使用
- try-with-resources構文によるリソース管理

## 📚 アーキテクチャ

### レイヤー構成

```
JSP View (Struts Tags)
    ↓
Action (Controller)
    ↓
EJB Service (@Stateless)
    ↓
DAO (JDBC + DataSource)
    ↓
Database (HSQLDB 等)
```

### 主要クラス

#### 1. Action (Struts Controller)

Strutsの`Action`クラスがリクエストを受け取り、EJBを呼び出してビジネスロジックを実行。

```java
public class PersonListAction extends Action {
    public ActionForward execute(...) {
        // EJBをJNDIルックアップ
        // ビジネスロジック実行
        // ビューに転送
    }
}
```

#### 2. EJB Service (@Stateless)

ステートレスセッションBeanでビジネスロジックを実装。トランザクション管理はコンテナが担当。

```java
@Stateless
public class PersonServiceBean implements PersonService {
    // ビジネスロジック実装
}
```

#### 3. DAO (Data Access Object)

JDBC + DataSourceでデータベースアクセス。

```java
public class PersonDao {
    // JNDIでDataSourceを取得
    // PreparedStatementでCRUD操作
}
```

#### 4. JSP View (Struts Tags)

Strutsタグライブラリを使用して動的コンテンツを表示。

```jsp
<logic:iterate id="person" name="personList">
    <bean:write name="person" property="name"/>
</logic:iterate>
```

## 📋 Gradleタスク（本プロジェクト）

| タスク | 説明 |
|--------|------|
| `:struts-person:war` | WARファイルをビルド |
| `:struts-person:setupHsqldb` | HSQLDB用のテーブル・データ作成（ローカルでHSQLDBを利用する場合） |

## 📖 参考リンク

- [Apache Struts 1.3.10 Documentation](https://struts.apache.org/struts1eol-announcement.html)
- [Java EE 8 Specification](https://jakarta.ee/specifications/platform/8/)
- [EJB 3.2 Specification](https://jakarta.ee/specifications/enterprise-beans/3.2/)
- [HSQLDB Documentation](http://hsqldb.org/doc/2.0/guide/)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。
