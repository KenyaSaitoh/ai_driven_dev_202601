# ArchUnitベストプラクティス

バージョン: 1.0.0  
最終更新日: 2026-02-11

---

## 概要

このドキュメントは、Jakarta EEプロジェクトでArchUnitを使用する際のベストプラクティスをまとめています。

---

## 1. レイヤードアーキテクチャのルール定義

### 1.1 基本原則

Jakarta EEプロジェクトでは、以下のレイヤー構造を採用します：

```
API/Resource層（JAX-RS）
    ↓ 依存可能
Service層（ビジネスロジック）
    ↓ 依存可能
DAO層（データアクセス）
    ↓ 依存可能
Entity層（JPA エンティティ）
```

### 1.2 レイヤー依存関係ルール

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

### 1.3 DTOとEntityの分離

原則: DTOとEntityは明確に分離すべき

理由:
- DTOはAPI層での入出力に使用
- EntityはDB層での永続化に使用
- 分離により、API変更がDB構造に影響しない

```java
@ArchTest
static final ArchRule dto_should_not_depend_on_entity = 
    noClasses()
        .that().resideInAPackage("..dto..")
        .should().dependOnClassesThat().resideInAPackage("..entity..")
        .because("DTOとEntityは分離されるべき");
```

---

## 2. 命名規則

### 2.1 クラス名のサフィックス

| レイヤー | サフィックス | 例 |
|---------|------------|-----|
| REST API | `*Resource` | `BookResource` |
| サービス | `*Service`, `*ServiceIF` | `OrderService`, `OrderServiceIF` |
| DAO | `*Dao` | `BookDao` |
| Entity | （パッケージで識別） | `Book`, `Order` |
| DTO | `*TO`, `*Request`, `*Response` | `BookTO`, `LoginRequest` |

### 2.2 命名規則ルール

```java
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
```

---

## 3. アノテーション使用ルール

### 3.1 JAX-RS（REST API層）

必須アノテーション:
- `@Path` - エンドポイントのパス定義
- `@GET`, `@POST`, `@PUT`, `@DELETE` - HTTPメソッド
- `@Produces`, `@Consumes` - メディアタイプ

```java
@ArchTest
static final ArchRule resource_classes_should_be_annotated_with_path = 
    classes()
        .that().resideInAPackage("..api..")
        .and().haveSimpleNameEndingWith("Resource")
        .should().beAnnotatedWith(Path.class)
        .because("REST APIクラスは @Path アノテーションを持つ");
```

### 3.2 CDI（依存性注入）

必須アノテーション:
- `@ApplicationScoped` - サービス層、DAO層
- `@Inject` - 依存性注入

```java
@ArchTest
static final ArchRule service_classes_should_be_annotated_with_application_scoped = 
    classes()
        .that().resideInAPackage("..service..")
        .and().haveSimpleNameEndingWith("Service")
        .and().areNotInterfaces()
        .should().beAnnotatedWith(ApplicationScoped.class)
        .because("サービスクラスは @ApplicationScoped アノテーションを持つ");
```

### 3.3 JPA（Entity層）

必須アノテーション:
- `@Entity` - エンティティクラス
- `@Table` - テーブルマッピング
- `@Id` - 主キー

```java
@ArchTest
static final ArchRule entity_classes_should_be_annotated_with_entity = 
    classes()
        .that().resideInAPackage("..entity..")
        .and().areNotInterfaces()
        .and().areNotEnums()
        .should().beAnnotatedWith(Entity.class)
        .because("Entityクラスは @Entity アノテーションを持つ");
```

---

## 4. パッケージ構造

### 4.1 標準パッケージ構造

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

### 4.2 パッケージ依存関係ルール

```java
// EntityはAPIに依存してはならない
@ArchTest
static final ArchRule entity_should_not_depend_on_api = 
    noClasses()
        .that().resideInAPackage("..entity..")
        .should().dependOnClassesThat().resideInAPackage("..api..")
        .because("Entityは独立したドメインモデルであるべき");

// EntityはDTOに依存してはならない
@ArchTest
static final ArchRule entity_should_not_depend_on_dto = 
    noClasses()
        .that().resideInAPackage("..entity..")
        .should().dependOnClassesThat().resideInAPackage("..dto..")
        .because("Entityは独立したドメインモデルであるべき");
```

### 4.3 サイクル依存の検出

```java
@ArchTest
static final ArchRule no_cycles_by_method_calls_between_slices = 
    slices()
        .matching("pro.kensait.berrybooks.(*)..")
        .should().beFreeOfCycles()
        .because("サイクル依存は複雑性を増加させる");
```

---

## 5. カスタムルールの作成

### 5.1 独自のルール定義

プロジェクト固有のルールは、カスタムルールとして定義できます。

```java
@ArchTest
static final ArchRule services_should_only_call_daos_in_same_domain = 
    classes()
        .that().resideInAPackage("..service..")
        .should()
        .onlyAccessClassesThat(
            new DescribedPredicate<JavaClass>("are in the same domain or common packages") {
                @Override
                public boolean test(JavaClass javaClass) {
                    String packageName = javaClass.getPackageName();
                    return packageName.contains(".service.") 
                        || packageName.contains(".dao.") 
                        || packageName.contains(".entity.")
                        || packageName.contains(".dto.")
                        || packageName.contains(".common.");
                }
            }
        )
        .because("サービスは同一ドメイン内のDAO、Entity、DTOのみにアクセスすべき");
```

### 5.2 条件付きルール

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

---

## 6. テストの実行とCI/CD統合

### 6.1 テスト実行

```bash
# すべてのテストを実行
./gradlew test

# ArchUnitテストのみ実行
./gradlew test --tests "*architecture.*"

# 特定のテストクラスのみ実行
./gradlew test --tests "*LayeredArchitectureTest"
```

### 6.2 CI/CDパイプラインへの統合

ArchUnitテストは通常のJUnitテストと同様に実行されるため、CI/CDパイプラインに簡単に統合できます。

```yaml
# GitHub Actions の例
- name: Run Architecture Tests
  run: ./gradlew test --tests "*architecture.*"
```

### 6.3 テスト失敗時の対応

テストが失敗した場合:
1. ログから違反箇所を特定
2. アーキテクチャルールに違反していないか確認
3. 違反している場合は、コードを修正
4. ルールが不適切な場合は、ルールを調整

---

## 7. ベストプラクティスのまとめ

### 7.1 ルール定義

- 明確なエラーメッセージ: `because()` メソッドでルールの意図を明示
- 適切なグループ化: 関連するルールは同じテストクラスにまとめる
- 段階的な導入: 既存プロジェクトには段階的にルールを追加

### 7.2 パフォーマンス

- クラス読み込みの最適化: `@AnalyzeClasses` でスキャン範囲を限定
- キャッシュの活用: ArchUnitは自動的にクラス情報をキャッシュ
- 並列実行: JUnit 5の並列実行機能を活用

### 7.3 保守性

- ドキュメント化: 各ルールの意図をコメントで記述
- テストの独立性: 各テストは独立して実行できるように
- 継続的な改善: プロジェクトの成長に合わせてルールを見直し

---

## 8. よくある問題と解決策

### 8.1 テストが遅い

原因: スキャン範囲が広すぎる

解決策:
```java
@AnalyzeClasses(packages = "pro.kensait.berrybooks", 
                importOptions = {ImportOption.DoNotIncludeTests.class})
```

### 8.2 誤検知

原因: ルールが厳しすぎる、または不適切

解決策: 特定のクラスを除外
```java
@ArchTest
static final ArchRule layer_dependencies = layeredArchitecture()
    .consideringAllDependencies()
    .layer("API").definedBy("..api..")
    .ignoreDependency("pro.kensait.berrybooks.api.SpecialResource", 
                      "pro.kensait.berrybooks.entity.Book");
```

### 8.3 既存プロジェクトへの導入

段階的導入:
1. まず、命名規則から検証開始
2. 次に、アノテーションルールを追加
3. 最後に、レイヤー依存関係ルールを追加

```java
// 段階1: 命名規則のみ
@ArchTest
static final ArchRule naming_conventions = classes()
    .that().resideInAPackage("..service..")
    .should().haveSimpleNameEndingWith("Service");

// 段階2: アノテーションルールを追加
@ArchTest
static final ArchRule annotation_rules = classes()
    .that().resideInAPackage("..service..")
    .should().beAnnotatedWith(ApplicationScoped.class);

// 段階3: レイヤー依存関係ルールを追加
@ArchTest
static final ArchRule layer_dependencies = layeredArchitecture()
    .consideringAllDependencies()
    // ... レイヤー定義
```

---

## 9. 参考資料

- [ArchUnit公式ドキュメント](https://www.archunit.org/)
- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)
- [Jakarta EE 10 Specification](https://jakarta.ee/specifications/platform/10/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

---

## 10. まとめ

ArchUnitを活用することで、以下のメリットが得られます：

1. アーキテクチャの自動検証: コードレビューの負担軽減
2. 継続的なアーキテクチャ改善: CI/CDパイプラインでの自動チェック
3. チーム全体での規約統一: ルールの可視化と共有
4. リファクタリングの安全性向上: 意図しない依存関係の検出

プロジェクトのアーキテクチャに合わせて、適切なルールを定義し、継続的に改善していくことが重要です。
