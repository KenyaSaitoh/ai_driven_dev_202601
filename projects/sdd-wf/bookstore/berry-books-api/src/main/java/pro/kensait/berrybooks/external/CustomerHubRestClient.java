package pro.kensait.berrybooks.external;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.external.dto.CustomerTO;

import java.util.concurrent.TimeUnit;

/**
 * customer-hub-api連携RESTクライアント
 */
@ApplicationScoped
public class CustomerHubRestClient {
    private static final Logger logger = LoggerFactory.getLogger(CustomerHubRestClient.class);
    
    @Inject
    @ConfigProperty(name = "customer-hub-api.base-url", defaultValue = "http://localhost:8080/customer-hub-api/customers")
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
        logger.info("[ CustomerHubRestClient#init ] Initialized with base URL: {}", baseUrl);
    }
    
    /**
     * メールアドレスで顧客を検索
     */
    public CustomerTO findByEmail(String email) {
        logger.info("[ CustomerHubRestClient#findByEmail ] Finding customer by email: {}", email);
        
        Response response = baseTarget.path("/query_email")
                .queryParam("email", email)
                .request(MediaType.APPLICATION_JSON)
                .get();
        
        if (response.getStatus() == 200) {
            CustomerTO customer = response.readEntity(CustomerTO.class);
            logger.info("[ CustomerHubRestClient#findByEmail ] Customer found: customerId={}", customer.customerId());
            return customer;
        } else if (response.getStatus() == 404) {
            logger.info("[ CustomerHubRestClient#findByEmail ] Customer not found: email={}", email);
            return null;
        } else {
            logger.error("[ CustomerHubRestClient#findByEmail ] Failed to find customer: status={}", response.getStatus());
            throw new RuntimeException("Failed to find customer from customer-hub-api");
        }
    }
    
    /**
     * 顧客IDで顧客情報を取得
     */
    public CustomerTO findById(Integer customerId) {
        logger.info("[ CustomerHubRestClient#findById ] Finding customer: customerId={}", customerId);
        
        Response response = baseTarget.path("/" + customerId)
                .request(MediaType.APPLICATION_JSON)
                .get();
        
        if (response.getStatus() == 200) {
            CustomerTO customer = response.readEntity(CustomerTO.class);
            logger.info("[ CustomerHubRestClient#findById ] Customer found: {}", customer.customerName());
            return customer;
        } else if (response.getStatus() == 404) {
            logger.warn("[ CustomerHubRestClient#findById ] Customer not found: customerId={}", customerId);
            return null;
        } else {
            logger.error("[ CustomerHubRestClient#findById ] Failed to find customer: status={}", response.getStatus());
            throw new RuntimeException("Failed to find customer from customer-hub-api");
        }
    }
    
    /**
     * 新規顧客を登録
     */
    public CustomerTO register(CustomerTO customerTO) {
        logger.info("[ CustomerHubRestClient#register ] Registering new customer: email={}", customerTO.email());
        
        Response response = baseTarget.path("/")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(customerTO));
        
        if (response.getStatus() == 201) {
            CustomerTO createdCustomer = response.readEntity(CustomerTO.class);
            logger.info("[ CustomerHubRestClient#register ] Customer registered: customerId={}", createdCustomer.customerId());
            return createdCustomer;
        } else if (response.getStatus() == 409) {
            logger.warn("[ CustomerHubRestClient#register ] Email already exists: email={}", customerTO.email());
            throw new RuntimeException("Email already exists");
        } else {
            logger.error("[ CustomerHubRestClient#register ] Failed to register customer: status={}", response.getStatus());
            throw new RuntimeException("Failed to register customer in customer-hub-api");
        }
    }
}
