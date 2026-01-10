package pro.kensait.backoffice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.backoffice.api.dto.CategoryTO;
import pro.kensait.backoffice.dao.CategoryDao;
import pro.kensait.backoffice.entity.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CategoryServiceの単体テスト
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryDao categoryDao;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory1;
    private Category testCategory2;
    private Category testCategory3;
    private Category testCategory4;

    @BeforeEach
    void setUp() {
        // テスト用カテゴリ
        testCategory1 = new Category(1, "文学");
        testCategory2 = new Category(2, "ビジネス");
        testCategory3 = new Category(3, "技術");
        testCategory4 = new Category(4, "歴史");
    }

    /**
     * テスト: 全カテゴリ取得（正常系）
     */
    @Test
    void testGetAllCategories() {
        // Given
        List<Category> mockCategories = Arrays.asList(
            testCategory1,
            testCategory2,
            testCategory3,
            testCategory4
        );
        when(categoryDao.findAll()).thenReturn(mockCategories);

        // When
        List<CategoryTO> result = categoryService.getAllCategories();

        // Then
        assertNotNull(result);
        assertEquals(4, result.size());
        
        // 1つ目のカテゴリを確認
        CategoryTO category1 = result.get(0);
        assertEquals(1, category1.categoryId());
        assertEquals("文学", category1.categoryName());
        
        // 2つ目のカテゴリを確認
        CategoryTO category2 = result.get(1);
        assertEquals(2, category2.categoryId());
        assertEquals("ビジネス", category2.categoryName());
        
        // 3つ目のカテゴリを確認
        CategoryTO category3 = result.get(2);
        assertEquals(3, category3.categoryId());
        assertEquals("技術", category3.categoryName());
        
        // 4つ目のカテゴリを確認
        CategoryTO category4 = result.get(3);
        assertEquals(4, category4.categoryId());
        assertEquals("歴史", category4.categoryName());

        // モックの呼び出し確認
        verify(categoryDao, times(1)).findAll();
    }

    /**
     * テスト: 全カテゴリ取得（0件）
     */
    @Test
    void testGetAllCategories_Empty() {
        // Given
        when(categoryDao.findAll()).thenReturn(Collections.emptyList());

        // When
        List<CategoryTO> result = categoryService.getAllCategories();

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());

        // モックの呼び出し確認
        verify(categoryDao, times(1)).findAll();
    }

    /**
     * テスト: カテゴリ詳細取得（正常系）
     */
    @Test
    void testGetCategoryById_Found() {
        // Given
        when(categoryDao.findById(1)).thenReturn(testCategory1);

        // When
        CategoryTO result = categoryService.getCategoryById(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.categoryId());
        assertEquals("文学", result.categoryName());

        // モックの呼び出し確認
        verify(categoryDao, times(1)).findById(1);
    }

    /**
     * テスト: カテゴリ詳細取得（存在しない）
     */
    @Test
    void testGetCategoryById_NotFound() {
        // Given
        when(categoryDao.findById(999)).thenReturn(null);

        // When
        CategoryTO result = categoryService.getCategoryById(999);

        // Then
        assertNull(result);

        // モックの呼び出し確認
        verify(categoryDao, times(1)).findById(999);
    }

    /**
     * テスト: 変換メソッド（nullチェック）
     */
    @Test
    void testConvertToTO_Null() {
        // Given
        when(categoryDao.findById(null)).thenReturn(null);

        // When
        CategoryTO result = categoryService.getCategoryById(null);

        // Then
        assertNull(result);
    }

    /**
     * テスト: 全カテゴリ取得（順序確認）
     */
    @Test
    void testGetAllCategories_Order() {
        // Given
        List<Category> mockCategories = Arrays.asList(
            testCategory1,  // ID: 1
            testCategory2,  // ID: 2
            testCategory3,  // ID: 3
            testCategory4   // ID: 4
        );
        when(categoryDao.findAll()).thenReturn(mockCategories);

        // When
        List<CategoryTO> result = categoryService.getAllCategories();

        // Then
        assertNotNull(result);
        assertEquals(4, result.size());
        
        // categoryId昇順であることを確認
        assertEquals(1, result.get(0).categoryId());
        assertEquals(2, result.get(1).categoryId());
        assertEquals(3, result.get(2).categoryId());
        assertEquals(4, result.get(3).categoryId());

        // モックの呼び出し確認
        verify(categoryDao, times(1)).findAll();
    }

    /**
     * テスト: カテゴリ名が日本語
     */
    @Test
    void testGetCategoryById_JapaneseName() {
        // Given
        when(categoryDao.findById(1)).thenReturn(testCategory1);

        // When
        CategoryTO result = categoryService.getCategoryById(1);

        // Then
        assertNotNull(result);
        assertEquals("文学", result.categoryName());
        assertTrue(result.categoryName().matches("^[\\p{Script=Han}\\p{Script=Hiragana}\\p{Script=Katakana}]+$"));

        // モックの呼び出し確認
        verify(categoryDao, times(1)).findById(1);
    }
}
