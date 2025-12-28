package pro.kensait.berrybooks.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pro.kensait.berrybooks.entity.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 書籍データアクセスクラス
 * 
 * N+1問題を回避するため、JOIN FETCHを使用してCategory、Publisher、Stockを一括取得
 */
@ApplicationScoped
public class BookDao {
    
    private static final Logger logger = LoggerFactory.getLogger(BookDao.class);
    
    @PersistenceContext(unitName = "BerryBooksPU")
    private EntityManager em;
    
    /**
     * 全書籍を取得（JOIN FETCH使用）
     * 
     * @return 書籍リスト
     */
    public List<Book> findAll() {
        logger.info("[ BookDao#findAll ] Retrieving all books with JOIN FETCH");
        
        TypedQuery<Book> query = em.createQuery(
            "SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN FETCH b.category " +
            "LEFT JOIN FETCH b.publisher " +
            "LEFT JOIN FETCH b.stock " +
            "ORDER BY b.bookId", 
            Book.class
        );
        
        List<Book> books = query.getResultList();
        logger.info("[ BookDao#findAll ] Found {} books", books.size());
        
        return books;
    }
    
    /**
     * 書籍IDで書籍を検索（JOIN FETCH使用）
     * 
     * @param bookId 書籍ID
     * @return Book（見つからない場合はnull）
     */
    public Book findById(Integer bookId) {
        logger.info("[ BookDao#findById ] bookId={}", bookId);
        
        TypedQuery<Book> query = em.createQuery(
            "SELECT b FROM Book b " +
            "LEFT JOIN FETCH b.category " +
            "LEFT JOIN FETCH b.publisher " +
            "LEFT JOIN FETCH b.stock " +
            "WHERE b.bookId = :bookId", 
            Book.class
        );
        query.setParameter("bookId", bookId);
        
        List<Book> books = query.getResultList();
        if (books.isEmpty()) {
            logger.info("[ BookDao#findById ] Book not found: {}", bookId);
            return null;
        }
        
        return books.get(0);
    }
    
    /**
     * カテゴリIDで書籍を検索（JOIN FETCH使用）
     * 
     * @param categoryId カテゴリID
     * @return 書籍リスト
     */
    public List<Book> findByCategoryId(Integer categoryId) {
        logger.info("[ BookDao#findByCategoryId ] categoryId={}", categoryId);
        
        TypedQuery<Book> query = em.createQuery(
            "SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN FETCH b.category " +
            "LEFT JOIN FETCH b.publisher " +
            "LEFT JOIN FETCH b.stock " +
            "WHERE b.category.categoryId = :categoryId " +
            "ORDER BY b.bookId", 
            Book.class
        );
        query.setParameter("categoryId", categoryId);
        
        List<Book> books = query.getResultList();
        logger.info("[ BookDao#findByCategoryId ] Found {} books", books.size());
        
        return books;
    }
    
    /**
     * 書籍検索（カテゴリID、キーワード）
     * 
     * @param categoryId カテゴリID（0または未指定=全カテゴリ）
     * @param keyword キーワード（書籍名、著者名で部分一致検索）
     * @return 書籍リスト
     */
    public List<Book> searchBooks(Integer categoryId, String keyword) {
        logger.info("[ BookDao#searchBooks ] categoryId={}, keyword={}", categoryId, keyword);
        
        StringBuilder jpql = new StringBuilder(
            "SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN FETCH b.category " +
            "LEFT JOIN FETCH b.publisher " +
            "LEFT JOIN FETCH b.stock " +
            "WHERE 1=1 "
        );
        
        // カテゴリID条件
        if (categoryId != null && categoryId != 0) {
            jpql.append("AND b.category.categoryId = :categoryId ");
        }
        
        // キーワード条件
        if (keyword != null && !keyword.isEmpty()) {
            jpql.append("AND (b.bookName LIKE :keyword OR b.author LIKE :keyword) ");
        }
        
        jpql.append("ORDER BY b.bookId");
        
        TypedQuery<Book> query = em.createQuery(jpql.toString(), Book.class);
        
        if (categoryId != null && categoryId != 0) {
            query.setParameter("categoryId", categoryId);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            query.setParameter("keyword", "%" + keyword + "%");
        }
        
        List<Book> books = query.getResultList();
        logger.info("[ BookDao#searchBooks ] Found {} books", books.size());
        
        return books;
    }
}

