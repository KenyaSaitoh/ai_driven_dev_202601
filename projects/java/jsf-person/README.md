# jsf-person プロジェクト

## 📖 概要

JSFとJPA (Java Persistence API) を組み合わせたデータベースCRUD操作のサンプルです。
エンティティ、永続化コンテキスト、トランザクション管理を学習できます。

## 🚀 セットアップとコマンド実行ガイド

### 前提条件

- JDK 21以上
- Gradle 8.x以上
- Payara Server 6（プロジェクトルートの`payara6/`に配置）
- HSQLDB（プロジェクトルートの`hsqldb/`に配置）

> **Note:** ① と ② の手順は、ルートの`README.md`を参照してください。

### ③ 依存関係の確認

このプロジェクトを開始する前に、以下が起動していることを確認してください：

- **① HSQLDBサーバー** （`./gradlew startHsqldb`）
- **② Payara Server** （`./gradlew startPayara`）

### ④ プロジェクトを開始するときに1回だけ実行

```bash
# 1. データベーステーブルとデータを作成
./gradlew :projects:java:jsf-person:setupHsqldb

# 2. プロジェクトをビルド
./gradlew :projects:java:jsf-person:war

# 3. プロジェクトをデプロイ
./gradlew :projects:java:jsf-person:deploy
```

### ⑤ プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# プロジェクトをアンデプロイ
./gradlew :projects:java:jsf-person:undeploy
```

### ⑥ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
./gradlew :projects:java:jsf-person:war
./gradlew :projects:java:jsf-person:deploy
```

## 📍 アクセスURL

デプロイ後、以下のURLにアクセス：

- **Person一覧**: http://localhost:8080/jsf-person/faces/PersonTablePage.xhtml

## 🎯 プロジェクト構成

```
projects/jsf-person/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── bean/              # マネージドBean
│   │   │   ├── entity/            # JPAエンティティ
│   │   │   └── service/           # ビジネスロジック
│   │   ├── resources/
│   │   │   └── META-INF/
│   │   │       └── persistence.xml  # JPA設定
│   │   └── webapp/
│   │       ├── css/
│   │       │   └── style.css
│   │       ├── *.xhtml
│   │       └── WEB-INF/
│   │           ├── web.xml
│   │           └── beans.xml
│   └── test/
├── sql/
│   └── hsqldb/                     # SQLスクリプト
└── build/
    └── libs/
        └── jsf-person.war
```

## 🔧 使用している技術

- **Jakarta EE 10**
- **Payara Server 6**
- **Jakarta Faces (JSF) 4.0**
- **Jakarta Persistence (JPA) 3.1**
- **Jakarta Transactions (JTA)**
- **Jakarta CDI 4.0**
- **HSQLDB 2.7.x**

## 📝 データソース設定について

このプロジェクトはルートの`build.gradle`で定義されたタスクを使用してデータソースを作成します。

### 設定内容

- **JNDI名**: `jdbc/HsqldbDS`
- **データベース**: `testdb`
- **ユーザー**: `SA`
- **パスワード**: （空文字）
- **TCPサーバー**: `localhost:9001`

データソースはPayara Serverのドメイン設定に登録されます。

### ⚠️ 注意事項

- HSQLDB Databaseサーバーが起動している必要があります
- データソース作成はPayara Server起動前に実行してください
- 初回のみ実行が必要です（2回目以降は不要）

## 🛑 アプリケーションを停止する

### アプリケーションのアンデプロイ

```bash
./gradlew :projects:java:jsf-person:undeploy
```

### Payara Server全体を停止

```bash
./gradlew stopPayara
```

### HSQLDBサーバーを停止

```bash
./gradlew stopHsqldb
```

## 🔍 ログ監視

別のターミナルでログをリアルタイム監視：

```bash
tail -f -n 50 payara6/glassfish/domains/domain1/logs/server.log
```

> **Note**: Windowsでは**Git Bash**を使用してください。

## 📚 アーキテクチャ

### レイヤー構成

```
JSF View (Facelets XHTML)
    ↓
JSF Managed Bean (@Named, @ViewScoped)
    ↓
CDI Service (@ApplicationScoped, @Transactional)
    ↓
JPA Entity (@Entity)
    ↓
Database (HSQLDB)
```

### 主要クラス

#### 1. PersonBean.java (JSF Managed Bean)

`@Named`と`@ViewScoped`を使用して、画面とビジネスロジックを仲介。

#### 2. PersonService.java (CDI Bean)

`@ApplicationScoped`と`@Transactional`でトランザクション管理を実現。

#### 3. Person.java (JPA Entity)

`@Entity`でデータベーステーブルとマッピング。`EntityManager`による永続化。

## 📖 参考リンク

- [Jakarta EE 10 Platform](https://jakarta.ee/specifications/platform/10/)
- [Jakarta Server Faces 4.0](https://jakarta.ee/specifications/faces/4.0/)
- [Jakarta Persistence (JPA) 3.1](https://jakarta.ee/specifications/persistence/3.1/)
- [Hibernate ORM Documentation](https://hibernate.org/orm/documentation/6.4/)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。
