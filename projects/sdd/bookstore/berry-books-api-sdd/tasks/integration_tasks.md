# berry-books-api-sdd - 結合テストタスク

担当者: 全員  
推奨スキル: REST Assured、JUnit 5、E2Eテスト  
想定工数: 8時間  
依存タスク: [API_001_auth](API_001_auth.md)、[API_002_books](API_002_books.md)、[API_003_orders](API_003_orders.md)、[API_004_images](API_004_images.md)

---

## 概要

本タスクは、全API実装完了後に実施する結合テストを行う。API間連携、E2E APIテスト、パフォーマンステスト、セキュリティテストを実施する。

---

## タスクリスト

### 1. E2E APIテスト（REST Assured）

#### T_INTG_001: 認証フローのE2Eテスト

* [ ] [P] T_INTG_001: 認証フローのE2Eテスト
  * 目的: 認証フロー全体をテストする
  * 対象: AuthenApiE2ETest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [behaviors.md](../specs/baseline/basic_design/behaviors.md) の「2. 認証API」
  * 注意事項:
    * テストシナリオ:
      1. 新規登録 → ログイン成功 → 現在のユーザー情報取得 → ログアウト
      2. ログイン失敗（メールアドレス不一致）
      3. ログイン失敗（パスワード不一致）
    * @Tag("e2e")を付与
    * 実際のHTTPリクエストを送信

---

#### T_INTG_002: 書籍検索フローのE2Eテスト

* [ ] [P] T_INTG_002: 書籍検索フローのE2Eテスト
  * 目的: 書籍検索フロー全体をテストする
  * 対象: BookApiE2ETest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [behaviors.md](../specs/baseline/basic_design/behaviors.md) の「3. 書籍API」
  * 注意事項:
    * テストシナリオ:
      1. カテゴリ一覧取得 → カテゴリIDで書籍検索
      2. キーワード検索
      3. 書籍詳細取得
      4. 画像取得
    * @Tag("e2e")を付与
    * 実際のHTTPリクエストを送信

---

#### T_INTG_003: 注文フローのE2Eテスト

* [ ] [P] T_INTG_003: 注文フローのE2Eテスト
  * 目的: 注文フロー全体をテストする
  * 対象: OrderApiE2ETest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [behaviors.md](../specs/baseline/basic_design/behaviors.md) の「4. 注文API」
  * 注意事項:
    * テストシナリオ:
      1. ログイン → 書籍検索 → 注文作成 → 注文履歴取得 → 注文詳細取得
      2. 在庫不足エラー
      3. 楽観的ロック競合エラー
    * @Tag("e2e")を付与
    * 実際のHTTPリクエストを送信
    * トランザクションロールバックを確認

---

#### T_INTG_004: 外部API連携テスト

* [ ] [P] T_INTG_004: 外部API連携テスト
  * 目的: 外部API連携が正常に動作することをテストする
  * 対象: ExternalApiIntegrationTest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [external_interface.md](../specs/baseline/basic_design/external_interface.md) の「3. customer-hub-api連携」「6. back-office-api連携」
  * 注意事項:
    * テストシナリオ:
      1. customer-hub-api: 顧客検索、顧客登録
      2. back-office-api: 書籍検索、在庫更新
    * @Tag("e2e")を付与
    * 外部APIが起動していることを確認
    * 実際のHTTPリクエストを送信

---

### 2. セキュリティテスト

#### T_INTG_005: JWT認証テスト

* [ ] [P] T_INTG_005: JWT認証テスト
  * 目的: JWT認証が正常に動作することをテストする
  * 対象: JwtSecurityTest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [behaviors.md](../specs/baseline/basic_design/behaviors.md) の「6. JWT認証フィルター」
  * 注意事項:
    * テストシナリオ:
      1. 有効なJWTで認証必須APIにアクセス → 200 OK
      2. 無効なJWTで認証必須APIにアクセス → 401 Unauthorized
      3. JWT未設定で認証必須APIにアクセス → 401 Unauthorized
      4. 認証不要なAPIはJWT未設定でもアクセス可能
    * @Tag("e2e")を付与

---

### 3. パフォーマンステスト

#### T_INTG_006: レスポンスタイムテスト

* [ ] [P] T_INTG_006: レスポンスタイムテスト
  * 目的: APIレスポンスタイムが基準を満たすことをテストする
  * 対象: PerformanceTest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [behaviors.md](../specs/baseline/basic_design/behaviors.md) の「10. パフォーマンス受入基準」
  * 注意事項:
    * テストシナリオ:
      1. 書籍一覧取得: 500ms以内（95パーセンタイル）
      2. 書籍検索: 500ms以内（95パーセンタイル）
      3. 注文作成: 500ms以内（95パーセンタイル）
      4. 注文履歴取得: 500ms以内（95パーセンタイル）
    * @Tag("performance")を付与
    * 複数回実行して95パーセンタイル値を測定

---

### 4. 並行制御テスト

#### T_INTG_007: 楽観的ロック競合テスト

* [ ] [P] T_INTG_007: 楽観的ロック競合テスト
  * 目的: 楽観的ロック制御が正常に動作することをテストする
  * 対象: OptimisticLockTest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [behaviors.md](../specs/baseline/basic_design/behaviors.md) の「9. 並行制御」
  * 注意事項:
    * テストシナリオ:
      1. 2つのスレッドで同時に同じ書籍を注文
      2. 1つ目は成功、2つ目は409 Conflictエラー
    * @Tag("e2e")を付与
    * 並行処理を実装

---

### 5. トランザクション管理テスト

#### T_INTG_008: トランザクションロールバックテスト

* [ ] [P] T_INTG_008: トランザクションロールバックテスト
  * 目的: エラー発生時のトランザクションロールバックをテストする
  * 対象: TransactionRollbackTest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [behaviors.md](../specs/baseline/basic_design/behaviors.md) の「8. トランザクション管理」
  * 注意事項:
    * テストシナリオ:
      1. 2冊注文（1冊目は在庫あり、2冊目は在庫不足）
      2. 注文作成失敗（409 Conflict）
      3. 1冊目の在庫減算もロールバックされることを確認
    * @Tag("e2e")を付与
    * データベース状態を確認

---

### 6. エラーハンドリングテスト

#### T_INTG_009: エラーレスポンステスト

* [ ] [P] T_INTG_009: エラーレスポンステスト
  * 目的: エラーレスポンスが統一的な形式で返却されることをテストする
  * 対象: ErrorResponseTest.java（JUnit 5 + REST Assured）
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8. エラーハンドリング」
  * 注意事項:
    * テストシナリオ:
      1. 400 Bad Request: バリデーションエラー
      2. 401 Unauthorized: 認証エラー
      3. 404 Not Found: リソース不存在
      4. 409 Conflict: ビジネスエラー
      5. 500 Internal Server Error: システムエラー
    * @Tag("e2e")を付与
    * エラーレスポンス形式（status、error、message、path）を確認

---

## 成果物チェックリスト

* [ ] E2E APIテストが実装されている
* [ ] セキュリティテストが実装されている
* [ ] パフォーマンステストが実装されている
* [ ] 並行制御テストが実装されている
* [ ] トランザクション管理テストが実装されている
* [ ] エラーハンドリングテストが実装されている
* [ ] 全てのテストが成功する
* [ ] テストカバレッジが80%以上
* [ ] 主要なビジネスフローが正常に動作する

---

## E2Eテスト実行方法

### E2Eテストのみを実行

```bash
# アプリケーションサーバーを起動
./gradlew run

# 別ターミナルでE2Eテストを実行
./gradlew :berry-books-api-sdd:test --tests "*E2ETest"

# または、@Tag("e2e")でフィルタリング
./gradlew :berry-books-api-sdd:test -Dgroups=e2e
```

### パフォーマンステストのみを実行

```bash
./gradlew :berry-books-api-sdd:test -Dgroups=performance
```

---

## 参考資料

* [behaviors.md](../specs/baseline/basic_design/behaviors.md) - 振る舞い仕様書（受入基準）
* [functional_design.md](../specs/baseline/basic_design/functional_design.md) - 機能設計書
* [external_interface.md](../specs/baseline/basic_design/external_interface.md) - 外部インターフェース仕様書
* REST Assured: https://rest-assured.io/
