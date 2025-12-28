package pro.kensait.berrybooks.service.category;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import pro.kensait.berrybooks.dao.CategoryDao;
import pro.kensait.berrybooks.entity.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * カテゴリ管理サービス
 */
@ApplicationScoped
public class CategoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    
    @Inject
    private CategoryDao categoryDao;
    
    /**
     * カテゴリ一覧をMap形式で取得
     * 
     * @return Map<カテゴリ名, カテゴリID>
     */
    public Map<String, Integer> getCategoryMap() {
        logger.info("[ CategoryService#getCategoryMap ] Retrieving category map");
        
        List<Category> categories = categoryDao.findAll();
        Map<String, Integer> categoryMap = new LinkedHashMap<>();
        
        for (Category category : categories) {
            categoryMap.put(category.getCategoryName(), category.getCategoryId());
        }
        
        logger.info("[ CategoryService#getCategoryMap ] Returned {} categories", categoryMap.size());
        
        return categoryMap;
    }
}

