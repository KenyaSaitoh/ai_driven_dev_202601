package pro.kensait.berrybooks.common;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * メッセージリソースユーティリティ
 * 
 * messages.propertiesからメッセージを読み込む
 */
@ApplicationScoped
public class MessageUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageUtil.class);
    private static final String BUNDLE_NAME = "messages";
    
    /**
     * メッセージキーからメッセージを取得
     * 
     * @param key メッセージキー
     * @return メッセージ文字列
     */
    public String getMessage(String key) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME);
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            logger.warn("Message key not found: {}", key);
            return "???" + key + "???";
        }
    }
    
    /**
     * メッセージキーからメッセージを取得（パラメータ置換）
     * 
     * @param key メッセージキー
     * @param params 置換パラメータ
     * @return メッセージ文字列
     */
    public String getMessage(String key, Object... params) {
        String message = getMessage(key);
        if (params != null && params.length > 0) {
            return String.format(message, params);
        }
        return message;
    }
}

