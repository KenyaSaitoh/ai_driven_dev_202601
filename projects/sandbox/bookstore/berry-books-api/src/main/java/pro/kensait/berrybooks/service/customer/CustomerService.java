package pro.kensait.berrybooks.service.customer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.kensait.berrybooks.external.CustomerHubRestClient;
import pro.kensait.berrybooks.external.dto.CustomerTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * 顧客登録と認証を行うサービスクラス
 * 
 * 【外部システム連携】
 * - 全ての顧客関連操作を berry-books-rest API（REST）経由で実行
 * - 直接データベースへのアクセスは行わない
 */
@ApplicationScoped
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(
            CustomerService.class);

    // REST APIクライアント（全ての顧客操作に使用）
    @Inject
    private CustomerHubRestClient customerHubClient;

    /**
     * 顧客を登録する（メールアドレス重複チェック含む）
     * 【外部システム連携】berry-books-rest API を呼び出す
     */
    @Transactional
    public CustomerTO registerCustomer(CustomerTO customer) {
        logger.info("[ CustomerService#registerCustomer ] - REST API呼び出し");
        
        // REST API経由で顧客登録（重複チェックはREST API側で実施）
        return customerHubClient.register(customer);
    }

    /**
     * ログイン認証を行う
     * 【外部システム連携】berry-books-rest API（query_email）を呼び出して顧客情報を取得し、パスワード検証
     */
    public CustomerTO authenticate(String email, String password) {
        logger.info("[ CustomerService#authenticate ] email=" + email + " - REST API呼び出し");
        
        // REST API経由でメールアドレスから顧客を検索
        CustomerTO customer = customerHubClient.findByEmail(email);
        if (customer == null) {
            logger.warn("Customer not found: " + email);
            return null;
        }
        
        // パスワード検証（平文比較）
        if (!customer.password().equals(password)) {
            logger.warn("Password mismatch for: " + email);
            return null;
        }
        
        return customer;
    }

    /**
     * 顧客IDで顧客を取得する
     * 【外部システム連携】berry-books-rest API を呼び出す
     */
    public CustomerTO getCustomer(Integer customerId) {
        logger.info("[ CustomerService#getCustomer ] customerId=" + customerId + " - REST API呼び出し");
        return customerHubClient.findById(customerId);
    }
}

