import { Page, Locator } from '@playwright/test';

/**
 * 書籍一覧ページのPage Object
 */
export class BookListPage {
  readonly page: Page;
  readonly pageTitle: Locator;
  readonly booksTable: Locator;
  readonly bookRows: Locator;
  readonly addToCartButtons: Locator;

  constructor(page: Page) {
    this.page = page;
    this.pageTitle = page.locator('h2', { hasText: '書籍を買い物カゴに入れてください' });
    this.booksTable = page.locator('table.table-primary');
    this.bookRows = page.locator('tbody tr');
    this.addToCartButtons = page.locator('button', { hasText: '買い物カゴへ' });
  }

  /**
   * 書籍一覧ページを開く
   */
  async goto() {
    await this.page.goto('/books');
  }

  /**
   * 指定した番号（1始まり）の書籍を買い物カゴに追加
   */
  async addToCartByIndex(index: number) {
    await this.addToCartButtons.nth(index - 1).click();
  }

  /**
   * 書籍行の数を取得
   */
  async getBookCount(): Promise<number> {
    return await this.bookRows.count();
  }

  /**
   * 指定した番号（1始まり）の書籍名を取得
   */
  async getBookNameByIndex(index: number): Promise<string> {
    const row = this.bookRows.nth(index - 1);
    const bookNameCell = row.locator('td').nth(1);
    return await bookNameCell.textContent() || '';
  }
}
