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
 * 在庫データアクセスオブジェクト
 * 
 * 在庫情報のCRUD操作を提供する
 * BookエンティティのSTOCK SecondaryTableを使用
 */
@ApplicationScoped
public class StockDao {

    private static final Logger logger = LoggerFactory.getLogger(StockDao.class);

    @PersistenceContext
    private EntityManager em;

    /**
     * 全在庫を取得
     * 
     * 論理削除されていない書籍の在庫情報を取得
     * 
     * @return 書籍エンティティのリスト（bookId昇順）
     */
    public List<Book> findAll() {
        logger.debug("[ StockDao#findAll ]");
        
        String jpql = "SELECT b FROM Book b " +
                     "WHERE b.deleted = false " +
                     "ORDER BY b.bookId";
        
        TypedQuery<Book> query = em.createQuery(jpql, Book.class);
        List<Book> books = query.getResultList();
        
        logger.debug("[ StockDao#findAll ] Found {} stocks", books.size());
        return books;
    }

    /**
     * 書籍IDで在庫を取得
     * 
     * @param bookId 書籍ID
     * @return 書籍エンティティ、存在しない場合はnull
     */
    public Book findById(Integer bookId) {
        logger.debug("[ StockDao#findById ] bookId: {}", bookId);
        
        String jpql = "SELECT b FROM Book b " +
                     "WHERE b.bookId = :bookId " +
                     "AND b.deleted = false";
        
        TypedQuery<Book> query = em.createQuery(jpql, Book.class);
        query.setParameter("bookId", bookId);
        
        List<Book> results = query.getResultList();
        
        if (results.isEmpty()) {
            logger.debug("[ StockDao#findById ] Stock not found: {}", bookId);
            return null;
        }
        
        logger.debug("[ StockDao#findById ] Stock found: {}", bookId);
        return results.get(0);
    }

    /**
     * 在庫情報を更新（UPDATE）
     * 
     * 楽観的ロック（@Version）により、バージョンチェックが自動的に行われる
     * 
     * @param book 書籍エンティティ
     * @return 更新後の書籍エンティティ
     */
    public Book update(Book book) {
        logger.info("[ StockDao#update ] bookId: {}, quantity: {}, version: {}", 
                   book.getBookId(), book.getQuantity(), book.getVersion());
        return em.merge(book);
    }
}
