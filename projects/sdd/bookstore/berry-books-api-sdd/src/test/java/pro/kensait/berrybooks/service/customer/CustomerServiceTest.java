package pro.kensait.berrybooks.service.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.berrybooks.client.customer.CustomerRestClient;
import pro.kensait.berrybooks.entity.Customer;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CustomerServiceのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    
    @Mock
    private CustomerRestClient customerRestClient;
    
    @InjectMocks
    private CustomerService customerService;
    
    private Customer testCustomer;
    
    @BeforeEach
    void setUp() {
        // テスト用の顧客データを準備
        testCustomer = new Customer();
        testCustomer.setCustomerId(1);
        testCustomer.setCustomerName("Alice");
        testCustomer.setEmail("alice@gmail.com");
        testCustomer.setPassword("password123");
        testCustomer.setBirthday(LocalDate.of(1990, 1, 1));
        testCustomer.setAddress("東京都渋谷区神南1-1-1");
    }
    
    /**
     * 顧客登録（正常系）
     * 
     * <p>正常に顧客が登録できることを確認</p>
     */
    @Test
    void testRegister_Success() {
        // Given: REST APIが正常に顧客を登録
        when(customerRestClient.registerCustomer(any(Customer.class))).thenReturn(testCustomer);
        
        // When: 顧客を登録
        Customer newCustomer = new Customer();
        newCustomer.setCustomerName("Alice");
        newCustomer.setEmail("alice@gmail.com");
        newCustomer.setPassword("password123");
        
        Customer result = customerService.register(newCustomer);
        
        // Then: 登録された顧客が返される
        assertNotNull(result);
        assertEquals(1, result.getCustomerId());
        assertEquals("Alice", result.getCustomerName());
        assertEquals("alice@gmail.com", result.getEmail());
        
        // REST APIが呼ばれたことを確認
        verify(customerRestClient, times(1)).registerCustomer(any(Customer.class));
    }
    
    /**
     * 顧客登録（異常系：メールアドレス重複）
     * 
     * <p>メールアドレスが既に登録されている場合、EmailAlreadyExistsExceptionがスローされることを確認</p>
     */
    @Test
    void testRegister_EmailAlreadyExists() {
        // Given: REST APIがメールアドレス重複エラーをスロー
        when(customerRestClient.registerCustomer(any(Customer.class)))
                .thenThrow(new EmailAlreadyExistsException("alice@gmail.com"));
        
        // When & Then: EmailAlreadyExistsExceptionがスローされる
        Customer newCustomer = new Customer();
        newCustomer.setCustomerName("Alice");
        newCustomer.setEmail("alice@gmail.com");
        newCustomer.setPassword("password123");
        
        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> customerService.register(newCustomer)
        );
        
        assertEquals("alice@gmail.com", exception.getEmail());
        
        // REST APIが呼ばれたことを確認
        verify(customerRestClient, times(1)).registerCustomer(any(Customer.class));
    }
    
    /**
     * 認証（正常系）
     * 
     * <p>正しいメールアドレスとパスワードで認証に成功することを確認</p>
     */
    @Test
    void testAuthenticate_Success() {
        // Given: REST APIが顧客情報を返す
        when(customerRestClient.getCustomerByEmail("alice@gmail.com")).thenReturn(testCustomer);
        
        // When: 認証を実行
        Customer result = customerService.authenticate("alice@gmail.com", "password123");
        
        // Then: 認証に成功し、顧客情報が返される
        assertNotNull(result);
        assertEquals(1, result.getCustomerId());
        assertEquals("Alice", result.getCustomerName());
        assertEquals("alice@gmail.com", result.getEmail());
        
        // REST APIが呼ばれたことを確認
        verify(customerRestClient, times(1)).getCustomerByEmail("alice@gmail.com");
    }
    
    /**
     * 認証（異常系：顧客が見つからない）
     * 
     * <p>メールアドレスが存在しない場合、nullが返されることを確認</p>
     */
    @Test
    void testAuthenticate_CustomerNotFound() {
        // Given: REST APIが顧客を見つけられない
        when(customerRestClient.getCustomerByEmail("notfound@gmail.com")).thenReturn(null);
        
        // When: 認証を実行
        Customer result = customerService.authenticate("notfound@gmail.com", "password123");
        
        // Then: nullが返される
        assertNull(result);
        
        // REST APIが呼ばれたことを確認
        verify(customerRestClient, times(1)).getCustomerByEmail("notfound@gmail.com");
    }
    
    /**
     * 認証（異常系：パスワード不一致）
     * 
     * <p>パスワードが一致しない場合、nullが返されることを確認</p>
     */
    @Test
    void testAuthenticate_PasswordMismatch() {
        // Given: REST APIが顧客情報を返す
        when(customerRestClient.getCustomerByEmail("alice@gmail.com")).thenReturn(testCustomer);
        
        // When: 誤ったパスワードで認証を実行
        Customer result = customerService.authenticate("alice@gmail.com", "wrongpassword");
        
        // Then: nullが返される
        assertNull(result);
        
        // REST APIが呼ばれたことを確認
        verify(customerRestClient, times(1)).getCustomerByEmail("alice@gmail.com");
    }
    
    /**
     * メールアドレスで顧客を検索（正常系）
     * 
     * <p>メールアドレスで顧客が検索できることを確認</p>
     */
    @Test
    void testFindByEmail_Success() {
        // Given: REST APIが顧客情報を返す
        when(customerRestClient.getCustomerByEmail("alice@gmail.com")).thenReturn(testCustomer);
        
        // When: メールアドレスで検索
        Customer result = customerService.findByEmail("alice@gmail.com");
        
        // Then: 顧客情報が返される
        assertNotNull(result);
        assertEquals(1, result.getCustomerId());
        assertEquals("Alice", result.getCustomerName());
        assertEquals("alice@gmail.com", result.getEmail());
        
        // REST APIが呼ばれたことを確認
        verify(customerRestClient, times(1)).getCustomerByEmail("alice@gmail.com");
    }
    
    /**
     * メールアドレスで顧客を検索（異常系：顧客が見つからない）
     * 
     * <p>顧客が見つからない場合、nullが返されることを確認</p>
     */
    @Test
    void testFindByEmail_NotFound() {
        // Given: REST APIが顧客を見つけられない
        when(customerRestClient.getCustomerByEmail("notfound@gmail.com")).thenReturn(null);
        
        // When: メールアドレスで検索
        Customer result = customerService.findByEmail("notfound@gmail.com");
        
        // Then: nullが返される
        assertNull(result);
        
        // REST APIが呼ばれたことを確認
        verify(customerRestClient, times(1)).getCustomerByEmail("notfound@gmail.com");
    }
}

