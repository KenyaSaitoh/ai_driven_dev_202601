package pro.kensait.berrybooks.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.kensait.berrybooks.external.dto.BookTO;

/**
 * BackOfficeRestClient 結合テスト
 * 
 * 対象: 外部API呼び出し（WireMock）
 * シナリオ: specs/baseline/basic_design/books_proxy/behaviors.md
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
        logger.info("[ BackOfficeRestClientIntegrationTest#setUp ] JAX-RS Client initialized");
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
     * Given: WireMockが書籍APIをスタブする
     * When: GET /api/booksを呼び出す
     * Then: 外部APIが呼ばれる
     * And: 書籍一覧が返される
     */
    @Test
    void testGetAllBooks_Success() {
        logger.info("[ testGetAllBooks_Success ] Starting test");
        
        // Given: WireMockが書籍APIをスタブする
        stubFor(get(urlEqualTo("/api/books"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[{\"bookId\": 1, \"bookName\": \"Java完全理解\", " +
                         "\"price\": 3000, \"publisherName\": \"技術評論社\"}]")));
        
        // When: GET /api/books
        try (Response response = client.target(baseUrl)
                .path("/books")
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            
            // Then: 200 OK
            assertEquals(200, response.getStatus());
            
            List<BookTO> books = response.readEntity(new GenericType<List<BookTO>>() {});
            assertNotNull(books);
            assertEquals(1, books.size());
            assertEquals("Java完全理解", books.get(0).getBookName());
        }
        
        // And: 外部APIが呼ばれる
        verify(getRequestedFor(urlEqualTo("/api/books")));
        
        logger.info("[ testGetAllBooks_Success ] Test completed successfully");
    }
    
    /**
     * Scenario: 外部API呼び出し失敗
     * Given: WireMockが500エラーを返す
     * When: GET /api/booksを呼び出す
     * Then: 500エラーが返される
     */
    @Test
    void testGetAllBooks_ServerError() {
        logger.info("[ testGetAllBooks_ServerError ] Starting test");
        
        // Given: WireMockが500エラーを返す
        stubFor(get(urlEqualTo("/api/books"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\": \"Internal Server Error\"}")));
        
        // When: GET /api/books
        try (Response response = client.target(baseUrl)
                .path("/books")
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            
            // Then: 500エラー
            assertEquals(500, response.getStatus());
        }
        
        // And: 外部APIが呼ばれる
        verify(getRequestedFor(urlEqualTo("/api/books")));
        
        logger.info("[ testGetAllBooks_ServerError ] Test completed successfully");
    }
    
    /**
     * Scenario: 書籍詳細を取得
     * Given: WireMockが書籍詳細APIをスタブする
     * When: GET /api/books/{bookId}を呼び出す
     * Then: 書籍詳細が返される
     */
    @Test
    void testGetBookById_Success() {
        logger.info("[ testGetBookById_Success ] Starting test");
        
        // Given: WireMockが書籍詳細APIをスタブする
        stubFor(get(urlEqualTo("/api/books/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"bookId\": 1, \"bookName\": \"Java完全理解\", " +
                         "\"price\": 3000, \"publisherName\": \"技術評論社\"}")));
        
        // When: GET /api/books/1
        try (Response response = client.target(baseUrl)
                .path("/books/1")
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            
            // Then: 200 OK
            assertEquals(200, response.getStatus());
            
            BookTO book = response.readEntity(BookTO.class);
            assertNotNull(book);
            assertEquals(Integer.valueOf(1), book.getBookId());
            assertEquals("Java完全理解", book.getBookName());
        }
        
        // And: 外部APIが呼ばれる
        verify(getRequestedFor(urlEqualTo("/api/books/1")));
        
        logger.info("[ testGetBookById_Success ] Test completed successfully");
    }
}