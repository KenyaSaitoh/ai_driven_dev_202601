# 基本設計変更記録

このファイルは、基本設計SPEC（functional_design.md、data_model.md等）の変更内容を記録するためのものです。

## 使い方

1. 基本設計SPECのマスターファイル（functional_design.md等）を自由に編集
2. このファイルをコピーして `CHANGES.md` として保存
3. 変更内容を以下のテンプレートに従って記載
4. `basic_design_change.md` を実行

---

## [YYYY-MM-DD] 変更タイトル

### 変更対象
- functional_design.md (または .xlsx)
- data_model.md (または .xlsx)
- behaviors.md (または .xlsx)
- external_interface.md (または .xlsx)

### 変更内容

#### functional_design.md の変更

##### セクション「API一覧」
**追加**:
- API_002_Order に DELETE /orders/{id} エンドポイント追加
  - 説明: 注文をキャンセルする
  - リクエスト: なし
  - レスポンス: { "message": "注文をキャンセルしました" }
  - ステータスコード: 200 (成功), 404 (注文なし), 400 (キャンセル不可)

**変更**:
- API_002_Order の説明を「注文管理API」→「注文管理・キャンセルAPI」に変更

**削除**:
- （該当なし）

##### セクション「Service設計」
**追加**:
- OrderService に cancelOrder(Long orderId) メソッド追加
  - 引数: orderId (Long) - キャンセル対象の注文ID
  - 戻り値: void
  - 例外: OrderNotFoundException, OrderAlreadyShippedException

#### data_model.md の変更

##### テーブル「ORDER_TRAN」
**追加**:
| カラム名 | 型 | NULL | デフォルト | 説明 |
|---------|-----|------|-----------|------|
| cancel_reason | VARCHAR(255) | YES | NULL | キャンセル理由 |

**変更**:
- （該当なし）

**削除**:
- （該当なし）

#### behaviors.md の変更

##### シナリオ
**追加**:
- 注文キャンセルシナリオ
  ```
  Given: 注文ID=123の注文が存在する
  And: 注文のステータスが「未発送」である
  When: OrderService.cancelOrder(123) を実行
  Then: 注文のステータスが「キャンセル済み」に更新される
  And: 在庫が復元される
  ```

### 変更理由

結合テストで「注文後のキャンセルができない」という問題が発覚したため。
顧客からの要望にも対応するため、キャンセル機能を追加する。

### 影響範囲（推定）

#### 詳細設計
- FUNC_001_entity/detailed_design.md - ORDER_TRAN エンティティの更新
- API_002_Order/detailed_design.md - OrderResource、OrderService の更新

#### コード
- src/main/java/.../entity/OrderTran.java - cancel_reason フィールド追加
- src/main/java/.../service/OrderService.java - cancelOrder() メソッド追加
- src/main/java/.../api/OrderResource.java - DELETE /orders/{id} エンドポイント追加
- src/main/java/.../api/dto/OrderCancelRequest.java - 新規作成（必要に応じて）

#### テスト
- src/test/java/.../OrderServiceTest.java - キャンセルテスト追加
- src/test/java/.../OrderServiceIntegrationTest.java - 結合テスト追加
- src/test/java/.../OrderE2ETest.java - E2Eテスト追加

---

## 記載のポイント

### 変更種別の明確化
- **追加**: 新規に追加されたもの
- **変更**: 既存のものが変更されたもの
- **削除**: 削除されたもの

### 詳細度
- セクション名、テーブル名、カラム名、メソッド名など、できるだけ具体的に記載
- EXCEL形式の場合は、シート名、行番号なども記載
- AIが正確に理解できるよう、具体的に記載

### 影響範囲
- 推定で構わないので、影響を受けると思われるファイルをリストアップ
- AIが影響分析時に参考にする

### 変更理由
- なぜこの変更が必要になったのかを明確に記載
- テストで発覚した問題、顧客要望、設計の誤り等
