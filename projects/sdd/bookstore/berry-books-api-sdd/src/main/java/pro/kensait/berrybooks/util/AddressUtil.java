package pro.kensait.berrybooks.util;

import java.util.Set;

/**
 * 住所バリデーション用ユーティリティ
 * 
 * 住所が都道府県名から始まるかどうかを検証する。
 */
public class AddressUtil {

    // 日本の47都道府県
    private static final Set<String> PREFECTURES = Set.of(
        "北海道", "青森県", "岩手県", "宮城県", "秋田県", "山形県", "福島県",
        "茨城県", "栃木県", "群馬県", "埼玉県", "千葉県", "東京都", "神奈川県",
        "新潟県", "富山県", "石川県", "福井県", "山梨県", "長野県", "岐阜県",
        "静岡県", "愛知県", "三重県", "滋賀県", "京都府", "大阪府", "兵庫県",
        "奈良県", "和歌山県", "鳥取県", "島根県", "岡山県", "広島県", "山口県",
        "徳島県", "香川県", "愛媛県", "高知県", "福岡県", "佐賀県", "長崎県",
        "熊本県", "大分県", "宮崎県", "鹿児島県", "沖縄県"
    );

    /**
     * 住所が都道府県名から始まるかどうかを検証する
     * 
     * @param address 住所
     * @return 都道府県名から始まる場合true
     */
    public static boolean startsWithValidPrefecture(String address) {
        if (address == null || address.isEmpty()) {
            return false;
        }

        for (String prefecture : PREFECTURES) {
            if (address.startsWith(prefecture)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 住所が指定された都道府県から始まるかどうかを検証する
     * 
     * @param address 住所
     * @param prefecture 都道府県名
     * @return 指定された都道府県から始まる場合true
     */
    public static boolean startsWithPrefecture(String address, String prefecture) {
        if (address == null || address.isEmpty() || prefecture == null) {
            return false;
        }

        return address.startsWith(prefecture);
    }
}
