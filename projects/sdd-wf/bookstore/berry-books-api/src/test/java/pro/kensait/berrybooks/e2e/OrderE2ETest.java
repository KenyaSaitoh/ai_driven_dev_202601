package pro.kensait.berrybooks.e2e;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * 注文API（/api/orders）のE2Eテスト
 * 
 * テスト対象:
 * - 注文作成（POST /api/orders）
 * - 注文履歴取得（GET /api/orders/history）
 * - 注文詳細取得（GET /api/orders/{tranId}）
 * - 注文明細取得（GET /api/orders/{tranId}/details/{detailId}）
 * 
 * テストシナリオは requirements/behaviors.md に基づく
 */
@Tag("e2e")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderE2ETest extends BaseE2ETest {
    
    // テスト用の顧客情報
    private static final String TEST_EMAIL = "alice@gmail.com";
    private static final String TEST_PASSWORD = "password";
    
    // テスト用の書籍ID（実際のDBに存在するID、在庫あり）
    private static final Long BOOK_ID_WITH_STOCK = 1L;
    
    // 注文IDを保存（後続のテストで使用）
    private static Long createdOrderTranId;
    private static Long createdOrderDetailId;
    
    // =========================================================================
    // 4.1 注文作成（POST /api/orders）
    // =========================================================================
    
    /**
     * シナリオID: ORDER-CREATE-001
     * 注文を作成できる
     */
    @Test
    @Order(1)
    void testCreateOrder_Success() {
        // Given: ログイン済み、在庫あり
        String jwtToken = loginAndGetCookie(TEST_EMAIL, TEST_PASSWORD);
        
        // 在庫情報を取得
        Response stockResponse = given(requestSpec)
                .when()
                .get("/api/books/" + BOOK_ID_WITH_STOCK);
        
        int currentStock = stockResponse.jsonPath().getInt("quantity");
        int version = stockResponse.jsonPath().getInt("version");
        
        System.out.println("Current stock before order: " + currentStock);
        
        // When: カート情報で注文リクエスト
        Response response = given(requestSpec)
                .cookie("jwtToken", jwtToken)
                .body(Map.of(
                        "cartItems", List.of(
                                Map.of(
                                        "bookId", BOOK_ID_WITH_STOCK,
                                        "quantity", 1,
                                        "version", version
                                )
                        )
                ))
                .when()
                .post("/api/orders");
        
        // Then: 200 OK、注文レコードが作成される、OrderResponseが返される
        response.then()
                .statusCode(200)
                .body("orderTranId", notNullValue())
                .body("orderDate", notNullValue())
                .body("totalAmount", notNullValue())
                .body("deliveryFee", notNullValue())
                .body("orderDetails", not(empty()))
                .body("orderDetails[0].orderDetailId", notNullValue())
                .body("orderDetails[0].bookName", notNullValue())
                .body("orderDetails[0].quantity", equalTo(1));
        
        // 注文IDを保存（後続のテストで使用）
        createdOrderTranId = response.jsonPath().getLong("orderTranId");
        createdOrderDetailId = response.jsonPath().getLong("orderDetails[0].orderDetailId");
        
        System.out.println("Created order: orderTranId=" + createdOrderTranId + ", orderDetailId=" + createdOrderDetailId);
    }
    
    /**
     * シナリオID: ORDER-CREATE-002
     * 複数書籍を同時に注文できる
     */
    @Test
    @Order(2)
    void testCreateOrder_MultipleBooks() {
        // Given: ログイン済み、全書籍の在庫あり
        String jwtToken = loginAndGetCookie(TEST_EMAIL, TEST_PASSWORD);
        
        // 2冊の書籍の在庫情報を取得
        Response stock1 = given(requestSpec).get("/api/books/1");
        Response stock2 = given(requestSpec).get("/api/books/2");
        
        int version1 = stock1.jsonPath().getInt("version");
        int version2 = stock2.jsonPath().getInt("version");
        
        // When: 複数カート項目で注文リクエスト
        Response response = given(requestSpec)
                .cookie("jwtToken", jwtToken)
                .body(Map.of(
                        "cartItems", List.of(
                                Map.of("bookId", 1L, "quantity", 1, "version", version1),
                                Map.of("bookId", 2L, "quantity", 1, "version", version2)
                        )
                ))
                .when()
                .post("/api/orders");
        
        // Then: 200 OK、複数の注文明細が作成される
        response.then()
                .statusCode(200)
                .body("orderDetails", hasSize(2));
    }
    
    /**
     * シナリオID: ORDER-CREATE-E001
     * 未ログインの場合、エラー
     */
    @Test
    @Order(3)
    void testCreateOrder_Unauthorized() {
        // Given: JWT Cookie未設定
        
        // When: 注文リクエスト
        Response response = given(requestSpec)
                .body(Map.of(
                        "cartItems", List.of(
                                Map.of("bookId", BOOK_ID_WITH_STOCK, "quantity", 1, "version", 1)
                        )
                ))
                .when()
                .post("/api/orders");
        
        // Then: 401 Unauthorized
        response.then()
                .statusCode(401)
                .body("message", containsString("認証が必要です"));
    }
    
    /**
     * シナリオID: ORDER-CREATE-E002
     * 在庫不足の場合、エラー
     * 
     * 注意: このテストは実際のDBの在庫数に依存する
     * 在庫が十分にある場合は、大量の注文数でテストする必要がある
     */
    @Test
    @Order(4)
    void testCreateOrder_OutOfStock() {
        // Given: ログイン済み、在庫数を取得
        String jwtToken = loginAndGetCookie(TEST_EMAIL, TEST_PASSWORD);
        
        Response stockResponse = given(requestSpec)
                .when()
                .get("/api/books/" + BOOK_ID_WITH_STOCK);
        
        int currentStock = stockResponse.jsonPath().getInt("quantity");
        int version = stockResponse.jsonPath().getInt("version");
        
        // When: 在庫を超える数量で注文リクエスト
        Response response = given(requestSpec)
                .cookie("jwtToken", jwtToken)
                .body(Map.of(
                        "cartItems", List.of(
                                Map.of(
                                        "bookId", BOOK_ID_WITH_STOCK,
                                        "quantity", currentStock + 100,  // 在庫を超える数量
                                        "version", version
                                )
                        )
                ))
                .when()
                .post("/api/orders");
        
        // Then: 409 Conflict
        response.then()
                .statusCode(409)
                .body("message", containsString("在庫が不足しています"));
    }
    
    /**
     * シナリオID: ORDER-CREATE-E005
     * カート項目が空の場合、エラー
     */
    @Test
    @Order(5)
    void testCreateOrder_EmptyCart() {
        // Given: ログイン済み
        String jwtToken = loginAndGetCookie(TEST_EMAIL, TEST_PASSWORD);
        
        // When: cartItems=[]で注文リクエスト
        Response response = given(requestSpec)
                .cookie("jwtToken", jwtToken)
                .body(Map.of("cartItems", List.of()))
                .when()
                .post("/api/orders");
        
        // Then: 400 Bad Request（Bean Validation）
        response.then()
                .statusCode(400);
    }
    
    // =========================================================================
    // 4.2 注文履歴取得（GET /api/orders/history）
    // =========================================================================
    
    /**
     * シナリオID: ORDER-HIST-001
     * ログイン中の顧客の注文履歴を取得できる
     */
    @Test
    @Order(10)
    void testGetOrderHistory_Success() {
        // Given: ログイン済み、注文履歴あり（testCreateOrder_Successで作成）
        String jwtToken = loginAndGetCookie(TEST_EMAIL, TEST_PASSWORD);
        
        // When: /api/orders/historyにリクエスト
        Response response = given(requestSpec)
                .cookie("jwtToken", jwtToken)
                .when()
                .get("/api/orders/history");
        
        // Then: 200 OK、注文履歴が返される、注文日降順でソート
        response.then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("[0].orderTranId", notNullValue())
                .body("[0].orderDate", notNullValue())
                .body("[0].bookName", notNullValue())
                .body("[0].quantity", notNullValue())
                .body("[0].price", notNullValue());
    }
    
    /**
     * シナリオID: ORDER-HIST-E001
     * 未ログインの場合、エラー
     */
    @Test
    @Order(11)
    void testGetOrderHistory_Unauthorized() {
        // Given: JWT Cookie未設定
        
        // When: /api/orders/historyにリクエスト
        Response response = given(requestSpec)
                .when()
                .get("/api/orders/history");
        
        // Then: 401 Unauthorized
        response.then()
                .statusCode(401)
                .body("message", containsString("認証が必要です"));
    }
    
    // =========================================================================
    // 4.3 注文詳細取得（GET /api/orders/{tranId}）
    // =========================================================================
    
    /**
     * シナリオID: ORDER-DETAIL-001
     * 指定IDの注文詳細を取得できる
     */
    @Test
    @Order(20)
    void testGetOrderDetail_Success() {
        // Given: 注文ID=createdOrderTranIdが存在する（testCreateOrder_Successで作成）
        Assumptions.assumeTrue(createdOrderTranId != null, "Order must be created first");
        
        // When: /api/orders/{tranId}にリクエスト
        Response response = given(requestSpec)
                .when()
                .get("/api/orders/" + createdOrderTranId);
        
        // Then: 200 OK、注文詳細が返される、注文明細リスト（orderDetails）を含む
        response.then()
                .statusCode(200)
                .body("orderTranId", equalTo(createdOrderTranId.intValue()))
                .body("orderDate", notNullValue())
                .body("totalAmount", notNullValue())
                .body("orderDetails", not(empty()));
    }
    
    /**
     * シナリオID: ORDER-DETAIL-E001
     * 指定IDの注文が存在しない場合、エラー
     */
    @Test
    @Order(21)
    void testGetOrderDetail_NotFound() {
        // When: /api/orders/999にリクエスト（存在しないID）
        Response response = given(requestSpec)
                .when()
                .get("/api/orders/999999");
        
        // Then: 404 Not Found
        response.then()
                .statusCode(404)
                .body("message", containsString("注文が見つかりません"));
    }
    
    // =========================================================================
    // 4.4 注文明細取得（GET /api/orders/{tranId}/details/{detailId}）
    // =========================================================================
    
    /**
     * シナリオID: ORDER-DETAIL-ITEM-001
     * 指定IDの注文明細を取得できる
     */
    @Test
    @Order(30)
    void testGetOrderDetailItem_Success() {
        // Given: 注文ID=createdOrderTranId, 明細ID=createdOrderDetailIdが存在する
        Assumptions.assumeTrue(createdOrderTranId != null && createdOrderDetailId != null, 
                "Order and detail must be created first");
        
        // When: /api/orders/{tranId}/details/{detailId}にリクエスト
        Response response = given(requestSpec)
                .when()
                .get("/api/orders/" + createdOrderTranId + "/details/" + createdOrderDetailId);
        
        // Then: 200 OK、注文明細が返される
        response.then()
                .statusCode(200)
                .body("orderDetailId", equalTo(createdOrderDetailId.intValue()))
                .body("orderTranId", equalTo(createdOrderTranId.intValue()))
                .body("bookName", notNullValue())
                .body("quantity", notNullValue())
                .body("price", notNullValue());
    }
    
    /**
     * シナリオID: ORDER-DETAIL-ITEM-E001
     * 指定IDの注文明細が存在しない場合、エラー
     */
    @Test
    @Order(31)
    void testGetOrderDetailItem_NotFound() {
        // Given: 注文ID=createdOrderTranIdは存在する
        Assumptions.assumeTrue(createdOrderTranId != null, "Order must be created first");
        
        // When: /api/orders/{tranId}/details/999にリクエスト（存在しない明細ID）
        Response response = given(requestSpec)
                .when()
                .get("/api/orders/" + createdOrderTranId + "/details/999999");
        
        // Then: 404 Not Found
        response.then()
                .statusCode(404)
                .body("message", containsString("注文明細が見つかりません"));
    }
}
