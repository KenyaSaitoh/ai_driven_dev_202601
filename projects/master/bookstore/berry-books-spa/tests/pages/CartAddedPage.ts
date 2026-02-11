import { Page, Locator } from '@playwright/test';

/**
 * カート追加確認ページのPage Object
 */
export class CartAddedPage {
  readonly page: Page;
  readonly pageTitle: Locator;
  readonly viewCartButton: Locator;
  readonly continueShoppingButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.pageTitle = page.locator('h2', { hasText: 'カートに入れました' });
    this.viewCartButton = page.locator('button', { hasText: '買い物カゴを見る' });
    this.continueShoppingButton = page.locator('button', { hasText: '買い物を続ける' });
  }
}
