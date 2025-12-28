package pro.kensait.berrybooks.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS設定クラス
 * 
 * ベースURL: /api
 */
@ApplicationPath("/api")
public class ApplicationConfig extends Application {
    // デフォルトの実装を使用（全てのResourceとProviderが自動的にスキャンされる）
}

