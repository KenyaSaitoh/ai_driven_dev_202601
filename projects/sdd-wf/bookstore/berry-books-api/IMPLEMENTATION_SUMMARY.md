# berry-books-api - Common Domain Implementation Summary

実装日: 2026-02-06  
ドメイン: common（共通ドメイン）  
実装バージョン: 1.0.0

---

## 実装完了コンポーネント

### 1. エンティティ層 (Entity Layer)

#### 実装クラス
* `OrderTran.java` - 注文トランザクションエンティティ
  * 注文の基本情報（注文日、顧客ID、合計金額、配送先等）を管理
  * @OneToMany: OrderDetail（注文明細）との関連
* `OrderDetail.java` - 注文明細エンティティ（スナップショットパターン）
  * 注文時点の書籍情報（書籍名、出版社名、価格）を保存
  * @ManyToOne: OrderTran（注文トランザクション）との関連
* `OrderDetailPK.java` - 注文明細の複合主キー
  * @Embeddable: orderTranId、orderDetailId
  * equals()、hashCode() 実装済み

#### 設計方針
* JPA 3.1準拠のアノテーション使用
* スナップショットパターンによるデータの永続化
* 複合主キー（OrderDetailPK）の適切な実装

---

### 2. DAO層 (Data Access Layer)

#### 実装クラス
* `OrderTranDao.java` - 注文トランザクションDAO
  * insert(OrderTran): 注文登録
  * findById(Integer): 注文ID検索（LEFT JOIN FETCH orderDetails）
  * findByCustomerId(Integer): 顧客別注文履歴取得（ORDER BY orderDate DESC）
* `OrderDetailDao.java` - 注文明細DAO
  * insert(OrderDetail): 注文明細登録
  * findByOrderTranId(Integer): 注文ID別明細取得（ORDER BY orderDetailId）

#### 設計方針
* @ApplicationScoped スコープ
* @PersistenceContext による EntityManager インジェクション
* JPQLによる効率的なクエリ実行
* LEFT JOIN FETCHによるN+1問題の回避
* SLF4Jログ出力

---

### 3. セキュリティ層 (Security Layer)

#### 実装クラス
* `JwtUtil.java` - JWT生成・検証ユーティリティ
  * generateToken(Integer, String): JWTトークン生成
  * validateToken(String): トークン検証
  * getCustomerIdFromToken(String): 顧客ID抽出
  * getEmailFromToken(String): メールアドレス抽出
  * HMAC-SHA256署名、24時間有効期限
* `AuthenInfo.java` - 認証情報コンテキスト
  * @RequestScoped: リクエストスコープで認証情報を管理
  * customerId、emailフィールド
  * isAuthenticated(): 認証済み判定
* `JwtAuthenFilter.java` - JWT認証フィルター
  * @WebFilter(urlPatterns = "/api/*")
  * Cookie（berry_auth）からJWT抽出
  * 認証除外パス: /api/auth/*, /api/books, /api/categories, /api/images
  * 認証必須パス: /api/orders, /api/auth/me

#### 設計方針
* MicroProfile Config によるJWT秘密鍵の外部設定
* HttpOnly Cookie によるXSS対策
* ステートレス認証の実装
* 認証情報のスレッドセーフな管理（@RequestScoped）

---

### 4. 外部API連携層 (External Integration Layer)

#### 実装クラス
* `BackOfficeRestClient.java` - back-office-api REST クライアント
  * getAllBooks(): 全書籍取得
  * getBookById(Integer): 書籍詳細取得
  * searchBooksJpql(Integer, String): 書籍検索（JPQL）
  * getAllCategories(): カテゴリ一覧取得
  * findStockById(Integer): 在庫取得
  * updateStock(Integer, Integer, Long): 在庫更新（楽観的ロック対応）
* `CustomerHubRestClient.java` - customer-hub-api REST クライアント
  * findByEmail(String): メールアドレス検索
  * findById(Integer): 顧客ID検索
  * register(CustomerTO): 新規顧客登録

#### 外部API用DTO
* `BookTO.java` - 書籍情報転送オブジェクト（Record）
* `StockTO.java` - 在庫情報転送オブジェクト（Record）
* `CustomerTO.java` - 顧客情報転送オブジェクト（Record）

#### 設計方針
* @ApplicationScoped スコープ
* JAX-RS Client による REST API呼び出し
* MicroProfile Config による外部APIベースURL設定
* 適切なエラーハンドリング（404、409等）
* @PostConstruct、@PreDestroy によるライフサイクル管理

---

### 5. 例外処理層 (Exception Handling Layer)

#### 例外クラス
* `OutOfStockException.java` - 在庫不足例外
* `AuthenticationException.java` - 認証失敗例外

#### Exception Mapper
* `OutOfStockExceptionMapper.java` - OutOfStockException → 409 Conflict
* `OptimisticLockExceptionMapper.java` - OptimisticLockException → 409 Conflict
* `ValidationExceptionMapper.java` - ConstraintViolationException → 400 Bad Request
* `GenericExceptionMapper.java` - Exception → 500 Internal Server Error

#### 共通DTO
* `ErrorResponse.java` - 統一的なエラーレスポンス形式（Record）
  * status: int
  * error: String
  * message: String
  * path: String

#### 設計方針
* @Provider による JAX-RS Exception Mapper 実装
* 統一的なエラーレスポンス形式
* HTTPステータスコードの適切な使用
* SLF4Jログ出力（WARN、ERROR）

---

### 6. 設定ファイル (Configuration Files)

#### 実装ファイル
* `persistence.xml` - JPA 永続化設定
  * persistence-unit: BerryBooksPU
  * jta-data-source: jdbc/HsqldbDS
  * Entity クラス登録: OrderTran、OrderDetail
  * EclipseLink ログ設定
* `microprofile-config.properties` - MicroProfile Config 設定
  * jwt.secret: JWT秘密鍵
  * back-office-api.base-url: back-office-api ベースURL
  * customer-hub-api.base-url: customer-hub-api ベースURL
* `beans.xml` - CDI 設定
  * bean-discovery-mode: all
* `log4j2.xml` - ログ設定
  * Console Appender、File Appender
  * パッケージ別ログレベル設定

---

## 単体テスト (Unit Tests)

### 実装テストクラス
* `OrderDetailPKTest.java` - 複合主キーのテスト
  * equals/hashCode テスト
  * null比較、境界値テスト
* `OrderTranDaoTest.java` - 注文TRANSACTIONSDAOテスト
  * insert、findById、findByCustomerIdテスト
  * モック使用（EntityManager、TypedQuery）
* `OrderDetailDaoTest.java` - 注文明細DAOテスト
  * insert、findByOrderTranIdテスト
  * モック使用（EntityManager、TypedQuery）
* `JwtUtilTest.java` - JWT生成・検証テスト
  * generateToken、validateToken、getClaims テスト
  * 正常系、異常系テスト
* `AuthenInfoTest.java` - 認証情報コンテキストテスト
  * isAuthenticated テスト
  * 境界値テスト
* `OutOfStockExceptionMapperTest.java` - 在庫不足例外マッパーテスト
  * 409 Conflict レスポンステスト
* `OptimisticLockExceptionMapperTest.java` - 楽観的ロック例外マッパーテスト
  * 409 Conflict レスポンステスト
* `ValidationExceptionMapperTest.java` - バリデーション例外マッパーテスト
  * 400 Bad Request レスポンステスト
* `GenericExceptionMapperTest.java` - 汎用例外マッパーテスト
  * 500 Internal Server Error レスポンステスト

### テスト設計方針
* JUnit 5 + Mockito 使用
* Given-When-Then パターン
* commonドメイン外の依存（EntityManager、外部API）はモック化
* behaviors.md の Gherkin シナリオに基づくテスト設計
* 正常系、異常系、境界値テストの実装

---

## 実装統計

### 生成ファイル数
* 本番コード: 20ファイル
  * Entity: 3ファイル
  * DAO: 2ファイル
  * Security: 3ファイル
  * External: 5ファイル
  * Exception: 7ファイル
* テストコード: 9ファイル
* 設定ファイル: 4ファイル

### 合計: 33ファイル

---

## 準拠仕様書

### 基本設計
* requirements.md - 要件定義書
* architecture_design.md - アーキテクチャ設計書
* functional_design.md - 共通機能設計書
* data_model.md - データモデル仕様書

### 詳細設計
* detailed_design/common/detailed_design.md - 共通ドメイン詳細設計書
* detailed_design/common/behaviors.md - 共通ドメイン振る舞い仕様書（単体テスト用）

### Agent Skills
* principles/common_rules.md - Jakarta EE 共通ルール
* principles/architecture.md - Jakarta EE APIアーキテクチャ標準
* principles/security.md - セキュリティ標準

---

## 技術スタック遵守状況

### 使用技術
* ✅ Java 21
* ✅ Jakarta EE 10
* ✅ JPA 3.1
* ✅ JAX-RS 3.1
* ✅ CDI 4.0
* ✅ Bean Validation 3.0
* ✅ MicroProfile Config 3.0.3
* ✅ jjwt 0.12.6（JWT）
* ✅ BCrypt（パスワードハッシュ化）
* ✅ SLF4J + Log4j2（ログ）
* ✅ JUnit 5（テスト）
* ✅ Mockito（モッキング）

### アーキテクチャパターン
* ✅ レイヤードアーキテクチャ
* ✅ リポジトリパターン（DAO）
* ✅ DTOパターン（Record）
* ✅ JWT認証
* ✅ Exception Mapper
* ✅ スナップショットパターン（OrderDetail）

---

## 検証チェックリスト

### コード生成完了
* [x] Entity層の実装
* [x] DAO層の実装
* [x] Security層の実装
* [x] External Integration層の実装
* [x] Exception処理層の実装
* [x] 設定ファイルの作成

### 単体テスト生成完了
* [x] Entity テスト
* [x] DAO テスト
* [x] Security テスト
* [x] Exception Mapper テスト

### 設計原則の遵守
* [x] architecture_design.mdの技術スタックを遵守
* [x] detailed_design.mdのクラス設計を遵守
* [x] behaviors.mdのテストシナリオを反映
* [x] principles/の原則ドキュメントを遵守
* [x] ログ出力の実装（SLF4J）
* [x] 適切なアノテーション使用
* [x] 命名規則の遵守

### コード品質
* [x] Java 21の機能活用（Records）
* [x] null安全性の考慮（Optional）
* [x] 適切なスコープ使用（@ApplicationScoped、@RequestScoped）
* [x] リソース管理（@PostConstruct、@PreDestroy）
* [x] エラーハンドリングの実装
* [x] ログ出力の実装

---

## 次のステップ

### 実装完了後の推奨タスク
1. 単体テスト実行: `gradle test`
2. カバレッジ確認: `gradle jacocoTestReport`
3. ビルド確認: `gradle build`
4. 次のドメイン実装（orders、books_proxy、images等）

### 注意事項
* commonドメインは他のすべてのドメインの基盤となるため、最優先で実装完了
* 他のドメイン（orders等）の実装前に、commonドメインの単体テストを実行し、動作確認を推奨
* データベーススキーマ（ORDER_TRAN、ORDER_DETAIL）の手動作成が必要
* 外部API（back-office-api、customer-hub-api）が起動していることを確認

---

## 実装完了

commonドメインの本番コードと単体テストの生成が完了しました。

実装バージョン: 1.0.0  
実装者: AI Agent  
実装日時: 2026-02-06
