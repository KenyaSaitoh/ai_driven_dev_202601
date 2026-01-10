package pro.kensait.backoffice.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.backoffice.entity.Book;
import pro.kensait.backoffice.entity.Category;
import pro.kensait.backoffice.entity.Publisher;

import java.util.ArrayList;
import java.util.List;

/**
 * 書籍データアクセスオブジェクト（Criteria API版）
 * 
 * 書籍情報の検索機能を提供する（Criteria API使用）
 */
@ApplicationScoped
public class BookDaoCriteria {

    private static final Logger logger = LoggerFactory.getLogger(BookDaoCriteria.class);

    @PersistenceContext
    private EntityManager em;

    /**
     * 書籍を検索（Criteria API動的クエリ）
     * 
     * カテゴリIDとキーワードで検索
     * 論理削除を除外
     * 
     * @param categoryId カテゴリID（nullまたは0の場合は全カテゴリ）
     * @param keyword キーワード（nullまたは空の場合は条件なし）
     * @return 書籍エンティティのリスト
     */
    public List<Book> searchByCriteria(Integer categoryId, String keyword) {
        logger.debug("[ BookDaoCriteria#searchByCriteria ] categoryId: {}, keyword: {}", categoryId, keyword);
        
        // CriteriaBuilderを取得
        CriteriaBuilder cb = em.getCriteriaBuilder();
        
        // CriteriaQueryを作成
        CriteriaQuery<Book> cq = cb.createQuery(Book.class);
        
        // Rootを定義
        Root<Book> book = cq.from(Book.class);
        
        // JOIN FETCHでカテゴリと出版社を一括取得
        book.fetch("category", JoinType.LEFT);
        book.fetch("publisher", JoinType.LEFT);
        
        // 動的にPredicateのリストを構築
        List<Predicate> predicates = new ArrayList<>();
        
        // 論理削除フラグの条件（必須）
        predicates.add(cb.equal(book.get("deleted"), false));
        
        // カテゴリID条件
        if (categoryId != null && categoryId > 0) {
            predicates.add(cb.equal(book.get("category").get("categoryId"), categoryId));
        }
        
        // キーワード条件（書籍名に部分一致）
        if (keyword != null && !keyword.isEmpty()) {
            String likePattern = "%" + keyword + "%";
            predicates.add(cb.like(book.get("bookName"), likePattern));
        }
        
        // すべての条件をAND結合
        cq.where(predicates.toArray(new Predicate[0]));
        
        // ORDER BY bookId
        cq.orderBy(cb.asc(book.get("bookId")));
        
        // クエリを実行
        TypedQuery<Book> query = em.createQuery(cq);
        List<Book> books = query.getResultList();
        
        logger.debug("[ BookDaoCriteria#searchByCriteria ] Found {} books", books.size());
        
        return books;
    }
}
