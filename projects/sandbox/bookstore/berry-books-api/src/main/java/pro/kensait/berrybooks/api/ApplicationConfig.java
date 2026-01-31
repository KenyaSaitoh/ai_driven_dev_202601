package pro.kensait.berrybooks.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS アプリケーション設定
 * すべてのAPIエンドポイントは /api/* パスで公開される
 */
@ApplicationPath("/api")
public class ApplicationConfig extends Application {
    // この設定により、すべての@Pathアノテーション付きクラスが自動的に登録される
    // 明示的にクラスを登録する必要がある場合は、getSingletons()やgetClasses()をオーバーライドする
}

