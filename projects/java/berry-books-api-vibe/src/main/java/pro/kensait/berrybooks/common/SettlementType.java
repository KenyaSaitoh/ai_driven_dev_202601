package pro.kensait.berrybooks.common;

// 決済方法を表すEnum
public enum SettlementType {
    
    // 銀行振り込み
    BANK_TRANSFER(1, "銀行振り込み"),
    
    // クレジットカード
    CREDIT_CARD(2, "クレジットカード"),
    
    // 着払い
    CASH_ON_DELIVERY(3, "着払い");
    
    private final Integer code;
    private final String displayName;
    
    // コンストラクタ
    SettlementType(Integer code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    // 決済方法コードを取得
    public Integer getCode() {
        return code;
    }
    
    // 表示名を取得
    public String getDisplayName() {
        return displayName;
    }
    
    // コードからEnumを取得
    // @param code 決済方法コード（1:銀行振り込み、2:クレジットカード、3:着払い）
    // @return 対応するSettlementType
    // @throws IllegalArgumentException コードが不正な場合
    public static SettlementType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        
        for (SettlementType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        
        throw new IllegalArgumentException("Invalid settlement type code: " + code);
    }
    
    // コードから表示名を取得（nullセーフ）
    // @param code 決済方法コード
    // @return 表示名（コードがnullまたは不正な場合は"未選択"）
    public static String getDisplayNameByCode(Integer code) {
        if (code == null) {
            return "未選択";
        }
        
        try {
            SettlementType type = fromCode(code);
            return type.getDisplayName();
        } catch (IllegalArgumentException e) {
            return "不明";
        }
    }
    
    // 全ての決済方法コードの配列を取得
    // @return 決済方法コードの配列
    public static Integer[] getAllCodes() {
        SettlementType[] types = values();
        Integer[] codes = new Integer[types.length];
        for (int i = 0; i < types.length; i++) {
            codes[i] = types[i].code;
        }
        return codes;
    }
}

