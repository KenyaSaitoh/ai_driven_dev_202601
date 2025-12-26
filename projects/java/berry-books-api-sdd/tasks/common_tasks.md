# 共通機能タスク

**担当者:** 共通機能チーム（1-2名）  
**推奨スキル:** Jakarta EE、JPA、CDI  
**想定工数:** 16時間  
**依存タスク:** setup_tasks.md

---

## 概要

本タスクは、複数機能で共有される共通コンポーネント（共通エンティティ、共通サービス、共通ユーティリティ、認証フィルター）を実装します。このタスク完了後、機能別実装を並行実行可能になります。

---

## タスクリスト

### セクション1: 共通ユーティリティ

- [X] **T_COMMON_001**: MessageUtilの作成
  - **目的**: メッセージリソース（messages.properties）からメッセージを取得するユーティリティクラスを実装する
  - **対象**: `pro.kensait.berrybooks.common.MessageUtil`（ユーティリティクラス）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「8. エラーハンドリング方針」
  - **注意事項**: `final`クラス、`static`メソッドで実装。`ResourceBundle`を使用してメッセージを取得

- [X] **T_COMMON_002**: SettlementTypeの作成
  - **目的**: 決済方法を表す列挙型を実装する
  - **対象**: `pro.kensait.berrybooks.common.SettlementType`（Enum）
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「4.6 ORDER_TRAN（注文トランザクション）」
  - **注意事項**: 定数: `BANK_TRANSFER(1)`, `CREDIT_CARD(2)`, `CASH_ON_DELIVERY(3)`。`getLabel()`メソッドで日本語ラベルを返す

- [X] **T_COMMON_003**: AddressUtilの作成
  - **目的**: 住所処理（沖縄県判定）のユーティリティクラスを実装する
  - **対象**: `pro.kensait.berrybooks.util.AddressUtil`（ユーティリティクラス）
  - **参照SPEC**: [functional_design.md](../specs/baseline/features/F_003_order_processing/functional_design.md) の「6.2 ビジネスロジック層」
  - **注意事項**: `isOkinawa(String address)`メソッドで住所に「沖縄」が含まれるか判定

---

### セクション2: 共通エンティティ

- [X] **T_COMMON_004**: Publisherエンティティの作成
  - **目的**: 出版社マスタのエンティティクラスを実装する
  - **対象**: `pro.kensait.berrybooks.entity.Publisher`（JPAエンティティ）
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「4.1 PUBLISHER（出版社マスタ）」
  - **注意事項**: `@Entity`、`@Id`、`@GeneratedValue(strategy = GenerationType.IDENTITY)`を使用。フィールド: `publisherId`, `publisherName`

- [X] **T_COMMON_005**: Categoryエンティティの作成
  - **目的**: カテゴリマスタのエンティティクラスを実装する
  - **対象**: `pro.kensait.berrybooks.entity.Category`（JPAエンティティ）
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「4.2 CATEGORY（カテゴリマスタ）」
  - **注意事項**: `@Entity`、`@Id`、`@GeneratedValue(strategy = GenerationType.IDENTITY)`を使用。フィールド: `categoryId`, `categoryName`

- [X] **T_COMMON_006**: Bookエンティティの作成
  - **目的**: 書籍マスタのエンティティクラスを実装する
  - **対象**: `pro.kensait.berrybooks.entity.Book`（JPAエンティティ）
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「4.3 BOOK（書籍マスタ）」
  - **注意事項**: 
    - フィールド: `bookId`, `bookName`, `author`, `categoryId`, `publisherId`, `price`
    - `@ManyToOne`で`Category`と`Publisher`を関連付け
    - `getImageFileName()`メソッドで`bookName + ".jpg"`を返す（カバー画像ファイル名生成用）
    - `quantity`フィールドは**含めない**（Stockエンティティに存在）

- [X] **T_COMMON_007**: Stockエンティティの作成
  - **目的**: 在庫情報のエンティティクラスを実装する（楽観的ロック対応）
  - **対象**: `pro.kensait.berrybooks.entity.Stock`（JPAエンティティ）
  - **参照SPEC**: 
    - [data_model.md](../specs/baseline/system/data_model.md) の「4.4 STOCK（在庫情報）」
    - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「7. 並行制御」
  - **注意事項**: 
    - フィールド: `bookId`, `quantity`, `version`
    - `@Version`アノテーションを`version`フィールドに付与（楽観的ロック用）
    - `@OneToOne`で`Book`と関連付け

- [X] **T_COMMON_008**: Customerエンティティの作成
  - **目的**: 顧客情報のエンティティクラスを実装する
  - **対象**: `pro.kensait.berrybooks.entity.Customer`（JPAエンティティ）
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「4.5 CUSTOMER（顧客情報）」
  - **注意事項**: 
    - フィールド: `customerId`, `customerName`, `email`, `password`, `birthday`, `address`
    - `@Column(unique = true)`を`email`に付与
    - パスワードは平文保存（学習用のみ）

- [X] **T_COMMON_009**: OrderTranエンティティの作成
  - **目的**: 注文トランザクションのエンティティクラスを実装する
  - **対象**: `pro.kensait.berrybooks.entity.OrderTran`（JPAエンティティ）
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「4.6 ORDER_TRAN（注文トランザクション）」
  - **注意事項**: 
    - フィールド: `orderTranId`, `orderDate`, `customerId`, `totalPrice`, `deliveryPrice`, `deliveryAddress`, `settlementType`
    - `@ManyToOne`で`Customer`と関連付け
    - `@OneToMany`で`OrderDetail`と関連付け（`mappedBy = "orderTran"`）

- [X] **T_COMMON_010**: OrderDetailエンティティの作成
  - **目的**: 注文明細のエンティティクラスを実装する
  - **対象**: `pro.kensait.berrybooks.entity.OrderDetail`（JPAエンティティ）
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「4.7 ORDER_DETAIL（注文明細）」
  - **注意事項**: 
    - フィールド: `orderTranId`, `orderDetailId`, `bookId`, `price`, `count`
    - `@EmbeddedId`で複合主キー（`OrderDetailPK`）を使用
    - `@ManyToOne`で`OrderTran`と`Book`を関連付け

- [X] **T_COMMON_011**: OrderDetailPKの作成
  - **目的**: 注文明細の複合主キークラスを実装する
  - **対象**: `pro.kensait.berrybooks.entity.OrderDetailPK`（埋め込み可能クラス）
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「4.7 ORDER_DETAIL（注文明細）」
  - **注意事項**: 
    - `@Embeddable`を付与
    - フィールド: `orderTranId`, `orderDetailId`
    - `Serializable`を実装
    - `equals()`, `hashCode()`をオーバーライド

---

### セクション3: 共通Daoクラス

- [X] **T_COMMON_012**: CategoryDaoの作成
  - **目的**: カテゴリマスタのデータアクセスクラスを実装する
  - **対象**: `pro.kensait.berrybooks.dao.CategoryDao`（DAOクラス）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「2.2 コンポーネントの責務」
  - **注意事項**: 
    - `@ApplicationScoped`を付与
    - `@PersistenceContext`で`EntityManager`をインジェクト
    - メソッド: `findAll()`（全カテゴリ取得、カテゴリID昇順）

---

### セクション4: 共通サービスクラス

- [X] **T_COMMON_013**: CategoryServiceの作成
  - **目的**: カテゴリ管理のビジネスロジックを実装する
  - **対象**: `pro.kensait.berrybooks.service.category.CategoryService`（サービスクラス）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「2.2 コンポーネントの責務」
  - **注意事項**: 
    - `@ApplicationScoped`を付与
    - `@Inject`で`CategoryDao`をインジェクト
    - メソッド: `findAll()`（カテゴリ一覧取得）

- [X] **T_COMMON_014**: DeliveryFeeServiceの作成
  - **目的**: 配送料金計算のビジネスロジックを実装する
  - **対象**: `pro.kensait.berrybooks.service.delivery.DeliveryFeeService`（サービスクラス）
  - **参照SPEC**: 
    - [functional_design.md](../specs/baseline/features/F_003_order_processing/functional_design.md) の「6.2 ビジネスロジック層」
    - [behaviors.md](../specs/baseline/system/behaviors.md) の「BR-020」
  - **注意事項**: 
    - `@ApplicationScoped`を付与
    - メソッド: `calculateDeliveryFee(BigDecimal totalPrice, String deliveryAddress)`
    - ロジック: 5000円以上は送料無料、沖縄県は1700円、その他は800円
    - `AddressUtil.isOkinawa()`を使用して沖縄県判定

---

### セクション5: 認証フィルター

- [X] **T_COMMON_015**: AuthenticationFilterの作成
  - **目的**: 未ログインユーザーの保護ページアクセスを制限する認証フィルターを実装する
  - **対象**: `pro.kensait.berrybooks.web.filter.AuthenticationFilter`（Servlet Filter）
  - **参照SPEC**: 
    - [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9. セキュリティ」
    - [behaviors.md](../specs/baseline/system/behaviors.md) の「BR-034」
  - **注意事項**: 
    - `@WebFilter(urlPatterns = "/*")`を付与
    - セッションから`CustomerBean`を取得
    - 公開ページ（`index.xhtml`, `customerInput.xhtml`, `customerOutput.xhtml`）はスキップ
    - 未ログインの場合、`index.xhtml`にリダイレクト

---

## 並行実行可能なタスク

以下のタスクは並行して実行できます：

- **セクション1: 共通ユーティリティ**（T_COMMON_001, T_COMMON_002, T_COMMON_003）
- **セクション2: 共通エンティティ**（T_COMMON_004〜T_COMMON_011）は依存関係あり：
  - Publisher, Category, Book, Stock は並行可能
  - OrderTran, OrderDetailPK, OrderDetail は順次実行（依存関係あり）
- **セクション3: 共通Dao**（T_COMMON_012）
- **セクション4: 共通サービス**（T_COMMON_013, T_COMMON_014）

---

## 完了条件

- [X] 全ての共通ユーティリティクラスが実装されている
- [X] 全ての共通エンティティクラスが実装され、JPAマッピングが正しい
- [X] 全ての共通Daoクラスが実装されている
- [X] 全ての共通サービスクラスが実装されている
- [X] 認証フィルターが実装されている
- [X] プロジェクトがビルドでき、エラーがない

---

## 次のステップ

共通機能完了後、以下のいずれかの機能別実装タスクに進んでください（並行実行可能）：

- [F_001_book_search.md](F_001_book_search.md) - 書籍検索・閲覧
- [F_002_shopping_cart.md](F_002_shopping_cart.md) - ショッピングカート管理
- [F_003_order_processing.md](F_003_order_processing.md) - 注文処理
- [F_004_customer_auth.md](F_004_customer_auth.md) - 顧客管理・認証
- [F_005_order_history.md](F_005_order_history.md) - 注文履歴参照

