import { Page, Locator } from '@playwright/test';

/**
 * 注文完了ページのPage Object
 */
export class OrderSuccessPage {
  readonly page: Page;
  readonly pageTitle: Locator;
  readonly orderIdText: Locator;
  readonly viewOrderHistoryButton: Locator;
  readonly backToBookListButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.pageTitle = page.locator('h2', { hasText: '注文が成功しました' });
    this.orderIdText = page.locator('text=注文ID').locator('..').locator('text=/[0-9]+/');
    this.viewOrderHistoryButton = page.locator('button', { hasText: '注文履歴を表示する' });
    this.backToBookListButton = page.locator('button', { hasText: '書籍の選択ページへ' });
  }
}
