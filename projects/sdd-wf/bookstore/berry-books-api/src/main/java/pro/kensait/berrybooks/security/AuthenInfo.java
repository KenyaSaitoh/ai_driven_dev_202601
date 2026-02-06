package pro.kensait.berrybooks.security;

import jakarta.enterprise.context.RequestScoped;
import java.io.Serializable;

/**
 * 認証情報コンテキスト
 * 
 * リクエストスコープで認証情報を管理する。
 * JwtAuthenFilterで認証情報を設定し、Resourceクラスで参照する。
 * 
 * @since 1.0.0
 */
@RequestScoped
public class AuthenInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer customerId;
    private String email;
    
    /**
     * デフォルトコンストラクタ
     */
    public AuthenInfo() {
    }
    
    /**
     * 認証済みかどうかを判定する
     * 
     * @return 認証済みの場合はtrue
     */
    public boolean isAuthenticated() {
        return customerId != null && email != null;
    }
    
    // Getters and Setters
    
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
    
    @Override
    public String toString() {
        return "AuthenInfo{" +
                "customerId=" + customerId +
                ", email='" + email + '\'' +
                '}';
    }
}
