# FUNC_003_delivery_service - 配送料金サービス

## メタデータ

* タスクID: FUNC_003
* 機能タイプ: ビジネスサービス
* 依存タスク: FUNC_001
* 並行実行可能: FUNC_002
* 担当者: 担当者B
* 推奨スキル: Jakarta EE, ビジネスロジック実装
* 想定工数: 4時間

## 実装内容

配送料金計算のビジネスロジックを実装する。
このサービスは、注文金額に基づいて配送料金を計算する。

---

## タスクリスト

### 1. サービスクラスの作成

* [ ] T_FUNC003_001: DeliveryFeeServiceの作成
  * 目的: 配送料金計算のビジネスロジックを実装する
  * 対象: pro.kensait.berrybooks.service.DeliveryFeeService
  * 参照SPEC: [architecture_design.md](../specs/baseline/basic_design/architecture_design.md) の「2.2 コンポーネントの責務」
  * 注意事項:
    * @ApplicationScoped
    * 外部依存なし（純粋な計算ロジック）

* [ ] T_FUNC003_002: calculateDeliveryFee()メソッドの実装
  * 目的: 注文金額に基づいて配送料金を計算する
  * 対象: DeliveryFeeService.calculateDeliveryFee()
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「5.1 注文ドメイン」
  * 注意事項:
    * 配送料金計算ルール:
      * 注文金額 < 3000円: 配送料金 800円
      * 注文金額 >= 3000円: 配送料金 0円（送料無料）
    * 引数: int orderAmount
    * 戻り値: int deliveryFee

### 2. バリデーション

* [ ] T_FUNC003_003: 入力値バリデーションの実装
  * 目的: 注文金額の妥当性を検証する
  * 対象: DeliveryFeeService.calculateDeliveryFee()
  * 参照SPEC: [functional_design.md](../specs/baseline/basic_design/functional_design.md) の「8. エラーハンドリング」
  * 注意事項:
    * 注文金額が負数の場合はIllegalArgumentExceptionをスロー
    * 注文金額が0円の場合も許容（配送料金800円）

### 3. テストケースの作成

* [ ] T_FUNC003_004: 単体テストの作成
  * 目的: 配送料金計算ロジックの正確性を検証する
  * 対象: DeliveryFeeServiceTest
  * 参照SPEC: [behaviors.md](../specs/baseline/basic_design/behaviors.md)
  * 注意事項:
    * テストケース:
      * 注文金額2999円 → 配送料金800円
      * 注文金額3000円 → 配送料金0円
      * 注文金額10000円 → 配送料金0円
      * 注文金額0円 → 配送料金800円
      * 注文金額-100円 → IllegalArgumentException

---

## 完了条件

* [ ] 配送料金計算が正確に動作する
* [ ] 境界値テスト（2999円、3000円）が成功する
* [ ] 負数入力時にIllegalArgumentExceptionがスローされる
* [ ] 単体テストが全て成功する

---

## 参考資料

* [../specs/baseline/basic_design/architecture_design.md](../specs/baseline/basic_design/architecture_design.md) - アーキテクチャ設計書
* [../specs/baseline/basic_design/functional_design.md](../specs/baseline/basic_design/functional_design.md) - 機能設計書
* [../specs/baseline/basic_design/behaviors.md](../specs/baseline/basic_design/behaviors.md) - 振る舞い仕様書
