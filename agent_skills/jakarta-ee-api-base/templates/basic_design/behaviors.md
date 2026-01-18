# [PROJECT_NAME] - Service層以下振る舞い仕様書

テンプレートパス: templates/basic_design/behaviors.md  
コピー先: {spec_directory}/basic_design/behaviors.md  
システム名: [SYSTEM_NAME]  
バージョン: 1.0.0  
最終更新日: [DATE]

---

## 1. 概要

本文書は、[PROJECT_NAME]システムの基本設計を外形的に捉えた振る舞い仕様書である。結合テスト用のシナリオを記述し、Service層以下（Service + DAO + Entity + DB）の受入基準を定義する。

**テスト対象:**
* Service層のビジネスロジック
* DAO層のデータアクセス
* Entity（JPA）のマッピング
* 実際のDB操作（メモリDB）
* 外部API呼び出し（WireMockでスタブ化）

**テスト対象外:**
* API層（Resource、JAX-RS） → requirements/behaviors.mdで記述
* HTTPリクエスト/レスポンス → requirements/behaviors.mdで記述
* 認証・認可（JWT、Cookie） → requirements/behaviors.mdで記述

**関連ドキュメント:**
* [../requirements/behaviors.md](../requirements/behaviors.md) - E2Eテスト用の振る舞い仕様書（API層を含む全体）
* [../requirements/requirements.md](../requirements/requirements.md) - 要件定義書
* [functional_design.md](functional_design.md) - システム機能設計書
* [architecture_design.md](architecture_design.md) - アーキテクチャ設計書
* [data_model.md](data_model.md) - データモデル仕様書

---

## 2. Service層のビジネスロジックシナリオ

### 2.1 [Service名] - [サービス説明]

#### シナリオ: [シナリオタイトル]

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| [前提条件を記述] | [Serviceメソッドを呼び出し] | [期待される結果を記述] |

**例:**

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| DBに書籍が存在:<br/>- bookId=1, bookName="Java完全理解", categoryId=1 | BookService.findById(bookId=1) | 書籍エンティティが取得される:<br/>- book.bookName="Java完全理解"<br/>- book.category.categoryName="プログラミング" |

---

## 3. DAO層のデータアクセスシナリオ

### 3.1 [Dao名] - [DAO説明]

#### シナリオ: [シナリオタイトル]

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| [前提条件を記述] | [Daoメソッドを呼び出し] | [期待される結果を記述] |

**例:**

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| DBに書籍が複数存在 | BookDao.searchByJpql(categoryId=1, keyword="Java") | JPQLクエリが正しく生成・実行される:<br/>"SELECT b FROM Book b WHERE b.categoryId = :categoryId AND b.bookName LIKE :keyword" |

---

## 4. トランザクション管理シナリオ

### 4.1 トランザクションロールバック

#### シナリオ: 例外発生時のロールバック

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| [エンティティが存在] | [Serviceメソッド内で例外発生] | トランザクション全体がロールバックされる:<br/>- DBの状態は変更されない |

**例:**

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Book(id=1) | BookService.update() 内で:<br/>1. Bookのname更新<br/>2. 例外発生（ValidationException） | トランザクション全体がロールバックされる:<br/>- Bookのnameは変更されない |

---

## 5. 外部API連携シナリオ（WireMock）

### 5.1 [外部API名] 連携

#### シナリオ: [シナリオタイトル]

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが以下をスタブ:<br/>- [HTTP_METHOD] [PATH]<br/>- Response: [STATUS_CODE], [BODY] | [Serviceメソッドを呼び出し] | 外部APIが呼ばれる:<br/>- URL: [URL]<br/>- Method: [METHOD]<br/>レスポンスが正しく処理される |

**例:**

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが以下をスタブ:<br/>- PUT /api/stocks/1<br/>- Response: 200 OK, {quantity: 15, version: 2} | StockService.updateStock(bookId=1, quantity=15, version=1) | 外部APIが呼ばれる:<br/>- URL: http://localhost:8089/api/stocks/1<br/>- Method: PUT<br/>- Body: {quantity: 15, version: 1} |

#### シナリオ: 外部API呼び出しエラー

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| WireMockが以下をスタブ:<br/>- [HTTP_METHOD] [PATH]<br/>- Response: [ERROR_STATUS], [ERROR_BODY] | [Serviceメソッドを呼び出し] | [Exception]がスローされる<br/>エラーメッセージ: "[MESSAGE]" |

---

## 6. バリデーションシナリオ

### 6.1 Bean Validation

#### シナリオ: エンティティのバリデーション

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| [エンティティに不正な値] | [Serviceメソッドを呼び出し] | ConstraintViolationExceptionがスローされる:<br/>- [field1]: "[error_message1]"<br/>- [field2]: "[error_message2]" |

**例:**

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Book(bookName="", price=-100) | BookService.createBook(book) | ConstraintViolationExceptionがスローされる:<br/>- bookName: "must not be blank"<br/>- price: "must be greater than or equal to 0" |

### 6.2 ビジネスルールバリデーション

#### シナリオ: [ビジネスルール名]

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| [前提条件] | [ビジネスルール違反の操作] | BusinessRuleViolationExceptionがスローされる:<br/>"[エラーメッセージ]" |

---

## 7. エンティティリレーションシナリオ

### 7.1 [Entity1] → [Entity2] (リレーション種別)

#### シナリオ: [シナリオタイトル]

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| [関連エンティティが存在] | [エンティティを取得] | [リレーションが正しく取得される] |

**例（OneToMany）:**

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Category(id=1)<br/>Book(id=1, categoryId=1)<br/>Book(id=2, categoryId=1) | CategoryDao.findById(1) | Categoryエンティティが取得され、<br/>category.books.size() == 2<br/>（Lazy Loadingで書籍リストも取得） |

**例（ManyToOne）:**

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Book(id=1, categoryId=1)<br/>Category(id=1, name="プログラミング") | BookDao.findById(1) | Bookエンティティが取得され、<br/>book.category.categoryName="プログラミング"<br/>（Lazy Loadingでカテゴリも取得） |

---

## 8. 性能関連シナリオ

### 8.1 N+1問題の回避

#### シナリオ: JOIN FETCHによる一括取得

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| [大量のエンティティとリレーション] | [JOIN FETCHを使用したクエリ] | SQLクエリが最小限で実行される:<br/>"[SQL]"<br/>N+1問題が発生しない |

**例:**

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Book 100件、各Bookに1つのCategory | BookDao.findAllWithCategory()<br/>（JOIN FETCH b.category） | SQLクエリが1回のみ実行される:<br/>"SELECT b, c FROM Book b JOIN FETCH b.category c"<br/>N+1問題が発生しない |

---

## 9. 楽観的ロックシナリオ

### 9.1 @Versionフィールドによる楽観的ロック

#### シナリオ: 楽観的ロック競合検知

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| [エンティティが存在、version=N] | [古いバージョンで更新試行] | OptimisticLockExceptionがスローされる<br/>DBの状態は更新されない |

**例:**

| Given（前提条件） | When（操作） | Then（期待結果） |
|----------------|------------|---------------|
| Stock(id=1, quantity=10, version=2) | StockService.updateStock(stock) で<br/>version=1で更新試行 | OptimisticLockExceptionがスローされる |

---

## 10. テストデータ準備

各テストケースでは、以下の方針でテストデータを準備する：

1. **@BeforeEach で初期データ投入**
   - EntityManagerを使用してエンティティを永続化
   - em.flush()で強制的にDBに反映

2. **@AfterEach でロールバック**
   - トランザクションロールバックで自動クリーンアップ
   - 次のテストへの影響を防ぐ

3. **テストデータの一意性**
   - UUIDやタイムスタンプを使用して一意なデータを生成
   - テスト間の干渉を防ぐ

---

## 11. WireMockスタブ設定例

### 11.1 [API名]（成功）

```java
@BeforeEach
void setupWireMock() {
    stubFor(get(urlEqualTo("/api/[resource]/1"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"id\": 1, \"name\": \"value\"}")));
}
```

### 11.2 [API名]（エラー）

```java
stubFor(get(urlEqualTo("/api/[resource]/1"))
    .willReturn(aResponse()
        .withStatus(400)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"message\": \"エラーメッセージ\"}")));
```

---

## 12. テスト実行コマンド

```bash
# 結合テストのみ実行
./gradlew :[project-name]:integrationTest

# 単体テスト→結合テスト→E2Eテストの順に実行
./gradlew :[project-name]:test integrationTest e2eTest
```

---

## 13. 参考情報

* **requirements/behaviors.md**: E2Eテスト用の受入基準（API層を含む全体フロー）
* **functional_design.md**: 機能設計書
* **data_model.md**: データモデル仕様書
* **agent_skills/jakarta-ee-api-base/instructions/it_generation.md**: 結合テスト生成指示書
