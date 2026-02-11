# ArchiUnitアーキテクチャテスト生成 Agent Skill

バージョン: 1.0.0  
最終更新日: 2026-02-11

---

## 概要

Jakarta EEプロジェクトのアーキテクチャルールをArchiUnitで自動テスト生成するAgent Skillです。

このAgent Skillは、プロジェクトのパッケージ構造を解析し、レイヤー依存関係、命名規則、アノテーション使用ルールなどを検証するテストコードを自動生成します。

対象プロジェクト: Jakarta EE 10 REST APIプロジェクト（Berry Books API、Back Office API等）

## クイックスタート

1. ArchiUnit依存関係がbuild.gradleに追加されていることを確認
2. `@agent_skills/archiunit-test/instructions/generate_archunit_tests.md` でテストコード生成

```
@agent_skills/archiunit-test/instructions/generate_archunit_tests.md

プロジェクトのアーキテクチャテストを生成してください

パラメータ
* project_path: projects/master/bookstore/berry-books-api
* package_root: pro.kensait.berrybooks
```

---

## フォルダ構造

```
agent_skills/archiunit-test/
│
├── SKILL.md                                    # Agent Skill説明書（エントリポイント）
│
├── instructions/                               # 開発インストラクション
│   └── generate_archunit_tests.md             # テスト生成指示
│       └─→ 遵守: principles/archunit_best_practices.md
│       └─→ 解析: {project_path}/src/main/java/
│       └─→ 出力: {test_output_dir}/（テストコード）
│                  {project_path}/README_ARCHUNIT.md
│
├── principles/                                 # 開発原則（全プロジェクト共通）
│   └── archunit_best_practices.md             # ArchiUnitベストプラクティス
│                                               - レイヤードアーキテクチャルール
│                                               - 命名規則
│                                               - アノテーションルール
│                                               - パッケージ依存関係ルール
│                                               - カスタムルールの作成
│
└── templates/                                  # テンプレート
    └── archunit_rules_template.md             # アーキテクチャルールテンプレート
```

---

## プロジェクトフォルダ構造

このAgent Skillを使用して生成されるテストコードの標準フォルダ構造です。

```
{project_path}/                               # プロジェクトルートディレクトリ
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── {package_root}/
│   │           ├── api/                       # REST APIエンドポイント
│   │           ├── service/                   # サービス層
│   │           ├── dao/                       # データアクセス層
│   │           ├── entity/                    # JPA エンティティ
│   │           ├── dto/                       # データ転送オブジェクト
│   │           ├── security/                  # セキュリティ
│   │           ├── exception/                 # 例外ハンドラー
│   │           └── ...
│   │
│   └── test/
│       └── java/
│           └── {package_root}/
│               └── architecture/              # ArchiUnitテスト
│                   ├── LayeredArchitectureTest.java
│                   ├── NamingConventionTest.java
│                   ├── AnnotationRulesTest.java
│                   └── PackageStructureTest.java
│
└── README_ARCHUNIT.md                         # テスト実行方法
```

---

## 検証されるアーキテクチャルール

### 1. レイヤードアーキテクチャ

Jakarta EEの標準的なレイヤー構造を検証します：

```
API/Resource層（JAX-RS）
    ↓ 依存可能
Service層（ビジネスロジック）
    ↓ 依存可能
DAO層（データアクセス）
    ↓ 依存可能
Entity層（JPA エンティティ）
```

**検証内容**:
- Resource層はService層とDTOにのみ依存
- Service層はDAO層、Entity、DTOに依存可能
- DAO層はEntityにのみ依存
- Entityは他のレイヤーに依存しない（独立）
- DTOはEntityを直接参照しない

### 2. 命名規則

**クラス名のサフィックス**:
- REST APIクラス: `*Resource`
- サービスクラス: `*Service` または `*ServiceIF`
- DAOクラス: `*Dao`
- Entityクラス: パッケージ `*.entity` に配置
- DTOクラス: パッケージ `*.dto` に配置、サフィックス `*TO`、`*Request`、`*Response` 等

**パッケージ命名**:
- 小文字のみ使用
- ドメイン駆動設計に準拠

### 3. アノテーションルール

**JAX-RS（REST API層）**:
- `@Path` - Resource クラスに付与
- `@GET`, `@POST`, `@PUT`, `@DELETE` - HTTPメソッド
- `@Produces`, `@Consumes` - メディアタイプ

**CDI（依存性注入）**:
- `@ApplicationScoped`, `@RequestScoped` - スコープ定義
- `@Inject` - 依存性注入

**JPA（Entity層）**:
- `@Entity` - Entityクラスに付与
- `@Table` - テーブルマッピング
- `@Id` - 主キー

**Bean Validation**:
- `@Valid` - バリデーション有効化
- `@NotNull`, `@NotBlank`, `@Email` 等 - 制約

### 4. パッケージ構造

**標準パッケージ構造**:
```
{package_root}
├── api/              # REST APIエンドポイント（@Path）
├── service/          # ビジネスロジック（@ApplicationScoped）
├── dao/              # データアクセス（@ApplicationScoped）
├── entity/           # JPA エンティティ（@Entity）
├── dto/              # データ転送オブジェクト
├── security/         # セキュリティ関連
├── exception/        # 例外ハンドラー
├── external/         # 外部システム連携
├── common/           # 共通ユーティリティ
└── util/             # ユーティリティクラス
```

**検証内容**:
- Entity層からAPI層への依存禁止
- DTO層からEntity層への依存禁止
- パッケージ循環依存の検出

---

## 生成されるテストファイル

### 1. LayeredArchitectureTest.java

レイヤー依存関係を検証するテストクラスです。

```java
@AnalyzeClasses(packages = "pro.kensait.berrybooks")
public class LayeredArchitectureTest {
    
    @ArchTest
    static final ArchRule layer_dependencies_are_respected = layeredArchitecture()
        .consideringAllDependencies()
        .layer("API").definedBy("..api..")
        .layer("Service").definedBy("..service..")
        .layer("DAO").definedBy("..dao..")
        .layer("Entity").definedBy("..entity..")
        .layer("DTO").definedBy("..dto..")
        
        .whereLayer("API").mayNotBeAccessedByAnyLayer()
        .whereLayer("Service").mayOnlyBeAccessedByLayers("API")
        .whereLayer("DAO").mayOnlyBeAccessedByLayers("Service")
        .whereLayer("Entity").mayOnlyBeAccessedByLayers("DAO", "Service");
}
```

### 2. NamingConventionTest.java

命名規則を検証するテストクラスです。

```java
@AnalyzeClasses(packages = "pro.kensait.berrybooks")
public class NamingConventionTest {
    
    @ArchTest
    static final ArchRule resource_classes_should_be_suffixed = 
        classes()
            .that().resideInAPackage("..api..")
            .and().areAnnotatedWith(Path.class)
            .should().haveSimpleNameEndingWith("Resource");
    
    @ArchTest
    static final ArchRule service_classes_should_be_suffixed = 
        classes()
            .that().resideInAPackage("..service..")
            .should().haveSimpleNameEndingWith("Service")
            .orShould().haveSimpleNameEndingWith("ServiceIF");
}
```

### 3. AnnotationRulesTest.java

アノテーション使用ルールを検証するテストクラスです。

```java
@AnalyzeClasses(packages = "pro.kensait.berrybooks")
public class AnnotationRulesTest {
    
    @ArchTest
    static final ArchRule resource_classes_should_be_annotated_with_path = 
        classes()
            .that().resideInAPackage("..api..")
            .and().haveSimpleNameEndingWith("Resource")
            .should().beAnnotatedWith(Path.class);
    
    @ArchTest
    static final ArchRule entity_classes_should_be_annotated_with_entity = 
        classes()
            .that().resideInAPackage("..entity..")
            .should().beAnnotatedWith(Entity.class);
}
```

### 4. PackageStructureTest.java

パッケージ構造を検証するテストクラスです。

```java
@AnalyzeClasses(packages = "pro.kensait.berrybooks")
public class PackageStructureTest {
    
    @ArchTest
    static final ArchRule dto_should_not_depend_on_entity = 
        noClasses()
            .that().resideInAPackage("..dto..")
            .should().dependOnClassesThat().resideInAPackage("..entity..");
    
    @ArchTest
    static final ArchRule entity_should_not_depend_on_api = 
        noClasses()
            .that().resideInAPackage("..entity..")
            .should().dependOnClassesThat().resideInAPackage("..api..");
}
```

---

## テスト実行方法

生成されたテストを実行するには:

```bash
# 単体テスト（ArchiUnitテストを含む）
./gradlew :berry-books-api:test

# 特定のテストクラスのみ実行
./gradlew :berry-books-api:test --tests "*LayeredArchitectureTest"

# すべてのArchiUnitテストを実行
./gradlew :berry-books-api:test --tests "*architecture.*"
```

---

## ベストプラクティス

### 1. レイヤー依存関係のルール化

プロジェクトのアーキテクチャを明確にし、レイヤー間の依存関係をルールとして定義します。

### 2. 命名規則の統一

チーム全体で命名規則を統一し、ArchiUnitでルールを強制します。

### 3. 継続的な検証

CI/CDパイプラインに組み込み、すべてのコミットでアーキテクチャルールを検証します。

### 4. カスタムルールの追加

プロジェクト固有のルールは、カスタムルールとして追加します。

詳細は [ArchiUnitベストプラクティス](principles/archunit_best_practices.md) を参照してください。

---

## トラブルシューティング

### テストが失敗する場合

**症状**: レイヤー依存関係の違反が検出される

**原因**: 不適切な依存関係が存在する

**解決策**: 
1. 違反箇所をログから特定
2. 依存関係を修正
3. または、ルールを調整

### パッケージ名が異なる場合

**症状**: クラスが見つからない

**原因**: パッケージ構造が想定と異なる

**解決策**: 
1. `@AnalyzeClasses` のパッケージ名を修正
2. レイヤー定義のパッケージパターンを修正

### アノテーションが見つからない場合

**症状**: アノテーションルールが失敗する

**原因**: Jakarta EE 10の依存関係が不足

**解決策**: 
build.gradleに依存関係を追加
```gradle
compileOnly "jakarta.platform:jakarta.jakartaee-api:10.0.0"
testImplementation "jakarta.platform:jakarta.jakartaee-api:10.0.0"
```

---

## 参考

* [SKILL.md](SKILL.md) - エントリポイント、クイックリファレンス
* [ArchiUnitベストプラクティス](principles/archunit_best_practices.md)
* [アーキテクチャルールテンプレート](templates/archunit_rules_template.md)
* [ArchUnit 公式ドキュメント](https://www.archunit.org/)
