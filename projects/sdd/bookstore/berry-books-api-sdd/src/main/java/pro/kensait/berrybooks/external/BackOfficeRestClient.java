package pro.kensait.berrybooks.external;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.external.dto.BookTO;
import pro.kensait.berrybooks.external.dto.StockTO;

import java.util.List;
import java.util.Map;

/**
 * BackOfficeRestClient - back-office-api連携クライアント
 * 
 * 責務:
 * - back-office-apiとのREST API連携
 * - 書籍・在庫・カテゴリ情報の取得と更新
 * 
 * アノテーション:
 * - @ApplicationScoped: CDI管理Bean（シングルトン）
 * 
 * 連携先:
 * - back-office-api（書籍・在庫・カテゴリ管理）
 */
@ApplicationScoped
public class BackOfficeRestClient {
    
    private static final Logger logger = LoggerFactory.getLogger(BackOfficeRestClient.class);
    
    private String baseUrl;
    private Client client;
    
    /**
     * REST Clientの初期化と設定読み込み
     * 
     * 処理:
     * 1. ConfigProvider.getConfig()でMicroProfile Configを取得
     * 2. config.getOptionalValue("back-office-api.base-url", String.class)で設定値を読み込み
     * 3. 設定値が存在しない場合、デフォルト値を設定
     * 4. ClientBuilder.newClient() - JAX-RS Clientを生成
     * 5. 初期化完了ログ出力
     * 
     * デフォルト値: "http://localhost:8080/back-office-api-sdd/api"
     */
    @PostConstruct
    public void init() {
        this.baseUrl = ConfigProvider.getConfig()
                .getOptionalValue("back-office-api.base-url", String.class)
                .orElse("http://localhost:8080/back-office-api-sdd/api");
        this.client = ClientBuilder.newClient();
        logger.info("BackOfficeRestClient initialized with baseUrl: {}", baseUrl);
    }
    
    /**
     * 全書籍を取得
     * 
     * エンドポイント: GET /books
     * 
     * @return List<BookTO> 書籍リスト
     */
    public List<BookTO> getAllBooks() {
        logger.info("[ BackOfficeRestClient#getAllBooks ] Fetching all books");
        return client.target(baseUrl)
                .path("/books")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<BookTO>>() {});
    }
    
    /**
     * 書籍詳細を取得
     * 
     * エンドポイント: GET /books/{bookId}
     * 
     * @param bookId 書籍ID
     * @return BookTO 書籍情報（存在しない場合はnull）
     */
    public BookTO getBookById(Integer bookId) {
        logger.info("[ BackOfficeRestClient#getBookById ] Fetching book by bookId={}", bookId);
        try {
            return client.target(baseUrl)
                    .path("/books/{id}")
                    .resolveTemplate("id", bookId)
                    .request(MediaType.APPLICATION_JSON)
                    .get(BookTO.class);
        } catch (Exception e) {
            logger.warn("[ BackOfficeRestClient#getBookById ] Book not found, bookId={}", bookId);
            return null;
        }
    }
    
    /**
     * 書籍検索（JPQL）
     * 
     * エンドポイント: GET /books/search/jpql?categoryId={id}&keyword={keyword}
     * 
     * @param categoryId カテゴリID（オプション）
     * @param keyword キーワード（オプション）
     * @return List<BookTO> 検索結果
     */
    public List<BookTO> searchBooksJpql(Integer categoryId, String keyword) {
        logger.info("[ BackOfficeRestClient#searchBooksJpql ] Searching books: categoryId={}, keyword={}", categoryId, keyword);
        WebTarget target = client.target(baseUrl).path("/books/search/jpql");
        
        if (categoryId != null) {
            target = target.queryParam("categoryId", categoryId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            target = target.queryParam("keyword", keyword);
        }
        
        return target.request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<BookTO>>() {});
    }
    
    /**
     * 書籍検索（Criteria API）
     * 
     * エンドポイント: GET /books/search/criteria?categoryId={id}&keyword={keyword}
     * 
     * @param categoryId カテゴリID（オプション）
     * @param keyword キーワード（オプション）
     * @return List<BookTO> 検索結果
     */
    public List<BookTO> searchBooksCriteria(Integer categoryId, String keyword) {
        logger.info("[ BackOfficeRestClient#searchBooksCriteria ] Searching books: categoryId={}, keyword={}", categoryId, keyword);
        WebTarget target = client.target(baseUrl).path("/books/search/criteria");
        
        if (categoryId != null) {
            target = target.queryParam("categoryId", categoryId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            target = target.queryParam("keyword", keyword);
        }
        
        return target.request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<BookTO>>() {});
    }
    
    /**
     * カテゴリ一覧を取得
     * 
     * エンドポイント: GET /categories
     * 
     * @return Map<String, Integer> カテゴリマップ（キー: カテゴリ名、値: カテゴリID）
     */
    public Map<String, Integer> getAllCategories() {
        logger.info("[ BackOfficeRestClient#getAllCategories ] Fetching all categories");
        return client.target(baseUrl)
                .path("/categories")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<Map<String, Integer>>() {});
    }
    
    /**
     * 在庫情報を取得
     * 
     * エンドポイント: GET /stocks/{bookId}
     * 
     * @param bookId 書籍ID
     * @return StockTO 在庫情報（存在しない場合はnull）
     */
    public StockTO findStockById(Integer bookId) {
        logger.info("[ BackOfficeRestClient#findStockById ] Fetching stock by bookId={}", bookId);
        try {
            return client.target(baseUrl)
                    .path("/stocks/{id}")
                    .resolveTemplate("id", bookId)
                    .request(MediaType.APPLICATION_JSON)
                    .get(StockTO.class);
        } catch (Exception e) {
            logger.warn("[ BackOfficeRestClient#findStockById ] Stock not found, bookId={}", bookId);
            return null;
        }
    }
    
    /**
     * 在庫更新（楽観的ロック対応）
     * 
     * エンドポイント: PUT /stocks/{bookId}
     * リクエストボディ: {"quantity": 8, "version": 1}
     * 
     * @param bookId 書籍ID
     * @param version バージョン番号
     * @param newQuantity 新しい在庫数
     * @return StockTO 更新後の在庫情報
     * @throws OptimisticLockException 楽観的ロック失敗時
     */
    public StockTO updateStock(Integer bookId, Long version, Integer newQuantity) {
        logger.info("[ BackOfficeRestClient#updateStock ] Updating stock: bookId={}, version={}, newQuantity={}", 
                   bookId, version, newQuantity);
        
        Map<String, Object> requestBody = Map.of(
            "quantity", newQuantity,
            "version", version
        );
        
        Response response = client.target(baseUrl)
                .path("/stocks/{id}")
                .resolveTemplate("id", bookId)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(requestBody));
        
        if (response.getStatus() == 409) {
            logger.warn("[ BackOfficeRestClient#updateStock ] Optimistic lock conflict, bookId={}, version={}", bookId, version);
            throw new OptimisticLockException("他のユーザーが購入済みです。最新の在庫情報を確認してください。");
        }
        
        if (response.getStatus() >= 400) {
            logger.error("[ BackOfficeRestClient#updateStock ] Failed to update stock, status={}", response.getStatus());
            throw new RuntimeException("在庫更新に失敗しました");
        }
        
        StockTO stockTO = response.readEntity(StockTO.class);
        logger.info("[ BackOfficeRestClient#updateStock ] Stock updated successfully, bookId={}", bookId);
        return stockTO;
    }
}
