package pro.kensait.berrybooks.external.dto;

/**
 * 在庫更新リクエストDTO（Books Stock API用）
 */
public class StockUpdateRequest {
    private Long version;
    private Integer quantity;

    // デフォルトコンストラクタ
    public StockUpdateRequest() {
    }

    // コンストラクタ
    public StockUpdateRequest(Long version, Integer quantity) {
        this.version = version;
        this.quantity = quantity;
    }

    // Getter/Setter
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "StockUpdateRequest [version=" + version + ", quantity=" + quantity + "]";
    }
}

