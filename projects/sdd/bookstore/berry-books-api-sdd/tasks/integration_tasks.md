# 結合テストタスク

担当者: 全員  
推奨スキル: REST API テスト, curl, JUnit 5, E2Eテスト  
想定工数: 4時間  
依存タスク: [API_001_auth.md](API_001_auth.md), [API_002_books.md](API_002_books.md), [API_003_orders.md](API_003_orders.md), [API_004_images.md](API_004_images.md)

---

## 概要

全APIの結合テストを実施する。API間の連携、E2E APIテスト、パフォーマンステスト、セキュリティテスト、最終検証を行う。

---

## タスクリスト

### API間結合テスト

#### T_INT_001: [P] 認証API + 注文APIの結合テスト

* 目的: 認証APIと注文APIの連携を検証する
* 対象: ログイン → 注文作成 → 注文履歴取得の一連のフロー
* 参照SPEC: 
  * [behaviors.md](../specs/baseline/system/behaviors.md) の「2. 認証API」、「4. 注文API」
  * [README.md](../README.md) の「📝 APIの使用例（curl）」
* 注意事項: 
  * JWT Cookieが正しく発行される
  * JWT Cookieが注文APIで正しく検証される
  * 認証必須エンドポイントで未認証時に401エラーが返る

---

#### T_INT_002: [P] 書籍API + 注文APIの結合テスト

* 目的: 書籍APIと注文APIの連携を検証する
* 対象: 書籍検索 → 注文作成の一連のフロー
* 参照SPEC: [behaviors.md](../specs/baseline/system/behaviors.md) の「3. 書籍API」、「4. 注文API」
* 注意事項: 
  * 書籍検索結果に在庫情報（version）が含まれる
  * 注文作成時にversionを使用して楽観的ロック制御が動作する

---

#### T_INT_003: [P] 外部API連携テスト（customer-hub-api）

* 目的: customer-hub-apiとの連携を検証する
* 対象: ログイン、新規登録、ユーザー情報取得
* 参照SPEC: [external_interface.md](../specs/baseline/system/external_interface.md) の「3. customer-hub-api連携」
* 注意事項: 
  * customer-hub-apiが起動していること
  * 顧客情報の取得・登録が正常に動作すること
  * エラー時に適切なエラーメッセージが返ること

---

#### T_INT_004: [P] 外部API連携テスト（back-office-api）

* 目的: back-office-apiとの連携を検証する
* 対象: 書籍一覧取得、書籍詳細取得、書籍検索、在庫更新
* 参照SPEC: [external_interface.md](../specs/baseline/system/external_interface.md) の「6. back-office-api連携」
* 注意事項: 
  * back-office-apiが起動していること
  * 書籍・在庫情報の取得が正常に動作すること
  * 在庫更新が正常に動作すること
  * 楽観的ロック制御が正常に動作すること

---

### E2E APIテスト

#### T_INT_005: E2Eテスト: ユーザー登録 → ログイン → 書籍検索 → 注文 → 注文履歴

* 目的: 主要な業務フローをE2Eテストで検証する
* 対象: 新規登録 → ログイン → 書籍検索 → 注文作成 → 注文履歴取得の一連のフロー
* 参照SPEC: [behaviors.md](../specs/baseline/system/behaviors.md)
* 注意事項: 
  * curlまたはREST Clientで実施
  * 各エンドポイントが正常に動作すること
  * JWT Cookieが正しく管理されること
  * トランザクションが正常にコミットされること

---

#### T_INT_006: [P] E2Eテスト: エラーケース（在庫不足）

* 目的: 在庫不足エラーのE2Eテストを検証する
* 対象: ログイン → 在庫を超える数量で注文
* 参照SPEC: [behaviors.md](../specs/baseline/system/behaviors.md) の「4.1.2 異常系」
* 注意事項: 
  * 409 Conflictエラーが返ること
  * エラーメッセージが適切であること
  * トランザクションがロールバックされること

---

#### T_INT_007: [P] E2Eテスト: エラーケース（楽観的ロック競合）

* 目的: 楽観的ロック競合エラーのE2Eテストを検証する
* 対象: 2ユーザーが同時に同じ書籍を注文
* 参照SPEC: [behaviors.md](../specs/baseline/system/behaviors.md) の「9. 並行制御（楽観的ロック）」
* 注意事項: 
  * 409 Conflictエラーが返ること
  * エラーメッセージが適切であること
  * トランザクションがロールバックされること

---

#### T_INT_008: [P] E2Eテスト: 認証エラー

* 目的: 認証エラーのE2Eテストを検証する
* 対象: JWT Cookie未設定で認証必須APIにアクセス
* 参照SPEC: [behaviors.md](../specs/baseline/system/behaviors.md) の「6. JWT認証フィルター」
* 注意事項: 
  * 401 Unauthorizedエラーが返ること
  * エラーメッセージが適切であること

---

### パフォーマンステスト

#### T_INT_009: パフォーマンステスト: APIレスポンスタイム

* 目的: 各APIのレスポンスタイムを計測する
* 対象: 全APIエンドポイント
* 参照SPEC: [behaviors.md](../specs/baseline/system/behaviors.md) の「10. パフォーマンス受入基準」
* 注意事項: 
  * 500ms以内（95パーセンタイル）を目標
  * 主要APIエンドポイント: /api/books, /api/books/search, /api/orders, /api/orders/history

---

#### T_INT_010: [P] パフォーマンステスト: スループット

* 目的: スループットを計測する
* 対象: 全APIエンドポイント
* 参照SPEC: [behaviors.md](../specs/baseline/system/behaviors.md) の「10.2 スループット」
* 注意事項: 
  * 100 req/sec以上を目標
  * 負荷テストツール（JMeter、Gatling等）を使用

---

### セキュリティテスト

#### T_INT_011: [P] セキュリティテスト: JWT Cookie検証

* 目的: JWT Cookieのセキュリティ設定を検証する
* 対象: JWT Cookie
* 参照SPEC: [behaviors.md](../specs/baseline/system/behaviors.md) の「11. セキュリティ受入基準」
* 注意事項: 
  * HttpOnly属性が設定されている
  * JavaScriptからアクセスできない
  * 有効期限が24時間

---

#### T_INT_012: [P] セキュリティテスト: パスワードハッシュ化

* 目的: パスワードがBCryptでハッシュ化されていることを検証する
* 対象: 新規登録、ログイン
* 参照SPEC: [behaviors.md](../specs/baseline/system/behaviors.md) の「11.1 認証・認可」
* 注意事項: 
  * パスワードが平文で保存されていない
  * BCryptハッシュで保存されている

---

#### T_INT_013: [P] セキュリティテスト: 認証必須エンドポイントのアクセス制御

* 目的: 認証必須エンドポイントのアクセス制御を検証する
* 対象: /api/orders, /api/orders/history, /api/auth/me
* 参照SPEC: [behaviors.md](../specs/baseline/system/behaviors.md) の「11.1 認証・認可」
* 注意事項: 
  * JWT Cookie未設定で401エラーが返る
  * JWT無効で401エラーが返る

---

### 最終検証

#### T_INT_014: 最終検証: 全APIエンドポイントの動作確認

* 目的: 全APIエンドポイントが正常に動作することを確認する
* 対象: 全APIエンドポイント
* 参照SPEC: [README.md](../README.md) の「API仕様」
* 注意事項: 
  * 各エンドポイントが正しいHTTPステータスコードを返す
  * エラーレスポンスが統一的なErrorResponse形式
  * MediaType.APPLICATION_JSONが設定されている

---

#### T_INT_015: 最終検証: データ整合性

* 目的: データベースのデータ整合性を検証する
* 対象: ORDER_TRAN, ORDER_DETAIL
* 参照SPEC: [data_model.md](../specs/baseline/system/data_model.md)
* 注意事項: 
  * 外部キー制約が正しく動作する
  * トランザクションが正常にコミットされる
  * ロールバックが正常に動作する

---

#### T_INT_016: 最終検証: ログ出力

* 目的: ログ出力が適切に行われていることを確認する
* 対象: server.log
* 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「11. ログ戦略」
* 注意事項: 
  * APIエンドポイントのエントリログが出力される
  * サービスメソッドのエントリログが出力される
  * エラーログが出力される
  * ログレベルが適切（INFO, WARN, ERROR, DEBUG）

---

## 完了基準

* [ ] 認証API + 注文APIの結合テストが完了している
* [ ] 書籍API + 注文APIの結合テストが完了している
* [ ] 外部API連携テスト（customer-hub-api）が完了している
* [ ] 外部API連携テスト（back-office-api）が完了している
* [ ] E2Eテスト（主要フロー）が完了している
* [ ] E2Eテスト（エラーケース）が完了している
* [ ] パフォーマンステスト（レスポンスタイム、スループット）が完了している
* [ ] セキュリティテスト（JWT Cookie、パスワードハッシュ化、アクセス制御）が完了している
* [ ] 最終検証（全APIエンドポイント、データ整合性、ログ出力）が完了している

---

## 参考資料

* [behaviors.md](../specs/baseline/system/behaviors.md) - 振る舞い仕様書（受入基準）
* [README.md](../README.md) - プロジェクトREADME
* [architecture_design.md](../specs/baseline/system/architecture_design.md) - アーキテクチャ設計書
* [data_model.md](../specs/baseline/system/data_model.md) - データモデル仕様書
* [external_interface.md](../specs/baseline/system/external_interface.md) - 外部インターフェース仕様書
