# back-office-api-sdd-wf プロジェクト

## 📖 概要

Jakarta EE 10とJAX-RS (Jakarta RESTful Web Services) 3.1を使用したオンライン書店「Berry Books」のバックオフィスAPIアプリケーションです。
書籍・在庫・カテゴリ・出版社の完全なデータ管理をREST APIとして提供するマイクロサービスです。

> Note: このプロジェクトは仕様駆動開発（SDD: Specification-Driven Development）の研修用プロジェクトです。

> SDDとは:
> - 詳細な仕様書（specs/）に基づいて、段階的にコードを生成する手法
> - AIを活用して、ドメイン単位で段階的に実装を進める
> - 憲章（principles/）に定められた設計原則とベストプラクティスに従う
> - 汎用Agent Skills (`agent_skills/jakarta-ee-api-base/`) を使用した開発

## 🤖 Agent Skillsを使った開発

このプロジェクトは、汎用的な Jakarta EE マイクロサービス開発 Agent Skills を使用して開発します。

開発は以下の6段階プロセスで進めます（ドメイン単位）：

```
ステップ1: 基本設計（ドメイン構造決定）← AIと対話しながら
    ↓
ステップ2: 詳細設計（ドメイン単位）← AIと対話しながら
    ↓
ステップ3: コード生成（ドメイン単位：実装コード + 単体テスト）
    ↓
ステップ4: 単体テスト実行評価（テスト実行 → カバレッジ分析 → フィードバック）
    ↓
ステップ5: 結合テスト生成（basic_design/{domain}/behaviors.md → JUnit + Weld SE）
    ↓
ステップ6: E2Eテスト生成（requirements/behaviors.md → REST Assured）
```

**ドメイン構成:**
- `common/` - 共通ドメイン（Entity, Dao, JWT等。最優先実装）
- `books/` - 書籍管理ドメイン
- `categories/` - カテゴリ管理ドメイン
- `publishers/` - 出版社管理ドメイン
- `stocks/` - 在庫管理ドメイン
- `workflows/` - ワークフロー管理ドメイン

---

### 📋 開発フロー

#### ステップ1: 基本設計（プロジェクト開始時の1フェーズ、AIと対話的に実施）

requirements.mdから、ドメイン単位の仕様書をAIと対話しながら作成・更新します。

**重要**: このステップは要件変更時に何度でも実行できます。初回は新規作成、2回目以降は既存SPECの差分更新となります。

```
@agent_skills/jakarta-ee-api-base/instructions/basic_design.md

仕様書を作成してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api
* spec_directory: projects/sdd-wf/bookstore/back-office-api/specs/baseline
```

* 対話の流れ:
  1. 既存SPECファイルの有無を確認し、実行モードを判定します（初回作成 or 増分更新）
  2. **増分更新モードの場合**:
     - 既存SPECファイルをすべて読み込みます
     - インプットファイル（requirements.md等）の変更点を特定します
     - 差分に関連する箇所のみをSPECファイルに反映します（変更のない箇所は一切触りません）
  3. **初回作成モードの場合**:
     - 既存資料（EXCEL、Word等）の有無を確認します
     - 既存資料がある場合は、Markdown形式に変換します
     - ドメイン構成を決定します（common, books, categories, publishers, stocks, workflows）
     - テンプレートを展開し、各ドメインの仕様書を対話的に作成します
       * @agent_skills/jakarta-ee-api-base/templates/basic_design/ から5ファイルをコピー
     - `specs/baseline/basic_design/{domain}/*.md` が生成されます

* テンプレート:
  - architecture_design.md - アーキテクチャ設計書
  - data_model.md - データモデル仕様書
  - external_interface.md - 外部インターフェース仕様書
  - functional_design.md - 機能設計書
  - behaviors.md - 振る舞い仕様書（結合テスト用）

* 生成されるファイル: 
  ```
  specs/baseline/basic_design/
  ├── common/                  # 共通ドメイン（最優先実装）
  │   ├── architecture_design.md
  │   ├── data_model.md
  │   ├── external_interface.md
  │   ├── functional_design.md
  │   └── behaviors.md
  ├── books/                   # 書籍管理ドメイン
  │   ├── functional_design.md
  │   └── behaviors.md
  ├── categories/              # カテゴリ管理ドメイン
  │   ├── functional_design.md
  │   └── behaviors.md
  ├── publishers/              # 出版社管理ドメイン
  │   ├── functional_design.md
  │   └── behaviors.md
  ├── stocks/                  # 在庫管理ドメイン
  │   ├── functional_design.md
  │   └── behaviors.md
  └── workflows/               # ワークフロー管理ドメイン
      ├── functional_design.md
      └── behaviors.md
  ```

---

#### ステップ2: 詳細設計（ドメイン単位、commonから順に実施、AIと対話的に実施）

**重要**: commonドメインを最優先で実施してください（他のドメインはcommonに依存）。

コマンドテンプレート（commonドメインの例）:

```
@agent_skills/jakarta-ee-api-base/instructions/detailed_design.md

commonドメインの詳細設計書を作成してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api
* spec_directory: projects/sdd-wf/bookstore/back-office-api/specs/baseline
* target_domain: common
```

対話の流れ:
1. AIがSPEC（basic_design/{target_domain}/）を読み込み、理解した内容を説明します
2. テンプレートを展開します
   * @agent_skills/jakarta-ee-api-base/templates/detailed_design/ から2ファイルをコピー
3. AIが不明点を質問します
4. あなたが回答します
5. detailed_design.md（実装クラス設計）とbehaviors.md（単体テスト用）を生成します
   * **簡潔性の原則**: 基本設計とコードの「橋渡し」となる設計判断のみを簡潔に記載
   * クラス名と責務、主要メソッドのシグネチャ、設計判断を示すアノテーション等
   * **実装詳細（処理ステップ等）は記載しない**（コードレビューで修正しやすくするため）
6. `specs/baseline/detailed_design/{target_domain}/detailed_design.md` と `behaviors.md` が生成されます

テンプレート:
* detailed_design.md - 詳細設計書（実装クラス設計）
* behaviors.md - 振る舞い仕様書（単体テスト用）

注意:
* commonドメインを最優先で実施してください（他のドメインはcommonに依存）
* 各ドメインの詳細設計が完了したら、次のドメインに進みます

---

#### ステップ3: コード生成（ドメイン単位、commonから順に実施）

**重要**: commonドメインを最優先で実施してください（他のドメインはcommonに依存）。

> 単体テストの方針: ドメイン内のコンポーネント間は実際の連携をテスト。ドメイン外の依存関係のみモック化。

コマンドテンプレート:

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

[ドメイン名]ドメインを実装してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api
* spec_directory: projects/sdd-wf/bookstore/back-office-api/specs/baseline
* target_domain: common
```

使用例（commonドメイン）:

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

commonドメインを実装してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api
* spec_directory: projects/sdd-wf/bookstore/back-office-api/specs/baseline
* target_domain: common
```

使用例（他のドメイン）:

```
@agent_skills/jakarta-ee-api-base/instructions/code_generation.md

booksドメインを実装してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api
* spec_directory: projects/sdd-wf/bookstore/back-office-api/specs/baseline
* target_domain: books
```

注意:
* commonドメインを最初に実装してください（他のドメインはcommonに依存）
* 各ドメインの実装には、本番コード生成と単体テスト生成の両方が含まれます

---

#### ステップ4: 単体テスト実行評価

単体テストを実行してカバレッジを分析し、品質を検証します。

```
@agent_skills/jakarta-ee-api-base/instructions/unit_test_execution.md

単体テストを実行してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api
* target_domain: books
```

AIが：
1. 🧪 テスト実行（gradle test jacocoTestReport）
2. 📊 テスト結果とカバレッジ分析
3. 🔍 問題の分類（テスト失敗、必要な振る舞い、デッドコード）
4. 📋 フィードバックレポート生成
5. 💬 ユーザーに推奨アクションを提示

重要：
* 問題を発見してもユーザー確認なしに修正しない
* カバレッジ不足やデッドコードを具体的に提案
* 必要に応じてステップ2（詳細設計）に戻ってループ

🔄 フィードバックループ:
```
詳細設計 → コード生成 → テスト実行評価
    ↑                         ↓
    └──── フィードバック ←────┘
```

---

#### ステップ5: 結合テスト生成（単体テスト完了後）

単体テスト完了後に、結合テスト（Integration Test）を生成します。

```
@agent_skills/jakarta-ee-api-base/instructions/it_generation.md

結合テストを生成してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api
* spec_directory: projects/sdd-wf/bookstore/back-office-api/specs/baseline
* target_domains: all
```

AIが：
1. 📄 basic_design/{domain}/behaviors.md（結合テストシナリオ）を読み込む
2. 🧪 JUnit 5 + Weld SE を使用した結合テストを生成
   * Service層以下（Service + DAO + Entity + DB）の連携テスト
   * 実際のDBアクセス（メモリDB）
   * 外部APIはWireMockでスタブ化
   * アプリケーションサーバー不要
3. 🏷️ `@Tag("integration")` で結合テストを分離
4. 📂 各ドメインのシナリオから結合テストを生成

実行方法:
```bash
# 結合テストを実行
./gradlew integrationTest
```

---

#### ステップ6: E2Eテスト生成（実装完了後）

全機能実装完了後に、E2Eテスト（End-to-End Test）を生成します。

```
@agent_skills/jakarta-ee-api-base/instructions/e2e_test_generation.md

E2Eテストを生成してください

パラメータ:
* project_root: projects/sdd-wf/bookstore/back-office-api
* spec_directory: projects/sdd-wf/bookstore/back-office-api/specs/baseline
```

AIが：
1. 📄 requirements/behaviors.md（E2Eテストシナリオ）を読み込む
2. 🧪 REST Assured を使用したE2Eテストを生成
   * 複数API間の連携テスト（認証 → 書籍検索 → 在庫更新等）
   * 実際のHTTPリクエスト/レスポンス
   * 実際のDBアクセスを含む
3. 🏷️ `@Tag("e2e")` でE2Eテストを分離

実行方法:
```bash
# アプリケーションサーバーを起動
./gradlew run

# 別ターミナルでE2Eテストを実行
./gradlew e2eTest
```

---

### 🔄 基本設計変更対応（手戻り・拡張案件）

結合テストやE2Eテストで不具合が見つかり、基本設計に戻る必要がある場合や、拡張案件で新機能を追加する場合に使用します。

#### 使用方法

1. 基本設計SPECのマスターファイルを更新
   ```bash
   vim specs/baseline/basic_design/functional_design.md
   vim specs/baseline/basic_design/data_model.md
   ```

2. CHANGES.mdを作成して変更内容を記載
   ```bash
   cp agent_skills/jakarta-ee-api-base/templates/basic_design/CHANGES_template.md \
      specs/baseline/basic_design/CHANGES.md
   vim specs/baseline/basic_design/CHANGES.md
   ```

3. 変更対応を実行
   ```
   @agent_skills/jakarta-ee-api-base/instructions/basic_design_change.md
   
   基本設計の変更を適用してください
   
   パラメータ:
   * project_root: projects/sdd-wf/bookstore/back-office-api
   * spec_directory: projects/sdd-wf/bookstore/back-office-api/specs/baseline
   ```

AIが：
1. 📄 CHANGES.md（変更差分ファイル）を読み込み
2. 🔍 変更の影響を受けるドメインを識別
3. 🎯 既存の指示書を呼び出して、影響を受けるドメインの設計・コード・テストを更新
4. ✅ すべての変更適用後、CHANGES.mdをアーカイブ

#### ディレクトリ構造

```
specs/baseline/basic_design/
  ├── common/
  │   ├── functional_design.md  # マスター（自由に編集）
  │   ├── data_model.md         # マスター（自由に編集）
  │   └── CHANGES.md            # アクティブな変更（未適用）
  ├── books/
  │   └── functional_design.md  # マスター（自由に編集）
  └── changes_archive/          # 履歴
      ├── 20260118_book_discount.md
      └── 20260125_stock_alert.md
```

---

### 📚 詳細情報

詳細は `@agent_skills/jakarta-ee-api-base/README.md` を参照してください。

#### 開発原則

このプロジェクトは、以下の原則に従って開発されます：

* 場所: `@agent_skills/jakarta-ee-api-base/principles/`
  * [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](../../../agent_skills/jakarta-ee-api-base/principles/security.md) - セキュリティ標準
  * [common_rules.md](../../../agent_skills/jakarta-ee-api-base/principles/common_rules.md) - 共通ルール

* 主な内容:
  * 標準技術スタック（Jakarta EE 10、JPA 3.1、JAX-RS 3.1）
  * レイヤードアーキテクチャ（API、Service、DAO、Entity）
  * 開発標準（命名規則、コーディング規約、バリデーション、エラーハンドリング）
  * セキュリティ実装（JWT認証、認証フィルター）
  * トランザクション管理と並行制御（楽観的ロック）
  * テスト戦略、パフォーマンス考慮事項

## 🎯 プロジェクトの特徴（マイクロサービスパターン）

### アーキテクチャ
* 独立したデータ管理サービス: 書籍・在庫・カテゴリ・出版社の完全管理
* マイクロサービス: berry-books-apiから呼ばれるバックエンドサービス
* REST API: データ管理機能をREST APIとして提供
* CORS対応: クロスオリジンリクエストに対応

### 実装する全エンティティ
* ✅ Book（書籍）
* ✅ Stock（在庫）- 楽観的ロック必須（@Version）
* ✅ Category（カテゴリ）
* ✅ Publisher（出版社）

### 重要な実装要件

#### 楽観的ロック（Optimistic Locking）
* Stockエンティティに`@Version`アノテーション使用
* 在庫更新時の競合を検出
* `OptimisticLockException` → HTTP 409 Conflict

#### 2種類の書籍検索実装
* JPQL検索（`BookDao`）: 動的クエリ、シンプル
* Criteria API検索（`BookDaoCriteria`）: 型安全、コンパイル時チェック
* 両方実装: 比較学習が可能

#### CORS設定
* berry-books-apiからのクロスオリジンリクエスト対応
* `CorsFilter`実装

## 🔧 使用している技術

### 本番環境

* Jakarta EE 10
* Payara Server 6
* JAX-RS (Jakarta RESTful Web Services) 3.1 - REST API
* Jakarta Persistence (JPA) 3.1 - Hibernate実装
* Jakarta Transactions (JTA)
* Jakarta CDI 4.0
* Jakarta Bean Validation 3.0
* HSQLDB 2.7.x

### テスト環境

* JUnit 5 - テストフレームワーク
* Mockito - モックライブラリ
* JaCoCo - カバレッジツール（オプション）

## プロジェクト構成

```
back-office-api/
├── specs/                          # 仕様書（SDD）
│   ├── baseline/
│   │   ├── requirements/           # システム要件
│   │   │   ├── requirements.md    # 要件定義書
│   │   │   └── behaviors.md       # E2Eテスト用（要件を外形的に捉えた振る舞い）
│   │   ├── basic_design/           # 基本設計SPEC（ドメイン単位）
│   │   │   ├── common/            # 共通ドメイン（最優先実装）
│   │   │   │   ├── architecture_design.md
│   │   │   │   ├── data_model.md
│   │   │   │   ├── external_interface.md
│   │   │   │   ├── functional_design.md
│   │   │   │   └── behaviors.md   # 結合テスト用
│   │   │   ├── books/             # 書籍管理ドメイン
│   │   │   │   ├── functional_design.md
│   │   │   │   └── behaviors.md
│   │   │   ├── categories/        # カテゴリ管理ドメイン
│   │   │   │   ├── functional_design.md
│   │   │   │   └── behaviors.md
│   │   │   ├── publishers/        # 出版社管理ドメイン
│   │   │   │   ├── functional_design.md
│   │   │   │   └── behaviors.md
│   │   │   ├── stocks/            # 在庫管理ドメイン
│   │   │   │   ├── functional_design.md
│   │   │   │   └── behaviors.md
│   │   │   └── workflows/         # ワークフロー管理ドメイン
│   │   │       ├── functional_design.md
│   │   │       └── behaviors.md
│   │   └── detailed_design/        # 詳細設計SPEC（ドメイン単位）
│   │       ├── common/
│   │       │   ├── detailed_design.md
│   │       │   └── behaviors.md   # 単体テスト用
│   │       ├── books/
│   │       │   ├── detailed_design.md
│   │       │   └── behaviors.md
│   │       ├── categories/
│   │       │   ├── detailed_design.md
│   │       │   └── behaviors.md
│   │       ├── publishers/
│   │       │   ├── detailed_design.md
│   │       │   └── behaviors.md
│   │       ├── stocks/
│   │       │   ├── detailed_design.md
│   │       │   └── behaviors.md
│   │       └── workflows/
│   │           ├── detailed_design.md
│   │           └── behaviors.md
│   └── enhancements/               # 機能拡張仕様
├── principles/                     # 開発憲章
│   └── constitution.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── pro/kensait/backoffice/
│   │   │       ├── api/              # JAX-RS Resources
│   │   │       │   ├── dto/          # API DTOs (Records)
│   │   │       │   └── exception/    # Exception Mappers
│   │   │       ├── service/          # Business Logic
│   │   │       ├── dao/              # Data Access Objects
│   │   │       ├── entity/           # JPA Entities (Book, Stock, Category, Publisher)
│   │   │       ├── util/             # Utilities
│   │   │       └── FUNC_001_infrastructure/   # Infrastructure
│   │   ├── resources/
│   │   │   ├── META-INF/
│   │   │   │   └── persistence.xml
│   │   │   ├── db/
│   │   │   │   ├── schema.sql
│   │   │   │   └── sample_data.sql
│   │   │   └── log4j2.xml
│   │   └── webapp/
│   │       └── WEB-INF/
│   │           └── web.xml
│   └── test/
│       └── java/
│           └── pro/kensait/backoffice/
├── build.gradle
└── README.md
```

## API仕様

### 書籍API (`/api/books`)

| メソッド | エンドポイント | 説明 |
|---------|--------------|------|
| GET | `/api/books` | 全書籍取得 |
| GET | `/api/books/{id}` | 書籍詳細取得 |
| GET | `/api/books/search/jpql` | 書籍検索（JPQL） |
| GET | `/api/books/search/criteria` | 書籍検索（Criteria API） |
| POST | `/api/books` | 書籍登録 |
| PUT | `/api/books/{id}` | 書籍更新 |
| DELETE | `/api/books/{id}` | 書籍削除 |

### 在庫API (`/api/stocks`)

| メソッド | エンドポイント | 説明 | 注意 |
|---------|--------------|------|-----|
| GET | `/api/stocks` | 全在庫取得 | |
| GET | `/api/stocks/{bookId}` | 在庫取得 | |
| PUT | `/api/stocks/{bookId}` | 在庫更新 | 楽観的ロック対応 |

* 重要: 在庫更新時は`version`パラメータが必須。競合時はHTTP 409 Conflictを返す。

### カテゴリAPI (`/api/categories`)

| メソッド | エンドポイント | 説明 |
|---------|--------------|------|
| GET | `/api/categories` | 全カテゴリ取得 |
| GET | `/api/categories/{id}` | カテゴリ詳細取得 |

### 出版社API (`/api/publishers`)

| メソッド | エンドポイント | 説明 |
|---------|--------------|------|
| GET | `/api/publishers` | 全出版社取得 |
| GET | `/api/publishers/{id}` | 出版社詳細取得 |

## 🚀 セットアップとコマンド実行ガイド

### 前提条件

* JDK 21以上
* Gradle 8.x以上
* Payara Server 6（プロジェクトルートの`payara6/`に配置）
* HSQLDB（プロジェクトルートの`hsqldb/`に配置）

> Note: ① と ② の手順は、ルートの`README.md`を参照してください。

### ③ 依存関係の確認

このプロジェクトを開始する前に、以下が起動していることを確認してください：

* ① HSQLDBサーバー （`./gradlew startHsqldb`）
* ② Payara Server （`./gradlew startPayara`）

### ④ プロジェクトを開始するときに1回だけ実行

```bash
# 1. データベーステーブルとデータを作成
./gradlew :back-office-api-sdd-wf:setupHsqldb

# 2. プロジェクトをビルド
./gradlew :back-office-api-sdd-wf:war

# 3. プロジェクトをデプロイ
./gradlew :back-office-api-sdd-wf:deploy
```

> 重要: `setupHsqldb`を実行すると、`src/main/resources/db/schema.sql`と`sample_data.sql`が実行されます。

### ⑤ プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# プロジェクトをアンデプロイ
./gradlew :back-office-api-sdd-wf:undeploy
```

### ⑥ アプリケーション作成・更新のたびに実行

```bash
# アプリケーションを再ビルドして再デプロイ
./gradlew :back-office-api-sdd-wf:war
./gradlew :back-office-api-sdd-wf:deploy
```

## 📍 APIエンドポイント

デプロイ後、以下のベースURLでAPIにアクセスできます：

* ベースURL: http://localhost:8080/back-office-api-sdd-wf/api
* ウェルカムページ: http://localhost:8080/back-office-api-sdd-wf/

## 📝 APIの使用例（curl）

### 1. 全書籍取得

```bash
curl -X GET http://localhost:8080/back-office-api-sdd-wf/api/books
```

### 2. 書籍検索（JPQL）

```bash
curl -X GET "http://localhost:8080/back-office-api-sdd-wf/api/books/search/jpql?keyword=Java&categoryId=1"
```

### 3. 書籍検索（Criteria API）

```bash
curl -X GET "http://localhost:8080/back-office-api-sdd-wf/api/books/search/criteria?keyword=Java&categoryId=1"
```

### 4. 在庫取得

```bash
curl -X GET http://localhost:8080/back-office-api-sdd-wf/api/stocks/1
```

### 5. 在庫更新（楽観的ロック）

```bash
curl -X PUT http://localhost:8080/back-office-api-sdd-wf/api/stocks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 50,
    "version": 0
  }'
```

> 重要: `version`パラメータが異なる場合、HTTP 409 Conflictが返されます。

### 6. カテゴリ一覧取得

```bash
curl -X GET http://localhost:8080/back-office-api-sdd-wf/api/categories
```

## 🧪 テスト

### テストの実行

このプロジェクトには、サービス層のユニットテストが含まれています。テストはJUnit 5とMockitoを使用して実装されています。

#### すべてのテストを実行

```bash
./gradlew :back-office-api-sdd-wf:test
```

#### 特定のテストクラスを実行

```bash
# BookServiceのテストのみを実行
./gradlew :back-office-api-sdd-wf:test --tests "*BookServiceTest"

# StockServiceのテストのみを実行（楽観的ロックテスト含む）
./gradlew :back-office-api-sdd-wf:test --tests "*StockServiceTest"
```

#### テストの継続的実行（変更検知）

```bash
./gradlew :back-office-api-sdd-wf:test --continuous
```

### テストレポートの確認

テスト実行後、HTMLレポートが生成されます：

```
projects/sdd-wf/bookstore/back-office-api-wf/build/reports/tests/test/index.html
```

ブラウザで開くとテスト結果の詳細が確認できます。

### テストカバレッジの確認（JaCoCo）

```bash
# テストカバレッジレポートを生成
./gradlew :back-office-api-sdd-wf:jacocoTestReport

# カバレッジレポートの場所
# projects/sdd-wf/bookstore/back-office-api-wf/build/reports/jacoco/test/html/index.html
```

## 📚 アーキテクチャ

### レイヤー構成

```
berry-books-api
    ↓ HTTP/JSON
JAX-RS Resource (@Path, @ApplicationScoped)
    ↓ CORS Filter
CDI Service (@ApplicationScoped)
    ↓
DAO (@ApplicationScoped)
    ↓ JPA
Database (HSQLDB)
```

注: このAPIはberry-books-apiから呼ばれるマイクロサービスです。

### 主要な設計パターン

* REST Resource Pattern: JAX-RS
* Service Layer Pattern: CDI + Transactional
* Repository Pattern: DAO
* DTO Pattern: Java Records
* Dependency Injection: CDI
* Optimistic Locking: `@Version`（在庫管理）
* Exception Mapper: JAX-RS
* CORS Filter: クロスオリジン対応

### 楽観的ロック制御

在庫テーブル（`STOCK`）に`@Version`カラムを使用し、更新時の同時更新による不整合を防止します。

### トランザクション管理

`StockService.updateStock()`メソッドに`@Transactional`を適用し、在庫更新をアトミックに実行します。

## 📝 データソース設定について

このプロジェクトはルートの`build.gradle`で定義されたタスクを使用してデータソースを作成します。

### 設定内容

* JNDI名: `jdbc/HsqldbDS`
* データベース: `testdb`
* ユーザー: `SA`
* パスワード: （空文字）
* TCPサーバー: `localhost:9001`

データソースはPayara Serverのドメイン設定に登録されます。

### ⚠️ 注意事項

* HSQLDB Databaseサーバーが起動している必要があります
* データソース作成はPayara Server起動後に実行してください
* 初回のみ実行が必要です（2回目以降は不要）

## 🛑 アプリケーションを停止する

### アプリケーションのアンデプロイ

```bash
./gradlew :back-office-api-sdd-wf:undeploy
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

> Note: WindowsではGit Bashを使用してください。

## 🧪 データベースのリセット

データベースを初期状態に戻したい場合：

```bash
# HSQLDBサーバーを停止
./gradlew stopHsqldb

# データファイルを削除
rm -f hsqldb/data/testdb.*

# HSQLDBサーバーを再起動
./gradlew startHsqldb

# 初期データをセットアップ
./gradlew :back-office-api-sdd-wf:setupHsqldb
```

## 🧹 SDD成果物のクリーンアップ

仕様駆動開発により何度でも再実装できます。詳細は [ルートREADMEのSDDクリーンアップ節](../../../README.md#仕様駆動開発sddプロジェクトの成果物クリーンアップ) を参照してください。

```bash
# 詳細設計SPECのみ削除
./gradlew :back-office-api-sdd-wf:cleanDetailedDesign

# 本番コード・単体テストコードを削除（src/main/, src/test/, build/）
./gradlew :back-office-api-sdd-wf:cleanCode

# すべて削除（requirements/, basic_design/ は保護）
./gradlew :back-office-api-sdd-wf:cleanAllSdd
```

* cleanCode の削除対象: 本番コード（src/main/）、単体テストコード（src/test/）、ビルド成果物（build/）。ディレクトリ構造は空で保持されます。
* 保護されるSPEC: `specs/baseline/requirements/`, `specs/baseline/basic_design/`

## 📖 参考リンク

### Agent Skills

* [Agent Skills README](../../../agent_skills/jakarta-ee-api-base/README.md) - 使い方ガイド
* [開発原則](../../../agent_skills/jakarta-ee-api-base/principles/)
  * [architecture.md](../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
  * [security.md](../../../agent_skills/jakarta-ee-api-base/principles/security.md) - セキュリティ標準
  * [common_rules.md](../../../agent_skills/jakarta-ee-api-base/principles/common_rules.md) - 共通ルール

### Jakarta EE仕様

* [Jakarta EE 10 Platform](https://jakarta.ee/specifications/platform/10/)
* [Jakarta RESTful Web Services 3.1](https://jakarta.ee/specifications/restful-ws/3.1/)
* [Jakarta Persistence 3.1](https://jakarta.ee/specifications/persistence/3.1/)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。
