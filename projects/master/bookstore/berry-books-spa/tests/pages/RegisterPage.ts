import { Page, Locator } from '@playwright/test';

/**
 * 新規登録ページのPage Object
 */
export class RegisterPage {
  readonly page: Page;
  readonly customerNameInput: Locator;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly birthdayInput: Locator;
  readonly addressInput: Locator;
  readonly registerButton: Locator;
  readonly cancelButton: Locator;

  constructor(page: Page) {
    this.page = page;
    // 新規登録フォームの各フィールド
    this.customerNameInput = page.locator('input[type="text"]').first();
    this.emailInput = page.locator('input[type="email"]').nth(1);
    this.passwordInput = page.locator('input[type="password"]').nth(1);
    this.birthdayInput = page.locator('input[type="text"]').nth(1);
    this.addressInput = page.locator('input[type="text"]').nth(2);
    this.registerButton = page.locator('button[type="submit"]', { hasText: '登録' });
    this.cancelButton = page.locator('button', { hasText: 'キャンセル' });
  }

  /**
   * 新規登録処理を実行
   */
  async register(data: {
    customerName: string;
    email: string;
    password: string;
    birthday?: string;
    address?: string;
  }) {
    await this.customerNameInput.fill(data.customerName);
    await this.emailInput.fill(data.email);
    await this.passwordInput.fill(data.password);
    if (data.birthday) {
      await this.birthdayInput.fill(data.birthday);
    }
    if (data.address) {
      await this.addressInput.fill(data.address);
    }
    await this.registerButton.click();
  }
}
