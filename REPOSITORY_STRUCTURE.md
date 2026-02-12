# リポジトリ構成説明書

## 📁 概要

このリポジトリは、Jakarta EE 10 と React を使用したフルスタック Web アプリケーションの学習・開発プロジェクト集です。AI 駆動開発と仕様駆動開発（SDD）の実践を目的としています。

---

## 🌲 ディレクトリツリー構造（概観）

```
ai_driven_dev_202601/
│
├── 📂 projects/                                    # アプリケーションプロジェクト（3カテゴリ）
│   │
│   ├── 📂 master/                                  # 完成版（模範解答・リファレンス実装）
│   │   ├── bookstore/                              # 書店ドメイン（フルスタック構成）
│   │   │   ├── berry-books-api/                    # REST API: 注文管理（Jakarta EE）
│   │   │   │   ├── src/main/java/.../             # JAX-RS, CDI, JPA実装
│   │   │   │   ├── src/main/resources/            # persistence.xml, beans.xml
│   │   │   │   ├── src/main/webapp/               # web.xml, 静的リソース
│   │   │   │   ├── src/test/java/                 # JUnit, ArchUnitテスト
│   │   │   │   ├── sql/hsqldb/                    # DB初期化SQL
│   │   │   │   └── build.gradle
│   │   │   │
│   │   │   ├── berry-books-spa/                    # SPA: 注文管理（React+TypeScript）
│   │   │   │   ├── src/
│   │   │   │   │   ├── components/                # 再利用可能なUIコンポーネント
│   │   │   │   │   ├── pages/                     # ページコンポーネント
│   │   │   │   │   ├── services/                  # API通信サービス
│   │   │   │   │   ├── contexts/                  # React Context（状態管理）
│   │   │   │   │   ├── types/                     # TypeScript型定義
│   │   │   │   │   └── styles/                    # CSS/Tailwind
│   │   │   │   ├── tests/                         # Playwrightテスト
│   │   │   │   ├── vite.config.ts
│   │   │   │   ├── package.json
│   │   │   │   └── tailwind.config.js
│   │   │   │
│   │   │   ├── back-office-api/                    # REST API: 書籍・在庫管理（Jakarta EE）
│   │   │   ├── back-office-spa/                    # SPA: 書籍管理（React+TypeScript）
│   │   │   ├── customer-hub-api/                   # REST API: 顧客管理（Jakarta EE）
│   │   │   ├── customer-hub-spa/                   # SPA: 顧客管理（React+TypeScript）
│   │   │   ├── customer-hub-swing/                 # Desktop: 顧客管理（Java Swing）
│   │   │   │   └── src/main/java/.../ui/          # SwingコンポーネントUI
│   │   │   │
│   │   │   ├── start-bookstore-all.sh              # 全アプリ一括起動スクリプト
│   │   │   ├── stop-bookstore-all.sh               # 全アプリ一括停止スクリプト
│   │   │   └── README.md
│   │   │
│   │   └── person/                                 # 人物管理ドメイン（レガシー技術学習用）
│   │       ├── jsf-person/                         # Person管理（JSF 4.0 + JPA 3.1）
│   │       └── struts-person/                      # Person管理（Struts 1.3 + EJB）
│   │
│   ├── 📂 sdd-wf/                                  # 仕様駆動開発（ウォーターフォール）研修用
│   │   ├── bookstore/
│   │   │   ├── back-office-api/                    # 【SDD対象】REST API: 書籍・在庫管理
│   │   │   │   ├── specs/                          # 【SDD】仕様書
│   │   │   │   │   └── baseline/
│   │   │   │   │       ├── requirements/           # 【保護】要件定義書
│   │   │   │   │       ├── basic_design/           # 【保護】基本設計書
│   │   │   │   │       └── detailed_design/        # 詳細設計書（クリーン可）
│   │   │   │   ├── tasks/                          # 【SDD】タスク分解結果（クリーン可）
│   │   │   │   ├── src/                            # 実装コード（クリーン可）
│   │   │   │   ├── sql/hsqldb/
│   │   │   │   └── README.md
│   │   │   │
│   │   │   ├── berry-books-api/                    # 【SDD対象】REST API: 注文管理
│   │   │   │   └── （同じSDD構造）
│   │   │   │
│   │   │   ├── back-office-spa/                    # （masterと同一）
│   │   │   ├── berry-books-spa/                    # （masterと同一）
│   │   │   ├── customer-hub-api/                   # （masterと同一）
│   │   │   └── customer-hub-spa/                   # （masterと同一）
│   │   │
│   │   └── person/
│   │       └── jsf-person/                         # 【SDD対象】Person管理（JSF）
│   │
│   └── 📂 sdd-agile/                               # 仕様駆動開発（アジャイル）研修用
│       └── bookstore/
│           ├── back-office-api/                    # 【SDD対象】REST API: 書籍・在庫管理
│           │   ├── specs/                          # 【SDD】仕様書
│           │   │   └── baseline/
│           │   │       ├── common/                 # 【保護】業務共通SPEC
│           │   │       ├── usecases/               # 【保護】ユースケースSPEC
│           │   │       └── detailed_design/        # 詳細設計（イテレーション毎）
│           │   ├── tasks/                          # 【SDD】タスク（イテレーション毎）
│           │   └── src/                            # 実装コード（イテレーション毎）
│           │
│           ├── berry-books-api/                    # 【SDD対象】REST API: 注文管理
│           ├── back-office-spa/                    # （masterと同一）
│           ├── berry-books-spa/                    # （masterと同一）
│           ├── customer-hub-api/                   # （masterと同一）
│           └── customer-hub-spa/                   # （masterと同一）
│
├── 📂 agent_skills/                                # AI エージェントスキル定義
│   ├── archunit-test/                              # アーキテクチャテスト自動生成
│   ├── cucumber-test/                              # Cucumber BDDテスト支援
│   ├── jakarta-ee-api-agile/                       # Jakarta EE API開発（アジャイル）
│   ├── jakarta-ee-api-base/                        # Jakarta EE API開発（基本）
│   ├── playwright-e2e-test/                        # Playwright E2Eテスト支援
│   └── struts-to-jsf-migration/                    # Struts→JSFマイグレーション
│
├── 📂 payara6/                                     # Payara Server 6（Jakarta EE 10 対応）
│   ├── bin/                                        # asadmin等の実行スクリプト
│   ├── glassfish/
│   │   └── domains/
│   │       └── domain1/                            # デフォルトドメイン
│   │           ├── config/
│   │           │   ├── domain.xml                  # サーバー設定（実行時）
│   │           │   └── domain.xml.template         # 初期設定テンプレート（Git管理）
│   │           ├── logs/                           # ログファイル
│   │           └── autodeploy/                     # 自動デプロイフォルダ
│   └── ...
│
├── 📂 hsqldb/                                      # HSQLDB データベースサーバー
│   ├── lib/                                        # hsqldb.jar, sqltool.jar
│   ├── data/                                       # データファイル格納先
│   ├── sqltool.rc                                  # SQL ツール接続設定
│   └── ...
│
├── 📂 mp4/                                         # デモ動画（実行例・使い方）
│   ├── ArchiUnit.mp4
│   ├── Playwright E2Eテスト実行.mp4
│   ├── Playwright_E2Eテストコード生成.mp4
│   ├── サンプルアプリ起動.mp4
│   └── ...
│
├── 📂 gradle/                                      # Gradle ラッパー
│   └── wrapper/
│
├── 📄 build.gradle                                 # Gradle ビルド設定（メイン）
├── 📄 settings.gradle                              # Gradle マルチプロジェクト設定
├── 📄 env-conf.gradle                              # 環境設定（Payara, HSQLDB）
│
├── 📄 README.md                                    # プロジェクト全体説明（環境構築・コマンド等）
├── 📄 CHANGELOG.md                                 # 変更履歴
├── 📄 プロンプト.md                                # AI プロンプトテンプレート
└── 📄 .gitignore                                   # Git 除外設定
```

---

## 📋 主要ディレクトリの説明

### 1. `projects/` - アプリケーションプロジェクト

3つのカテゴリに分かれています。

#### 📁 `projects/master/` - 完成版（模範解答）

動作確認済みの完成版コード。**これ以上手を入れない想定**で、学習のリファレンス実装として利用します。

```
projects/master/
├── bookstore/                            # 書店ドメイン（フルスタック）
│   ├── berry-books-api/                  # REST API: 注文管理（Jakarta EE）
│   ├── berry-books-spa/                  # SPA: 注文管理 (React+TS)
│   ├── back-office-api/                  # REST API: 書籍・在庫管理（Jakarta EE）
│   ├── back-office-spa/                  # SPA: 書籍管理 (React+TS)
│   ├── customer-hub-api/                 # REST API: 顧客管理（Jakarta EE）
│   ├── customer-hub-spa/                 # SPA: 顧客管理 (React+TS)
│   ├── customer-hub-swing/               # Desktop: 顧客管理 (Swing)
│   ├── start-bookstore-all.sh            # 全アプリ一括起動スクリプト
│   ├── stop-bookstore-all.sh             # 全アプリ一括停止スクリプト
│   └── README.md                         # Bookstoreドメインの詳細説明
│
└── person/                               # 人物管理ドメイン
    ├── jsf-person/                       # Person管理（JSF + JPA）
    └── struts-person/                    # Person管理（Struts 1.3 + EJB）
```

##### 📦 Jakarta EE API プロジェクトの構造（例: berry-books-api）

```
berry-books-api/
├── src/
│   ├── main/
│   │   ├── java/pro/kensait/berrybooks/
│   │   │   ├── api/                      # REST API エンドポイント（JAX-RS）
│   │   │   │   ├── dto/                  # データ転送オブジェクト
│   │   │   │   └── exception/            # API例外ハンドラ
│   │   │   ├── service/                  # ビジネスロジック（CDI）
│   │   │   │   ├── order/                # 注文サービス
│   │   │   │   ├── customer/             # 顧客サービス
│   │   │   │   └── delivery/             # 配送サービス
│   │   │   ├── dao/                      # データアクセス層（JPA）
│   │   │   ├── entity/                   # JPAエンティティ
│   │   │   ├── external/                 # 外部API連携
│   │   │   ├── security/                 # セキュリティ（CORS等）
│   │   │   ├── common/                   # 共通ユーティリティ
│   │   │   └── util/                     # ヘルパークラス
│   │   ├── resources/
│   │   │   └── META-INF/
│   │   │       ├── persistence.xml       # JPA設定
│   │   │       └── beans.xml             # CDI設定
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml               # Webアプリ設定
│   │       └── resources/                # 静的リソース
│   │           └── images/               # 画像ファイル
│   └── test/java/                        # JUnitテスト、ArchUnitテスト
│
├── sql/hsqldb/                           # データベース初期化SQL
├── test_script/                          # 動作確認用スクリプト
├── build.gradle                          # Gradleビルド設定
└── README_ARCHUNIT.md                    # アーキテクチャテストの説明
```

##### 📦 React SPA プロジェクトの構造（例: berry-books-spa）

```
berry-books-spa/
├── src/
│   ├── components/                       # 再利用可能なUIコンポーネント
│   │   ├── Layout/                       # レイアウトコンポーネント
│   │   ├── OrderList/                    # 注文一覧コンポーネント
│   │   └── ...
│   ├── pages/                            # ページコンポーネント（ルーティング）
│   │   ├── LoginPage.tsx
│   │   ├── OrderPage.tsx
│   │   └── ...
│   ├── services/                         # API通信サービス
│   │   ├── api.ts                        # API基本設定
│   │   ├── orderService.ts               # 注文API呼び出し
│   │   └── ...
│   ├── contexts/                         # React Context（状態管理）
│   │   └── AuthContext.tsx               # 認証コンテキスト
│   ├── types/                            # TypeScript型定義
│   │   └── order.ts
│   ├── styles/                           # スタイル（CSS/Tailwind）
│   ├── App.tsx                           # アプリケーションルート
│   └── main.tsx                          # エントリーポイント
│
├── tests/                                # Playwrightテスト
│   └── pages/
│
├── public/                               # 静的ファイル
├── index.html                            # HTMLテンプレート
├── vite.config.ts                        # Vite設定
├── tsconfig.json                         # TypeScript設定
├── tailwind.config.js                    # Tailwind CSS設定
├── package.json                          # npm依存関係
└── README.md                             # プロジェクト説明
```

##### 📦 Swing デスクトップアプリの構造（customer-hub-swing）

```
customer-hub-swing/
├── src/main/java/pro/kensait/customerhub/
│   ├── ui/                               # Swing UIコンポーネント
│   │   ├── MainFrame.java                # メインウィンドウ
│   │   ├── CustomerPanel.java            # 顧客管理パネル
│   │   └── ...
│   ├── service/                          # REST API呼び出しサービス
│   ├── model/                            # データモデル
│   └── util/                             # ユーティリティ
│
├── build.gradle                          # Gradleビルド設定
└── README.md
```

#### 📁 `projects/sdd-wf/` - 仕様駆動開発（ウォーターフォール）

仕様書→タスク分解→詳細設計→実装の順で進める手法を学習するための研修用プロジェクトです。

```
projects/sdd-wf/
├── bookstore/                            # 書店ドメイン
│   ├── back-office-api/                  # 【SDD対象】REST API: 書籍・在庫管理
│   ├── berry-books-api/                  # 【SDD対象】REST API: 注文管理
│   ├── back-office-spa/                  # （master と同一）
│   ├── berry-books-spa/                  # （master と同一）
│   ├── customer-hub-api/                 # （master と同一）
│   └── customer-hub-spa/                 # （master と同一）
│
└── person/
    └── jsf-person/                       # 【SDD対象】Person管理（JSF）
```

##### 📦 SDD対象プロジェクトの構造（例: berry-books-api）

master プロジェクトの構造に加えて、以下のSDD関連フォルダが存在します。

```
berry-books-api/
├── specs/                                # 【SDD】仕様書フォルダ
│   └── baseline/                         # ベースライン仕様
│       ├── requirements/                 # 【保護】要件定義書（削除不可）
│       ├── basic_design/                 # 【保護】基本設計書（削除不可）
│       └── detailed_design/              # 詳細設計書（クリーン可能）
│
├── tasks/                                # 【SDD】タスク分解結果（クリーン可能）
│
├── src/                                  # 実装コード（クリーン可能）
│   ├── main/java/...                     # （master と同じ構造）
│   └── test/java/...
│
├── sql/hsqldb/                           # DB初期化SQL
├── test-reports/                         # テストレポート出力先
└── README.md                             # SDD手順・Gradleタスク説明
```

**SDD特有の機能:**

- `./gradlew :berry-books-api-sdd-wf:cleanTasks` - タスクのみクリーン
- `./gradlew :berry-books-api-sdd-wf:cleanDetailedDesign` - 詳細設計のみクリーン
- `./gradlew :berry-books-api-sdd-wf:cleanCode` - 実装コードのみクリーン
- `./gradlew :berry-books-api-sdd-wf:cleanAllSdd` - 全成果物をクリーン（要件定義・基本設計は保護）

#### 📁 `projects/sdd-agile/` - 仕様駆動開発（アジャイル）

イテレーション単位で仕様駆動開発を行い、アジャイル向けの進め方を学習します。

```
projects/sdd-agile/
└── bookstore/                            # 書店ドメイン
    ├── back-office-api/                  # 【SDD対象】REST API: 書籍・在庫管理
    ├── berry-books-api/                  # 【SDD対象】REST API: 注文管理
    ├── back-office-spa/                  # （master と同一）
    ├── berry-books-spa/                  # （master と同一）
    ├── customer-hub-api/                 # （master と同一）
    └── customer-hub-spa/                 # （master と同一）
```

##### 📦 SDD-Agile プロジェクトの構造（例: berry-books-api）

sdd-wf とほぼ同じ構造ですが、アジャイル開発向けに最適化されています。

```
berry-books-api/
├── specs/                                # 【SDD】仕様書フォルダ
│   └── baseline/                         # ベースライン仕様
│       ├── common/                       # 【保護】業務共通SPEC（削除不可）
│       ├── usecases/                     # 【保護】ユースケースSPEC（削除不可）
│       └── detailed_design/              # 詳細設計書（イテレーション毎に更新）
│
├── tasks/                                # 【SDD】タスク分解結果（イテレーション毎）
│
├── src/                                  # 実装コード（イテレーション毎に追加）
│   ├── main/java/...
│   └── test/java/...
│
└── README.md                             # SDD-Agile手順説明
```

**SDD-Agile特有の機能:**

- イテレーション単位での開発サイクル
- 業務共通SPEC + ユースケースSPEC の保護
- 詳細設計は sdd-agile では使用しない想定

---

### 2. `agent_skills/` - AI エージェントスキル

AI駆動開発を支援するスキル定義が格納されています。

| スキル | 説明 |
|--------|------|
| `archunit-test/` | ArchUnit を使用したアーキテクチャテストの自動生成 |
| `cucumber-test/` | Cucumber を使用した BDD テストの支援 |
| `jakarta-ee-api-agile/` | Jakarta EE API のアジャイル開発支援 |
| `jakarta-ee-api-base/` | Jakarta EE API の基本的な開発支援 |
| `playwright-e2e-test/` | Playwright を使用した E2E テストの支援 |
| `struts-to-jsf-migration/` | Struts から JSF へのマイグレーション支援 |

---

### 3. `payara6/` - Payara Server

Jakarta EE 10 対応のアプリケーションサーバー（GlassFish ベース）。

```
payara6/
├── bin/                                  # asadmin等の実行スクリプト
├── glassfish/
│   └── domains/
│       └── domain1/                      # デフォルトドメイン
│           ├── config/
│           │   ├── domain.xml            # サーバー設定（実行時）
│           │   └── domain.xml.template   # 初期設定テンプレート（Git管理）
│           └── logs/                     # ログファイル
└── ...
```

---

### 4. `hsqldb/` - HSQLDB データベース

組み込み可能な軽量 Java データベース。

```
hsqldb/
├── lib/                                  # hsqldb.jar, sqltool.jar
├── data/                                 # データファイル格納先
├── sqltool.rc                            # SQL ツール接続設定
└── ...
```

---

### 5. `mp4/` - デモ動画

各機能の実行例やチュートリアル動画が格納されています。

- `ArchiUnit.mp4` - ArchUnit テストのデモ
- `Playwright E2Eテスト実行.mp4` - E2E テスト実行のデモ
- `Playwright_E2Eテストコード生成.mp4` - テストコード生成のデモ
- `サンプルアプリ起動.mp4` - アプリ起動手順のデモ
- `リバースエンジニアリング.mp4` - リバースエンジニアリングのデモ
- `単体テスト評価.mp4` - 単体テスト評価のデモ

#### 📚 ドメイン別の特徴

| ドメイン | 説明 | 主要技術 | プロジェクト数 |
|---------|------|---------|-------------|
| **bookstore** | 書店の注文・在庫・顧客管理システム | Jakarta EE 10 + React + Swing | 7個（API×3 + SPA×3 + Swing×1） |
| **person** | 人物管理システム（レガシー技術学習用） | JSF, Struts, JPA, EJB | 2個（JSF×1 + Struts×1） |

##### 🏪 bookstore ドメインの構成

**3つのサブシステム（マイクロサービス的な分割）:**

| サブシステム | API | フロントエンド | 役割 |
|-------------|-----|---------------|------|
| **Berry Books** | berry-books-api | berry-books-spa | 注文管理・書籍検索・カート機能 |
| **Back Office** | back-office-api | back-office-spa | 書籍管理・在庫管理・発注管理 |
| **Customer Hub** | customer-hub-api | customer-hub-spa / customer-hub-swing | 顧客情報管理・顧客検索 |

**システム連携:**
- Berry Books API が Customer Hub API と Back Office API を呼び出す
- 各 SPA は対応する API のみを呼び出す
- Swing デスクトップアプリも Customer Hub API を利用

##### 👤 person ドメインの特徴

**レガシー技術の学習・マイグレーション研修用:**

| プロジェクト | 技術スタック | 用途 |
|-------------|-------------|------|
| **jsf-person** | JSF 4.0 + JPA 3.1 | Jakarta EE のビュー技術（JSF）を学習 |
| **struts-person** | Struts 1.3 + EJB | レガシーフレームワークからのマイグレーション実践 |

---

## 🚀 クイックスタート

### バックエンド（Jakarta EE API）

```bash
# 1. HSQLDBサーバー起動
./gradlew startHsqldb

# 2. Payara Server起動
./gradlew startPayara

# 3. データソースセットアップ
./gradlew setupDataSource

# 4. プロジェクトのDB初期化、ビルド、デプロイ
./gradlew :berry-books-api:setupHsqldb
./gradlew :berry-books-api:war
./gradlew :berry-books-api:deploy
```

### フロントエンド（React SPA）

```bash
# SPAプロジェクトディレクトリに移動
cd projects/master/bookstore/berry-books-spa

# 依存関係をインストール（初回のみ）
npm install

# 開発サーバーを起動
npm run dev
```

### 一括起動（Bookstore フルスタック）

```bash
# Bookstoreドメインの全アプリケーションを一括起動
cd projects/master/bookstore
./start-bookstore-all.sh
```

---

## 📚 詳細ドキュメント

- **[README.md](./README.md)** - 環境構築・コマンド・トラブルシューティング
- **[projects/master/bookstore/README.md](./projects/master/bookstore/README.md)** - Bookstore ドメインの詳細
- **各プロジェクトの README.md** - プロジェクト固有の情報

---

## 🛠️ 技術スタック

| カテゴリ | 技術 |
|---------|------|
| **バックエンド** | Jakarta EE 10 (JAX-RS, CDI, JPA) + Payara Server 6 |
| **フロントエンド** | React 18 + TypeScript 5 + Vite 5 |
| **データベース** | HSQLDB 2.7.x |
| **ビルドツール** | Gradle 8.x |
| **レガシー技術** | Servlet/JSP, JSF, Struts, Swing |

---

## 📝 補足

### SDD（仕様駆動開発）プロジェクトについて

- **sdd-wf / sdd-agile** の `bookstore` において、SDD の対象となるのは `back-office-api` と `berry-books-api` の2つのみです。
- それ以外（SPA、`customer-hub-api` 等）は `master` と同じ内容です。
- 基本設計 SPEC（`specs/*/basic_design/`）は保護され、削除されません。
- タスク（`tasks/`）、詳細設計 SPEC（`specs/*/detailed_design/`）、実装コード（`src/`）は個別にクリーンアップ可能です。

### プロジェクトカテゴリの使い分け

| カテゴリ | 用途 | 想定される利用者 | 特有フォルダ |
|---------|------|----------------|-------------|
| **master** | 動作確認済みの完成版コード | リファレンス実装の参照 | なし（純粋な実装のみ） |
| **sdd-wf** | ウォーターフォール型の仕様駆動開発 | 段階的開発手法を学習したい方 | specs/, tasks/ |
| **sdd-agile** | アジャイル型の仕様駆動開発 | イテレーション型開発を学習したい方 | specs/, tasks/ |

### プロジェクトカテゴリの詳細比較

| 項目 | master | sdd-wf | sdd-agile |
|-----|--------|--------|-----------|
| **コード状態** | 完成版 | 再実装可能 | 再実装可能 |
| **SDD対象** | なし | back-office-api, berry-books-api, jsf-person | back-office-api, berry-books-api |
| **仕様書** | なし | requirements/, basic_design/, detailed_design/ | common/, usecases/, detailed_design/ |
| **開発スタイル** | - | 要件→基本設計→詳細設計→実装 | イテレーション単位で段階的に実装 |
| **タスク管理** | なし | tasks/ フォルダで一括管理 | tasks/ フォルダでイテレーション毎 |
| **クリーン機能** | なし | cleanTasks, cleanDetailedDesign, cleanCode, cleanAllSdd | 同左 |
| **保護される成果物** | - | requirements/, basic_design/ | common/, usecases/ |

---

## 🎯 推奨される学習フロー

1. **環境構築**: `README.md` に従って Payara Server と HSQLDB をセットアップ
2. **完成版で動作確認**: `projects/master/bookstore/` で全体の動作を確認
3. **SDD実践**: `projects/sdd-wf/` または `projects/sdd-agile/` で仕様駆動開発を体験
4. **AI スキル活用**: `agent_skills/` のスキルを使って AI 駆動開発を実践

---

**作成日**: 2026年2月12日  
**最終更新**: 2026年2月12日  
**バージョン**: 1.1
