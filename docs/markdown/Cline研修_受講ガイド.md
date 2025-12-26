# Cline研修_受講ガイド

## 📖 はじめに

このガイドは、Clineを使用した実践的な研修プログラムの受講生向けマニュアルである。

**前提条件:**
- 研修講師により環境セットアップは完了済み
- Payara Server 6とHSQLDB Databaseが起動済み
- データソース（`jdbc/HsqldbDS`）が作成済み
- 初期データがセットアップ済み
- `berry-books-1`と`berry-books-rest`がデプロイ済み

## 📁 プロジェクト構成

研修では以下のプロジェクトを使用する：

```
cline_training_work/
├── projects/
│   ├── java/
│   │   ├── berry-books-1/           # レッスン1～5: Jakarta EE基礎【研修用】
│   │   │   ├── src/                 # Javaソースコード
│   │   │   ├── spec/                # 詳細設計書、アーキテクチャガイドライン
│   │   │   └── prompts/             # レッスン1～5のプロンプト
│   │   │       ├── lesson_1_minor_improvement/
│   │   │       ├── lesson_2_check_guideline/
│   │   │       ├── lesson_3_potential_bag/
│   │   │       ├── lesson_4_enhance/
│   │   │       └── lesson_5_unittest/
│   │   │
│   │   ├── berry-books-2/           # レッスン6: リファクタリング【研修用】
│   │   │   ├── src/                 # Javaソースコード
│   │   │   ├── spec/                # アーキテクチャガイドライン
│   │   │   └── prompts/
│   │   │       └── lesson_6_refactoring/
│   │   │
│   │   ├── berry-books-rest/        # REST API【完成版】
│   │   │   ├── src/                 # Javaソースコード
│   │   │   └── spec/                # OpenAPI仕様書
│   │   │
│   │   └── struts-to-jsf-person-sdd/    # レッスン8: マイグレーション【研修用】
│   │       ├── src/                 # Javaソースコード（Struts 1.3）
│   │       ├── spec/                # 移行仕様書
│   │       └── prompts/
│   │           └── lesson_8_migration/
│   │
│   ├── react/
│   │   └── berry-books-frontend-vibe/ # レッスン7: React SPA【研修用】
│   │       ├── src/                 # React + TypeScriptソースコード
│   │       └── prompts/
│   │           └── lesson_7_spec_driven/
│   │
│   └── python/
│       └── accounting_etl_sdd/      # レッスン9: ETLスクラッチ開発【研修用】
│           ├── src/                 # Pythonソースコード
│           ├── spec/                # 要件定義書、基本設計書、詳細設計書
│           ├── ddl/                 # データ定義（入出力仕様）
│           └── prompts/
│               └── lesson_9_etl_scratch/
```

### プロジェクトの役割

| プロジェクト | 技術スタック | 学習内容 |
|------------|------------|---------|
| `berry-books-1` | Jakarta EE 10<br>JSF, CDI, JPA | 小規模改善、ガイドライン準拠、<br>不具合検出、機能拡張、単体テスト |
| `berry-books-2` | Jakarta EE 10<br>JSF, CDI, JPA | 大規模リファクタリング<br>（Service/DAO分離） |
| `berry-books-rest` | Jakarta EE 10<br>JAX-RS, CDI, JPA | REST API【完成版】<br>バックエンドAPI提供 |
| `berry-books-frontend-vibe` | React 18 + TypeScript 5<br>Vite | スペック駆動開発<br>（OpenAPI仕様からのReact SPA実装） |
| `struts-to-jsf-person-sdd` | Struts 1.3 → Jakarta EE 10<br>JSF, CDI, JPA | レガシーFWの<br>モダナイゼーション |
| `accounting_etl_sdd` | Python<br>Pandas / PySpark | ETL処理のスクラッチ開発<br>（データ統合・変換） |

## 🎓 学習手順

### 基本的な進め方

各レッスンは以下の流れで進める：

#### 1. コンテキストの追加

Clineのチャットパネルに、対象プロジェクトのソースフォルダを追加する。

**操作手順:**
```
1. VS Codeのエクスプローラーで各プロジェクトのルートフォルダを選択
2. SHIFT + ドラッグ＆ドロップで、Clineチャットパネルにフォルダをドロップ
3. コンテキストとして追加されたことを確認
```

**例:**
- レッスン1～5: `projects/java/berry-books-1/` を追加
- レッスン6: `projects/java/berry-books-2/` を追加
- レッスン7: `projects/react/berry-books-frontend-vibe` を追加
- レッスン8: `projects/java/struts-to-jsf-person-sdd` を追加
- レッスン9: `projects/python/accounting_etl_sdd` を追加

#### 2. プロンプトの選択

各レッスンには3つのレベルのプロンプトが用意されている：

| プロンプト | ファイル名 | 特徴 | 推奨対象 |
|----------|-----------|-----|---------|
| **Simple** | `prompt_1_simple.txt` | **最も抽象的**<br>要求のみ記述、実装詳細なし | 生成AIの推論能力を試したい方 |
| **Just** | `prompt_2_just.txt` | **バランス型**<br>適度な情報量 | 標準的な進め方 |
| **Much** | `prompt_3_much.txt` | **最も具体的**<br>詳細な指示、参照資料明示 | 確実に成果を出したい方 |

**選び方のポイント:**
- **Simple**: 生成AIがどこまで仕様を理解できるか試したい場合
- **Just**: 適度なガイドでバランスよく進めたい場合（推奨）
- **Much**: 詳細な指示で生成AIを確実に誘導したい場合

#### 3. Clineの実行モード選択

Clineには2つの実行モードがある：

**A. Planモード（推奨）**
```
1. プロンプトを投入
2. ClineがPlanモードで実行計画を提示
3. 計画を確認し、問題なければActモードで実行を指示
4. コード生成・修正が実行される
```

**利点:**
- 生成AIの精度を事前確認できる
- 誤った方向性を早期に修正できる

**B. Actモード（直接実行）**
```
1. プロンプトを投入
2. Clineがいきなりコード生成・修正を実行
```

**利点:**
- 素早く結果が得られる
- シンプルな作業に適している

#### 4. 成果物の確認とテスト

Clineが作成したコードを確認し、動作をテストする。

**確認ポイント:**
- ビルドエラーがないか
- 期待通りの動作をするか
- コード品質（可読性、保守性）

**デプロイとテスト:**
```bash
# ビルド
./gradlew :projects:java:berry-books-1:war

# デプロイ
./gradlew :projects:java:berry-books-1:deploy

# ブラウザでアクセス
http://localhost:8080/berry-books-1
```

---

### 各レッスンの進め方

#### レッスン1: 小規模改善【berry-books-1】

**目的:** 既存コードの品質改善（定数管理、メッセージ管理、共通化）

**プロンプト格納先:**
```
projects/java/berry-books-1/prompts/lesson_1_minor_improvement/
├── prompt_1_simple.txt
├── prompt_2_just.txt
└── prompt_3_much.txt
```

---

#### レッスン2: ガイドライン準拠チェック【berry-books-1】

**目的:** アーキテクチャガイドラインへの準拠状況を生成AIに診断させる

**プロンプト格納先:**
```
projects/java/berry-books-1/prompts/lesson_2_check_guideline/
├── prompt_1_simple.txt
├── prompt_2_just.txt
└── prompt_3_much.txt
```

---

#### レッスン3: 潜在的不具合検出【berry-books-1】

**目的:** 生成AIによる解析で潜在的な不具合を検出

**プロンプト格納先:**
```
projects/java/berry-books-1/prompts/lesson_3_potential_bag/
├── prompt_1_simple.txt
├── prompt_2_just.txt
└── prompt_3_much.txt
```

---

#### レッスン4: 機能拡張【berry-books-1】

**目的:** 詳細設計書からの機能拡張（在庫チェック機能の追加）

**プロンプト格納先:**
```
projects/java/berry-books-1/prompts/lesson_4_enhance/
├── prompt_1_simple.txt
├── prompt_2_just.txt
└── prompt_3_much.txt
```

---

#### レッスン5: 単体テスト作成【berry-books-1】

**目的:** JUnitテストコードの自動生成

**プロンプト格納先:**
```
projects/java/berry-books-1/prompts/lesson_5_unittest/
├── prompt_1_simple.txt
├── prompt_2_just.txt
├── prompt_3_much.txt
└── prompt_4_bad.txt（失敗例）
```

---

#### レッスン6: 大規模リファクタリング【berry-books-2】

**目的:** アーキテクチャの再設計（Service/DAO分離）

**プロンプト格納先:**
```
projects/java/berry-books-2/prompts/lesson_6_refactoring/
├── prompt_1_simple.txt
├── prompt_2_just.txt
└── prompt_3_much.txt
```

---

#### レッスン7: スペック駆動開発【berry-books-frontend-vibe】

**目的:** OpenAPI仕様書からのReact SPA実装

**プロンプト格納先:**
```
projects/react/berry-books-frontend-vibe/prompts/lesson_7_spec_driven/
├── prompt_1_simple.txt
├── prompt_2_just.txt
└── prompt_3_much.txt
```

---

#### レッスン8: Jakarta EE 10マイグレーション【struts-to-jsf-person-sdd】

**目的:** レガシーシステム（Struts 1.3）のモダナイゼーション

**プロンプト格納先:**
```
projects/java/struts-to-jsf-person-sdd/prompts/lesson_8_migration/
├── prompt_1_simple.txt
├── prompt_2_just.txt
├── prompt_3_much_1-jpa.txt（データアクセス層）
├── prompt_3_much_2-cdi.txt（ビジネスロジック層）
├── prompt_3_much_3-jsf.txt（バッキングBean）
├── prompt_3_much_4-facelets.txt（プレゼンテーション層）
└── prompt_3_much_5-config.txt（設定ファイル）
```

---

#### レッスン9: ETLスクラッチ開発【accounting_etl_sdd】

**目的:** Python/PySparkによるデータ統合ETL処理の実装

**プロンプト格納先:**
```
projects/python/accounting_etl_sdd/prompts/lesson_9_etl_scratch/
├── prompt_1_simple.txt
├── prompt_2_just.txt
└── prompt_3_much.txt
```

---

## 📖 各プロジェクトの詳細

#### 📘 berry-books-1（レッスン1～5）

**概要**

Jakarta EE 10を使用したオンライン書店のWebアプリケーション（顧客向け画面）。書籍検索、ショッピングカート、注文処理などのEC機能を実装している。

> **Note**: 管理者画面は別途`berry-books-frontend`/`berry-books-frontend-vibe`（React SPA）として実装。データベースは両プロジェクトで共有。

**採用技術**

- **フレームワーク**: Jakarta EE 10（JSF, CDI, JPA）
- **サーバー**: Payara Server 6
- **データベース**: HSQLDB（`berry-books-frontend`/`berry-books-frontend-vibe`と共有）
- **テスト**: JUnit 5 + Mockito

**システム構成**

```
[Webブラウザ] ⇄ [Payara Server] ⇄ [HSQLDB（共有DB）]
              (berry-books-1.war)       ↑
              └─ JSF View (Facelets)    │
              └─ Managed Bean (@Named)  │ 同じDBを使用
              └─ Service (@ApplicationScoped) │
              └─ Entity (JPA)           ↓
                                  (berry-books-rest.war)
                                  └─ 管理者画面用REST API
```

**主な機能**

- 書籍検索・閲覧（カテゴリ/キーワード検索）
- ショッピングカート（セッション管理）
- 注文処理（配送先入力、決済方法選択、在庫チェック）
- 注文履歴表示

**研修で学ぶこと**

- **レッスン1**: 決済方法のEnum化、メッセージリソース化、住所判定ロジックの共通化
- **レッスン2**: アーキテクチャガイドラインへの準拠チェック
- **レッスン3**: 潜在的不具合の検出（メールアドレスバリデーション漏れ、楽観的ロック不正など）
- **レッスン4**: 在庫チェック機能の追加（詳細設計書からの要件読み取り）
- **レッスン5**: 単体テスト作成（JUnit + Mockito）

---

#### 📙 berry-books-2（レッスン6）

**概要**

`berry-books-1`をCDI + JPAでリアーキテクトしたバージョン。現状ではServiceクラスに直接EntityManagerが注入されており、DAOロジックがServiceに混在している。これをレイヤードアーキテクチャに再設計するのがレッスン6の課題である。

**採用技術**

- **フレームワーク**: Jakarta EE 10（JSF, CDI, JPA）
- **サーバー**: Payara Server 6
- **データベース**: HSQLDB
- **テスト**: JUnit 5 + Mockito

**システム構成（リファクタリング後）**

```
[Webブラウザ] ⇄ [Payara Server] ⇄ [HSQLDB]
              (berry-books-2.war)
              └─ JSF View (Facelets)
              └─ Managed Bean (@Named)
              └─ Service (@ApplicationScoped)
              └─ DAO (@ApplicationScoped)  ← リファクタリングで新規作成
              └─ Entity
```

**主な機能**

- `berry-books-1`と同等の機能（書籍検索、カート、注文処理）
- JPAによるデータベースアクセス

**研修で学ぶこと**

- **レッスン6**: 大規模リファクタリング
  - Service層とDAO層の責務分離
  - レイヤードアーキテクチャの実装
  - JPA EntityManagerの適切な配置
  - アーキテクチャガイドラインへの準拠

---

#### 📕 berry-books-rest（REST API）

**概要**

Berry Books オンライン書店のREST APIバックエンド。Jakarta EE 10のJAX-RSを使用し、顧客管理機能をRESTful APIとして提供している。

> **Note**: このプロジェクトは研修課題ではなく、**レッスン7（berry-books-frontend-vibe）のバックエンドAPIとして使用**される。

**採用技術**

- **フレームワーク**: Jakarta EE 10（JAX-RS, CDI, JPA）
- **サーバー**: Payara Server 6
- **データベース**: HSQLDB（`berry-books-1`と共有）
- **データ形式**: JSON（JSON-B）

**システム構成**

```
[React Frontend] ⇄ [Payara Server] ⇄ [HSQLDB（共有DB）]
(berry-books-frontend-vibe) (berry-books-rest.war)
                        └─ JAX-RS Resource
                        └─ Service (@ApplicationScoped)
                        └─ DAO (@ApplicationScoped)
                        └─ Entity
```

**主な機能（REST API）**

- 顧客情報取得（GET `/customers/{id}`）
- 顧客一覧取得（GET `/customers`）
- 顧客情報更新（PUT `/customers/{id}`）
- 顧客の注文履歴取得（GET `/customers/{id}/orders`）

**API仕様**

- `spec/openapi.yaml` - OpenAPI 3.0仕様書

**アクセスURL**

```
http://localhost:8080/berry-books-rest/
```

---

#### 📗 berry-books-frontend-vibe（レッスン7）

**概要**

Berry Books オンライン書店の管理者画面。React + TypeScriptによるSPA（Single Page Application）として実装している。`berry-books-rest` APIと連携して顧客管理機能を提供する。

**採用技術**

- **フロントエンド**: React 18 + TypeScript 5
- **ビルドツール**: Vite 5
- **スタイリング**: CSS3（Berry Booksテーマカラー: #CF3F4E）
- **バックエンドAPI**: `berry-books-rest`

**システム構成**

```
[Webブラウザ] ⇄ [Vite Dev Server] ⇄ [Payara Server]
              http://localhost:3000  (berry-books-rest.war)
              └─ React SPA           └─ REST API
```

**主な機能**

- 顧客一覧表示（テーブル形式）
- 顧客情報の編集（ダイアログ形式）
- 注文件数・購入冊数の統計情報表示

**研修で学ぶこと**

- **レッスン7**: スペック駆動開発
  - OpenAPI仕様書の読解
  - React + TypeScriptによるSPA実装
  - REST API連携
  - レスポンシブデザイン

**実行方法**

```bash
# 依存関係のインストール（初回のみ）
cd projects/react/berry-books-frontend
npm install

# 開発サーバー起動
npm run dev
```

**アクセスURL**

```
http://localhost:3000
```

---

#### 📙 struts-to-jsf-person-sdd（レッスン8）

**概要**

レガシーなStruts 1.3.10を使用した人材管理システム。フレームワークマイグレーション（Struts → Jakarta EE 10 JSF）の研修課題として使用する。

**採用技術（移行前）**

- **フレームワーク**: Apache Struts 1.3.10（レガシー）
- **ビジネスロジック**: EJB 3.2（Stateless Session Bean）
- **データアクセス**: JDBC（PreparedStatement）
- **サーバー**: Apache TomEE 8（研修では使用しない）
- **データベース**: HSQLDB

**システム構成（移行前）**

```
[Webブラウザ] ⇄ [TomEE 8] ⇄ [HSQLDB]
              (struts-person.war)
              └─ JSP + Strutsタグ
              └─ Action (Struts Controller)
              └─ ActionForm
              └─ EJB Service (@Stateless)
              └─ DAO (JDBC)
```

**移行後の技術スタック（レッスン8で実現）**

- **フレームワーク**: Jakarta EE 10（JSF, CDI, JPA）
- **サーバー**: Payara Server 6
- **データベース**: HSQLDB

**主な機能**

- Person一覧表示
- Person追加・編集（入力→確認→登録の3画面遷移）
- Person削除

**研修で学ぶこと**

- **レッスン8**: フレームワークのマイグレーション
  - Strutsタグライブラリの理解
  - レガシーコードの解析
  - 全レイヤーの書き換え（JSP → Facelets、Action → Managed Bean、JDBC → JPA、EJB → CDI）
  - モダンなアーキテクチャへの全面移行

---

#### 📘 accounting_etl_sdd（レッスン9）

**概要**

上流システム（売上・人事・在庫）から会計パッケージへのデータ統合を行うETL処理。ローカル（開発）とAWS Glue（本番）の両方で実行可能な設計となっている。

**採用技術**

- **言語**: Python 3.11
- **実行エンジン**: PySpark（ローカル/AWS Glue）
- **データソース**: CSV（ファイルシステム/S3）
- **テスト**: pytest
- **並列処理**: ThreadPoolExecutor / Spark

**システム構成（ローカル環境）**

```
[test_data/]
├─ sales/*.csv（売上データ）
├─ hr/*.csv（人事データ）
└─ inventory/*.csv（在庫データ）
    ↓ read CSV
[ETL Orchestrator]
├─ Sales ETL Job
├─ HR ETL Job
└─ Inventory ETL Job
    ↓ transform（共通Transformer）
[output/]
└─ accounting_txn_interface.csv（統合ファイル）
```

**システム構成（本番環境 - AWS）**

```
[S3: input/]
    ↓
[AWS Glue Jobs（3ジョブ並列実行）]
    ↓ transform（ローカルと同じTransformer）
[S3: output/]
```

**主な機能**

- **Sales ETL**: 売上データ → 売掛金/売上高の仕訳生成
- **HR ETL**: 人事データ → 給料手当/未払費用の仕訳生成
- **Inventory ETL**: 在庫データ → 商品/売上原価の仕訳生成
- **並列処理**: 3つのETLジョブを同時実行
- **統合出力**: 会計パッケージ取込用の単一CSVファイル生成

**研修で学ぶこと**

- **レッスン9**: 仕様書からのスクラッチ開発
  - ETL処理の設計・実装
  - データ変換ロジック（Transformer）
  - Python標準版とPySpark版の両対応
  - 単体テスト・統合テスト（pytest）
  - クラウド対応（AWS Glue）
