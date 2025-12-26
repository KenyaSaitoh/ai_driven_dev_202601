package pro.kensait.berrybooks.service.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import pro.kensait.berrybooks.dao.CategoryDao;
import pro.kensait.berrybooks.entity.Category;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryDao categoryDao;

    @InjectMocks
    private CategoryService categoryService;

    private List<Category> testCategoryList;
    private Category category1;
    private Category category2;
    private Category category3;

    @BeforeEach
    void setUp() {
        category1 = new Category();
        category1.setCategoryId(1);
        category1.setCategoryName("文学");

        category2 = new Category();
        category2.setCategoryId(2);
        category2.setCategoryName("ビジネス");

        category3 = new Category();
        category3.setCategoryId(3);
        category3.setCategoryName("技術書");

        testCategoryList = new ArrayList<>();
        testCategoryList.add(category1);
        testCategoryList.add(category2);
        testCategoryList.add(category3);
    }

    @Test
    @DisplayName("カテゴリの全件取得が正常に動作することをテストする")
    void testGetCategoriesAll() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(categoryDao.findAll()).thenReturn(testCategoryList);

        // 実行フェーズ
        List<Category> result = categoryService.getCategoriesAll();

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("文学", result.get(0).getCategoryName());
        assertEquals("ビジネス", result.get(1).getCategoryName());
        assertEquals("技術書", result.get(2).getCategoryName());
        verify(categoryDao, times(1)).findAll();
    }

    @Test
    @DisplayName("カテゴリが0件の場合に空のリストが返されることをテストする")
    void testGetCategoriesAllEmpty() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(categoryDao.findAll()).thenReturn(new ArrayList<>());

        // 実行フェーズ
        List<Category> result = categoryService.getCategoriesAll();

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(categoryDao, times(1)).findAll();
    }

    @Test
    @DisplayName("カテゴリ名をキーとしたマップが正しく生成されることをテストする")
    void testGetCategoryMap() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(categoryDao.findAll()).thenReturn(testCategoryList);

        // 実行フェーズ
        Map<String, Integer> result = categoryService.getCategoryMap();

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1, result.get("文学"));
        assertEquals(2, result.get("ビジネス"));
        assertEquals(3, result.get("技術書"));
        verify(categoryDao, times(1)).findAll();
    }

    @Test
    @DisplayName("カテゴリが0件の場合に空のマップが返されることをテストする")
    void testGetCategoryMapEmpty() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(categoryDao.findAll()).thenReturn(new ArrayList<>());

        // 実行フェーズ
        Map<String, Integer> result = categoryService.getCategoryMap();

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(categoryDao, times(1)).findAll();
    }

    @Test
    @DisplayName("カテゴリが1件のみの場合に正しくマップが生成されることをテストする")
    void testGetCategoryMapSingleItem() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        List<Category> singleItemList = new ArrayList<>();
        singleItemList.add(category1);
        when(categoryDao.findAll()).thenReturn(singleItemList);

        // 実行フェーズ
        Map<String, Integer> result = categoryService.getCategoryMap();

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get("文学"));
        verify(categoryDao, times(1)).findAll();
    }

    @Test
    @DisplayName("同じカテゴリ名が存在する場合に後者で上書きされることをテストする")
    void testGetCategoryMapDuplicateNames() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Category category4 = new Category();
        category4.setCategoryId(4);
        category4.setCategoryName("文学"); // 重複するカテゴリ名

        List<Category> listWithDuplicate = new ArrayList<>();
        listWithDuplicate.add(category1);
        listWithDuplicate.add(category4);

        when(categoryDao.findAll()).thenReturn(listWithDuplicate);

        // 実行フェーズ
        Map<String, Integer> result = categoryService.getCategoryMap();

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(1, result.size());
        // 後で追加されたものが上書きされる
        assertEquals(4, result.get("文学"));
        verify(categoryDao, times(1)).findAll();
    }
}
