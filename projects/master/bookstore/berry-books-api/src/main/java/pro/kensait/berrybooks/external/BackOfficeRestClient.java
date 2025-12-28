package pro.kensait.berrybooks.external;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.GenericType;

import pro.kensait.berrybooks.external.dto.BookTO;
import pro.kensait.berrybooks.external.dto.StockTO;
import pro.kensait.berrybooks.external.dto.StockUpdateRequest;

/**
 * back-office-api APIを呼び出すRESTクライアント
 * 書籍・在庫情報のAPI呼び出しを担当
 */
@ApplicationScoped
public class BackOfficeRestClient {
    private static final Logger logger = LoggerFactory.getLogger(BackOfficeRestClient.class);

    private static final String PROPERTY_KEY = "back-office-api.base-url";
    private static final String ENV_VAR_KEY = "BACK_OFFICE_API_BASE_URL";
    private static final String DEFAULT_URL = "http://localhost:8080/back-office-api/api";
    
    private String baseUrl;
    private Client client;

    @PostConstruct
    public void init() {
        // 設定値を優先順位に従って読み込む
        baseUrl = loadConfig();
        
        // JAX-RS Clientを作成
        client = ClientBuilder.newClient();
        logger.info("BackOfficeRestClient initialized with baseUrl: " + baseUrl);
    }

    /**
     * 設定値を優先順位に従って読み込む
     * 1. システムプロパティ
     * 2. 環境変数
     * 3. プロパティファイル（META-INF/microprofile-config.properties）
     * 4. デフォルト値
     */
    private String loadConfig() {
        // 1. システムプロパティから取得
        String value = System.getProperty(PROPERTY_KEY);
        if (value != null && !value.trim().isEmpty()) {
            logger.info("Using books-stock API URL from system property: " + value);
            return value.trim();
        }
        
        // 2. 環境変数から取得
        value = System.getenv(ENV_VAR_KEY);
        if (value != null && !value.trim().isEmpty()) {
            logger.info("Using books-stock API URL from environment variable: " + value);
            return value.trim();
        }
        
        // 3. プロパティファイルから取得
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("META-INF/microprofile-config.properties")) {
            if (is != null) {
                props.load(is);
                value = props.getProperty(PROPERTY_KEY);
                if (value != null && !value.trim().isEmpty()) {
                    logger.info("Using books-stock API URL from properties file: " + value);
                    return value.trim();
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to load microprofile-config.properties: " + e.getMessage());
        }
        
        // 4. デフォルト値を使用
        logger.info("Using default books-stock API URL: " + DEFAULT_URL);
        return DEFAULT_URL;
    }

    /**
     * すべての書籍を取得する
     * GET /books
     */
    public List<BookTO> findAllBooks() {
        logger.info("[ BackOfficeRestClient#findAllBooks ]");

        WebTarget target = client.target(baseUrl).path("/books");
        logger.info("Request URL: " + target.getUri().toString());

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            if (response.getStatus() == 200) {
                return response.readEntity(new GenericType<List<BookTO>>() {});
            } else {
                logger.error("Unexpected response status: " + response.getStatus());
                return List.of();
            }
        } catch (Exception e) {
            logger.error("Error calling REST API: findAllBooks", e);
            return List.of();
        }
    }

    /**
     * 書籍を検索する（JPQL）
     * GET /books/search/jpql?categoryId=&keyword=
     */
    public List<BookTO> searchBooksJpql(Integer categoryId, String keyword) {
        logger.info("[ BackOfficeRestClient#searchBooksJpql ] categoryId={}, keyword={}", categoryId, keyword);

        WebTarget target = client.target(baseUrl).path("/books/search/jpql");
        if (categoryId != null) {
            target = target.queryParam("categoryId", categoryId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            target = target.queryParam("keyword", keyword);
        }
        
        logger.info("Request URL: " + target.getUri().toString());

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            if (response.getStatus() == 200) {
                return response.readEntity(new GenericType<List<BookTO>>() {});
            } else {
                logger.error("Unexpected response status: " + response.getStatus());
                return List.of();
            }
        } catch (Exception e) {
            logger.error("Error calling REST API: searchBooksJpql", e);
            return List.of();
        }
    }

    /**
     * 書籍を検索する（Criteria API）
     * GET /books/search/criteria?categoryId=&keyword=
     */
    public List<BookTO> searchBooksCriteria(Integer categoryId, String keyword) {
        logger.info("[ BackOfficeRestClient#searchBooksCriteria ] categoryId={}, keyword={}", categoryId, keyword);

        WebTarget target = client.target(baseUrl).path("/books/search/criteria");
        if (categoryId != null) {
            target = target.queryParam("categoryId", categoryId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            target = target.queryParam("keyword", keyword);
        }
        
        logger.info("Request URL: " + target.getUri().toString());

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            if (response.getStatus() == 200) {
                return response.readEntity(new GenericType<List<BookTO>>() {});
            } else {
                logger.error("Unexpected response status: " + response.getStatus());
                return List.of();
            }
        } catch (Exception e) {
            logger.error("Error calling REST API: searchBooksCriteria", e);
            return List.of();
        }
    }

    /**
     * カテゴリ一覧を取得する
     * GET /categories
     * 注: back-office-apiは {"Java": 1, "SpringBoot": 2} 形式を返す（名前→ID）
     */
    public Map<String, Integer> findAllCategories() {
        logger.info("[ BackOfficeRestClient#findAllCategories ]");

        WebTarget target = client.target(baseUrl).path("/categories");
        logger.info("Request URL: " + target.getUri().toString());

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            if (response.getStatus() == 200) {
                return response.readEntity(new GenericType<Map<String, Integer>>() {});
            } else {
                logger.error("Unexpected response status: " + response.getStatus());
                return Map.of();
            }
        } catch (Exception e) {
            logger.error("Error calling REST API: findAllCategories", e);
            return Map.of();
        }
    }

    /**
     * 書籍IDで書籍情報を取得する
     * GET /books/{bookId}
     */
    public BookTO findBookById(Integer bookId) {
        logger.info("[ BackOfficeRestClient#findBookById ] bookId=" + bookId);

        WebTarget target = client.target(baseUrl)
                .path("/books/" + bookId);
        
        logger.info("Request URL: " + target.getUri().toString());

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            return switch (response.getStatus()) {
                case 200 -> response.readEntity(BookTO.class);
                case 404 -> {
                    logger.info("Book not found: " + bookId);
                    yield null;
                }
                default -> {
                    logger.error("Unexpected response status: " + response.getStatus());
                    yield null;
                }
            };
        } catch (Exception e) {
            logger.error("Error calling REST API: findBookById", e);
            return null;
        }
    }

    /**
     * 書籍IDで在庫情報を取得する
     * GET /stock/{bookId}
     */
    public StockTO findStockById(Integer bookId) {
        logger.info("[ BackOfficeRestClient#findStockById ] bookId=" + bookId);

        WebTarget target = client.target(baseUrl)
                .path("/stocks/" + bookId);
        
        logger.info("Request URL: " + target.getUri().toString());

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            return switch (response.getStatus()) {
                case 200 -> response.readEntity(StockTO.class);
                case 404 -> {
                    logger.info("Stock not found: " + bookId);
                    yield null;
                }
                default -> {
                    logger.error("Unexpected response status: " + response.getStatus());
                    yield null;
                }
            };
        } catch (Exception e) {
            logger.error("Error calling REST API: findStockById", e);
            return null;
        }
    }

    /**
     * 在庫を更新する（楽観的ロック対応）
     * PUT /stock/{bookId}/update
     * 
     * @param bookId 書籍ID
     * @param version 楽観的ロック用バージョン番号
     * @param newQuantity 更新後の在庫数
     * @return 更新後の在庫情報
     * @throws OptimisticLockException 楽観的ロック例外
     */
    public StockTO updateStock(Integer bookId, Long version, Integer newQuantity) 
            throws OptimisticLockException {
        logger.info("[ BackOfficeRestClient#updateStock ] bookId=" + bookId 
                + ", version=" + version + ", newQuantity=" + newQuantity);

        StockUpdateRequest request = new StockUpdateRequest(version, newQuantity);

        WebTarget target = client.target(baseUrl)
                .path("/stocks/" + bookId);
        
        logger.info("Request URL: " + target.getUri().toString());

        try (Response response = target.request(MediaType.APPLICATION_JSON)
                .put(Entity.json(request))) {
            
            return switch (response.getStatus()) {
                case 200 -> {
                    // 更新成功
                    StockTO updatedStock = response.readEntity(StockTO.class);
                    logger.info("Stock updated successfully: " + updatedStock);
                    yield updatedStock;
                }
                case 409 -> {
                    // 楽観的ロック失敗
                    logger.warn("Optimistic lock conflict for bookId: " + bookId);
                    throw new OptimisticLockException("在庫が他のユーザーによって更新されました");
                }
                case 404 -> {
                    // 在庫が見つからない
                    logger.error("Stock not found: " + bookId);
                    throw new RuntimeException("在庫情報が見つかりません: bookId=" + bookId);
                }
                default -> {
                    // その他のエラー
                    logger.error("Unexpected response status: " + response.getStatus());
                    throw new RuntimeException("在庫更新に失敗しました: HTTP " + response.getStatus());
                }
            };
            
        } catch (OptimisticLockException e) {
            // そのまま再スロー
            throw e;
        } catch (Exception e) {
            logger.error("Error calling REST API: updateStock", e);
            throw new RuntimeException("在庫更新中にエラーが発生しました", e);
        }
    }
}

