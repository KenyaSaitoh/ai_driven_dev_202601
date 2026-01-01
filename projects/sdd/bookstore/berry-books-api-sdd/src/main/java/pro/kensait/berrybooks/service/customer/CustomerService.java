package pro.kensait.berrybooks.service.customer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import pro.kensait.berrybooks.entity.Customer;
import pro.kensait.berrybooks.external.CustomerRestClient;
import pro.kensait.berrybooks.external.dto.CustomerTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 顧客サービス
 * 
 * 顧客管理に関するビジネスロジックを提供
 */
@ApplicationScoped
public class CustomerService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
    
    @Inject
    private CustomerRestClient customerRestClient;
    
    /**
     * 顧客を登録
     * 
     * @param customer 顧客情報
     * @return 登録された顧客情報
     * @throws EmailAlreadyExistsException メールアドレスが既に存在する場合
     */
    public Customer register(Customer customer) {
        logger.info("[ CustomerService#register ] email={}", customer.getEmail());
        
        // Customer -> CustomerTO 変換
        CustomerTO customerTO = toCustomerTO(customer);
        
        try {
            // REST API経由で顧客を登録
            CustomerTO registeredTO = customerRestClient.register(customerTO);
            
            if (registeredTO == null) {
                // 409 Conflict - メールアドレス重複
                throw new EmailAlreadyExistsException(customer.getEmail());
            }
            
            // CustomerTO -> Customer 変換
            return toCustomer(registeredTO);
            
        } catch (EmailAlreadyExistsException e) {
            // そのままスロー
            throw e;
        } catch (Exception e) {
            logger.error("[ CustomerService#register ] Unexpected error", e);
            throw new RuntimeException("Customer registration failed", e);
        }
    }
    
    /**
     * 顧客認証
     * 
     * @param email メールアドレス
     * @param password パスワード
     * @return 認証成功時は顧客情報、失敗時はnull
     */
    public Customer authenticate(String email, String password) {
        logger.info("[ CustomerService#authenticate ] email={}", email);
        
        // メールアドレスで顧客を検索
        CustomerTO customerTO = customerRestClient.findByEmail(email);
        
        if (customerTO == null) {
            logger.info("[ CustomerService#authenticate ] Customer not found");
            return null;
        }
        
        // パスワード検証
        if (!customerTO.password().equals(password)) {
            logger.info("[ CustomerService#authenticate ] Password mismatch");
            return null;
        }
        
        logger.info("[ CustomerService#authenticate ] Authentication successful");
        return toCustomer(customerTO);
    }
    
    /**
     * メールアドレスで顧客を検索
     * 
     * @param email メールアドレス
     * @return 顧客情報（見つからない場合はnull）
     */
    public Customer findByEmail(String email) {
        logger.info("[ CustomerService#findByEmail ] email={}", email);
        
        CustomerTO customerTO = customerRestClient.findByEmail(email);
        
        if (customerTO == null) {
            return null;
        }
        
        return toCustomer(customerTO);
    }
    
    /**
     * 顧客IDで顧客を検索
     * 
     * @param customerId 顧客ID
     * @return 顧客情報（見つからない場合はnull）
     */
    public Customer findById(Integer customerId) {
        logger.info("[ CustomerService#findById ] customerId={}", customerId);
        
        CustomerTO customerTO = customerRestClient.findById(customerId);
        
        if (customerTO == null) {
            return null;
        }
        
        return toCustomer(customerTO);
    }
    
    /**
     * Customer -> CustomerTO 変換
     */
    private CustomerTO toCustomerTO(Customer customer) {
        return new CustomerTO(
            customer.getCustomerId(),
            customer.getCustomerName(),
            customer.getPassword(),
            customer.getEmail(),
            customer.getBirthday(),
            customer.getAddress()
        );
    }
    
    /**
     * CustomerTO -> Customer 変換
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

