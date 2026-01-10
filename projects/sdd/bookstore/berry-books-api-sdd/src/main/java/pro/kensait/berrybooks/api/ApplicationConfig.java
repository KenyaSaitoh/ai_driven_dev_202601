package pro.kensait.berrybooks.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RSアプリケーション設定クラス
 * 
 * ベースパス: /api
 */
@ApplicationPath("/api")
public class ApplicationConfig extends Application {
}
