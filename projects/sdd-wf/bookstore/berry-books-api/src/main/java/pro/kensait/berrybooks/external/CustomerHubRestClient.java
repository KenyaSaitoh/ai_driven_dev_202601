package pro.kensait.berrybooks.external;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.external.dto.CustomerTO;

/**
 * customer-hub-api REST クライアント
 * 
 * 顧客管理APIとの連携を担当する。
 * 
 * @since 1.0.0
 */
@ApplicationScoped
public class CustomerHubRestClient {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerHubRestClient.class);
    
    @Inject
    @ConfigProperty(name = "customer-hub-api.base-url", defaultValue = "http://localhost:8080/customer-hub-api/customers")
    private String baseUrl;
    
    private Client client;
    
    @PostConstruct
    public void init() {
        this.client = ClientBuilder.newClient();
        logger.info("[ CustomerHubRestClient#init ] Initialized with baseUrl={}", baseUrl);
    }
    
    @PreDestroy
    public void destroy() {
        if (client != null) {
            client.close();
            logger.info("[ CustomerHubRestClient#destroy ] Client closed");
        }
    }
    
    /**
     * メールアドレスで顧客を検索する
     * 
     * @param email メールアドレス
     * @return 顧客情報（存在しない場合はnull）
     */
    public CustomerTO findByEmail(String email) {
        logger.info("[ CustomerHubRestClient#findByEmail ] email={}", email);
        
        try (Response response = client.target(baseUrl)
                .path("/query_email")
                .queryParam("email", email)
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            
            if (response.getStatus() == 200) {
                CustomerTO customer = response.readEntity(CustomerTO.class);
                logger.info("[ CustomerHubRestClient#findByEmail ] Found customer: customerId={}", customer.customerId());
                return customer;
            } else if (response.getStatus() == 404) {
                logger.warn("[ CustomerHubRestClient#findByEmail ] Customer not found: email={}", email);
                return null;
            } else {
                logger.error("[ CustomerHubRestClient#findByEmail ] Failed with status: {}", response.getStatus());
                throw new RuntimeException("Failed to find customer: " + response.getStatus());
            }
        }
    }
    
    /**
     * 顧客IDで顧客情報を取得する
     * 
     * @param customerId 顧客ID
     * @return 顧客情報（存在しない場合はnull）
     */
    public CustomerTO findById(Integer customerId) {
        logger.info("[ CustomerHubRestClient#findById ] customerId={}", customerId);
        
        try (Response response = client.target(baseUrl)
                .path("/" + customerId)
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            
            if (response.getStatus() == 200) {
                CustomerTO customer = response.readEntity(CustomerTO.class);
                logger.info("[ CustomerHubRestClient#findById ] Found customer: {}", customer.customerName());
                return customer;
            } else if (response.getStatus() == 404) {
                logger.warn("[ CustomerHubRestClient#findById ] Customer not found: customerId={}", customerId);
                return null;
            } else {
                logger.error("[ CustomerHubRestClient#findById ] Failed with status: {}", response.getStatus());
                throw new RuntimeException("Failed to find customer: " + response.getStatus());
            }
        }
    }
    
    /**
     * 新規顧客を登録する
     * 
     * @param customerTO 顧客情報
     * @return 登録された顧客情報
     */
    public CustomerTO register(CustomerTO customerTO) {
        logger.info("[ CustomerHubRestClient#register ] email={}", customerTO.email());
        
        try (Response response = client.target(baseUrl)
                .path("/")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(customerTO, MediaType.APPLICATION_JSON))) {
            
            if (response.getStatus() == 201) {
                CustomerTO createdCustomer = response.readEntity(CustomerTO.class);
                logger.info("[ CustomerHubRestClient#register ] Customer registered: customerId={}", 
                        createdCustomer.customerId());
                return createdCustomer;
            } else if (response.getStatus() == 409) {
                logger.warn("[ CustomerHubRestClient#register ] Email already exists: {}", customerTO.email());
                throw new RuntimeException("Email already exists");
            } else {
                logger.error("[ CustomerHubRestClient#register ] Failed with status: {}", response.getStatus());
                throw new RuntimeException("Failed to register customer: " + response.getStatus());
            }
        }
    }
}
