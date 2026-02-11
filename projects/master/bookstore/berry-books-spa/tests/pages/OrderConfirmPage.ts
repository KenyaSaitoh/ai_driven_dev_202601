import { Page, Locator } from '@playwright/test';

/**
 * 注文確認ページのPage Object
 */
export class OrderConfirmPage {
  readonly page: Page;
  readonly pageTitle: Locator;
  readonly deliveryAddressInput: Locator;
  readonly bankTransferRadio: Locator;
  readonly creditCardRadio: Locator;
  readonly cashOnDeliveryRadio: Locator;
  readonly orderButton1: Locator;
  readonly orderButton2: Locator;
  readonly continueShoppingLink: Locator;
  readonly deliveryPriceText: Locator;

  constructor(page: Page) {
    this.page = page;
    this.pageTitle = page.locator('h2', { hasText: '以下の内容で注文します' });
    this.deliveryAddressInput = page.locator('input[type="text"]').first();
    this.bankTransferRadio = page.locator('input[type="radio"][value="1"]');
    this.creditCardRadio = page.locator('input[type="radio"][value="2"]');
    this.cashOnDeliveryRadio = page.locator('input[type="radio"][value="3"]');
    this.orderButton1 = page.locator('#orderButton1');
    this.orderButton2 = page.locator('#orderButton2');
    this.continueShoppingLink = page.locator('#continueLink');
    this.deliveryPriceText = page.locator('text=配送料金').locator('..').locator('text=/¥[0-9,]+/');
  }

  /**
   * 注文確認ページを開く
   */
  async goto() {
    await this.page.goto('/orders/confirm');
  }

  /**
   * 注文情報を入力
   */
  async fillOrderInfo(deliveryAddress: string, settlementType: 'bank' | 'credit' | 'cod') {
    await this.deliveryAddressInput.fill(deliveryAddress);
    
    if (settlementType === 'bank') {
      await this.bankTransferRadio.click();
    } else if (settlementType === 'credit') {
      await this.creditCardRadio.click();
    } else {
      await this.cashOnDeliveryRadio.click();
    }
  }

  /**
   * 注文を確定（方式1）
   */
  async submitOrder1() {
    await this.orderButton1.click();
  }

  /**
   * 注文を確定（方式2）
   */
  async submitOrder2() {
    await this.orderButton2.click();
  }
}
