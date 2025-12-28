package pro.kensait.berrybooks.common;

/**
 * 決済方法列挙型
 * 
 * データベースのSETTLEMENT_TYPEカラムの値と対応
 */
public enum SettlementType {
    
    BANK_TRANSFER(1, "銀行振込"),
    CREDIT_CARD(2, "クレジットカード"),
    CASH_ON_DELIVERY(3, "着払い");
    
    private final int code;
    private final String displayName;
    
    SettlementType(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * コードから列挙型を取得
     * 
     * @param code 決済方法コード（1, 2, 3）
     * @return SettlementType
     * @throws IllegalArgumentException 無効なコードの場合
     */
    public static SettlementType fromCode(int code) {
        for (SettlementType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid settlement type code: " + code);
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}

