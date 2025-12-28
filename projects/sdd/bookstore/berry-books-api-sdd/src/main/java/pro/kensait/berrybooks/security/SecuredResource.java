package pro.kensait.berrybooks.security;

import jakarta.enterprise.context.RequestScoped;

/**
 * 認証情報を保持するCDI Bean
 * 
 * JwtAuthenticationFilterでセットされ、各Resourceで参照される
 */
@RequestScoped
public class SecuredResource {
    
    private Integer customerId;
    private String email;
    
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
}
