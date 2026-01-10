package pro.kensait.berrybooks.security;

import jakarta.enterprise.context.RequestScoped;

/**
 * JWT認証情報を保持するスレッドローカルコンテキスト
 * 
 * JwtAuthenFilterで設定され、認証必須のResourceで参照する。
 */
@RequestScoped
public class AuthenContext {

    private Integer customerId;
    private String email;

    // デフォルトコンストラクタ
    public AuthenContext() {
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
        return "AuthenContext{" +
               "customerId=" + customerId +
               ", email='" + email + '\'' +
               '}';
    }
}
