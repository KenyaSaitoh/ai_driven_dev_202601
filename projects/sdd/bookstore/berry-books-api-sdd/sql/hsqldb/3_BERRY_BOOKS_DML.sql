-- ============================================
-- Berry Books API (SDD): データ投入
-- ============================================
-- 注文データのみ（書籍・在庫はback-office-api-sdd、顧客はcustomer-hub-api）

-- ============================================
-- データ一括削除
-- ============================================
DELETE FROM ORDER_DETAIL;
DELETE FROM ORDER_TRAN;

-- ============================================
-- データ投入：ORDER_TRAN
-- ============================================
INSERT INTO ORDER_TRAN
(ORDER_TRAN_ID, ORDER_DATE,  CUSTOMER_ID, TOTAL_PRICE, DELIVERY_PRICE, DELIVERY_ADDRESS, SETTLEMENT_TYPE)
VALUES
(1, DATE '2023-03-01', 1, 5600, 500, '東京都中央区1-1-1', 1),
(2, DATE '2023-04-01', 1, 5700, 500, '東京都中央区1-1-1', 2),
(3, DATE '2023-05-01', 1,11500, 500, '東京都中央区1-1-1', 3),
(4, DATE '2023-06-01', 1, 4800, 500, '東京都中央区1-1-1', 1);

-- ============================================
-- データ投入：ORDER_DETAIL
-- 注意: BOOK_NAME, PUBLISHER_NAMEカラム（スナップショット：注文時点のデータを保持）
-- ============================================
INSERT INTO ORDER_DETAIL (ORDER_TRAN_ID, ORDER_DETAIL_ID, BOOK_ID, BOOK_NAME, PUBLISHER_NAME, PRICE, COUNT) VALUES
-- Order 1: 3400 + 2200 = 5600
(1, 1, 1,  'Java SEディープダイブ', 'ネットワークノード出版', 3400, 1),
(1, 2, 17, 'SQLの冒険～RDBの深層', 'コードブレイクプレス', 2200, 1),

-- Order 2: 3200 + 2500 = 5700
(2, 1, 12, 'SpringBootでのAPI実践', 'ネットワークノード出版', 3200, 1),
(2, 2, 26, 'ES6＋完全ガイド', 'デジタルフロンティア出版', 2500, 1),

-- Order 3: 3000 + 4000 + 4500 = 11500
(3, 1, 9,  'SpringBoot in Cloud', 'ネットワークノード出版', 3000, 1),
(3, 2, 29, 'JSアーキテクチャパターンの探求', 'デジタルフロンティア出版', 4000, 1),
(3, 3, 46, 'テスト自動化のためのPython', 'デジタルフロンティア出版', 4500, 1);

-- シーケンスをリセット（HSQLDB用）
ALTER TABLE ORDER_TRAN ALTER COLUMN ORDER_TRAN_ID RESTART WITH 5;
