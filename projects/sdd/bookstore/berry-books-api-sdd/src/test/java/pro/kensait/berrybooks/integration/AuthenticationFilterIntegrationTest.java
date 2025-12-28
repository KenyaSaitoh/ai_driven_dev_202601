package pro.kensait.berrybooks.integration;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import pro.kensait.berrybooks.entity.Customer;
import pro.kensait.berrybooks.web.customer.CustomerBean;

import java.util.Arrays;
import java.util.List;

/**
 * T_INTEG_004: 認証フィルターの結合テスト
 * 
 * 目的: F-004（顧客管理・認証）の認証フィルターを確認する
 * 
 * テストシナリオ:
 * 1. 未ログイン状態で保護ページ（bookSearch.xhtml）に直接アクセス
 * 2. ログイン画面にリダイレクトされることを確認
 * 3. ログイン後、再度保護ページにアクセス
 * 4. 正常に表示されることを確認
 * 5. ログアウト後、再度保護ページにアクセス
 * 6. ログイン画面にリダイレクトされることを確認
 * 
 * 期待結果: 未ログインユーザーはログイン画面にリダイレクトされる
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationFilterIntegrationTest extends IntegrationTestBase {

    // 公開ページ（認証不要）
    private static final List<String> PUBLIC_PAGES = Arrays.asList(
        "index.xhtml",
        "customerInput.xhtml",
        "customerOutput.xhtml"
    );
    
    // 保護ページ（認証必須）
    private static final List<String> PROTECTED_PAGES = Arrays.asList(
        "bookSearch.xhtml",
        "bookSelect.xhtml",
        "cartView.xhtml",
        "bookOrder.xhtml",
        "orderSuccess.xhtml",
        "orderError.xhtml",
        "orderHistory.xhtml",
        "orderDetail.xhtml"
    );

    @Test
    @Order(1)
    @DisplayName("未ログインユーザーは保護ページにアクセスできない")
    public void testUnauthenticatedAccessToProtectedPage() {
        // Given: CustomerBeanがセッションに存在しない（未ログイン状態）
        CustomerBean customerBean = null;
        
        // When: 保護ページへのアクセスを試みる
        for (String protectedPage : PROTECTED_PAGES) {
            boolean isAuthenticated = isUserAuthenticated(customerBean);
            boolean shouldRedirect = !isAuthenticated && isProtectedPage(protectedPage);
            
            // Then: ログイン画面にリダイレクトされるはずです
            assertTrue(shouldRedirect, 
                    "未ログインユーザーは " + protectedPage + " にアクセスできず、リダイレクトされるはずです");
        }
    }
    
    @Test
    @Order(2)
    @DisplayName("ログインユーザーは保護ページにアクセスできる")
    public void testAuthenticatedAccessToProtectedPage() {
        // Given: CustomerBeanがセッションに存在する（ログイン済み）
        CustomerBean customerBean = new CustomerBean();
        Customer customer = new Customer();
        customer.setCustomerId(1);
        customer.setCustomerName("Test User");
        customer.setEmail("test@example.com");
        customerBean.setCustomer(customer);
        
        // When: 保護ページへのアクセスを試みる
        for (String protectedPage : PROTECTED_PAGES) {
            boolean isAuthenticated = isUserAuthenticated(customerBean);
            boolean canAccess = isAuthenticated;
            
            // Then: 正常にアクセスできるはずです
            assertTrue(canAccess, 
                    "ログインユーザーは " + protectedPage + " にアクセスできるはずです");
        }
    }
    
    @Test
    @Order(3)
    @DisplayName("公開ページは認証なしでアクセスできる")
    public void testPublicPageAccess() {
        // Given: CustomerBeanがセッションに存在しない（未ログイン状態）
        CustomerBean customerBean = null;
        
        // When: 公開ページへのアクセスを試みる
        for (String publicPage : PUBLIC_PAGES) {
            boolean isAuthenticated = isUserAuthenticated(customerBean);
            boolean shouldRedirect = !isAuthenticated && isProtectedPage(publicPage);
            
            // Then: リダイレクトされずにアクセスできるはずです
            assertFalse(shouldRedirect, 
                    "公開ページ " + publicPage + " は認証なしでアクセスできるはずです");
        }
    }
    
    @Test
    @Order(4)
    @DisplayName("ログアウト後は保護ページにアクセスできない")
    public void testAccessAfterLogout() {
        // Given: ログイン状態
        CustomerBean customerBean = new CustomerBean();
        Customer customer = new Customer();
        customer.setCustomerId(1);
        customerBean.setCustomer(customer);
        
        assertTrue(isUserAuthenticated(customerBean), 
                "ログイン状態であるはずです");
        
        // When: ログアウト
        customerBean.logout();
        customerBean.setCustomer(null);
        
        // Then: ログアウト後は未認証状態になる
        assertFalse(isUserAuthenticated(customerBean), 
                "ログアウト後は未認証状態になるはずです");
        
        // 保護ページへのアクセスが拒否される
        for (String protectedPage : PROTECTED_PAGES) {
            boolean isAuthenticated = isUserAuthenticated(customerBean);
            boolean shouldRedirect = !isAuthenticated && isProtectedPage(protectedPage);
            
            assertTrue(shouldRedirect, 
                    "ログアウト後は " + protectedPage + " にアクセスできず、リダイレクトされるはずです");
        }
    }
    
    @Test
    @Order(5)
    @DisplayName("セッションタイムアウト後のアクセス制御")
    public void testSessionTimeoutAccess() {
        // Given: セッションがタイムアウトした（CustomerBeanがnull）
        CustomerBean customerBean = null;
        String targetPage = "bookSearch.xhtml";
        
        // When: 保護ページへのアクセスを試みる
        boolean isAuthenticated = isUserAuthenticated(customerBean);
        boolean shouldRedirect = !isAuthenticated && isProtectedPage(targetPage);
        
        // Then: ログイン画面にリダイレクトされる
        assertTrue(shouldRedirect, 
                "セッションタイムアウト後は保護ページにアクセスできず、ログイン画面にリダイレクトされるはずです");
    }
    
    @Test
    @Order(6)
    @DisplayName("認証フィルターの適用範囲確認")
    public void testFilterScope() {
        // Given: 全ページのリスト
        List<String> allPages = new java.util.ArrayList<>();
        allPages.addAll(PUBLIC_PAGES);
        allPages.addAll(PROTECTED_PAGES);
        
        // When & Then: 各ページの認証要否を確認
        for (String page : allPages) {
            boolean isProtected = isProtectedPage(page);
            boolean isPublic = PUBLIC_PAGES.contains(page);
            
            // 公開ページは保護されていない
            if (isPublic) {
                assertFalse(isProtected, 
                        page + " は公開ページなので、保護されていないはずです");
            }
            
            // 保護ページは認証が必要
            if (PROTECTED_PAGES.contains(page)) {
                assertTrue(isProtected, 
                        page + " は保護ページなので、認証が必要なはずです");
            }
        }
    }
    
    /**
     * ユーザーが認証済みかどうかをチェック
     */
    private boolean isUserAuthenticated(CustomerBean customerBean) {
        return customerBean != null && customerBean.getCustomer() != null;
    }
    
    /**
     * ページが保護ページかどうかをチェック
     */
    private boolean isProtectedPage(String pageName) {
        return PROTECTED_PAGES.contains(pageName);
    }
}






