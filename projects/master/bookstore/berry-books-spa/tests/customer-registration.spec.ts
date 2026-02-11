import { test, expect } from '@playwright/test';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { HeaderNav } from './pages/HeaderNav';

test.describe('シナリオ 3: 新規顧客登録', () => {
  test('新規ユーザー登録フローを確認する', async ({ page }) => {
    const loginPage = new LoginPage(page);
    const registerPage = new RegisterPage(page);
    const header = new HeaderNav(page);

    // ステップ1: ページを開く
    await loginPage.goto();

    // ステップ2: ページタイトルを確認
    await expect(loginPage.pageTitle).toContainText('Berry Books オンライン書店');

    // ステップ3: お客様のご登録ボタンをクリック
    await loginPage.showRegisterForm();

    // ステップ4: 登録フォームが表示されることを確認
    await expect(registerPage.customerNameInput).toBeVisible();

    // ステップ5-9: 登録情報を入力
    await registerPage.customerNameInput.fill('Frank');
    await registerPage.emailInput.fill('frank@example.com');
    await registerPage.passwordInput.fill('password');
    await registerPage.birthdayInput.fill('2000-01-01');
    await registerPage.addressInput.fill('東京都新宿区1-1-1');

    // ステップ10: 登録ボタンをクリック
    await registerPage.registerButton.click();

    // ステップ11: 登録処理の完了を待つ
    await page.waitForURL('/books', { timeout: 10000 });

    // ステップ12: URLを確認
    await expect(page).toHaveURL('/books');

    // ステップ13: ユーザー名が表示されることを確認
    const userName = await header.getUserName();
    expect(userName).toBe('Frank');

    // ステップ14: ログアウトボタンをクリック
    await header.logout();
  });
});
