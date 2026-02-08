package pro.kensait.berrybooks.api;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.berrybooks.external.BackOfficeRestClient;
import pro.kensait.berrybooks.external.dto.CategoryTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CategoryResourceの単体テスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryResource 単体テスト")
class CategoryResourceTest {

    @Mock
    private BackOfficeRestClient backOfficeClient;

    @InjectMocks
    private CategoryResource categoryResource;

    private List<CategoryTO> sampleCategories;

    @BeforeEach
    void setUp() {
        // Given: テストデータの準備
        sampleCategories = new ArrayList<>();

        CategoryTO category1 = new CategoryTO();
        category1.setCategoryId(1);
        category1.setCategoryName("プログラミング");
        sampleCategories.add(category1);

        CategoryTO category2 = new CategoryTO();
        category2.setCategoryId(2);
        category2.setCategoryName("データベース");
        sampleCategories.add(category2);

        CategoryTO category3 = new CategoryTO();
        category3.setCategoryId(3);
        category3.setCategoryName("ネットワーク");
        sampleCategories.add(category3);
    }

    // ============================================================
    // 2.5 CategoryResource - カテゴリ一覧取得
    // ============================================================

    @Test
    @DisplayName("カテゴリ一覧を正常に取得")
    void testGetAllCategories_Success() {
        // Given: BackOfficeRestClientがモック化されている
        //        モック設定: findAllCategories()がカテゴリリストを返す
        when(backOfficeClient.findAllCategories()).thenReturn(sampleCategories);

        // When: CategoryResource.getAllCategories()を呼び出す
        Response response = categoryResource.getAllCategories();

        // Then: HTTPステータス200 OKが返される
        //       レスポンスボディにMap<String, Integer>が含まれる
        //       キーがカテゴリ名、値がカテゴリIDである
        //       BackOfficeRestClient.findAllCategories()が1回呼ばれる
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Integer> categoryMap = (Map<String, Integer>) response.getEntity();

        assertEquals(3, categoryMap.size());
        assertEquals(1, categoryMap.get("プログラミング"));
        assertEquals(2, categoryMap.get("データベース"));
        assertEquals(3, categoryMap.get("ネットワーク"));

        verify(backOfficeClient, times(1)).findAllCategories();
    }

    @Test
    @DisplayName("外部API呼び出し失敗時（カテゴリ一覧取得）")
    void testGetAllCategories_ExternalApiFailure() {
        // Given: BackOfficeRestClientがモック化されている
        //        モック設定: findAllCategories()がRuntimeExceptionをスローする
        when(backOfficeClient.findAllCategories())
            .thenThrow(new RuntimeException("External API Error"));

        // When & Then: CategoryResource.getAllCategories()を呼び出すとRuntimeExceptionがスローされる
        //              ExceptionMapperで500 Internal Server Errorに変換される
        assertThrows(RuntimeException.class, () -> categoryResource.getAllCategories());
    }

    @Test
    @DisplayName("カテゴリが空の場合")
    void testGetAllCategories_EmptyList() {
        // Given: BackOfficeRestClientがモック化されている
        //        モック設定: findAllCategories()が空のリストを返す
        when(backOfficeClient.findAllCategories()).thenReturn(new ArrayList<>());

        // When: CategoryResource.getAllCategories()を呼び出す
        Response response = categoryResource.getAllCategories();

        // Then: HTTPステータス200 OKが返される
        //       レスポンスボディに空のMapが含まれる
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        Map<String, Integer> categoryMap = (Map<String, Integer>) response.getEntity();
        assertTrue(categoryMap.isEmpty());
    }
}
