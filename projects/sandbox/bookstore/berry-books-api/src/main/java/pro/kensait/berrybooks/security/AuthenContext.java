package pro.kensait.berrybooks.security;

import java.io.Serializable;

import jakarta.enterprise.context.RequestScoped;

/**
 * JWT認証コンテキスト情報を保持するクラス
 * リクエストスコープで管理され、JAX-RSリソースクラスから認証済み顧客情報を取得するために使用する
 */
@RequestScoped
public class AuthenContext implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer customerId;
    private String email;

    public AuthenContext() {
    }

    public AuthenContext(Integer customerId, String email) {
        this.customerId = customerId;
        this.email = email;
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

