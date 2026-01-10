# 共通機能タスク

**担当者:** 共通機能チーム（1-2名）  
**推奨スキル:** JPA、JWT認証、REST Client API、CDI  
**想定工数:** 8時間  
**依存タスク:** [setup_tasks.md](setup_tasks.md)

---

## タスク一覧

### エンティティ層（注文関連のみ）

- [X] **T_COMMON_001**: OrderDetailPK（複合主キークラス）の作成
  - **目的**: 注文明細の複合主キーを定義する
  - **対象**: OrderDetailPK.java（@Embeddable）
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.7.5 複合主キーの設計」
  - **注意事項**: orderTranId, orderDetailIdをフィールドとして持つ、equalsとhashCodeを実装

---

- [X] [P] **T_COMMON_002**: OrderTranエンティティの作成
  - **目的**: 注文トランザクションのエンティティを実装する
  - **対象**: OrderTran.java（@Entity）
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.6 ORDER_TRAN」
  - **注意事項**: customerId（外部キー）のみ保持、Customerエンティティとのリレーションは不要

---

- [X] [P] **T_COMMON_003**: OrderDetailエンティティの作成
  - **目的**: 注文明細のエンティティを実装する
  - **対象**: OrderDetail.java（@Entity）
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.7 ORDER_DETAIL」
  - **注意事項**: 複合主キー（@IdClass）、bookId（外部キー）のみ保持、Bookエンティティとのリレーションは不要

---

### DAO層（注文関連のみ）

- [X] **T_COMMON_004**: OrderTranDaoの作成
  - **目的**: 注文トランザクションのデータアクセスを実装する
  - **対象**: OrderTranDao.java（@ApplicationScoped）
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「5.2 注文履歴取得」
  - **注意事項**: insert、findById、findByCustomerId、findHistoryByCustomerIdメソッドを実装

---

- [X] **T_COMMON_005**: OrderDetailDaoの作成
  - **目的**: 注文明細のデータアクセスを実装する
  - **対象**: OrderDetailDao.java（@ApplicationScoped）
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.7 ORDER_DETAIL」
  - **注意事項**: insert、findByOrderTranId、findByOrderDetailPKメソッドを実装

---

### JWT認証基盤

- [X] **T_COMMON_006**: JwtUtilの作成
  - **目的**: JWT生成・検証ユーティリティを実装する
  - **対象**: JwtUtil.java（@ApplicationScoped）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「6. JWT認証アーキテクチャ」
  - **注意事項**: generateToken、validateToken、getCustomerIdFromToken、getEmailFromTokenメソッドを実装、@PostConstructで初期化

---

- [X] **T_COMMON_007**: AuthenContextの作成
  - **目的**: JWT認証情報を保持するスレッドローカルコンテキストを実装する
  - **対象**: AuthenContext.java（@RequestScoped）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「6.1 認証フロー」
  - **注意事項**: customerId、emailをスレッドローカルで保持

---

- [X] **T_COMMON_008**: JwtAuthenFilterの作成
  - **目的**: JWT認証フィルターを実装する
  - **対象**: JwtAuthenFilter.java（@Provider, @PreMatching）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「6.3 JWTフィルターの実装詳細」
  - **注意事項**: コンテキストパス対応、公開エンドポイント除外（/api/auth/login等）

---

### 外部API連携クライアント

- [X] **T_COMMON_009**: CustomerHubRestClientの作成
  - **目的**: customer-hub-apiとの連携クライアントを実装する
  - **対象**: CustomerHubRestClient.java（@ApplicationScoped）
  - **参照SPEC**: [external_interface.md](../specs/baseline/system/external_interface.md) の「3. customer-hub-api連携」
  - **注意事項**: findByEmail、findById、registerメソッドを実装、@PostConstructで初期化

---

- [X] **T_COMMON_010**: BackOfficeRestClientの作成
  - **目的**: back-office-apiとの連携クライアントを実装する
  - **対象**: BackOfficeRestClient.java（@ApplicationScoped）
  - **参照SPEC**: [external_interface.md](../specs/baseline/system/external_interface.md) の「14. back-office-api連携」「15. back-office-api エンドポイント仕様」
  - **注意事項**: findAllBooks、findBookById、searchBooksJpql、searchBooksCriteria、findAllCategories、findStockById、updateStockメソッドを実装

---

### 外部API用DTO

- [X] [P] **T_COMMON_011**: CustomerTOの作成
  - **目的**: 顧客情報転送用DTOを実装する
  - **対象**: CustomerTO.java（Record）
  - **参照SPEC**: [external_interface.md](../specs/baseline/system/external_interface.md) の「5.1 CustomerTO」
  - **注意事項**: customerId、customerName、password、email、birthday、addressフィールドを持つ

---

- [X] [P] **T_COMMON_012**: BookTOの作成
  - **目的**: 書籍情報転送用DTOを実装する
  - **対象**: BookTO.java（Record）
  - **参照SPEC**: [external_interface.md](../specs/baseline/system/external_interface.md) の「15.1 書籍一覧取得」
  - **注意事項**: bookId、bookName、author、categoryId、publisherId、price、category、publisher、stockフィールドを持つ

---

- [X] [P] **T_COMMON_013**: StockTOの作成
  - **目的**: 在庫情報転送用DTOを実装する
  - **対象**: StockTO.java（Record）
  - **参照SPEC**: [external_interface.md](../specs/baseline/system/external_interface.md) の「15.6 在庫取得」
  - **注意事項**: bookId、quantity、versionフィールドを持つ

---

- [X] [P] **T_COMMON_014**: CategoryTOの作成
  - **目的**: カテゴリ情報転送用DTOを実装する
  - **対象**: CategoryTO.java（Record）
  - **参照SPEC**: [external_interface.md](../specs/baseline/system/external_interface.md) の「15.5 カテゴリ一覧取得」
  - **注意事項**: categoryId、categoryNameフィールドを持つ

---

- [X] [P] **T_COMMON_015**: PublisherTOの作成
  - **目的**: 出版社情報転送用DTOを実装する
  - **対象**: PublisherTO.java（Record）
  - **参照SPEC**: [external_interface.md](../specs/baseline/system/external_interface.md) の「15.1 書籍一覧取得」
  - **注意事項**: publisherId、publisherNameフィールドを持つ

---

### 共通DTO

- [X] [P] **T_COMMON_016**: ErrorResponseの作成
  - **目的**: 統一的なエラーレスポンスDTOを実装する
  - **対象**: ErrorResponse.java（Record）
  - **参照SPEC**: [functional_design.md](../specs/baseline/system/functional_design.md) の「6. エラーレスポンス仕様」
  - **注意事項**: status、error、message、pathフィールドを持つ

---

### 共通例外クラス

- [X] [P] **T_COMMON_017**: OutOfStockExceptionの作成
  - **目的**: 在庫不足例外を実装する
  - **対象**: OutOfStockException.java（RuntimeException）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.1 例外階層」
  - **注意事項**: bookId、bookNameを保持

---

- [X] [P] **T_COMMON_018**: EmailAlreadyExistsExceptionの作成
  - **目的**: メールアドレス重複例外を実装する
  - **対象**: EmailAlreadyExistsException.java（RuntimeException）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.1 例外階層」
  - **注意事項**: emailを保持

---

- [X] [P] **T_COMMON_019**: OptimisticLockExceptionの作成
  - **目的**: 楽観的ロック競合例外を実装する
  - **対象**: OptimisticLockException.java（RuntimeException）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.1 例外階層」
  - **注意事項**: 汎用的なメッセージを保持

---

### Exception Mappers

- [X] [P] **T_COMMON_020**: OutOfStockExceptionMapperの作成
  - **目的**: 在庫不足例外を409 Conflictにマッピングする
  - **対象**: OutOfStockExceptionMapper.java（@Provider）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.2 Exception Mapper」
  - **注意事項**: 409 Conflict、ErrorResponseを返却

---

- [X] [P] **T_COMMON_021**: OptimisticLockExceptionMapperの作成
  - **目的**: 楽観的ロック競合例外を409 Conflictにマッピングする
  - **対象**: OptimisticLockExceptionMapper.java（@Provider）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.2 Exception Mapper」
  - **注意事項**: 409 Conflict、ErrorResponseを返却

---

- [X] [P] **T_COMMON_022**: ValidationExceptionMapperの作成
  - **目的**: Bean Validation例外を400 Bad Requestにマッピングする
  - **対象**: ValidationExceptionMapper.java（@Provider）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.2 Exception Mapper」
  - **注意事項**: ConstraintViolationExceptionを処理

---

- [X] [P] **T_COMMON_023**: GenericExceptionMapperの作成
  - **目的**: その他の例外を500 Internal Server Errorにマッピングする
  - **対象**: GenericExceptionMapper.java（@Provider）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「9.2 Exception Mapper」
  - **注意事項**: 500 Internal Server Error、詳細はログ出力のみ

---

### ユーティリティクラス

- [X] [P] **T_COMMON_024**: AddressUtilの作成
  - **目的**: 住所バリデーション用ユーティリティを実装する
  - **対象**: AddressUtil.java
  - **参照SPEC**: [API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「3.3.5 ビジネスルール」
  - **注意事項**: startsWithValidPrefectureメソッドを実装

---

- [X] [P] **T_COMMON_025**: MessageUtilの作成（オプション）
  - **目的**: メッセージプロパティファイルからメッセージを取得するユーティリティを実装する
  - **対象**: MessageUtil.java
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「16.3 ログ出力規約」
  - **注意事項**: ResourceBundleを使用

---

### Filters

- [X] **T_COMMON_026**: CorsFilterの作成（オプション）
  - **目的**: CORS対応フィルターを実装する
  - **対象**: CorsFilter.java（@Provider, @PreMatching）
  - **参照SPEC**: [requirements.md](../specs/baseline/system/requirements.md) の「6.2 セキュリティ要件」
  - **注意事項**: Access-Control-Allow-Originヘッダーを設定（スキップ）

---

### JAX-RS Application

- [X] **T_COMMON_027**: ApplicationConfigの作成
  - **目的**: JAX-RSアプリケーション設定クラスを実装する
  - **対象**: ApplicationConfig.java（@ApplicationPath）
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「2.2 BFF レイヤードアーキテクチャ」
  - **注意事項**: @ApplicationPath("/api")を設定

---

### 共通Enumクラス

- [X] [P] **T_COMMON_028**: SettlementTypeの作成
  - **目的**: 決済方法のEnumを実装する
  - **対象**: SettlementType.java（Enum）
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.6.5 決済方法」
  - **注意事項**: BANK_TRANSFER(1)、CREDIT_CARD(2)、CASH_ON_DELIVERY(3)を定義

---

### 単体テスト（共通機能）

- [X] [P] **T_COMMON_029**: AddressUtilのテスト
  - **目的**: AddressUtilの単体テストを実装する
  - **対象**: AddressUtilTest.java（JUnit 5）
  - **参照SPEC**: [behaviors.md](../specs/baseline/system/behaviors.md) の「2.3 新規登録」
  - **注意事項**: 都道府県名から始まる住所のテスト、境界値テスト

---

## 参考資料

- [architecture_design.md](../specs/baseline/system/architecture_design.md) - アーキテクチャ設計書
- [data_model.md](../specs/baseline/system/data_model.md) - データモデル仕様書
- [external_interface.md](../specs/baseline/system/external_interface.md) - 外部インターフェース仕様書
- [functional_design.md](../specs/baseline/system/functional_design.md) - 機能設計書
