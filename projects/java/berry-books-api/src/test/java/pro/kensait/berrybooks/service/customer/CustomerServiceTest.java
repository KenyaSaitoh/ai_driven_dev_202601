package pro.kensait.berrybooks.service.customer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pro.kensait.berrybooks.external.CustomerRestClient;
import pro.kensait.berrybooks.entity.Customer;

/**
 * CustomerServiceのテスト
 * 
 * 【外部システム連携】
 * - 全てのメソッドでCustomerRestClient（モック）を使用
 * - CustomerDaoは使用しない
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    // REST API クライアント（全操作で使用）
    @Mock
    private CustomerRestClient customerRestClient;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;
    private String testEmail;
    private String testPassword;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testPassword = "password123";
        
        testCustomer = new Customer();
        testCustomer.setCustomerId(1);
        testCustomer.setEmail(testEmail);
        testCustomer.setPassword(testPassword);
        testCustomer.setCustomerName("テスト太郎");
        testCustomer.setAddress("東京都渋谷区神宮前1-1-1");
    }

    // registerCustomerのテスト

    @Test
    @DisplayName("新規顧客の登録が正常に完了することをテストする")
    void testRegisterCustomerSuccess() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Customer newCustomer = new Customer();
        newCustomer.setEmail("new@example.com");
        newCustomer.setPassword("newpass123");
        newCustomer.setCustomerName("新規太郎");
        
        Customer registeredCustomer = new Customer();
        registeredCustomer.setCustomerId(2);
        registeredCustomer.setEmail("new@example.com");
        registeredCustomer.setPassword("newpass123");
        registeredCustomer.setCustomerName("新規太郎");
        
        when(customerRestClient.register(newCustomer)).thenReturn(registeredCustomer);

        // 実行フェーズ
        Customer result = customerService.registerCustomer(newCustomer);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        assertEquals(2, result.getCustomerId());
        verify(customerRestClient, times(1)).register(newCustomer);
    }

    @Test
    @DisplayName("重複したメールアドレスで登録時に例外がスローされることをテストする")
    void testRegisterCustomerDuplicateEmail() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Customer newCustomer = new Customer();
        newCustomer.setEmail(testEmail);
        newCustomer.setPassword("newpass123");
        
        when(customerRestClient.register(newCustomer))
                .thenThrow(new EmailAlreadyExistsException(testEmail, "このメールアドレスは既に登録されています"));

        // 実行フェーズと検証フェーズ（出力値ベース、コミュニケーションベース）
        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> {
            customerService.registerCustomer(newCustomer);
        });
        assertEquals("このメールアドレスは既に登録されています", exception.getMessage());
        verify(customerRestClient, times(1)).register(newCustomer);
    }

    @Test
    @DisplayName("メールアドレスがnullの場合でも登録処理が実行されることをテストする")
    void testRegisterCustomerNullEmail() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Customer newCustomer = new Customer();
        newCustomer.setEmail(null);
        newCustomer.setPassword("password123");
        
        Customer registeredCustomer = new Customer();
        registeredCustomer.setCustomerId(3);
        registeredCustomer.setEmail(null);
        registeredCustomer.setPassword("password123");
        
        when(customerRestClient.register(newCustomer)).thenReturn(registeredCustomer);

        // 実行フェーズ
        Customer result = customerService.registerCustomer(newCustomer);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        verify(customerRestClient, times(1)).register(newCustomer);
    }

    // authenticateのテスト

    @Test
    @DisplayName("正しいメールアドレスとパスワードで認証が成功することをテストする")
    void testAuthenticateSuccess() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(customerRestClient.findByEmail(testEmail)).thenReturn(testCustomer);

        // 実行フェーズ
        Customer result = customerService.authenticate(testEmail, testPassword);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(testEmail, result.getEmail());
        assertEquals(testCustomer.getCustomerId(), result.getCustomerId());
        verify(customerRestClient, times(1)).findByEmail(testEmail);
    }

    @Test
    @DisplayName("誤ったパスワードで認証が失敗することをテストする")
    void testAuthenticateWrongPassword() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(customerRestClient.findByEmail(testEmail)).thenReturn(testCustomer);

        // 実行フェーズ
        Customer result = customerService.authenticate(testEmail, "wrongpassword");

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNull(result);
        verify(customerRestClient, times(1)).findByEmail(testEmail);
    }

    @Test
    @DisplayName("存在しないメールアドレスで認証が失敗することをテストする")
    void testAuthenticateUserNotFound() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(customerRestClient.findByEmail("notfound@example.com")).thenReturn(null);

        // 実行フェーズ
        Customer result = customerService.authenticate("notfound@example.com", testPassword);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNull(result);
        verify(customerRestClient, times(1)).findByEmail("notfound@example.com");
    }

    @Test
    @DisplayName("メールアドレスがnullの場合に認証が失敗することをテストする")
    void testAuthenticateNullEmail() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(customerRestClient.findByEmail(null)).thenReturn(null);

        // 実行フェーズ
        Customer result = customerService.authenticate(null, testPassword);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNull(result);
        verify(customerRestClient, times(1)).findByEmail(null);
    }

    @Test
    @DisplayName("パスワードがnullの場合に認証が失敗することをテストする")
    void testAuthenticateNullPassword() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(customerRestClient.findByEmail(testEmail)).thenReturn(testCustomer);

        // 実行フェーズ
        Customer result = customerService.authenticate(testEmail, null);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNull(result);
        verify(customerRestClient, times(1)).findByEmail(testEmail);
    }

    @Test
    @DisplayName("空文字列のパスワードで認証が成功することをテストする（一致する場合）")
    void testAuthenticateEmptyPassword() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Customer customerWithEmptyPassword = new Customer();
        customerWithEmptyPassword.setCustomerId(1);
        customerWithEmptyPassword.setEmail(testEmail);
        customerWithEmptyPassword.setPassword("");
        
        when(customerRestClient.findByEmail(testEmail)).thenReturn(customerWithEmptyPassword);

        // 実行フェーズ
        Customer result = customerService.authenticate(testEmail, "");

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(testEmail, result.getEmail());
        verify(customerRestClient, times(1)).findByEmail(testEmail);
    }

    @Test
    @DisplayName("パスワードが大文字小文字を区別することをテストする")
    void testAuthenticateCaseSensitivePassword() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(customerRestClient.findByEmail(testEmail)).thenReturn(testCustomer);

        // 実行フェーズ
        Customer result = customerService.authenticate(testEmail, "PASSWORD123");

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        // パスワードは大文字小文字を区別する
        assertNull(result);
        verify(customerRestClient, times(1)).findByEmail(testEmail);
    }

    // getCustomerのテスト

    @Test
    @DisplayName("顧客IDで顧客情報を取得できることをテストする")
    void testGetCustomerSuccess() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer customerId = 1;
        when(customerRestClient.findById(customerId)).thenReturn(testCustomer);

        // 実行フェーズ
        Customer result = customerService.getCustomer(customerId);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals(testEmail, result.getEmail());
        verify(customerRestClient, times(1)).findById(customerId);
    }

    @Test
    @DisplayName("存在しない顧客IDで取得時にnullが返されることをテストする")
    void testGetCustomerNotFound() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer customerId = 999;
        when(customerRestClient.findById(customerId)).thenReturn(null);

        // 実行フェーズ
        Customer result = customerService.getCustomer(customerId);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNull(result);
        verify(customerRestClient, times(1)).findById(customerId);
    }

    @Test
    @DisplayName("顧客IDがnullの場合にnullが返されることをテストする")
    void testGetCustomerNullId() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(customerRestClient.findById(null)).thenReturn(null);

        // 実行フェーズ
        Customer result = customerService.getCustomer(null);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNull(result);
        verify(customerRestClient, times(1)).findById(null);
    }

    @Test
    @DisplayName("すべてのフィールドを持つ顧客情報を取得できることをテストする")
    void testGetCustomerWithAllFields() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer customerId = 1;
        testCustomer.setAddress("東京都新宿区西新宿2-2-2");
        
        when(customerRestClient.findById(customerId)).thenReturn(testCustomer);

        // 実行フェーズ
        Customer result = customerService.getCustomer(customerId);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals(testEmail, result.getEmail());
        assertEquals("テスト太郎", result.getCustomerName());
        assertEquals("東京都新宿区西新宿2-2-2", result.getAddress());
        verify(customerRestClient, times(1)).findById(customerId);
    }
}
