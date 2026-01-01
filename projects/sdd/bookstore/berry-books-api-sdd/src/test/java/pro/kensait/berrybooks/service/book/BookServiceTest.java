package pro.kensait.berrybooks.service.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.kensait.berrybooks.dao.BookDao;
import pro.kensait.berrybooks.entity.Book;
import pro.kensait.berrybooks.entity.Category;
import pro.kensait.berrybooks.entity.Publisher;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * BookServiceのユニットテストクラス
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    
    @Mock
    private BookDao bookDao;
    
    @InjectMocks
    private BookService bookService;
    
    private List<Book> testBooks;
    
    @BeforeEach
    void setUp() {
        // テストデータの準備
        testBooks = new ArrayList<>();
        
        Category category = new Category();
        category.setCategoryId(1);
        category.setCategoryName("Java");
        
        Publisher publisher = new Publisher();
        publisher.setPublisherId(1);
        publisher.setPublisherName("Test Publisher");
        
        Book book1 = new Book();
        book1.setBookId(1);
        book1.setBookName("Java SE ディープダイブ");
        book1.setAuthor("Michael Johnson");
        book1.setPrice(3400);
        book1.setCategory(category);
        book1.setPublisher(publisher);
        
        Book book2 = new Book();
        book2.setBookId(2);
        book2.setBookName("JVM とバイトコードの探求");
        book2.setAuthor("James Lopez");
        book2.setPrice(4200);
        book2.setCategory(category);
        book2.setPublisher(publisher);
        
        testBooks.add(book1);
        testBooks.add(book2);
    }
    
    /**
     * カテゴリとキーワードで検索（正常系）
     */
    @Test
    void testSearchBookWithCategoryAndKeyword() {
        // Arrange
        SearchParam searchParam = new SearchParam();
        searchParam.setCategoryId(1);
        searchParam.setKeyword("Java");
        
        when(bookDao.findByCategoryAndKeyword(1, "Java")).thenReturn(testBooks);
        
        // Act
        List<Book> result = bookService.searchBook(searchParam);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Java SE ディープダイブ", result.get(0).getBookName());
        verify(bookDao, times(1)).findByCategoryAndKeyword(1, "Java");
    }
    
    /**
     * キーワードのみで検索（正常系）
     */
    @Test
    void testSearchBookWithKeywordOnly() {
        // Arrange
        SearchParam searchParam = new SearchParam();
        searchParam.setKeyword("Java");
        
        when(bookDao.findByKeyword("Java")).thenReturn(testBooks);
        
        // Act
        List<Book> result = bookService.searchBook(searchParam);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookDao, times(1)).findByKeyword("Java");
    }
    
    /**
     * カテゴリのみで検索（正常系）
     */
    @Test
    void testSearchBookWithCategoryOnly() {
        // Arrange
        SearchParam searchParam = new SearchParam();
        searchParam.setCategoryId(1);
        
        when(bookDao.findByCategory(1)).thenReturn(testBooks);
        
        // Act
        List<Book> result = bookService.searchBook(searchParam);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookDao, times(1)).findByCategory(1);
    }
    
    /**
     * 全書籍取得（正常系）
     */
    @Test
    void testSearchBookWithoutParams() {
        // Arrange
        SearchParam searchParam = new SearchParam();
        
        when(bookDao.findAll()).thenReturn(testBooks);
        
        // Act
        List<Book> result = bookService.searchBook(searchParam);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookDao, times(1)).findAll();
    }
    
    /**
     * 検索結果0件（正常系）
     */
    @Test
    void testSearchBookNoResults() {
        // Arrange
        SearchParam searchParam = new SearchParam();
        searchParam.setKeyword("存在しない書籍");
        
        when(bookDao.findByKeyword("存在しない書籍")).thenReturn(new ArrayList<>());
        
        // Act
        List<Book> result = bookService.searchBook(searchParam);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(bookDao, times(1)).findByKeyword("存在しない書籍");
    }
    
    /**
     * 空白キーワードの検索（正常系）
     */
    @Test
    void testSearchBookWithEmptyKeyword() {
        // Arrange
        SearchParam searchParam = new SearchParam();
        searchParam.setKeyword("   ");
        
        when(bookDao.findAll()).thenReturn(testBooks);
        
        // Act
        List<Book> result = bookService.searchBook(searchParam);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookDao, times(1)).findAll();
    }
    
    /**
     * 書籍IDで検索（正常系）
     */
    @Test
    void testFindBookById() {
        // Arrange
        Book book = testBooks.get(0);
        when(bookDao.findById(1)).thenReturn(book);
        
        // Act
        Book result = bookService.findBookById(1);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getBookId());
        assertEquals("Java SE ディープダイブ", result.getBookName());
        verify(bookDao, times(1)).findById(1);
    }
    
    /**
     * 存在しない書籍IDで検索（正常系）
     */
    @Test
    void testFindBookByIdNotFound() {
        // Arrange
        when(bookDao.findById(999)).thenReturn(null);
        
        // Act
        Book result = bookService.findBookById(999);
        
        // Assert
        assertNull(result);
        verify(bookDao, times(1)).findById(999);
    }
}






