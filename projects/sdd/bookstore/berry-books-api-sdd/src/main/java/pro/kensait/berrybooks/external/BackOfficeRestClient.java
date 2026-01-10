package pro.kensait.berrybooks.external;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pro.kensait.berrybooks.external.dto.BookTO;
import pro.kensait.berrybooks.external.dto.StockTO;
import pro.kensait.berrybooks.service.exception.OptimisticLockException;

/**
 * back-office-api連携クライアント
 * 
 * 書籍・在庫・カテゴリ管理APIとの連携を提供する。
 */
@ApplicationScoped
public class BackOfficeRestClient {

    private static final Logger logger = LoggerFactory.getLogger(BackOfficeRestClient.class);

    private String baseUrl;
    private Client client;

    @PostConstruct
    public void init() {
        this.client = ClientBuilder.newClient();
        // ConfigProviderを使って直接設定を取得
        this.baseUrl = ConfigProvider.getConfig()
                .getOptionalValue("back-office-api.base-url", String.class)
                .orElse("http://localhost:8080/back-office-api-sdd/api");
        logger.info("BackOfficeRestClient initialized, baseUrl: {}", baseUrl);
    }

    /**
     * 全書籍を取得する
     * 
     * @return 書籍リスト
     */
    public List<BookTO> findAllBooks() {
        logger.info("[ BackOfficeRestClient#findAllBooks ]");
        String url = baseUrl + "/books";
        logger.debug("Request URL: {}", url);

        try (Response response = client.target(url)
                .request(MediaType.APPLICATION_JSON)
                .get()) {

            if (response.getStatus() == 200) {
                return response.readEntity(new GenericType<List<BookTO>>() {});
            } else {
                logger.error("Unexpected response status: {}", response.getStatus());
                throw new RuntimeException("Failed to call external API: status=" + response.getStatus());
            }
        }
    }

    /**
     * 書籍IDで書籍を検索する
     * 
     * @param bookId 書籍ID
     * @return 書籍情報（存在しない場合はnull）
     */
    public BookTO findBookById(Integer bookId) {
        logger.info("[ BackOfficeRestClient#findBookById ] bookId={}", bookId);
        String url = baseUrl + "/books/" + bookId;
        logger.debug("Request URL: {}", url);

        try (Response response = client.target(url)
                .request(MediaType.APPLICATION_JSON)
                .get()) {

            if (response.getStatus() == 200) {
                return response.readEntity(BookTO.class);
            } else if (response.getStatus() == 404) {
                logger.warn("Book not found: bookId={}", bookId);
                return null;
            } else {
                logger.error("Unexpected response status: {}", response.getStatus());
                throw new RuntimeException("Failed to call external API: status=" + response.getStatus());
            }
        }
    }

    /**
     * 書籍を検索する（JPQL）
     * 
     * @param categoryId カテゴリID（0または未指定=全カテゴリ）
     * @param keyword キーワード（書籍名、著者名で部分一致検索）
     * @return 書籍リスト
     */
    public List<BookTO> searchBooksJpql(Integer categoryId, String keyword) {
        logger.info("[ BackOfficeRestClient#searchBooksJpql ] categoryId={}, keyword={}", categoryId, keyword);
        StringBuilder url = new StringBuilder(baseUrl + "/books/search/jpql");
        
        boolean hasParam = false;
        if (categoryId != null && categoryId > 0) {
            url.append("?categoryId=").append(categoryId);
            hasParam = true;
        }
        if (keyword != null && !keyword.isEmpty()) {
            url.append(hasParam ? "&" : "?").append("keyword=").append(keyword);
        }
        
        logger.debug("Request URL: {}", url);

        try (Response response = client.target(url.toString())
                .request(MediaType.APPLICATION_JSON)
                .get()) {

            if (response.getStatus() == 200) {
                return response.readEntity(new GenericType<List<BookTO>>() {});
            } else {
                logger.error("Unexpected response status: {}", response.getStatus());
                throw new RuntimeException("Failed to call external API: status=" + response.getStatus());
            }
        }
    }

    /**
     * 書籍を検索する（Criteria API）
     * 
     * @param categoryId カテゴリID（0または未指定=全カテゴリ）
     * @param keyword キーワード（書籍名、著者名で部分一致検索）
     * @return 書籍リスト
     */
    public List<BookTO> searchBooksCriteria(Integer categoryId, String keyword) {
        logger.info("[ BackOfficeRestClient#searchBooksCriteria ] categoryId={}, keyword={}", categoryId, keyword);
        StringBuilder url = new StringBuilder(baseUrl + "/books/search/criteria");
        
        boolean hasParam = false;
        if (categoryId != null && categoryId > 0) {
            url.append("?categoryId=").append(categoryId);
            hasParam = true;
        }
        if (keyword != null && !keyword.isEmpty()) {
            url.append(hasParam ? "&" : "?").append("keyword=").append(keyword);
        }
        
        logger.debug("Request URL: {}", url);

        try (Response response = client.target(url.toString())
                .request(MediaType.APPLICATION_JSON)
                .get()) {

            if (response.getStatus() == 200) {
                return response.readEntity(new GenericType<List<BookTO>>() {});
            } else {
                logger.error("Unexpected response status: {}", response.getStatus());
                throw new RuntimeException("Failed to call external API: status=" + response.getStatus());
            }
        }
    }

    /**
     * 全カテゴリを取得する
     * 
     * @return カテゴリMap（カテゴリ名 → カテゴリID）
     */
    public Map<String, Integer> findAllCategories() {
        logger.info("[ BackOfficeRestClient#findAllCategories ]");
        String url = baseUrl + "/categories";
        logger.debug("Request URL: {}", url);

        try (Response response = client.target(url)
                .request(MediaType.APPLICATION_JSON)
                .get()) {

            if (response.getStatus() == 200) {
                return response.readEntity(new GenericType<Map<String, Integer>>() {});
            } else {
                logger.error("Unexpected response status: {}", response.getStatus());
                throw new RuntimeException("Failed to call external API: status=" + response.getStatus());
            }
        }
    }

    /**
     * 在庫を取得する
     * 
     * @param bookId 書籍ID
     * @return 在庫情報（存在しない場合はnull）
     */
    public StockTO findStockById(Integer bookId) {
        logger.info("[ BackOfficeRestClient#findStockById ] bookId={}", bookId);
        String url = baseUrl + "/stocks/" + bookId;
        logger.debug("Request URL: {}", url);

        try (Response response = client.target(url)
                .request(MediaType.APPLICATION_JSON)
                .get()) {

            if (response.getStatus() == 200) {
                return response.readEntity(StockTO.class);
            } else if (response.getStatus() == 404) {
                logger.warn("Stock not found: bookId={}", bookId);
                return null;
            } else {
                logger.error("Unexpected response status: {}", response.getStatus());
                throw new RuntimeException("Failed to call external API: status=" + response.getStatus());
            }
        }
    }

    /**
     * 在庫を更新する（楽観的ロック対応）
     * 
     * @param bookId 書籍ID
     * @param version バージョン番号
     * @param newQuantity 更新後の在庫数
     * @return 更新後の在庫情報
     * @throws OptimisticLockException 楽観的ロック競合
     */
    public StockTO updateStock(Integer bookId, Long version, Integer newQuantity) {
        logger.info("[ BackOfficeRestClient#updateStock ] bookId={}, version={}, newQuantity={}", 
                    bookId, version, newQuantity);
        String url = baseUrl + "/stocks/" + bookId;
        logger.debug("Request URL: {}", url);

        Map<String, Object> requestBody = Map.of(
            "quantity", newQuantity,
            "version", version
        );

        try (Response response = client.target(url)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(requestBody))) {

            int status = response.getStatus();
            logger.debug("Response status: {}", status);

            if (status == 200) {
                StockTO result = response.readEntity(StockTO.class);
                logger.info("Stock updated: bookId={}, newVersion={}", bookId, result.version());
                return result;
            } else if (status == 409) {
                logger.warn("Optimistic lock conflict: bookId={}, version={}", bookId, version);
                throw new OptimisticLockException("在庫が他のユーザーによって更新されました。最新の在庫情報を確認してください。");
            } else {
                logger.error("Unexpected response status: {}", status);
                throw new RuntimeException("Failed to call external API: status=" + status);
            }
        } catch (OptimisticLockException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error calling REST API: updateStock", e);
            throw new RuntimeException("Failed to call external API", e);
        }
    }
}
