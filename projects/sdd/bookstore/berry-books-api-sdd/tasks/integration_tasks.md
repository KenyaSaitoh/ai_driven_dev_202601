# 結合テストタスク

担当者: 全員  
推奨スキル: JUnit 5、REST Assured、E2Eテスト、API統合テスト  
想定工数: 6時間  
依存タスク: [API_001_auth.md](API_001_auth.md), [API_002_books.md](API_002_books.md), [API_003_orders.md](API_003_orders.md), [API_004_images.md](API_004_images.md)

---

## タスク一覧

### テスト環境準備

* [ ] T_INTEG_001: E2Eテスト設定の追加
  * 目的: E2Eテストをビルドプロセスから分離し、個別実行可能にする
  * 対象: build.gradle
  * 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「12. テスト戦略」
  * 注意事項: @Tag("e2e")でタグ付けされたテストを除外、別タスクで実行可能にする

---

* [ ] T_INTEG_002: テストデータ準備スクリプト
  * 目的: E2Eテスト用のテストデータを準備する
  * 対象: src/test/resources/db/test_data.sql
  * 参照SPEC: [data_model.md](../specs/baseline/system/data_model.md) の「8.1 初期データ投入」
  * 注意事項: 注文データのサンプルを作成（書籍・顧客データは外部APIで管理）

---

### API間結合テスト

* [ ] [P] T_INTEG_003: 認証→書籍検索結合テスト
  * 目的: 認証後に書籍検索が正常に動作することを確認する
  * 対象: AuthBookIntegrationTest.java（JUnit 5 + REST Assured）
  * 参照SPEC: 
    * [behaviors.md](../specs/baseline/system/behaviors.md) の「2. 認証API」「3. 書籍API」
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「7. データフロー」
  * 注意事項: ログイン → JWT Cookie取得 → 書籍検索のシナリオテスト

---

* [ ] [P] T_INTEG_004: 認証→注文作成結合テスト
  * 目的: 認証後に注文作成が正常に動作することを確認する
  * 対象: AuthOrderIntegrationTest.java（JUnit 5 + REST Assured）
  * 参照SPEC: 
    * [behaviors.md](../specs/baseline/system/behaviors.md) の「4.1 注文作成」
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「7.1 注文処理全体フロー」
  * 注意事項: ログイン → 書籍検索 → 注文作成 → 注文履歴取得のシナリオテスト

---

### E2E APIテスト（主要業務フロー）

* [ ] T_INTEG_005: 新規登録→ログイン→注文E2Eテスト
  * 目的: 主要な業務フロー全体をE2Eテストで確認する
  * 対象: OrderFlowE2ETest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [functional_design.md](../specs/baseline/system/functional_design.md) の「7.1 注文処理全体フロー」
  * 注意事項: 新規登録 → ログイン → 書籍検索 → 注文作成 → 注文履歴取得のシナリオテスト、@Tag("e2e")でタグ付け

---

* [ ] T_INTEG_006: 在庫不足エラーE2Eテスト
  * 目的: 在庫不足時のエラーハンドリングをE2Eテストで確認する
  * 対象: OutOfStockE2ETest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [behaviors.md](../specs/baseline/system/behaviors.md) の「4.1.2 異常系」
  * 注意事項: 在庫数を超える注文を試行、409 Conflictエラーを確認、@Tag("e2e")でタグ付け

---

### 楽観的ロックテスト

* [ ] T_INTEG_007: 楽観的ロック競合E2Eテスト
  * 目的: 楽観的ロック競合時のエラーハンドリングをE2Eテストで確認する
  * 対象: OptimisticLockE2ETest.java（JUnit 5 + REST Assured）
  * 参照SPEC: 
    * [behaviors.md](../specs/baseline/system/behaviors.md) の「9. 並行制御（楽観的ロック）」
    * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「8. 並行制御」
  * 注意事項: 2人のユーザーが同じ書籍を同時に注文、VERSION不一致で409 Conflictエラーを確認、@Tag("e2e")でタグ付け

---

* [ ] T_INTEG_008: トランザクションロールバックテスト
  * 目的: エラー発生時にトランザクションが正常にロールバックされることを確認する
  * 対象: TransactionRollbackTest.java（JUnit 5）
  * 参照SPEC: [behaviors.md](../specs/baseline/system/behaviors.md) の「8. トランザクション管理」
  * 注意事項: 複数書籍注文時に2冊目で在庫不足、1冊目の在庫減算もロールバックされることを確認

---

### JWT認証・認可テスト

* [ ] [P] T_INTEG_009: JWT認証必須エンドポイントテスト
  * 目的: JWT認証が必要なエンドポイントが正しく保護されていることを確認する
  * 対象: JwtAuthenticationTest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [behaviors.md](../specs/baseline/system/behaviors.md) の「6. JWT認証フィルター」
  * 注意事項: JWT Cookie未設定で認証必須APIにアクセス、401 Unauthorizedエラーを確認

---

* [ ] [P] T_INTEG_010: JWT有効期限テスト
  * 目的: JWTの有効期限が切れた場合に認証エラーが発生することを確認する
  * 対象: JwtExpirationTest.java（JUnit 5）
  * 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「6.2 JWT設定」
  * 注意事項: 期限切れJWTで認証必須APIにアクセス、401 Unauthorizedエラーを確認

---

* [ ] [P] T_INTEG_011: JWT改ざん検証テスト
  * 目的: JWTが改ざんされた場合に認証エラーが発生することを確認する
  * 対象: JwtTamperingTest.java（JUnit 5）
  * 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「6.4 セキュリティ対策」
  * 注意事項: JWTを改ざんして認証必須APIにアクセス、401 Unauthorizedエラーを確認

---

### セキュリティテスト

* [ ] [P] T_INTEG_012: CORS動作確認テスト（オプション）
  * 目的: CORS設定が正しく動作することを確認する
  * 対象: CorsTest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [requirements.md](../specs/baseline/system/requirements.md) の「6.2 セキュリティ要件」
  * 注意事項: Originヘッダーを含むリクエスト、Access-Control-Allow-Originヘッダーを確認

---

### パフォーマンステスト（オプション）

* [ ] [P] T_INTEG_013: APIレスポンスタイムテスト
  * 目的: 主要APIのレスポンスタイムが要件を満たすことを確認する
  * 対象: PerformanceTest.java（JUnit 5）
  * 参照SPEC: [behaviors.md](../specs/baseline/system/behaviors.md) の「10. パフォーマンス受入基準」
  * 注意事項: 全書籍取得、書籍検索、注文作成のレスポンスタイムを計測、500ms以内を確認

---

* [ ] [P] T_INTEG_014: スループットテスト（オプション）
  * 目的: 同時リクエスト処理能力が要件を満たすことを確認する
  * 対象: ThroughputTest.java（JUnit 5）
  * 参照SPEC: [behaviors.md](../specs/baseline/system/behaviors.md) の「10.2 スループット」
  * 注意事項: 100 req/sec以上を処理できることを確認

---

### 外部API連携テスト

* [ ] [P] T_INTEG_015: back-office-api連携テスト
  * 目的: back-office-apiとの連携が正常に動作することを確認する
  * 対象: BackOfficeIntegrationTest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [external_interface.md](../specs/baseline/system/external_interface.md) の「14. back-office-api連携」
  * 注意事項: 書籍一覧取得、在庫更新が正常に動作することを確認

---

* [ ] [P] T_INTEG_016: customer-hub-api連携テスト
  * 目的: customer-hub-apiとの連携が正常に動作することを確認する
  * 対象: CustomerHubIntegrationTest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [external_interface.md](../specs/baseline/system/external_interface.md) の「3. customer-hub-api連携」
  * 注意事項: 顧客検索、顧客登録が正常に動作することを確認

---

### 最終検証

* [ ] T_INTEG_017: 全機能統合テスト実行
  * 目的: 全てのE2Eテストを実行し、システム全体が正常に動作することを確認する
  * 対象: Gradleタスク実行
  * 参照SPEC: [README.md](../README.md) の「🧪 テスト」
  * 注意事項: `./gradlew :berry-books-api-sdd:test --tests "*E2ETest"` を実行

---

* [ ] T_INTEG_018: テストレポートの確認
  * 目的: テスト結果を確認し、全テストが成功していることを確認する
  * 対象: build/reports/tests/test/index.html
  * 参照SPEC: [README.md](../README.md) の「🧪 テスト」
  * 注意事項: HTMLレポートで全テストの成功を確認

---

* [ ] T_INTEG_019: カバレッジレポートの確認（オプション）
  * 目的: テストカバレッジを確認し、目標値を達成していることを確認する
  * 対象: build/reports/jacoco/test/html/index.html
  * 参照SPEC: [requirements.md](../specs/baseline/system/requirements.md) の「9.3 品質要件の充足」
  * 注意事項: サービス層のカバレッジが80%以上であることを確認

---

## テスト実行環境

### 前提条件

* [ ] HSQLDBサーバーが起動している
* [ ] Payara Serverが起動している
* [ ] データソース（jdbc/HsqldbDS）が作成されている
* [ ] back-office-apiが起動している（http://localhost:8080/back-office-api-sdd/api）
* [ ] customer-hub-apiが起動している（http://localhost:8080/customer-hub-api/customers）
* [ ] berry-books-api-sddがデプロイされている（http://localhost:8080/berry-books-api-sdd/api）

### テスト実行方法

ユニットテスト（E2Eテスト除く）:
```bash
./gradlew :berry-books-api-sdd:test
```

E2Eテストのみ:
```bash
./gradlew :berry-books-api-sdd:test --tests "*E2ETest"
```

カバレッジレポート生成:
```bash
./gradlew :berry-books-api-sdd:jacocoTestReport
```

---

## 参考資料

* [behaviors.md](../specs/baseline/system/behaviors.md) - 振る舞い仕様書（受入基準）
* [architecture_design.md](../specs/baseline/system/architecture_design.md) - テスト戦略
* [functional_design.md](../specs/baseline/system/functional_design.md) - データフロー
* [external_interface.md](../specs/baseline/system/external_interface.md) - 外部API連携仕様
* [README.md](../README.md) - テスト実行方法
