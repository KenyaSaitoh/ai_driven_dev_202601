import { Page, Locator } from '@playwright/test';

/**
 * 書籍検索ページのPage Object
 */
export class BookSearchPage {
  readonly page: Page;
  readonly pageTitle: Locator;
  readonly categorySelect: Locator;
  readonly keywordInput: Locator;
  readonly searchJpqlButton: Locator;
  readonly searchCriteriaButton: Locator;
  readonly searchResults: Locator;
  readonly searchResultRows: Locator;
  readonly searchResultHeader: Locator;
  readonly addToCartButtons: Locator;

  constructor(page: Page) {
    this.page = page;
    this.pageTitle = page.locator('h2', { hasText: '条件を入力して書籍を検索してください' });
    this.categorySelect = page.locator('#category');
    this.keywordInput = page.locator('#keyword');
    this.searchJpqlButton = page.locator('#search1Button');
    this.searchCriteriaButton = page.locator('#search2Button');
    this.searchResults = page.locator('table.table-primary');
    this.searchResultRows = page.locator('tbody tr');
    this.searchResultHeader = page.locator('h3', { hasText: '検索結果' });
    this.addToCartButtons = page.locator('button', { hasText: '買い物カゴへ' });
  }

  /**
   * 書籍検索ページを開く
   */
  async goto() {
    await this.page.goto('/books/search');
  }

  /**
   * カテゴリ選択肢の読み込み完了を待つ
   */
  async waitForCategoryOptions() {
    // カテゴリ選択肢が2つ以上になるのを待つ（「すべて」以外の選択肢が読み込まれる）
    await this.page.locator('#category option').nth(1).waitFor({ state: 'attached' });
  }

  /**
   * 検索を実行（JPQL版）
   */
  async searchJpql(categoryIndex?: number, keyword?: string) {
    if (categoryIndex !== undefined) {
      // 0=すべて、1以降=カテゴリ
      await this.categorySelect.selectOption({ index: categoryIndex });
    }
    if (keyword) {
      await this.keywordInput.fill(keyword);
    }
    await this.searchJpqlButton.click();
  }

  /**
   * 検索を実行（Criteria版）
   */
  async searchCriteria(categoryIndex?: number, keyword?: string) {
    if (categoryIndex !== undefined) {
      await this.categorySelect.selectOption({ index: categoryIndex });
    }
    if (keyword) {
      await this.keywordInput.fill(keyword);
    }
    await this.searchCriteriaButton.click();
  }

  /**
   * 検索結果の表示を待つ
   * ローディングが完了するまで待機
   */
  async waitForSearchResults() {
    // 検索ボタンがdisabled属性を持たなくなるまで待つ（検索処理完了の確認）
    // loading状態の間、ボタンはdisabledになる
    await this.page.waitForFunction(
      () => {
        const jpqlButton = document.querySelector('#search1Button');
        const criteriaButton = document.querySelector('#search2Button');
        return (jpqlButton && !jpqlButton.hasAttribute('disabled')) ||
               (criteriaButton && !criteriaButton.hasAttribute('disabled'));
      },
      { timeout: 15000 }
    );
    
    // 検索処理が完了してDOMが更新されるまで少し待機
    await this.page.waitForTimeout(300);
  }

  /**
   * 検索結果の指定した番号（1始まり）の書籍を買い物カゴに追加
   */
  async addToCartByIndex(index: number) {
    await this.addToCartButtons.nth(index - 1).click();
  }

  /**
   * 検索結果の行数を取得
   */
  async getResultCount(): Promise<number> {
    return await this.searchResultRows.count();
  }
}
