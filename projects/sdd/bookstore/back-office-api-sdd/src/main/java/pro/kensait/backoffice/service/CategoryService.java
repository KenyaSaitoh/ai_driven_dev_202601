package pro.kensait.backoffice.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.backoffice.api.dto.CategoryTO;
import pro.kensait.backoffice.dao.CategoryDao;
import pro.kensait.backoffice.entity.Category;

import java.util.List;
import java.util.stream.Collectors;

/**
 * カテゴリサービス
 * 
 * カテゴリ管理のビジネスロジックを提供する
 */
@ApplicationScoped
@Transactional
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Inject
    private CategoryDao categoryDao;

    /**
     * 全カテゴリを取得
     * 
     * @return カテゴリTOのリスト（categoryId昇順）
     */
    public List<CategoryTO> getAllCategories() {
        logger.info("[ CategoryService#getAllCategories ]");
        
        List<Category> categories = categoryDao.findAll();
        
        return categories.stream()
                .map(this::convertToTO)
                .collect(Collectors.toList());
    }

    /**
     * カテゴリIDでカテゴリを取得
     * 
     * @param categoryId カテゴリID
     * @return カテゴリTO、存在しない場合はnull
     */
    public CategoryTO getCategoryById(Integer categoryId) {
        logger.info("[ CategoryService#getCategoryById ] categoryId: {}", categoryId);
        
        Category category = categoryDao.findById(categoryId);
        
        if (category == null) {
            logger.warn("[ CategoryService#getCategoryById ] Category not found: {}", categoryId);
            return null;
        }
        
        logger.info("[ CategoryService#getCategoryById ] Success: categoryId={}", categoryId);
        return convertToTO(category);
    }

    /**
     * CategoryエンティティをCategoryTOに変換
     * 
     * @param category Categoryエンティティ
     * @return CategoryTO
     */
    private CategoryTO convertToTO(Category category) {
        if (category == null) {
            return null;
        }
        
        return new CategoryTO(
            category.getCategoryId(),
            category.getCategoryName()
        );
    }
}
