package pro.kensait.berrybooks.external.dto;

import java.io.Serializable;

/**
 * カテゴリ情報の転送オブジェクト
 */
public class CategoryTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer categoryId;
    private String categoryName;
    
    public CategoryTO() {
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}