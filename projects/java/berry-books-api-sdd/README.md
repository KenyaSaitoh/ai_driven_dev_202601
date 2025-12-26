# berry-books-api-sdd プロジェクト

## 📖 概要

Jakarta EE 10によるオンライン書店「**Berry Books**」のWebアプリケーションです。
書籍検索、ショッピングカート、注文処理などのEC機能を実装しています。

**主な機能:**
- 📚 書籍検索・閲覧（カバー画像表示対応）
- 🛒 ショッピングカート管理
- 💳 注文処理（配送先・決済方法選択）
- 👤 顧客管理・認証
- 📋 注文履歴参照

このプロジェクトは**仕様駆動開発（SDD: Specification-Driven Development）** アプローチを採用し、完成したSPECから生成AIによる実装を実践します。

---

## 🎯 このプロジェクトの位置付け

### 完成したSPECが存在する
**このプロジェクト（berry-books-api-sdd）は、完成したベースラインSPECが `specs/baseline/system/` に用意されています。**

仕様駆動開発の実装フェーズに焦点を当てており、**生成AIにSPECを読ませて実装させる**ことを目的としています。

### ベースラインSPECの構造
- **`specs/baseline/system/`**: システム全体のアーキテクチャ・設計・要件を定義したSPEC（概要）
- **`specs/baseline/features/`**: 個別機能のSPEC（F_001～F_005の詳細設計）

### 2つの利用シナリオ

#### シナリオ1: 新規・更改案件
完成したSPECを使って、生成AIにアプリケーション全体を実装させます。

**対象:**
- 新規開発（ゼロからの構築）
- 大規模な更改・リプレース
- 完全なSPECからの実装体験

**詳細:** 下記の「[📦 シナリオ1: 新規・更改案件の実装](#-シナリオ1-新規更改案件の実装)」を参照

#### シナリオ2: 小規模改善・機能拡張案件
既に稼働しているシステムに対して、小規模な改善や機能追加を行います。

**対象:**
- 新機能の追加（例: 在庫アラート機能）
- 既存機能の改善・拡張
- バグ修正・セキュリティ対策
- パフォーマンス改善

**詳細:** 下記の「[🚀 シナリオ2: 小規模改善・機能拡張案件](#-シナリオ2-小規模改善機能拡張案件)」を参照

---

## 📁 プロジェクト構成

```
berry-books-api-sdd/
├── instructions/             # 生成AI用インストラクションファイル
│   ├── generate_tasks.md     # タスク分解インストラクション
│   └── generate_code.md      # 実装インストラクション
├── memory/                   # プロジェクト憲章（AIが参照）
│   └── constitution.md       # 開発憲章（開発原則、品質基準、組織標準）
├── specs/                    # SPEC（仕様書）
│   ├── baseline/             # 001: ベースラインSPEC
│   │   ├── system/           # システム全体のSPEC
│   │   │   ├── requirements.md       # 要件定義書
│   │   │   ├── architecture_design.md  # アーキテクチャ設計書
│   │   │   ├── functional_design.md  # 機能設計書
│   │   │   ├── behaviors.md          # 振る舞い仕様書
│   │   │   ├── data_model.md         # データモデル仕様書
│   │   │   ├── screen_design.md      # 画面設計書
│   │   │   └── external_interface.md # 外部インターフェース仕様書
│   │   └── features/         # 個別機能のSPEC（F_001～F_005）
│   │       ├── F_001_book_search/      # 書籍検索・閲覧
│   │       ├── F_002_shopping_cart/    # ショッピングカート管理
│   │       ├── F_003_order_processing/ # 注文処理
│   │       ├── F_004_customer_auth/    # 顧客管理・認証
│   │       └── F_005_order_history/    # 注文履歴参照
│   ├── tasks/                # 実装タスク（並行作業対応・複数ファイルに分割）
│   │   ├── tasks.md                       # メインタスクリスト（全体概要）
│   │   ├── setup_tasks.md                 # セットアップタスク
│   │   ├── common_tasks.md                # 共通機能タスク
│   │   ├── F_001_book_search.md           # F_001: 書籍検索・閲覧
│   │   ├── F_002_shopping_cart.md         # F_002: ショッピングカート管理
│   │   ├── F_003_order_processing.md      # F_003: 注文処理
│   │   ├── F_004_customer_auth.md         # F_004: 顧客管理・認証
│   │   ├── F_005_order_history.md         # F_005: 注文履歴参照
│   │   └── integration_tasks.md           # 結合テストタスク
│   ├── enhancements/         # 拡張SPEC（機能追加・改善・修正）
│   │   └── 202512_inventory_alert/  # 例: 在庫アラート機能
│   │       ├── requirements.md
│   │       ├── functional_design.md
│   │       ├── behaviors.md
│   │       └── tasks/
│   │           ├── tasks.md
│   │           ├── setup_tasks.md
│   │           └── feature_tasks.md
│   └── templates/            # SPECテンプレート
│       ├── requirements.md
│       ├── architecture_design.md
│       ├── functional_design.md
│       ├── behaviors.md
│       ├── data_model.md
│       ├── screen_design.md
│       ├── external_interface.md
│       └── tasks.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   ├── resources/
│   │   └── webapp/
│   └── test/
├── sql/
│   └── hsqldb/
└── README.md
```

**主要フォルダの説明:**

- **`instructions/`**: 生成AIに指示を出すためのインストラクションファイル
- **`memory/`**: プロジェクトの憲章（開発原則、品質基準）※人間が作成、AIが参照
- **`specs/baseline/`**: ベースラインSPEC（システム全体・個別機能）
- **`specs/tasks/`**: 実装タスク（複数ファイルに分割、並行作業対応）
- **`specs/enhancements/`**: 拡張SPEC（002以降: 機能追加・改善・修正）
- **`specs/templates/`**: 拡張のSPECを作成する際のテンプレート
- **`src/`**: 実装コード（生成AIが生成）
- **`sql/`**: SQL（生成AIが生成）

---

## 🔧 セットアップとビルド

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
./gradlew :projects:java:berry-books-api-sdd:setupHsqldb

# 2. プロジェクトをビルド
./gradlew :projects:java:berry-books-api-sdd:war

# 3. プロジェクトをデプロイ
./gradlew :projects:java:berry-books-api-sdd:deploy
```

> **重要:** このプロジェクトは独自のデータベーススキーマを使用します。他のプロジェクトとは異なるテーブル構成のため、`setupHsqldb`を必ず実行してください。

### ⑤ プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# プロジェクトをアンデプロイ
./gradlew :projects:java:berry-books-api-sdd:undeploy
```

### ⑥ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
./gradlew :projects:java:berry-books-api-sdd:war
./gradlew :projects:java:berry-books-api-sdd:deploy
```

### 📍 アクセスURL

デプロイ後、以下のURLにアクセス：

- **トップページ**: http://localhost:8080/berry-books-api-sdd

---

## 🧪 テストの実行

アプリケーションをデプロイせずに、テストのみを実行したい場合は以下の手順に従ってください。

### 前提条件

テストを実行する前に、以下が起動していることを確認してください：

- **① HSQLDBサーバー** （`./gradlew startHsqldb`）
- **② データベースのセットアップ** （`./gradlew :projects:java:berry-books-api-sdd:setupHsqldb`）

> **Note:** Payara Server は起動不要です（単体テストの場合）。E2Eテストを実行する場合は Payara Server も起動が必要です。

### すべてのテストを実行（E2Eテストを除く）

```bash
# プロジェクトのすべてのテストを実行（E2Eテストは除外）
./gradlew :projects:java:berry-books-api-sdd:test
```

> **Note:** 通常のビルド時テスト（`./gradlew test`）では、Playwright E2Eテストは除外されます。E2Eテストは個別に実行する設計となっています。

### 特定のテストクラスを実行

```bash
# 特定のテストクラスのみを実行（例: BookServiceTest）
./gradlew :projects:java:berry-books-api-sdd:test --tests "pro.kensait.berrybooks.service.BookServiceTest"

# パターンマッチングで複数のテストクラスを実行（例: すべての統合テスト）
./gradlew :projects:java:berry-books-api-sdd:test --tests "*IntegrationTest"
```

### E2Eテスト（Playwright）を実行

E2Eテストは通常のビルド時テストとは別に、専用のGradleタスクで個別実行します。E2Eテストを実行する場合は、アプリケーションがデプロイされている必要があります：

```bash
# 1. Payara Server を起動（まだの場合）
./gradlew startPayara

# 2. アプリケーションをデプロイ（まだの場合）
./gradlew :projects:java:berry-books-api-sdd:deploy

# 3. E2Eテスト専用タスクを実行
./gradlew :projects:java:berry-books-api-sdd:e2eTest
```

> **Note:** E2Eテストクラスには `@Tag("e2e")` アノテーションが付与されており、通常の `test` タスクでは実行されません。`e2eTest` タスクで個別に実行する設計です。

### テストレポートの確認

テスト実行後、レポートは以下の場所に生成されます：

```
projects/java/berry-books-api-sdd/build/reports/tests/test/index.html
```

ブラウザでこのファイルを開くと、テスト結果の詳細を確認できます。

---

## 🧹 成果物のクリーンアップ（仕様駆動開発の再実行）

このプロジェクトは仕様駆動開発（SDD）により、何度でも再実装できます。既存の成果物をクリーンアップして、SPECから再実装する場合は以下のタスクを実行してください。

### クリーンアップタスク

```bash
# 成果物をクリーンアップ（ディレクトリ構造は保持）
./gradlew :projects:java:berry-books-api-sdd:cleanSddArtifacts
```

**このタスクが削除するもの:**
- `src/` 配下のすべてのファイル（Java、XHTML、CSS、設定ファイルなど）
- `sql/hsqldb/` 配下のすべてのSQLファイル

**このタスクが保持するもの:**
- 以下のディレクトリ構造（空のディレクトリも含む）：
  - `src/main/java`
  - `src/main/resources/META-INF`
  - `src/main/webapp/resources`
  - `src/main/webapp/WEB-INF`
  - `src/test/java`
  - `src/test/resources`
  - `sql/hsqldb`
- `specs/` フォルダ（SPEC）
- `instructions/` フォルダ（インストラクション）
- `memory/` フォルダ（憲章）
- その他のプロジェクト設定ファイル（`build.gradle`、`README.md`など）

**クリーンアップ後の状態:**
- 空のディレクトリには `.gitkeep` ファイルが自動配置されます
- ディレクトリ構造が保持されるため、再実装がスムーズに進められます

**クリーンアップ後の再実装手順:**
1. `@instructions/generate_tasks.md` を使ってタスクリストを生成（`tasks/` に複数ファイルとして保存）
2. `@instructions/generate_code.md` を使ってタスクに従って実装
   - タスク1から順に実行（setup_tasks.md → common_tasks.md → F_001_*.md ～ F_005_*.md → integration_tasks.md）
   - タスクの依存関係を考慮して、並行作業可能なタスクは分担可能

---

## 📦 シナリオ1: 新規・更改案件の実装

**このプロジェクト（berry-books-api-sdd）は、完成したSPECが `specs/baseline/` に用意されています。**

完成したSPECから、生成AIにアプリケーション全体を実装させます。

> プロジェクト構成の詳細は「[📁 プロジェクト構成](#-プロジェクト構成)」を参照してください。

### 実行手順

#### タスク分解フェーズ

**注意:** このプロジェクトには既にタスクファイルが `tasks/` ディレクトリに存在します。**既存のタスクファイルをそのまま使用する場合は、このフェーズをスキップして「実装フェーズ」に進んでください。**

以下の場合にのみ、タスク分解フェーズを実行してください：
- SPECを修正した場合
- タスクファイルを再生成したい場合
- 新規プロジェクトの場合

**生成AIへの指示:**
```
@instructions/generate_tasks.md このインストラクションに従って、
プロジェクトルート: @projects/java/berry-books-api-sdd
SPEC: @projects/java/berry-books-api-sdd/specs/baseline/system/
から実装タスクリストを生成してください。

タスクリストは projects/java/berry-books-api-sdd/tasks/ ディレクトリに複数ファイルとして保存してください。

注意: タスクファイルの取り扱い
- タスクファイルが存在しない場合: 新規作成してください。
- タスクファイルが既に存在する場合: 既存の内容を確認し、SPECとの整合性をチェックして、必要な部分のみを更新してください。
```

**生成されるタスクファイル（複数人での並行作業用に分割）:**
- `tasks/tasks.md` - メインタスクリスト（全体概要と担当割り当て）
- `tasks/setup_tasks.md` - セットアップタスク（全員が実行）
- `tasks/common_tasks.md` - 共通機能タスク（共通機能チーム：1-2名）
- `tasks/F_001_book_search.md` - F_001: 書籍検索・閲覧（担当者A）
- `tasks/F_002_shopping_cart.md` - F_002: ショッピングカート管理（担当者B）
- `tasks/F_003_order_processing.md` - F_003: 注文処理（担当者C）
- `tasks/F_004_customer_auth.md` - F_004: 顧客管理・認証（担当者D）
- `tasks/F_005_order_history.md` - F_005: 注文履歴参照（担当者E）
- `tasks/integration_tasks.md` - 結合テストタスク（全員参加）

#### 実装フェーズ

**既存のタスクファイル（`tasks/`ディレクトリ）** を使って、生成AIに実装を依頼します。以下の順序で各タスクを**1つずつ**実行してください。

**重要:** 
- 各タスクは独立した作業単位です。1つのタスクが完了したら、次のタスクに進む前に内容を確認してください。
- タスクファイルが既に存在する場合は、そのファイルをそのまま使用できます。
- タスクファイルを更新したい場合は、前述の「タスク分解フェーズ」を実行してください。

##### タスク1: セットアップタスクの実行

**目的:** プロジェクト初期化、開発環境構築、データベース設定

```
@instructions/generate_code.md このインストラクションに従って、
プロジェクトルート: @projects/java/berry-books-api-sdd
タスクリスト: @projects/java/berry-books-api-sdd/tasks/setup_tasks.md
に基づいて実装を進めてください。

このタスクのみを実行し、完了したら停止してください。
```

**内容:**
- 開発環境のセットアップ
- データベース初期化（DDL、初期データ）
- アプリケーションサーバー設定
- ログ設定

##### タスク2: 共通機能タスクの実行

**目的:** 複数機能で共有される共通コンポーネントの実装

**依存:** タスク1（setup_tasks.md）完了後

```
@instructions/generate_code.md このインストラクションに従って、
プロジェクトルート: @projects/java/berry-books-api-sdd
タスクリスト: @projects/java/berry-books-api-sdd/tasks/common_tasks.md
に基づいて実装を進めてください。

このタスクのみを実行し、完了したら停止してください。
```

**内容:**
- 共通エンティティ（Book, Category, Publisher, Stock等）
- 共通サービス（DeliveryFeeService等）
- 共通ユーティリティ
- 認証フィルター

##### タスク3: F_001 書籍検索・閲覧の実装

**依存:** タスク2（common_tasks.md）完了後

```
@instructions/generate_code.md このインストラクションに従って、
プロジェクトルート: @projects/java/berry-books-api-sdd
タスクリスト: @projects/java/berry-books-api-sdd/tasks/F_001_book_search.md
に基づいて実装を進めてください。

このタスクのみを実行し、完了したら停止してください。
```

**内容:**
- 書籍検索機能
- 書籍詳細表示機能
- カバー画像表示対応

**注意:** タスク4～7と並行実行可能

##### タスク4: F_002 ショッピングカート管理の実装

**依存:** タスク2（common_tasks.md）完了後

```
@instructions/generate_code.md このインストラクションに従って、
プロジェクトルート: @projects/java/berry-books-api-sdd
タスクリスト: @projects/java/berry-books-api-sdd/tasks/F_002_shopping_cart.md
に基づいて実装を進めてください。

このタスクのみを実行し、完了したら停止してください。
```

**内容:**
- カートアイテム追加・削除・数量変更
- セッションベースのカート管理
- カート内容表示

**注意:** タスク3,5～7と並行実行可能

##### タスク5: F_003 注文処理の実装

**依存:** タスク2（common_tasks.md）完了後

```
@instructions/generate_code.md このインストラクションに従って、
プロジェクトルート: @projects/java/berry-books-mvc-sdd
タスクリスト: @projects/java/berry-books-mvc-sdd/tasks/F_003_order_processing.md
に基づいて実装を進めてください。

このタスクのみを実行し、完了したら停止してください。
```

**内容:**
- 注文確定処理
- 配送先・決済方法選択
- 在庫減算（楽観的ロック対応）
- 送料計算

**注意:** タスク3,4,6,7と並行実行可能

##### タスク6: F_004 顧客管理・認証の実装

**依存:** タスク2（common_tasks.md）完了後

```
@instructions/generate_code.md このインストラクションに従って、
プロジェクトルート: @projects/java/berry-books-api-sdd
タスクリスト: @projects/java/berry-books-api-sdd/tasks/F_004_customer_auth.md
に基づいて実装を進めてください。

このタスクのみを実行し、完了したら停止してください。
```

**内容:**
- 顧客登録機能
- ログイン・ログアウト機能
- 認証フィルター
- セッション管理

**注意:** タスク3～5,7と並行実行可能

##### タスク7: F_005 注文履歴参照の実装

**依存:** タスク2（common_tasks.md）完了後

```
@instructions/generate_code.md このインストラクションに従って、
プロジェクトルート: @projects/java/berry-books-api-sdd
タスクリスト: @projects/java/berry-books-api-sdd/tasks/F_005_order_history.md
に基づいて実装を進めてください。

このタスクのみを実行し、完了したら停止してください。
```

**内容:**
- 注文履歴一覧表示
- 注文詳細表示
- 顧客ごとのフィルタリング

**注意:** タスク3～6と並行実行可能

##### タスク8: 結合テストタスクの実行

**依存:** タスク3～7（全機能）完了後

```
@instructions/generate_code.md このインストラクションに従って、
プロジェクトルート: @projects/java/berry-books-api-sdd
タスクリスト: @projects/java/berry-books-api-sdd/tasks/integration_tasks.md
に基づいて実装を進めてください。

このタスクのみを実行し、完了したら停止してください。
```

**内容:**
- 機能間結合テスト
- エンドツーエンドテスト（Playwright推奨・個別実行）
- パフォーマンステスト
- 最終検証

> **Note:** Playwright E2Eテストは通常のビルド（`./gradlew test`）では実行されず、`./gradlew e2eTest` で個別に実行する設計です。

---

**実装の依存関係:**
```
タスク1（setup_tasks.md）
    ↓
タスク2（common_tasks.md）
    ↓
    ├─→ タスク3（F_001_book_search.md）─┐
    ├─→ タスク4（F_002_shopping_cart.md）├─→ 並行実行可能
    ├─→ タスク5（F_003_order_processing.md）│
    ├─→ タスク6（F_004_customer_auth.md）│
    └─→ タスク7（F_005_order_history.md）─┘
                ↓
    タスク8（integration_tasks.md）
```

**重要なポイント:**
- **依存関係の遵守**: セットアップ → 共通機能 → 機能別実装 → 結合テストの順序を守る
- **並行作業の判断**: タスク3～7は並行実行可能。人員配分は作業者が判断
- **進捗の追跡**: 各タスクファイルのチェックボックスで進捗を管理
- **SPEC参照**: 各タスク実行時、必要に応じて対応するSPEC（`specs/baseline/system/`、`specs/baseline/features/`）を参照

---

## 🚀 シナリオ2: 小規模改善・機能拡張案件

既に稼働しているシステムに対して、小規模な改善や機能追加を行う場合の開発フローです。

### 開発フロー

```
設計フェーズ（人間） → タスク分解フェーズ（AI） → 実装フェーズ（AI）
```

**重要:** SPECの作成は人間が行います。`templates/` フォルダのテンプレートか、既存の `specs/` のSPECを参考にしてください。

### 重要な違い

| 項目 | ベースプロジェクト（001） | 拡張（002以降） |
|------|------------|---------|
| 憲章 | 既に存在 | 既存を参照 |
| アーキテクチャ | 全体設計 | 既存に準拠 |
| SPECの配置 | `specs/baseline/` | `specs/enhancements/YYYYMM_案件名/` |
| 実装範囲 | 全レイヤー | 必要な部分のみ |
| SPEC作成 | テンプレート参照 | テンプレート参照 |

### 拡張のSPEC配置

拡張内容ごとに専用のフォルダを `specs/enhancements/YYYYMM_案件名/` に作成します。

**例:**
- `specs/enhancements/202512_inventory_alert/` - 在庫アラート機能（新機能）
- `specs/enhancements/202512_security_patch/` - セキュリティパッチ（修正）

> プロジェクト構成の詳細は「[📁 プロジェクト構成](#-プロジェクト構成)」を参照してください。

### 設計フェーズにおけるSPECの作成方法

**ステップ1: SPECを作成**

`specs/templates/` フォルダのテンプレートを使って、拡張内容のSPECを作成します。

例えば、`specs/enhancements/202512_inventory_alert/` に以下のファイルを作成：
- `requirements.md` - 要件定義書
- `functional_design.md` - 機能設計書
- `behaviors.md` - 振る舞い仕様書
- その他、必要に応じて

**ステップ2: テンプレートを参考に記述**

`specs/templates/` のテンプレートの構成を参考に、拡張内容の仕様を記述します。

**参考資料:**
- `specs/templates/` - 各SPECのテンプレート（構成と記述例）
- `specs/baseline/system/` - システム全体のSPEC（概要）
- `specs/baseline/features/` - 個別機能のSPEC（詳細設計の参考）

**ステップ3: 既存SPECとの整合性確認**

拡張内容のSPECが既存のアーキテクチャと整合していることを確認します：
- ベースプロジェクトの `specs/baseline/system/architecture_design.md` に準拠
- 既存機能の設計（`specs/baseline/features/`）を参考に
- 既存のクラスやテーブルとの連携を考慮
- 命名規則やコーディング規約を踏襲

---

## 🎯 実際の開発例（シナリオ2: 小規模改善・機能拡張案件）

### 例1: 在庫アラート機能の追加（新機能）

**前提条件:**
- システムが既に稼働している
- 既存の在庫管理機能（Stock エンティティ、楽観的ロック）がある
- 拡張内容は `specs/enhancements/202512_inventory_alert/` に配置

#### ステップ1: SPECの作成（手動）

`specs/templates/` フォルダのテンプレートを使って、`specs/enhancements/202512_inventory_alert/` に新機能のSPECを作成します：

**`specs/enhancements/202512_inventory_alert/requirements.md`:**
- 在庫数が閾値（例：5冊）を下回ったら管理者に通知
- 管理者画面で在庫アラート一覧を表示
- 書籍ごとに閾値を設定可能
- 在庫補充後、アラートを解除

**`specs/enhancements/202512_inventory_alert/functional_design.md`:**
- 既存のStockエンティティに閾値フィールドを追加
- 新規InventoryAlertエンティティとInventoryAlertDaoを設計
- 既存のOrderServiceでの在庫減少時にアラートチェックを追加

**`specs/enhancements/202512_inventory_alert/behaviors.md`:**
- Given-When-Then形式で受入基準を記述

**参考:** `specs/baseline/system/` （概要）、`specs/baseline/features/` （詳細設計）と `specs/templates/` のテンプレートを参考に記述してください。

#### ステップ2: タスク分解（生成AI）

**注意:** 既に当該拡張のタスクファイルが存在する場合（例: `specs/enhancements/202512_inventory_alert/tasks/`）は、このステップをスキップして既存のタスクファイルを使用できます。タスクファイルを更新したい場合や新規作成する場合のみ、以下を実行してください。

**生成AIへの指示:**
```
@instructions/generate_tasks.md このインストラクションに従って、
プロジェクトルート: @projects/java/berry-books-api-sdd
SPEC: @projects/java/berry-books-api-sdd/specs/enhancements/202512_inventory_alert/
から実装タスクリストを生成してください。

タスクリストは projects/java/berry-books-api-sdd/specs/enhancements/202512_inventory_alert/tasks/ ディレクトリに保存してください。

タスクファイルの取り扱い:
- タスクファイルが存在しない場合: 新規作成してください。
- タスクファイルが既に存在する場合: 既存の内容を確認し、SPECとの整合性をチェックして、必要な部分のみを更新してください。
```

**生成されるタスクファイル（拡張の規模に応じて）:**
- **小規模な拡張**: `tasks/tasks.md` のみ（1ファイル）
- **中規模以上の拡張**: 複数ファイルに分割（`tasks.md`, `setup_tasks.md`, `feature_tasks.md`等）

#### ステップ3: 実装（生成AI）

**既存のタスクファイル** または **新規作成したタスクファイル** を使って実装を進めます。

**生成AIへの指示:**
```
@instructions/generate_code.md このインストラクションに従って、
プロジェクトルート: @projects/java/berry-books-api-sdd
タスクリスト: @projects/java/berry-books-api-sdd/specs/enhancements/202512_inventory_alert/tasks/tasks.md
に基づいて実装を進めてください。

このタスクのみを実行し、完了したら停止してください。
```

**複数人で実装する場合:**
各担当者が個別のタスクファイルを指定して並行作業が可能です。

---

## 📊 技術スタック

| カテゴリ | 技術 | バージョン |
|---------|------|----------|
| **Java** | JDK | 21+ |
| **Jakarta EE** | Platform | 10.0 |
| **JSF** | Jakarta Faces | 4.0 |
| **JPA** | Jakarta Persistence | 3.1 |
| **アプリケーションサーバー** | Payara Server | 6 |
| **データベース** | HSQLDB | 2.7.x |
| **ビルドツール** | Gradle | 8.x+ |

## 🖼️ 画像リソース

**書籍カバー画像:**
- 画像ファイルは `images/covers/` に格納（約50冊分）
- セットアップ時に `webapp/resources/covers/` にコピー
- ファイル名は書籍名と対応（例: `Java SEディープダイブ.jpg`）
- 画像ファイルが存在しない場合は `no-image.jpg` を表示

**画像ファイル名の生成:**
- BookエンティティのgetImageFileName()メソッドで書籍名 + ".jpg" を返す
- JSFビューでは `#{book.imageFileName}` で参照
- 例: `<h:graphicImage value="resources/covers/#{book.imageFileName}"/>`

---

## 📖 主要ドキュメント

> 詳細なプロジェクト構成は「[📁 プロジェクト構成](#-プロジェクト構成)」を参照してください。

### 憲章とインストラクション

- **[constitution.md](memory/constitution.md)** - 開発憲章（開発原則、品質基準、組織標準）
  - AIはタスク生成・コード生成時にこれを参照します
- **[generate_tasks.md](instructions/generate_tasks.md)** - タスク分解インストラクション
- **[generate_code.md](instructions/generate_code.md)** - 実装インストラクション

### SPEC（仕様書）

- **[specs/baseline/system/](specs/baseline/system/)** - システム全体のSPEC（要件定義、アーキテクチャ設計、データモデル等）
- **[specs/baseline/features/](specs/baseline/features/)** - 個別機能のSPEC（F_001～F_005）
- **[specs/templates/](specs/templates/)** - 拡張SPEC作成用テンプレート

### 実装タスク

- **[tasks/tasks.md](tasks/tasks.md)** - メインタスクリスト（全体概要）
- **[tasks/](tasks/)** - 機能別タスクファイル（setup、common、F_001～F_005、integration）

---

## 🔗 関連リンク

- [Jakarta EE 10 Platform](https://jakarta.ee/specifications/platform/10/)
- [Payara Server Documentation](https://docs.payara.fish/)
- [PlantUML](https://plantuml.com/)
- [Mermaid](https://mermaid.js.org/)
