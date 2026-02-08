package pro.kensait.berrybooks.external.dto;

import java.io.Serializable;

/**
 * 在庫情報の転送オブジェクト
 */
public class StockTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer bookId;
    private Integer quantity;
    private Long version;
    
    public StockTO() {
    }
    
    public Integer getBookId() {
        return bookId;
    }
    
    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
}