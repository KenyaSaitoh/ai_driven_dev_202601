package pro.kensait.berrybooks.external;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.external.dto.BookTO;
import pro.kensait.berrybooks.external.dto.StockTO;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * back-office-api REST クライアント
 * 
 * 書籍・在庫・カテゴリ管理APIとの連携を担当する。
 * 
 * @since 1.0.0
 */
@ApplicationScoped
public class BackOfficeRestClient {
    
    private static final Logger logger = LoggerFactory.getLogger(BackOfficeRestClient.class);
    
    @Inject
    @ConfigProperty(name = "back-office-api.base-url", defaultValue = "http://localhost:8080/back-office-api/api")
    private String baseUrl;
    
    private Client client;
    
    @PostConstruct
    public void init() {
        this.client = ClientBuilder.newClient();
        logger.info("[ BackOfficeRestClient#init ] Initialized with baseUrl={}", baseUrl);
    }
    
    @PreDestroy
    public void destroy() {
        if (client != null) {
            client.close();
            logger.info("[ BackOfficeRestClient#destroy ] Client closed");
        }
    }
    
    /**
     * 全書籍を在庫情報と共に取得する
     * 
     * @return 書籍リスト
     */
    public List<BookTO> getAllBooks() {
        logger.info("[ BackOfficeRestClient#getAllBooks ]");
        
        try (Response response = client.target(baseUrl)
                .path("/books")
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            
            if (response.getStatus() == 200) {
                List<BookTO> books = response.readEntity(new GenericType<List<BookTO>>() {});
                logger.info("[ BackOfficeRestClient#getAllBooks ] Found {} books", books.size());
                return books;
            } else {
                logger.error("[ BackOfficeRestClient#getAllBooks ] Failed with status: {}", response.getStatus());
                throw new RuntimeException("Failed to fetch books: " + response.getStatus());
            }
        }
    }
    
    /**
     * 書籍詳細を取得する
     * 
     * @param bookId 書籍ID
     * @return 書籍情報
     */
    public BookTO getBookById(Integer bookId) {
        logger.info("[ BackOfficeRestClient#getBookById ] bookId={}", bookId);
        
        try (Response response = client.target(baseUrl)
                .path("/books/" + bookId)
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            
            if (response.getStatus() == 200) {
                BookTO book = response.readEntity(BookTO.class);
                logger.info("[ BackOfficeRestClient#getBookById ] Found book: {}", book.bookName());
                return book;
            } else if (response.getStatus() == 404) {
                logger.warn("[ BackOfficeRestClient#getBookById ] Book not found: bookId={}", bookId);
                return null;
            } else {
                logger.error("[ BackOfficeRestClient#getBookById ] Failed with status: {}", response.getStatus());
                throw new RuntimeException("Failed to fetch book: " + response.getStatus());
            }
        }
    }
    
    /**
     * カテゴリIDまたはキーワードで書籍を検索する（JPQL使用）
     * 
     * @param categoryId カテゴリID（省略可）
     * @param keyword キーワード（省略可）
     * @return 書籍リスト
     */
    public List<BookTO> searchBooksJpql(Integer categoryId, String keyword) {
        logger.info("[ BackOfficeRestClient#searchBooksJpql ] categoryId={}, keyword={}", categoryId, keyword);
        
        var target = client.target(baseUrl).path("/books/search/jpql");
        
        if (categoryId != null) {
            target = target.queryParam("categoryId", categoryId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            target = target.queryParam("keyword", keyword);
        }
        
        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            if (response.getStatus() == 200) {
                List<BookTO> books = response.readEntity(new GenericType<List<BookTO>>() {});
                logger.info("[ BackOfficeRestClient#searchBooksJpql ] Found {} books", books.size());
                return books;
            } else {
                logger.error("[ BackOfficeRestClient#searchBooksJpql ] Failed with status: {}", response.getStatus());
                throw new RuntimeException("Failed to search books: " + response.getStatus());
            }
        }
    }
    
    /**
     * カテゴリ一覧をマップ形式で取得する
     * 
     * @return カテゴリマップ（カテゴリ名 -> カテゴリID）
     */
    public Map<String, Integer> getAllCategories() {
        logger.info("[ BackOfficeRestClient#getAllCategories ]");
        
        try (Response response = client.target(baseUrl)
                .path("/categories")
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            
            if (response.getStatus() == 200) {
                Map<String, Integer> categories = response.readEntity(new GenericType<Map<String, Integer>>() {});
                logger.info("[ BackOfficeRestClient#getAllCategories ] Found {} categories", categories.size());
                return categories;
            } else {
                logger.error("[ BackOfficeRestClient#getAllCategories ] Failed with status: {}", response.getStatus());
                throw new RuntimeException("Failed to fetch categories: " + response.getStatus());
            }
        }
    }
    
    /**
     * 在庫情報を取得する
     * 
     * @param bookId 書籍ID
     * @return 在庫情報
     */
    public StockTO findStockById(Integer bookId) {
        logger.info("[ BackOfficeRestClient#findStockById ] bookId={}", bookId);
        
        try (Response response = client.target(baseUrl)
                .path("/stocks/" + bookId)
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            
            if (response.getStatus() == 200) {
                StockTO stock = response.readEntity(StockTO.class);
                logger.info("[ BackOfficeRestClient#findStockById ] Found stock: quantity={}", stock.quantity());
                return stock;
            } else if (response.getStatus() == 404) {
                logger.warn("[ BackOfficeRestClient#findStockById ] Stock not found: bookId={}", bookId);
                return null;
            } else {
                logger.error("[ BackOfficeRestClient#findStockById ] Failed with status: {}", response.getStatus());
                throw new RuntimeException("Failed to fetch stock: " + response.getStatus());
            }
        }
    }
    
    /**
     * 在庫を更新する（楽観的ロック対応）
     * 
     * @param bookId 書籍ID
     * @param quantity 新しい在庫数
     * @param version 現在のバージョン番号
     * @return 更新後の在庫情報
     */
    public StockTO updateStock(Integer bookId, Integer quantity, Long version) {
        logger.info("[ BackOfficeRestClient#updateStock ] bookId={}, quantity={}, version={}", 
                bookId, quantity, version);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("quantity", quantity);
        requestBody.put("version", version);
        
        try (Response response = client.target(baseUrl)
                .path("/stocks/" + bookId)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(requestBody, MediaType.APPLICATION_JSON))) {
            
            if (response.getStatus() == 200) {
                StockTO stock = response.readEntity(StockTO.class);
                logger.info("[ BackOfficeRestClient#updateStock ] Updated stock: version={}", stock.version());
                return stock;
            } else if (response.getStatus() == 409) {
                logger.warn("[ BackOfficeRestClient#updateStock ] Optimistic lock conflict: bookId={}", bookId);
                throw new jakarta.persistence.OptimisticLockException("Stock was updated by another user");
            } else {
                logger.error("[ BackOfficeRestClient#updateStock ] Failed with status: {}", response.getStatus());
                throw new RuntimeException("Failed to update stock: " + response.getStatus());
            }
        }
    }
}
