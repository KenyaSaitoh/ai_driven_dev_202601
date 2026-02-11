import { Page, Locator } from '@playwright/test';

/**
 * カートページのPage Object
 */
export class CartPage {
  readonly page: Page;
  readonly pageTitle: Locator;
  readonly cartItems: Locator;
  readonly checkboxes: Locator;
  readonly removeSelectedButton: Locator;
  readonly clearCartButton: Locator;
  readonly checkoutButton: Locator;
  readonly continueShoppingButton: Locator;
  readonly emptyCartMessage: Locator;

  constructor(page: Page) {
    this.page = page;
    this.pageTitle = page.locator('h2', { hasText: '現在の買い物カゴの内容です' });
    this.cartItems = page.locator('tbody tr');
    this.checkboxes = page.locator('input[type="checkbox"]');
    this.removeSelectedButton = page.locator('button', { hasText: '選択した書籍をカートから削除する' });
    this.clearCartButton = page.locator('button', { hasText: '買い物カゴをクリアする' });
    this.checkoutButton = page.locator('button', { hasText: '買い物を終了し注文する' });
    this.continueShoppingButton = page.locator('button', { hasText: '買い物を続ける' });
    this.emptyCartMessage = page.locator('p', { hasText: 'カートは空です' });
  }

  /**
   * カートページを開く
   */
  async goto() {
    await this.page.goto('/cart');
  }

  /**
   * 指定した番号（1始まり）のチェックボックスを選択
   */
  async selectItemByIndex(index: number) {
    await this.checkboxes.nth(index - 1).click();
  }

  /**
   * 選択した書籍を削除（ダイアログハンドラー設定必要）
   */
  async removeSelected() {
    await this.removeSelectedButton.click();
  }

  /**
   * カートをクリア（ダイアログハンドラー設定必要）
   */
  async clearCart() {
    await this.clearCartButton.click();
  }

  /**
   * 注文画面へ進む
   */
  async proceedToCheckout() {
    await this.checkoutButton.click();
  }

  /**
   * カート内のアイテム数を取得
   */
  async getItemCount(): Promise<number> {
    return await this.cartItems.count();
  }
}
