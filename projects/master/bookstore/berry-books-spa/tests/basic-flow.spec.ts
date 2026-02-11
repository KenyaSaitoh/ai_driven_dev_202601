import { test, expect } from '@playwright/test';
import { LoginPage } from './pages/LoginPage';
import { BookListPage } from './pages/BookListPage';
import { BookSearchPage } from './pages/BookSearchPage';
import { CartPage } from './pages/CartPage';
import { OrderConfirmPage } from './pages/OrderConfirmPage';
import { HeaderNav } from './pages/HeaderNav';

test.describe('シナリオ 1: 基本フロー（検証なし）', () => {
  test('ログインから注文までの基本操作を確認する', async ({ page }) => {
    // ダイアログハンドラーの設定（削除確認ダイアログを受諾）
    page.on('dialog', async dialog => await dialog.accept());

    const loginPage = new LoginPage(page);
    const bookListPage = new BookListPage(page);
    const bookSearchPage = new BookSearchPage(page);
    const cartPage = new CartPage(page);
    const orderConfirmPage = new OrderConfirmPage(page);
    const header = new HeaderNav(page);

    // ステップ1: ページを開く
    await loginPage.goto();

    // ステップ2-4: ログイン
    await loginPage.login('alice@example.com', 'password');

    // ステップ5: URLを確認
    await expect(page).toHaveURL('/books');

    // ステップ6: 買い物カゴへボタンをクリック（1番目）
    await bookListPage.addToCartByIndex(1);

    // ステップ7: URLを確認
    await expect(page).toHaveURL('/cart/added');

    // ステップ8: 書籍一覧リンクをクリック
    await header.goToBookList();

    // ステップ9: 買い物カゴへボタンをクリック（2番目）
    await bookListPage.addToCartByIndex(2);

    // ステップ10: 書籍一覧リンクをクリック
    await header.goToBookList();

    // ステップ11: 書籍検索リンクをクリック
    await header.goToBookSearch();

    // ステップ12: カテゴリ選択肢の読み込み完了を待つ
    await bookSearchPage.waitForCategoryOptions();

    // ステップ13: カテゴリを選択（最初の選択肢、すべて以外）
    await bookSearchPage.categorySelect.selectOption({ index: 1 });

    // ステップ14: キーワードを入力
    await bookSearchPage.keywordInput.fill('Cloud');

    // ステップ15: 検索実行ボタンをクリック（JPQL版）
    await bookSearchPage.searchJpqlButton.click();

    // ステップ16: 検索結果が表示されることを確認
    await expect(bookSearchPage.searchResults).toBeVisible();

    // ステップ17: 買い物カゴへボタンをクリック（検索結果の1番目）
    await bookSearchPage.addToCartByIndex(1);

    // ステップ18: カートリンクをクリック
    await header.goToCart();

    // ステップ19: チェックボックスをクリック（カート内の1番目）
    await cartPage.selectItemByIndex(1);

    // ステップ20: 選択した書籍を削除ボタンをクリック
    await cartPage.removeSelected();

    // ステップ21: ダイアログ受諾（OKをクリック）
    // → page.on('dialog') で設定済み

    // ステップ22: 買い物を終了し注文するボタンをクリック
    await cartPage.proceedToCheckout();

    // ステップ23: 配送先住所を入力
    await orderConfirmPage.deliveryAddressInput.fill('東京都新宿区1-1-1');

    // ステップ24: 決済方法を選択（銀行振り込み）
    await orderConfirmPage.bankTransferRadio.click();

    // ステップ25: 注文するボタンをクリック（方式1）
    await orderConfirmPage.submitOrder1();

    // ステップ26: 注文処理完了を待ってURLを確認
    await page.waitForURL('/orders/success', { timeout: 10000 });

    // ステップ27: ログアウトボタンをクリック
    await header.logout();
  });
});
