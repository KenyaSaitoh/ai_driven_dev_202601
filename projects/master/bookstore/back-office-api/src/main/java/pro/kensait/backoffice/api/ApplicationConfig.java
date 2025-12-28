package pro.kensait.backoffice.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Books Stock API - アプリケーション設定
 * 
 * ベースパス: /api
 */
@ApplicationPath("/api")
public class ApplicationConfig extends Application {
    // JAX-RSの自動スキャンを使用するため、実装は不要
}

