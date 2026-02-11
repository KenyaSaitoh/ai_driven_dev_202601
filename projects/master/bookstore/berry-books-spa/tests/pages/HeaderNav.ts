import { Page, Locator } from '@playwright/test';

/**
 * ヘッダーナビゲーションのPage Object
 */
export class HeaderNav {
  readonly page: Page;
  readonly bookListLink: Locator;
  readonly bookSearchLink: Locator;
  readonly cartLink: Locator;
  readonly logoutButton: Locator;
  readonly userNameText: Locator;

  constructor(page: Page) {
    this.page = page;
    this.bookListLink = page.locator('a', { hasText: '書籍一覧' });
    this.bookSearchLink = page.locator('a', { hasText: '書籍検索' });
    this.cartLink = page.locator('a', { hasText: 'カート' });
    this.logoutButton = page.locator('button', { hasText: 'ログアウト' });
    this.userNameText = page.locator('text=/.*さん$/');
  }

  /**
   * 書籍一覧ページへ移動
   */
  async goToBookList() {
    await this.bookListLink.click();
  }

  /**
   * 書籍検索ページへ移動
   */
  async goToBookSearch() {
    await this.bookSearchLink.click();
  }

  /**
   * カートページへ移動
   */
  async goToCart() {
    await this.cartLink.click();
  }

  /**
   * ログアウト
   */
  async logout() {
    await this.logoutButton.click();
  }

  /**
   * ユーザー名を取得
   */
  async getUserName(): Promise<string> {
    const text = await this.userNameText.textContent();
    return text?.replace(' さん', '') || '';
  }
}
