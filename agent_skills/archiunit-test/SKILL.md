---
name: archiunit-test
description: Jakarta EEプロジェクトのアーキテクチャルールをArchiUnitでテスト自動生成。レイヤー依存関係、命名規則、パッケージ構造、アノテーション使用ルールを検証。JUnit 5統合、カスタムルール対応。
---

# ArchiUnitアーキテクチャテスト生成 Agent Skill

## 使い方

### ArchiUnitテストコード生成

```
@agent_skills/archiunit-test/instructions/generate_archunit_tests.md

プロジェクトのアーキテクチャテストを生成してください

パラメータ
* project_path: <プロジェクトルートパス>
* package_root: <ベースパッケージ名（例: pro.kensait.berrybooks）>
* test_output_dir: <テスト出力ディレクトリ（省略可、デフォルト: {project_path}/src/test/java）>
```

AIが自動で以下を実行
1. プロジェクトのパッケージ構造を解析
2. レイヤードアーキテクチャのルールを定義
3. 命名規則のルールを定義
4. アノテーション使用ルールを定義
5. ArchiUnitテストクラスを生成
6. README（テスト実行方法）を生成

特徴: プロジェクトのパッケージ構造を自動解析し、適切なアーキテクチャルールを推論します。

---

## 実践例

### 例1: Berry Books APIのアーキテクチャテスト生成

```
@agent_skills/archiunit-test/instructions/generate_archunit_tests.md

Berry Books APIのアーキテクチャテストを生成してください

パラメータ
* project_path: projects/master/bookstore/berry-books-api
* package_root: pro.kensait.berrybooks
```

AIが自動で実行:
1. パッケージ構造を解析（api, service, dao, entity, dto, security等）
2. レイヤー依存関係ルールを生成
3. 命名規則ルールを生成
4. テストクラスを生成

生成されるテスト:
- `LayeredArchitectureTest.java` - レイヤー依存関係の検証
- `NamingConventionTest.java` - 命名規則の検証
- `AnnotationRulesTest.java` - アノテーション使用ルールの検証
- `PackageStructureTest.java` - パッケージ構造の検証

---

## 対応する主要機能

* レイヤードアーキテクチャの検証
  - Resource層 → Service層 → DAO層 → Entity層の依存関係ルール
  - DTOとEntityの分離
  - レイヤー循環依存の検出
* 命名規則の強制
  - クラス名サフィックス（Resource, Service, Dao, Entity等）
  - パッケージ命名規則
* アノテーション使用ルールの検証
  - JAX-RS（@Path, @GET, @POST等）
  - CDI（@ApplicationScoped, @Inject等）
  - JPA（@Entity, @Table等）
* パッケージ構造の検証
  - パッケージ依存関係
  - 禁止パッケージへのアクセス
* カスタムルールの追加

## 重要な注意事項

### レイヤー依存関係

標準的なレイヤードアーキテクチャを前提としています：

```
Resource/API層（JAX-RS）
    ↓
Service層（ビジネスロジック）
    ↓
DAO層（データアクセス）
    ↓
Entity層（JPA エンティティ）
```

### DTOとEntityの分離

- DTOはAPIレイヤーで使用（JSON変換）
- EntityはDAOレイヤー以下で使用
- ResourceクラスでEntityを直接返却しない

### パッケージ構造

Jakarta EEプロジェクトの標準的なパッケージ構造を前提としています：
- `{package_root}.api` - REST APIエンドポイント
- `{package_root}.service` - サービス層
- `{package_root}.dao` - データアクセス層
- `{package_root}.entity` - エンティティ
- `{package_root}.dto` - データ転送オブジェクト
- `{package_root}.security` - セキュリティ
- `{package_root}.exception` - 例外ハンドラー

---

## ディレクトリ構造

```
agent_skills/archiunit-test/
├── SKILL.md                          # このファイル
├── README.md                         # クイックスタートガイド
├── principles/                       # 原則（全プロジェクト共通）
│   └── archunit_best_practices.md   # ArchiUnitベストプラクティス
├── templates/                        # テンプレート
│   └── archunit_rules_template.md   # アーキテクチャルールテンプレート
└── instructions/
    └── generate_archunit_tests.md   # テスト生成指示
```

---

## 参考資料

* [ArchUnit 公式ドキュメント](https://www.archunit.org/)
* [ArchiUnitベストプラクティス](principles/archunit_best_practices.md)
* [アーキテクチャルールテンプレート](templates/archunit_rules_template.md)
