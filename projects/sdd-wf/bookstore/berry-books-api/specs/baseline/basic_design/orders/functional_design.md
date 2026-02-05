# 注文管理ドメイン - 機能設計書

プロジェクトID: berry-books-api  
ドメイン: orders  
バージョン: 1.0.0  
最終更新日: 2026-02-05

---

## 1. 概要

本ドキュメントは、注文管理ドメインの機能を定義する。

* 実装方式: 独自実装 + 外部連携
* 注文処理は本システム、在庫更新はback-office-api経由

---

## 2. 機能一覧

### 2.1 注文管理機能

| 機能ID | 機能名 | 説明 |
|--------|--------|------|
| API_003 | 注文API | 注文処理は本システム、在庫更新はback-office-api経由 |

---

## 3. API詳細設計

### 3.1 注文作成

#### 3.1.1 エンドポイント

* メソッド: POST
* パス: `/api/orders`
* 認証: 必要（JWT）

#### 3.1.2 入力

* customerId: Long
* orderItems: List<OrderItemRequest>
  * bookId: Long
  * quantity: Integer

#### 3.1.3 処理フロー

1. OrderServiceを呼び出し
2. 在庫確認（back-office-api経由）
3. 在庫数が注文数以上であることを確認
4. Orderエンティティを作成
5. OrderItemエンティティを作成
6. OrderDaoでDBに永続化（トランザクション）
7. 在庫更新（back-office-api経由）
8. レスポンス生成

#### 3.1.4 出力

* 成功（201 Created）: OrderTO
* 在庫不足（400 Bad Request）: ErrorResponse
* エラー（500 Internal Server Error）: ErrorResponse

---

## 4. ビジネスルール

| ルールID | 説明 |
|---------|------|
| BR-ORDER-001 | 注文作成時、在庫数が注文数以上であること |
| BR-ORDER-002 | 在庫更新は楽観的ロックで並行制御すること |
| BR-ORDER-003 | 注文トランザクションと注文明細は同一トランザクション内で作成すること |
| BR-ORDER-004 | 注文履歴は、1注文明細=1レコードの非正規化形式で返却すること |

---

（以下、注文履歴取得等の詳細設計を記載してください）

---

## 5. 参考資料

* [../common/architecture_design.md](../common/architecture_design.md) - アーキテクチャ設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書
* [../common/external_interface.md](../common/external_interface.md) - 外部インターフェース仕様書
* [behaviors.md](behaviors.md) - 注文管理ドメインの振る舞い仕様書（結合テスト用）
