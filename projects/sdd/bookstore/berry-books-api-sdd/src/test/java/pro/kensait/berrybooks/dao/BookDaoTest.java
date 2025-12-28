package pro.kensait.berrybooks.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import pro.kensait.berrybooks.entity.Book;
import pro.kensait.berrybooks.entity.Category;
import pro.kensait.berrybooks.entity.Publisher;
import pro.kensait.berrybooks.entity.Stock;

/**
 * BookDaoのテストクラス
 * 
 * <p>注: これはユニットテストです。完全な結合テストを実行するには、
 * 実際のデータベース（HSQLDBインメモリ）を使用したテストクラスを別途作成してください。</p>
 */
@ExtendWith(MockitoExtension.class)
class BookDaoTest {
    
    @Mock
    private EntityManager em;
    
    @InjectMocks
    private BookDao bookDao;
    
    @Mock
    private TypedQuery<Book> typedQuery;
    
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
        
        Stock stock1 = new Stock();
        stock1.setBookId(1);
        stock1.setQuantity(10);
        stock1.setVersion(0L);
        
        Book book1 = new Book();
        book1.setBookId(1);
        book1.setBookName("Java SE ディープダイブ");
        book1.setAuthor("Michael Johnson");
        book1.setPrice(BigDecimal.valueOf(3400));
        book1.setCategory(category);
        book1.setPublisher(publisher);
        book1.setStock(stock1);
        
        Stock stock2 = new Stock();
        stock2.setBookId(2);
        stock2.setQuantity(5);
        stock2.setVersion(0L);
        
        Book book2 = new Book();
        book2.setBookId(2);
        book2.setBookName("JVM とバイトコードの探求");
        book2.setAuthor("James Lopez");
        book2.setPrice(BigDecimal.valueOf(4200));
        book2.setCategory(category);
        book2.setPublisher(publisher);
        book2.setStock(stock2);
        
        testBooks.add(book1);
        testBooks.add(book2);
    }
    
    /**
     * 全書籍取得（正常系）
     */
    @Test
    void testFindAll() {
        // Arrange
        when(em.createQuery(anyString(), eq(Book.class))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(testBooks);
        
        // Act
        List<Book> result = bookDao.findAll();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Java SE ディープダイブ", result.get(0).getBookName());
        verify(em, times(1)).createQuery(anyString(), eq(Book.class));
        verify(typedQuery, times(1)).getResultList();
    }
    
    /**
     * 書籍ID検索（正常系）
     */
    @Test
    void testFindById() {
        // Arrange
        Book book = testBooks.get(0);
        when(em.find(Book.class, 1)).thenReturn(book);
        
        // Act
        Book result = bookDao.findById(1);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getBookId());
        assertEquals("Java SE ディープダイブ", result.getBookName());
        verify(em, times(1)).find(Book.class, 1);
    }
    
    /**
     * 存在しない書籍ID検索（正常系）
     */
    @Test
    void testFindByIdNotFound() {
        // Arrange
        when(em.find(Book.class, 999)).thenReturn(null);
        
        // Act
        Book result = bookDao.findById(999);
        
        // Assert
        assertNull(result);
        verify(em, times(1)).find(Book.class, 999);
    }
    
    /**
     * キーワード検索（正常系、LIKE検索）
     */
    @Test
    void testFindByKeyword() {
        // Arrange
        when(em.createQuery(anyString(), eq(Book.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("keyword"), anyString())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(testBooks);
        
        // Act
        List<Book> result = bookDao.findByKeyword("Java");
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(em, times(1)).createQuery(anyString(), eq(Book.class));
        verify(typedQuery, times(1)).setParameter("keyword", "%Java%");
        verify(typedQuery, times(1)).getResultList();
    }
    
    /**
     * カテゴリのみで検索（正常系）
     */
    @Test
    void testFindByCategory() {
        // Arrange
        when(em.createQuery(anyString(), eq(Book.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("categoryId"), anyInt())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(testBooks);
        
        // Act
        List<Book> result = bookDao.findByCategory(1);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(em, times(1)).createQuery(anyString(), eq(Book.class));
        verify(typedQuery, times(1)).setParameter("categoryId", 1);
        verify(typedQuery, times(1)).getResultList();
    }
    
    /**
     * カテゴリとキーワード検索（正常系）
     */
    @Test
    void testFindByCategoryAndKeyword() {
        // Arrange
        when(em.createQuery(anyString(), eq(Book.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("categoryId"), anyInt())).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("keyword"), anyString())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(testBooks);
        
        // Act
        List<Book> result = bookDao.findByCategoryAndKeyword(1, "Java");
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(em, times(1)).createQuery(anyString(), eq(Book.class));
        verify(typedQuery, times(1)).setParameter("categoryId", 1);
        verify(typedQuery, times(1)).setParameter("keyword", "%Java%");
        verify(typedQuery, times(1)).getResultList();
    }
    
    /**
     * 検索結果が0件の場合（正常系）
     */
    @Test
    void testFindByKeywordNoResults() {
        // Arrange
        when(em.createQuery(anyString(), eq(Book.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("keyword"), anyString())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(new ArrayList<>());
        
        // Act
        List<Book> result = bookDao.findByKeyword("存在しない書籍");
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(em, times(1)).createQuery(anyString(), eq(Book.class));
        verify(typedQuery, times(1)).setParameter("keyword", "%存在しない書籍%");
        verify(typedQuery, times(1)).getResultList();
    }
}






