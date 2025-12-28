-- ============================================
-- Berry Books API: テーブル削除
-- ============================================
-- 注文テーブルのみ（書籍・在庫はback-office-api、顧客はcustomer-hub-api）

DROP TABLE ORDER_DETAIL IF EXISTS;
DROP TABLE ORDER_TRAN IF EXISTS;
