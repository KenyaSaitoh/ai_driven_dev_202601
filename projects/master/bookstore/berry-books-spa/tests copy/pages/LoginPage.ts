import { Page, Locator } from '@playwright/test';

/**
 * ログインページのPage Object
 */
export class LoginPage {
  readonly page: Page;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly loginButton: Locator;
  readonly registerLink: Locator;
  readonly pageTitle: Locator;

  constructor(page: Page) {
    this.page = page;
    this.emailInput = page.locator('input[type="email"]').first();
    this.passwordInput = page.locator('input[type="password"]').first();
    this.loginButton = page.locator('button[type="submit"]', { hasText: 'ログイン' });
    this.registerLink = page.locator('button', { hasText: 'お客様のご登録' });
    this.pageTitle = page.locator('h2', { hasText: 'Berry Books オンライン書店' });
  }

  /**
   * ログインページを開く
   */
  async goto() {
    await this.page.goto('/');
  }

  /**
   * ログイン処理を実行
   */
  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.loginButton.click();
  }

  /**
   * 新規登録フォームを表示
   */
  async showRegisterForm() {
    await this.registerLink.click();
  }
}
