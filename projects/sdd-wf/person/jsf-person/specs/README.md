# jsf-person プロジェクト - Specs フォルダ構成ガイド

プロジェクトID: jsf-person  
最終更新日: 2026-02-06

---

## 概要

このドキュメントは、jsf-person プロジェクトの specs フォルダ構成とファイル配置ルールを説明します。

本構成は、Agent Skills指示書群（`@agent_skills/struts-to-jsf-migration/instructions/`）が想定する標準構造に準拠しています。

重要:
* JSFは画面中心のサーバーサイドMVCフレームワーク
* basic_design/は「画面グループ単位」で構成（関連する画面群をまとめる）
* detailed_design/は「画面単位」で構成（個別画面の実装設計）

テンプレート参照:
* 基本設計: @agent_skills/struts-to-jsf-migration/templates/basic_design/
* 詳細設計: @agent_skills/jakarta-ee-api-base/templates/detailed_design/

---

## フォルダ構成

```
specs/
└── baseline/                                   # ベースライン仕様（初回リリース）
    ├── requirements/                           # 要件定義
    │   ├── requirements.md                     # システム要件定義書
    │   └── behaviors.md                        # 要件レベルの振る舞い仕様
    │
    ├── basic_design/                           # 基本設計
    │   ├── common/                             # 共通設計（必須）
    │   │   ├── architecture_design.md          # アーキテクチャ設計書
    │   │   ├── data_model.md                   # データモデル（ERD、エンティティ定義）
    │   │   ├── external_interface.md           # 外部インターフェース仕様書
    │   │   ├── functional_design.md            # 共通機能設計書
    │   │   ├── behaviors.md                    # 共通振る舞い仕様書
    │   │   ├── CHANGES.md                      # 共通設計変更管理
    │   │   └── changes_archive/                # 適用済み変更のアーカイブ
    │   │
    │   └── person_management/                  # Person管理画面グループ
    │       ├── functional_design.md            # 機能設計書（画面一覧、画面遷移）
    │       ├── screen_design.md                # 画面設計書（全画面のレイアウト、バリデーション）
    │       ├── behaviors.md                    # 振る舞い仕様書（E2Eテスト用、複数画面またぐフロー、Gherkin記法）
    │       ├── CHANGES.md                      # 画面グループ設計変更管理
    │       └── changes_archive/                # 適用済み変更のアーカイブ
    │
    └── detailed_design/                        # 詳細設計
        ├── common/                             # 共通詳細設計
        │   ├── detailed_design.md              # Personエンティティ、PersonDao等の詳細設計
        │   └── behaviors.md                    # 単体テスト用振る舞い仕様（Gherkin記法）
        │
        ├── FUNC_001_PersonList/                # PersonList画面の詳細設計
        │   ├── detailed_design.md              # PersonListBean、PersonService等の詳細設計
        │   └── behaviors.md                    # 単体テスト用振る舞い仕様
        │
        ├── FUNC_002_PersonInput/               # PersonInput画面の詳細設計
        │   ├── detailed_design.md              # PersonInputBean等の詳細設計
        │   └── behaviors.md                    # 単体テスト用振る舞い仕様
        │
        └── FUNC_003_PersonConfirm/             # PersonConfirm画面の詳細設計
            ├── detailed_design.md              # PersonConfirmBean等の詳細設計
            └── behaviors.md                    # 単体テスト用振る舞い仕様
```

---

## フォルダ配置ルール

### 1. requirements/

* システム全体の要件を定義
* 画面グループ構造は持たない（システム全体で1セット）

### 2. basic_design/

#### 2.1 common/ フォルダ（必須）

* すべての画面グループから参照される共通設計を配置
* テンプレート: @agent_skills/struts-to-jsf-migration/templates/basic_design/
* 配置するファイル:
  * `architecture_design.md` - 技術スタック、アーキテクチャパターン、パッケージ構造
  * `data_model.md` - エンティティ定義、ERD、テーブル定義
  * `external_interface.md` - 外部API仕様、外部システム連携
  * `functional_design.md` - 共通機能（該当する場合）
  * `behaviors.md` - 共通振る舞い（該当する場合）
  * `CHANGES.md` - 共通設計の変更管理
  * `changes_archive/` - 適用済み変更のアーカイブフォルダ

#### 2.2 {screen_group}/ フォルダ（画面グループごと）

* 画面グループ固有の設計を配置
* 本プロジェクトでは `person_management/` のみ
* 画面グループ: 関連する画面群（一覧、入力、確認等）をまとめたもの
* テンプレート: @agent_skills/struts-to-jsf-migration/templates/basic_design/
* 配置するファイル:
  * `functional_design.md` - 画面一覧、画面遷移図、画面グループ全体の機能設計
  * `screen_design.md` - 画面グループ内全画面のレイアウト、入力項目、バリデーション（JSF特有）
  * `behaviors.md` - E2Eテスト用の振る舞い仕様（複数画面またぐフロー、Gherkin記法）
  * `CHANGES.md` - 画面グループ設計の変更管理
  * `changes_archive/` - 適用済み変更のアーカイブフォルダ

重要:
* JSFは画面中心のサーバーサイドMVCフレームワーク
* 画面グループは画面遷移フローを持つ関連画面の集まり
* REST APIのドメイン駆動設計とは異なるアプローチ

### 3. detailed_design/

#### 3.1 common/ フォルダ（必須）

* 共通コンポーネント（Entity、Dao等）の詳細設計
* 配置するファイル:
  * `detailed_design.md` - クラス設計、メソッドシグネチャ、設計判断
    - **簡潔性の原則**: 基本設計とコードの「橋渡し」となる設計判断のみを簡潔に記載
    - **実装詳細（処理ステップ等）は記載しない**（コードレビューで修正しやすくするため）
  * `behaviors.md` - 単体テスト用振る舞い仕様（Gherkin記法、ブラックボックステスト）

#### 3.2 FUNC_XXX_YYY/ フォルダ（画面/機能ごと）

* 画面単位の詳細設計を配置
* フォルダ命名規則: `FUNC_{連番}_{機能名}`
* 配置するファイル:
  * `detailed_design.md` - ManagedBean、Service等のクラス設計
    - **簡潔性の原則**: 基本設計とコードの「橋渡し」となる設計判断のみを簡潔に記載
    - クラス名と責務、主要メソッドのシグネチャ、設計判断を示すアノテーション等
    - **実装詳細（処理ステップ等）は記載しない**
  * `behaviors.md` - 単体テスト用振る舞い仕様（Gherkin記法）

---

## 変更管理（CHANGES.md）

### 概要

基本設計の変更は、`CHANGES.md` ファイルで管理します。

### 配置場所

* 共通設計の変更: `basic_design/common/CHANGES.md`
* ドメイン固有設計の変更: `basic_design/{domain}/CHANGES.md`

### 使用方法

1. 変更内容を該当する `CHANGES.md` に記載
2. `@agent_skills/struts-to-jsf-migration/instructions/basic_design_change.md` の指示に従って影響分析
3. 詳細設計・コード・テストを更新
4. 適用完了後、`changes_archive/YYYYMMDD_変更タイトル.md` にアーカイブ

---

## 振る舞い仕様（behaviors.md）の種別

### 1. requirements/behaviors.md

* 対象: 要件レベルの振る舞い
* 記法: Gherkin記法
* 用途: 要件トレーサビリティ

### 2. basic_design/{domain}/behaviors.md

* 対象: E2Eテスト・結合テスト用の振る舞い
* 記法: Gherkin記法
* 特徴: 画面間遷移、実際のDB・画面レンダリング

### 3. detailed_design/{FUNC_XXX}/behaviors.md

* 対象: 単体テスト用の振る舞い
* 記法: Gherkin記法
* 特徴: 1メソッド単位、ブラックボックステスト、依存関係はモック

---

## Agent Skills指示書との対応

本フォルダ構成は、以下の指示書が想定する構造に準拠しています：

### 1. basic_design_change.md

* パス参照:
  * `{spec_directory}/basic_design/common/`
  * `{spec_directory}/basic_design/{domain}/CHANGES.md`
  * `{spec_directory}/detailed_design/common/detailed_design.md`

### 2. detailed_design.md

* パス参照:
  * `{spec_directory}/basic_design/common/architecture_design.md`
  * `{spec_directory}/basic_design/{domain}/functional_design.md`
  * `{spec_directory}/basic_design/{domain}/screen_design.md`
  * `{spec_directory}/detailed_design/{FUNC_ID}/detailed_design.md`

### 3. code_generation.md

* パス参照:
  * `{project_root}/specs/baseline/basic_design/common/architecture_design.md`
  * `{project_root}/specs/baseline/basic_design/{domain}/functional_design.md`
  * `{project_root}/specs/baseline/detailed_design/{target_type}/detailed_design.md`

### 4. その他の指示書

* `unit_test_execution.md`
* `it_generation.md`
* `e2e_test_generation.md`
* `reverse_engineering.md`

すべて上記のフォルダ構成を前提としています。

---

## マルチプロジェクト構成について

### Gradle マルチプロジェクト構成

このプロジェクト（jsf-person-sdd-wf）は、リポジトリルートの `build.gradle` を使用するマルチプロジェクト構成の一部です。

**重要な考慮事項:**

* **build.gradleの場所**: リポジトリルート（`ai_driven_dev_202601/build.gradle`）
* **Gradleタスク実行**: リポジトリルートまたはプロジェクトルートで実行可能
  * ルートから実行: `cd ai_driven_dev_202601 && ./gradlew :jsf-person-sdd-wf:test`
  * プロジェクトルートから実行: `cd projects/sdd-wf/person/jsf-person && ../../../../gradlew test`
* **Agent Skills使用時**: `build_script_path` パラメータにリポジトリルートの build.gradle のパスを指定することを推奨
  ```yaml
  project_root: projects/sdd-wf/person/jsf-person
  build_script_path: ./build.gradle  # リポジトリルートのbuild.gradleを指定
  ```

### テスト実行時の注意点

単体テスト実行評価（unit_test_execution.md）使用時は、以下のようにパラメータを指定:

```
@agent_skills/struts-to-jsf-migration/instructions/unit_test_execution.md

単体テストを実行してください

パラメータ:
* project_root: projects/sdd-wf/person/jsf-person
* target_type: FUNC_001_PersonList
* build_script_path: ./build.gradle  # リポジトリルートのbuild.gradleを指定
```

---

## 拡張時のガイドライン

### 新しい画面グループを追加する場合

1. `basic_design/` 配下に新しい画面グループフォルダを作成
   * 例: `basic_design/address_management/`（住所管理画面グループ）
   * 画面グループ: 関連する画面群（一覧、入力、確認等）をまとめたもの
2. 以下のファイルを配置:
   * `functional_design.md` - 画面グループ内の画面一覧、画面遷移図
   * `screen_design.md` - 画面グループ内全画面の画面設計
   * `behaviors.md` - E2Eテスト用（複数画面またぐフロー）
   * `CHANGES.md` - 画面グループの変更管理
   * `changes_archive/` フォルダ
3. 必要に応じて `detailed_design/` 配下に画面単位のフォルダを作成

### 新しい画面/機能を追加する場合

1. `detailed_design/` 配下に新しいフォルダを作成
   * 命名規則: `FUNC_{連番}_{画面名}/`（画面単位）
2. 以下のファイルを配置:
   * `detailed_design.md` - 個別画面の実装設計
   * `behaviors.md` - 単体テスト用

---

## 参考資料

* [@agent_skills/struts-to-jsf-migration/instructions/](../../../agent_skills/struts-to-jsf-migration/instructions/) - Agent Skills指示書群
* [@agent_skills/struts-to-jsf-migration/principles/common_rules.md](../../../agent_skills/struts-to-jsf-migration/principles/common_rules.md) - 共通ルール、Gherkin記法
* [berry-books-api/specs/](../../bookstore/berry-books-api/specs/) - 参考構成（複数ドメインの例）

---

## 更新履歴

* 2026-02-06: 初版作成、ドメイン分割構造への移行完了
