package pro.kensait.backoffice.api.dto;

/**
 * 在庫更新リクエストDTO
 */
public class StockUpdateRequest {
    private Long version;    // 楽観的ロック用バージョン番号
    private Integer quantity; // 更新後の在庫数

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

