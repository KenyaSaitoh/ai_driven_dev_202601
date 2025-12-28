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
 * 顧客管理REST APIクライアント
 * 
 * 外部API（berry-books-rest）との連携を担当
 * Jakarta RESTful Web Services Client APIを使用
 */
@ApplicationScoped
public class CustomerRestClient {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerRestClient.class);
    
    @Inject
    @ConfigProperty(name = "customer.api.base-url", 
                    defaultValue = "http://localhost:8080/customer-api/customers")
    private String baseUrl;
    
    private Client client;
    
    @PostConstruct
    public void init() {
        this.client = ClientBuilder.newClient();
        logger.info("[ CustomerRestClient#init ] Initialized with baseUrl: {}", baseUrl);
    }
    
    @PreDestroy
    public void destroy() {
        if (client != null) {
            client.close();
            logger.info("[ CustomerRestClient#destroy ] Client closed");
        }
    }
    
    /**
     * メールアドレスで顧客を検索
     * 
     * @param email メールアドレス
     * @return CustomerTO（見つからない場合はnull）
     */
    public CustomerTO findByEmail(String email) {
        logger.info("[ CustomerRestClient#findByEmail ] email={}", email);
        
        try {
            Response response = client.target(baseUrl)
                    .path("/query_email")
                    .queryParam("email", email)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            
            if (response.getStatus() == 200) {
                CustomerTO customer = response.readEntity(CustomerTO.class);
                logger.info("[ CustomerRestClient#findByEmail ] Customer found: customerId={}", 
                           customer.customerId());
                return customer;
            } else if (response.getStatus() == 404) {
                logger.info("[ CustomerRestClient#findByEmail ] Customer not found");
                return null;
            } else {
                logger.error("[ CustomerRestClient#findByEmail ] Unexpected status: {}", 
                            response.getStatus());
                return null;
            }
        } catch (Exception e) {
            logger.error("[ CustomerRestClient#findByEmail ] Error occurred", e);
            return null;
        }
    }
    
    /**
     * 顧客IDで顧客を検索
     * 
     * @param customerId 顧客ID
     * @return CustomerTO（見つからない場合はnull）
     */
    public CustomerTO findById(Integer customerId) {
        logger.info("[ CustomerRestClient#findById ] customerId={}", customerId);
        
        try {
            Response response = client.target(baseUrl)
                    .path("/" + customerId)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            
            if (response.getStatus() == 200) {
                CustomerTO customer = response.readEntity(CustomerTO.class);
                logger.info("[ CustomerRestClient#findById ] Customer found: {}", customer.email());
                return customer;
            } else if (response.getStatus() == 404) {
                logger.info("[ CustomerRestClient#findById ] Customer not found");
                return null;
            } else {
                logger.error("[ CustomerRestClient#findById ] Unexpected status: {}", 
                            response.getStatus());
                return null;
            }
        } catch (Exception e) {
            logger.error("[ CustomerRestClient#findById ] Error occurred", e);
            return null;
        }
    }
    
    /**
     * 新規顧客を登録
     * 
     * @param customer 顧客情報
     * @return 登録された顧客情報（失敗時はnull）
     */
    public CustomerTO register(CustomerTO customer) {
        logger.info("[ CustomerRestClient#register ] email={}", customer.email());
        
        try {
            Response response = client.target(baseUrl)
                    .path("/")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(customer, MediaType.APPLICATION_JSON));
            
            if (response.getStatus() == 200) {
                CustomerTO createdCustomer = response.readEntity(CustomerTO.class);
                logger.info("[ CustomerRestClient#register ] Customer registered: customerId={}", 
                           createdCustomer.customerId());
                return createdCustomer;
            } else if (response.getStatus() == 409) {
                logger.warn("[ CustomerRestClient#register ] Email already exists: {}", 
                           customer.email());
                return null;
            } else {
                logger.error("[ CustomerRestClient#register ] Unexpected status: {}", 
                            response.getStatus());
                return null;
            }
        } catch (Exception e) {
            logger.error("[ CustomerRestClient#register ] Error occurred", e);
            return null;
        }
    }
}

