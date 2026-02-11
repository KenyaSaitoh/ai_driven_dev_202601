# ArchUnitアーキテクチャテスト

## 概要

このプロジェクトでは、ArchUnitを使用してアーキテクチャルールを検証しています。
Berry Books APIプロジェクトのレイヤー依存関係、命名規則、アノテーション使用ルールなどを自動的に検証し、アーキテクチャの整合性を保証します。

## テストクラス

### 1. LayeredArchitectureTest

**目的:** レイヤー依存関係を検証します。

**検証内容:**
- API層 → Service層、DTO、External、Security の依存を許可
- Service層 → DAO層、Entity、DTO、External の依存を許可
- DAO層 → Entity のみの依存を許可
- Entity層 → Common のみの依存を許可（基本的に独立）
- DTOとEntityの分離
- EntityはAPI層に依存してはならない

### 2. NamingConventionTest

**目的:** 命名規則を検証します。

**検証内容:**
- REST APIクラスは `*Resource` サフィックスを持つ
- サービス実装クラスは `*Service` サフィックスを持つ
- サービスインターフェースは `*ServiceIF` サフィックスを持つ
- DAOクラスは `*Dao` サフィックスを持つ
- Entityクラスは `entity` パッケージに配置される
- API DTOクラスは `*Request` または `*Response` サフィックスを持つ
- External DTOクラスは `*TO`、`*Request` または `*Response` サフィックスを持つ

### 3. AnnotationRulesTest

**目的:** アノテーション使用ルールを検証します。

**検証内容:**
- REST APIクラスは `@Path` アノテーションを持つ
- Entityクラスは `@Entity` アノテーションを持つ
- サービスクラスは `@ApplicationScoped` アノテーションを持つ
- DAOクラスは `@ApplicationScoped` アノテーションを持つ
- REST APIクラスは `@ApplicationScoped` アノテーションを持つ
- External Clientクラスは `@ApplicationScoped` アノテーションを持つ

### 4. PackageStructureTest

**目的:** パッケージ構造とサイクル依存を検証します。

**検証内容:**
- DTOはEntityに依存してはならない
- EntityはAPIに依存してはならない
- EntityはDTOに依存してはならない
- DAOはAPIに依存してはならない
- DAOはExternalに依存してはならない
- EntityはExternalに依存してはならない
- API DTOはExternal DTOに依存してはならない
- サイクル依存が存在してはならない

## テスト実行方法

### プロジェクトルートから実行

すべてのコマンドは、プロジェクトルート（`ai_driven_dev_202601/`）で実行してください。

### すべてのテストを実行

```bash
./gradlew :berry-books-api:test
```

### ArchUnitテストのみ実行

```bash
./gradlew :berry-books-api:test --tests "*architecture.*"
```

### 特定のテストクラスを実行

```bash
# レイヤードアーキテクチャテスト
./gradlew :berry-books-api:test --tests "*LayeredArchitectureTest"

# 命名規則テスト
./gradlew :berry-books-api:test --tests "*NamingConventionTest"

# アノテーションルールテスト
./gradlew :berry-books-api:test --tests "*AnnotationRulesTest"

# パッケージ構造テスト
./gradlew :berry-books-api:test --tests "*PackageStructureTest"
```

## アーキテクチャルール

### レイヤードアーキテクチャ

Berry Books APIプロジェクトは以下のレイヤー構造に従っています：

```
┌─────────────────────────┐
│   API/Resource層        │ ← JAX-RS REST エンドポイント
│   (@Path)               │
└───────────┬─────────────┘
            │
            ↓
┌─────────────────────────┐
│   Service層             │ ← ビジネスロジック
│   (@ApplicationScoped)  │
└───────────┬─────────────┘
            │
            ↓
┌─────────────────────────┐
│   DAO層                 │ ← データアクセス
│   (@ApplicationScoped)  │
└───────────┬─────────────┘
            │
            ↓
┌─────────────────────────┐
│   Entity層              │ ← JPA エンティティ
│   (@Entity)             │
└─────────────────────────┘

  ┌─────────────┐   ┌──────────────┐
  │   DTO       │   │  External    │
  │  (API)      │   │  (統合層)    │
  └─────────────┘   └──────────────┘

  ┌─────────────┐   ┌──────────────┐
  │  Security   │   │   Common     │
  │  (認証)     │   │  (共通)      │
  └─────────────┘   └──────────────┘
```

### 命名規則

| コンポーネント | サフィックス | 例 |
|--------------|------------|-----|
| REST APIクラス | `*Resource` | `OrderResource`, `BookResource` |
| サービス実装 | `*Service` | `OrderService`, `CustomerService` |
| サービスIF | `*ServiceIF` | `OrderServiceIF` |
| DAOクラス | `*Dao` | `OrderTranDao`, `OrderDetailDao` |
| API DTO | `*Request`, `*Response` | `OrderRequest`, `OrderResponse` |
| External DTO | `*TO`, `*Request`, `*Response` | `BookTO`, `CustomerTO` |
| エンティティ | なし | `OrderTran`, `OrderDetail` |

### パッケージ構造

```
pro.kensait.berrybooks
├── api                    # REST APIエンドポイント
│   ├── dto               # API用データ転送オブジェクト
│   └── exception         # 例外マッパー
├── service               # サービス層（ビジネスロジック）
│   ├── customer          # 顧客サービス
│   ├── delivery          # 配送サービス
│   └── order             # 注文サービス
├── dao                   # データアクセス層
├── entity                # JPAエンティティ
├── external              # 外部システム統合
│   └── dto               # 外部システム用DTO
├── security              # セキュリティ（JWT認証）
├── common                # 共通コンポーネント
└── util                  # ユーティリティ
```

## トラブルシューティング

### テストが失敗する場合

1. **ログから違反箇所を特定**
   ```
   Architecture Violation [Priority: MEDIUM] - Rule 'classes that reside in a package '..api..' 
   should not depend on classes that reside in a package '..dao..'' was violated
   ```

2. **依存関係を修正**
   - 不正な依存関係を削除
   - 適切なレイヤーを経由するように修正

3. **ルールを調整（必要に応じて）**
   - プロジェクト固有の要件に応じてルールをカスタマイズ

### よくあるエラーと対処法

#### エラー1: レイヤー依存違反

```
Entity層のクラスがService層のクラスに依存している
```

**対処法:** Entityは純粋なドメインモデルであるべきです。Serviceへの依存を削除してください。

#### エラー2: 命名規則違反

```
DAOクラスが "Dao" サフィックスを持っていない
```

**対処法:** クラス名を `*Dao` に変更してください（例: `OrderRepository` → `OrderDao`）。

#### エラー3: アノテーション不足

```
Serviceクラスに @ApplicationScoped アノテーションがない
```

**対処法:** サービスクラスに `@ApplicationScoped` アノテーションを追加してください。

### カスタムルールの追加

プロジェクト固有のルールは、各テストクラスに `@ArchTest` アノテーション付きのフィールドとして追加してください。

**例:**

```java
@ArchTest
static final ArchRule custom_rule = 
    classes()
        .that().resideInAPackage("..service..")
        .should().beAnnotatedWith(Transactional.class)
        .because("サービスクラスはトランザクション管理が必要");
```

## 依存関係

ArchUnitテストを実行するには、`build.gradle` に以下の依存関係が必要です：

```gradle
testImplementation 'com.tngtech.archunit:archunit-junit5:1.3.0'
```

すでに `berry-books-api/build.gradle` に含まれています。

## 参考資料

- [ArchUnit公式ドキュメント](https://www.archunit.org/)
- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)
- [ArchUnitベストプラクティス](../../agent_skills/archiunit-test/principles/archunit_best_practices.md)
- [Jakarta EE 10 Specification](https://jakarta.ee/specifications/platform/10/)

## 生成情報

- **生成日時:** 2026-02-12
- **プロジェクト:** berry-books-api
- **ベースパッケージ:** pro.kensait.berrybooks
- **発見されたレイヤー:** api, service, dao, entity, dto, external, security, common, util
