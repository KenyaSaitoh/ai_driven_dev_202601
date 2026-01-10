package pro.kensait.backoffice.dao;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import pro.kensait.backoffice.entity.Book;
import pro.kensait.backoffice.entity.Category;
import pro.kensait.backoffice.entity.Publisher;

/**
 * BookDaoCriteriaの単体テスト
 * 
 * Criteria APIの動作をモックを使用してテスト
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class BookDaoCriteriaTest {

    @Mock
    private EntityManager em;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private CriteriaQuery<Book> cq;

    @Mock
    private Root<Book> root;

    @Mock
    private TypedQuery<Book> typedQuery;

    @Mock
    private Path<Object> path;

    @Mock
    private Path<Object> categoryPath;

    @Mock
    private Path<Object> categoryIdPath;

    @Mock
    private Path<String> bookNamePath;

    @Mock
    private Path<Object> deletedPath;

    @Mock
    @SuppressWarnings("rawtypes")
    private Fetch fetch;

    @Mock
    private Predicate predicate;

    @InjectMocks
    private BookDaoCriteria bookDaoCriteria;

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
     * テスト: カテゴリのみ指定
     */
    @Test
    void testSearch_ByCategoryOnly() {
        // Given
        Integer categoryId = 1;
        String keyword = null;

        // Criteria APIのモック設定
        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Book.class)).thenReturn(cq);
        when(cq.from(Book.class)).thenReturn(root);
        when(root.fetch(anyString(), any(JoinType.class))).thenReturn(fetch);
        
        // Path設定
        when(root.get("deleted")).thenReturn(deletedPath);
        when(root.get("category")).thenReturn(categoryPath);
        when(categoryPath.get("categoryId")).thenReturn(categoryIdPath);
        when(root.get("bookId")).thenReturn(path);
        
        // Predicate設定
        when(cb.equal(deletedPath, false)).thenReturn(predicate);
        when(cb.equal(categoryIdPath, categoryId)).thenReturn(predicate);
        when(cb.asc(path)).thenReturn(mock(Order.class));
        
        // TypedQuery設定
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.singletonList(testBook1));

        // When
        List<Book> result = bookDaoCriteria.searchByCriteria(categoryId, keyword);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java入門", result.get(0).getBookName());

        // モックの呼び出し確認
        verify(em, times(1)).getCriteriaBuilder();
        verify(cb, times(1)).createQuery(Book.class);
        verify(em, times(1)).createQuery(cq);
        verify(typedQuery, times(1)).getResultList();
    }

    /**
     * テスト: キーワードのみ指定
     */
    @Test
    void testSearch_ByKeywordOnly() {
        // Given
        Integer categoryId = null;
        String keyword = "Java";

        // Criteria APIのモック設定
        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Book.class)).thenReturn(cq);
        when(cq.from(Book.class)).thenReturn(root);
        when(root.fetch(anyString(), any(JoinType.class))).thenReturn(fetch);
        
        // Path設定
        when(root.get("deleted")).thenReturn(deletedPath);
        when(root.<String>get("bookName")).thenReturn(bookNamePath);
        when(root.get("bookId")).thenReturn(path);
        
        // Predicate設定
        when(cb.equal(deletedPath, false)).thenReturn(predicate);
        when(cb.like(bookNamePath, "%Java%")).thenReturn(predicate);
        when(cb.asc(path)).thenReturn(mock(Order.class));
        
        // TypedQuery設定
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.singletonList(testBook1));

        // When
        List<Book> result = bookDaoCriteria.searchByCriteria(categoryId, keyword);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java入門", result.get(0).getBookName());

        // モックの呼び出し確認
        verify(em, times(1)).getCriteriaBuilder();
        verify(cb, times(1)).createQuery(Book.class);
        verify(em, times(1)).createQuery(cq);
        verify(typedQuery, times(1)).getResultList();
    }

    /**
     * テスト: カテゴリとキーワード両方指定
     */
    @Test
    void testSearch_ByCategoryAndKeyword() {
        // Given
        Integer categoryId = 1;
        String keyword = "Java";

        // Criteria APIのモック設定
        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Book.class)).thenReturn(cq);
        when(cq.from(Book.class)).thenReturn(root);
        when(root.fetch(anyString(), any(JoinType.class))).thenReturn(fetch);
        
        // Path設定
        when(root.get("deleted")).thenReturn(deletedPath);
        when(root.get("category")).thenReturn(categoryPath);
        when(categoryPath.get("categoryId")).thenReturn(categoryIdPath);
        when(root.<String>get("bookName")).thenReturn(bookNamePath);
        when(root.get("bookId")).thenReturn(path);
        
        // Predicate設定
        when(cb.equal(deletedPath, false)).thenReturn(predicate);
        when(cb.equal(categoryIdPath, categoryId)).thenReturn(predicate);
        when(cb.like(bookNamePath, "%Java%")).thenReturn(predicate);
        when(cb.asc(path)).thenReturn(mock(Order.class));
        
        // TypedQuery設定
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.singletonList(testBook1));

        // When
        List<Book> result = bookDaoCriteria.searchByCriteria(categoryId, keyword);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java入門", result.get(0).getBookName());

        // モックの呼び出し確認
        verify(em, times(1)).getCriteriaBuilder();
        verify(cb, times(1)).createQuery(Book.class);
        verify(em, times(1)).createQuery(cq);
        verify(typedQuery, times(1)).getResultList();
    }

    /**
     * テスト: 条件なし（全件取得）
     */
    @Test
    void testSearch_NoCondition() {
        // Given
        Integer categoryId = null;
        String keyword = null;

        // Criteria APIのモック設定
        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Book.class)).thenReturn(cq);
        when(cq.from(Book.class)).thenReturn(root);
        when(root.fetch(anyString(), any(JoinType.class))).thenReturn(fetch);
        
        // Path設定
        when(root.get("deleted")).thenReturn(deletedPath);
        when(root.get("bookId")).thenReturn(path);
        
        // Predicate設定
        when(cb.equal(deletedPath, false)).thenReturn(predicate);
        when(cb.asc(path)).thenReturn(mock(Order.class));
        
        // TypedQuery設定
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(testBook1, testBook2));

        // When
        List<Book> result = bookDaoCriteria.searchByCriteria(categoryId, keyword);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // モックの呼び出し確認
        verify(em, times(1)).getCriteriaBuilder();
        verify(cb, times(1)).createQuery(Book.class);
        verify(em, times(1)).createQuery(cq);
        verify(typedQuery, times(1)).getResultList();
    }

    /**
     * テスト: categoryId=0の場合（全カテゴリ）
     */
    @Test
    void testSearch_CategoryIdZero() {
        // Given
        Integer categoryId = 0;
        String keyword = null;

        // Criteria APIのモック設定
        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Book.class)).thenReturn(cq);
        when(cq.from(Book.class)).thenReturn(root);
        when(root.fetch(anyString(), any(JoinType.class))).thenReturn(fetch);
        
        // Path設定
        when(root.get("deleted")).thenReturn(deletedPath);
        when(root.get("bookId")).thenReturn(path);
        
        // Predicate設定
        when(cb.equal(deletedPath, false)).thenReturn(predicate);
        when(cb.asc(path)).thenReturn(mock(Order.class));
        
        // TypedQuery設定
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(testBook1, testBook2));

        // When
        List<Book> result = bookDaoCriteria.searchByCriteria(categoryId, keyword);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // categoryId=0の場合、カテゴリ条件は追加されない
        verify(em, times(1)).getCriteriaBuilder();
        verify(cb, times(1)).createQuery(Book.class);
        verify(em, times(1)).createQuery(cq);
        verify(typedQuery, times(1)).getResultList();
    }

    /**
     * テスト: 空文字列のキーワード
     */
    @Test
    void testSearch_EmptyKeyword() {
        // Given
        Integer categoryId = null;
        String keyword = "";

        // Criteria APIのモック設定
        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Book.class)).thenReturn(cq);
        when(cq.from(Book.class)).thenReturn(root);
        when(root.fetch(anyString(), any(JoinType.class))).thenReturn(fetch);
        
        // Path設定
        when(root.get("deleted")).thenReturn(deletedPath);
        when(root.get("bookId")).thenReturn(path);
        
        // Predicate設定
        when(cb.equal(deletedPath, false)).thenReturn(predicate);
        when(cb.asc(path)).thenReturn(mock(Order.class));
        
        // TypedQuery設定
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(testBook1, testBook2));

        // When
        List<Book> result = bookDaoCriteria.searchByCriteria(categoryId, keyword);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // 空文字列の場合、キーワード条件は追加されない
        verify(em, times(1)).getCriteriaBuilder();
        verify(cb, times(1)).createQuery(Book.class);
        verify(em, times(1)).createQuery(cq);
        verify(typedQuery, times(1)).getResultList();
    }

    /**
     * テスト: 検索結果が0件
     */
    @Test
    void testSearch_NoResults() {
        // Given
        Integer categoryId = 1;
        String keyword = "NotExist";

        // Criteria APIのモック設定
        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Book.class)).thenReturn(cq);
        when(cq.from(Book.class)).thenReturn(root);
        when(root.fetch(anyString(), any(JoinType.class))).thenReturn(fetch);
        
        // Path設定
        when(root.get("deleted")).thenReturn(deletedPath);
        when(root.get("category")).thenReturn(categoryPath);
        when(categoryPath.get("categoryId")).thenReturn(categoryIdPath);
        when(root.<String>get("bookName")).thenReturn(bookNamePath);
        when(root.get("bookId")).thenReturn(path);
        
        // Predicate設定
        when(cb.equal(deletedPath, false)).thenReturn(predicate);
        when(cb.equal(categoryIdPath, categoryId)).thenReturn(predicate);
        when(cb.like(bookNamePath, "%NotExist%")).thenReturn(predicate);
        when(cb.asc(path)).thenReturn(mock(Order.class));
        
        // TypedQuery設定
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        // When
        List<Book> result = bookDaoCriteria.searchByCriteria(categoryId, keyword);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());

        // モックの呼び出し確認
        verify(em, times(1)).getCriteriaBuilder();
        verify(cb, times(1)).createQuery(Book.class);
        verify(em, times(1)).createQuery(cq);
        verify(typedQuery, times(1)).getResultList();
    }
}
