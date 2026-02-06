package pro.kensait.berrybooks.external;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.berrybooks.external.dto.CustomerTO;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerHubRestClient単体テスト")
class CustomerHubRestClientTest {
    
    @Mock
    private Client client;
    
    @Mock
    private WebTarget baseTarget;
    
    @Mock
    private WebTarget pathTarget;
    
    @Mock
    private WebTarget queryTarget;
    
    @Mock
    private Invocation.Builder builder;
    
    @Mock
    private Response response;
    
    private CustomerHubRestClient restClient;
    
    @BeforeEach
    void setUp() throws Exception {
        restClient = new CustomerHubRestClient();
        
        // プライベートフィールドに値を設定
        setPrivateField(restClient, "baseUrl", "http://localhost:8080/customer-hub-api/customers");
        setPrivateField(restClient, "client", client);
        setPrivateField(restClient, "baseTarget", baseTarget);
    }
    
    @Test
    @DisplayName("メールアドレスで顧客を検索（正常系）")
    void testFindByEmail_Success() {
        // Given: モック設定
        CustomerTO expectedCustomer = new CustomerTO(
            1,
            "山田太郎",
            "$2a$10$hashedpassword",
            "test@example.com",
            LocalDate.of(1990, 1, 1),
            "東京都"
        );
        
        when(baseTarget.path("/query_email")).thenReturn(pathTarget);
        when(pathTarget.queryParam(eq("email"), anyString())).thenReturn(queryTarget);
        when(queryTarget.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.get()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(CustomerTO.class)).thenReturn(expectedCustomer);
        
        // When: メソッド呼び出し
        CustomerTO result = restClient.findByEmail("test@example.com");
        
        // Then: 検証
        assertNotNull(result);
        assertEquals(1, result.customerId());
        assertEquals("test@example.com", result.email());
        assertEquals("山田太郎", result.customerName());
        
        verify(baseTarget).path("/query_email");
        verify(pathTarget).queryParam("email", "test@example.com");
        verify(builder).get();
    }
    
    @Test
    @DisplayName("存在しないメールアドレスで検索（異常系）")
    void testFindByEmail_NotFound() {
        // Given: モック設定（404）
        when(baseTarget.path("/query_email")).thenReturn(pathTarget);
        when(pathTarget.queryParam(eq("email"), anyString())).thenReturn(queryTarget);
        when(queryTarget.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.get()).thenReturn(response);
        when(response.getStatus()).thenReturn(404);
        
        // When: メソッド呼び出し
        CustomerTO result = restClient.findByEmail("notfound@example.com");
        
        // Then: 検証
        assertNull(result);
        
        verify(baseTarget).path("/query_email");
        verify(pathTarget).queryParam("email", "notfound@example.com");
        verify(builder).get();
    }
    
    @Test
    @DisplayName("顧客IDで顧客情報を取得（正常系）")
    void testFindById_Success() {
        // Given: モック設定
        CustomerTO expectedCustomer = new CustomerTO(
            1,
            "山田太郎",
            "$2a$10$hashedpassword",
            "test@example.com",
            LocalDate.of(1990, 1, 1),
            "東京都"
        );
        
        when(baseTarget.path("/1")).thenReturn(pathTarget);
        when(pathTarget.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.get()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(CustomerTO.class)).thenReturn(expectedCustomer);
        
        // When: メソッド呼び出し
        CustomerTO result = restClient.findById(1);
        
        // Then: 検証
        assertNotNull(result);
        assertEquals(1, result.customerId());
        assertEquals("山田太郎", result.customerName());
        
        verify(baseTarget).path("/1");
        verify(builder).get();
    }
    
    @Test
    @DisplayName("存在しない顧客IDで検索（異常系）")
    void testFindById_NotFound() {
        // Given: モック設定（404）
        when(baseTarget.path("/999")).thenReturn(pathTarget);
        when(pathTarget.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.get()).thenReturn(response);
        when(response.getStatus()).thenReturn(404);
        
        // When: メソッド呼び出し
        CustomerTO result = restClient.findById(999);
        
        // Then: 検証
        assertNull(result);
        
        verify(baseTarget).path("/999");
        verify(builder).get();
    }
    
    @Test
    @DisplayName("新規顧客を登録（正常系）")
    void testRegister_Success() {
        // Given: モック設定
        CustomerTO newCustomer = new CustomerTO(
            null,
            "新規太郎",
            "password123",
            "new@example.com",
            LocalDate.of(1995, 5, 15),
            "大阪府"
        );
        
        CustomerTO createdCustomer = new CustomerTO(
            10,
            "新規太郎",
            "$2a$10$hashedpassword",
            "new@example.com",
            LocalDate.of(1995, 5, 15),
            "大阪府"
        );
        
        when(baseTarget.path("/")).thenReturn(pathTarget);
        when(pathTarget.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.post(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.readEntity(CustomerTO.class)).thenReturn(createdCustomer);
        
        // When: メソッド呼び出し
        CustomerTO result = restClient.register(newCustomer);
        
        // Then: 検証
        assertNotNull(result);
        assertEquals(10, result.customerId());
        assertEquals("新規太郎", result.customerName());
        assertEquals("new@example.com", result.email());
        
        verify(baseTarget).path("/");
        verify(builder).post(any());
    }
    
    @Test
    @DisplayName("重複したメールアドレスで登録（異常系）")
    void testRegister_EmailAlreadyExists() {
        // Given: モック設定（409 Conflict）
        CustomerTO duplicateCustomer = new CustomerTO(
            null,
            "重複太郎",
            "password123",
            "duplicate@example.com",
            LocalDate.of(1990, 1, 1),
            "東京都"
        );
        
        when(baseTarget.path("/")).thenReturn(pathTarget);
        when(pathTarget.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.post(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(409);
        
        // When/Then: 例外がスローされることを検証
        assertThrows(RuntimeException.class, () -> {
            restClient.register(duplicateCustomer);
        });
        
        verify(baseTarget).path("/");
        verify(builder).post(any());
    }
    
    // ユーティリティメソッド: プライベートフィールドに値を設定
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
