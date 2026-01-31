package pro.kensait.berrybooks.service.customer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pro.kensait.berrybooks.external.CustomerHubRestClient;
import pro.kensait.berrybooks.external.dto.CustomerTO;

/**
 * CustomerServiceのテスト
 * 
 * 【外部システム連携】
 * - 全てのメソッドでCustomerHubRestClient（モック）を使用
 * - CustomerDaoは使用しない
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    // REST API クライアント（全操作で使用）
    @Mock
    private CustomerHubRestClient customerHubClient;

    @InjectMocks
    private CustomerService customerService;

    private CustomerTO testCustomer;
    private String testEmail;
    private String testPassword;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testPassword = "password123";
        
        testCustomer = new CustomerTO(
                1,
                "テスト太郎",
                testPassword,
                testEmail,
                LocalDate.of(1990, 1, 1),
                "東京都渋谷区神宮前1-1-1"
        );
    }

    // registerCustomerのテスト

    @Test
    @DisplayName("新規顧客の登録が正常に完了することをテストする")
    void testRegisterCustomerSuccess() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        CustomerTO newCustomer = new CustomerTO(null, "新規太郎", "newpass123", "new@example.com", null, null);
        CustomerTO registeredCustomer = new CustomerTO(2, "新規太郎", "newpass123", "new@example.com", null, null);
        
        when(customerHubClient.register(newCustomer)).thenReturn(registeredCustomer);

        // 実行フェーズ
        CustomerTO result = customerService.registerCustomer(newCustomer);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals("new@example.com", result.email());
        assertEquals(2, result.customerId());
        verify(customerHubClient, times(1)).register(newCustomer);
    }

    @Test
    @DisplayName("重複したメールアドレスで登録時に例外がスローされることをテストする")
    void testRegisterCustomerDuplicateEmail() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        CustomerTO newCustomer = new CustomerTO(null, "新規太郎", "newpass123", testEmail, null, null);
        
        when(customerHubClient.register(newCustomer))
                .thenThrow(new EmailAlreadyExistsException(testEmail, "このメールアドレスは既に登録されています"));

        // 実行フェーズと検証フェーズ（出力値ベース、コミュニケーションベース）
        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> {
            customerService.registerCustomer(newCustomer);
        });
        assertEquals("このメールアドレスは既に登録されています", exception.getMessage());
        verify(customerHubClient, times(1)).register(newCustomer);
    }

    // authenticateのテスト

    @Test
    @DisplayName("正しいメールアドレスとパスワードで認証が成功することをテストする")
    void testAuthenticateSuccess() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(customerHubClient.findByEmail(testEmail)).thenReturn(testCustomer);

        // 実行フェーズ
        CustomerTO result = customerService.authenticate(testEmail, testPassword);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(testEmail, result.email());
        assertEquals(testCustomer.customerId(), result.customerId());
        verify(customerHubClient, times(1)).findByEmail(testEmail);
    }

    @Test
    @DisplayName("誤ったパスワードで認証が失敗することをテストする")
    void testAuthenticateWrongPassword() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(customerHubClient.findByEmail(testEmail)).thenReturn(testCustomer);

        // 実行フェーズ
        CustomerTO result = customerService.authenticate(testEmail, "wrongpassword");

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNull(result);
        verify(customerHubClient, times(1)).findByEmail(testEmail);
    }

    @Test
    @DisplayName("存在しないメールアドレスで認証が失敗することをテストする")
    void testAuthenticateUserNotFound() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(customerHubClient.findByEmail("notfound@example.com")).thenReturn(null);

        // 実行フェーズ
        CustomerTO result = customerService.authenticate("notfound@example.com", testPassword);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNull(result);
        verify(customerHubClient, times(1)).findByEmail("notfound@example.com");
    }

    // getCustomerのテスト

    @Test
    @DisplayName("顧客IDで顧客情報を取得できることをテストする")
    void testGetCustomerSuccess() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer customerId = 1;
        when(customerHubClient.findById(customerId)).thenReturn(testCustomer);

        // 実行フェーズ
        CustomerTO result = customerService.getCustomer(customerId);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(customerId, result.customerId());
        assertEquals(testEmail, result.email());
        verify(customerHubClient, times(1)).findById(customerId);
    }

    @Test
    @DisplayName("存在しない顧客IDで取得時にnullが返されることをテストする")
    void testGetCustomerNotFound() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer customerId = 999;
        when(customerHubClient.findById(customerId)).thenReturn(null);

        // 実行フェーズ
        CustomerTO result = customerService.getCustomer(customerId);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNull(result);
        verify(customerHubClient, times(1)).findById(customerId);
    }
}
