package pro.kensait.berrybooks.service.book;

/**
 * 書籍検索パラメータ
 */
public class SearchParam {
    
    private Integer categoryId;
    private String keyword;
    
    public SearchParam() {
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}

