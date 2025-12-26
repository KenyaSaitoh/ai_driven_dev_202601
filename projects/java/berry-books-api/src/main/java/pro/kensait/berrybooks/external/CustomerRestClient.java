package pro.kensait.berrybooks.external;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pro.kensait.berrybooks.external.dto.CustomerTO;
import pro.kensait.berrybooks.external.dto.ErrorResponse;
import pro.kensait.berrybooks.entity.Customer;
import pro.kensait.berrybooks.service.customer.EmailAlreadyExistsException;

/**
 * berry-books-rest APIを呼び出すRESTクライアント
 * Customer関連のAPI呼び出しを担当
 */
@ApplicationScoped
public class CustomerRestClient {
    private static final Logger logger = LoggerFactory.getLogger(
            CustomerRestClient.class);

    private static final String PROPERTY_KEY = "customer.api.base-url";
    private static final String ENV_VAR_KEY = "CUSTOMER_API_BASE_URL";
    private static final String DEFAULT_URL = "http://localhost:8080/berry-books-rest/customers";
    
    private String baseUrl;
    private Client client;

    @PostConstruct
    public void init() {
        // 設定値を優先順位に従って読み込む
        baseUrl = loadConfig();
        
        // JAX-RS Clientを作成
        client = ClientBuilder.newClient();
        logger.info("CustomerRestClient initialized with baseUrl: " + baseUrl);
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
            logger.info("Using customer API URL from system property: " + value);
            return value.trim();
        }
        
        // 2. 環境変数から取得
        value = System.getenv(ENV_VAR_KEY);
        if (value != null && !value.trim().isEmpty()) {
            logger.info("Using customer API URL from environment variable: " + value);
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
                    logger.info("Using customer API URL from properties file: " + value);
                    return value.trim();
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to load microprofile-config.properties: " + e.getMessage());
        }
        
        // 4. デフォルト値を使用
        logger.info("Using default customer API URL: " + DEFAULT_URL);
        return DEFAULT_URL;
    }

    /**
     * メールアドレスで顧客を検索する
     * GET /customers/query_email?email={email}
     */
    public Customer findByEmail(String email) {
        logger.info("[ CustomerRestClient#findByEmail ] email=" + email);

        WebTarget target = client.target(baseUrl)
                .path("/query_email")
                .queryParam("email", email);

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            return switch (response.getStatus()) {
                case 200 -> {
                    CustomerTO customerTO = response.readEntity(CustomerTO.class);
                    yield toCustomer(customerTO);
                }
                case 404 -> {
                    // 顧客が見つからない場合
                    logger.info("Customer not found: " + email);
                    yield null;
                }
                default -> {
                    // その他のエラー
                    logger.error("Unexpected response status: " + response.getStatus());
                    yield null;
                }
            };
        } catch (Exception e) {
            logger.error("Error calling REST API: findByEmail", e);
            return null;
        }
    }

    /**
     * 顧客IDで顧客を検索する
     * GET /customers/{customerId}
     */
    public Customer findById(Integer customerId) {
        logger.info("[ CustomerRestClient#findById ] customerId=" + customerId);

        WebTarget target = client.target(baseUrl)
                .path("/" + customerId);

        try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
            return switch (response.getStatus()) {
                case 200 -> {
                    CustomerTO customerTO = response.readEntity(CustomerTO.class);
                    yield toCustomer(customerTO);
                }
                case 404 -> {
                    // 顧客が見つからない場合
                    logger.info("Customer not found: " + customerId);
                    yield null;
                }
                default -> {
                    // その他のエラー
                    logger.error("Unexpected response status: " + response.getStatus());
                    yield null;
                }
            };
        } catch (Exception e) {
            logger.error("Error calling REST API: findById", e);
            return null;
        }
    }

    /**
     * 顧客を新規登録する
     * POST /customers/
     */
    public Customer register(Customer customer) {
        logger.info("[ CustomerRestClient#register ] customer=" + customer);

        // CustomerエンティティからCustomerTOへ変換
        CustomerTO requestTO = new CustomerTO(
                null, // customerId は新規登録時はnull
                customer.getCustomerName(),
                customer.getPassword(),
                customer.getEmail(),
                customer.getBirthday(),
                customer.getAddress()
        );

        WebTarget target = client.target(baseUrl).path("/");

        try (Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.json(requestTO))) {
            
            return switch (response.getStatus()) {
                case 200 -> {
                    // 登録成功
                    CustomerTO responseTO = response.readEntity(CustomerTO.class);
                    yield toCustomer(responseTO);
                }
                case 409 -> {
                    // メールアドレス重複
                    ErrorResponse error = response.readEntity(ErrorResponse.class);
                    logger.warn("Customer already exists: " + error.message());
                    throw new EmailAlreadyExistsException(customer.getEmail(), error.message());
                }
                default -> {
                    // その他のエラー
                    logger.error("Unexpected response status: " + response.getStatus());
                    throw new RuntimeException("Failed to register customer: HTTP " + response.getStatus());
                }
            };
            
        } catch (EmailAlreadyExistsException e) {
            // そのまま再スロー
            throw e;
        } catch (Exception e) {
            logger.error("Error calling REST API: register", e);
            throw new RuntimeException("Failed to register customer", e);
        }
    }

    /**
     * CustomerTOをCustomerエンティティに変換
     */
    private Customer toCustomer(CustomerTO customerTO) {
        Customer customer = new Customer();
        customer.setCustomerId(customerTO.customerId());
        customer.setCustomerName(customerTO.customerName());
        customer.setPassword(customerTO.password());
        customer.setEmail(customerTO.email());
        customer.setBirthday(customerTO.birthday());
        customer.setAddress(customerTO.address());
        return customer;
    }
}


