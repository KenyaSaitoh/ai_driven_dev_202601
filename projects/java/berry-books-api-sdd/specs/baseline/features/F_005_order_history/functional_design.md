# F-005: 注文履歴参照 - 機能設計書

**機能ID:** F-005  
**機能名:** 注文履歴参照  
**バージョン:** 1.0.0  
**最終更新日:** 2025-12-16

---

## 1. 概要

本文書は、注文履歴参照機能の詳細設計を記述します。

**関連ドキュメント:**
- [../../system/requirements.md](../../system/requirements.md) - システム要件定義書
- [../../system/architecture_design.md](../../system/architecture_design.md) - アーキテクチャ設計書
- [behaviors.md](behaviors.md) - 振る舞い仕様書（Acceptance Criteria）
- [screen_design.md](screen_design.md) - 画面設計書

---

## 2. ユーザーストーリー

```
As a 登録顧客
I want to 過去の注文履歴を確認する
So that 購入済み書籍と配送状況を把握できる
```

---

## 3. ビジネスルール

| ルールID | 説明 |
|---------|-------------|
| BR-040 | 注文履歴は顧客IDでフィルタリング |
| BR-041 | 注文日降順（新しい順）でソート |
| BR-042 | 注文詳細は注文IDで取得 |

---

## 4. 機能フロー

### 4.1 注文履歴一覧表示フロー

1. メニューから「注文履歴」を選択
2. システムがログイン中の顧客の注文履歴を取得
3. 注文履歴を注文日降順で一覧表示

### 4.2 注文詳細表示フロー

1. 注文履歴一覧から注文を選択
2. 注文詳細画面に遷移
3. 注文情報、配送先情報、注文明細を表示

### 4.3 表示項目

- 注文番号
- 注文日
- 合計金額
- 配送料金
- 配送先住所
- 決済方法
- 注文明細（書籍名、単価、数量、小計）

---

## 5. クラス設計

### 5.1 プレゼンテーション層

**OrderHistoryBean**
- **責務**: 注文履歴のコントローラー
- **タイプ**: @ViewScoped Bean
- **フィールド**: 
  - `orderHistoryList` - 注文履歴リスト
  - `orderSummary` - 注文サマリー
  - `orderTranId` - 注文ID（注文詳細画面用パラメータ）
  - `selectedDetailId` - 注文明細ID（注文詳細画面用パラメータ）
  - `orderService` - 注文サービス（注入）
  - `customerBean` - 顧客Bean（注入）
- **主要メソッド**: 
  - `init()` - 初期化処理（viewActionから明示的にメソッド呼び出しされるため、実処理なし）
  - `loadOrderHistory()` - 注文履歴を読み込む（customerBeanのnullチェック実施）
  - `loadOrderDetail()` - 注文詳細を読み込む
  - `getOrderDetail(Integer orderTranId)` - 注文詳細画面に遷移
- **エラーハンドリング**: 
  - customerBeanまたはcustomerがnullの場合、空のリストを初期化してNullPointerExceptionを防止

### 5.2 ビジネスロジック層

**OrderService**
- **責務**: 注文履歴のビジネスロジック
- **タイプ**: @ApplicationScoped
- **主要メソッド**: 
  - `getOrderHistory(Integer customerId)` - 注文履歴を取得（注文明細単位でOrderHistoryTOのリストを返す）
  - `getOrderDetail(Integer orderTranId)` - 注文詳細を取得（OrderSummaryTOを返す）
- **実装詳細**:
  - `getOrderHistory()`は注文トランザクションと注文明細を結合し、各明細ごとに1つのOrderHistoryTOを生成
  - N+1問題を回避するため、JOIN FETCHを使用して注文明細、書籍、出版社を同時に取得

### 5.3 転送オブジェクト

**OrderHistoryTO**
- **責務**: 注文履歴情報をレイヤー間で転送
- **タイプ**: Record クラス（Java 17以降）
- **データ構造の設計意図**:
  - 注文履歴一覧画面の表示要件に最適化したフラットな構造
  - 注文トランザクション（ORDER_TRAN）と注文明細（ORDER_DETAIL）を非正規化して結合
  - 各注文明細ごとに1つのRecordインスタンスを生成（1注文＝N明細 → N個のOrderHistoryTO）
  - 一覧画面で必要な情報のみを含む軽量なDTO
- **コンポーネント（フィールド）**: 
  - `orderDate` (LocalDate) - 注文日
  - `tranId` (Integer) - 注文トランザクションID
  - `detailId` (Integer) - 注文明細ID
  - `bookName` (String) - 書籍名
  - `publisherName` (String) - 出版社名
  - `price` (BigDecimal) - 明細単価
  - `count` (Integer) - 注文数量

**OrderSummaryTO**
- **責務**: 注文サマリー情報をレイヤー間で転送
- **タイプ**: Transfer Object
- **フィールド**: 
  - `orderTran` - 注文トランザクション
  - `orderDetails` - 注文明細のリスト

### 5.4 データアクセス層

**OrderTranDao**
- **責務**: 注文トランザクションエンティティのCRUD操作
- **タイプ**: @ApplicationScoped
- **主要メソッド**: 
  - `findByCustomerId(Integer)` - 顧客IDで注文履歴を取得
  - `findByCustomerIdWithDetails(Integer)` - 顧客IDで注文履歴を取得（JOIN FETCHで明細、書籍、出版社を同時取得）
  - `findById(Integer)` - 注文IDで検索
  - `findByIdWithDetails(Integer)` - 注文と明細をJOIN FETCHで取得

**OrderDetailDao**
- **責務**: 注文明細エンティティのCRUD操作
- **タイプ**: @ApplicationScoped
- **主要メソッド**: 
  - `findByOrderTranId(Long)` - 注文IDで明細を取得

---

## 6. 例外・エラー処理

| シナリオ | 期待される動作 |
|----------|-------------------|
| 注文履歴が空 | 「注文履歴はありません」メッセージ表示 |
| 存在しない注文IDで詳細表示 | エラーメッセージ表示 |
| 他の顧客の注文IDで詳細表示 | アクセス拒否 |

---

## 7. 受入基準

詳細は [behaviors.md](behaviors.md) を参照

