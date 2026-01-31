package pro.kensait.berrybooks.common;

import java.util.ResourceBundle;

// メッセージリソース（messages.properties）からメッセージを取得するユーティリティクラス
public final class MessageUtil {
    
    // リソースバンドル（messages.properties）
    private static final ResourceBundle bundle = ResourceBundle.getBundle("messages");
    
    // ユーティリティメソッド：プロパティファイルからメッセージを取得
    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            // キーが見つからない場合はキー自体を返す
            return key;
        }
    }
    
    // ユーティリティメソッド：プロパティファイルからメッセージを取得し、パラメータを置換
    public static String get(String key, Object... params) {
        String message = get(key);
        return String.format(message, params);
    }
}
