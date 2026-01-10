package pro.kensait.berrybooks.common;

/**
 * 決済方法Enum
 * 
 * ORDER_TRANテーブルのSETTLEMENT_TYPEカラムに対応する。
 */
public enum SettlementType {
    BANK_TRANSFER(1, "銀行振込"),
    CREDIT_CARD(2, "クレジットカード"),
    CASH_ON_DELIVERY(3, "着払い");

    private final int value;
    private final String displayName;

    SettlementType(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 値からSettlementTypeを取得する
     * 
     * @param value 値
     * @return SettlementType
     * @throws IllegalArgumentException 無効な値の場合
     */
    public static SettlementType valueOf(int value) {
        for (SettlementType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid settlement type value: " + value);
    }
}
