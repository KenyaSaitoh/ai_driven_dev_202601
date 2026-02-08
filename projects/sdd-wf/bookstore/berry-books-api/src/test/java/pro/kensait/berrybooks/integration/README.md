# berry-books-api 結合テスト

## 概要

このディレクトリには、berry-books-apiプロジェクトの結合テスト（Integration Test）が含まれています。

* テストフレームワーク: JUnit 5 + Weld SE（CDIコンテナ）
* テスト対象: Service層 + DAO層 + Entity + DB（メモリDB）+ 外部API（WireMock）
* テストタグ: @Tag("integration")
* 実行方法: `gradle integrationTest`

## 結合テストの特徴

* アプリケーションサーバー不要（Weld SEでCDIコンテナを起動）
* 実際のDB操作を検証（HSQLDB メモリDB）
* 外部APIはWireMockでスタブ化
* 各テスト後にトランザクションをロールバック（データの独立性保証）
* API層（Resource）は含まない（E2Eテストで検証）

## テストクラス一覧

### BaseIntegrationTest

全結合テストの基底クラス

* Weld SE（CDIコンテナ）の起動・停止
* WireMockServer（外部APIモック）の起動・停止
* EntityManagerFactoryの作成・破棄
* テストごとのEntityManagerとトランザクション管理

### OrderServiceIntegrationTest

注文管理ドメインの結合テスト

* 対象: OrderService + OrderTranDao + OrderDetailDao + DB + 外部API
* シナリオ: specs/baseline/basic_design/orders/behaviors.md
* テストケース:
  * 注文作成（正常系）
  * 在庫不足で注文失敗
  * 楽観的ロック競合で注文失敗
  * 注文履歴取得

### BackOfficeRestClientIntegrationTest

書籍API連携ドメインの結合テスト

* 対象: 外部API呼び出し（WireMock）
* シナリオ: specs/baseline/basic_design/books_proxy/behaviors.md
* テストケース:
  * 書籍一覧を取得（正常系）
  * 外部API呼び出し失敗（500エラー）
  * 書籍詳細を取得

## テスト用設定ファイル

### src/test/resources/META-INF/persistence.xml

* persistence-unit名: test-pu
* transaction-type: RESOURCE_LOCAL
* データベース: HSQLDB メモリDB（jdbc:hsqldb:mem:testdb）
* テスト対象エンティティ: OrderTran, OrderDetail, OrderDetailPK

### src/test/resources/META-INF/beans.xml

* Jakarta EE Beans 4.0
* bean-discovery-mode: all
* CDI Beanの自動検出を有効化

## 実行方法

### 結合テストのみ実行

```bash
gradle integrationTest
```

### 単体テスト + 結合テスト

```bash
gradle test integrationTest
```

### 全テスト（単体 + 結合 + E2E）

```bash
gradle test integrationTest e2eTest
```

## テスト結果

* HTMLレポート: `build/reports/tests/integrationTest/index.html`
* XMLレポート: `build/test-results/integrationTest/*.xml`

## 依存関係

* Weld SE 5.1.0 - CDIコンテナ
* WireMock 2.35.0 - 外部APIモック
* Hibernate 6.4.0 - JPA実装
* JUnit 5 - テストフレームワーク
* HSQLDB 2.7.2 - メモリDB
* Jersey Client 3.1.3 - REST API呼び出し
* Jersey JSON Binding 3.1.3 - JSON処理

## 注意事項

* 結合テストは単体テストよりも実行時間が長い
* WireMockServerはポート8089を使用する
* テスト実行前にポート8089が空いていることを確認すること
* 各テスト後にトランザクションがロールバックされるため、データは永続化されない

## 参考資料

* [it_generation.md](../../../../../../agent_skills/jakarta-ee-api-base/instructions/it_generation.md) - 結合テスト生成インストラクション
* [architecture.md](../../../../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
* [common_rules.md](../../../../../../agent_skills/jakarta-ee-api-base/principles/common_rules.md) - 共通ルール