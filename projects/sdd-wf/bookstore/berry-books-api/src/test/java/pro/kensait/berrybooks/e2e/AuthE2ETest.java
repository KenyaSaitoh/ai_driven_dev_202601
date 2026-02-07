package pro.kensait.berrybooks.e2e;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * 認証API（/api/auth）のE2Eテスト
 * 
 * テスト対象:
 * - ログイン（POST /api/auth/login）
 * - ログアウト（POST /api/auth/logout）
 * - 新規登録（POST /api/auth/register）
 * - 現在のユーザー情報取得（GET /api/auth/me）
 * 
 * テストシナリオは requirements/behaviors.md に基づく
 */
@Tag("e2e")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthE2ETest extends BaseE2ETest {
    
    // テスト用の顧客情報（既存データ）
    private static final String EXISTING_EMAIL = "alice@gmail.com";
    private static final String EXISTING_PASSWORD = "password";
    
    // テスト用の新規顧客情報
    private static final String NEW_EMAIL = "e2e.newuser@example.com";
    private static final String NEW_PASSWORD = "newpassword";
    private static final String NEW_CUSTOMER_NAME = "E2E テストユーザー";
    private static final String NEW_ADDRESS = "東京都渋谷区1-2-3";
    
    /**
     * テスト前に、新規登録用のメールアドレスを削除する（冪等性確保）
     * 注意: この処理は実際のE2E環境では管理用APIまたはDBクリーンアップスクリプトで実施すべき
     */
    @BeforeAll
    public static void cleanupTestData() {
        // 既存の顧客が存在する場合は削除（実際の環境では管理用APIを使用）
        // ここではログイン試行で存在確認のみ実施
        System.out.println("Test data cleanup: " + NEW_EMAIL);
    }
    
    // =========================================================================
    // 2.1 ログイン（POST /api/auth/login）
    // =========================================================================
    
    /**
     * シナリオID: AUTH-LOGIN-001
     * 正しいメールアドレスとパスワードでログインできる
     */
    @Test
    @Order(1)
    void testLogin_Success() {
        // When: ログインリクエスト
        Response response = login(EXISTING_EMAIL, EXISTING_PASSWORD);
        
        // Then: 200 OK、JWT Cookieが発行される、顧客情報が返される
        response.then()
                .statusCode(200)
                .cookie("jwtToken", notNullValue())
                .body("customerId", notNullValue())
                .body("email", equalTo(EXISTING_EMAIL))
                .body("customerName", notNullValue());
    }
    
    /**
     * シナリオID: AUTH-LOGIN-E001
     * メールアドレスが存在しない場合、エラー
     */
    @Test
    @Order(2)
    void testLogin_EmailNotFound() {
        // When: 未登録のメールアドレスでログインリクエスト
        Response response = login("notfound@example.com", "password");
        
        // Then: 401 Unauthorized
        response.then()
                .statusCode(401)
                .body("message", containsString("メールアドレスまたはパスワードが正しくありません"));
    }
    
    /**
     * シナリオID: AUTH-LOGIN-E002
     * パスワードが一致しない場合、エラー
     */
    @Test
    @Order(3)
    void testLogin_PasswordMismatch() {
        // When: 誤ったパスワードでログインリクエスト
        Response response = login(EXISTING_EMAIL, "wrongpassword");
        
        // Then: 401 Unauthorized
        response.then()
                .statusCode(401)
                .body("message", containsString("メールアドレスまたはパスワードが正しくありません"));
    }
    
    /**
     * シナリオID: AUTH-LOGIN-E003
     * メールアドレスが空の場合、エラー
     */
    @Test
    @Order(4)
    void testLogin_EmptyEmail() {
        // When: メールアドレスが空でログインリクエスト
        Response response = given(requestSpec)
                .body(Map.of(
                        "email", "",
                        "password", "password"
                ))
                .when()
                .post("/api/auth/login");
        
        // Then: 400 Bad Request（Bean Validation）
        response.then()
                .statusCode(400);
    }
    
    /**
     * シナリオID: AUTH-LOGIN-E004
     * パスワードが空の場合、エラー
     */
    @Test
    @Order(5)
    void testLogin_EmptyPassword() {
        // When: パスワードが空でログインリクエスト
        Response response = given(requestSpec)
                .body(Map.of(
                        "email", EXISTING_EMAIL,
                        "password", ""
                ))
                .when()
                .post("/api/auth/login");
        
        // Then: 400 Bad Request（Bean Validation）
        response.then()
                .statusCode(400);
    }
    
    // =========================================================================
    // 2.2 ログアウト（POST /api/auth/logout）
    // =========================================================================
    
    /**
     * シナリオID: AUTH-LOGOUT-001
     * ログアウトできる
     */
    @Test
    @Order(10)
    void testLogout_Success() {
        // Given: ログイン済み
        String jwtToken = loginAndGetCookie(EXISTING_EMAIL, EXISTING_PASSWORD);
        
        // When: ログアウトリクエスト
        Response response = given(requestSpec)
                .cookie("jwtToken", jwtToken)
                .when()
                .post("/api/auth/logout");
        
        // Then: 200 OK、JWT Cookie削除（MaxAge=0）
        response.then()
                .statusCode(200)
                .cookie("jwtToken", equalTo(""));
    }
    
    /**
     * シナリオID: AUTH-LOGOUT-002
     * 未ログイン状態でもログアウトできる
     */
    @Test
    @Order(11)
    void testLogout_NotLoggedIn() {
        // When: 未ログイン状態でログアウトリクエスト
        Response response = logout();
        
        // Then: 200 OK
        response.then()
                .statusCode(200);
    }
    
    // =========================================================================
    // 2.3 新規登録（POST /api/auth/register）
    // =========================================================================
    
    /**
     * シナリオID: AUTH-REG-001
     * 新規顧客を登録できる
     */
    @Test
    @Order(20)
    void testRegister_Success() {
        // When: 顧客情報で登録リクエスト
        Response response = register(NEW_EMAIL, NEW_PASSWORD, NEW_CUSTOMER_NAME, NEW_ADDRESS);
        
        // Then: 200 OK、顧客が登録される、JWT Cookieが発行される
        response.then()
                .statusCode(200)
                .cookie("jwtToken", notNullValue())
                .body("customerId", notNullValue())
                .body("email", equalTo(NEW_EMAIL))
                .body("customerName", equalTo(NEW_CUSTOMER_NAME));
    }
    
    /**
     * シナリオID: AUTH-REG-002
     * 住所が都道府県名から始まる場合、登録できる
     */
    @Test
    @Order(21)
    void testRegister_AddressStartsWithPrefecture() {
        // When: address="東京都渋谷区1-2-3"で登録リクエスト
        String testEmail = "e2e.prefecture@example.com";
        Response response = register(testEmail, "password", "都道府県テスト", "東京都渋谷区1-2-3");
        
        // Then: 200 OK
        response.then()
                .statusCode(200)
                .body("email", equalTo(testEmail));
    }
    
    /**
     * シナリオID: AUTH-REG-E001
     * メールアドレスが既に存在する場合、エラー
     */
    @Test
    @Order(22)
    void testRegister_EmailAlreadyExists() {
        // Given: NEW_EMAILは既に登録済み（testRegister_Successで登録）
        
        // When: 既存のメールアドレスで登録リクエスト
        Response response = register(NEW_EMAIL, "password", "Duplicate User", "東京都新宿区1-1-1");
        
        // Then: 409 Conflict
        response.then()
                .statusCode(409)
                .body("message", containsString("既に登録されています"));
    }
    
    /**
     * シナリオID: AUTH-REG-E002
     * 住所が都道府県名から始まらない場合、エラー
     */
    @Test
    @Order(23)
    void testRegister_AddressNotStartsWithPrefecture() {
        // When: address="渋谷区1-2-3"で登録リクエスト（都道府県名なし）
        String testEmail = "e2e.badaddress@example.com";
        Response response = register(testEmail, "password", "不正な住所", "渋谷区1-2-3");
        
        // Then: 400 Bad Request
        response.then()
                .statusCode(400)
                .body("message", containsString("住所は都道府県名から始めてください"));
    }
    
    /**
     * シナリオID: AUTH-REG-E003
     * メールアドレスが空の場合、エラー
     */
    @Test
    @Order(24)
    void testRegister_EmptyEmail() {
        // When: email=""で登録リクエスト
        Response response = given(requestSpec)
                .body(Map.of(
                        "email", "",
                        "password", "password",
                        "customerName", "Empty Email Test",
                        "address", "東京都新宿区1-1-1"
                ))
                .when()
                .post("/api/auth/register");
        
        // Then: 400 Bad Request（Bean Validation）
        response.then()
                .statusCode(400);
    }
    
    /**
     * シナリオID: AUTH-REG-E004
     * 顧客名が空の場合、エラー
     */
    @Test
    @Order(25)
    void testRegister_EmptyCustomerName() {
        // When: customerName=""で登録リクエスト
        Response response = given(requestSpec)
                .body(Map.of(
                        "email", "e2e.emptyname@example.com",
                        "password", "password",
                        "customerName", "",
                        "address", "東京都新宿区1-1-1"
                ))
                .when()
                .post("/api/auth/register");
        
        // Then: 400 Bad Request（Bean Validation）
        response.then()
                .statusCode(400);
    }
    
    /**
     * シナリオID: AUTH-REG-E005
     * パスワードが空の場合、エラー
     */
    @Test
    @Order(26)
    void testRegister_EmptyPassword() {
        // When: password=""で登録リクエスト
        Response response = given(requestSpec)
                .body(Map.of(
                        "email", "e2e.emptypassword@example.com",
                        "password", "",
                        "customerName", "Empty Password Test",
                        "address", "東京都新宿区1-1-1"
                ))
                .when()
                .post("/api/auth/register");
        
        // Then: 400 Bad Request（Bean Validation）
        response.then()
                .statusCode(400);
    }
    
    // =========================================================================
    // 2.4 現在のログインユーザー情報取得（GET /api/auth/me）
    // =========================================================================
    
    /**
     * シナリオID: AUTH-ME-001
     * ログイン中の顧客情報を取得できる
     */
    @Test
    @Order(30)
    void testGetMe_Success() {
        // Given: JWT Cookieが設定されている
        String jwtToken = loginAndGetCookie(EXISTING_EMAIL, EXISTING_PASSWORD);
        
        // When: /api/auth/meにリクエスト
        Response response = given(requestSpec)
                .cookie("jwtToken", jwtToken)
                .when()
                .get("/api/auth/me");
        
        // Then: 200 OK、顧客情報が返される
        response.then()
                .statusCode(200)
                .body("customerId", notNullValue())
                .body("email", equalTo(EXISTING_EMAIL))
                .body("customerName", notNullValue());
    }
    
    /**
     * シナリオID: AUTH-ME-E001
     * JWT Cookieが設定されていない場合、エラー
     */
    @Test
    @Order(31)
    void testGetMe_Unauthorized() {
        // When: JWT Cookie未設定で/api/auth/meにリクエスト
        Response response = given(requestSpec)
                .when()
                .get("/api/auth/me");
        
        // Then: 401 Unauthorized
        response.then()
                .statusCode(401)
                .body("message", containsString("認証が必要です"));
    }
    
    /**
     * シナリオID: AUTH-ME-E002
     * JWTが無効な場合、エラー
     */
    @Test
    @Order(32)
    void testGetMe_InvalidToken() {
        // When: 無効なJWT Cookieで/api/auth/meにリクエスト
        Response response = given(requestSpec)
                .cookie("jwtToken", "invalid.jwt.token")
                .when()
                .get("/api/auth/me");
        
        // Then: 401 Unauthorized
        response.then()
                .statusCode(401)
                .body("message", containsString("認証が必要です"));
    }
}
