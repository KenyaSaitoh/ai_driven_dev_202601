# ArchUnitアーキテクチャルールテンプレート

バージョン: 1.0.0  
最終更新日: 2026-02-11

---

## 概要

このドキュメントは、Jakarta EEプロジェクトでArchUnitのアーキテクチャルールを定義する際のテンプレートを提供します。

---

## 1. レイヤードアーキテクチャルール

### テンプレート1-1: 基本的なレイヤー依存関係

```java
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
```

### テンプレート1-2: DTOとEntityの分離

```java
@ArchTest
static final ArchRule dto_should_not_depend_on_entity = 
    noClasses()
        .that().resideInAPackage("..dto..")
        .should().dependOnClassesThat().resideInAPackage("..entity..")
        .because("DTOとEntityは分離されるべき");
```

### テンプレート1-3: Entityの独立性

```java
@ArchTest
static final ArchRule entity_should_not_depend_on_api = 
    noClasses()
        .that().resideInAPackage("..entity..")
        .should().dependOnClassesThat().resideInAPackage("..api..")
        .because("Entityは独立したドメインモデルであるべき");

@ArchTest
static final ArchRule entity_should_not_depend_on_dto = 
    noClasses()
        .that().resideInAPackage("..entity..")
        .should().dependOnClassesThat().resideInAPackage("..dto..")
        .because("Entityは独立したドメインモデルであるべき");
```

---

## 2. 命名規則ルール

### テンプレート2-1: REST APIクラスのサフィックス

```java
@ArchTest
static final ArchRule resource_classes_should_be_suffixed = 
    classes()
        .that().resideInAPackage("..api..")
        .and().areAnnotatedWith(Path.class)
        .should().haveSimpleNameEndingWith("Resource")
        .because("REST APIクラスは 'Resource' サフィックスを使用する");
```

### テンプレート2-2: サービスクラスのサフィックス

```java
@ArchTest
static final ArchRule service_classes_should_be_suffixed = 
    classes()
        .that().resideInAPackage("..service..")
        .and().areNotInterfaces()
        .should().haveSimpleNameEndingWith("Service")
        .because("サービス実装クラスは 'Service' サフィックスを使用する");

@ArchTest
static final ArchRule service_interfaces_should_be_suffixed = 
    classes()
        .that().resideInAPackage("..service..")
        .and().areInterfaces()
        .should().haveSimpleNameEndingWith("ServiceIF")
        .orShould().haveSimpleNameEndingWith("Service")
        .because("サービスインターフェースは 'ServiceIF' または 'Service' サフィックスを使用する");
```

### テンプレート2-3: DAOクラスのサフィックス

```java
@ArchTest
static final ArchRule dao_classes_should_be_suffixed = 
    classes()
        .that().resideInAPackage("..dao..")
        .should().haveSimpleNameEndingWith("Dao")
        .because("DAOクラスは 'Dao' サフィックスを使用する");
```

### テンプレート2-4: パッケージ名は小文字のみ

```java
@ArchTest
static final ArchRule package_names_should_be_lowercase = 
    classes()
        .should().resideInAPackage("..{package_root}..")
        .because("パッケージ名は小文字のみを使用する");
```

---

## 3. アノテーションルール

### テンプレート3-1: JAX-RS（REST API層）

```java
@ArchTest
static final ArchRule resource_classes_should_be_annotated_with_path = 
    classes()
        .that().resideInAPackage("..api..")
        .and().haveSimpleNameEndingWith("Resource")
        .should().beAnnotatedWith(Path.class)
        .because("REST APIクラスは @Path アノテーションを持つ");

@ArchTest
static final ArchRule resource_methods_should_have_http_method_annotation = 
    methods()
        .that().areDeclaredInClassesThat().resideInAPackage("..api..")
        .and().arePublic()
        .and().areDeclaredInClassesThat().areAnnotatedWith(Path.class)
        .should().beAnnotatedWith(GET.class)
        .orShould().beAnnotatedWith(POST.class)
        .orShould().beAnnotatedWith(PUT.class)
        .orShould().beAnnotatedWith(DELETE.class)
        .because("REST APIメソッドはHTTPメソッドアノテーションを持つ");
```

### テンプレート3-2: CDI（依存性注入）

```java
@ArchTest
static final ArchRule service_classes_should_be_annotated_with_application_scoped = 
    classes()
        .that().resideInAPackage("..service..")
        .and().haveSimpleNameEndingWith("Service")
        .and().areNotInterfaces()
        .should().beAnnotatedWith(ApplicationScoped.class)
        .because("サービスクラスは @ApplicationScoped アノテーションを持つ");

@ArchTest
static final ArchRule dao_classes_should_be_annotated_with_application_scoped = 
    classes()
        .that().resideInAPackage("..dao..")
        .and().haveSimpleNameEndingWith("Dao")
        .should().beAnnotatedWith(ApplicationScoped.class)
        .because("DAOクラスは @ApplicationScoped アノテーションを持つ");
```

### テンプレート3-3: JPA（Entity層）

```java
@ArchTest
static final ArchRule entity_classes_should_be_annotated_with_entity = 
    classes()
        .that().resideInAPackage("..entity..")
        .and().areNotInterfaces()
        .and().areNotEnums()
        .should().beAnnotatedWith(Entity.class)
        .because("Entityクラスは @Entity アノテーションを持つ");

@ArchTest
static final ArchRule entity_classes_should_have_id_field = 
    classes()
        .that().areAnnotatedWith(Entity.class)
        .should()
        .containAnyFieldsThat(
            ArchConditions.haveModifier(JavaModifier.PRIVATE)
            .and(ArchConditions.beAnnotatedWith(Id.class))
        )
        .because("Entityクラスは @Id アノテーションを持つフィールドが必要");
```

---

## 4. パッケージ構造ルール

### テンプレート4-1: パッケージ依存関係

```java
@ArchTest
static final ArchRule api_should_not_depend_on_dao = 
    noClasses()
        .that().resideInAPackage("..api..")
        .should().dependOnClassesThat().resideInAPackage("..dao..")
        .because("API層はDAO層に直接依存してはならない");

@ArchTest
static final ArchRule dao_should_not_depend_on_service = 
    noClasses()
        .that().resideInAPackage("..dao..")
        .should().dependOnClassesThat().resideInAPackage("..service..")
        .because("DAO層はService層に依存してはならない（逆方向の依存）");
```

### テンプレート4-2: サイクル依存の検出

```java
@ArchTest
static final ArchRule no_cycles_by_method_calls_between_slices = 
    slices()
        .matching("{package_root}.(*)..")
        .should().beFreeOfCycles()
        .because("サイクル依存は複雑性を増加させる");
```

---

## 5. カスタムルール

### テンプレート5-1: 例外処理ルール

```java
@ArchTest
static final ArchRule rest_resources_should_not_throw_generic_exceptions = 
    noClasses()
        .that().resideInAPackage("..api..")
        .should()
        .declareThrowableOfType(Exception.class)
        .orShould()
        .declareThrowableOfType(Throwable.class)
        .because("REST APIは具体的な例外を定義すべき");
```

### テンプレート5-2: セキュリティルール

```java
@ArchTest
static final ArchRule security_sensitive_methods_should_be_in_security_package = 
    methods()
        .that().haveNameMatching(".*authenticate.*|.*authorize.*|.*encrypt.*|.*decrypt.*")
        .should().beDeclaredIn(classes().that().resideInAPackage("..security.."))
        .because("セキュリティ関連メソッドはsecurityパッケージに配置すべき");
```

### テンプレート5-3: ユーティリティクラスルール

```java
@ArchTest
static final ArchRule utility_classes_should_be_final = 
    classes()
        .that().resideInAPackage("..util..")
        .or().resideInAPackage("..common..")
        .should().haveOnlyFinalFields()
        .andShould().haveOnlyPrivateConstructors()
        .because("ユーティリティクラスはfinalフィールドとprivateコンストラクタを持つべき");
```

### テンプレート5-4: 外部システム連携ルール

```java
@ArchTest
static final ArchRule external_clients_should_be_in_external_package = 
    classes()
        .that().implement(jakarta.ws.rs.client.ClientBuilder.class)
        .or().areAnnotatedWith(org.eclipse.microprofile.rest.client.inject.RegisterRestClient.class)
        .should().resideInAPackage("..external..")
        .because("外部システム連携クラスはexternalパッケージに配置すべき");
```

---

## 6. テストクラステンプレート

### 完全なテストクラステンプレート

```java
package {package_root}.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.DELETE;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.enterprise.context.ApplicationScoped;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * {テストクラスの説明}
 * 
 * {検証内容の詳細}
 */
@AnalyzeClasses(packages = "{package_root}")
public class {TestClassName} {

    /**
     * {ルールの説明}
     */
    @ArchTest
    static final ArchRule {rule_name} = 
        // ルール定義
        .because("{ルールの理由}");
    
    // 他のルール...
}
```

---

## 7. 使用例

### 例1: Berry Books APIのレイヤードアーキテクチャテスト

```java
package pro.kensait.berrybooks.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

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
        .layer("Security").definedBy("..security..")
        .layer("External").definedBy("..external..")
        
        .whereLayer("API").mayNotBeAccessedByAnyLayer()
        .whereLayer("Service").mayOnlyBeAccessedByLayers("API", "External")
        .whereLayer("DAO").mayOnlyBeAccessedByLayers("Service")
        .whereLayer("Entity").mayOnlyBeAccessedByLayers("DAO", "Service", "External");
}
```

### 例2: Back Office APIの命名規則テスト

```java
package pro.kensait.backoffice.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.ws.rs.Path;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "pro.kensait.backoffice")
public class NamingConventionTest {

    @ArchTest
    static final ArchRule resource_classes_should_be_suffixed = 
        classes()
            .that().resideInAPackage("..api..")
            .and().areAnnotatedWith(Path.class)
            .should().haveSimpleNameEndingWith("Resource")
            .because("REST APIクラスは 'Resource' サフィックスを使用する");
    
    @ArchTest
    static final ArchRule service_classes_should_be_suffixed = 
        classes()
            .that().resideInAPackage("..service..")
            .and().areNotInterfaces()
            .should().haveSimpleNameEndingWith("Service")
            .because("サービス実装クラスは 'Service' サフィックスを使用する");
    
    @ArchTest
    static final ArchRule dao_classes_should_be_suffixed = 
        classes()
            .that().resideInAPackage("..dao..")
            .should().haveSimpleNameEndingWith("Dao")
            .because("DAOクラスは 'Dao' サフィックスを使用する");
}
```

---

## 8. カスタマイズのヒント

### 8.1 プロジェクト固有のレイヤー追加

プロジェクトに特有のレイヤーがある場合は、レイヤー定義に追加します。

```java
.layer("Workflow").definedBy("..workflow..")
.whereLayer("Workflow").mayOnlyBeAccessedByLayers("Service")
```

### 8.2 特定クラスの除外

特定のクラスやパッケージを除外する場合は、`ignoreDependency()`を使用します。

```java
.layer("API").definedBy("..api..")
.ignoreDependency("pro.kensait.berrybooks.api.ApplicationConfig", Object.class)
```

### 8.3 より厳格なルール

より厳格なルールを適用する場合は、`onlyDependOnClassesThat()`を使用します。

```java
@ArchTest
static final ArchRule services_should_only_use_allowed_dependencies = 
    classes()
        .that().resideInAPackage("..service..")
        .should().onlyDependOnClassesThat()
        .resideInAnyPackage("..service..", "..dao..", "..entity..", "..dto..", "java..", "jakarta..");
```

---

## 9. 参考資料

- [ArchUnit公式ドキュメント](https://www.archunit.org/)
- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)
- [Jakarta EE 10 Specification](https://jakarta.ee/specifications/platform/10/)

---

## まとめ

このテンプレートを使用して、プロジェクトに適したArchUnitルールを定義してください。

重要なポイント:
1. プロジェクトのアーキテクチャに合わせてルールをカスタマイズ
2. `because()` メソッドでルールの意図を明示
3. 段階的にルールを追加（既存プロジェクトの場合）
4. CI/CDパイプラインに統合して継続的に検証

アーキテクチャルールは、チーム全体でのコード品質向上と保守性の改善に貢献します。
