# 結合テストタスク

**担当者:** 全員（各API担当者が協力して実施）  
**推奨スキル:** REST Assured、JAX-RS Client、並行処理テスト  
**想定工数:** 6時間  
**依存タスク:** すべてのAPI実装タスク（[API_001_auth.md](API_001_auth.md)～[API_006_workflows.md](API_006_workflows.md)）

---

## タスクリスト

### T_INTEGRATION_001: [P] API間結合テストの作成

- **目的**: 複数のAPIを組み合わせた結合テストを作成する
- **対象**: IntegrationTest.java（テストクラス）
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「5. データフロー」
  - [functional_design.md (system)](../specs/baseline/system/functional_design.md) の「10. テスト要件」
- **注意事項**: 
  - REST AssuredまたはJAX-RS Clientを使用する
  - 認証 → 書籍一覧取得 → 書籍詳細取得のシーケンステスト
  - 認証 → カテゴリ一覧取得 → 書籍検索のシーケンステスト
  - 認証 → 在庫取得 → 在庫更新のシーケンステスト

---

### T_INTEGRATION_002: [P] E2E APIテストの作成（主要業務フロー）

- **目的**: 主要な業務フローをAPIシーケンスでテストする
- **対象**: E2EApiTest.java（テストクラス）
- **参照SPEC**: 
  - [behaviors.md (system)](../specs/baseline/system/behaviors.md)
  - [functional_design.md (system)](../specs/baseline/system/functional_design.md) の「10. テスト要件」
- **注意事項**: 
  - **ワークフロー完全フロー**: 作成 → 申請 → 承認 → 書籍マスタ反映確認
  - **新規書籍追加フロー**: ワークフロー作成（ADD_NEW_BOOK） → 申請 → 承認 → 書籍一覧で確認
  - **書籍削除フロー**: ワークフロー作成（REMOVE_BOOK） → 申請 → 承認 → 論理削除確認
  - **価格改定フロー**: ワークフロー作成（ADJUST_BOOK_PRICE） → 申請 → 承認 → 価格更新確認
  - **ワークフロー却下フロー**: 作成 → 申請 → 却下 → 再申請 → 承認
  - REST AssuredまたはJAX-RS Clientを使用する
  - 各フローの最後に期待される状態を検証する

---

### T_INTEGRATION_003: 並行処理テストの作成（在庫更新競合）

- **目的**: 在庫更新の並行処理シナリオをテストする（楽観的ロック）
- **対象**: ConcurrencyTest.java（テストクラス）
- **参照SPEC**: 
  - [functional_design.md (API_005_stocks)](../specs/baseline/api/API_005_stocks/functional_design.md) の「4. 楽観的ロックの仕組み」
  - [functional_design.md (API_005_stocks)](../specs/baseline/api/API_005_stocks/functional_design.md) の「11.6 並行更新フローチャート」
- **注意事項**: 
  - **重要**: 2つのスレッドが同時に同じ在庫を更新するシナリオをテストする
  - ユーザーA: 在庫取得（version=1） → 更新リクエスト（version=1） → 成功（version=2）
  - ユーザーB: 在庫取得（version=1） → 更新リクエスト（version=1） → 409 Conflict
  - ユーザーB: 再取得（version=2） → 更新リクエスト（version=2） → 成功（version=3）
  - ExecutorServiceを使用して並行実行する
  - 楽観的ロック例外が正しく処理されることを確認する

---

### T_INTEGRATION_004: [P] パフォーマンステストの作成

- **目的**: APIのパフォーマンス要件を満たすことを確認する
- **対象**: PerformanceTest.java（テストクラス）
- **参照SPEC**: 
  - [requirements.md](../specs/baseline/system/requirements.md) の「3.1 性能要件」
  - [functional_design.md (system)](../specs/baseline/system/functional_design.md) の「9. パフォーマンス考慮事項」
- **注意事項**: 
  - 書籍一覧取得: 200ms以内
  - 書籍詳細取得: 100ms以内
  - 書籍検索: 500ms以内
  - 在庫更新: 200ms以内
  - ログイン: 500ms以内
  - ワークフロー承認: 1秒以内
  - 複数回実行して平均レスポンスタイムを測定する

---

### T_INTEGRATION_005: [P] CORS動作確認テストの作成

- **目的**: CORSフィルタが正しく動作することを確認する
- **対象**: CorsTest.java（テストクラス）
- **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.5 Cross-Cutting Concerns」
- **注意事項**: 
  - OPTIONSリクエストでプリフライトチェックを確認する
  - Access-Control-Allow-Originヘッダーが設定されていることを確認する
  - Access-Control-Allow-Methodsヘッダーが設定されていることを確認する
  - Access-Control-Allow-Headersヘッダーが設定されていることを確認する

---

### T_INTEGRATION_006: [P] エラーハンドリング統合テストの作成

- **目的**: すべてのException Mapperが正しく動作することを確認する
- **対象**: ErrorHandlingTest.java（テストクラス）
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「8. エラーハンドリング」
  - [functional_design.md (system)](../specs/baseline/system/functional_design.md) の「6. エラーハンドリング」
- **注意事項**: 
  - 400 Bad Request: バリデーションエラー
  - 401 Unauthorized: 認証失敗
  - 403 Forbidden: 承認権限不足
  - 404 Not Found: リソースが見つからない
  - 409 Conflict: 楽観的ロック失敗
  - 500 Internal Server Error: 予期しないエラー
  - すべてのエラーレスポンスがErrorResponse形式であることを確認する

---

### T_INTEGRATION_007: トランザクション統合テストの作成

- **目的**: トランザクション管理が正しく動作することを確認する
- **対象**: TransactionTest.java（テストクラス）
- **参照SPEC**: 
  - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「7. トランザクション設計」
  - [functional_design.md (system)](../specs/baseline/system/functional_design.md) の「7. トランザクション管理」
- **注意事項**: 
  - **ワークフロー承認トランザクション**: ワークフロー履歴追加と書籍マスタ反映が1トランザクションで実行されることを確認
  - エラー発生時にロールバックされることを確認
  - 意図的にエラーを発生させてロールバックをテストする

---

### T_INTEGRATION_008: [P] JPQL vs Criteria API比較テストの作成

- **目的**: JPQLとCriteria APIの検索結果が一致することを確認する
- **対象**: QueryComparisonTest.java（テストクラス）
- **参照SPEC**: 
  - [functional_design.md (API_002_books)](../specs/baseline/api/API_002_books/functional_design.md) の「3.4 書籍検索（JPQL）」
  - [functional_design.md (API_002_books)](../specs/baseline/api/API_002_books/functional_design.md) の「3.5 書籍検索（Criteria API）」
- **注意事項**: 
  - 同一の検索条件でJPQLとCriteria APIを実行する
  - 結果セットが完全に一致することを確認する
  - カテゴリ+キーワード、カテゴリのみ、キーワードのみ、条件なしの各パターンをテストする

---

### T_INTEGRATION_009: 最終検証テストの作成

- **目的**: システム全体の最終検証を行う
- **対象**: FinalVerificationTest.java（テストクラス）
- **参照SPEC**: 
  - [requirements.md](../specs/baseline/system/requirements.md) の「2. 機能要件」
  - [requirements.md](../specs/baseline/system/requirements.md) の「3. 非機能要件」
- **注意事項**: 
  - すべての機能要件が実装されていることを確認する
  - すべてのエンドポイントが正常に動作することを確認する
  - データベースの整合性を確認する
  - ログが正しく出力されることを確認する

---

### T_INTEGRATION_010: テスト実行設定の作成

- **目的**: 結合テストの実行設定を作成する
- **対象**: Gradle設定、JUnit設定
- **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「11. デプロイメント構成」
- **注意事項**: 
  - E2E APIテストは通常ビルドから除外する
  - `@Tag("integration")`でタグ付けする
  - Gradleタスクで個別実行可能にする（例: `gradle integrationTest`）
  - テスト実行前にデータベースを初期化する
  - テスト実行後にクリーンアップする

---

## 結合テスト完了チェックリスト

- [ ] API間結合テストが作成された
- [ ] E2E APIテストが作成された
  - [ ] ワークフロー完全フロー
  - [ ] 新規書籍追加フロー
  - [ ] 書籍削除フロー
  - [ ] 価格改定フロー
  - [ ] ワークフロー却下フロー
- [ ] 並行処理テストが作成された
  - [ ] 在庫更新競合シナリオ
  - [ ] 楽観的ロック動作確認
- [ ] パフォーマンステストが作成された
  - [ ] すべてのAPIのレスポンスタイム測定
- [ ] CORS動作確認テストが作成された
- [ ] エラーハンドリング統合テストが作成された
  - [ ] すべてのHTTPステータスコード確認
- [ ] トランザクション統合テストが作成された
  - [ ] ロールバック動作確認
- [ ] JPQL vs Criteria API比較テストが作成された
- [ ] 最終検証テストが作成された
- [ ] テスト実行設定が作成された
  - [ ] Gradle設定
  - [ ] JUnit設定（@Tag）

---

## 次のステップ

すべての結合テストが完了し、テストが成功したら、プロジェクトは完成です！

最終確認事項：
- [ ] すべての単体テストが成功する
- [ ] すべての結合テストが成功する
- [ ] すべての機能要件が実装されている
- [ ] すべての非機能要件が満たされている
- [ ] ドキュメントが最新の状態である
- [ ] コードレビューが完了している
- [ ] デプロイメント準備が完了している
