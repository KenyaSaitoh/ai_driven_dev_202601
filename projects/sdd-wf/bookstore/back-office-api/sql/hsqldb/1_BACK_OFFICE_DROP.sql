-- ============================================
-- Books Stock API: テーブル削除
-- ============================================
-- 書籍マスターと在庫テーブル、部署・社員テーブル、ワークフローテーブル

DROP TABLE WORKFLOW IF EXISTS CASCADE;

-- 社員・部署テーブル
DROP TABLE EMPLOYEE IF EXISTS CASCADE;
DROP TABLE DEPARTMENT IF EXISTS CASCADE;

-- 書籍関連テーブル
DROP TABLE STOCK IF EXISTS CASCADE;
DROP TABLE BOOK IF EXISTS CASCADE;
DROP TABLE CATEGORY IF EXISTS CASCADE;
DROP TABLE PUBLISHER IF EXISTS CASCADE;
