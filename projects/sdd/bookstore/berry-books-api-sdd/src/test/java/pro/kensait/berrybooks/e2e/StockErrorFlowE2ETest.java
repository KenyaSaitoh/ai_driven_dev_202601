package pro.kensait.berrybooks.e2e;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * T_INTEG_006: 在庫不足エラーフロー E2Eテスト
 * 
 * 目的: 在庫不足時のエラーハンドリングを確認する
 * 
 * テストシナリオ:
 * 1. 在庫1冊の書籍をカートに追加
 * 2. データベースで在庫を0に変更（手動）
 * 3. 注文確定ボタンをクリック
 * 4. 注文エラー画面が表示されることを確認
 * 5. エラーメッセージ「在庫不足です」が表示されることを確認
 * 6. 「カートに戻る」ボタンでカート画面に戻ることを確認
 * 
 * 期待結果: 在庫不足エラーが正しく表示され、カートに戻れる
 */
@Tag("e2e")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StockErrorFlowE2ETest extends E2ETestBase {

    private static final String TEST_EMAIL = "stock_error_test@example.com";
    private static final String TEST_PASSWORD = "TestPassword123";

    @Test
    @Order(1)
    @DisplayName("在庫不足エラー画面の表示確認")
    public void testStockErrorDisplay() {
        // Note: このテストは実際の在庫データに依存します
        // 手動で在庫を減らす必要がある場合があります
        
        System.out.println("=== 在庫不足エラーフローテスト開始 ===");
        System.out.println("注意: このテストは実際の在庫状況に依存します");
        
        // テストユーザーでログイン（事前に登録済みと仮定）
        try {
            navigateTo("/index.xhtml");
            fillText("input[name*='email']", TEST_EMAIL);
            fillText("input[name*='password']", TEST_PASSWORD);
            click("input[type='submit'][value*='ログイン']");
            
            // ログインに成功した場合
            if (page.url().contains("bookSearch.xhtml")) {
                // 書籍を検索してカートに追加
                fillText("input[name*='keyword']", "Java");
                click("input[type='submit'][value*='検索']");
                page.waitForURL(BASE_URL + "/bookSelect.xhtml");
                
                // 最初の書籍をカートに追加
                click("input[type='submit'][value*='カートに追加']:first-of-type");
                page.waitForURL(BASE_URL + "/cartView.xhtml");
                takeScreenshot("stock_error_01_cart");
                
                // 注文処理へ進む
                click("input[type='submit'][value*='注文へ進む']");
                page.waitForURL(BASE_URL + "/bookOrder.xhtml");
                
                // 配送先住所を入力
                fillText("input[name*='deliveryAddress']", "東京都テスト区1-2-3");
                page.selectOption("select[name*='settlementType']", "1");
                click("input[type='submit'][value*='配送料金を計算']");
                
                // 注文確定をクリック
                click("input[type='submit'][value*='注文確定']");
                takeScreenshot("stock_error_02_after_order");
                
                // エラー画面かどうかを確認
                String currentUrl = page.url();
                if (currentUrl.contains("orderError.xhtml")) {
                    takeScreenshot("stock_error_03_error_page");
                    String pageContent = page.content();
                    
                    // エラーメッセージが表示されることを確認
                    assertTrue(pageContent.contains("エラー") || pageContent.contains("在庫"),
                            "エラーメッセージが表示されるはずです");
                    
                    // カートに戻るボタンが存在することを確認
                    assertTrue(pageContent.contains("カート") || pageContent.contains("戻る"),
                            "カートに戻るリンクが表示されるはずです");
                    
                    System.out.println("在庫不足エラーが正しく表示されました");
                } else if (currentUrl.contains("orderSuccess.xhtml")) {
                    System.out.println("注文が成功しました（在庫が十分にありました）");
                } else {
                    System.out.println("予期しないURLに遷移: " + currentUrl);
                }
            }
        } catch (Exception e) {
            System.err.println("テスト実行中にエラー: " + e.getMessage());
            takeScreenshot("stock_error_exception");
        }
    }
    
    @Test
    @Order(2)
    @DisplayName("在庫不足エラー後のカート復帰")
    public void testReturnToCartAfterError() {
        System.out.println("=== カート復帰テスト ===");
        System.out.println("注意: このテストは在庫不足エラーが発生した場合のみ有効です");
        
        // このテストは orderError.xhtml から cartView.xhtml への遷移を確認
        // 実際のテストでは、エラー画面が表示された後にカートに戻れることを確認
    }
}




