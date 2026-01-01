# 共通機能タスク

**担当者:** 共通機能チーム（1-2名）  
**推奨スキル:** Jakarta EE、JPA、JWT認証、JAX-RS  
**想定工数:** 8時間  
**依存タスク:** [setup_tasks.md](setup_tasks.md)

---

## 概要

複数のAPIで共有される共通コンポーネントを実装します。Entity、共通Service、JWT認証基盤、共通DTO、Exception Mapper等を含みます。

---

## タスクリスト

### 1.1 JPAエンティティ実装

- [ ] [P] **T_COMMON_001**: Publisher エンティティの作成
  - **目的**: 出版社マスタエンティティを実装する
  - **対象**: `pro.kensait.berrybooks.entity.Publisher`
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.1 PUBLISHER（出版社）」
  - **注意事項**: @Entity, @Id, @GeneratedValue（IDENTITY）を使用

- [ ] [P] **T_COMMON_002**: Category エンティティの作成
  - **目的**: カテゴリマスタエンティティを実装する
  - **対象**: `pro.kensait.berrybooks.entity.Category`
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.2 CATEGORY（カテゴリ）」
  - **注意事項**: @Entity, @Id, @GeneratedValue（IDENTITY）を使用

- [ ] **T_COMMON_003**: Book エンティティの作成
  - **目的**: 書籍マスタエンティティを実装する
  - **対象**: `pro.kensait.berrybooks.entity.Book`
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.3 BOOK（書籍）」
  - **注意事項**: 
    - Category、Publisherへの@ManyToOne関係
    - Stockへの@OneToOne関係

- [ ] **T_COMMON_004**: Stock エンティティの作成
  - **目的**: 在庫マスタエンティティを実装する（楽観的ロック対応）
  - **対象**: `pro.kensait.berrybooks.entity.Stock`
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.4 STOCK（在庫）」
  - **注意事項**: 
    - @Version アノテーションを使用
    - Bookへの@OneToOne関係

- [ ] **T_COMMON_005**: Customer エンティティの作成
  - **目的**: 顧客マスタエンティティを実装する
  - **対象**: `pro.kensait.berrybooks.entity.Customer`
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.5 CUSTOMER（顧客）」
  - **注意事項**: パスワードはBCryptハッシュで保存

- [ ] **T_COMMON_006**: OrderTran エンティティの作成
  - **目的**: 注文トランザクションエンティティを実装する
  - **対象**: `pro.kensait.berrybooks.entity.OrderTran`
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.6 ORDER_TRAN（注文トランザクション）」
  - **注意事項**: 
    - Customerへの@ManyToOne関係
    - OrderDetailへの@OneToMany関係

- [ ] **T_COMMON_007**: OrderDetail エンティティと複合主キーの作成
  - **目的**: 注文明細エンティティと複合主キークラスを実装する
  - **対象**: 
    - `pro.kensait.berrybooks.entity.OrderDetail`
    - `pro.kensait.berrybooks.entity.OrderDetailPK`
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.7 ORDER_DETAIL（注文明細）」
  - **注意事項**: 
    - 複合主キー（@IdClass）を使用
    - OrderTranへの@ManyToOne関係
    - Bookへの@ManyToOne関係

---

### 1.2 共通DTO/Record実装

- [ ] [P] **T_COMMON_008**: ErrorResponse レコードの作成
  - **目的**: 統一的なエラーレスポンス形式を実装する
  - **対象**: `pro.kensait.berrybooks.api.dto.ErrorResponse`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「8.3 統一的なエラーレスポンス形式」
  - **注意事項**: Java Record を使用（status, error, message, path）

---

### 1.3 JWT認証基盤実装

- [ ] **T_COMMON_009**: JwtUtil クラスの作成
  - **目的**: JWT生成・検証ユーティリティを実装する
  - **対象**: `pro.kensait.berrybooks.security.JwtUtil`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「5. JWT認証アーキテクチャ」
  - **注意事項**: 
    - jjwt 0.12.6 ライブラリを使用
    - generateToken(), validateToken(), getCustomerIdFromToken() メソッドを実装
    - MicroProfile Configから秘密鍵と有効期限を読み込む

- [ ] **T_COMMON_010**: SecuredResource クラスの作成
  - **目的**: 認証済みリソース情報を管理する
  - **対象**: `pro.kensait.berrybooks.security.SecuredResource`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.1 パッケージ編成」
  - **注意事項**: 
    - @RequestScoped を使用
    - customerId, email を保持

- [ ] **T_COMMON_011**: JwtAuthenticationFilter の作成
  - **目的**: JWT認証フィルターを実装する
  - **対象**: `pro.kensait.berrybooks.security.JwtAuthenticationFilter`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「5.1 認証フロー」
  - **注意事項**: 
    - @Provider を使用
    - ContainerRequestFilter を実装
    - JWT Cookie から認証情報を取得し、SecuredResource に設定

---

### 1.4 共通ユーティリティ実装

- [ ] [P] **T_COMMON_012**: MessageUtil クラスの作成
  - **目的**: メッセージリソースユーティリティを実装する
  - **対象**: `pro.kensait.berrybooks.common.MessageUtil`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.1 パッケージ編成」
  - **注意事項**: messages.propertiesからメッセージを読み込む

- [ ] [P] **T_COMMON_013**: AddressUtil クラスの作成
  - **目的**: 住所処理ユーティリティを実装する
  - **対象**: `pro.kensait.berrybooks.util.AddressUtil`
  - **参照SPEC**: [api/API_001_auth/functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「3.3.5 ビジネスルール」
  - **注意事項**: startsWithValidPrefecture() メソッドで都道府県名チェック

- [ ] [P] **T_COMMON_014**: SettlementType 列挙型の作成
  - **目的**: 決済方法列挙型を実装する
  - **対象**: `pro.kensait.berrybooks.common.SettlementType`
  - **参照SPEC**: [data_model.md](../specs/baseline/system/data_model.md) の「3.6.5 決済方法（SETTLEMENT_TYPE）」
  - **注意事項**: 
    - BANK_TRANSFER(1), CREDIT_CARD(2), CASH_ON_DELIVERY(3) を定義

---

### 1.5 共通サービス実装

- [ ] [P] **T_COMMON_015**: DeliveryFeeService の作成
  - **目的**: 配送料金計算サービスを実装する
  - **対象**: `pro.kensait.berrybooks.service.delivery.DeliveryFeeService`
  - **参照SPEC**: [functional_design.md](../specs/baseline/system/functional_design.md) の「4.1.5 ビジネスルール」
  - **注意事項**: 
    - @ApplicationScoped を使用
    - 購入金額10,000円未満: 800円
    - 購入金額10,000円以上: 0円
    - 沖縄県: 特別料金

---

### 1.6 共通DAO実装

- [ ] [P] **T_COMMON_016**: CategoryDao の作成
  - **目的**: カテゴリデータアクセスクラスを実装する
  - **対象**: `pro.kensait.berrybooks.dao.CategoryDao`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.1 パッケージ編成」
  - **注意事項**: 
    - @ApplicationScoped を使用
    - @PersistenceContext で EntityManager を注入
    - findAll() メソッドを実装

---

### 1.7 外部API連携クライアント実装

- [ ] **T_COMMON_017**: CustomerRestClient の作成
  - **目的**: 顧客管理REST APIクライアントを実装する
  - **対象**: `pro.kensait.berrybooks.external.CustomerRestClient`
  - **参照SPEC**: [external_interface.md](../specs/baseline/system/external_interface.md) の「4. APIエンドポイント仕様」
  - **注意事項**: 
    - @ApplicationScoped を使用
    - Jakarta RESTful Web Services Client API を使用
    - findByEmail(), findById(), register() メソッドを実装

- [ ] [P] **T_COMMON_018**: CustomerTO レコードの作成
  - **目的**: 顧客転送オブジェクトを実装する
  - **対象**: `pro.kensait.berrybooks.external.dto.CustomerTO`
  - **参照SPEC**: [external_interface.md](../specs/baseline/system/external_interface.md) の「5.1 CustomerTO」
  - **注意事項**: Java Record を使用

---

### 1.8 Exception Mapper実装

- [ ] [P] **T_COMMON_019**: ValidationExceptionMapper の作成
  - **目的**: Bean Validation例外マッパーを実装する
  - **対象**: `pro.kensait.berrybooks.api.exception.ValidationExceptionMapper`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「8.2 Exception Mapper」
  - **注意事項**: 
    - @Provider を使用
    - ConstraintViolationException → 400 Bad Request

- [ ] [P] **T_COMMON_020**: GenericExceptionMapper の作成
  - **目的**: 汎用例外マッパーを実装する
  - **対象**: `pro.kensait.berrybooks.api.exception.GenericExceptionMapper`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「8.2 Exception Mapper」
  - **注意事項**: 
    - @Provider を使用
    - Exception → 500 Internal Server Error

---

### 1.9 JAX-RS設定

- [ ] **T_COMMON_021**: ApplicationConfig の作成
  - **目的**: JAX-RS設定クラスを実装する
  - **対象**: `pro.kensait.berrybooks.api.ApplicationConfig`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「4.1 パッケージ編成」
  - **注意事項**: 
    - @ApplicationPath("/api") を使用
    - jakarta.ws.rs.core.Application を継承

---

### 1.10 ユニットテスト（共通機能）

- [ ] [P] **T_COMMON_022**: JwtUtil のユニットテスト
  - **目的**: JWT生成・検証ロジックのテストを実装する
  - **対象**: `pro.kensait.berrybooks.security.JwtUtilTest`
  - **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「11.2 テストアプローチ」
  - **注意事項**: 
    - JUnit 5 を使用
    - トークン生成、検証、期限切れのテストを実施

- [ ] [P] **T_COMMON_023**: DeliveryFeeService のユニットテスト
  - **目的**: 配送料金計算ロジックのテストを実装する
  - **対象**: `pro.kensait.berrybooks.service.delivery.DeliveryFeeServiceTest`
  - **参照SPEC**: [behaviors.md](../specs/baseline/system/behaviors.md) の「7. 配送料金計算」
  - **注意事項**: 
    - JUnit 5 を使用
    - 境界値テスト（10,000円未満、以上）を実施

- [ ] [P] **T_COMMON_024**: AddressUtil のユニットテスト
  - **目的**: 住所検証ロジックのテストを実装する
  - **対象**: `pro.kensait.berrybooks.util.AddressUtilTest`
  - **参照SPEC**: [api/API_001_auth/behaviors.md](../specs/baseline/api/API_001_auth/behaviors.md) の「4. 新規登録」
  - **注意事項**: 
    - JUnit 5 を使用
    - 都道府県名の正常系・異常系テストを実施

---

## 完了条件

以下の全ての条件を満たしていること：

- [ ] 全てのJPAエンティティが作成され、リレーションシップが定義されている
- [ ] JWT認証基盤（JwtUtil, JwtAuthenticationFilter, SecuredResource）が実装されている
- [ ] 共通DTO、ユーティリティ、サービス、DAOが実装されている
- [ ] 外部API連携クライアント（CustomerRestClient）が実装されている
- [ ] Exception Mapperが実装されている
- [ ] ユニットテストが実装され、カバレッジが80%以上である
- [ ] プロジェクトが正常にビルドできる

---

## 参考資料

- [architecture_design.md](../specs/baseline/system/architecture_design.md) - アーキテクチャ設計書
- [data_model.md](../specs/baseline/system/data_model.md) - データモデル仕様書
- [external_interface.md](../specs/baseline/system/external_interface.md) - 外部インターフェース仕様書
- [behaviors.md](../specs/baseline/system/behaviors.md) - 振る舞い仕様書
