package pro.kensait.berrybooks.external;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.external.dto.BookTO;
import pro.kensait.berrybooks.external.dto.StockTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * back-office-api連携REST クライアント
 */
@ApplicationScoped
public class BackOfficeRestClient {
    private static final Logger logger = LoggerFactory.getLogger(BackOfficeRestClient.class);
    
    @Inject
    @ConfigProperty(name = "back-office-api.base-url", defaultValue = "http://localhost:8080/back-office-api/api")
    private String baseUrl;
    
    private Client client;
    private WebTarget baseTarget;
    
    @PostConstruct
    public void init() {
        this.client = ClientBuilder.newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        
        this.baseTarget = client.target(baseUrl);
        logger.info("[ BackOfficeRestClient#init ] Initialized with base URL: {}", baseUrl);
    }
    
    /**
     * 全書籍を取得
     */
    public List<BookTO> getAllBooks() {
        logger.info("[ BackOfficeRestClient#getAllBooks ] Fetching all books");
        
        Response response = baseTarget.path("/books")
                .request(MediaType.APPLICATION_JSON)
                .get();
        
        if (response.getStatus() == 200) {
            List<BookTO> books = response.readEntity(new GenericType<List<BookTO>>() {});
            logger.info("[ BackOfficeRestClient#getAllBooks ] Retrieved {} books", books.size());
            return books;
        } else {
            logger.error("[ BackOfficeRestClient#getAllBooks ] Failed to fetch books: status={}", response.getStatus());
            throw new RuntimeException("Failed to fetch books from back-office-api");
        }
    }
    
    /**
     * 書籍詳細を取得
     */
    public BookTO getBookById(Integer bookId) {
        logger.info("[ BackOfficeRestClient#getBookById ] Fetching book: bookId={}", bookId);
        
        Response response = baseTarget.path("/books/" + bookId)
                .request(MediaType.APPLICATION_JSON)
                .get();
        
        if (response.getStatus() == 200) {
            BookTO book = response.readEntity(BookTO.class);
            logger.info("[ BackOfficeRestClient#getBookById ] Retrieved book: {}", book.bookName());
            return book;
        } else if (response.getStatus() == 404) {
            logger.warn("[ BackOfficeRestClient#getBookById ] Book not found: bookId={}", bookId);
            return null;
        } else {
            logger.error("[ BackOfficeRestClient#getBookById ] Failed to fetch book: status={}", response.getStatus());
            throw new RuntimeException("Failed to fetch book from back-office-api");
        }
    }
    
    /**
     * 書籍検索（JPQL版）
     */
    public List<BookTO> searchBooksJpql(Integer categoryId, String keyword) {
        logger.info("[ BackOfficeRestClient#searchBooksJpql ] Searching books: categoryId={}, keyword={}", categoryId, keyword);
        
        WebTarget target = baseTarget.path("/books/search/jpql");
        
        if (categoryId != null) {
            target = target.queryParam("categoryId", categoryId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            target = target.queryParam("keyword", keyword);
        }
        
        Response response = target.request(MediaType.APPLICATION_JSON).get();
        
        if (response.getStatus() == 200) {
            List<BookTO> books = response.readEntity(new GenericType<List<BookTO>>() {});
            logger.info("[ BackOfficeRestClient#searchBooksJpql ] Found {} books", books.size());
            return books;
        } else {
            logger.error("[ BackOfficeRestClient#searchBooksJpql ] Search failed: status={}", response.getStatus());
            throw new RuntimeException("Failed to search books from back-office-api");
        }
    }
    
    /**
     * カテゴリ一覧を取得
     */
    public Map<String, Integer> getAllCategories() {
        logger.info("[ BackOfficeRestClient#getAllCategories ] Fetching all categories");
        
        Response response = baseTarget.path("/categories")
                .request(MediaType.APPLICATION_JSON)
                .get();
        
        if (response.getStatus() == 200) {
            Map<String, Integer> categories = response.readEntity(new GenericType<Map<String, Integer>>() {});
            logger.info("[ BackOfficeRestClient#getAllCategories ] Retrieved {} categories", categories.size());
            return categories;
        } else {
            logger.error("[ BackOfficeRestClient#getAllCategories ] Failed to fetch categories: status={}", response.getStatus());
            throw new RuntimeException("Failed to fetch categories from back-office-api");
        }
    }
    
    /**
     * 在庫情報を取得
     */
    public StockTO findStockById(Integer bookId) {
        logger.info("[ BackOfficeRestClient#findStockById ] Fetching stock: bookId={}", bookId);
        
        Response response = baseTarget.path("/stocks/" + bookId)
                .request(MediaType.APPLICATION_JSON)
                .get();
        
        if (response.getStatus() == 200) {
            StockTO stock = response.readEntity(StockTO.class);
            logger.info("[ BackOfficeRestClient#findStockById ] Retrieved stock: quantity={}, version={}", 
                       stock.quantity(), stock.version());
            return stock;
        } else if (response.getStatus() == 404) {
            logger.warn("[ BackOfficeRestClient#findStockById ] Stock not found: bookId={}", bookId);
            return null;
        } else {
            logger.error("[ BackOfficeRestClient#findStockById ] Failed to fetch stock: status={}", response.getStatus());
            throw new RuntimeException("Failed to fetch stock from back-office-api");
        }
    }
    
    /**
     * 在庫を更新（楽観的ロック対応）
     */
    public StockTO updateStock(Integer bookId, Integer quantity, Long version) {
        logger.info("[ BackOfficeRestClient#updateStock ] Updating stock: bookId={}, quantity={}, version={}", 
                   bookId, quantity, version);
        
        Map<String, Object> updateRequest = Map.of(
            "quantity", quantity,
            "version", version
        );
        
        Response response = baseTarget.path("/stocks/" + bookId)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(updateRequest));
        
        if (response.getStatus() == 200) {
            StockTO updatedStock = response.readEntity(StockTO.class);
            logger.info("[ BackOfficeRestClient#updateStock ] Stock updated successfully: newVersion={}", 
                       updatedStock.version());
            return updatedStock;
        } else if (response.getStatus() == 409) {
            logger.warn("[ BackOfficeRestClient#updateStock ] Optimistic lock conflict: bookId={}", bookId);
            throw new jakarta.persistence.OptimisticLockException("Stock was updated by another user");
        } else {
            logger.error("[ BackOfficeRestClient#updateStock ] Failed to update stock: status={}", response.getStatus());
            throw new RuntimeException("Failed to update stock in back-office-api");
        }
    }
}
