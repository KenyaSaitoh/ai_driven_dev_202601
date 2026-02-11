import { test, expect } from '@playwright/test';
import { LoginPage } from './pages/LoginPage';
import { BookListPage } from './pages/BookListPage';
import { CartPage } from './pages/CartPage';
import { HeaderNav } from './pages/HeaderNav';

test.describe('シナリオ 5: カート操作（複数商品の削除）', () => {
  test('カート内の複数商品を同時に削除する操作を確認する', async ({ page }) => {
    // ダイアログハンドラーの設定（削除確認ダイアログを受諾）
    page.on('dialog', async dialog => await dialog.accept());

    const loginPage = new LoginPage(page);
    const bookListPage = new BookListPage(page);
    const cartPage = new CartPage(page);
    const header = new HeaderNav(page);

    // ステップ1: ページを開く
    await loginPage.goto();

    // ステップ2-4: ログイン
    await loginPage.login('alice@example.com', 'password');

    // ステップ5: 買い物カゴへボタンをクリック（2番目の書籍）
    await bookListPage.addToCartByIndex(2);

    // ステップ6: 書籍一覧リンクをクリック
    await header.goToBookList();

    // ステップ7: 買い物カゴへボタンをクリック（5番目の書籍）
    await bookListPage.addToCartByIndex(5);

    // ステップ8: 書籍一覧リンクをクリック
    await header.goToBookList();

    // ステップ9: 買い物カゴへボタンをクリック（8番目の書籍）
    await bookListPage.addToCartByIndex(8);

    // ステップ10: カートリンクをクリック
    await header.goToCart();

    // ステップ11: カート内の商品数が3行存在することを確認
    expect(await cartPage.getItemCount()).toBe(3);

    // ステップ12: チェックボックスをクリック（1番目の商品）
    await cartPage.selectItemByIndex(1);

    // ステップ13: チェックボックスをクリック（3番目の商品）
    await cartPage.selectItemByIndex(3);

    // ステップ14: 選択した書籍を削除ボタンをクリック
    await cartPage.removeSelected();

    // ステップ15: ダイアログ受諾（OKをクリック）
    // → page.on('dialog') で設定済み

    // ステップ16: カート内の商品数が1行に減少したことを確認
    await page.waitForTimeout(500); // 削除処理完了を待つ
    expect(await cartPage.getItemCount()).toBe(1);

    // ステップ17: 買い物カゴをクリアするボタンをクリック
    await cartPage.clearCart();

    // ステップ18: ダイアログ受諾（OKをクリック）
    // → page.on('dialog') で設定済み

    // ステップ19: 空のカートメッセージが表示されることを確認
    await page.waitForTimeout(500); // クリア処理完了を待つ
    await expect(cartPage.emptyCartMessage).toBeVisible();
  });
});
