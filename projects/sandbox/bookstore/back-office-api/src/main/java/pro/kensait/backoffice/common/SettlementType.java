package pro.kensait.backoffice.common;

/**
 * 決済方法を表すEnum
 */
public enum SettlementType {
    BANK_TRANSFER(1, "銀行振り込み"),
    CREDIT_CARD(2, "クレジットカード"),
    CASH_ON_DELIVERY(3, "着払い");

    private final Integer code;
    private final String displayName;

    SettlementType(Integer code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    /**
     * コードを取得する
     * @return コード
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 表示名を取得する
     * @return 表示名
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * コードから決済方法のEnumを取得する
     * @param code コード
     * @return 決済方法のEnum、codeがnullの場合はnull
     * @throws IllegalArgumentException 不正なコードの場合
     */
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

    /**
     * コードから表示名を取得する
     * @param code コード
     * @return 表示名、codeがnullの場合は"未選択"、不正なコードの場合は"不明"
     */
    public static String getDisplayNameByCode(Integer code) {
        if (code == null) {
            return "未選択";
        }
        
        try {
            SettlementType type = fromCode(code);
            return type != null ? type.getDisplayName() : "不明";
        } catch (IllegalArgumentException e) {
            return "不明";
        }
    }

    /**
     * 全ての決済方法コードを取得する
     * @return 決済方法コードの配列
     */
    public static Integer[] getAllCodes() {
        SettlementType[] types = values();
        Integer[] codes = new Integer[types.length];
        for (int i = 0; i < types.length; i++) {
            codes[i] = types[i].getCode();
        }
        return codes;
    }
}

