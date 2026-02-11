# ArchUnitアーキテクチャテスト生成指示書

バージョン: 1.0.0  
最終更新日: 2026-02-11

---

## 目的

Jakarta EEプロジェクトのパッケージ構造を解析し、ArchUnitによるアーキテクチャテストコード（Java）を自動生成する。

---

## パラメータ

| パラメータ名 | 必須 | デフォルト値 | 説明 |
|------------|------|-------------|------|
| `project_path` | ✓ | - | プロジェクトのルートディレクトリパス |
| `package_root` | ✓ | - | ベースパッケージ名（例: pro.kensait.berrybooks） |
| `test_output_dir` | - | `{project_path}/src/test/java` | テストコードの出力ディレクトリ |
| `generate_readme` | - | `true` | README_ARCHUNIT.mdを生成するか |

---

## 実行手順

### ステップ1: プロジェクト構造の解析

1. プロジェクトルートの確認
   ```
   project_path が存在することを確認
   ```

2. パッケージ構造の解析
   ```
   {project_path}/src/main/java/{package_rootのパス}/ 配下を解析
   ```

3. レイヤーの識別
   
   以下のパッケージを探索し、存在するレイヤーを特定:
   - `api` または `resource` - REST APIエンドポイント層
   - `service` - サービス層（ビジネスロジック）
   - `dao` - データアクセス層
   - `entity` - エンティティ層（JPA）
   - `dto` - データ転送オブジェクト
   - `security` - セキュリティ関連
   - `exception` - 例外ハンドラー
   - `external` - 外部システム連携
   - `common` - 共通ユーティリティ
   - `util` - ユーティリティクラス

4. クラスの分類
   
   各レイヤーのクラスを分類:
   - Resource/APIクラス（`@Path`アノテーション）
   - Serviceクラス（サフィックス: `Service`, `ServiceIF`）
   - DAOクラス（サフィックス: `Dao`）
   - Entityクラス（`@Entity`アノテーション）
   - DTOクラス（サフィックス: `TO`, `Request`, `Response`等）

5. 依存関係の確認
   
   実際の依存関係を確認し、違反がないかチェック:
   - Resource → Service
   - Service → DAO, Entity
   - DAO → Entity
   - DTOとEntityの分離

### ステップ2: テスト出力ディレクトリの準備

1. 出力ディレクトリの作成
   ```
   {test_output_dir}/{package_rootのパス}/architecture/ ディレクトリを作成
   ```

2. 既存テストの確認
   ```
   既存のArchUnitテストファイルを確認
   ```

### ステップ3: ArchUnitテストクラスの生成

#### 3-1. LayeredArchitectureTest.java の生成

目的: レイヤー依存関係を検証

ファイルパス: `{test_output_dir}/{package_rootのパス}/architecture/LayeredArchitectureTest.java`

生成内容:

```java
package {package_root}.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * レイヤードアーキテクチャの依存関係を検証するテストクラス
 * 
 * Jakarta EE 10の標準的なレイヤー構造を検証:
 * - API/Resource層（JAX-RS）
 * - Service層（ビジネスロジック）
 * - DAO層（データアクセス）
 * - Entity層（JPA エンティティ）
 */
@AnalyzeClasses(packages = "{package_root}")
public class LayeredArchitectureTest {

    /**
     * レイヤー依存関係ルール
     * 
     * 依存関係:
     * - API層 → Service層、DTO
     * - Service層 → DAO層、Entity、DTO
     * - DAO層 → Entity
     * - Entity層 → （独立）
     * - DTO → （独立）
     */
    @ArchTest
    static final ArchRule layer_dependencies_are_respected = layeredArchitecture()
        .consideringAllDependencies()
        
        // レイヤー定義
        .layer("API").definedBy("..api..")
        .layer("Service").definedBy("..service..")
        .layer("DAO").definedBy("..dao..")
        .layer("Entity").definedBy("..entity..")
        .layer("DTO").definedBy("..dto..")
        
        // 依存関係ルール
        .whereLayer("API").mayNotBeAccessedByAnyLayer()
        .whereLayer("Service").mayOnlyBeAccessedByLayers("API")
        .whereLayer("DAO").mayOnlyBeAccessedByLayers("Service")
        .whereLayer("Entity").mayOnlyBeAccessedByLayers("DAO", "Service");
    
    /**
     * DTOとEntityの分離ルール
     * DTOはEntityに依存してはならない
     */
    @ArchTest
    static final ArchRule dto_should_not_depend_on_entity = 
        com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
            .that().resideInAPackage("..dto..")
            .should().dependOnClassesThat().resideInAPackage("..entity..");
    
    /**
     * EntityはAPI層に依存してはならない
     */
    @ArchTest
    static final ArchRule entity_should_not_depend_on_api = 
        com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
            .that().resideInAPackage("..entity..")
            .should().dependOnClassesThat().resideInAPackage("..api..");
}
```

注意点:
- 存在しないレイヤーは定義から除外
- プロジェクト固有のレイヤー構造に合わせて調整

#### 3-2. NamingConventionTest.java の生成

目的: 命名規則を検証

ファイルパス: `{test_output_dir}/{package_rootのパス}/architecture/NamingConventionTest.java`

生成内容:

```java
package {package_root}.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import jakarta.ws.rs.Path;
import jakarta.persistence.Entity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * 命名規則を検証するテストクラス
 * 
 * 検証内容:
 * - Resourceクラスのサフィックス
 * - Serviceクラスのサフィックス
 * - DAOクラスのサフィックス
 * - Entityクラスのパッケージ配置
 * - DTOクラスのサフィックス
 */
@AnalyzeClasses(packages = "{package_root}")
public class NamingConventionTest {

    /**
     * REST APIクラスは "*Resource" で終わること
     */
    @ArchTest
    static final ArchRule resource_classes_should_be_suffixed = 
        classes()
            .that().resideInAPackage("..api..")
            .and().areAnnotatedWith(Path.class)
            .should().haveSimpleNameEndingWith("Resource")
            .because("REST APIクラスは 'Resource' サフィックスを使用する");
    
    /**
     * サービスクラスは "*Service" または "*ServiceIF" で終わること
     */
    @ArchTest
    static final ArchRule service_classes_should_be_suffixed = 
        classes()
            .that().resideInAPackage("..service..")
            .and().areNotInterfaces()
            .should().haveSimpleNameEndingWith("Service")
            .because("サービス実装クラスは 'Service' サフィックスを使用する");
    
    /**
     * DAOクラスは "*Dao" で終わること
     */
    @ArchTest
    static final ArchRule dao_classes_should_be_suffixed = 
        classes()
            .that().resideInAPackage("..dao..")
            .should().haveSimpleNameEndingWith("Dao")
            .because("DAOクラスは 'Dao' サフィックスを使用する");
    
    /**
     * Entityクラスは entity パッケージに配置すること
     */
    @ArchTest
    static final ArchRule entity_classes_should_reside_in_entity_package = 
        classes()
            .that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..entity..")
            .because("Entityクラスは entity パッケージに配置する");
    
    /**
     * パッケージ名は小文字のみ使用すること
     */
    @ArchTest
    static final ArchRule package_names_should_be_lowercase = 
        classes()
            .should().resideInAPackage("..{package_root}..")
            .because("パッケージ名は小文字のみを使用する");
}
```

注意点:
- プロジェクト固有の命名規則に合わせて調整
- インターフェースとクラスを区別

#### 3-3. AnnotationRulesTest.java の生成

目的: アノテーション使用ルールを検証

ファイルパス: `{test_output_dir}/{package_rootのパス}/architecture/AnnotationRulesTest.java`

生成内容:

```java
package {package_root}.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import jakarta.ws.rs.Path;
import jakarta.persistence.Entity;
import jakarta.enterprise.context.ApplicationScoped;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * アノテーション使用ルールを検証するテストクラス
 * 
 * 検証内容:
 * - JAX-RS アノテーション（@Path）
 * - JPA アノテーション（@Entity）
 * - CDI アノテーション（@ApplicationScoped）
 */
@AnalyzeClasses(packages = "{package_root}")
public class AnnotationRulesTest {

    /**
     * REST APIクラスは @Path アノテーションを持つこと
     */
    @ArchTest
    static final ArchRule resource_classes_should_be_annotated_with_path = 
        classes()
            .that().resideInAPackage("..api..")
            .and().haveSimpleNameEndingWith("Resource")
            .should().beAnnotatedWith(Path.class)
            .because("REST APIクラスは @Path アノテーションを持つ");
    
    /**
     * Entityクラスは @Entity アノテーションを持つこと
     */
    @ArchTest
    static final ArchRule entity_classes_should_be_annotated_with_entity = 
        classes()
            .that().resideInAPackage("..entity..")
            .and().areNotInterfaces()
            .and().areNotEnums()
            .should().beAnnotatedWith(Entity.class)
            .because("Entityクラスは @Entity アノテーションを持つ");
    
    /**
     * サービスクラスは @ApplicationScoped アノテーションを持つこと
     */
    @ArchTest
    static final ArchRule service_classes_should_be_annotated_with_application_scoped = 
        classes()
            .that().resideInAPackage("..service..")
            .and().haveSimpleNameEndingWith("Service")
            .and().areNotInterfaces()
            .should().beAnnotatedWith(ApplicationScoped.class)
            .because("サービスクラスは @ApplicationScoped アノテーションを持つ");
    
    /**
     * DAOクラスは @ApplicationScoped アノテーションを持つこと
     */
    @ArchTest
    static final ArchRule dao_classes_should_be_annotated_with_application_scoped = 
        classes()
            .that().resideInAPackage("..dao..")
            .and().haveSimpleNameEndingWith("Dao")
            .should().beAnnotatedWith(ApplicationScoped.class)
            .because("DAOクラスは @ApplicationScoped アノテーションを持つ");
}
```

注意点:
- Jakarta EE 10のアノテーションパッケージを使用
- プロジェクト固有のアノテーション使用ルールに合わせて調整

#### 3-4. PackageStructureTest.java の生成

目的: パッケージ構造とサイクル依存を検証

ファイルパス: `{test_output_dir}/{package_rootのパス}/architecture/PackageStructureTest.java`

生成内容:

```java
package {package_root}.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * パッケージ構造とサイクル依存を検証するテストクラス
 * 
 * 検証内容:
 * - パッケージ間の依存関係
 * - サイクル依存の検出
 * - 禁止パッケージへのアクセス
 */
@AnalyzeClasses(packages = "{package_root}")
public class PackageStructureTest {

    /**
     * DTOはEntityに依存してはならない
     */
    @ArchTest
    static final ArchRule dto_should_not_depend_on_entity = 
        noClasses()
            .that().resideInAPackage("..dto..")
            .should().dependOnClassesThat().resideInAPackage("..entity..")
            .because("DTOとEntityは分離されるべき");
    
    /**
     * EntityはAPIに依存してはならない
     */
    @ArchTest
    static final ArchRule entity_should_not_depend_on_api = 
        noClasses()
            .that().resideInAPackage("..entity..")
            .should().dependOnClassesThat().resideInAPackage("..api..")
            .because("Entityは独立したドメインモデルであるべき");
    
    /**
     * EntityはDTOに依存してはならない
     */
    @ArchTest
    static final ArchRule entity_should_not_depend_on_dto = 
        noClasses()
            .that().resideInAPackage("..entity..")
            .should().dependOnClassesThat().resideInAPackage("..dto..")
            .because("Entityは独立したドメインモデルであるべき");
    
    /**
     * サイクル依存が存在してはならない
     */
    @ArchTest
    static final ArchRule no_cycles_by_method_calls_between_slices = 
        slices()
            .matching("{package_root}.(*)..")
            .should().beFreeOfCycles()
            .because("サイクル依存は複雑性を増加させる");
}
```

注意点:
- サイクル依存のチェックは負荷が高いため、必要に応じて調整
- プロジェクト固有の依存関係ルールを追加

### ステップ4: README_ARCHUNIT.md の生成（generate_readme=trueの場合）

ファイルパス: `{project_path}/README_ARCHUNIT.md`

生成内容:

```markdown
# ArchUnitアーキテクチャテスト

## 概要

このプロジェクトでは、ArchUnitを使用してアーキテクチャルールを検証しています。

## テストクラス

### 1. LayeredArchitectureTest
レイヤー依存関係を検証します。

### 2. NamingConventionTest
命名規則を検証します。

### 3. AnnotationRulesTest
アノテーション使用ルールを検証します。

### 4. PackageStructureTest
パッケージ構造とサイクル依存を検証します。

## テスト実行方法

### すべてのテストを実行
```bash
./gradlew :{プロジェクト名}:test
```

### ArchUnitテストのみ実行
```bash
./gradlew :{プロジェクト名}:test --tests "*architecture.*"
```

### 特定のテストクラスを実行
```bash
./gradlew :{プロジェクト名}:test --tests "*LayeredArchitectureTest"
```

## アーキテクチャルール

### レイヤードアーキテクチャ

```
API/Resource層（JAX-RS）
    ↓
Service層（ビジネスロジック）
    ↓
DAO層（データアクセス）
    ↓
Entity層（JPA エンティティ）
```

### 命名規則

- REST APIクラス: `*Resource`
- サービスクラス: `*Service` または `*ServiceIF`
- DAOクラス: `*Dao`
- DTOクラス: `*TO`, `*Request`, `*Response` 等

### パッケージ構造

- `{package_root}.api` - REST APIエンドポイント
- `{package_root}.service` - サービス層
- `{package_root}.dao` - データアクセス層
- `{package_root}.entity` - エンティティ
- `{package_root}.dto` - データ転送オブジェクト

## トラブルシューティング

### テストが失敗する場合

1. 違反箇所をログから特定
2. 依存関係を修正
3. または、ルールを調整

### カスタムルールの追加

プロジェクト固有のルールは、テストクラスに追加してください。

## 参考資料

- [ArchUnit公式ドキュメント](https://www.archunit.org/)
- [ArchUnitベストプラクティス](../../agent_skills/archiunit-test/principles/archunit_best_practices.md)
```

### ステップ5: 完了確認と報告

すべてのファイル生成が完了したら、以下を確認してユーザーに報告する:

1. 生成されたファイル一覧
   - LayeredArchitectureTest.java
   - NamingConventionTest.java
   - AnnotationRulesTest.java
   - PackageStructureTest.java
   - README_ARCHUNIT.md（オプション）

2. 次のステップの案内
   ```
   以下のコマンドでテストを実行できます:
   
   # すべてのテスト（単体テストとArchUnitテスト）
   ./gradlew :{プロジェクト名}:test
   
   # ArchUnitテストのみ
   ./gradlew :{プロジェクト名}:test --tests "*architecture.*"
   
   # 特定のテストクラス
   ./gradlew :{プロジェクト名}:test --tests "*LayeredArchitectureTest"
   ```

3. 注意事項
   - ArchUnit依存関係がbuild.gradleに追加されていることを確認
   - テストが失敗した場合は、アーキテクチャルールの違反を修正
   - プロジェクト固有のルールは、テストクラスに追加

---

## 生成ルール

### 必須事項

1. 遵守するベストプラクティス
   - `@agent_skills/archiunit-test/principles/archunit_best_practices.md` を参照
   - JUnit 5との統合（`@ArchTest`アノテーション）
   - わかりやすいエラーメッセージ（`because()`メソッド）
   - 適切なルールのグループ化

2. Java型定義
   - 厳密な型定義を使用
   - Jakarta EE 10のアノテーションを使用
   - static finalなArchRuleフィールド

3. コメント
   - 日本語でクラス・メソッドの説明を記述
   - ルールの意図を明確に記述
   - 各テストクラスにJavadocを追加

4. コード品質
   - 標準的なJavaコーディング規約に準拠
   - 変数名・メソッド名は英語（camelCase）
   - クラス名はPascalCase

### 任意事項

1. カスタムルールの追加
   - プロジェクト固有のルールは、必要に応じて追加

2. ルールの無効化
   - 特定のクラスやパッケージを除外する場合は、`.ignoreDependency()`を使用

---

## エラーハンドリング

### プロジェクトルートが見つからない場合

```
エラー: プロジェクトルートが見つかりません
ディレクトリパス: {project_path}

以下を確認してください:
1. ディレクトリパスが正しいか
2. ディレクトリが存在するか
3. アクセス権限があるか
```

### パッケージが見つからない場合

```
エラー: ベースパッケージが見つかりません
パッケージ: {package_root}

以下を確認してください:
1. パッケージ名が正しいか
2. src/main/java配下にパッケージが存在するか
```

### レイヤーが存在しない場合

```
警告: 一部のレイヤーが見つかりませんでした

見つからなかったレイヤー:
- {レイヤー名}

これらのレイヤーはテストから除外されます。
```

---

## 実装例

### 例1: Berry Books APIのアーキテクチャテスト

入力パラメータ:
```
project_path: projects/master/bookstore/berry-books-api
package_root: pro.kensait.berrybooks
```

解析結果:
- 発見されたレイヤー: api, service, dao, entity, dto, security, external
- Resourceクラス: 5個
- Serviceクラス: 4個
- DAOクラス: 2個
- Entityクラス: 3個

生成されるテスト:
- LayeredArchitectureTest.java - レイヤー依存関係の検証
- NamingConventionTest.java - 命名規則の検証
- AnnotationRulesTest.java - アノテーションルールの検証
- PackageStructureTest.java - パッケージ構造の検証

---

## 参考資料

* [ArchUnitベストプラクティス](../principles/archunit_best_practices.md)
* [アーキテクチャルールテンプレート](../templates/archunit_rules_template.md)
* [ArchUnit公式ドキュメント](https://www.archunit.org/)
* [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)
