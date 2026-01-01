package pro.kensait.berrybooks.web.customer;

import java.io.Serializable;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import pro.kensait.berrybooks.entity.Customer;

/**
 * 顧客情報を保持するセッションスコープBean
 * 
 * <p>ログイン中の顧客情報をセッションに保持します。</p>
 * <p>認証フィルターによって認証状態のチェックに使用されます。</p>
 */
@Named
@SessionScoped
public class CustomerBean implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Customer customer;
    
    /**
     * ログイン中の顧客情報を取得
     * 
     * @return ログイン中の顧客情報（未ログインの場合はnull）
     */
    public Customer getCustomer() {
        return customer;
    }
    
    /**
     * ログイン中の顧客情報を設定
     * 
     * @param customer 顧客情報
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    /**
     * ログイン状態をチェック
     * 
     * @return ログイン済みの場合はtrue
     */
    public boolean isLoggedIn() {
        return customer != null;
    }
    
    /**
     * ログアウト処理（顧客情報をクリア）
     */
    public void logout() {
        this.customer = null;
    }
}

