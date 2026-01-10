package pro.kensait.backoffice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.backoffice.api.dto.BookTO;
import pro.kensait.backoffice.dao.BookDao;
import pro.kensait.backoffice.dao.BookDaoCriteria;
import pro.kensait.backoffice.entity.Book;
import pro.kensait.backoffice.entity.Category;
import pro.kensait.backoffice.entity.Publisher;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BookServiceの単体テスト
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookDao bookDao;

    @Mock
    private BookDaoCriteria bookDaoCriteria;

    @InjectMocks
    private BookService bookService;

    private Category testCategory;
    private Publisher testPublisher;
    private Book testBook1;
    private Book testBook2;

    @BeforeEach
    void setUp() {
        // テスト用カテゴリ
        testCategory = new Category(1, "文学");

        // テスト用出版社
        testPublisher = new Publisher(1, "出版社A");

        // テスト用書籍1
        testBook1 = new Book(
            1,
            "Java入門",
            "山田太郎",
            new BigDecimal("2500.00"),
            "http://example.com/image1.jpg",
            false,
            testCategory,
            testPublisher,
            10,
            1L
        );

        // テスト用書籍2
        testBook2 = new Book(
            2,
            "Python入門",
            "鈴木花子",
            new BigDecimal("3000.00"),
            "http://example.com/image2.jpg",
            false,
            testCategory,
            testPublisher,
            5,
            1L
        );
    }

    /**
     * テスト: 全書籍取得（正常系）
     */
    @Test
    void testGetBooksAll() {
        // Given
        List<Book> mockBooks = Arrays.asList(testBook1, testBook2);
        when(bookDao.findAll()).thenReturn(mockBooks);

        // When
        List<BookTO> result = bookService.getBooksAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        BookTO book1 = result.get(0);
        assertEquals(1, book1.getBookId());
        assertEquals("Java入門", book1.getBookName());
        assertEquals("山田太郎", book1.getAuthor());
        assertEquals(new BigDecimal("2500.00"), book1.getPrice());
        assertEquals(10, book1.getQuantity());
        assertEquals(1L, book1.getVersion());
        
        assertNotNull(book1.getCategory());
        assertEquals(1, book1.getCategory().getCategoryId());
        assertEquals("文学", book1.getCategory().getCategoryName());
        
        assertNotNull(book1.getPublisher());
        assertEquals(1, book1.getPublisher().getPublisherId());
        assertEquals("出版社A", book1.getPublisher().getPublisherName());

        // モックの呼び出し確認
        verify(bookDao, times(1)).findAll();
    }

    /**
     * テスト: 全書籍取得（0件）
     */
    @Test
    void testGetBooksAll_Empty() {
        // Given
        when(bookDao.findAll()).thenReturn(Collections.emptyList());

        // When
        List<BookTO> result = bookService.getBooksAll();

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());

        // モックの呼び出し確認
        verify(bookDao, times(1)).findAll();
    }

    /**
     * テスト: 書籍詳細取得（正常系）
     */
    @Test
    void testGetBook_Found() {
        // Given
        when(bookDao.findById(1)).thenReturn(testBook1);

        // When
        BookTO result = bookService.getBook(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getBookId());
        assertEquals("Java入門", result.getBookName());
        assertEquals("山田太郎", result.getAuthor());
        assertEquals(new BigDecimal("2500.00"), result.getPrice());

        // モックの呼び出し確認
        verify(bookDao, times(1)).findById(1);
    }

    /**
     * テスト: 書籍詳細取得（存在しない）
     */
    @Test
    void testGetBook_NotFound() {
        // Given
        when(bookDao.findById(999)).thenReturn(null);

        // When
        BookTO result = bookService.getBook(999);

        // Then
        assertNull(result);

        // モックの呼び出し確認
        verify(bookDao, times(1)).findById(999);
    }

    /**
     * テスト: カテゴリ別検索
     */
    @Test
    void testSearchBook_ByCategory() {
        // Given
        List<Book> mockBooks = Arrays.asList(testBook1, testBook2);
        when(bookDao.searchByJpql(1, null)).thenReturn(mockBooks);

        // When
        List<BookTO> result = bookService.searchBook(1);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // モックの呼び出し確認
        verify(bookDao, times(1)).searchByJpql(1, null);
    }

    /**
     * テスト: キーワード検索
     */
    @Test
    void testSearchBook_ByKeyword() {
        // Given
        List<Book> mockBooks = Collections.singletonList(testBook1);
        when(bookDao.searchByJpql(null, "Java")).thenReturn(mockBooks);

        // When
        List<BookTO> result = bookService.searchBook("Java");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java入門", result.get(0).getBookName());

        // モックの呼び出し確認
        verify(bookDao, times(1)).searchByJpql(null, "Java");
    }

    /**
     * テスト: カテゴリ + キーワード検索
     */
    @Test
    void testSearchBook_ByCategoryAndKeyword() {
        // Given
        List<Book> mockBooks = Collections.singletonList(testBook1);
        when(bookDao.searchByJpql(1, "Java")).thenReturn(mockBooks);

        // When
        List<BookTO> result = bookService.searchBook(1, "Java");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java入門", result.get(0).getBookName());
        assertEquals(1, result.get(0).getCategory().getCategoryId());

        // モックの呼び出し確認
        verify(bookDao, times(1)).searchByJpql(1, "Java");
    }

    /**
     * テスト: 検索結果が0件
     */
    @Test
    void testSearchBook_NoResults() {
        // Given
        when(bookDao.searchByJpql(null, "NotExist")).thenReturn(Collections.emptyList());

        // When
        List<BookTO> result = bookService.searchBook("NotExist");

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());

        // モックの呼び出し確認
        verify(bookDao, times(1)).searchByJpql(null, "NotExist");
    }

    /**
     * テスト: Criteria API検索
     */
    @Test
    void testSearchBookWithCriteria() {
        // Given
        List<Book> mockBooks = Collections.singletonList(testBook1);
        when(bookDaoCriteria.searchByCriteria(1, "Java")).thenReturn(mockBooks);

        // When
        List<BookTO> result = bookService.searchBookWithCriteria(1, "Java");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java入門", result.get(0).getBookName());

        // モックの呼び出し確認
        verify(bookDaoCriteria, times(1)).searchByCriteria(1, "Java");
    }

    /**
     * テスト: Criteria API検索（条件なし）
     */
    @Test
    void testSearchBookWithCriteria_NoCondition() {
        // Given
        List<Book> mockBooks = Arrays.asList(testBook1, testBook2);
        when(bookDaoCriteria.searchByCriteria(null, null)).thenReturn(mockBooks);

        // When
        List<BookTO> result = bookService.searchBookWithCriteria(null, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // モックの呼び出し確認
        verify(bookDaoCriteria, times(1)).searchByCriteria(null, null);
    }

    /**
     * テスト: カテゴリ情報がnullの場合
     */
    @Test
    void testConvertToTO_NullCategory() {
        // Given
        Book bookWithoutCategory = new Book(
            3,
            "書籍名",
            "著者名",
            new BigDecimal("1000.00"),
            "http://example.com/image.jpg",
            false,
            null,  // カテゴリなし
            testPublisher,
            10,
            1L
        );
        when(bookDao.findById(3)).thenReturn(bookWithoutCategory);

        // When
        BookTO result = bookService.getBook(3);

        // Then
        assertNotNull(result);
        assertNull(result.getCategory());
        assertNotNull(result.getPublisher());
    }

    /**
     * テスト: 出版社情報がnullの場合
     */
    @Test
    void testConvertToTO_NullPublisher() {
        // Given
        Book bookWithoutPublisher = new Book(
            4,
            "書籍名",
            "著者名",
            new BigDecimal("1000.00"),
            "http://example.com/image.jpg",
            false,
            testCategory,
            null,  // 出版社なし
            10,
            1L
        );
        when(bookDao.findById(4)).thenReturn(bookWithoutPublisher);

        // When
        BookTO result = bookService.getBook(4);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCategory());
        assertNull(result.getPublisher());
    }
}
