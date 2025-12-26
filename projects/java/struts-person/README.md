# struts-person プロジェクト

## 📖 概要

レガシーなStruts 1.3.10フレームワークを使用した人材管理システムです。
データベースアクセスには旧来型のDAOクラス（データソース使用）、
ビジネスロジックにはステートレスセッションBean（EJB）を採用しています。

## 🚀 セットアップとコマンド実行ガイド

### 前提条件

- JDK 21以上
- Gradle 8.x以上
- Apache TomEE 8（プロジェクトルートの`tomee8/`に配置）
- HSQLDB（プロジェクトルートの`hsqldb/`に配置）

> **Note:** このプロジェクトはTomEE 8を使用します（Payaraではありません）。

### ① TomEE 8の初期設定（初回のみ・研修開催時に実行）

```bash
# 1. TomEE 8のserver.xmlを初期化（クリーンな状態にリセット）
./gradlew :projects:java:struts-person:initTomee8Config

# 2. TomEE 8のデータソース設定（HSQLDB接続のための重要な設定）
./gradlew :projects:java:struts-person:configureTomee8DataSource
```

### ② HSQLDBサーバーの起動

```bash
# HSQLDBサーバーを起動（バックグラウンド）
./gradlew startHsqldb
```

### ③ 依存関係の確認

このプロジェクトを開始する前に、以下が起動していることを確認してください：

- **HSQLDBサーバー** （`./gradlew startHsqldb`）
- **TomEE 8サーバー** （`./gradlew :projects:java:struts-person:startTomee8`）

> **Note:** TomEE 8は`startTomee8`（フォアグラウンド、Ctrl+Cで停止）または`startTomee8Background`（バックグラウンド）で起動できます。
> フォアグラウンドモードはログがターミナルに直接表示されるため、デバッグに便利です。

### ④ プロジェクトを開始するときに実行

```bash
# 1. HSQLDBサーバーを起動（まだ起動していない場合）
./gradlew startHsqldb

# 2. データベーステーブルとデータを作成（初回のみ）
./gradlew :projects:java:struts-person:setupHsqldb

# 3. TomEE 8を起動（フォアグラウンド - Ctrl+Cで停止可能）
./gradlew :projects:java:struts-person:startTomee8

# 4. プロジェクトをビルドしてデプロイ（別のターミナルで実行）
./gradlew :projects:java:struts-person:deployToTomee8
```

> **Note:** `deployToTomee8`タスクは自動的に`war`タスクを実行するため、WARファイルのビルドとデプロイを1つのコマンドで実行できます。

### ⑤ プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# プロジェクトをアンデプロイ
./gradlew :projects:java:struts-person:undeployFromTomee8
```

### ⑥ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
./gradlew :projects:java:struts-person:deployToTomee8
```

## 📍 アクセスURL

デプロイ後、以下のURLにアクセス：

- **トップページ**: http://localhost:8080/struts-person/

## 🎯 プロジェクト構成

```
projects/struts-person/
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
│   │       ├── personInput.jsp         # Strutsタグライブラリ使用
│   │       ├── personConfirm.jsp       # Strutsタグライブラリ使用
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
- **Apache TomEE 8.0.x Plume**
- **Apache Struts 1.3.10** (レガシーフレームワーク)
  - Strutsタグライブラリ（`<logic:iterate>`, `<bean:write>`, `<html:form>`等）
- **EJB 3.2** (Stateless Session Bean)
- **JDBC** (旧来型データソースアクセス)
- **HSQLDB 2.7.x**

## 📝 データソース設定について

TomEE 8でHSQLDBデータソースを使用するには、**2つの設定**が必要です。

### 1. TomEE 8側の設定（`tomee8/conf/tomee.xml`）

データソースのリソース定義を追加します。これは`configureTomee8DataSource`タスクで自動設定されます：

```xml
<Resource id="HsqldbDS" type="DataSource">
  JdbcDriver org.hsqldb.jdbc.JDBCDriver
  JdbcUrl jdbc:hsqldb:hsql://localhost:9001/testdb
  UserName SA
  Password 
  JtaManaged true
</Resource>
```

### 2. アプリケーション側の設定（`WEB-INF/web.xml`）

データソースの参照を定義します（既に設定済み）：

```xml
<resource-ref>
  <res-ref-name>jdbc/HsqldbDS</res-ref-name>
  <res-type>javax.sql.DataSource</res-type>
  <res-auth>Container</res-auth>
</resource-ref>
```

### データソース接続情報

- **JNDI名**: `jdbc/HsqldbDS`
- **データベース**: `testdb`
- **ユーザー**: `SA`
- **パスワード**: （空文字）
- **TCPサーバー**: `localhost:9001`

### ⚠️ 重要な注意事項

1. **初回セットアップ時**: 必ず`configureTomee8DataSource`タスクを実行してください
2. **起動順序**: HSQLDBサーバー → TomEE 8 の順で起動してください
3. **エラー時**: `java.sql.SQLSyntaxErrorException: user lacks privilege or object not found: PERSON`が出る場合は、`setupHsqldb`タスクでテーブルを作成してください

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

## 🛑 アプリケーションを停止する

### アプリケーションのアンデプロイ

```bash
./gradlew :projects:java:struts-person:undeployFromTomee8
```

### TomEE 8を停止

```bash
./gradlew :projects:java:struts-person:stopTomee8
```

### HSQLDBサーバーを停止

```bash
./gradlew stopHsqldb
```

## 🔍 ログ監視

### フォアグラウンドモードの場合

`./gradlew :projects:java:struts-person:startTomee8`で起動した場合、ログは自動的にターミナルに表示されます。
Ctrl+Cでサーバーを停止できます。

### バックグラウンドモードの場合

`./gradlew :projects:java:struts-person:startTomee8Background`で起動した場合は、別のターミナルでログを監視できます：

```bash
tail -f -n 50 tomee8/logs/catalina.out
```

> **Note**: Windowsでは**Git Bash**を使用してください。

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
Database (HSQLDB)
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

## 📋 Gradleタスク一覧

### TomEE 8関連

| タスク | 説明 |
|--------|------|
| `:projects:java:struts-person:initTomee8Config` | server.xmlを初期状態にリセット（研修開催時に実行） |
| `:projects:java:struts-person:configureTomee8Ports` | ポートを8080に設定（初回のみ） |
| `:projects:java:struts-person:configureTomee8DataSource` | HSQLDBデータソースを設定（初回のみ・重要） |
| `:projects:java:struts-person:startTomee8` | TomEE 8を起動（フォアグラウンド、Ctrl+Cで停止） |
| `:projects:java:struts-person:startTomee8Background` | TomEE 8をバックグラウンドで起動 |
| `:projects:java:struts-person:stopTomee8` | TomEE 8を停止（バックグラウンド起動時） |
| `:projects:java:struts-person:restartTomee8` | TomEE 8を再起動 |
| `:projects:java:struts-person:deployToTomee8` | アプリケーションをデプロイ（WARビルド含む） |
| `:projects:java:struts-person:undeployFromTomee8` | アプリケーションをアンデプロイ |

### プロジェクト関連

| タスク | 説明 |
|--------|------|
| `:projects:java:struts-person:war` | WARファイルをビルド |
| `:projects:java:struts-person:setupHsqldb` | データベース初期化 |

### HSQLDB関連

| タスク | 説明 |
|--------|------|
| `startHsqldb` | HSQLDBサーバーを起動 |
| `stopHsqldb` | HSQLDBサーバーを停止 |

## 📖 参考リンク

- [Apache Struts 1.3.10 Documentation](https://struts.apache.org/struts1eol-announcement.html)
- [Apache TomEE 8 Documentation](https://tomee.apache.org/tomee-8.0/)
- [Java EE 8 Specification](https://jakarta.ee/specifications/platform/8/)
- [EJB 3.2 Specification](https://jakarta.ee/specifications/enterprise-beans/3.2/)
- [HSQLDB Documentation](http://hsqldb.org/doc/2.0/guide/)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。
