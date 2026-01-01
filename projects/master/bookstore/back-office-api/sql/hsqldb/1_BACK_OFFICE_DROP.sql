-- ============================================
-- Books Stock API: テーブル削除
-- ============================================
-- 書籍マスターと在庫テーブル、部署・社員テーブル、ワークフローテーブル

DROP TABLE WORKFLOW IF EXISTS;

-- 社員・部署テーブル
DROP TABLE EMPLOYEE IF EXISTS;
DROP TABLE DEPARTMENT IF EXISTS;

-- 書籍関連テーブル
DROP TABLE STOCK IF EXISTS;
DROP TABLE BOOK IF EXISTS;
DROP TABLE CATEGORY IF EXISTS;
DROP TABLE PUBLISHER IF EXISTS;