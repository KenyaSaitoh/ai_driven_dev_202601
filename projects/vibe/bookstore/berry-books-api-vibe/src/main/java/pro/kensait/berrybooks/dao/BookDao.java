package pro.kensait.berrybooks.dao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.kensait.berrybooks.entity.Book;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

// 書籍テーブルへのアクセスを行うDAOクラス
@ApplicationScoped
public class BookDao {
    private static final Logger logger = LoggerFactory.getLogger(
            BookDao.class);

    @PersistenceContext(unitName = "bookstorePU")
    private EntityManager em;

    // DAOメソッド：書籍を主キーで検索
    public Book findById(Integer bookId) {
        logger.info("[ BookDao#findById ]");
        return em.find(Book.class, bookId);
    }

    // DAOメソッド：全書籍を取得
    public List<Book> findAll() {
        logger.info("[ BookDao#findAll ]");
        
        TypedQuery<Book> query = em.createQuery(
                "SELECT b FROM Book b", Book.class);
        
        List<Book> books = query.getResultList();
        
        // 各エンティティをデータベースから再読み込みして最新の在庫データを取得
        for (Book book : books) {
            em.refresh(book);
        }
        
        return books;
    }

    // DAOメソッド：カテゴリIDで書籍を検索
    public List<Book> queryByCategory(Integer categoryId) {
        logger.info("[ BookDao#queryByCategory ]");
        
        TypedQuery<Book> query = em.createQuery(
                "SELECT b FROM Book b WHERE b.category.categoryId = :categoryId", 
                Book.class);
        query.setParameter("categoryId", categoryId);
        
        List<Book> books = query.getResultList();
        
        // 各エンティティをデータベースから再読み込みして最新の在庫データを取得
        for (Book book : books) {
            em.refresh(book);
        }
        
        return books;
    }

    // DAOメソッド：キーワードで書籍を検索
    public List<Book> queryByKeyword(String keyword) {
        logger.info("[ BookDao#queryByKeyword ]");
        
        TypedQuery<Book> query = em.createQuery(
                "SELECT b FROM Book b WHERE b.bookName like :keyword", 
                Book.class);
        query.setParameter("keyword", keyword);
        
        List<Book> books = query.getResultList();
        
        // 各エンティティをデータベースから再読み込みして最新の在庫データを取得
        for (Book book : books) {
            em.refresh(book);
        }
        
        return books;
    }

    // DAOメソッド：カテゴリIDとキーワードで書籍を検索
    public List<Book> query(Integer categoryId, String keyword) {
        logger.info("[ BookDao#query ]");
        
        TypedQuery<Book> query = em.createQuery(
                "SELECT b FROM Book b WHERE b.category.categoryId = :categoryId " +
                "AND b.bookName like :keyword", 
                Book.class);
        query.setParameter("categoryId", categoryId);
        query.setParameter("keyword", keyword);
        
        List<Book> books = query.getResultList();
        
        // 各エンティティをデータベースから再読み込みして最新の在庫データを取得
        for (Book book : books) {
            em.refresh(book);
        }
        
        return books;
    }

    // DAOメソッド：動的クエリで書籍を検索（Criteria API）
    public List<Book> searchWithCriteria(Integer categoryId, String keyword) {
        logger.info("[ BookDao#searchWithCriteria ]");
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Book> cq = cb.createQuery(Book.class);
        Root<Book> book = cq.from(Book.class);

        // 動的に条件を構築
        List<Predicate> predicates = new ArrayList<>();
        
        if (categoryId != null) {
            predicates.add(cb.equal(
                    book.get("category").get("categoryId"), categoryId));
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            predicates.add(cb.like(
                    book.get("bookName"), keyword));
        }

        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        TypedQuery<Book> query = em.createQuery(cq);
        List<Book> books = query.getResultList();
        
        // 各エンティティをデータベースから再読み込みして最新の在庫データを取得
        for (Book book2 : books) {
            em.refresh(book2);
        }
        
        return books;
    }
}


