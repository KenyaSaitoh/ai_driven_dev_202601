// 顧客情報の型定義
export interface Customer {
  customerId: number;
  customerName: string;
  email: string;
  birthday?: string; // オプション（nullの可能性あり）
  address: string;
}

