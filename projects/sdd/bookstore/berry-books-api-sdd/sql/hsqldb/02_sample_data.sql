-- berry-books-api-sdd Sample Data
-- Database: HSQLDB
-- Purpose: Sample order data for testing

-- ============================================================
-- Sample ORDER_TRAN Data
-- ============================================================
-- Note: CUSTOMER_ID references customer-hub-api managed data
-- Sample customers should exist in the CUSTOMER table (ID: 1, 2, 3)

INSERT INTO ORDER_TRAN (ORDER_TRAN_ID, ORDER_DATE, CUSTOMER_ID, TOTAL_PRICE, DELIVERY_PRICE, DELIVERY_ADDRESS, SETTLEMENT_TYPE) 
VALUES (1, '2026-01-10', 1, 8800, 800, '東京都渋谷区1-1-1', 2);

INSERT INTO ORDER_TRAN (ORDER_TRAN_ID, ORDER_DATE, CUSTOMER_ID, TOTAL_PRICE, DELIVERY_PRICE, DELIVERY_ADDRESS, SETTLEMENT_TYPE) 
VALUES (2, '2026-01-11', 2, 6500, 500, '大阪府大阪市北区2-2-2', 1);

INSERT INTO ORDER_TRAN (ORDER_TRAN_ID, ORDER_DATE, CUSTOMER_ID, TOTAL_PRICE, DELIVERY_PRICE, DELIVERY_ADDRESS, SETTLEMENT_TYPE) 
VALUES (3, '2026-01-12', 1, 11300, 1300, '沖縄県那覇市3-3-3', 3);

-- ============================================================
-- Sample ORDER_DETAIL Data
-- ============================================================
-- Note: BOOK_ID references back-office-api managed data
-- Sample books should exist in the BOOK table (ID: 1, 2, 3, 4, 5)

-- Order 1 details
INSERT INTO ORDER_DETAIL (ORDER_TRAN_ID, ORDER_DETAIL_ID, BOOK_ID, BOOK_NAME, PUBLISHER_NAME, PRICE, COUNT) 
VALUES (1, 1, 1, 'Effective Java', 'Ohmsha', 4000, 2);

-- Order 2 details
INSERT INTO ORDER_DETAIL (ORDER_TRAN_ID, ORDER_DETAIL_ID, BOOK_ID, BOOK_NAME, PUBLISHER_NAME, PRICE, COUNT) 
VALUES (2, 1, 2, 'Clean Code', 'ASCII', 3000, 1);

INSERT INTO ORDER_DETAIL (ORDER_TRAN_ID, ORDER_DETAIL_ID, BOOK_ID, BOOK_NAME, PUBLISHER_NAME, PRICE, COUNT) 
VALUES (2, 2, 3, 'Design Patterns', 'SoftBank', 3000, 1);

-- Order 3 details
INSERT INTO ORDER_DETAIL (ORDER_TRAN_ID, ORDER_DETAIL_ID, BOOK_ID, BOOK_NAME, PUBLISHER_NAME, PRICE, COUNT) 
VALUES (3, 1, 4, 'Refactoring', 'Ohmsha', 5000, 1);

INSERT INTO ORDER_DETAIL (ORDER_TRAN_ID, ORDER_DETAIL_ID, BOOK_ID, BOOK_NAME, PUBLISHER_NAME, PRICE, COUNT) 
VALUES (3, 2, 5, 'The Pragmatic Programmer', 'Ohmsha', 5000, 1);

-- ============================================================
-- Data Summary
-- ============================================================
-- Total orders: 3
-- Total order details: 5
-- Customers referenced: 1, 2 (managed by customer-hub-api)
-- Books referenced: 1, 2, 3, 4, 5 (managed by back-office-api)
