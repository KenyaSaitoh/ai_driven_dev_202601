-- ============================================
-- データ一括削除
-- ============================================
DELETE FROM ORDER_DETAIL;
DELETE FROM ORDER_TRAN;
DELETE FROM CUSTOMER;
DELETE FROM STOCK;
DELETE FROM BOOK;
DELETE FROM CATEGORY;
DELETE FROM PUBLISHER;

-- ============================================
-- データ投入：PUBLISHER / CATEGORY
-- ============================================
INSERT INTO PUBLISHER (PUBLISHER_ID, PUBLISHER_NAME) VALUES
(1, 'デジタルフロンティア出版'),
(2, 'コードブレイクプレス'),
(3, 'ネットワークノード出版'),
(4, 'クラウドキャスティング社'),
(5, 'データドリフト社');

INSERT INTO CATEGORY (CATEGORY_ID, CATEGORY_NAME) VALUES
(1, 'Java'),
(2, 'SpringBoot'),
(3, 'SQL'),
(4, 'HTML/CSS'),
(5, 'JavaScript'),
(6, 'Python'),
(7, '生成AI'),
(8, 'クラウド'),
(9, 'AWS');

-- ============================================
-- データ投入：BOOK（最新50冊＝ID 1..50）
-- ============================================
INSERT INTO BOOK (BOOK_ID, BOOK_NAME, AUTHOR, CATEGORY_ID, PUBLISHER_ID, PRICE) VALUES
(1,  'Java SEディープダイブ',                         'Michael Johnson',   1, 3, 3400),
(2,  'JVMとバイトコードの探求',                       'James Lopez',       1, 1, 4200),
(3,  'Javaアーキテクトのための設計原理',              'David Jones',       1, 4, 3000),
(4,  'コンカレントプログラミング in Java SE',         'William Miller',    1, 1, 3500),
(5,  'Javaでのエレガントなコード設計',                'Joseph Davis',      1, 3, 2800),
(6,  'Jakarta EE究極テストガイド',                    'Thomas Rodriguez',  1, 4, 5200),
(7,  'Jakarta EEによるアーキテクチャ設計',            'Chris Wilson',      1, 3, 3200),
(8,  'Jakarta EEパターンライブラリ',                  'Daniel Hall',       1, 1, 4000),

(9,  'SpringBoot in Cloud',                           'Paul Martin',       2, 3, 3000),
(10, 'SpringBootによるエンタープライズ開発',          'Matthew Brown',     2, 2, 3900),
(11, 'SpringBoot魔法のレシピ',                        'Tim Taylor',        2, 4, 4500),
(12, 'SpringBootアーキテクチャの深層',                'Richard White',     2, 1, 2700),
(13, 'SpringBootでのAPI実践',                         'Steven Thomas',     2, 5, 3500),

(14, 'データベースの科学',                            'Mark Jackson',      3, 4, 2500),
(15, '実践！SQLパフォーマンス最適化の秘訣',           'George Harris',     3, 5, 3200),
(16, 'SQLデザインパターン～効率的なクエリ構築',       'Kevin Lewis',       3, 1, 2800),
(17, 'SQLの冒険～RDBの深層',                          'Brian Lee',         3, 2, 2200),
(18, 'SQLアナリティクス実践ガイド',                   'Jason Walker',      3, 4, 4300),

(19, 'HTML5エッセンス～Webの未来',                    'Emily Davis',       4, 1, 2400),
(20, 'HTMLとCSSハンズオンプロジェクト',               'Tim Allen',         4, 3, 2500),
(21, 'HTMLとCSS実践ガイド',                           'Eric Edwards',      4, 4, 3100),
(22, 'Webアクセシビリティ基礎',                       'Nicholas King',     4, 2, 2600),

(23, 'JavaScriptマジック',                            'Adam Wright',       5, 4, 2800),
(24, 'ES6＋完全ガイド',                               'Ryan Hill',         5, 5, 3000),
(25, 'JavaScriptアルゴリズム実践集',                  'Aaron Scott',       5, 1, 3500),
(26, 'JSアーキテクチャパターンの探求',                'Mark Jackson',      5, 4, 4200),
(27, 'Vue・React・Angular徹底比較入門',               'Amanda Brown',      5, 5, 3800),
(28, 'Node.jsによるサーバーサイド開発',               'Sarah Jones',       5, 1, 3500),
(29, 'フロントエンドのためのテスト入門',              'John Smith',        5, 3, 2800),

-- Python (6) 6冊
(30, 'Pythonプログラミング実践入門',                  'Alice Carter',      6, 2, 3000),
(31, 'Pythonデータ分析パターン',                      'Benjamin Clark',    6, 5, 3800),
(32, 'Pythonで学ぶアルゴリズムとデータ構造',          'Charlotte Evans',   6, 1, 3200),
(33, 'テスト自動化のためのPython',                    'Daniel Moore',      6, 3, 3400),
(34, '高速Web開発のためのPythonフレームワーク',       'Ethan Turner',      6, 4, 3600),
(35, 'Pythonで学ぶ並列処理と最適化',                  'Samuel Reed',       6, 5, 3700),

-- 生成AI (7) 6冊
(36, '生成AIシステム設計ガイド',                      'Fiona Walker',      7, 2, 4200),
(37, 'プロンプトエンジニアリング実践',                'Gabriel Harris',    7, 5, 3300),
(38, 'LLMアプリケーションアーキテクチャ',             'Hannah Lewis',      7, 1, 4500),
(39, 'ベクトル検索とRAG入門',                         'Isaac Thompson',    7, 3, 3700),
(40, '生成AIの評価と監視',                            'Julia Martinez',    7, 4, 4000),
(41, 'マルチモーダルAI実践ハンドブック',              'Tara Nguyen',       7, 2, 4400),

-- クラウド (8) 4冊
(42, 'クラウドアーキテクチャ実践パターン',             'Kevin Anderson',    8, 1, 3900),
(43, 'サーバーレス実装ガイド',                         'Laura Baker',       8, 2, 3500),
(44, 'コンテナとオーケストレーション入門',             'Michael Carter',    8, 3, 3600),
(45, 'SREとクラウド運用ハンドブック',                  'Natalie Perez',     8, 4, 4100),

-- AWS (9) 5冊
(46, 'AWS設計原則とベストプラクティス',                'Oliver Ramirez',    9, 5, 4200),
(47, 'AWSネットワークとセキュリティ入門',              'Patricia Scott',    9, 1, 3700),
(48, 'AWSサーバーレスアーキテクチャ実践',              'Quentin Foster',    9, 2, 3800),
(49, 'IaCで進めるAWSインフラ構築',                     'Rachel Hughes',     9, 3, 4000),
(50, 'AWS監視とコスト最適化ガイド',                    'Uma Patel',         9, 4, 3600);

-- ============================================
-- データ投入：CUSTOMER
-- ============================================
INSERT INTO CUSTOMER VALUES(1, 'Alice', 'password', 'alice@gmail.com', '1998-04-10', '東京都中央区1-1-1');
INSERT INTO CUSTOMER VALUES(2, 'Bob', 'password', 'bob@gmail.com', '1988-05-10', '東京都杉並区2-2-2');
INSERT INTO CUSTOMER VALUES(3, 'Carol', 'password', 'carol@gmail.com', '1993-06-10', '東京都文教区3-3-3');
INSERT INTO CUSTOMER VALUES(4, 'Dave', 'password', 'dave@gmail.com', '1990-07-10', '東京都品川区4-4-4');
INSERT INTO CUSTOMER VALUES(5, 'Ellen', 'password', 'ellen@gmail.com', '1999-08-10', '東京都中野区5-5-5');

-- ============================================
-- データ投入：STOCK
-- テスト用に在庫を少なめに設定、在庫1冊を5冊配置
-- ============================================
INSERT INTO STOCK VALUES
-- Java (1-8): 8冊
(1,  3, 0), (2,  2, 0), (3,  1, 0), (4,  3, 0), (5,  2, 0),
(6,  2, 0), (7,  0, 0), (8,  3, 0),

-- SpringBoot (9-13): 5冊
(9,  2, 0), (10, 3, 0), (11, 2, 0), (12, 1, 0), (13, 3, 0),

-- SQL (14-18): 5冊
(14, 3, 0), (15, 2, 0), (16, 0, 0), (17, 3, 0), (18, 2, 0),

-- HTML/CSS (19-22): 4冊
(19, 3, 0), (20, 2, 0), (21, 1, 0), (22, 0, 0),

-- JavaScript (23-29): 7冊
(23, 2, 0), (24, 3, 0), (25, 2, 0), (26, 2, 0), (27, 3, 0),
(28, 1, 0), (29, 2, 0),

-- Python (30-35): 6冊
(30, 2, 0), (31, 2, 0), (32, 3, 0), (33, 0, 0), (34, 2, 0),
(35, 2, 0),

-- 生成AI (36-41): 6冊
(36, 2, 0), (37, 3, 0), (38, 1, 0), (39, 2, 0), (40, 0, 0),
(41, 2, 0),

-- クラウド (42-45): 4冊
(42, 3, 0), (43, 2, 0), (44, 2, 0), (45, 2, 0),

-- AWS (46-50): 5冊
(46, 2, 0), (47, 2, 0), (48, 2, 0), (49, 0, 0), (50, 3, 0);

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
-- ============================================
INSERT INTO ORDER_DETAIL VALUES
-- Order 1: 3400 + 2200 = 5600
(1, 1, 1,  3400, 1),   -- BOOK_ID=1  Java SEディープダイブ
(1, 2, 17, 2200, 1),   -- BOOK_ID=17 SQLの冒険～RDBの深層

-- Order 2: 2700 + 3000 = 5700
(2, 1, 12, 2700, 1),   -- SpringBootアーキテクチャの深層
(2, 2, 24, 3000, 1),   -- ES6＋完全ガイド

-- Order 3: 3900 + 4200 + 3400 = 11500
(3, 1, 10, 3900, 1),   -- SpringBoot in Cloud
(3, 2, 26, 4200, 1),   -- JSアーキテクチャパターンの探求
(3, 3, 33, 3400, 1);   -- テスト自動化のためのPython

-- シーケンスをリセット（HSQLDB用）
ALTER TABLE PUBLISHER ALTER COLUMN PUBLISHER_ID RESTART WITH 6;
ALTER TABLE CATEGORY ALTER COLUMN CATEGORY_ID RESTART WITH 6;
ALTER TABLE BOOK ALTER COLUMN BOOK_ID RESTART WITH 35;
ALTER TABLE CUSTOMER ALTER COLUMN CUSTOMER_ID RESTART WITH 6;
ALTER TABLE ORDER_TRAN ALTER COLUMN ORDER_TRAN_ID RESTART WITH 5;
