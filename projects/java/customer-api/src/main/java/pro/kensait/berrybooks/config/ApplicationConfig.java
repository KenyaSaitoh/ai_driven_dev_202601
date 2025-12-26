package pro.kensait.berrybooks.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

// JAX-RS アプリケーション設定クラス（/ 以下のパスでREST APIを公開する）
@ApplicationPath("/")
public class ApplicationConfig extends Application {
    // デフォルトでは全てのJAX-RSリソースが自動検出される
}

