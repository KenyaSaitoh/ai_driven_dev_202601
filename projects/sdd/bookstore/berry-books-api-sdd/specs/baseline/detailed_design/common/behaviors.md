# berry-books-api - 共通機能単体テスト仕様書

プロジェクトID: berry-books-api  
バージョン: 1.0.0  
最終更新日: 2026-01-18  
ステータス: 単体テスト仕様確定

---

## 1. 概要

本文書は、berry-books-apiプロジェクトの共通機能（注文ドメインエンティティ、DAO、JWT認証基盤、外部API連携クライアント、ビジネスロジック層）の単体テスト仕様を記述する。

* テスト方針: メソッドレベルの単体テスト（Given-When-Then形式）
* モックライブラリ: Mockito
* テストフレームワーク: JUnit 5
* テスト粒度: 1メソッド＝1テストケース
* モック対象: 依存関係（EntityManager、外部APIクライアント、DAO等）

* 重要:
  * basic_design/behaviors.md（E2Eテスト用）とは完全に別物
  * basic_design/behaviors.md: システム全体の振る舞い、複数コンポーネント連携、実際のDB/外部API
  * detailed_design/common/behaviors.md: 純粋な単体テスト、1メソッド単位、依存関係はモック

---

## 2. DeliveryFeeService 単体テスト

### 2.1 テスト対象

* クラス: `pro.kensait.berrybooks.service.delivery.DeliveryFeeService`
* メソッド: `calculateDeliveryFee(Integer totalPrice, String deliveryAddress)`

### 2.2 テストケース

#### 2.2.1 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| DFS-001 | 購入金額10,000円未満の場合、標準配送料金800円 | totalPrice=9999, deliveryAddress="東京都渋谷区" | calculateDeliveryFee(9999, "東京都渋谷区") | 800 |
| DFS-002 | 購入金額10,000円の場合、送料無料（0円） | totalPrice=10000, deliveryAddress="東京都渋谷区" | calculateDeliveryFee(10000, "東京都渋谷区") | 0 |
| DFS-003 | 購入金額10,000円以上の場合、送料無料（0円） | totalPrice=15000, deliveryAddress="東京都渋谷区" | calculateDeliveryFee(15000, "東京都渋谷区") | 0 |
| DFS-004 | 沖縄県の場合、特別配送料金1,500円 | totalPrice=9999, deliveryAddress="沖縄県那覇市" | calculateDeliveryFee(9999, "沖縄県那覇市") | 1500 |
| DFS-005 | 沖縄県で購入金額10,000円以上の場合、送料無料（0円） | totalPrice=10000, deliveryAddress="沖縄県那覇市" | calculateDeliveryFee(10000, "沖縄県那覇市") | 0 |

#### 2.2.2 境界値テスト

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| DFS-BV-001 | 購入金額0円の場合、標準配送料金800円 | totalPrice=0, deliveryAddress="東京都渋谷区" | calculateDeliveryFee(0, "東京都渋谷区") | 800 |
| DFS-BV-002 | 購入金額1円の場合、標準配送料金800円 | totalPrice=1, deliveryAddress="東京都渋谷区" | calculateDeliveryFee(1, "東京都渋谷区") | 800 |
| DFS-BV-003 | 購入金額9,999円の場合、標準配送料金800円 | totalPrice=9999, deliveryAddress="東京都渋谷区" | calculateDeliveryFee(9999, "東京都渋谷区") | 800 |
| DFS-BV-004 | 購入金額10,000円の場合、送料無料（0円） | totalPrice=10000, deliveryAddress="東京都渋谷区" | calculateDeliveryFee(10000, "東京都渋谷区") | 0 |
| DFS-BV-005 | 購入金額10,001円の場合、送料無料（0円） | totalPrice=10001, deliveryAddress="東京都渋谷区" | calculateDeliveryFee(10001, "東京都渋谷区") | 0 |

#### 2.2.3 配送先住所のバリエーション

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| DFS-ADDR-001 | 東京都の場合、標準配送料金800円 | totalPrice=9999, deliveryAddress="東京都渋谷区" | calculateDeliveryFee(9999, "東京都渋谷区") | 800 |
| DFS-ADDR-002 | 大阪府の場合、標準配送料金800円 | totalPrice=9999, deliveryAddress="大阪府大阪市" | calculateDeliveryFee(9999, "大阪府大阪市") | 800 |
| DFS-ADDR-003 | 北海道の場合、標準配送料金800円 | totalPrice=9999, deliveryAddress="北海道札幌市" | calculateDeliveryFee(9999, "北海道札幌市") | 800 |
| DFS-ADDR-004 | 沖縄県の場合、特別配送料金1,500円 | totalPrice=9999, deliveryAddress="沖縄県那覇市" | calculateDeliveryFee(9999, "沖縄県那覇市") | 1500 |

---

## 3. OrderService 単体テスト

### 3.1 テスト対象

* クラス: `pro.kensait.berrybooks.service.order.OrderService`
* メソッド: `orderBooks(Long customerId, OrderRequest request)`

### 3.2 モック対象

* OrderTranDao
* OrderDetailDao
* BackOfficeRestClient
* DeliveryFeeService

### 3.3 テストケース

#### 3.3.1 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| OS-001 | 注文作成成功（1冊） | customerId=1<br/>在庫あり（quantity=10, version=1）<br/>配送料金=800 | orderBooks(1, orderRequest) | OrderTranが作成される<br/>OrderDetailが作成される<br/>在庫更新が呼ばれる<br/>配送料金計算が呼ばれる |
| OS-002 | 注文作成成功（複数冊） | customerId=1<br/>2冊の在庫あり | orderBooks(1, orderRequest) | OrderTranが作成される<br/>2件のOrderDetailが作成される<br/>各書籍の在庫更新が呼ばれる |
| OS-003 | 配送料金が正しく計算される | customerId=1<br/>在庫あり<br/>totalPrice=9999 | orderBooks(1, orderRequest) | deliveryFeeService.calculateDeliveryFeeが呼ばれる<br/>deliveryPrice=800 |

#### 3.3.2 異常系（在庫不足）

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| OS-E-001 | 在庫不足エラー | customerId=1<br/>在庫数=5<br/>注文数=10 | orderBooks(1, orderRequest) | OutOfStockExceptionがスローされる<br/>エラーメッセージ: "在庫が不足しています: {bookName}" |
| OS-E-002 | 複数冊注文で1冊在庫不足 | customerId=1<br/>1冊目: 在庫あり<br/>2冊目: 在庫不足 | orderBooks(1, orderRequest) | OutOfStockExceptionがスローされる<br/>1冊目の在庫更新が呼ばれない（トランザクションロールバック） |
| OS-E-003 | 在庫情報が存在しない | customerId=1<br/>findStockByIdがnullを返す | orderBooks(1, orderRequest) | OutOfStockExceptionがスローされる<br/>エラーメッセージ: "在庫情報が見つかりません" |

#### 3.3.3 異常系（楽観的ロック競合）

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| OS-E-004 | 楽観的ロック競合エラー | customerId=1<br/>在庫あり<br/>updateStockでOptimisticLockExceptionをスロー | orderBooks(1, orderRequest) | OptimisticLockExceptionが再スローされる<br/>OrderTran、OrderDetailは作成されない（トランザクションロールバック） |
| OS-E-005 | 複数冊注文で1冊楽観的ロック競合 | customerId=1<br/>1冊目: 在庫更新成功<br/>2冊目: OptimisticLockException | orderBooks(1, orderRequest) | OptimisticLockExceptionが再スローされる<br/>1冊目の在庫更新もロールバック |

#### 3.3.4 Daoメソッド呼び出し検証

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| OS-DAO-001 | OrderTranDao.insert()が呼ばれる | customerId=1<br/>在庫あり | orderBooks(1, orderRequest) | orderTranDao.insert(orderTran)が1回呼ばれる |
| OS-DAO-002 | OrderDetailDao.insert()が呼ばれる（1冊） | customerId=1<br/>在庫あり<br/>1冊注文 | orderBooks(1, orderRequest) | orderDetailDao.insert(orderDetail)が1回呼ばれる |
| OS-DAO-003 | OrderDetailDao.insert()が呼ばれる（複数冊） | customerId=1<br/>在庫あり<br/>3冊注文 | orderBooks(1, orderRequest) | orderDetailDao.insert(orderDetail)が3回呼ばれる |

#### 3.3.5 BackOfficeRestClient呼び出し検証

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| OS-EXT-001 | findStockById()が呼ばれる（1冊） | customerId=1<br/>在庫あり<br/>1冊注文 | orderBooks(1, orderRequest) | backOfficeClient.findStockById(bookId)が1回呼ばれる |
| OS-EXT-002 | findStockById()が呼ばれる（複数冊） | customerId=1<br/>在庫あり<br/>3冊注文 | orderBooks(1, orderRequest) | backOfficeClient.findStockById(bookId)が3回呼ばれる |
| OS-EXT-003 | updateStock()が呼ばれる（1冊） | customerId=1<br/>在庫あり<br/>1冊注文 | orderBooks(1, orderRequest) | backOfficeClient.updateStock(bookId, version, newQuantity)が1回呼ばれる |
| OS-EXT-004 | updateStock()が呼ばれる（複数冊） | customerId=1<br/>在庫あり<br/>3冊注文 | orderBooks(1, orderRequest) | backOfficeClient.updateStock(bookId, version, newQuantity)が3回呼ばれる |

#### 3.3.6 スナップショット保存検証

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| OS-SNAP-001 | BOOK_NAMEがスナップショット保存される | customerId=1<br/>bookName="Java完全理解" | orderBooks(1, orderRequest) | OrderDetail.bookName="Java完全理解" |
| OS-SNAP-002 | PUBLISHER_NAMEがスナップショット保存される | customerId=1<br/>publisherName="技術評論社" | orderBooks(1, orderRequest) | OrderDetail.publisherName="技術評論社" |
| OS-SNAP-003 | PRICEがスナップショット保存される | customerId=1<br/>price=3200 | orderBooks(1, orderRequest) | OrderDetail.price=3200 |

---

## 4. JwtUtil 単体テスト

### 4.1 テスト対象

* クラス: `pro.kensait.berrybooks.security.JwtUtil`
* メソッド: 
  * `generateToken(Long customerId, String email)`
  * `validateToken(String token)`
  * `getUserIdFromToken(String token)`
  * `getEmailFromToken(String token)`

### 4.2 テストケース

#### 4.2.1 generateToken() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| JWT-GEN-001 | JWTトークンが生成される | customerId=1, email="alice@gmail.com" | generateToken(1L, "alice@gmail.com") | JWT文字列が返される<br/>nullでない<br/>空文字列でない |
| JWT-GEN-002 | トークンにcustomerIdが含まれる | customerId=123, email="alice@gmail.com" | generateToken(123L, "alice@gmail.com") | getUserIdFromToken(token) == 123L |
| JWT-GEN-003 | トークンにemailが含まれる | customerId=1, email="test@example.com" | generateToken(1L, "test@example.com") | getEmailFromToken(token) == "test@example.com" |

#### 4.2.2 validateToken() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| JWT-VAL-001 | 有効なトークンの検証に成功 | 有効なJWT | validateToken(token) | true |
| JWT-VAL-002 | 生成直後のトークンの検証に成功 | 生成直後のJWT | validateToken(token) | true |

#### 4.2.3 validateToken() - 異常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| JWT-VAL-E001 | 無効なトークン形式 | token="invalid-token" | validateToken("invalid-token") | false |
| JWT-VAL-E002 | 改ざんされたトークン | 改ざんされたJWT | validateToken(tamperedToken) | false |
| JWT-VAL-E003 | nullトークン | token=null | validateToken(null) | false |
| JWT-VAL-E004 | 空文字列トークン | token="" | validateToken("") | false |

#### 4.2.4 getUserIdFromToken() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| JWT-UID-001 | 顧客IDが取得できる | customerId=1のJWT | getUserIdFromToken(token) | 1L |
| JWT-UID-002 | 大きな顧客IDが取得できる | customerId=999999のJWT | getUserIdFromToken(token) | 999999L |

#### 4.2.5 getEmailFromToken() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| JWT-EMAIL-001 | メールアドレスが取得できる | email="alice@gmail.com"のJWT | getEmailFromToken(token) | "alice@gmail.com" |
| JWT-EMAIL-002 | 長いメールアドレスが取得できる | email="very.long.email@example.com"のJWT | getEmailFromToken(token) | "very.long.email@example.com" |

#### 4.2.6 extractJwtFromRequest() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| JWT-EXT-001 | CookieからJWTを抽出できる | HttpServletRequestにJWT Cookieあり | extractJwtFromRequest(request) | JWT文字列が返される |
| JWT-EXT-002 | 複数Cookieの中からJWTを抽出できる | 複数Cookie（JWT Cookie含む） | extractJwtFromRequest(request) | JWT文字列が返される |

#### 4.2.7 extractJwtFromRequest() - 異常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| JWT-EXT-E001 | Cookie未設定 | HttpServletRequestにCookieなし | extractJwtFromRequest(request) | null |
| JWT-EXT-E002 | JWT Cookie未設定 | HttpServletRequestに他のCookieのみ | extractJwtFromRequest(request) | null |

---

## 5. OrderTranDao 単体テスト

### 5.1 テスト対象

* クラス: `pro.kensait.berrybooks.dao.OrderTranDao`
* メソッド:
  * `insert(OrderTran orderTran)`
  * `findById(Integer orderTranId)`
  * `findByCustomerId(Integer customerId)`

### 5.2 モック対象

* EntityManager

### 5.3 テストケース

#### 5.3.1 insert() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| OTDAO-INS-001 | 注文トランザクションが登録される | OrderTranエンティティ | insert(orderTran) | em.persist(orderTran)が呼ばれる<br/>em.flush()が呼ばれる<br/>OrderTranが返される |

#### 5.3.2 findById() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| OTDAO-FID-001 | IDで注文トランザクションを取得できる | orderTranId=1のOrderTranあり | findById(1) | em.find(OrderTran.class, 1)が呼ばれる<br/>OrderTranが返される |
| OTDAO-FID-002 | 存在しないIDの場合nullを返す | orderTranId=999のOrderTranなし | findById(999) | em.find(OrderTran.class, 999)が呼ばれる<br/>nullが返される |

#### 5.3.3 findByCustomerId() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| OTDAO-FCID-001 | 顧客IDで注文履歴を取得できる | customerId=1の注文あり | findByCustomerId(1) | TypedQueryが作成される<br/>List&lt;OrderTran&gt;が返される |
| OTDAO-FCID-002 | 注文履歴が0件の場合空リストを返す | customerId=999の注文なし | findByCustomerId(999) | 空リストが返される |
| OTDAO-FCID-003 | 注文日降順でソートされる | customerId=1の複数注文あり | findByCustomerId(1) | ORDER BY orderDate DESCでソート |

---

## 6. OrderDetailDao 単体テスト

### 6.1 テスト対象

* クラス: `pro.kensait.berrybooks.dao.OrderDetailDao`
* メソッド:
  * `insert(OrderDetail orderDetail)`
  * `findByOrderTranId(Integer orderTranId)`
  * `findById(OrderDetailPK id)`

### 6.2 モック対象

* EntityManager

### 6.3 テストケース

#### 6.3.1 insert() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| ODDAO-INS-001 | 注文明細が登録される | OrderDetailエンティティ | insert(orderDetail) | em.persist(orderDetail)が呼ばれる<br/>OrderDetailが返される |

#### 6.3.2 findByOrderTranId() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| ODDAO-FTID-001 | 注文IDで注文明細を取得できる | orderTranId=1の明細あり | findByOrderTranId(1) | TypedQueryが作成される<br/>List&lt;OrderDetail&gt;が返される |
| ODDAO-FTID-002 | 注文明細が0件の場合空リストを返す | orderTranId=999の明細なし | findByOrderTranId(999) | 空リストが返される |
| ODDAO-FTID-003 | 明細ID昇順でソートされる | orderTranId=1の複数明細あり | findByOrderTranId(1) | ORDER BY orderDetailId ASCでソート |

#### 6.3.3 findById() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| ODDAO-FID-001 | 複合主キーで注文明細を取得できる | OrderDetailPK(1, 1)の明細あり | findById(pk) | em.find(OrderDetail.class, pk)が呼ばれる<br/>OrderDetailが返される |
| ODDAO-FID-002 | 存在しない明細の場合nullを返す | OrderDetailPK(999, 999)の明細なし | findById(pk) | em.find(OrderDetail.class, pk)が呼ばれる<br/>nullが返される |

---

## 7. BackOfficeRestClient 単体テスト

### 7.1 テスト対象

* クラス: `pro.kensait.berrybooks.external.BackOfficeRestClient`
* メソッド:
  * `getAllBooks()`
  * `getBookById(Integer bookId)`
  * `findStockById(Integer bookId)`
  * `updateStock(Integer bookId, Integer version, Integer newQuantity)`

### 7.2 モック対象

* JAX-RS Client
* WebTarget
* Invocation.Builder

### 7.3 テストケース

#### 7.3.1 getAllBooks() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| BOC-GAB-001 | 全書籍を取得できる | 外部APIが書籍リストを返す | getAllBooks() | List&lt;BookTO&gt;が返される<br/>GETリクエストが送信される |

#### 7.3.2 getBookById() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| BOC-GBI-001 | 書籍詳細を取得できる | 外部APIが書籍情報を返す | getBookById(1) | BookTOが返される<br/>GETリクエストが送信される |
| BOC-GBI-002 | 存在しない書籍の場合nullを返す | 外部APIが404を返す | getBookById(999) | nullが返される |

#### 7.3.3 findStockById() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| BOC-FSI-001 | 在庫情報を取得できる | 外部APIが在庫情報を返す | findStockById(1) | StockTOが返される<br/>GETリクエストが送信される |
| BOC-FSI-002 | 存在しない在庫の場合nullを返す | 外部APIが404を返す | findStockById(999) | nullが返される |

#### 7.3.4 updateStock() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| BOC-UPD-001 | 在庫更新が成功する | 外部APIが更新後の在庫情報を返す | updateStock(1, 1, 8) | StockTOが返される<br/>PUTリクエストが送信される<br/>リクエストボディに{"quantity": 8, "version": 1} |

#### 7.3.5 updateStock() - 異常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| BOC-UPD-E001 | 楽観的ロック競合エラー | 外部APIが409を返す | updateStock(1, 1, 8) | OptimisticLockExceptionがスローされる |

---

## 8. CustomerHubRestClient 単体テスト

### 8.1 テスト対象

* クラス: `pro.kensait.berrybooks.external.CustomerHubRestClient`
* メソッド:
  * `findByEmail(String email)`
  * `findById(Long customerId)`
  * `register(CustomerTO customer)`

### 8.2 モック対象

* JAX-RS Client
* WebTarget
* Invocation.Builder

### 8.3 テストケース

#### 8.3.1 findByEmail() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| CHC-FBE-001 | メールアドレスで顧客を取得できる | 外部APIが顧客情報を返す | findByEmail("alice@gmail.com") | CustomerTOが返される<br/>GETリクエストが送信される |
| CHC-FBE-002 | 存在しないメールアドレスの場合nullを返す | 外部APIが404を返す | findByEmail("notfound@example.com") | nullが返される |

#### 8.3.2 findById() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| CHC-FBI-001 | 顧客IDで顧客を取得できる | 外部APIが顧客情報を返す | findById(1L) | CustomerTOが返される<br/>GETリクエストが送信される |
| CHC-FBI-002 | 存在しない顧客IDの場合nullを返す | 外部APIが404を返す | findById(999L) | nullが返される |

#### 8.3.3 register() - 正常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| CHC-REG-001 | 新規顧客を登録できる | 外部APIが作成された顧客情報を返す | register(customerTO) | CustomerTOが返される<br/>POSTリクエストが送信される |

#### 8.3.4 register() - 異常系

| テストID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|---------|------|----------------|------------|---------------|
| CHC-REG-E001 | メールアドレス重複エラー | 外部APIが409を返す | register(customerTO) | EmailAlreadyExistsExceptionがスローされる |

---

## 9. テスト受入基準

### 9.1 カバレッジ基準

* ビジネスロジック層（Service）: 80%以上
* データアクセス層（Dao）: 70%以上
* 外部API連携層（RestClient）: 70%以上
* セキュリティ層（JwtUtil）: 80%以上

### 9.2 テスト実施基準

* 全ての正常系テストケースが成功すること
* 全ての異常系テストケースが成功すること
* 境界値テストが成功すること
* モックの呼び出し検証が成功すること

### 9.3 品質基準

* テストコードがコンパイルエラーなく動作すること
* テスト実行時間が妥当であること（1テストケースあたり1秒以内）
* テストが独立して実行可能であること（テスト順序依存なし）
* テストが再現可能であること（実行毎に結果が一定）

---

## 10. 参考資料

### 10.1 関連仕様書

* [detailed_design.md](detailed_design.md) - 共通機能詳細設計書
* [../../basic_design/functional_design.md](../../basic_design/functional_design.md) - 機能設計書
* [../../basic_design/behaviors.md](../../basic_design/behaviors.md) - 振る舞い仕様書（E2Eテスト用）

### 10.2 関連タスク

* [../../../tasks/common.md](../../../tasks/common.md) - 共通機能タスク

### 10.3 Agent Skills

* [../../../../../agent_skills/jakarta-ee-api-base/principles/architecture.md](../../../../../agent_skills/jakarta-ee-api-base/principles/architecture.md) - Jakarta EE APIアーキテクチャ標準
