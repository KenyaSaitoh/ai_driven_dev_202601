# 基本設計変更記録

このファイルは、基本設計SPEC（functional_design.md、data_model.md等）の変更内容を記録するためのものです。

## 使い方

1. 基本設計SPECのマスターファイル（functional_design.md等）を自由に編集
2. このファイルをコピーして `CHANGES.md` として保存
3. 変更内容を以下のテンプレートに従って記載
4. `basic_design_change.md` を実行

---

## [YYYY-MM-DD] 変更タイトル

### 影響を受けるドメイン
- common
- orders
- books_proxy
- （該当するドメインを列挙）

### 変更対象ファイル
- common/functional_design.md (または .xlsx)
- common/data_model.md (または .xlsx)
- orders/functional_design.md (または .xlsx)
- orders/behaviors.md (または .xlsx)

### 変更内容

#### common/data_model.md の変更

##### テーブル「ORDER_TRAN」
追加:
- カラム `CANCEL_REASON` (VARCHAR(500), NULL) - キャンセル理由
- カラム `CANCELLED_AT` (TIMESTAMP, NULL) - キャンセル日時

変更:
- カラム `STATUS` に新しい値 'CANCELLED' を追加
  - 既存値: 'PENDING', 'CONFIRMED', 'SHIPPED'
  - 新規値: 'CANCELLED'

削除:
- （該当なし）

#### orders/functional_design.md の変更

##### セクション「API仕様」
追加:
- DELETE /orders/{id} エンドポイント追加
  - 説明: 注文をキャンセルする
  - リクエスト: パスパラメータ id (Long) - キャンセル対象の注文ID
  - レスポンス: { "message": "注文をキャンセルしました" }
  - ステータスコード: 200 (成功), 404 (注文なし), 400 (キャンセル不可)

変更:
- API一覧の説明を「注文管理API」→「注文管理・キャンセルAPI」に変更

削除:
- （該当なし）

##### セクション「ビジネスロジック」
追加:
- OrderService に注文キャンセル処理を追加
  - 目的: 注文をキャンセルし、在庫を復元する
  - ビジネスルール:
    - 発送済みの注文はキャンセル不可
    - キャンセル時に在庫を復元する
    - キャンセル理由を記録する

#### orders/behaviors.md の変更

##### 結合テストシナリオ
追加:
- シナリオ「注文キャンセル」
  - Given: 確認済みの注文が存在する
  - When: 注文をキャンセルする
  - Then: 注文ステータスがCANCELLEDに更新される
  - And: 在庫が復元される

---

## 変更の理由

[なぜこの変更が必要なのか、背景を記述]

例:
- 顧客からの要望により、注文後のキャンセル機能を追加する必要が生じた
- 在庫管理の改善のため、キャンセル時の在庫復元処理を実装する

---

## 影響範囲

### 影響を受けるドメイン
- common: データモデル（ORDER_TRANテーブル）
- orders: 注文管理機能（キャンセルAPI、ビジネスロジック、テストシナリオ）

### 影響を受けるファイル
- detailed_design/common/detailed_design.md - OrderTranエンティティのフィールド追加
- detailed_design/orders/detailed_design.md - OrderResourceとOrderServiceのメソッド追加
- detailed_design/orders/behaviors.md - 単体テストシナリオ追加
- src/main/java/.../entity/OrderTran.java - フィールド追加
- src/main/java/.../api/OrderResource.java - キャンセルエンドポイント追加
- src/main/java/.../service/OrderService.java - キャンセルメソッド追加
- src/test/java/.../OrderServiceTest.java - 単体テスト追加

---

## 備考

[その他の注意事項、制約事項等]
