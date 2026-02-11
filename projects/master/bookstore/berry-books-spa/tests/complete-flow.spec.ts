import { test, expect } from '@playwright/test';
import { LoginPage } from './pages/LoginPage';
import { BookListPage } from './pages/BookListPage';
import { BookSearchPage } from './pages/BookSearchPage';
import { CartPage } from './pages/CartPage';
import { OrderConfirmPage } from './pages/OrderConfirmPage';
import { OrderSuccessPage } from './pages/OrderSuccessPage';
import { HeaderNav } from './pages/HeaderNav';

test.describe('シナリオ 2: 完全フロー（検証あり）', () => {
  test('全画面遷移と各ページの要素を検証する', async ({ page }) => {
    // ダイアログハンドラーの設定（削除確認ダイアログを受諾）
    page.on('dialog', async dialog => await dialog.accept());

    const loginPage = new LoginPage(page);
    const bookListPage = new BookListPage(page);
    const bookSearchPage = new BookSearchPage(page);
    const cartPage = new CartPage(page);
    const orderConfirmPage = new OrderConfirmPage(page);
    const orderSuccessPage = new OrderSuccessPage(page);
    const header = new HeaderNav(page);

    // ステップ1: ページを開く
    await loginPage.goto();

    // ステップ2: ページタイトルを確認
    await expect(loginPage.pageTitle).toContainText('Berry Books オンライン書店');

    // ステップ3-4: ログイン情報を入力
    await loginPage.emailInput.fill('alice@example.com');
    await loginPage.passwordInput.fill('password');

    // ステップ5: ログインボタンをクリック
    await loginPage.loginButton.click();

    // ステップ6: URLを確認
    await expect(page).toHaveURL('/books');

    // ステップ7: ページタイトルを確認
    await expect(bookListPage.pageTitle).toContainText('書籍を買い物カゴに入れてください');

    // ステップ8: 書籍一覧が30行以上存在することを確認
    const bookCount = await bookListPage.getBookCount();
    expect(bookCount).toBeGreaterThanOrEqual(30);

    // ステップ9: 1行目の書籍名が存在することを確認
    const firstBookName = await bookListPage.getBookNameByIndex(1);
    expect(firstBookName.length).toBeGreaterThan(0);

    // ステップ10: 買い物カゴへボタンをクリック（3番目の書籍）
    await bookListPage.addToCartByIndex(3);

    // ステップ11: URLを確認
    await expect(page).toHaveURL('/cart/added');

    // ステップ12: ページタイトルが表示されることを確認
    await expect(page.locator('h2', { hasText: 'カートに入れました' })).toBeVisible();

    // ステップ13: 書籍一覧リンクをクリック
    await header.goToBookList();

    // ステップ14: URLを確認
    await expect(page).toHaveURL('/books');

    // ステップ15: 買い物カゴへボタンをクリック（5番目の書籍）
    await bookListPage.addToCartByIndex(5);

    // ステップ16: URLを確認
    await expect(page).toHaveURL('/cart/added');

    // ステップ17: 書籍一覧リンクをクリック
    await header.goToBookList();

    // ステップ18: 書籍検索リンクをクリック
    await header.goToBookSearch();

    // ステップ19: URLを確認
    await expect(page).toHaveURL('/books/search');

    // ステップ20: ページタイトルを確認
    await expect(bookSearchPage.pageTitle).toContainText('条件を入力して書籍を検索してください');

    // ステップ21: カテゴリ選択肢の読み込み完了を待つ
    await bookSearchPage.waitForCategoryOptions();

    // ステップ22: カテゴリを選択（最初の選択肢、すべて以外）
    await bookSearchPage.categorySelect.selectOption({ index: 1 });

    // ステップ23: キーワードを入力
    await bookSearchPage.keywordInput.fill('Cloud');

    // ステップ24: 検索実行ボタンをクリック（JPQL版）
    await bookSearchPage.searchJpqlButton.click();

    // ステップ25: 検索結果が表示されることを確認
    await expect(bookSearchPage.searchResults).toBeVisible();

    // ステップ26: 検索結果ヘッダーが表示されることを確認
    await expect(bookSearchPage.searchResultHeader).toBeVisible();

    // ステップ27: 検索結果が1行以上存在することを確認
    const resultCount = await bookSearchPage.getResultCount();
    expect(resultCount).toBeGreaterThanOrEqual(1);

    // ステップ28: 買い物カゴへボタンをクリック（検索結果の1番目）
    await bookSearchPage.addToCartByIndex(1);

    // ステップ29: URLを確認
    await expect(page).toHaveURL('/cart/added');

    // ステップ30: カートリンクをクリック
    await header.goToCart();

    // ステップ31: URLを確認
    await expect(page).toHaveURL('/cart');

    // ステップ32: ページタイトルを確認
    await expect(cartPage.pageTitle).toContainText('現在の買い物カゴの内容です');

    // ステップ33: カート内の商品数が3行存在することを確認
    expect(await cartPage.getItemCount()).toBe(3);

    // ステップ34: チェックボックスをクリック（1番目の商品）
    await cartPage.selectItemByIndex(1);

    // ステップ35: 選択した書籍を削除ボタンをクリック
    await cartPage.removeSelected();

    // ステップ36: ダイアログ受諾（OKをクリック）
    // → page.on('dialog') で設定済み

    // ステップ37: カート内の商品数が2行に減少したことを確認
    await page.waitForTimeout(500); // 削除処理完了を待つ
    expect(await cartPage.getItemCount()).toBe(2);

    // ステップ38: 買い物を終了し注文するボタンをクリック
    await cartPage.proceedToCheckout();

    // ステップ39: URLを確認
    await expect(page).toHaveURL('/orders/confirm');

    // ステップ40: ページタイトルを確認
    await expect(orderConfirmPage.pageTitle).toContainText('以下の内容で注文します');

    // ステップ41: 配送先住所を入力
    await orderConfirmPage.deliveryAddressInput.fill('東京都渋谷区1-2-3');

    // ステップ42: 決済方法を選択（銀行振り込み）
    await orderConfirmPage.bankTransferRadio.click();

    // ステップ43: 注文するボタンをクリック（方式1）
    await orderConfirmPage.submitOrder1();

    // ステップ44: 注文処理の完了を待つ
    await page.waitForURL('/orders/success', { timeout: 10000 });

    // ステップ45: URLを確認
    await expect(page).toHaveURL('/orders/success');

    // ステップ46: ページタイトルを確認
    await expect(orderSuccessPage.pageTitle).toContainText('注文が成功しました');

    // ステップ47: 注文IDが数値で表示されることを確認
    await expect(orderSuccessPage.orderIdText).toBeVisible();
    const orderIdText = await orderSuccessPage.orderIdText.textContent();
    expect(orderIdText).toMatch(/^[0-9]+$/);

    // ステップ48: ログアウトボタンをクリック
    await header.logout();

    // ステップ49: URLを確認
    await expect(page).toHaveURL('/');
  });
});
