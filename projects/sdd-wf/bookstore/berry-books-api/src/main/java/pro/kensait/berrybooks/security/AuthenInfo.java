package pro.kensait.berrybooks.security;

import jakarta.enterprise.context.RequestScoped;
import java.io.Serializable;

/**
 * 認証情報のスレッドローカル管理
 */
@RequestScoped
public class AuthenInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer customerId;
    private String email;
    
    public AuthenInfo() {
    }
    
    public Integer getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public boolean isAuthenticated() {
        return customerId != null && email != null;
    }
}
