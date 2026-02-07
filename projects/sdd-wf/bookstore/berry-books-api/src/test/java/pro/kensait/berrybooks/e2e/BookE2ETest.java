package pro.kensait.berrybooks.e2e;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * 書籍API（/api/books）のE2Eテスト
 * 
 * テスト対象:
 * - 書籍一覧取得（GET /api/books）
 * - 書籍詳細取得（GET /api/books/{id}）
 * - 書籍検索（GET /api/books/search）
 * - カテゴリ一覧取得（GET /api/books/categories）
 * 
 * テストシナリオは requirements/behaviors.md に基づく
 */
@Tag("e2e")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookE2ETest extends BaseE2ETest {
    
    // テスト用の書籍ID（実際のDBに存在するID）
    private static final Long EXISTING_BOOK_ID = 1L;
    private static final Long NON_EXISTING_BOOK_ID = 999L;
    
    // テスト用のカテゴリID
    private static final Long EXISTING_CATEGORY_ID = 1L;
    
    // =========================================================================
    // 3.1 書籍一覧取得（GET /api/books）
    // =========================================================================
    
    /**
     * シナリオID: BOOK-LIST-001
     * 全書籍を取得できる
     */
    @Test
    @Order(1)
    void testGetAllBooks_Success() {
        // When: /api/booksにリクエスト
        Response response = given(requestSpec)
                .when()
                .get("/api/books");
        
        // Then: 200 OK、全書籍が返される、カテゴリ、出版社、在庫情報を含む
        response.then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("[0].bookId", notNullValue())
                .body("[0].title", notNullValue())
                .body("[0].categoryName", notNullValue())
                .body("[0].publisherName", notNullValue())
                .body("[0].price", notNullValue())
                .body("[0].quantity", notNullValue())
                .body("[0].version", notNullValue());
    }
    
    // =========================================================================
    // 3.2 書籍詳細取得（GET /api/books/{id}）
    // =========================================================================
    
    /**
     * シナリオID: BOOK-DETAIL-001
     * 指定IDの書籍を取得できる
     */
    @Test
    @Order(10)
    void testGetBookById_Success() {
        // When: /api/books/1にリクエスト
        Response response = given(requestSpec)
                .when()
                .get("/api/books/" + EXISTING_BOOK_ID);
        
        // Then: 200 OK、書籍ID=1の情報が返される
        response.then()
                .statusCode(200)
                .body("bookId", equalTo(EXISTING_BOOK_ID.intValue()))
                .body("title", notNullValue())
                .body("categoryName", notNullValue())
                .body("publisherName", notNullValue())
                .body("price", notNullValue())
                .body("quantity", notNullValue())
                .body("version", notNullValue());
    }
    
    /**
     * シナリオID: BOOK-DETAIL-E001
     * 指定IDの書籍が存在しない場合、エラー
     */
    @Test
    @Order(11)
    void testGetBookById_NotFound() {
        // When: /api/books/999にリクエスト（存在しないID）
        Response response = given(requestSpec)
                .when()
                .get("/api/books/" + NON_EXISTING_BOOK_ID);
        
        // Then: 404 Not Found
        response.then()
                .statusCode(404)
                .body("message", containsString("書籍が見つかりません"));
    }
    
    // =========================================================================
    // 3.3 書籍検索（GET /api/books/search）
    // =========================================================================
    
    /**
     * シナリオID: BOOK-SEARCH-001
     * カテゴリIDで検索できる
     */
    @Test
    @Order(20)
    void testSearchBooks_ByCategoryId() {
        // When: categoryId=1で検索リクエスト
        Response response = given(requestSpec)
                .queryParam("categoryId", EXISTING_CATEGORY_ID)
                .when()
                .get("/api/books/search");
        
        // Then: 200 OK、カテゴリID=1の書籍が返される
        response.then()
                .statusCode(200)
                .body("$", not(empty()));
    }
    
    /**
     * シナリオID: BOOK-SEARCH-002
     * キーワードで検索できる
     */
    @Test
    @Order(21)
    void testSearchBooks_ByKeyword() {
        // When: keyword="Java"で検索リクエスト
        Response response = given(requestSpec)
                .queryParam("keyword", "Java")
                .when()
                .get("/api/books/search");
        
        // Then: 200 OK、書籍名または著者名に"Java"を含む書籍が返される
        response.then()
                .statusCode(200);
        // 注意: 書籍データによっては空の配列が返る可能性がある
    }
    
    /**
     * シナリオID: BOOK-SEARCH-003
     * カテゴリID+キーワードで検索できる
     */
    @Test
    @Order(22)
    void testSearchBooks_ByCategoryIdAndKeyword() {
        // When: categoryId=1&keyword="Java"で検索
        Response response = given(requestSpec)
                .queryParam("categoryId", EXISTING_CATEGORY_ID)
                .queryParam("keyword", "Java")
                .when()
                .get("/api/books/search");
        
        // Then: 200 OK、カテゴリID=1かつ"Java"を含む書籍が返される
        response.then()
                .statusCode(200);
    }
    
    /**
     * シナリオID: BOOK-SEARCH-004
     * categoryId=0は全カテゴリを検索する
     */
    @Test
    @Order(23)
    void testSearchBooks_CategoryIdZero() {
        // When: categoryId=0&keyword="Java"で検索
        Response response = given(requestSpec)
                .queryParam("categoryId", 0)
                .queryParam("keyword", "Java")
                .when()
                .get("/api/books/search");
        
        // Then: 200 OK、全カテゴリから"Java"を含む書籍が返される
        response.then()
                .statusCode(200);
    }
    
    /**
     * シナリオID: BOOK-SEARCH-005
     * パラメータ未指定で全書籍を返す
     */
    @Test
    @Order(24)
    void testSearchBooks_NoParameters() {
        // When: パラメータなしで検索リクエスト
        Response response = given(requestSpec)
                .when()
                .get("/api/books/search");
        
        // Then: 200 OK、全書籍が返される
        response.then()
                .statusCode(200)
                .body("$", not(empty()));
    }
    
    /**
     * シナリオID: BOOK-SEARCH-006
     * 検索結果が0件の場合、空配列を返す
     */
    @Test
    @Order(25)
    void testSearchBooks_NoResults() {
        // When: keyword="存在しないキーワード"で検索
        Response response = given(requestSpec)
                .queryParam("keyword", "存在しないキーワードXYZ123")
                .when()
                .get("/api/books/search");
        
        // Then: 200 OK、空配列が返される
        response.then()
                .statusCode(200)
                .body("$", empty());
    }
    
    // =========================================================================
    // 3.4 カテゴリ一覧取得（GET /api/books/categories）
    // =========================================================================
    
    /**
     * シナリオID: BOOK-CAT-001
     * 全カテゴリをMapで取得できる
     */
    @Test
    @Order(30)
    void testGetCategories_Success() {
        // When: /api/books/categoriesにリクエスト
        Response response = given(requestSpec)
                .when()
                .get("/api/books/categories");
        
        // Then: 200 OK、カテゴリMapが返される（例: {"Java": 1, "JavaScript": 2, ...}）
        response.then()
                .statusCode(200)
                .body("$", not(empty()));
    }
}
