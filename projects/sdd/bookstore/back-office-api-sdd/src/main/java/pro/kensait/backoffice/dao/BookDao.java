package pro.kensait.backoffice.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.backoffice.entity.Book;

import java.util.List;

/**
 * 書籍データアクセスオブジェクト（JPQL版）
 * 
 * 書籍情報のCRUD操作を提供する（JPQL使用）
 */
@ApplicationScoped
public class BookDao {

    private static final Logger logger = LoggerFactory.getLogger(BookDao.class);

    @PersistenceContext
    private EntityManager em;

    /**
     * 全書籍を取得（論理削除を除外）
     * 
     * カテゴリと出版社をJOIN FETCHで一括取得
     * 
     * @return 書籍エンティティのリスト
     */
    public List<Book> findAll() {
        logger.debug("[ BookDao#findAll ]");
        
        String jpql = "SELECT b FROM Book b " +
                     "LEFT JOIN FETCH b.category " +
                     "LEFT JOIN FETCH b.publisher " +
                     "WHERE b.deleted = false " +
                     "ORDER BY b.bookId";
        
        TypedQuery<Book> query = em.createQuery(jpql, Book.class);
        List<Book> books = query.getResultList();
        
        logger.debug("[ BookDao#findAll ] Found {} books", books.size());
        return books;
    }

    /**
     * 書籍IDで書籍を取得
     * 
     * カテゴリと出版社をJOIN FETCHで一括取得
     * 論理削除されたものも取得可能
     * 
     * @param bookId 書籍ID
     * @return 書籍エンティティ、存在しない場合はnull
     */
    public Book findById(Integer bookId) {
        logger.debug("[ BookDao#findById ] bookId: {}", bookId);
        
        String jpql = "SELECT b FROM Book b " +
                     "LEFT JOIN FETCH b.category " +
                     "LEFT JOIN FETCH b.publisher " +
                     "WHERE b.bookId = :bookId";
        
        TypedQuery<Book> query = em.createQuery(jpql, Book.class);
        query.setParameter("bookId", bookId);
        
        List<Book> results = query.getResultList();
        
        if (results.isEmpty()) {
            logger.debug("[ BookDao#findById ] Book not found: {}", bookId);
            return null;
        }
        
        logger.debug("[ BookDao#findById ] Book found: {}", bookId);
        return results.get(0);
    }

    /**
     * 書籍を検索（JPQL動的クエリ）
     * 
     * カテゴリIDとキーワードで検索
     * 論理削除を除外
     * 
     * @param categoryId カテゴリID（nullまたは0の場合は全カテゴリ）
     * @param keyword キーワード（nullまたは空の場合は条件なし）
     * @return 書籍エンティティのリスト
     */
    public List<Book> searchByJpql(Integer categoryId, String keyword) {
        logger.debug("[ BookDao#searchByJpql ] categoryId: {}, keyword: {}", categoryId, keyword);
        
        StringBuilder jpql = new StringBuilder(
            "SELECT b FROM Book b " +
            "LEFT JOIN FETCH b.category " +
            "LEFT JOIN FETCH b.publisher " +
            "WHERE b.deleted = false"
        );
        
        // カテゴリID条件
        if (categoryId != null && categoryId > 0) {
            jpql.append(" AND b.category.categoryId = :categoryId");
        }
        
        // キーワード条件（書籍名に部分一致）
        if (keyword != null && !keyword.isEmpty()) {
            jpql.append(" AND b.bookName LIKE :keyword");
        }
        
        jpql.append(" ORDER BY b.bookId");
        
        TypedQuery<Book> query = em.createQuery(jpql.toString(), Book.class);
        
        // パラメータ設定
        if (categoryId != null && categoryId > 0) {
            query.setParameter("categoryId", categoryId);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            query.setParameter("keyword", "%" + keyword + "%");
        }
        
        List<Book> books = query.getResultList();
        logger.debug("[ BookDao#searchByJpql ] Found {} books", books.size());
        
        return books;
    }

    /**
     * 書籍情報を保存（INSERT）
     * 
     * @param book 書籍エンティティ
     */
    public void insert(Book book) {
        logger.info("[ BookDao#insert ] bookName: {}", book.getBookName());
        em.persist(book);
    }

    /**
     * 書籍情報を更新（UPDATE）
     * 
     * @param book 書籍エンティティ
     * @return 更新後の書籍エンティティ
     */
    public Book update(Book book) {
        logger.info("[ BookDao#update ] bookId: {}", book.getBookId());
        return em.merge(book);
    }
}
