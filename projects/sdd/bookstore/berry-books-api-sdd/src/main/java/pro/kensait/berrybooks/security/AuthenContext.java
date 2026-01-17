package pro.kensait.berrybooks.security;

import jakarta.enterprise.context.RequestScoped;
import java.io.Serializable;

/**
 * AuthenContext - 認証コンテキスト
 * 
 * 責務:
 * - 認証コンテキスト管理（リクエストスコープ）
 * - JWTフィルターで設定されたユーザー情報を保持
 * - Resourceクラスでユーザー情報を取得
 * 
 * アノテーション:
 * - @RequestScoped: CDI管理Bean（リクエストスコープ）
 *   リクエスト終了時に自動的にクリーンアップされる
 * 
 * 実装:
 * - Serializable: CDI要件
 */
@RequestScoped
public class AuthenContext implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long customerId;
    private String email;
    
    /**
     * デフォルトコンストラクタ（CDI要件）
     */
    public AuthenContext() {
    }
    
    /**
     * 認証済みかどうかを判定
     * 
     * 処理:
     * 1. customerIdとemailがともにnullでない場合、trueを返却
     * 2. それ以外の場合、falseを返却
     * 
     * @return 認証済み=true、未認証=false
     */
    public boolean isAuthenticated() {
        return customerId != null && email != null;
    }
    
    // Getters and Setters
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
