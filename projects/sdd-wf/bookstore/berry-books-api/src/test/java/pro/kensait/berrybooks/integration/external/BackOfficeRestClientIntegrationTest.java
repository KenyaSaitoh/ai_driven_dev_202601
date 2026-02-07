package pro.kensait.berrybooks.integration.external;

import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.external.dto.BookTO;
import pro.kensait.berrybooks.external.dto.StockTO;
import pro.kensait.berrybooks.integration.BaseIntegrationTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * BackOfficeRestClient 結合テスト
 * 
 * テスト対象: 外部API呼び出し（WireMockでスタブ化）
 * 
 * シナリオ:
 * * 書籍一覧取得
 * * 書籍詳細取得
 * * 在庫情報取得
 * * 在庫更新（楽観的ロック対応）
 */
class BackOfficeRestClientIntegrationTest extends BaseIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(BackOfficeRestClientIntegrationTest.class);
    
    private Client client;
    private String baseUrl;
    
    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        this.client = ClientBuilder.newClient();
        this.baseUrl = "http://localhost:8089/api";
        logger.info("[ BackOfficeRestClientIntegrationTest#setUp ] Client initialized with baseUrl={}", baseUrl);
    }
    
    @AfterEach
    @Override
    public void tearDown() {
        if (client != null) {
            client.close();
        }
        super.tearDown();
    }
    
    /**
     * Scenario: 書籍一覧を取得（WireMockスタブ）
     * 
     * Given: WireMockが書籍一覧APIをスタブする
     * When: GET /api/books を呼び出す
     * Then: 書籍一覧が返される
     */
    @Test
    void testGetAllBooks_Success() {
        logger.info("[ BackOfficeRestClientIntegrationTest#testGetAllBooks_Success ] START");
        
        // Given: WireMockスタブ設定
        String jsonResponse = """
            [
                {
                    "bookId": 1,
                    "bookName": "Java完全理解",
                    "author": "山田太郎",
                    "categoryId": 1,
                    "categoryName": "プログラミング",
                    "publisherId": 1,
                    "publisherName": "技術評論社",
                    "price": 3000,
                    "quantity": 10,
                    "version": 1
                },
                {
                    "bookId": 2,
                    "bookName": "Spring Boot入門",
                    "author": "鈴木一郎",
                    "categoryId": 1,
                    "categoryName": "プログラミング",
                    "publisherId": 2,
                    "publisherName": "翔泳社",
                    "price": 2500,
                    "quantity": 5,
                    "version": 1
                }
            ]
            """;
        
        stubFor(get(urlEqualTo("/api/books"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(jsonResponse)));
        
        // When: GET /api/books
        try (Response response = client.target(baseUrl)
                .path("/books")
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            
            // Then: 200 OK
            assertEquals(200, response.getStatus());
            
            List<BookTO> books = response.readEntity(new GenericType<List<BookTO>>() {});
            assertNotNull(books);
            assertEquals(2, books.size());
            
            BookTO book1 = books.get(0);
            assertEquals(1, book1.bookId());
            assertEquals("Java完全理解", book1.bookName());
            assertEquals(3000, book1.price());
            assertEquals(10, book1.quantity());
            
            BookTO book2 = books.get(1);
            assertEquals(2, book2.bookId());
            assertEquals("Spring Boot入門", book2.bookName());
            assertEquals(2500, book2.price());
            assertEquals(5, book2.quantity());
        }
        
        // WireMock呼び出し検証
        verify(getRequestedFor(urlEqualTo("/api/books")));
        
        logger.info("[ BackOfficeRestClientIntegrationTest#testGetAllBooks_Success ] PASS");
    }
    
    /**
     * Scenario: 書籍詳細を取得
     * 
     * Given: WireMockが書籍詳細APIをスタブする
     * When: GET /api/books/1 を呼び出す
     * Then: 書籍詳細が返される
     */
    @Test
    void testGetBookById_Success() {
        logger.info("[ BackOfficeRestClientIntegrationTest#testGetBookById_Success ] START");
        
        // Given: WireMockスタブ設定
        String jsonResponse = """
            {
                "bookId": 1,
                "bookName": "Java完全理解",
                "author": "山田太郎",
                "categoryId": 1,
                "categoryName": "プログラミング",
                "publisherId": 1,
                "publisherName": "技術評論社",
                "price": 3000,
                "quantity": 10,
                "version": 1
            }
            """;
        
        stubFor(get(urlEqualTo("/api/books/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(jsonResponse)));
        
        // When: GET /api/books/1
        try (Response response = client.target(baseUrl)
                .path("/books/1")
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            
            // Then: 200 OK
            assertEquals(200, response.getStatus());
            
            BookTO book = response.readEntity(BookTO.class);
            assertNotNull(book);
            assertEquals(1, book.bookId());
            assertEquals("Java完全理解", book.bookName());
            assertEquals("山田太郎", book.author());
            assertEquals(3000, book.price());
            assertEquals(10, book.quantity());
        }
        
        verify(getRequestedFor(urlEqualTo("/api/books/1")));
        
        logger.info("[ BackOfficeRestClientIntegrationTest#testGetBookById_Success ] PASS");
    }
    
    /**
     * Scenario: 在庫情報を取得
     * 
     * Given: WireMockが在庫APIをスタブする
     * When: GET /api/stocks/1 を呼び出す
     * Then: 在庫情報が返される
     */
    @Test
    void testFindStockById_Success() {
        logger.info("[ BackOfficeRestClientIntegrationTest#testFindStockById_Success ] START");
        
        // Given: WireMockスタブ設定
        String jsonResponse = """
            {
                "bookId": 1,
                "bookName": "Java完全理解",
                "quantity": 10,
                "version": 1
            }
            """;
        
        stubFor(get(urlEqualTo("/api/stocks/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(jsonResponse)));
        
        // When: GET /api/stocks/1
        try (Response response = client.target(baseUrl)
                .path("/stocks/1")
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            
            // Then: 200 OK
            assertEquals(200, response.getStatus());
            
            StockTO stock = response.readEntity(StockTO.class);
            assertNotNull(stock);
            assertEquals(1, stock.bookId());
            assertEquals("Java完全理解", stock.bookName());
            assertEquals(10, stock.quantity());
            assertEquals(1L, stock.version());
        }
        
        verify(getRequestedFor(urlEqualTo("/api/stocks/1")));
        
        logger.info("[ BackOfficeRestClientIntegrationTest#testFindStockById_Success ] PASS");
    }
    
    /**
     * Scenario: 在庫を更新（楽観的ロック対応）
     * 
     * Given: WireMockが在庫更新APIをスタブする
     * When: PUT /api/stocks/1 を呼び出す（quantity=8, version=1）
     * Then: 更新後の在庫情報が返される
     */
    @Test
    void testUpdateStock_Success() {
        logger.info("[ BackOfficeRestClientIntegrationTest#testUpdateStock_Success ] START");
        
        // Given: WireMockスタブ設定
        String jsonResponse = """
            {
                "bookId": 1,
                "bookName": "Java完全理解",
                "quantity": 8,
                "version": 2
            }
            """;
        
        stubFor(put(urlEqualTo("/api/stocks/1"))
            .withRequestBody(matchingJsonPath("$.quantity", equalTo("8")))
            .withRequestBody(matchingJsonPath("$.version", equalTo("1")))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(jsonResponse)));
        
        // When: PUT /api/stocks/1
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("quantity", 8);
        requestBody.put("version", 1);
        
        try (Response response = client.target(baseUrl)
                .path("/stocks/1")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(requestBody, MediaType.APPLICATION_JSON))) {
            
            // Then: 200 OK
            assertEquals(200, response.getStatus());
            
            StockTO stock = response.readEntity(StockTO.class);
            assertNotNull(stock);
            assertEquals(1, stock.bookId());
            assertEquals(8, stock.quantity());
            assertEquals(2L, stock.version()); // バージョンがインクリメント
        }
        
        verify(putRequestedFor(urlEqualTo("/api/stocks/1"))
            .withRequestBody(matchingJsonPath("$.quantity"))
            .withRequestBody(matchingJsonPath("$.version")));
        
        logger.info("[ BackOfficeRestClientIntegrationTest#testUpdateStock_Success ] PASS");
    }
    
    /**
     * Scenario: 楽観的ロック競合で在庫更新失敗
     * 
     * Given: WireMockが409エラーをスタブする
     * When: PUT /api/stocks/1 を呼び出す（古いバージョン）
     * Then: 409 Conflictが返される
     */
    @Test
    void testUpdateStock_OptimisticLockConflict() {
        logger.info("[ BackOfficeRestClientIntegrationTest#testUpdateStock_OptimisticLockConflict ] START");
        
        // Given: WireMockスタブ設定（楽観的ロック競合）
        String jsonErrorResponse = """
            {
                "error": "OptimisticLockException",
                "message": "在庫データが他のユーザーによって更新されました"
            }
            """;
        
        stubFor(put(urlEqualTo("/api/stocks/1"))
            .willReturn(aResponse()
                .withStatus(409)
                .withHeader("Content-Type", "application/json")
                .withBody(jsonErrorResponse)));
        
        // When: PUT /api/stocks/1（古いバージョン）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("quantity", 8);
        requestBody.put("version", 1); // 古いバージョン
        
        try (Response response = client.target(baseUrl)
                .path("/stocks/1")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(requestBody, MediaType.APPLICATION_JSON))) {
            
            // Then: 409 Conflict
            assertEquals(409, response.getStatus());
        }
        
        verify(putRequestedFor(urlEqualTo("/api/stocks/1")));
        
        logger.info("[ BackOfficeRestClientIntegrationTest#testUpdateStock_OptimisticLockConflict ] PASS");
    }
    
    /**
     * Scenario: 外部API呼び出し失敗
     * 
     * Given: WireMockが500エラーをスタブする
     * When: GET /api/books を呼び出す
     * Then: 500 Internal Server Errorが返される
     */
    @Test
    void testGetAllBooks_ExternalApiError() {
        logger.info("[ BackOfficeRestClientIntegrationTest#testGetAllBooks_ExternalApiError ] START");
        
        // Given: WireMockスタブ設定（500エラー）
        stubFor(get(urlEqualTo("/api/books"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Internal Server Error\"}")));
        
        // When: GET /api/books
        try (Response response = client.target(baseUrl)
                .path("/books")
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            
            // Then: 500 Internal Server Error
            assertEquals(500, response.getStatus());
        }
        
        verify(getRequestedFor(urlEqualTo("/api/books")));
        
        logger.info("[ BackOfficeRestClientIntegrationTest#testGetAllBooks_ExternalApiError ] PASS");
    }
}
