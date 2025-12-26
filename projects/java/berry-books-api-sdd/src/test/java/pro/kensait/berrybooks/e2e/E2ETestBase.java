package pro.kensait.berrybooks.e2e;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Paths;

/**
 * E2Eテストの基底クラス
 * 
 * Playwright（Java）を使用したエンドツーエンドテストの基盤を提供します。
 * 
 * 主な機能:
 * - ブラウザの起動と終了
 * - ページコンテキストの管理
 * - スクリーンショット取得
 * - 共通のテストユーティリティ
 */
public abstract class E2ETestBase {

    protected static Playwright playwright;
    protected static Browser browser;
    protected BrowserContext context;
    protected Page page;
    
    // アプリケーションのベースURL
    protected static final String BASE_URL = "http://localhost:8080/berry-books";
    
    // スクリーンショット保存先
    protected static final String SCREENSHOT_DIR = "build/test-results/screenshots/";
    
    @BeforeAll
    public static void launchBrowser() {
        playwright = Playwright.create();
        // Chromiumブラウザを起動（ヘッドレスモード）
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setSlowMo(50)); // 操作を少し遅くして安定性を向上
        
        System.out.println("=== E2E Test Browser Launched ===");
    }
    
    @BeforeEach
    public void createContextAndPage() {
        // 新しいブラウザコンテキストを作成（各テストで独立したセッション）
        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1280, 720)
                .setLocale("ja-JP"));
        
        page = context.newPage();
        System.out.println("=== New Page Context Created ===");
    }
    
    @AfterEach
    public void closeContext() {
        if (context != null) {
            context.close();
        }
        System.out.println("=== Page Context Closed ===");
    }
    
    @AfterAll
    public static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        System.out.println("=== E2E Test Browser Closed ===");
    }
    
    /**
     * ページに遷移
     */
    protected void navigateTo(String path) {
        String url = BASE_URL + path;
        page.navigate(url);
        System.out.println("Navigated to: " + url);
    }
    
    /**
     * スクリーンショットを保存
     */
    protected void takeScreenshot(String filename) {
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get(SCREENSHOT_DIR + filename + ".png"))
                .setFullPage(true));
        System.out.println("Screenshot saved: " + filename + ".png");
    }
    
    /**
     * 要素が表示されるまで待機
     */
    protected void waitForElement(String selector) {
        page.waitForSelector(selector, new Page.WaitForSelectorOptions()
                .setTimeout(5000));
    }
    
    /**
     * テキストを入力
     */
    protected void fillText(String selector, String text) {
        page.fill(selector, text);
    }
    
    /**
     * ボタンをクリック
     */
    protected void click(String selector) {
        page.click(selector);
    }
    
    /**
     * ログイン処理（共通ヘルパー）
     */
    protected void login(String email, String password) {
        navigateTo("/index.xhtml");
        fillText("input[name*='email']", email);
        fillText("input[name*='password']", password);
        click("input[type='submit'][value*='ログイン']");
        
        // ログイン成功後、書籍検索画面に遷移するまで待機
        page.waitForURL(BASE_URL + "/bookSearch.xhtml", new Page.WaitForURLOptions()
                .setTimeout(5000));
    }
    
    /**
     * ログアウト処理（共通ヘルパー）
     */
    protected void logout() {
        click("a:has-text('ログアウト')");
        page.waitForURL(BASE_URL + "/index.xhtml");
    }
}




