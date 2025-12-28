package pro.kensait.berrybooks.entity;

import jakarta.persistence.*;

/**
 * カテゴリエンティティ
 * 
 * テーブル: CATEGORY
 */
@Entity
@Table(name = "CATEGORY")
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATEGORY_ID")
    private Integer categoryId;
    
    @Column(name = "CATEGORY_NAME", length = 20, nullable = false)
    private String categoryName;
    
    // コンストラクタ
    public Category() {
    }
    
    public Category(Integer categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }
    
    // Getter/Setter
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
    
    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}

