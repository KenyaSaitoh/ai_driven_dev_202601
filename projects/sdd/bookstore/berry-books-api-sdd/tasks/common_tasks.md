# 共通機能タスク

担当者: 共通機能チーム（1-2名）  
推奨スキル: Jakarta EE, JPA, CDI, JWT, REST Client  
想定工数: 8時間  
依存タスク: [setup_tasks.md](setup_tasks.md)

---

## 概要

複数のAPIで共有される共通コンポーネントを実装する。エンティティ、DAO、JWT認証基盤、外部API連携クライアント、共通DTO、ユーティリティ、例外ハンドラを作成する。

---

## タスクリスト

### エンティティ層

#### T_COMMON_001: [P] OrderTran エンティティの作成

* 目的: 注文トランザクションエンティティを実装する
* 対象: OrderTran.java (JPAエンティティ)
* 参照SPEC: [data_model.md](../specs/baseline/system/data_model.md) の「3.6 ORDER_TRAN（注文トランザクション）」
* 注意事項: 
  * CUSTOMER_IDは外部キー制約なし（論理参照のみ、customer-hub-apiが管理）
  * IDENTITYによる自動採番

---

#### T_COMMON_002: [P] OrderDetail エンティティの作成

* 目的: 注文明細エンティティを実装する
* 対象: OrderDetail.java (JPAエンティティ)
* 参照SPEC: [data_model.md](../specs/baseline/system/data_model.md) の「3.7 ORDER_DETAIL（注文明細）」
* 注意事項: 
  * 複合主キー（ORDER_TRAN_ID, ORDER_DETAIL_ID）
  * BOOK_IDは外部キー制約なし（論理参照のみ、back-office-apiが管理）
  * スナップショットパターン（BOOK_NAME, PUBLISHER_NAME, PRICE）

---

#### T_COMMON_003: [P] OrderDetailPK エンティティの作成

* 目的: 注文明細の複合主キークラスを実装する
* 対象: OrderDetailPK.java (Embeddableクラス)
* 参照SPEC: [data_model.md](../specs/baseline/system/data_model.md) の「3.7.5 複合主キー」
* 注意事項: @Embeddable、equals/hashCode実装

---

### DAO層

#### T_COMMON_004: [P] OrderTranDao の作成

* 目的: 注文トランザクションのCRUD操作を実装する
* 対象: OrderTranDao.java (DAOクラス)
* 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「2.3 コンポーネントの責務」
* 注意事項: 
  * @ApplicationScoped
  * EntityManagerをインジェクション
  * insert, findById, findByCustomerIdメソッド

---

#### T_COMMON_005: [P] OrderDetailDao の作成

* 目的: 注文明細のCRUD操作を実装する
* 対象: OrderDetailDao.java (DAOクラス)
* 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「2.3 コンポーネントの責務」
* 注意事項: 
  * @ApplicationScoped
  * EntityManagerをインジェクション
  * insert, findByIdメソッド

---

### JWT認証基盤

#### T_COMMON_006: [P] JwtUtil の作成

* 目的: JWT生成・検証ユーティリティを実装する
* 対象: JwtUtil.java (ユーティリティクラス)
* 参照SPEC: 
  * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「6. JWT認証アーキテクチャ」
  * [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「6.1 JWT設定」
* 注意事項: 
  * @ApplicationScoped
  * jjwt 0.12.6を使用
  * MicroProfile Configで設定読み込み（@PostConstructで明示的に初期化）
  * generateToken, validateToken, getCustomerIdFromToken, getEmailFromToken, extractJwtFromRequestメソッド

---

#### T_COMMON_007: [P] AuthenContext の作成

* 目的: 認証情報をスレッドローカルで管理するコンテキストクラスを実装する
* 対象: AuthenContext.java (ユーティリティクラス)
* 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「2.3 コンポーネントの責務」
* 注意事項: 
  * ThreadLocalで顧客ID、メールアドレスを保持
  * set, get, clearメソッド

---

#### T_COMMON_008: JwtAuthenFilter の作成

* 目的: JWT認証フィルターを実装する
* 対象: JwtAuthenFilter.java (Servletフィルター)
* 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「6.3 JWTフィルターの実装詳細」
* 注意事項: 
  * @WebFilter
  * コンテキストパス対応（リクエストURIからコンテキストパスを除外）
  * PUBLIC_ENDPOINTSで認証除外エンドポイントを定義
  * JWT検証失敗時は401エラー、MediaType.APPLICATION_JSONを設定

---

### 外部API連携クライアント

#### T_COMMON_009: [P] BackOfficeRestClient の作成

* 目的: back-office-apiとの連携クライアントを実装する
* 対象: BackOfficeRestClient.java (REST Clientクラス)
* 参照SPEC: [external_interface.md](../specs/baseline/system/external_interface.md) の「6. back-office-api連携」、「7. back-office-api エンドポイント概要」
* 注意事項: 
  * @ApplicationScoped
  * JAX-RS Client APIを使用
  * MicroProfile Configで外部API URL読み込み（ConfigProvider.getConfig()で明示的に初期化）
  * findAllBooks, findBookById, searchBooks（JPQL/Criteria）, getAllCategories, findStockById, updateStockメソッド

---

#### T_COMMON_010: [P] CustomerHubRestClient の作成

* 目的: customer-hub-apiとの連携クライアントを実装する
* 対象: CustomerHubRestClient.java (REST Clientクラス)
* 参照SPEC: [external_interface.md](../specs/baseline/system/external_interface.md) の「3. customer-hub-api連携」、「4. customer-hub-api エンドポイント」
* 注意事項: 
  * @ApplicationScoped
  * JAX-RS Client APIを使用
  * MicroProfile Configで外部API URL読み込み（ConfigProvider.getConfig()で明示的に初期化）
  * findByEmail, findById, registerCustomerメソッド

---

### 外部API用DTO

#### T_COMMON_011: [P] BookTO の作成

* 目的: 外部API（back-office-api）の書籍DTOを実装する
* 対象: BookTO.java (Record型DTO)
* 参照SPEC: [API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「4. データ構造」
* 注意事項: 
  * Record型（immutable）
  * bookId, bookName, author, categoryId, publisherId, price, category, publisher, stockフィールド

---

#### T_COMMON_012: [P] CategoryTO の作成

* 目的: 外部API（back-office-api）のカテゴリDTOを実装する
* 対象: CategoryTO.java (Record型DTO)
* 参照SPEC: [API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「4.2 Category」
* 注意事項: Record型、categoryId, categoryNameフィールド

---

#### T_COMMON_013: [P] PublisherTO の作成

* 目的: 外部API（back-office-api）の出版社DTOを実装する
* 対象: PublisherTO.java (Record型DTO)
* 参照SPEC: [API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「4.3 Publisher」
* 注意事項: Record型、publisherId, publisherNameフィールド

---

#### T_COMMON_014: [P] StockTO の作成

* 目的: 外部API（back-office-api）の在庫DTOを実装する
* 対象: StockTO.java (Record型DTO)
* 参照SPEC: [API_002_books/functional_design.md](../specs/baseline/api/API_002_books/functional_design.md) の「4.4 Stock」
* 注意事項: Record型、bookId, quantity, versionフィールド

---

#### T_COMMON_015: [P] CustomerTO の作成

* 目的: 外部API（customer-hub-api）の顧客DTOを実装する
* 対象: CustomerTO.java (Record型DTO)
* 参照SPEC: [external_interface.md](../specs/baseline/system/external_interface.md) の「5. CustomerTO スキーマ」
* 注意事項: Record型、customerId, customerName, password, email, birthday, addressフィールド

---

### 共通DTO

#### T_COMMON_016: [P] ErrorResponse の作成

* 目的: 統一的なエラーレスポンスDTOを実装する
* 対象: ErrorResponse.java (Record型DTO)
* 参照SPEC: [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「4.4 ErrorResponse」
* 注意事項: Record型、status, error, message, pathフィールド

---

### 共通ユーティリティ

#### T_COMMON_017: [P] AddressUtil の作成

* 目的: 住所検証ユーティリティを実装する
* 対象: AddressUtil.java (ユーティリティクラス)
* 参照SPEC: [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「3.3.5 ビジネスルール」
* 注意事項: startsWithValidPrefectureメソッド（住所が都道府県名から始まるか検証）

---

#### T_COMMON_018: [P] MessageUtil の作成

* 目的: メッセージリソース読み込みユーティリティを実装する
* 対象: MessageUtil.java (ユーティリティクラス)
* 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md)
* 注意事項: messages.propertiesからメッセージを読み込むgetMessageメソッド

---

### 共通Enum

#### T_COMMON_019: [P] SettlementType の作成

* 目的: 決済方法Enumを実装する
* 対象: SettlementType.java (Enumクラス)
* 参照SPEC: [data_model.md](../specs/baseline/system/data_model.md) の「3.6.5 決済方法（SETTLEMENT_TYPE）」
* 注意事項: BANK(1), CREDIT(2), COD(3)の3種類

---

### 例外ハンドラ

#### T_COMMON_020: [P] GenericExceptionMapper の作成

* 目的: 汎用例外マッパーを実装する
* 対象: GenericExceptionMapper.java (ExceptionMapper)
* 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.2 Exception Mapper」
* 注意事項: 
  * @Provider
  * Exception.classをマッピング
  * 500 Internal Server Error
  * MediaType.APPLICATION_JSONを設定

---

#### T_COMMON_021: [P] ValidationExceptionMapper の作成

* 目的: Bean Validation例外マッパーを実装する
* 対象: ValidationExceptionMapper.java (ExceptionMapper)
* 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.2 Exception Mapper」
* 注意事項: 
  * @Provider
  * ConstraintViolationExceptionをマッピング
  * 400 Bad Request
  * MediaType.APPLICATION_JSONを設定

---

#### T_COMMON_022: [P] OutOfStockExceptionMapper の作成

* 目的: 在庫不足例外マッパーを実装する
* 対象: OutOfStockExceptionMapper.java (ExceptionMapper)
* 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.2 Exception Mapper」
* 注意事項: 
  * @Provider
  * OutOfStockExceptionをマッピング
  * 409 Conflict
  * MediaType.APPLICATION_JSONを設定

---

#### T_COMMON_023: [P] OptimisticLockExceptionMapper の作成

* 目的: 楽観的ロック例外マッパーを実装する
* 対象: OptimisticLockExceptionMapper.java (ExceptionMapper)
* 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.2 Exception Mapper」
* 注意事項: 
  * @Provider
  * OptimisticLockExceptionをマッピング
  * 409 Conflict
  * MediaType.APPLICATION_JSONを設定

---

### ビジネス例外

#### T_COMMON_024: [P] OutOfStockException の作成

* 目的: 在庫不足例外を実装する
* 対象: OutOfStockException.java (RuntimeException)
* 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.1 例外階層」
* 注意事項: bookId, bookNameフィールド、カスタムメッセージ

---

### サービス層（配送料金計算）

#### T_COMMON_025: [P] DeliveryFeeService の作成

* 目的: 配送料金計算サービスを実装する
* 対象: DeliveryFeeService.java (サービスクラス)
* 参照SPEC: [behaviors.md](../specs/baseline/system/behaviors.md) の「7. 配送料金計算」
* 注意事項: 
  * @ApplicationScoped
  * calculateFeeメソッド（購入金額、配送先住所から配送料金を計算）
  * 購入金額10,000円未満: 800円、10,000円以上: 0円
  * 沖縄県は別料金

---

## 完了基準

* [ ] 注文エンティティ（OrderTran, OrderDetail, OrderDetailPK）が実装されている
* [ ] 注文DAO（OrderTranDao, OrderDetailDao）が実装されている
* [ ] JWT認証基盤（JwtUtil, AuthenContext, JwtAuthenFilter）が実装されている
* [ ] 外部API連携クライアント（BackOfficeRestClient, CustomerHubRestClient）が実装されている
* [ ] 外部API用DTO（BookTO, CategoryTO, PublisherTO, StockTO, CustomerTO）が実装されている
* [ ] 共通DTO（ErrorResponse）が実装されている
* [ ] 共通ユーティリティ（AddressUtil, MessageUtil）が実装されている
* [ ] 共通Enum（SettlementType）が実装されている
* [ ] 例外ハンドラ（GenericExceptionMapper, ValidationExceptionMapper, OutOfStockExceptionMapper, OptimisticLockExceptionMapper）が実装されている
* [ ] ビジネス例外（OutOfStockException）が実装されている
* [ ] 配送料金計算サービス（DeliveryFeeService）が実装されている
* [ ] 共通コンポーネントのユニットテストが実装されている（DeliveryFeeService、AddressUtil等）

---

## 参考資料

* [architecture_design.md](../specs/baseline/system/architecture_design.md) - アーキテクチャ設計書
* [data_model.md](../specs/baseline/system/data_model.md) - データモデル仕様書
* [external_interface.md](../specs/baseline/system/external_interface.md) - 外部インターフェース仕様書
* [behaviors.md](../specs/baseline/system/behaviors.md) - 振る舞い仕様書
