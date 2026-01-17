# API_003_orders - 注文API 単体テスト仕様書

API ID: API_003_orders  
API名: 注文API  
バージョン: 1.0.0  
最終更新日: 2026-01-18  
ステータス: 単体テスト仕様確定

---

## 1. 概要

本文書は、API_003_orders（注文API）の単体テスト仕様を記述する。

* テスト対象: OrderService、OrderResource
* テストフレームワーク: JUnit 5 + Mockito
* テスト戦略: 純粋な単体テスト（依存関係はモック化）

---

## 2. OrderService 単体テスト

### 2.1 テスト対象メソッド

* `OrderTran orderBooks(OrderRequest request, Integer customerId)`

### 2.2 テストケース

#### TC_ORDER_SERVICE_001: 正常系 - 注文作成成功

* **Given（前提条件）**:
  * customerId = 1
  * OrderRequest:
    * cartItems: 1件（bookId=1, bookName="Java完全理解", publisherName="技術評論社", price=3200, count=2, version=0）
    * totalPrice: 6400
    * deliveryPrice: 800
    * deliveryAddress: "東京都渋谷区1-2-3"
    * settlementType: 1（銀行振込）
  * BackOfficeRestClient.findStockById(1):
    * 戻り値: StockTO（bookId=1, quantity=10, version=0）
  * BackOfficeRestClient.updateStock(1, 0L, 8):
    * 戻り値: StockTO（bookId=1, quantity=8, version=1）
  * OrderTranDao.insert():
    * 戻り値: OrderTran（orderTranId=1、その他フィールド設定済み）
  * OrderDetailDao.insert():
    * 戻り値: OrderDetail（設定済み）

* **When（操作）**:
  * `orderService.orderBooks(request, 1)` を呼び出す

* **Then（期待結果）**:
  * OrderTranが返却される
  * orderTranId: 1
  * customerId: 1
  * totalPrice: 6400
  * deliveryPrice: 800
  * deliveryAddress: "東京都渋谷区1-2-3"
  * settlementType: 1
  * BackOfficeRestClient.findStockById(1)が1回呼ばれる
  * BackOfficeRestClient.updateStock(1, 0L, 8)が1回呼ばれる
  * OrderTranDao.insert()が1回呼ばれる
  * OrderDetailDao.insert()が1回呼ばれる

#### TC_ORDER_SERVICE_002: 正常系 - 複数書籍の注文

* **Given（前提条件）**:
  * customerId = 1
  * OrderRequest:
    * cartItems: 2件
      * 1件目: bookId=1, bookName="Java完全理解", publisherName="技術評論社", price=3200, count=2, version=0
      * 2件目: bookId=2, bookName="Python入門", publisherName="秀和システム", price=2800, count=1, version=0
    * totalPrice: 9200
    * deliveryPrice: 800
    * deliveryAddress: "東京都渋谷区1-2-3"
    * settlementType: 2（クレジットカード）
  * BackOfficeRestClient.findStockById(1):
    * 戻り値: StockTO（bookId=1, quantity=10, version=0）
  * BackOfficeRestClient.findStockById(2):
    * 戻り値: StockTO（bookId=2, quantity=5, version=0）
  * BackOfficeRestClient.updateStock(1, 0L, 8):
    * 戻り値: StockTO（bookId=1, quantity=8, version=1）
  * BackOfficeRestClient.updateStock(2, 0L, 4):
    * 戻り値: StockTO（bookId=2, quantity=4, version=1）
  * OrderTranDao.insert():
    * 戻り値: OrderTran（orderTranId=1）
  * OrderDetailDao.insert():
    * 戻り値: OrderDetail（各明細）

* **When（操作）**:
  * `orderService.orderBooks(request, 1)` を呼び出す

* **Then（期待結果）**:
  * OrderTranが返却される
  * BackOfficeRestClient.findStockById()が2回呼ばれる（bookId=1, bookId=2）
  * BackOfficeRestClient.updateStock()が2回呼ばれる
  * OrderTranDao.insert()が1回呼ばれる
  * OrderDetailDao.insert()が2回呼ばれる

#### TC_ORDER_SERVICE_E001: 異常系 - 在庫不足エラー

* **Given（前提条件）**:
  * customerId = 1
  * OrderRequest:
    * cartItems: 1件（bookId=1, bookName="Java完全理解", publisherName="技術評論社", price=3200, count=10, version=0）
    * totalPrice: 32000
    * deliveryPrice: 0
    * deliveryAddress: "東京都渋谷区1-2-3"
    * settlementType: 1
  * BackOfficeRestClient.findStockById(1):
    * 戻り値: StockTO（bookId=1, quantity=5, version=0） ← 在庫数5、注文数10

* **When（操作）**:
  * `orderService.orderBooks(request, 1)` を呼び出す

* **Then（期待結果）**:
  * OutOfStockExceptionがスローされる
  * 例外メッセージ: "在庫が不足しています: Java完全理解"
  * BackOfficeRestClient.findStockById(1)が1回呼ばれる
  * BackOfficeRestClient.updateStock()は呼ばれない（在庫不足で処理中断）
  * OrderTranDao.insert()は呼ばれない
  * OrderDetailDao.insert()は呼ばれない

#### TC_ORDER_SERVICE_E002: 異常系 - 楽観的ロック競合エラー

* **Given（前提条件）**:
  * customerId = 1
  * OrderRequest:
    * cartItems: 1件（bookId=1, bookName="Java完全理解", publisherName="技術評論社", price=3200, count=2, version=0）
    * totalPrice: 6400
    * deliveryPrice: 800
    * deliveryAddress: "東京都渋谷区1-2-3"
    * settlementType: 1
  * BackOfficeRestClient.findStockById(1):
    * 戻り値: StockTO（bookId=1, quantity=10, version=0）
  * BackOfficeRestClient.updateStock(1, 0L, 8):
    * OptimisticLockExceptionをスロー（version不一致）

* **When（操作）**:
  * `orderService.orderBooks(request, 1)` を呼び出す

* **Then（期待結果）**:
  * OptimisticLockExceptionがスローされる
  * BackOfficeRestClient.findStockById(1)が1回呼ばれる
  * BackOfficeRestClient.updateStock(1, 0L, 8)が1回呼ばれる
  * OrderTranDao.insert()は呼ばれない（楽観的ロック競合で処理中断）
  * OrderDetailDao.insert()は呼ばれない

#### TC_ORDER_SERVICE_E003: 異常系 - 複数書籍で在庫不足（2冊目で失敗）

* **Given（前提条件）**:
  * customerId = 1
  * OrderRequest:
    * cartItems: 2件
      * 1件目: bookId=1, bookName="Java完全理解", publisherName="技術評論社", price=3200, count=2, version=0
      * 2件目: bookId=2, bookName="Python入門", publisherName="秀和システム", price=2800, count=10, version=0
    * totalPrice: 9200
    * deliveryPrice: 800
    * deliveryAddress: "東京都渋谷区1-2-3"
    * settlementType: 1
  * BackOfficeRestClient.findStockById(1):
    * 戻り値: StockTO（bookId=1, quantity=10, version=0）
  * BackOfficeRestClient.findStockById(2):
    * 戻り値: StockTO（bookId=2, quantity=5, version=0） ← 在庫数5、注文数10

* **When（操作）**:
  * `orderService.orderBooks(request, 1)` を呼び出す

* **Then（期待結果）**:
  * OutOfStockExceptionがスローされる
  * 例外メッセージ: "在庫が不足しています: Python入門"
  * BackOfficeRestClient.findStockById()が2回呼ばれる（bookId=1, bookId=2）
  * BackOfficeRestClient.updateStock()は呼ばれない（2冊目で在庫不足を検出）
  * OrderTranDao.insert()は呼ばれない
  * OrderDetailDao.insert()は呼ばれない

#### TC_ORDER_SERVICE_E004: 異常系 - 複数書籍で楽観的ロック競合（2冊目で失敗）

* **Given（前提条件）**:
  * customerId = 1
  * OrderRequest:
    * cartItems: 2件
      * 1件目: bookId=1, bookName="Java完全理解", publisherName="技術評論社", price=3200, count=2, version=0
      * 2件目: bookId=2, bookName="Python入門", publisherName="秀和システム", price=2800, count=1, version=0
    * totalPrice: 9200
    * deliveryPrice: 800
    * deliveryAddress: "東京都渋谷区1-2-3"
    * settlementType: 1
  * BackOfficeRestClient.findStockById(1):
    * 戻り値: StockTO（bookId=1, quantity=10, version=0）
  * BackOfficeRestClient.findStockById(2):
    * 戻り値: StockTO（bookId=2, quantity=5, version=0）
  * BackOfficeRestClient.updateStock(1, 0L, 8):
    * 戻り値: StockTO（bookId=1, quantity=8, version=1）
  * BackOfficeRestClient.updateStock(2, 0L, 4):
    * OptimisticLockExceptionをスロー（2冊目で楽観的ロック競合）

* **When（操作）**:
  * `orderService.orderBooks(request, 1)` を呼び出す

* **Then（期待結果）**:
  * OptimisticLockExceptionがスローされる
  * BackOfficeRestClient.findStockById()が2回呼ばれる
  * BackOfficeRestClient.updateStock()が2回呼ばれる（1冊目は成功、2冊目で失敗）
  * OrderTranDao.insert()は呼ばれない（楽観的ロック競合で処理中断）
  * OrderDetailDao.insert()は呼ばれない
  * 注意: @Transactionalにより、1冊目の在庫更新もロールバックされる（分散トランザクションの課題）

---

## 3. OrderResource 単体テスト

### 3.1 テスト対象メソッド

* `Response createOrder(@Valid OrderRequest request)`
* `Response getOrderHistory()`
* `Response getOrderById(@PathParam("orderTranId") Integer orderTranId)`
* `Response getOrderDetail(@PathParam("orderTranId") Integer orderTranId, @PathParam("orderDetailId") Integer orderDetailId)`

### 3.2 テストケース

#### TC_ORDER_RESOURCE_001: createOrder() - 正常系 - 注文作成成功

* **Given（前提条件）**:
  * AuthenContext.isAuthenticated(): true
  * AuthenContext.getCustomerId(): 1
  * OrderRequest:
    * cartItems: 1件（bookId=1, bookName="Java完全理解", publisherName="技術評論社", price=3200, count=2, version=0）
    * totalPrice: 6400
    * deliveryPrice: 800
    * deliveryAddress: "東京都渋谷区1-2-3"
    * settlementType: 1
  * OrderService.orderBooks(request, 1):
    * 戻り値: OrderTran（orderTranId=1、その他フィールド設定済み）

* **When（操作）**:
  * `orderResource.createOrder(request)` を呼び出す

* **Then（期待結果）**:
  * Response.status: 200 OK
  * Response.entity: OrderResponse（orderTranId=1、その他フィールド）
  * AuthenContext.isAuthenticated()が1回呼ばれる
  * AuthenContext.getCustomerId()が1回呼ばれる
  * OrderService.orderBooks(request, 1)が1回呼ばれる

#### TC_ORDER_RESOURCE_E001: createOrder() - 異常系 - 未認証

* **Given（前提条件）**:
  * AuthenContext.isAuthenticated(): false

* **When（操作）**:
  * `orderResource.createOrder(request)` を呼び出す

* **Then（期待結果）**:
  * Response.status: 401 Unauthorized
  * AuthenContext.isAuthenticated()が1回呼ばれる
  * AuthenContext.getCustomerId()は呼ばれない
  * OrderService.orderBooks()は呼ばれない

#### TC_ORDER_RESOURCE_E002: createOrder() - 異常系 - 在庫不足

* **Given（前提条件）**:
  * AuthenContext.isAuthenticated(): true
  * AuthenContext.getCustomerId(): 1
  * OrderService.orderBooks(request, 1):
    * OutOfStockExceptionをスロー（"在庫が不足しています: Java完全理解"）

* **When（操作）**:
  * `orderResource.createOrder(request)` を呼び出す

* **Then（期待結果）**:
  * OutOfStockExceptionがスローされる（OutOfStockExceptionMapperが409 Conflictに変換）
  * AuthenContext.isAuthenticated()が1回呼ばれる
  * AuthenContext.getCustomerId()が1回呼ばれる
  * OrderService.orderBooks(request, 1)が1回呼ばれる

#### TC_ORDER_RESOURCE_002: getOrderHistory() - 正常系 - 注文履歴取得

* **Given（前提条件）**:
  * AuthenContext.isAuthenticated(): true
  * AuthenContext.getCustomerId(): 1
  * OrderTranDao.findByCustomerId(1):
    * 戻り値: List<OrderTran>（1件）
      * OrderTran（orderTranId=1、customerId=1、orderDate=2026-01-18、その他フィールド）
  * OrderDetailDao.findByOrderTranId(1):
    * 戻り値: List<OrderDetail>（2件）
      * OrderDetail（orderDetailId=1、bookId=1、bookName="Java完全理解"、その他フィールド）
      * OrderDetail（orderDetailId=2、bookId=2、bookName="Python入門"、その他フィールド）

* **When（操作）**:
  * `orderResource.getOrderHistory()` を呼び出す

* **Then（期待結果）**:
  * Response.status: 200 OK
  * Response.entity: List<OrderHistoryTO>（2件）
    * 1件目: 注文1の明細1
    * 2件目: 注文1の明細2
  * AuthenContext.isAuthenticated()が1回呼ばれる
  * AuthenContext.getCustomerId()が1回呼ばれる
  * OrderTranDao.findByCustomerId(1)が1回呼ばれる
  * OrderDetailDao.findByOrderTranId(1)が1回呼ばれる

#### TC_ORDER_RESOURCE_E003: getOrderHistory() - 異常系 - 未認証

* **Given（前提条件）**:
  * AuthenContext.isAuthenticated(): false

* **When（操作）**:
  * `orderResource.getOrderHistory()` を呼び出す

* **Then（期待結果）**:
  * Response.status: 401 Unauthorized
  * AuthenContext.isAuthenticated()が1回呼ばれる
  * AuthenContext.getCustomerId()は呼ばれない
  * OrderTranDao.findByCustomerId()は呼ばれない

#### TC_ORDER_RESOURCE_003: getOrderHistory() - 正常系 - 注文履歴0件

* **Given（前提条件）**:
  * AuthenContext.isAuthenticated(): true
  * AuthenContext.getCustomerId(): 1
  * OrderTranDao.findByCustomerId(1):
    * 戻り値: 空リスト

* **When（操作）**:
  * `orderResource.getOrderHistory()` を呼び出す

* **Then（期待結果）**:
  * Response.status: 200 OK
  * Response.entity: 空リスト
  * AuthenContext.isAuthenticated()が1回呼ばれる
  * AuthenContext.getCustomerId()が1回呼ばれる
  * OrderTranDao.findByCustomerId(1)が1回呼ばれる

#### TC_ORDER_RESOURCE_004: getOrderById() - 正常系 - 注文詳細取得

* **Given（前提条件）**:
  * orderTranId = 1
  * OrderTranDao.findById(1):
    * 戻り値: OrderTran（orderTranId=1、その他フィールド）
  * OrderDetailDao.findByOrderTranId(1):
    * 戻り値: List<OrderDetail>（2件）

* **When（操作）**:
  * `orderResource.getOrderById(1)` を呼び出す

* **Then（期待結果）**:
  * Response.status: 200 OK
  * Response.entity: OrderResponse（orderTranId=1、orderDetails含む）
  * OrderTranDao.findById(1)が1回呼ばれる
  * OrderDetailDao.findByOrderTranId(1)が1回呼ばれる

#### TC_ORDER_RESOURCE_E004: getOrderById() - 異常系 - 注文が見つからない

* **Given（前提条件）**:
  * orderTranId = 999
  * OrderTranDao.findById(999):
    * 戻り値: null

* **When（操作）**:
  * `orderResource.getOrderById(999)` を呼び出す

* **Then（期待結果）**:
  * Response.status: 404 Not Found
  * OrderTranDao.findById(999)が1回呼ばれる
  * OrderDetailDao.findByOrderTranId()は呼ばれない

#### TC_ORDER_RESOURCE_005: getOrderDetail() - 正常系 - 注文明細取得

* **Given（前提条件）**:
  * orderTranId = 1
  * orderDetailId = 1
  * OrderDetailDao.findById(OrderDetailPK(1, 1)):
    * 戻り値: OrderDetail（orderDetailId=1、bookId=1、bookName="Java完全理解"、その他フィールド）

* **When（操作）**:
  * `orderResource.getOrderDetail(1, 1)` を呼び出す

* **Then（期待結果）**:
  * Response.status: 200 OK
  * Response.entity: OrderDetailTO（orderDetailId=1、bookId=1、bookName="Java完全理解"、その他フィールド）
  * OrderDetailDao.findById(OrderDetailPK(1, 1))が1回呼ばれる

#### TC_ORDER_RESOURCE_E005: getOrderDetail() - 異常系 - 注文明細が見つからない

* **Given（前提条件）**:
  * orderTranId = 1
  * orderDetailId = 999
  * OrderDetailDao.findById(OrderDetailPK(1, 999)):
    * 戻り値: null

* **When（操作）**:
  * `orderResource.getOrderDetail(1, 999)` を呼び出す

* **Then（期待結果）**:
  * Response.status: 404 Not Found
  * OrderDetailDao.findById(OrderDetailPK(1, 999))が1回呼ばれる

---

## 4. Bean Validation テスト

### 4.1 CartItemRequest バリデーション

#### TC_VALIDATION_001: bookId が null

* **Given**: CartItemRequest（bookId=null、その他フィールド正常）
* **When**: @Validアノテーション検証
* **Then**: ConstraintViolationException（"書籍IDは必須です"）

#### TC_VALIDATION_002: bookName が空文字

* **Given**: CartItemRequest（bookName=""、その他フィールド正常）
* **When**: @Validアノテーション検証
* **Then**: ConstraintViolationException（"書籍名は必須です"）

#### TC_VALIDATION_003: price が負の数

* **Given**: CartItemRequest（price=-1、その他フィールド正常）
* **When**: @Validアノテーション検証
* **Then**: ConstraintViolationException（"価格は正の数である必要があります"）

#### TC_VALIDATION_004: count が 0

* **Given**: CartItemRequest（count=0、その他フィールド正常）
* **When**: @Validアノテーション検証
* **Then**: ConstraintViolationException（"注文数は1以上である必要があります"）

### 4.2 OrderRequest バリデーション

#### TC_VALIDATION_005: cartItems が null

* **Given**: OrderRequest（cartItems=null、その他フィールド正常）
* **When**: @Validアノテーション検証
* **Then**: ConstraintViolationException（"カート項目は必須です"）

#### TC_VALIDATION_006: cartItems が空リスト

* **Given**: OrderRequest（cartItems=[]、その他フィールド正常）
* **When**: @Validアノテーション検証
* **Then**: ConstraintViolationException（"カート項目は空にできません"）

#### TC_VALIDATION_007: totalPrice が負の数

* **Given**: OrderRequest（totalPrice=-1、その他フィールド正常）
* **When**: @Validアノテーション検証
* **Then**: ConstraintViolationException（"合計金額は正の数である必要があります"）

#### TC_VALIDATION_008: deliveryAddress が空文字

* **Given**: OrderRequest（deliveryAddress=""、その他フィールド正常）
* **When**: @Validアノテーション検証
* **Then**: ConstraintViolationException（"配送先住所は必須です"）

---

## 5. テスト実装ガイドライン

### 5.1 モック化対象

* OrderService単体テスト:
  * OrderTranDao（@Mock）
  * OrderDetailDao（@Mock）
  * BackOfficeRestClient（@Mock）

* OrderResource単体テスト:
  * OrderService（@Mock）
  * AuthenContext（@Mock）
  * OrderTranDao（@Mock）
  * OrderDetailDao（@Mock）

### 5.2 テストクラス構造

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderTranDao orderTranDao;
    
    @Mock
    private OrderDetailDao orderDetailDao;
    
    @Mock
    private BackOfficeRestClient backOfficeClient;
    
    @InjectMocks
    private OrderService orderService;
    
    @Test
    void testOrderBooks_Success() {
        // Given
        // ...
        
        // When
        OrderTran result = orderService.orderBooks(request, customerId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getOrderTranId());
        verify(backOfficeClient, times(1)).findStockById(1);
        verify(backOfficeClient, times(1)).updateStock(1, 0L, 8);
        verify(orderTranDao, times(1)).insert(any(OrderTran.class));
        verify(orderDetailDao, times(1)).insert(any(OrderDetail.class));
    }
}
```

### 5.3 アサーション戦略

* 戻り値の検証: assertNotNull、assertEquals、assertTrue等
* モックメソッド呼び出し検証: verify、times
* 例外検証: assertThrows

---

## 6. 参考資料

* [detailed_design.md](detailed_design.md) - API_003_orders詳細設計書
* [common/detailed_design.md](../common/detailed_design.md) - 共通機能詳細設計書
* [tasks/API_003_orders.md](../../../tasks/API_003_orders.md) - 注文APIタスクリスト
