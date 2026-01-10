package pro.kensait.berrybooks.external;

import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pro.kensait.berrybooks.api.dto.ErrorResponse;
import pro.kensait.berrybooks.external.dto.CustomerTO;
import pro.kensait.berrybooks.service.exception.EmailAlreadyExistsException;

/**
 * customer-hub-api連携クライアント
 * 
 * 顧客情報管理APIとの連携を提供する。
 */
@ApplicationScoped
public class CustomerHubRestClient {

    private static final Logger logger = LoggerFactory.getLogger(CustomerHubRestClient.class);

    private String baseUrl;
    private Client client;

    @PostConstruct
    public void init() {
        this.client = ClientBuilder.newClient();
        // ConfigProviderを使って直接設定を取得
        this.baseUrl = ConfigProvider.getConfig()
                .getOptionalValue("customer-hub-api.base-url", String.class)
                .orElse("http://localhost:8080/customer-hub-api/api/customers");
        logger.info("CustomerHubRestClient initialized, baseUrl: {}", baseUrl);
    }

    /**
     * メールアドレスで顧客を検索する
     * 
     * @param email メールアドレス
     * @return 顧客情報（存在しない場合はnull）
     */
    public CustomerTO findByEmail(String email) {
        logger.info("[ CustomerHubRestClient#findByEmail ] email={}", email);
        String url = baseUrl + "/query_email?email=" + email;
        logger.debug("Request URL: {}", url);

        try (Response response = client.target(url)
                .request(MediaType.APPLICATION_JSON)
                .get()) {

            int status = response.getStatus();
            logger.debug("Response status: {}", status);

            if (status == 200) {
                CustomerTO customerTO = response.readEntity(CustomerTO.class);
                logger.info("Customer found: customerId={}", customerTO.customerId());
                return customerTO;
            } else if (status == 404) {
                logger.warn("Customer not found: email={}", email);
                return null;
            } else {
                logger.error("Unexpected response status: {}", status);
                throw new RuntimeException("Failed to call external API: status=" + status);
            }
        } catch (Exception e) {
            logger.error("Error calling REST API: findByEmail", e);
            throw new RuntimeException("Failed to call external API", e);
        }
    }

    /**
     * 顧客IDで顧客を検索する
     * 
     * @param customerId 顧客ID
     * @return 顧客情報（存在しない場合はnull）
     */
    public CustomerTO findById(Integer customerId) {
        logger.info("[ CustomerHubRestClient#findById ] customerId={}", customerId);
        String url = baseUrl + "/" + customerId;
        logger.debug("Request URL: {}", url);

        try (Response response = client.target(url)
                .request(MediaType.APPLICATION_JSON)
                .get()) {

            int status = response.getStatus();
            logger.debug("Response status: {}", status);

            if (status == 200) {
                CustomerTO customerTO = response.readEntity(CustomerTO.class);
                logger.info("Customer found: customerId={}", customerTO.customerId());
                return customerTO;
            } else if (status == 404) {
                logger.warn("Customer not found: customerId={}", customerId);
                return null;
            } else {
                logger.error("Unexpected response status: {}", status);
                throw new RuntimeException("Failed to call external API: status=" + status);
            }
        } catch (Exception e) {
            logger.error("Error calling REST API: findById", e);
            throw new RuntimeException("Failed to call external API", e);
        }
    }

    /**
     * 新規顧客を登録する
     * 
     * @param customerTO 顧客情報
     * @return 登録された顧客情報
     * @throws EmailAlreadyExistsException メールアドレスが既に存在する場合
     */
    public CustomerTO register(CustomerTO customerTO) {
        logger.info("[ CustomerHubRestClient#register ] email={}", customerTO.email());
        String url = baseUrl;
        logger.debug("Request URL: {}", url);

        try (Response response = client.target(url)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(customerTO))) {

            int status = response.getStatus();
            logger.debug("Response status: {}", status);

            if (status == 200 || status == 201) {
                CustomerTO result = response.readEntity(CustomerTO.class);
                logger.info("Customer registered: customerId={}", result.customerId());
                return result;
            } else if (status == 409) {
                ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);
                logger.warn("Email already exists: email={}", customerTO.email());
                throw new EmailAlreadyExistsException(customerTO.email(), errorResponse.message());
            } else {
                logger.error("Unexpected response status: {}", status);
                throw new RuntimeException("Failed to call external API: status=" + status);
            }
        } catch (EmailAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error calling REST API: register", e);
            throw new RuntimeException("Failed to call external API", e);
        }
    }
}
