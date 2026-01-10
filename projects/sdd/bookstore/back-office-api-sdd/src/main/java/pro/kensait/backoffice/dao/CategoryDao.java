package pro.kensait.backoffice.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.backoffice.entity.Category;

import java.util.List;

/**
 * カテゴリデータアクセスオブジェクト
 * 
 * カテゴリ情報のCRUD操作を提供する
 */
@ApplicationScoped
public class CategoryDao {

    private static final Logger logger = LoggerFactory.getLogger(CategoryDao.class);

    @PersistenceContext
    private EntityManager em;

    /**
     * 全カテゴリを取得
     * 
     * @return カテゴリエンティティのリスト（categoryId昇順）
     */
    public List<Category> findAll() {
        logger.debug("[ CategoryDao#findAll ]");
        
        String jpql = "SELECT c FROM Category c ORDER BY c.categoryId";
        
        TypedQuery<Category> query = em.createQuery(jpql, Category.class);
        List<Category> categories = query.getResultList();
        
        logger.debug("[ CategoryDao#findAll ] Found {} categories", categories.size());
        return categories;
    }

    /**
     * カテゴリIDでカテゴリを取得
     * 
     * @param categoryId カテゴリID
     * @return カテゴリエンティティ、存在しない場合はnull
     */
    public Category findById(Integer categoryId) {
        logger.debug("[ CategoryDao#findById ] categoryId: {}", categoryId);
        return em.find(Category.class, categoryId);
    }

    /**
     * カテゴリ情報を保存（INSERT）
     * 
     * @param category カテゴリエンティティ
     */
    public void insert(Category category) {
        logger.info("[ CategoryDao#insert ] categoryName: {}", category.getCategoryName());
        em.persist(category);
    }

    /**
     * カテゴリ情報を更新（UPDATE）
     * 
     * @param category カテゴリエンティティ
     * @return 更新後のカテゴリエンティティ
     */
    public Category update(Category category) {
        logger.info("[ CategoryDao#update ] categoryId: {}", category.getCategoryId());
        return em.merge(category);
    }

    /**
     * カテゴリ情報を削除（DELETE）
     * 
     * @param categoryId カテゴリID
     */
    public void delete(Integer categoryId) {
        logger.info("[ CategoryDao#delete ] categoryId: {}", categoryId);
        Category category = findById(categoryId);
        if (category != null) {
            em.remove(category);
        }
    }
}
