# 結合テストタスク

**担当者:** 全員（API実装完了後）  
**推奨スキル:** REST Assured、JAX-RS Client、JUnit 5  
**想定工数:** 8時間  
**依存タスク:** 全APIタスク完了後

---

## 概要

このタスクリストは、全API実装完了後に実施する結合テストタスクを含みます。API間の連携、E2E APIテスト、パフォーマンステスト、セキュリティテストを実施します。

**重要:** 結合テストは通常のビルドから除外し、個別実行可能にする設定（JUnit `@Tag`、Gradle設定等）を明記してください。

---

## タスクリスト

## 1. テスト環境設定

### T_INTEGRATION_001: 結合テスト用の設定

- **目的**: 結合テスト実行環境を設定する
- **対象**: 
  - `build.gradle`（Gradleタスク設定）
  - テストクラスに`@Tag("integration")`を付与
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「10. テスト」
- **注意事項**: 
  - REST Assured依存関係を追加
  - 結合テスト用のGradleタスク `integrationTest` を作成
  - `./gradlew integrationTest` で実行可能にする
  - 通常の `./gradlew test` では結合テストを除外
  - テストクラスに`@Tag("integration")`を付与して識別

---

## 2. E2E APIテスト（REST Assured）

### T_INTEGRATION_002: 認証フローのE2Eテスト

- **目的**: ログインからログアウトまでのE2Eフローをテストする
- **対象**: `pro.kensait.backoffice.integration.AuthenticationE2ETest`
- **参照SPEC**: 
  - [behaviors.md](../specs/baseline/system/behaviors.md) の「3.1 E2E-001」
- **注意事項**: 
  - `@Tag("integration")`を付与
  - REST Assured使用
  - テストケース:
    - `testLoginFlow()`: ログイン → Cookie取得 → APIアクセス
    - `testLogoutFlow()`: ログアウト → Cookie削除

---

### T_INTEGRATION_003: [P] 書籍検索フローのE2Eテスト

- **目的**: 書籍一覧取得、検索、詳細取得のE2Eフローをテストする
- **対象**: `pro.kensait.backoffice.integration.BookSearchE2ETest`
- **参照SPEC**: 
  - [behaviors.md](../specs/baseline/system/behaviors.md) の「3.1 E2E-001」
- **注意事項**: 
  - `@Tag("integration")`を付与
  - REST Assured使用
  - テストケース:
    - `testBookListFlow()`: ログイン → 書籍一覧取得
    - `testBookSearchFlow()`: ログイン → 書籍検索（JPQL） → 書籍詳細取得
    - `testBookSearchCriteriaFlow()`: ログイン → 書籍検索（Criteria API）

---

### T_INTEGRATION_004: [P] 在庫更新フローのE2Eテスト

- **目的**: 在庫取得、更新のE2Eフローをテストする
- **対象**: `pro.kensait.backoffice.integration.StockUpdateE2ETest`
- **参照SPEC**: 
  - [behaviors.md](../specs/baseline/system/behaviors.md) の「3.1 E2E-003」
- **注意事項**: 
  - `@Tag("integration")`を付与
  - REST Assured使用
  - テストケース:
    - `testStockUpdateFlow()`: ログイン → 在庫取得 → 在庫更新 → 書籍詳細取得（在庫確認）
    - `testOptimisticLockFlow()`: 2つのリクエストで同時更新 → 楽観的ロック失敗（409 Conflict）

---

### T_INTEGRATION_005: [P] ワークフロー申請・承認フローのE2Eテスト

- **目的**: ワークフロー作成、申請、承認、書籍マスタ反映のE2Eフローをテストする
- **対象**: `pro.kensait.backoffice.integration.WorkflowApprovalE2ETest`
- **参照SPEC**: 
  - [behaviors.md](../specs/baseline/system/behaviors.md) の「3.1 E2E-002」
- **注意事項**: 
  - `@Tag("integration")`を付与
  - REST Assured使用
  - テストケース:
    - `testAddNewBookFlow()`: ログイン（一般社員） → ワークフロー作成（ADD_NEW_BOOK） → 申請 → ログイン（マネージャー） → 承認 → 書籍一覧取得（新規書籍確認）
    - `testRemoveBookFlow()`: ログイン → ワークフロー作成（REMOVE_BOOK） → 申請 → 承認 → 書籍一覧取得（削除された書籍が表示されない）
    - `testAdjustPriceFlow()`: ログイン → ワークフロー作成（ADJUST_BOOK_PRICE） → 申請 → 承認 → 書籍詳細取得（価格確認）

---

## 3. API間結合テスト

### T_INTEGRATION_006: [P] 全APIエンドポイントの疎通確認

- **目的**: 全APIエンドポイントが正常に動作することを確認する
- **対象**: `pro.kensait.backoffice.integration.AllApiEndpointsTest`
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/system/functional_design.md) の「2. 機能一覧」
- **注意事項**: 
  - `@Tag("integration")`を付与
  - REST Assured使用
  - 全エンドポイント（認証、書籍、カテゴリ、出版社、在庫、ワークフロー）にアクセス
  - HTTPステータスコードが期待値であることを確認

---

## 4. トランザクションテスト

### T_INTEGRATION_007: ワークフロー承認時のトランザクション整合性テスト

- **目的**: ワークフロー承認時のトランザクション整合性を確認する
- **対象**: `pro.kensait.backoffice.integration.WorkflowTransactionTest`
- **参照SPEC**: 
  - [behaviors.md](../specs/baseline/system/behaviors.md) の「7.1 TRAN-001」
- **注意事項**: 
  - `@Tag("integration")`を付与
  - テストケース:
    - `testApprovalTransaction_Success()`: 承認成功時、WORKFLOW_OPERATION INSERTとBOOK INSERT/UPDATEが両方コミットされる
    - `testApprovalTransaction_Rollback()`: 書籍マスタ反映失敗時、WORKFLOW_OPERATIONもロールバックされる

---

## 5. パフォーマンステスト

### T_INTEGRATION_008: APIレスポンスタイムテスト

- **目的**: 各APIのレスポンスタイムが要件を満たすことを確認する
- **対象**: `pro.kensait.backoffice.integration.PerformanceTest`
- **参照SPEC**: 
  - [behaviors.md](../specs/baseline/system/behaviors.md) の「5.1 レスポンスタイム」
- **注意事項**: 
  - `@Tag("integration")`を付与
  - REST Assured使用
  - テストケース:
    - `testAuthApiResponseTime()`: 認証API（500ms以内）
    - `testBookApiResponseTime()`: 書籍API（500ms以内）
    - `testCategoryApiResponseTime()`: カテゴリAPI（50ms以内）
    - `testPublisherApiResponseTime()`: 出版社API（50ms以内）
    - `testStockApiResponseTime()`: 在庫API（200ms以内）
    - `testWorkflowApiResponseTime()`: ワークフローAPI（1秒以内）
  - 各APIに10回アクセスし、95パーセンタイル値を計測

---

### T_INTEGRATION_009: N+1問題の確認

- **目的**: N+1問題が発生していないことを確認する
- **対象**: `pro.kensait.backoffice.integration.NplusOneTest`
- **参照SPEC**: 
  - [behaviors.md](../specs/baseline/system/behaviors.md) の「5.3 N+1問題の防止」
- **注意事項**: 
  - `@Tag("integration")`を付与
  - SQLログを監視してクエリ実行回数を確認
  - テストケース:
    - `testBookListQuery()`: 書籍一覧取得時、1回のクエリで全データ取得
    - `testWorkflowListQuery()`: ワークフロー一覧取得時、1回のクエリで全データ取得

---

## 6. セキュリティテスト

### T_INTEGRATION_010: [P] JWT認証のセキュリティテスト

- **目的**: JWT認証のセキュリティ要件を確認する
- **対象**: `pro.kensait.backoffice.integration.JwtSecurityTest`
- **参照SPEC**: 
  - [behaviors.md](../specs/baseline/system/behaviors.md) の「6.1 認証・認可」
- **注意事項**: 
  - `@Tag("integration")`を付与
  - テストケース:
    - `testJwtCookieIsHttpOnly()`: JWT CookieがHttpOnlyである
    - `testJwtExpiration()`: JWT有効期限が24時間である
    - `testPasswordIsHashed()`: パスワードがBCryptハッシュ化されている

---

### T_INTEGRATION_011: [P] ワークフロー承認権限のテスト

- **目的**: ワークフロー承認権限が正しく制御されることを確認する
- **対象**: `pro.kensait.backoffice.integration.WorkflowAuthorizationTest`
- **参照SPEC**: 
  - [behaviors.md](../specs/baseline/system/behaviors.md) の「6.1 SEC-004」
- **注意事項**: 
  - `@Tag("integration")`を付与
  - テストケース:
    - `testAssociateCannotApprove()`: ASSOCIATE（一般社員）は承認不可（403 Forbidden）
    - `testManagerCanApproveSameDepartment()`: MANAGER（マネージャー）は同一部署のみ承認可
    - `testManagerCannotApproveOtherDepartment()`: MANAGERは他部署のワークフローを承認不可（403 Forbidden）
    - `testDirectorCanApproveAllDepartments()`: DIRECTOR（ディレクター）は全部署のワークフローを承認可

---

## 7. エラーハンドリングテスト

### T_INTEGRATION_012: [P] エラーレスポンスの確認

- **目的**: エラーレスポンスが統一フォーマットで返されることを確認する
- **対象**: `pro.kensait.backoffice.integration.ErrorHandlingTest`
- **参照SPEC**: 
  - [behaviors.md](../specs/baseline/system/behaviors.md) の「10.1 エラーレスポンス」
- **注意事項**: 
  - `@Tag("integration")`を付与
  - テストケース:
    - `test404NotFound()`: リソースが見つからない（404 Not Found）
    - `test400BadRequest()`: バリデーションエラー（400 Bad Request）
    - `test403Forbidden()`: 権限不足（403 Forbidden）
    - `test409Conflict()`: 楽観的ロック失敗（409 Conflict）
    - `test500InternalServerError()`: システムエラー（500 Internal Server Error）
  - レスポンス形式: `{"error": "...", "message": "..."}`

---

## 8. データ整合性テスト

### T_INTEGRATION_013: [P] データ整合性の確認

- **目的**: データベースの外部キー整合性を確認する
- **対象**: `pro.kensait.backoffice.integration.DataIntegrityTest`
- **参照SPEC**: 
  - [behaviors.md](../specs/baseline/system/behaviors.md) の「4.1 外部キー整合性」
- **注意事項**: 
  - `@Tag("integration")`を付与
  - テストケース:
    - `testBookCategoryIntegrity()`: 全書籍にcategoryIdが存在し、CATEGORYテーブルに対応レコードが存在
    - `testBookPublisherIntegrity()`: 全書籍にpublisherIdが存在し、PUBLISHERテーブルに対応レコードが存在
    - `testBookStockIntegrity()`: 全書籍にSTOCKレコードが存在（1:1関係）
    - `testWorkflowEmployeeIntegrity()`: 全ワークフローにcreatedBy, operatedByが存在し、EMPLOYEEテーブルに対応レコードが存在

---

## 9. 最終検証

### T_INTEGRATION_014: システム全体の最終検証

- **目的**: システム全体が要件通りに動作することを確認する
- **対象**: 手動テスト + 自動テスト
- **参照SPEC**: 
  - [behaviors.md](../specs/baseline/system/behaviors.md) の「2. ユーザーストーリー」
- **注意事項**: 
  - 全ユーザーストーリーを実行
  - 主要なビジネスフローをエンドツーエンドで確認
  - データ整合性を確認
  - ログ出力を確認
  - エラーハンドリングを確認

---

## 完了確認

### チェックリスト

#### テスト環境設定
- [ ] 結合テスト用の設定（Gradle、`@Tag`）

#### E2E APIテスト
- [ ] 認証フローのE2Eテスト
- [ ] 書籍検索フローのE2Eテスト
- [ ] 在庫更新フローのE2Eテスト
- [ ] ワークフロー申請・承認フローのE2Eテスト

#### API間結合テスト
- [ ] 全APIエンドポイントの疎通確認

#### トランザクションテスト
- [ ] ワークフロー承認時のトランザクション整合性テスト

#### パフォーマンステスト
- [ ] APIレスポンスタイムテスト
- [ ] N+1問題の確認

#### セキュリティテスト
- [ ] JWT認証のセキュリティテスト
- [ ] ワークフロー承認権限のテスト

#### エラーハンドリングテスト
- [ ] エラーレスポンスの確認

#### データ整合性テスト
- [ ] データ整合性の確認

#### 最終検証
- [ ] システム全体の最終検証

---

## 実行方法

### 結合テストの実行
```bash
# 全結合テストを実行
./gradlew :back-office-api-sdd:integrationTest

# 特定のテストクラスを実行
./gradlew :back-office-api-sdd:integrationTest --tests "*AuthenticationE2ETest"
```

### 通常のビルド（結合テスト除外）
```bash
# 単体テストのみ実行（結合テストは除外）
./gradlew :back-office-api-sdd:test
```

---

## 次のステップ

結合テスト完了後、以下の作業に進んでください:

1. **ドキュメント更新**: README.md、API仕様書の更新
2. **デプロイ準備**: 本番環境へのデプロイ準備
3. **運用準備**: 監視、ログ設定、バックアップ設定

---

**タスクファイル作成日:** 2025-01-10  
**想定実行順序:** 9番目（最後）
