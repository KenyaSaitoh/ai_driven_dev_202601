# コード実装インストラクション

## パラメータ設定

**実行前に以下のパラメータを設定してください:**

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
task_file: "ここに実行するタスクファイルのパスを入力"
skip_infrastructure: false  # trueの場合、インフラセットアップをスキップ
```

**例（berry-books-apiの場合）:**
```yaml
project_root: "projects/sdd/bookstore/berry-books-api-sdd"
task_file: "projects/sdd/bookstore/berry-books-api-sdd/tasks/setup_tasks.md"
skip_infrastructure: true  # インフラセットアップをスキップ
```

**注意:** 
- パス区切りはOS環境に応じて調整してください（Windows: `\`, Unix/Linux/Mac: `/`）
- 以降、`{project_root}` と表記されている箇所は、上記で設定した値に置き換えてください

---

## 実装の実行

**重要: 指定されたタスクファイルのタスクのみを実行し、完了したら停止してください。次のタスクに自動的に進んではいけません。**

パラメータとして指定されたプロジェクトルートとタスクファイルに基づいて、以下を実行してください：

### 1. 実装コンテキストをロードして分析してください

#### 読み込むべきドキュメント（優先順）

1. **最優先**: `{project_root}/principles/` 配下の憲章ファイルでプロジェクトの開発原則、アーキテクチャ方針、品質基準、組織標準を確認

2. **必須**: 指定されたタスクファイルで完全なタスクリストと実行計画を確認
   - タスクの「参照SPEC」はMarkdownリンク形式で記述されています（クリック可能）
   - リンク先のSPECファイルと指定されたセクションを必ず参照してください

3. **必須**: `{project_root}/specs/baseline/system/architecture_design.md` で以下を確認：
   - 技術スタック（言語、バージョン、フレームワーク、ライブラリ）
   - アーキテクチャパターンとレイヤー構成
   - パッケージ構造と命名規則
   - デザインパターン、トランザクション戦略、並行制御
   - ログ戦略、エラーハンドリング、セキュリティ
   - テスト戦略（テストフレームワーク、カバレッジ目標、テスト方針）
   - **コード生成時は、ここで定義された技術スタックを厳密に遵守すること**

4-10. （back-office-apiと同じため省略）

**注意**: `{project_root}` は、パラメータで明示的に指定されたプロジェクトルートのパスに置き換えてください。

### 2-4. （back-office-apiと同じため省略）

### 5-6. （テスト生成ガイドライン - back-office-apiと同じため省略）

---

## コンポーネント別の参照ドキュメント優先度と使用方法

（back-office-apiと同じため省略）

---

## 進捗追跡とエラーハンドリング

（back-office-apiと同じため省略）

---

## 完了検証

（back-office-apiと同じため省略）

---

## BFFパターン特有の実装要件

### berry-books-apiのアーキテクチャ特性

**BFF（Backend for Frontend）としての役割:**
- フロントエンド（berry-books-spa）の唯一のエントリーポイント
- 複数のバックエンドマイクロサービスを統合
- フロントエンドに最適化されたAPIを提供

### 実装パターンの区別

#### プロキシパターン（外部APIへの透過的転送）
以下のResourceは独自のビジネスロジックを持たず、外部APIへ転送のみを行います：

**BookResource (プロキシ → back-office-api)**:
- GET /api/books → back-office-api
- GET /api/books/{id} → back-office-api
- GET /api/books/search/jpql → back-office-api
- GET /api/books/search/criteria → back-office-api
- **実装方法**: BackOfficeRestClient経由でそのまま転送
- **注意**: BookService、BookDao、Bookエンティティは実装しない

**CategoryResource (プロキシ → back-office-api)**:
- GET /api/categories → back-office-api
- **実装方法**: BackOfficeRestClient経由でそのまま転送
- **注意**: CategoryService、CategoryDao、Categoryエンティティは実装しない

#### 独自実装パターン（ビジネスロジックを持つ）

**AuthenResource (独自実装 + customer-hub-api連携)**:
- JWT生成・検証はBFF層で実装
- 顧客情報取得はcustomer-hub-api経由
- **実装**: AuthenResource、JwtUtil、JwtAuthenFilter、AuthenContext
- **外部連携**: CustomerHubRestClient
- **注意**: Customerエンティティは実装しない（外部API管理）

**OrderResource (独自実装 + 外部API連携)**:
- 注文処理のビジネスロジックを実装
- 在庫チェック・更新はback-office-api経由
- 注文データはローカルDBで管理
- **実装**: OrderResource、OrderService、OrderTranDao、OrderDetailDao
- **エンティティ**: OrderTran、OrderDetail、OrderDetailPK（これらのみ実装）
- **外部連携**: BackOfficeRestClient（在庫管理）

**ImageResource (独自実装)**:
- WAR内リソース（画像ファイル）を直接配信
- ServletContext.getResourceAsStream()を使用
- **実装**: ImageResource

### 実装してはいけないコンポーネント

以下のコンポーネントは外部APIで管理されるため、**berry-books-apiでは実装しません**：

**エンティティ（実装しない）**:
- ❌ Book エンティティ
- ❌ Stock エンティティ
- ❌ Category エンティティ
- ❌ Publisher エンティティ
- ❌ Customer エンティティ

**DAO（実装しない）**:
- ❌ BookDao
- ❌ StockDao
- ❌ CategoryDao
- ❌ PublisherDao
- ❌ CustomerDao

**Service（実装しない）**:
- ❌ BookService
- ❌ CategoryService

### 外部API連携コンポーネント（必須実装）

**BackOfficeRestClient**:
- back-office-apiとの連携を担当
- 書籍・在庫・カテゴリ情報の取得
- 在庫更新（楽観的ロック対応）
- **設定**: `back-office-api.base-url` プロパティ
- **参照SPEC**: external_interface.mdの「14. back-office-api連携」

**CustomerHubRestClient**:
- customer-hub-apiとの連携を担当
- 顧客情報の取得・登録
- **設定**: `customer-hub-api.base-url` プロパティ
- **参照SPEC**: external_interface.mdの「3. customer-hub-api連携」

### データモデル制約

**実装するエンティティ（注文関連のみ）**:
- ✅ OrderTran（注文トランザクション）
- ✅ OrderDetail（注文明細）
- ✅ OrderDetailPK（注文明細複合主キー）

**データベーステーブル管理**:
- ORDER_TRAN、ORDER_DETAILテーブルのみ直接管理
- 他のテーブル（BOOK、STOCK等）は外部APIが管理

### トランザクション管理の注意点

**分散トランザクション**:
- 在庫更新: back-office-apiの独立トランザクション
- 注文作成: berry-books-apiのJTAトランザクション
- **結果整合性**: 在庫更新成功後、注文作成失敗のケースに注意
- **参照SPEC**: architecture_design.mdの「6. トランザクション管理（BFFパターン）」

---

## 重要な注意事項

### タスクの実行範囲
- このインストラクションは、タスクファイルに完全なタスク分解が存在することを前提としています
- タスクが不完全または欠落している場合は、まず `task_generation.md` インストラクションを使用してタスクリストを生成してください
- **指定されたタスクファイルのタスクのみを実行してください。他のタスクファイル（例: 次の機能のタスク）に自動的に進んではいけません。**
- タスクは分業の単位です。1つのタスクが完了したら、次のタスクに進む前にユーザーの確認を待ってください。

### REST API特有の注意点
- 画面（UI）は含まれないため、View/XHTMLの実装は行わない
- エンドポイントのテストにはREST AssuredまたはJAX-RS Clientを使用
- JWT認証、CORS、HTTPステータスコードの適切な使用を考慮
- リクエスト/レスポンスのJSON形式のバリデーションを実装

### プロジェクトルートの扱い
- `{project_root}` は、パラメータで明示的に指定されたパスに置き換えてください
- 相対パスでも絶対パスでも構いません
- 全てのファイル操作は、このプロジェクトルートを基準に行います
