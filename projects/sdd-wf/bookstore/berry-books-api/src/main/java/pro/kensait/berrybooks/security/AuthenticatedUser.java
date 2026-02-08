package pro.kensait.berrybooks.security;

import jakarta.enterprise.context.RequestScoped;
import java.io.Serializable;

/**
 * 認証済みユーザー情報の保持クラス
 * リクエストスコープで認証情報を管理
 */
@RequestScoped
public class AuthenticatedUser implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer customerId;
    private String customerName;
    
    public AuthenticatedUser() {
    }
    
    public Integer getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    /**
     * 認証済みかどうかを判定する
     * 
     * @return 認証済みの場合true
     */
    public boolean isAuthenticated() {
        return customerId != null && customerName != null;
    }
}