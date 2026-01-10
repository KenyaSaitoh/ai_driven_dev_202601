package pro.kensait.berrybooks.util;

import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * メッセージプロパティファイルからメッセージを取得するユーティリティ
 * 
 * messages.propertiesからメッセージを取得する。
 */
public class MessageUtil {

    private static final Logger logger = LoggerFactory.getLogger(MessageUtil.class);

    private static final String BUNDLE_NAME = "messages";
    private static ResourceBundle bundle;

    static {
        try {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME);
        } catch (Exception e) {
            logger.warn("Failed to load resource bundle: {}", BUNDLE_NAME, e);
        }
    }

    /**
     * メッセージキーからメッセージを取得する
     * 
     * @param key メッセージキー
     * @return メッセージ（見つからない場合はキー自体を返す）
     */
    public static String getMessage(String key) {
        if (bundle == null) {
            return key;
        }
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            logger.warn("Message key not found: {}", key);
            return key;
        }
    }

    /**
     * メッセージキーとパラメータからメッセージを取得する
     * 
     * @param key メッセージキー
     * @param params パラメータ
     * @return メッセージ
     */
    public static String getMessage(String key, Object... params) {
        String message = getMessage(key);
        return String.format(message, params);
    }
}
