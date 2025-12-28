# 結合テストタスク

**担当者:** 全員（各API担当者が協力して実施）  
**推奨スキル:** Jakarta EE、REST Assured、JUnit 5、パフォーマンステスト  
**想定工数:** 6時間  
**依存タスク:** [API_001_auth.md](API_001_auth.md), [API_002_books.md](API_002_books.md), [API_003_orders.md](API_003_orders.md), [API_004_images.md](API_004_images.md)

---

## 概要

全API実装後に実施する結合テストを実装します。API間結合テスト、E2E APIテスト、パフォーマンステスト、セキュリティテスト、最終検証を含みます。

---

## タスクリスト

### 6.1 API間結合テスト

- [ ] [P] **T_INTEGRATION_001**: 認証 → 書籍検索 → 注文作成の結合テスト
  - **目的**: 主要な業務フローの結合テストを実装する
  - **対象**: E2Eテストスクリプト（REST Assured）
  - **参照SPEC**: 
    - [behaviors.md](../specs/baseline/system/behaviors.md)
    - [functional_design.md](../specs/baseline/system/functional_design.md) の「7.1 注文処理全体フロー」
  - **注意事項**: 
    - REST Assured または JAX-RS Client を使用
    - ログイン → JWT Cookie取得 → 書籍検索 → 注文作成 → 注文履歴取得のフロー
    - JUnit 5 の @Tag("e2e") を使用

- [ ] [P] **T_INTEGRATION_002**: 複数ユーザー同時注文の結合テスト
  - **目的**: 楽観的ロック競合のテストを実装する
  - **対象**: E2Eテストスクリプト（並行実行）
  - **参照SPEC**: [behaviors.md](../specs/baseline/system/behaviors.md) の「9. 並行制御（楽観的ロック）」
  - **注意事項**: 
    - 複数スレッドで同時に同一書籍を注文
    - 1つは成功、他は409 Conflictを確認
    - JUnit 5 の @Tag("e2e") を使用

- [ ] [P] **T_INTEGRATION_003**: 在庫不足時のトランザクションロールバックテスト
  - **目的**: トランザクション管理のテストを実装する
  - **対象**: E2Eテストスクリプト
  - **参照SPEC**: [behaviors.md](../specs/baseline/system/behaviors.md) の「8. トランザクション管理」
  - **注意事項**: 
    - 複数書籍を注文し、途中で在庫不足
    - 全ての在庫更新がロールバックされることを確認
    - JUnit 5 の @Tag("e2e") を使用

---

### 6.2 E2E APIテスト（主要業務フロー）

- [ ] **T_INTEGRATION_004**: 新規ユーザー登録 → 書籍購入フローのE2Eテスト
  - **目的**: 新規ユーザーの登録から購入までのE2Eテストを実装する
  - **対象**: E2Eテストスクリプト（REST Assured）
  - **参照SPEC**: [behaviors.md](../specs/baseline/system/behaviors.md)
  - **注意事項**: 
    - 新規登録 → 自動ログイン → 書籍検索 → カートに追加 → 注文作成 → 注文履歴確認
    - JUnit 5 の @Tag("e2e") を使用

- [ ] **T_INTEGRATION_005**: 既存ユーザーログイン → 書籍購入フローのE2Eテスト
  - **目的**: 既存ユーザーの購入フローのE2Eテストを実装する
  - **対象**: E2Eテストスクリプト（REST Assured）
  - **参照SPEC**: [behaviors.md](../specs/baseline/system/behaviors.md)
  - **注意事項**: 
    - ログイン → 書籍検索 → カートに追加 → 注文作成 → 注文履歴確認
    - JUnit 5 の @Tag("e2e") を使用

- [ ] **T_INTEGRATION_006**: カテゴリ検索 → 複数書籍購入フローのE2Eテスト
  - **目的**: カテゴリ検索から複数書籍購入までのE2Eテストを実装する
  - **対象**: E2Eテストスクリプト（REST Assured）
  - **参照SPEC**: [behaviors.md](../specs/baseline/system/behaviors.md)
  - **注意事項**: 
    - ログイン → カテゴリ検索 → 複数書籍をカートに追加 → 注文作成
    - JUnit 5 の @Tag("e2e") を使用

---

### 6.3 エラーシナリオテスト

- [ ] [P] **T_INTEGRATION_007**: 認証エラーのE2Eテスト
  - **目的**: 認証エラーシナリオのテストを実装する
  - **対象**: E2Eテストスクリプト
  - **参照SPEC**: [behaviors.md](../specs/baseline/system/behaviors.md) の「6. JWT認証フィルター」
  - **注意事項**: 
    - JWT Cookie未設定、JWT無効、JWT期限切れのテスト
    - 認証必須APIへのアクセスで401 Unauthorizedを確認
    - JUnit 5 の @Tag("e2e") を使用

- [ ] [P] **T_INTEGRATION_008**: バリデーションエラーのE2Eテスト
  - **目的**: バリデーションエラーシナリオのテストを実装する
  - **対象**: E2Eテストスクリプト
  - **参照SPEC**: [behaviors.md](../specs/baseline/system/behaviors.md)
  - **注意事項**: 
    - 必須項目未入力、形式不正のテスト
    - 400 Bad Requestとエラーメッセージを確認
    - JUnit 5 の @Tag("e2e") を使用

- [ ] [P] **T_INTEGRATION_009**: ビジネスエラーのE2Eテスト
  - **目的**: ビジネスエラーシナリオのテストを実装する
  - **対象**: E2Eテストスクリプト
  - **参照SPEC**: [behaviors.md](../specs/baseline/system/behaviors.md)
  - **注意事項**: 
    - 在庫不足、楽観的ロック競合、メールアドレス重複のテスト
    - 409 Conflictとエラーメッセージを確認
    - JUnit 5 の @Tag("e2e") を使用

---

### 6.4 パフォーマンステスト

- [ ] **T_INTEGRATION_010**: 書籍一覧取得のパフォーマンステスト
  - **目的**: 書籍一覧取得APIのレスポンスタイムを測定する
  - **対象**: パフォーマンステストスクリプト
  - **参照SPEC**: 
    - [requirements.md](../specs/baseline/system/requirements.md) の「6.1 パフォーマンス要件」
    - [behaviors.md](../specs/baseline/system/behaviors.md) の「10. パフォーマンス受入基準」
  - **注意事項**: 
    - 100回実行し、95パーセンタイルが500ms以内であることを確認
    - N+1問題が発生していないことを確認
    - JUnit 5 の @Tag("performance") を使用

- [ ] **T_INTEGRATION_011**: 注文作成のパフォーマンステスト
  - **目的**: 注文作成APIのレスポンスタイムを測定する
  - **対象**: パフォーマンステストスクリプト
  - **参照SPEC**: [behaviors.md](../specs/baseline/system/behaviors.md) の「10. パフォーマンス受入基準」
  - **注意事項**: 
    - 100回実行し、95パーセンタイルが500ms以内であることを確認
    - トランザクション処理が最適化されていることを確認
    - JUnit 5 の @Tag("performance") を使用

- [ ] **T_INTEGRATION_012**: スループットテスト
  - **目的**: 同時リクエスト処理数を測定する
  - **対象**: パフォーマンステストスクリプト
  - **参照SPEC**: [requirements.md](../specs/baseline/system/requirements.md) の「6.1 パフォーマンス要件」
  - **注意事項**: 
    - 100 req/sec以上を処理できることを確認
    - JUnit 5 の @Tag("performance") を使用

---

### 6.5 セキュリティテスト

- [ ] [P] **T_INTEGRATION_013**: JWT Cookie セキュリティテスト
  - **目的**: JWT Cookie のセキュリティ設定を確認する
  - **対象**: セキュリティテストスクリプト
  - **参照SPEC**: [behaviors.md](../specs/baseline/system/behaviors.md) の「11. セキュリティ受入基準」
  - **注意事項**: 
    - HttpOnly Cookieであることを確認
    - JavaScriptからアクセス不可であることを確認
    - JUnit 5 の @Tag("security") を使用

- [ ] [P] **T_INTEGRATION_014**: パスワードハッシュ化テスト
  - **目的**: パスワードがBCryptハッシュ化されていることを確認する
  - **対象**: セキュリティテストスクリプト
  - **参照SPEC**: [behaviors.md](../specs/baseline/system/behaviors.md) の「11. セキュリティ受入基準」
  - **注意事項**: 
    - データベースに平文パスワードが保存されていないことを確認
    - JUnit 5 の @Tag("security") を使用

- [ ] [P] **T_INTEGRATION_015**: 入力検証テスト
  - **目的**: サーバーサイド入力検証が機能していることを確認する
  - **対象**: セキュリティテストスクリプト
  - **参照SPEC**: [requirements.md](../specs/baseline/system/requirements.md) の「6.2 セキュリティ要件」
  - **注意事項**: 
    - Bean Validationが機能していることを確認
    - 不正な入力で400 Bad Requestが返されることを確認
    - JUnit 5 の @Tag("security") を使用

---

### 6.6 Gradle設定

- [ ] **T_INTEGRATION_016**: E2Eテスト用Gradle設定
  - **目的**: E2Eテストを通常ビルドから除外する設定を追加する
  - **対象**: `build.gradle`
  - **参照SPEC**: [generate_tasks.md](../instructions/generate_tasks.md) の「8. 重要な注意事項」
  - **注意事項**: 
    - JUnit 5 の @Tag("e2e") を使用
    - Gradleで `./gradlew test` では実行されず、`./gradlew e2eTest` で個別実行可能にする
    - パフォーマンステスト、セキュリティテストも同様

---

### 6.7 最終検証

- [ ] **T_INTEGRATION_017**: 全API動作確認
  - **目的**: 全APIが正常に動作していることを最終確認する
  - **対象**: 全APIエンドポイント
  - **参照SPEC**: [requirements.md](../specs/baseline/system/requirements.md) の「9. 成功基準」
  - **注意事項**: 
    - 全APIエンドポイントが正常に動作する
    - 全ビジネスルールが実装されている
    - JWT認証が正常に機能する

- [ ] **T_INTEGRATION_018**: テストカバレッジ確認
  - **目的**: テストカバレッジが基準を満たしていることを確認する
  - **対象**: JaCoCo カバレッジレポート
  - **参照SPEC**: 
    - [constitution.md](../principles/constitution.md) の「原則3: テスト駆動品質」
    - [requirements.md](../specs/baseline/system/requirements.md) の「9.3 品質要件の充足」
  - **注意事項**: 
    - サービス層のカバレッジが80%以上
    - `./gradlew :berry-books-api:jacocoTestReport` で確認

- [ ] **T_INTEGRATION_019**: ログ出力確認
  - **目的**: ログ出力が適切に行われていることを確認する
  - **対象**: アプリケーションログ
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「10. ログ戦略」
  - **注意事項**: 
    - 全REST APIエンドポイントのエントリがINFOログに出力される
    - ビジネス例外がWARNログに出力される
    - システム例外がERRORログ（スタックトレース付き）に出力される

- [ ] **T_INTEGRATION_020**: ドキュメント最終確認
  - **目的**: ドキュメントが完成していることを確認する
  - **対象**: 全SPEC文書
  - **参照SPEC**: [requirements.md](../specs/baseline/system/requirements.md) の「9.4 ドキュメント要件の充足」
  - **注意事項**: 
    - 全SPECドキュメントが完成している
    - README.mdが完成している

---

## 完了条件

以下の全ての条件を満たしていること：

- [ ] 全てのAPI間結合テストが実装され、正常に動作する
- [ ] 全てのE2E APIテストが実装され、主要フローが正常に動作する
- [ ] エラーシナリオテストが実装され、適切なエラーレスポンスが返される
- [ ] パフォーマンステストが実装され、要件を満たしている
- [ ] セキュリティテストが実装され、要件を満たしている
- [ ] テストカバレッジが80%以上である
- [ ] 全APIが正常に動作している
- [ ] ログ出力が適切に行われている
- [ ] ドキュメントが完成している

---

## 参考資料

- [requirements.md](../specs/baseline/system/requirements.md) - 要件定義書
- [behaviors.md](../specs/baseline/system/behaviors.md) - 振る舞い仕様書
- [architecture_design.md](../specs/baseline/system/architecture_design.md) - アーキテクチャ設計書
- [constitution.md](../principles/constitution.md) - プロジェクト開発憲章
