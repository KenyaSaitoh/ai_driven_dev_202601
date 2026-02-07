# language: ja
機能: 注文履歴取得（結合テスト）
  顧客の注文履歴を取得する

  シナリオ: 注文履歴を取得
    前提 DBに注文が存在する:
      | orderId | customerId | orderDate  |
      | 1       | 1          | 2026-01-01 |
      | 2       | 1          | 2026-01-02 |
    かつ 注文明細が存在する:
      | orderItemId | orderId | bookId | quantity |
      | 1           | 1       | 1      | 2        |
      | 2           | 2       | 2      | 1        |
    もし OrderServiceで注文履歴を取得する（customerId=1）
    ならば 顧客ID=1の注文履歴が返される:
      | orderId | orderDate  | bookId | quantity |
      | 1       | 2026-01-01 | 1      | 2        |
      | 2       | 2026-01-02 | 2      | 1        |
