# Cucumber BDD結合テスト生成 Agent Skill

バージョン: 1.0.0  
最終更新日: 2026-02-11

---

## 概要

Jakarta EEプロジェクトのCucumber BDD（振る舞い駆動開発）結合テストを自動生成するAgent Skillです。

このAgent Skillは、Gherkin形式でビジネスシナリオを記述し、JUnit 5 + CDI（Weld SE）を使用した結合テストコードを自動生成します。

対象プロジェクト: Jakarta EE 10 REST APIプロジェクト（Berry Books API、Back Office API等）

## クイックスタート

### 方法1: behaviors.mdから.featureファイルを生成

```
@agent_skills/cucumber-test/instructions/generate_feature_from_behaviors.md

behaviors.mdからFeatureファイルを生成してください

パラメータ
* project_path: projects/master/bookstore/berry-books-api
```

### 方法2: 完全なCucumberテストを生成

1. Cucumber依存関係がbuild.gradleに追加されていることを確認
2. `@agent_skills/cucumber-test/instructions/generate_cucumber_tests.md` でテストコード生成

```
@agent_skills/cucumber-test/instructions/generate_cucumber_tests.md

プロジェクトのCucumber結合テストを生成してください

パラメータ
* project_path: projects/master/bookstore/berry-books-api
* package_root: pro.kensait.berrybooks
```

---

## フォルダ構造

```
agent_skills/cucumber-test/
│
├── SKILL.md                                    # Agent Skill説明書（エントリポイント）
│
├── instructions/                               # 開発インストラクション
│   ├── generate_feature_from_behaviors.md     # Featureファイル生成指示（behaviors.mdから）
│   │   └─→ 読込: {project_path}/**/behaviors.md
│   │   └─→ 出力: {behavior_file_directory}/*.feature
│   │
│   └── generate_cucumber_tests.md             # 完全なテスト生成指示
│       └─→ 遵守: principles/cucumber_best_practices.md
│       └─→ 解析: {project_path}/src/main/java/
│       └─→ 読込: {feature_file}（省略可）
│       └─→ 出力: {test_output_dir}/（テストコード）
│                  {project_path}/README_CUCUMBER.md
│
├── principles/                                 # 開発原則（全プロジェクト共通）
│   └── cucumber_best_practices.md             # Cucumberベストプラクティス
│                                               - Gherkinシナリオ作成
│                                               - Step Definitions実装
│                                               - CDI統合
│                                               - データベーステスト
│                                               - トランザクション管理
│
└── templates/                                  # テンプレート
    └── cucumber_feature_template.md           # Featureファイルテンプレート
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
│   │           └── entity/                    # JPA エンティティ
│   │
│   └── test/
│       ├── java/
│       │   └── {package_root}/
│       │       ├── cucumber/                  # Cucumberテスト
│       │       │   ├── CucumberIntegrationTestRunner.java
│       │       │   ├── steps/                 # Step Definitions
│       │       │   │   ├── OrderManagementSteps.java
│       │       │   │   ├── CustomerManagementSteps.java
│       │       │   │   └── CommonSteps.java
│       │       │   └── support/               # テストサポート
│       │       │       ├── TestContext.java
│       │       │       ├── TestDatabase.java
│       │       │       └── Hooks.java
│       │       ├── service/                   # 単体テスト
│       │       └── architecture/              # ArchiUnitテスト
│       │
│       └── resources/
│           └── features/                      # Featureファイル
│               ├── order_management.feature
│               ├── customer_management.feature
│               └── book_search.feature
│
└── README_CUCUMBER.md                         # テスト実行方法
```

---

## Cucumber BDDテストの特徴

### 1. Gherkin形式でのシナリオ記述

ビジネス要件を自然言語（日本語可）で記述します。

```gherkin
# language: ja
機能: 注文管理

  シナリオ: 顧客が書籍を注文する
    前提 顧客"alice@example.com"でログインしている
    かつ 書籍ID 1 の在庫が10冊ある
    もし 書籍ID 1 を2冊カートに追加する
    かつ 注文を確定する
    ならば 注文が成功する
    かつ 書籍ID 1 の在庫が8冊になる
```

### 2. JUnit 5統合

JUnit 5のテストランナーとして動作します。

```java
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "pro.kensait.berrybooks.cucumber.steps")
public class CucumberIntegrationTestRunner {
}
```

### 3. CDI統合（Weld SE）

Weld SEを使用してCDI Beanを起動し、実際のサービス層・DAO層をテストします。

```java
@ApplicationScoped
public class TestContext {
    @Inject
    private OrderService orderService;
    
    @Inject
    private EntityManager em;
    
    // テストコンテキストの管理
}
```

### 4. データベーステスト

テスト用のEntityManagerを使用し、各シナリオ後にロールバックします。

```java
@Before
public void beforeScenario() {
    em.getTransaction().begin();
}

@After
public void afterScenario() {
    if (em.getTransaction().isActive()) {
        em.getTransaction().rollback();
    }
}
```

---

## 生成されるファイル

### 1. Featureファイル（Gherkin）

```gherkin
# language: ja
機能: 注文管理

  背景:
    前提 テストデータベースが初期化されている

  シナリオ: 正常な注文処理
    前提 顧客"alice@example.com"でログインしている
    かつ 以下の書籍が在庫に存在する
      | 書籍ID | 書籍名       | 価格  | 在庫数 |
      | 1      | Java入門     | 3000  | 10     |
      | 2      | Python基礎   | 2800  | 5      |
    もし 以下の書籍をカートに追加する
      | 書籍ID | 冊数 |
      | 1      | 2    |
      | 2      | 1    |
    かつ 注文を確定する
    ならば 注文が成功する
    かつ 注文番号が発行される
    かつ 在庫が以下のように減少する
      | 書籍ID | 在庫数 |
      | 1      | 8      |
      | 2      | 4      |

  シナリオ: 在庫不足による注文失敗
    前提 顧客"alice@example.com"でログインしている
    かつ 書籍ID 1 の在庫が1冊ある
    もし 書籍ID 1 を2冊カートに追加する
    かつ 注文を確定する
    ならば 注文が失敗する
    かつ エラーメッセージ"在庫不足"が表示される
```

### 2. Step Definitionsクラス

```java
package pro.kensait.berrybooks.cucumber.steps;

import io.cucumber.java.ja.*;
import io.cucumber.datatable.DataTable;
import jakarta.inject.Inject;
import pro.kensait.berrybooks.service.order.OrderService;
import pro.kensait.berrybooks.cucumber.support.TestContext;

import static org.junit.jupiter.api.Assertions.*;

public class OrderManagementSteps {

    @Inject
    private TestContext testContext;
    
    @Inject
    private OrderService orderService;

    @前提("顧客{string}でログインしている")
    public void loginAsCustomer(String email) {
        testContext.setCurrentCustomer(email);
    }

    @かつ("書籍ID {int} の在庫が{int}冊ある")
    public void setStockForBook(int bookId, int stock) {
        testContext.setStock(bookId, stock);
    }

    @もし("書籍ID {int} を{int}冊カートに追加する")
    public void addBookToCart(int bookId, int count) {
        testContext.addToCart(bookId, count);
    }

    @かつ("注文を確定する")
    public void confirmOrder() {
        try {
            testContext.setOrder(orderService.createOrder(
                testContext.getCartItems(),
                testContext.getCurrentCustomer()
            ));
        } catch (Exception e) {
            testContext.setException(e);
        }
    }

    @ならば("注文が成功する")
    public void orderShouldSucceed() {
        assertNull(testContext.getException());
        assertNotNull(testContext.getOrder());
    }
}
```

### 3. テストランナークラス

```java
package pro.kensait.berrybooks.cucumber;

import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:build/reports/cucumber/cucumber.html, json:build/reports/cucumber/cucumber.json")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "pro.kensait.berrybooks.cucumber.steps")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@integration")
public class CucumberIntegrationTestRunner {
}
```

### 4. テストコンテキスト管理クラス

```java
package pro.kensait.berrybooks.cucumber.support;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.*;

@ApplicationScoped
public class TestContext {
    @Inject
    private EntityManager em;
    
    private String currentCustomer;
    private List<CartItem> cartItems = new ArrayList<>();
    private Order order;
    private Exception exception;
    
    // Getters and Setters
    
    public void clear() {
        currentCustomer = null;
        cartItems.clear();
        order = null;
        exception = null;
    }
}
```

### 5. Hooks（テストライフサイクル管理）

```java
package pro.kensait.berrybooks.cucumber.support;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

public class Hooks {
    
    @Inject
    private EntityManager em;
    
    @Inject
    private TestContext testContext;
    
    @Before
    public void beforeScenario() {
        // トランザクション開始
        em.getTransaction().begin();
        testContext.clear();
    }
    
    @After
    public void afterScenario() {
        // トランザクションロールバック
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}
```

---

## テスト実行方法

生成されたテストを実行するには:

```bash
# 結合テスト（Cucumberテスト）を実行
./gradlew :berry-books-api:integrationTest

# 特定のFeatureファイルのみ実行（タグ指定）
./gradlew :berry-books-api:integrationTest -Dcucumber.filter.tags="@order"

# HTMLレポートを生成
./gradlew :berry-books-api:integrationTest
# build/reports/cucumber/cucumber.html を開く
```

---

## ベストプラクティス

### 1. Gherkinシナリオの記述

- ビジネス言語で記述: 技術的な詳細は避ける
- Given-When-Then: 前提条件、アクション、期待結果を明確に
- データテーブル: 複数のデータを扱う場合は表形式で

### 2. Step Definitionsの実装

- 再利用可能なステップ: 共通ステップは`CommonSteps`に集約
- パラメータ化: 正規表現でパラメータを受け取る
- アサーション: JUnit Assertionsを使用

### 3. CDI Beanのテスト

- Weld SE: CDIコンテナを起動
- 依存性注入: `@Inject`でサービス層・DAO層を注入
- スコープ: `@ApplicationScoped`でシングルトン

### 4. データベーステスト

- トランザクション管理: 各シナリオ後にロールバック
- テストデータ: Featureファイルで定義
- 独立性: 各シナリオは独立して実行可能

詳細は [Cucumberベストプラクティス](principles/cucumber_best_practices.md) を参照してください。

---

## トラブルシューティング

### CDI Beanが見つからない

症状: `UnsatisfiedResolutionException` エラー

原因: beans.xmlが不足またはCDI Beanが正しくスキャンされていない

解決策: 
- `src/test/resources/META-INF/beans.xml` を作成
- `@ApplicationScoped` アノテーションを確認

### トランザクションがロールバックされない

症状: テストデータがデータベースに残る

原因: トランザクション管理が不適切

解決策: 
- `@Before`と`@After`フックでトランザクションを管理
- `em.getTransaction().rollback()` を確実に呼び出す

### Featureファイルが見つからない

症状: `No features found` エラー

原因: Featureファイルのパスが間違っている

解決策: 
- `src/test/resources/features/` にFeatureファイルを配置
- `@SelectClasspathResource("features")` を確認

---

## 参考

* [SKILL.md](SKILL.md) - エントリポイント、クイックリファレンス
* [Cucumberベストプラクティス](principles/cucumber_best_practices.md)
* [Featureファイルテンプレート](templates/cucumber_feature_template.md)
* [Cucumber 公式ドキュメント](https://cucumber.io/docs/cucumber/)
* [Gherkin Reference](https://cucumber.io/docs/gherkin/reference/)
