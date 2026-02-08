package pro.kensait.berrybooks.external;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.berrybooks.external.dto.CustomerTO;

/**
 * customer-hub-api連携クライアント
 */
@ApplicationScoped
public class CustomerHubRestClient {
    private static final Logger logger = LoggerFactory.getLogger(CustomerHubRestClient.class);
    
    @Inject
    @RestClient
    private CustomerHubApi customerHubApi;
    
    /**
     * メールアドレスで顧客を検索する
     * 
     * @param email メールアドレス
     * @return 顧客情報
     */
    public CustomerTO findByEmail(String email) {
        logger.info("[ CustomerHubRestClient#findByEmail ] email={}", email);
        CustomerTO customer = customerHubApi.findByEmail(email);
        if (customer != null) {
            logger.debug("[ CustomerHubRestClient#findByEmail ] Customer found: customerId={}", 
                customer.getCustomerId());
        } else {
            logger.debug("[ CustomerHubRestClient#findByEmail ] Customer not found");
        }
        return customer;
    }
    
    /**
     * 顧客を登録する
     * 
     * @param customer 顧客情報
     * @return 登録された顧客情報
     */
    public CustomerTO createCustomer(CustomerTO customer) {
        logger.info("[ CustomerHubRestClient#createCustomer ] email={}, customerName={}", 
            customer.getEmail(), customer.getCustomerName());
        CustomerTO created = customerHubApi.createCustomer(customer);
        logger.info("[ CustomerHubRestClient#createCustomer ] Customer created: customerId={}", 
            created.getCustomerId());
        return created;
    }
}