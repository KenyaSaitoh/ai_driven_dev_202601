package pro.kensait.berrybooks.e2e;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * 画像API（/api/images）のE2Eテスト
 * 
 * テスト対象:
 * - 書籍表紙画像取得（GET /api/images/covers/{bookId}）
 * 
 * テストシナリオは requirements/behaviors.md に基づく
 */
@Tag("e2e")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImageE2ETest extends BaseE2ETest {
    
    // テスト用の書籍ID（画像ファイルが存在するID）
    private static final Long BOOK_ID_WITH_IMAGE = 1L;
    
    // テスト用の書籍ID（画像ファイルが存在しないID）
    private static final Long BOOK_ID_WITHOUT_IMAGE = 999L;
    
    // =========================================================================
    // 5.1 書籍表紙画像取得（GET /api/images/covers/{bookId}）
    // =========================================================================
    
    /**
     * シナリオID: IMAGE-001
     * 書籍表紙画像を取得できる
     */
    @Test
    @Order(1)
    void testGetBookCoverImage_Success() {
        // When: /api/images/covers/1にリクエスト
        Response response = given(requestSpec)
                .when()
                .get("/api/images/covers/" + BOOK_ID_WITH_IMAGE);
        
        // Then: 200 OK、Content-Type: image/jpeg、画像バイナリが返される
        response.then()
                .statusCode(200)
                .contentType(anyOf(equalTo("image/jpeg"), equalTo("image/jpg")))
                .body(notNullValue());
        
        // 画像バイナリのサイズを確認（空でないこと）
        byte[] imageBytes = response.asByteArray();
        Assertions.assertTrue(imageBytes.length > 0, "Image should not be empty");
    }
    
    /**
     * シナリオID: IMAGE-002
     * 画像が存在しない場合、フォールバック画像を返す
     */
    @Test
    @Order(2)
    void testGetBookCoverImage_Fallback() {
        // When: /api/images/covers/999にリクエスト（画像ファイルが存在しない）
        Response response = given(requestSpec)
                .when()
                .get("/api/images/covers/" + BOOK_ID_WITHOUT_IMAGE);
        
        // Then: 200 OK、no-image.jpgが返される
        response.then()
                .statusCode(200)
                .contentType(anyOf(equalTo("image/jpeg"), equalTo("image/jpg")))
                .body(notNullValue());
        
        // フォールバック画像もバイナリが存在することを確認
        byte[] imageBytes = response.asByteArray();
        Assertions.assertTrue(imageBytes.length > 0, "Fallback image should not be empty");
    }
    
    /**
     * 追加テスト: 複数の書籍画像を取得できる
     */
    @Test
    @Order(3)
    void testGetBookCoverImage_MultipleBooks() {
        // When: 複数の書籍IDで画像を取得
        for (long bookId = 1; bookId <= 5; bookId++) {
            Response response = given(requestSpec)
                    .when()
                    .get("/api/images/covers/" + bookId);
            
            // Then: 200 OK
            response.then()
                    .statusCode(200)
                    .contentType(anyOf(equalTo("image/jpeg"), equalTo("image/jpg")));
        }
    }
    
    /**
     * 追加テスト: 画像APIは認証不要であることを確認
     */
    @Test
    @Order(4)
    void testGetBookCoverImage_NoAuthRequired() {
        // Given: JWT Cookie未設定（認証なし）
        
        // When: /api/images/covers/1にリクエスト
        Response response = given(requestSpec)
                .when()
                .get("/api/images/covers/" + BOOK_ID_WITH_IMAGE);
        
        // Then: 200 OK（認証不要のため）
        response.then()
                .statusCode(200)
                .contentType(anyOf(equalTo("image/jpeg"), equalTo("image/jpg")));
    }
}
