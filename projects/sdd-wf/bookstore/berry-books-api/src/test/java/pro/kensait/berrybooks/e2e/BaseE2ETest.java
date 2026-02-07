package pro.kensait.berrybooks.e2e;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * E2Eテストのベースクラス
 * 
 * REST Assuredの設定、認証トークン取得などの共通機能を提供する
 * 
 * 使用方法:
 * - E2Eテストクラスはこのクラスを継承する
 * - {@code @Tag("e2e")} を付与し、e2eTestタスクで実行されるようにする
 * - login()メソッドを使用してJWT Cookieを取得する
 */
@Tag("e2e")
public abstract class BaseE2ETest {
    
    /**
     * ベースURL（デフォルト: http://localhost:8080）
     * システムプロパティ "e2e.baseUrl" で上書き可能
     */
    protected static final String BASE_URL = System.getProperty("e2e.baseUrl", "http://localhost:8080");
    
    /**
     * ベースパス（デフォルト: /berry-books-api）
     * システムプロパティ "e2e.basePath" で上書き可能
     */
    protected static final String BASE_PATH = System.getProperty("e2e.basePath", "/berry-books-api");
    
    /**
     * 共通のRequestSpecification
     * Content-Type, Accept ヘッダーを設定
     */
    protected static RequestSpecification requestSpec;
    
    /**
     * REST Assuredの初期設定
     * - baseURI, basePath を設定
     * - Content-Type, Accept ヘッダーを設定
     * - リクエスト/レスポンスのログ出力を有効化（デバッグ用）
     */
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.basePath = BASE_PATH;
        
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
        
        System.out.println("==================================================");
        System.out.println("E2E Test Base URL: " + BASE_URL + BASE_PATH);
        System.out.println("==================================================");
    }
    
    /**
     * ログイン処理を実行し、JWT Cookieを含むResponseを返す
     * 
     * @param email メールアドレス
     * @param password パスワード
     * @return ログインAPIのレスポンス（JWT Cookieを含む）
     */
    protected Response login(String email, String password) {
        return given(requestSpec)
                .body(Map.of(
                        "email", email,
                        "password", password
                ))
                .when()
                .post("/api/auth/login");
    }
    
    /**
     * ログイン処理を実行し、JWT Cookie文字列を返す
     * 
     * @param email メールアドレス
     * @param password パスワード
     * @return JWT Cookie文字列（Cookie名=jwtToken）
     */
    protected String loginAndGetCookie(String email, String password) {
        Response response = login(email, password);
        response.then().statusCode(200);
        return response.getCookie("jwtToken");
    }
    
    /**
     * ログアウト処理を実行する
     * 
     * @return ログアウトAPIのレスポンス
     */
    protected Response logout() {
        return given(requestSpec)
                .when()
                .post("/api/auth/logout");
    }
    
    /**
     * 新規顧客登録を実行する
     * 
     * @param email メールアドレス
     * @param password パスワード
     * @param customerName 顧客名
     * @param address 住所
     * @return 登録APIのレスポンス
     */
    protected Response register(String email, String password, String customerName, String address) {
        return given(requestSpec)
                .body(Map.of(
                        "email", email,
                        "password", password,
                        "customerName", customerName,
                        "address", address
                ))
                .when()
                .post("/api/auth/register");
    }
}
