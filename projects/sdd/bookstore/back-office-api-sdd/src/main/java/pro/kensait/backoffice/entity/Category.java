package pro.kensait.backoffice.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * カテゴリエンティティ
 * 
 * テーブル: CATEGORY
 */
@Entity
@Table(name = "CATEGORY")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATEGORY_ID")
    private Integer categoryId;

    @Column(name = "CATEGORY_NAME", length = 100)
    private String categoryName;

    // デフォルトコンストラクタ（JPA必須）
    public Category() {
    }

    // コンストラクタ
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

    // equals/hashCode（主キーベース）
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(categoryId, category.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId);
    }

    // toString
    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}
