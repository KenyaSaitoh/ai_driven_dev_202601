package pro.kensait.berrybooks.external;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.external.dto.CustomerTO;

/**
 * CustomerHubRestClient - customer-hub-api連携クライアント
 * 
 * 責務:
 * - customer-hub-apiとのREST API連携
 * - 顧客情報の取得と登録
 * 
 * アノテーション:
 * - @ApplicationScoped: CDI管理Bean（シングルトン）
 * 
 * 連携先:
 * - customer-hub-api（顧客管理）
 */
@ApplicationScoped
public class CustomerHubRestClient {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerHubRestClient.class);
    
    private String baseUrl;
    private Client client;
    
    /**
     * REST Clientの初期化と設定読み込み
     * 
     * 処理:
     * 1. ConfigProvider.getConfig()でMicroProfile Configを取得
     * 2. config.getOptionalValue("customer-hub-api.base-url", String.class)で設定値を読み込み
     * 3. 設定値が存在しない場合、デフォルト値を設定
     * 4. ClientBuilder.newClient() - JAX-RS Clientを生成
     * 5. 初期化完了ログ出力
     * 
     * デフォルト値: "http://localhost:8080/customer-hub-api/api/customers"
     */
    @PostConstruct
    public void init() {
        this.baseUrl = ConfigProvider.getConfig()
                .getOptionalValue("customer-hub-api.base-url", String.class)
                .orElse("http://localhost:8080/customer-hub-api/api/customers");
        this.client = ClientBuilder.newClient();
        logger.info("CustomerHubRestClient initialized with baseUrl: {}", baseUrl);
    }
    
    /**
     * メールアドレスで顧客を検索
     * 
     * エンドポイント: GET /customers/query_email?email={email}
     * 目的: ログイン認証時にメールアドレスで顧客を検索
     * 
     * @param email メールアドレス
     * @return CustomerTO 顧客情報（存在しない場合はnull）
     */
    public CustomerTO findByEmail(String email) {
        logger.info("[ CustomerHubRestClient#findByEmail ] Fetching customer by email={}", email);
        try {
            return client.target(baseUrl)
                    .path("/query_email")
                    .queryParam("email", email)
                    .request(MediaType.APPLICATION_JSON)
                    .get(CustomerTO.class);
        } catch (Exception e) {
            logger.warn("[ CustomerHubRestClient#findByEmail ] Customer not found, email={}", email);
            return null;
        }
    }
    
    /**
     * 顧客IDで顧客を検索
     * 
     * エンドポイント: GET /customers/{customerId}
     * 目的: JWT Claimsから顧客IDを取得して顧客情報を取得
     * 
     * @param customerId 顧客ID
     * @return CustomerTO 顧客情報（存在しない場合はnull）
     */
    public CustomerTO findById(Long customerId) {
        logger.info("[ CustomerHubRestClient#findById ] Fetching customer by customerId={}", customerId);
        try {
            return client.target(baseUrl)
                    .path("/{id}")
                    .resolveTemplate("id", customerId)
                    .request(MediaType.APPLICATION_JSON)
                    .get(CustomerTO.class);
        } catch (Exception e) {
            logger.warn("[ CustomerHubRestClient#findById ] Customer not found, customerId={}", customerId);
            return null;
        }
    }
    
    /**
     * 新規顧客を登録
     * 
     * エンドポイント: POST /customers/
     * リクエストボディ: CustomerTO（customerId=null, password=BCryptハッシュ）
     * 
     * @param customer 顧客情報
     * @return CustomerTO 作成された顧客情報
     * @throws RuntimeException メールアドレス重複時（409 Conflict）
     */
    public CustomerTO register(CustomerTO customer) {
        logger.info("[ CustomerHubRestClient#register ] Registering customer: email={}", customer.email());
        
        Response response = client.target(baseUrl)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(customer));
        
        if (response.getStatus() == 409) {
            logger.warn("[ CustomerHubRestClient#register ] Email already exists: {}", customer.email());
            throw new RuntimeException("指定されたメールアドレスは既に登録されています");
        }
        
        if (response.getStatus() >= 400) {
            logger.error("[ CustomerHubRestClient#register ] Failed to register customer, status={}", response.getStatus());
            throw new RuntimeException("顧客登録に失敗しました");
        }
        
        CustomerTO createdCustomer = response.readEntity(CustomerTO.class);
        logger.info("[ CustomerHubRestClient#register ] Customer registered successfully, customerId={}", createdCustomer.customerId());
        return createdCustomer;
    }
}
