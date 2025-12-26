package pro.kensait.berrybooks.service.book;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pro.kensait.berrybooks.dao.BookDao;
import pro.kensait.berrybooks.entity.Book;
import pro.kensait.berrybooks.entity.Category;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookDao bookDao;

    @InjectMocks
    private BookService bookService;

    private Book testBook1;
    private Book testBook2;
    private Book testBook3;
    private List<Book> testBookList;

    @BeforeEach
    void setUp() {
        Category category1 = new Category();
        category1.setCategoryId(1);
        category1.setCategoryName("技術書");
        
        Category category2 = new Category();
        category2.setCategoryId(2);
        category2.setCategoryName("ビジネス");
        
        testBook1 = new Book();
        testBook1.setBookId(1);
        testBook1.setBookName("Java入門");
        testBook1.setPrice(new BigDecimal("2800"));
        testBook1.setCategory(category1);

        testBook2 = new Book();
        testBook2.setBookId(2);
        testBook2.setBookName("Spring Boot実践");
        testBook2.setPrice(new BigDecimal("3500"));
        testBook2.setCategory(category1);

        testBook3 = new Book();
        testBook3.setBookId(3);
        testBook3.setBookName("ビジネスマナー");
        testBook3.setPrice(new BigDecimal("1800"));
        testBook3.setCategory(category2);

        testBookList = new ArrayList<>();
        testBookList.add(testBook1);
        testBookList.add(testBook2);
        testBookList.add(testBook3);
    }

    // getBookのテスト

    @Test
    @DisplayName("書籍IDで書籍情報を取得できることをテストする")
    void testGetBookSuccess() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer bookId = 1;
        when(bookDao.findById(bookId)).thenReturn(testBook1);

        // 実行フェーズ
        Book result = bookService.getBook(bookId);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(bookId, result.getBookId());
        assertEquals("Java入門", result.getBookName());
        verify(bookDao, times(1)).findById(bookId);
    }

    @Test
    @DisplayName("存在しない書籍IDで例外がスローされることをテストする")
    void testGetBookNotFound() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer bookId = 999;
        when(bookDao.findById(bookId)).thenReturn(null);

        // 実行フェーズと検証フェーズ（出力値ベース、コミュニケーションベース）
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookService.getBook(bookId);
        });
        assertTrue(exception.getMessage().contains("Book not found"));
        verify(bookDao, times(1)).findById(bookId);
    }

    @Test
    @DisplayName("書籍IDがnullの場合に例外がスローされることをテストする")
    void testGetBookNullId() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(bookDao.findById(null)).thenReturn(null);

        // 実行フェーズと検証フェーズ（出力値ベース、コミュニケーションベース）
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookService.getBook(null);
        });
        assertTrue(exception.getMessage().contains("Book not found"));
        verify(bookDao, times(1)).findById(null);
    }

    // getBooksAllのテスト

    @Test
    @DisplayName("全書籍を取得できることをテストする")
    void testGetBooksAll() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(bookDao.findAll()).thenReturn(testBookList);

        // 実行フェーズ
        List<Book> result = bookService.getBooksAll();

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Java入門", result.get(0).getBookName());
        assertEquals("Spring Boot実践", result.get(1).getBookName());
        assertEquals("ビジネスマナー", result.get(2).getBookName());
        verify(bookDao, times(1)).findAll();
    }

    @Test
    @DisplayName("書籍が0件の場合に空のリストが返されることをテストする")
    void testGetBooksAllEmpty() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        when(bookDao.findAll()).thenReturn(new ArrayList<>());

        // 実行フェーズ
        List<Book> result = bookService.getBooksAll();

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(bookDao, times(1)).findAll();
    }

    // searchBook(Integer categoryId, String keyword)のテスト

    @Test
    @DisplayName("カテゴリIDとキーワードで書籍を検索できることをテストする")
    void testSearchBookByCategoryAndKeyword() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer categoryId = 1;
        String keyword = "Java";
        List<Book> searchResults = new ArrayList<>();
        searchResults.add(testBook1);
        
        when(bookDao.query(categoryId, "%" + keyword + "%")).thenReturn(searchResults);

        // 実行フェーズ
        List<Book> result = bookService.searchBook(categoryId, keyword);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java入門", result.get(0).getBookName());
        verify(bookDao, times(1)).query(categoryId, "%" + keyword + "%");
    }

    @Test
    @DisplayName("カテゴリIDとキーワードの検索で結果が0件の場合に空のリストが返されることをテストする")
    void testSearchBookByCategoryAndKeywordNoResults() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer categoryId = 1;
        String keyword = "存在しないキーワード";
        
        when(bookDao.query(categoryId, "%" + keyword + "%")).thenReturn(new ArrayList<>());

        // 実行フェーズ
        List<Book> result = bookService.searchBook(categoryId, keyword);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(bookDao, times(1)).query(categoryId, "%" + keyword + "%");
    }

    // searchBook(Integer categoryId)のテスト

    @Test
    @DisplayName("カテゴリIDで書籍を検索できることをテストする")
    void testSearchBookByCategoryId() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer categoryId = 1;
        List<Book> searchResults = new ArrayList<>();
        searchResults.add(testBook1);
        searchResults.add(testBook2);
        
        when(bookDao.queryByCategory(categoryId)).thenReturn(searchResults);

        // 実行フェーズ
        List<Book> result = bookService.searchBook(categoryId);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Java入門", result.get(0).getBookName());
        assertEquals("Spring Boot実践", result.get(1).getBookName());
        verify(bookDao, times(1)).queryByCategory(categoryId);
    }

    @Test
    @DisplayName("カテゴリIDの検索で結果が0件の場合に空のリストが返されることをテストする")
    void testSearchBookByCategoryIdNoResults() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer categoryId = 999;
        
        when(bookDao.queryByCategory(categoryId)).thenReturn(new ArrayList<>());

        // 実行フェーズ
        List<Book> result = bookService.searchBook(categoryId);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(bookDao, times(1)).queryByCategory(categoryId);
    }

    // searchBook(String keyword)のテスト

    @Test
    @DisplayName("キーワードで書籍を検索できることをテストする")
    void testSearchBookByKeyword() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String keyword = "Java";
        List<Book> searchResults = new ArrayList<>();
        searchResults.add(testBook1);
        
        when(bookDao.queryByKeyword("%" + keyword + "%")).thenReturn(searchResults);

        // 実行フェーズ
        List<Book> result = bookService.searchBook(keyword);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java入門", result.get(0).getBookName());
        verify(bookDao, times(1)).queryByKeyword("%" + keyword + "%");
    }

    @Test
    @DisplayName("キーワードの検索で結果が0件の場合に空のリストが返されることをテストする")
    void testSearchBookByKeywordNoResults() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String keyword = "存在しないキーワード";
        
        when(bookDao.queryByKeyword("%" + keyword + "%")).thenReturn(new ArrayList<>());

        // 実行フェーズ
        List<Book> result = bookService.searchBook(keyword);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(bookDao, times(1)).queryByKeyword("%" + keyword + "%");
    }

    @Test
    @DisplayName("空文字列のキーワードで検索が実行されることをテストする")
    void testSearchBookByEmptyKeyword() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        String keyword = "";
        
        when(bookDao.queryByKeyword("%" + keyword + "%")).thenReturn(testBookList);

        // 実行フェーズ
        List<Book> result = bookService.searchBook(keyword);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(bookDao, times(1)).queryByKeyword("%" + keyword + "%");
    }

    // searchBookWithCriteriaのテスト

    @Test
    @DisplayName("動的クエリでカテゴリIDとキーワードを使って書籍を検索できることをテストする")
    void testSearchBookWithCriteria() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer categoryId = 1;
        String keyword = "Java";
        List<Book> searchResults = new ArrayList<>();
        searchResults.add(testBook1);
        
        when(bookDao.searchWithCriteria(categoryId, "%" + keyword + "%")).thenReturn(searchResults);

        // 実行フェーズ
        List<Book> result = bookService.searchBookWithCriteria(categoryId, keyword);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java入門", result.get(0).getBookName());
        verify(bookDao, times(1)).searchWithCriteria(categoryId, "%" + keyword + "%");
    }

    @Test
    @DisplayName("動的クエリでカテゴリIDのみを指定して検索できることをテストする")
    void testSearchBookWithCriteriaCategoryOnly() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer categoryId = 1;
        String keyword = null;
        List<Book> searchResults = new ArrayList<>();
        searchResults.add(testBook1);
        searchResults.add(testBook2);
        
        when(bookDao.searchWithCriteria(categoryId, null)).thenReturn(searchResults);

        // 実行フェーズ
        List<Book> result = bookService.searchBookWithCriteria(categoryId, keyword);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookDao, times(1)).searchWithCriteria(categoryId, null);
    }

    @Test
    @DisplayName("動的クエリでキーワードが空文字列の場合にnullとして処理されることをテストする")
    void testSearchBookWithCriteriaEmptyKeyword() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer categoryId = 1;
        String keyword = "";
        List<Book> searchResults = new ArrayList<>();
        searchResults.add(testBook1);
        searchResults.add(testBook2);
        
        when(bookDao.searchWithCriteria(categoryId, null)).thenReturn(searchResults);

        // 実行フェーズ
        List<Book> result = bookService.searchBookWithCriteria(categoryId, keyword);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookDao, times(1)).searchWithCriteria(categoryId, null);
    }

    @Test
    @DisplayName("動的クエリで検索条件がnullの場合に検索が実行されることをテストする")
    void testSearchBookWithCriteriaNullConditions() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer categoryId = null;
        String keyword = null;
        
        when(bookDao.searchWithCriteria(null, null)).thenReturn(testBookList);

        // 実行フェーズ
        List<Book> result = bookService.searchBookWithCriteria(categoryId, keyword);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(bookDao, times(1)).searchWithCriteria(null, null);
    }

    @Test
    @DisplayName("動的クエリで検索結果が0件の場合に空のリストが返されることをテストする")
    void testSearchBookWithCriteriaNoResults() {
        // 準備フェーズ（テストフィクスチャのセットアップ）
        Integer categoryId = 1;
        String keyword = "存在しないキーワード";
        
        when(bookDao.searchWithCriteria(categoryId, "%" + keyword + "%")).thenReturn(new ArrayList<>());

        // 実行フェーズ
        List<Book> result = bookService.searchBookWithCriteria(categoryId, keyword);

        // 検証フェーズ（出力値ベース、コミュニケーションベース）
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(bookDao, times(1)).searchWithCriteria(categoryId, "%" + keyword + "%");
    }
}
