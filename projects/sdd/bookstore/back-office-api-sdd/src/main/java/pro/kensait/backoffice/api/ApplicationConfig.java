package pro.kensait.backoffice.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RSアプリケーション設定
 * 
 * ベースパス: /api
 */
@ApplicationPath("/api")
public class ApplicationConfig extends Application {
    // デフォルトの自動スキャンを使用
}
