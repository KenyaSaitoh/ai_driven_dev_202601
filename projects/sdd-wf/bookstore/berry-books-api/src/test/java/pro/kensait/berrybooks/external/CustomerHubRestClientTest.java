package pro.kensait.berrybooks.external;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import pro.kensait.berrybooks.external.dto.CustomerTO;

/**
 * CustomerHubRestClient 単体テスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerHubRestClient 単体テスト")
class CustomerHubRestClientTest {

    @Mock
    private CustomerHubApi customerHubApi;

    @InjectMocks
    private CustomerHubRestClient customerHubClient;

    @BeforeEach
    void setUp() {
        // モックの初期化は@ExtendWith(MockitoExtension.class)で自動実行
    }

    @Test
    @DisplayName("メールアドレスで顧客を検索")
    void testFindByEmail_Success() {
        // Given: モック設定
        CustomerTO customer = new CustomerTO();
        customer.setCustomerId(1);
        customer.setCustomerName("山田太郎");
        customer.setEmail("yamada@example.com");

        when(customerHubApi.findByEmail("yamada@example.com")).thenReturn(customer);

        // When
        CustomerTO result = customerHubClient.findByEmail("yamada@example.com");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCustomerId());
        assertEquals("山田太郎", result.getCustomerName());
        assertEquals("yamada@example.com", result.getEmail());
        verify(customerHubApi, times(1)).findByEmail("yamada@example.com");
    }

    @Test
    @DisplayName("新規顧客を登録")
    void testCreateCustomer_Success() {
        // Given: モック設定
        CustomerTO requestCustomer = new CustomerTO();
        requestCustomer.setCustomerName("鈴木花子");
        requestCustomer.setEmail("suzuki@example.com");
        requestCustomer.setPassword("hashedPassword");

        CustomerTO createdCustomer = new CustomerTO();
        createdCustomer.setCustomerId(2);
        createdCustomer.setCustomerName("鈴木花子");
        createdCustomer.setEmail("suzuki@example.com");

        when(customerHubApi.createCustomer(requestCustomer)).thenReturn(createdCustomer);

        // When
        CustomerTO result = customerHubClient.createCustomer(requestCustomer);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getCustomerId());
        assertEquals("鈴木花子", result.getCustomerName());
        assertEquals("suzuki@example.com", result.getEmail());
        verify(customerHubApi, times(1)).createCustomer(requestCustomer);
    }

    @Test
    @DisplayName("存在しない顧客を検索（404 Not Found）")
    void testFindByEmail_NotFound() {
        // Given: 404エラーのモック設定
        Response mockResponse = Response.status(Response.Status.NOT_FOUND)
            .entity("{\"error\":\"Customer not found\"}")
            .build();

        when(customerHubApi.findByEmail("notfound@example.com"))
            .thenThrow(new WebApplicationException(mockResponse));

        // When & Then: WebApplicationExceptionがスローされる
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            customerHubClient.findByEmail("notfound@example.com");
        });

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), exception.getResponse().getStatus());
        verify(customerHubApi, times(1)).findByEmail("notfound@example.com");
    }

    @Test
    @DisplayName("メールアドレス重複で顧客登録失敗（409 Conflict）")
    void testCreateCustomer_EmailDuplicate() {
        // Given: メール重複エラーのモック設定
        CustomerTO requestCustomer = new CustomerTO();
        requestCustomer.setCustomerName("鈴木花子");
        requestCustomer.setEmail("yamada@example.com"); // 既存のメールアドレス
        requestCustomer.setPassword("hashedPassword");

        Response mockResponse = Response.status(Response.Status.CONFLICT)
            .entity("{\"error\":\"Email already exists\"}")
            .build();

        when(customerHubApi.createCustomer(requestCustomer))
            .thenThrow(new WebApplicationException(mockResponse));

        // When & Then: WebApplicationExceptionがスローされる
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            customerHubClient.createCustomer(requestCustomer);
        });

        assertEquals(Response.Status.CONFLICT.getStatusCode(), exception.getResponse().getStatus());
        verify(customerHubApi, times(1)).createCustomer(requestCustomer);
    }
}
