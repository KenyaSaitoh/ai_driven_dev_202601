package pro.kensait.berrybooks.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pro.kensait.berrybooks.entity.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * カテゴリデータアクセスクラス
 */
@ApplicationScoped
public class CategoryDao {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryDao.class);
    
    @PersistenceContext(unitName = "BerryBooksPU")
    private EntityManager em;
    
    /**
     * 全カテゴリを取得
     * 
     * @return カテゴリリスト
     */
    public List<Category> findAll() {
        logger.info("[ CategoryDao#findAll ] Retrieving all categories");
        
        TypedQuery<Category> query = em.createQuery(
            "SELECT c FROM Category c ORDER BY c.categoryId", 
            Category.class
        );
        
        List<Category> categories = query.getResultList();
        logger.info("[ CategoryDao#findAll ] Found {} categories", categories.size());
        
        return categories;
    }
    
    /**
     * カテゴリIDでカテゴリを検索
     * 
     * @param categoryId カテゴリID
     * @return Category（見つからない場合はnull）
     */
    public Category findById(Integer categoryId) {
        logger.info("[ CategoryDao#findById ] categoryId={}", categoryId);
        return em.find(Category.class, categoryId);
    }
}

