import { test, expect } from '@playwright/test';
import { LoginPage } from './pages/LoginPage';
import { BookSearchPage } from './pages/BookSearchPage';
import { CartPage } from './pages/CartPage';
import { OrderConfirmPage } from './pages/OrderConfirmPage';
import { OrderSuccessPage } from './pages/OrderSuccessPage';
import { HeaderNav } from './pages/HeaderNav';

test.describe('シナリオ 4: 書籍検索と注文（Criteria API版）', () => {
  test('動的クエリ（Criteria API）を使った検索から注文までのフローを確認する', async ({ page }) => {
    const loginPage = new LoginPage(page);
    const bookSearchPage = new BookSearchPage(page);
    const cartPage = new CartPage(page);
    const orderConfirmPage = new OrderConfirmPage(page);
    const orderSuccessPage = new OrderSuccessPage(page);
    const header = new HeaderNav(page);

    // ステップ1: ページを開く
    await loginPage.goto();

    // ステップ2-4: ログイン
    await loginPage.login('alice@example.com', 'password');

    // ステップ5: URLを確認
    await expect(page).toHaveURL('/books');

    // ステップ6: 書籍検索リンクをクリック
    await header.goToBookSearch();

    // ステップ7: URLを確認
    await expect(page).toHaveURL('/books/search');

    // ステップ8: カテゴリ選択肢の読み込み完了を待つ
    await bookSearchPage.waitForCategoryOptions();

    // ステップ9: カテゴリを選択（最初の選択肢、すべて以外）
    await bookSearchPage.categorySelect.selectOption({ index: 1 });

    // ステップ10: キーワードを入力
    await bookSearchPage.keywordInput.fill('Cloud');

    // ステップ11: 検索実行ボタンをクリック（Criteria版）
    await bookSearchPage.searchCriteriaButton.click();

    // ステップ12: 検索結果が表示されることを確認
    await expect(bookSearchPage.searchResults).toBeVisible();

    // ステップ13: 検索結果ヘッダーが表示されることを確認
    await expect(bookSearchPage.searchResultHeader).toBeVisible();

    // ステップ14: 買い物カゴへボタンをクリック（検索結果の1番目）
    await bookSearchPage.addToCartByIndex(1);

    // ステップ15: カートリンクをクリック
    await header.goToCart();

    // ステップ16: 買い物を終了し注文するボタンをクリック
    await cartPage.proceedToCheckout();

    // ステップ17: 配送先住所を入力
    await orderConfirmPage.deliveryAddressInput.fill('沖縄県那覇市1-1-1');

    // ステップ18: 配送料金が¥1,700と表示されることを確認
    await expect(orderConfirmPage.deliveryPriceText).toContainText('¥1,700');

    // ステップ19: 決済方法を選択（クレジットカード）
    await orderConfirmPage.creditCardRadio.click();

    // ステップ20: 注文するボタンをクリック（方式2）
    await orderConfirmPage.submitOrder2();

    // ステップ21: 注文処理の完了を待つ
    await page.waitForURL('/orders/success', { timeout: 10000 });

    // ステップ22: URLを確認
    await expect(page).toHaveURL('/orders/success');

    // ステップ23: ページタイトルを確認
    await expect(orderSuccessPage.pageTitle).toContainText('注文が成功しました');

    // ステップ24: 注文履歴を表示するボタンをクリック
    await orderSuccessPage.viewOrderHistoryButton.click();

    // ステップ25: URLを確認
    await expect(page).toHaveURL('/orders/history');
  });
});
