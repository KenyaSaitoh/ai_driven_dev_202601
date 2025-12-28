-- ============================================
-- Books Stock API: データ投入
-- ============================================
-- 書籍マスターと在庫データのみ

-- ============================================
-- データ一括削除
-- ============================================
DELETE FROM EMPLOYEE;
DELETE FROM DEPARTMENT;
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
INSERT INTO BOOK (BOOK_ID, BOOK_NAME, AUTHOR, CATEGORY_ID, PUBLISHER_ID, PRICE, IMAGE_URL) VALUES
(1,  'Java SEディープダイブ',                         'Michael Johnson',   1, 3, 3400, '/api/images/covers/1'),
(2,  'JVMとバイトコードの探求',                       'James Lopez',       1, 1, 4200, '/api/images/covers/2'),
(3,  'Javaアーキテクトのための設計原理',              'David Jones',       1, 4, 3000, '/api/images/covers/3'),
(4,  'コンカレントプログラミング in Java SE',         'William Miller',    1, 1, 3500, '/api/images/covers/4'),
(5,  'Javaでのエレガントなコード設計',                'Joseph Davis',      1, 3, 2800, '/api/images/covers/5'),
(6,  'Jakarta EE究極テストガイド',                    'Thomas Rodriguez',  1, 4, 5200, '/api/images/covers/6'),
(7,  'Jakarta EEによるアーキテクチャ設計',            'Chris Wilson',      1, 3, 3200, '/api/images/covers/7'),
(8,  'Jakarta EEパターンライブラリ',                  'Daniel Hall',       1, 1, 4000, '/api/images/covers/8'),

(9,  'SpringBoot in Cloud',                           'Paul Martin',       2, 3, 3000, '/api/images/covers/9'),
(10, 'SpringBootによるエンタープライズ開発',          'Matthew Brown',     2, 2, 3900, '/api/images/covers/10'),
(11, 'SpringBoot魔法のレシピ',                        'Tim Taylor',        2, 4, 4500, '/api/images/covers/11'),
(12, 'SpringBootアーキテクチャの深層',                'Richard White',     2, 1, 2700, '/api/images/covers/12'),
(13, 'SpringBootでのAPI実践',                         'Steven Thomas',     2, 5, 3500, '/api/images/covers/13'),

(14, 'データベースの科学',                            'Mark Jackson',      3, 4, 2500, '/api/images/covers/14'),
(15, '実践！SQLパフォーマンス最適化の秘訣',           'George Harris',     3, 5, 3200, '/api/images/covers/15'),
(16, 'SQLデザインパターン～効率的なクエリ構築',       'Kevin Lewis',       3, 1, 2800, '/api/images/covers/16'),
(17, 'SQLの冒険～RDBの深層',                          'Brian Lee',         3, 2, 2200, '/api/images/covers/17'),
(18, 'SQLアナリティクス実践ガイド',                   'Jason Walker',      3, 4, 4300, '/api/images/covers/18'),

(19, 'HTML5エッセンス～Webの未来',                    'Emily Davis',       4, 1, 2400, '/api/images/covers/19'),
(20, 'HTMLとCSSハンズオンプロジェクト',               'Tim Allen',         4, 3, 2500, '/api/images/covers/20'),
(21, 'HTMLとCSS実践ガイド',                           'Eric Edwards',      4, 4, 3100, '/api/images/covers/21'),
(22, 'Webアクセシビリティ基礎',                       'Nicholas King',     4, 2, 2600, '/api/images/covers/22'),

(23, 'JavaScriptマジック',                            'Adam Wright',       5, 4, 2800, '/api/images/covers/23'),
(24, 'ES6＋完全ガイド',                               'Ryan Hill',         5, 5, 3000, '/api/images/covers/24'),
(25, 'JavaScriptアルゴリズム実践集',                  'Aaron Scott',       5, 1, 3500, '/api/images/covers/25'),
(26, 'JSアーキテクチャパターンの探求',                'Mark Jackson',      5, 4, 4200, '/api/images/covers/26'),
(27, 'Vue・React・Angular徹底比較入門',               'Amanda Brown',      5, 5, 3800, '/api/images/covers/27'),
(28, 'Node.jsによるサーバーサイド開発',               'Sarah Jones',       5, 1, 3500, '/api/images/covers/28'),
(29, 'フロントエンドのためのテスト入門',              'John Smith',        5, 3, 2800, '/api/images/covers/29'),

-- Python (6) 6冊
(30, 'Pythonプログラミング実践入門',                  'Alice Carter',      6, 2, 3000, '/api/images/covers/30'),
(31, 'Pythonデータ分析パターン',                      'Benjamin Clark',    6, 5, 3800, '/api/images/covers/31'),
(32, 'Pythonで学ぶアルゴリズムとデータ構造',          'Charlotte Evans',   6, 1, 3200, '/api/images/covers/32'),
(33, 'テスト自動化のためのPython',                    'Daniel Moore',      6, 3, 3400, '/api/images/covers/33'),
(34, '高速Web開発のためのPythonフレームワーク',       'Ethan Turner',      6, 4, 3600, '/api/images/covers/34'),
(35, 'Pythonで学ぶ並列処理と最適化',                  'Samuel Reed',       6, 5, 3700, '/api/images/covers/35'),

-- 生成AI (7) 6冊
(36, '生成AIシステム設計ガイド',                      'Fiona Walker',      7, 2, 4200, '/api/images/covers/36'),
(37, 'プロンプトエンジニアリング実践',                'Gabriel Harris',    7, 5, 3300, '/api/images/covers/37'),
(38, 'LLMアプリケーションアーキテクチャ',             'Hannah Lewis',      7, 1, 4500, '/api/images/covers/38'),
(39, 'ベクトル検索とRAG入門',                         'Isaac Thompson',    7, 3, 3700, '/api/images/covers/39'),
(40, '生成AIの評価と監視',                            'Julia Martinez',    7, 4, 4000, '/api/images/covers/40'),
(41, 'マルチモーダルAI実践ハンドブック',              'Tara Nguyen',       7, 2, 4400, '/api/images/covers/41'),

-- クラウド (8) 4冊
(42, 'クラウドアーキテクチャ実践パターン',             'Kevin Anderson',    8, 1, 3900, '/api/images/covers/42'),
(43, 'サーバーレス実装ガイド',                         'Laura Baker',       8, 2, 3500, '/api/images/covers/43'),
(44, 'コンテナとオーケストレーション入門',             'Michael Carter',    8, 3, 3600, '/api/images/covers/44'),
(45, 'SREとクラウド運用ハンドブック',                  'Natalie Perez',     8, 4, 4100, '/api/images/covers/45'),

-- AWS (9) 5冊
(46, 'AWS設計原則とベストプラクティス',                'Oliver Ramirez',    9, 5, 4200, '/api/images/covers/46'),
(47, 'AWSネットワークとセキュリティ入門',              'Patricia Scott',    9, 1, 3700, '/api/images/covers/47'),
(48, 'AWSサーバーレスアーキテクチャ実践',              'Quentin Foster',    9, 2, 3800, '/api/images/covers/48'),
(49, 'IaCで進めるAWSインフラ構築',                     'Rachel Hughes',     9, 3, 4000, '/api/images/covers/49'),
(50, 'AWS監視とコスト最適化ガイド',                    'Uma Patel',         9, 4, 3600, '/api/images/covers/50');

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
-- データ投入：DEPARTMENT
-- ============================================
INSERT INTO DEPARTMENT (DEPARTMENT_ID, DEPARTMENT_NAME) VALUES
(1, '企画部'),
(2, '人事部'),
(3, '営業一部'),
(4, '営業二部'),
(5, '営業三部');

-- ============================================
-- データ投入：EMPLOYEE
-- ============================================
-- パスワードは全て "password" をBCryptハッシュ化したもの
-- BCrypt Hash: $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2
-- （customer-hub-apiと同じハッシュを使用）
-- JOB_RANK: 1=ASSOCIATE, 2=MANAGER, 3=DIRECTOR
INSERT INTO EMPLOYEE (EMPLOYEE_ID, EMPLOYEE_CODE, EMPLOYEE_NAME, EMAIL, PASSWORD, DEPARTMENT_ID, JOB_RANK) VALUES
(1, 'E00001', '山田太郎', 'yamada@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 1, 3),
(2, 'E00002', '佐藤花子', 'sato@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 1, 2),
(3, 'E00003', '高橋健太', 'takahashi@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 2, 2),
(4, 'E00004', '伊藤優子', 'ito@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 2, 1),
(5, 'E00005', '渡辺誠', 'watanabe@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 3, 2),
(6, 'E00006', '中村里奈', 'nakamura@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 3, 1),
(7, 'E00007', '小林達也', 'kobayashi@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 3, 1),
(8, 'E00008', '加藤恵', 'kato@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 4, 2),
(9, 'E00009', '吉田麻美', 'yoshida@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 4, 1),
(10, 'E00010', '山本修', 'yamamoto@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 4, 1),
(11, 'E00011', '松本直樹', 'matsumoto@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 5, 2),
(12, 'E00012', '井上真理子', 'inoue@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 5, 1),
(13, 'E00013', '木村真也', 'kimura@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 5, 1);

-- ============================================
-- データ投入：BOOK_WORKFLOW（履歴形式）
-- ============================================
-- 1つのワークフローに対して複数の操作履歴行を持つ
-- WORKFLOW_IDで同じワークフローの操作をグループ化

INSERT INTO BOOK_WORKFLOW (
    OPERATION_ID, WORKFLOW_ID, WORKFLOW_TYPE, STATE,
    BOOK_ID, BOOK_NAME, AUTHOR, CATEGORY_ID, PUBLISHER_ID, PRICE, IMAGE_URL,
    APPLY_REASON, START_DATE, END_DATE,
    OPERATION_TYPE, OPERATED_BY, OPERATED_AT, OPERATION_REASON
) VALUES
-- ============================================
-- ワークフロー1：書籍追加（CREATE）- NEW状態
-- 営業一部の担当者が作成、まだ申請していない
-- ============================================
(1, 1, 'CREATE', 'NEW',
 NULL, 'Java実践プログラミング入門', '田中一郎', 1, 1, 3200, 'https://example.com/images/java-practice.jpg',
 NULL, NULL, NULL,
 'CREATED', 6, TIMESTAMP '2025-01-15 09:00:00', NULL),

-- ============================================
-- ワークフロー2：割引キャンペーン（PRICE_TEMP_ADJUSTMENT）- APPLIED状態
-- 営業二部の担当者が作成・申請、承認待ち
-- ============================================
-- 操作1：作成（NEW状態）
(2, 2, 'PRICE_TEMP_ADJUSTMENT', 'NEW',
 1, NULL, NULL, NULL, NULL, 2400, NULL,
 NULL, DATE '2025-01-20', DATE '2025-01-31',
 'CREATED', 9, TIMESTAMP '2025-01-10 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(3, 2, 'PRICE_TEMP_ADJUSTMENT', 'APPLIED',
 1, NULL, NULL, NULL, NULL, 2400, NULL,
 NULL, DATE '2025-01-20', DATE '2025-01-31',
 'APPLY', 9, TIMESTAMP '2025-01-10 10:00:00', NULL),

-- ============================================
-- ワークフロー3：書籍削除（DELETE）- APPROVED状態
-- 企画部のマネージャーが作成・申請し、ディレクターが承認
-- ============================================
-- 操作1：作成（NEW状態）
(4, 3, 'DELETE', 'NEW',
 50, NULL, NULL, NULL, NULL, NULL, NULL,
 '絶版につき販売終了', NULL, NULL,
 'CREATED', 2, TIMESTAMP '2025-01-08 10:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(5, 3, 'DELETE', 'APPLIED',
 50, NULL, NULL, NULL, NULL, NULL, NULL,
 '絶版につき販売終了', NULL, NULL,
 'APPLY', 2, TIMESTAMP '2025-01-08 11:00:00', NULL),

-- 操作3：承認（APPROVED状態）
(6, 3, 'DELETE', 'APPROVED',
 50, NULL, NULL, NULL, NULL, NULL, NULL,
 '絶版につき販売終了', NULL, NULL,
 'APPROVE', 1, TIMESTAMP '2025-01-09 15:00:00', NULL),

-- ============================================
-- ワークフロー4：書籍削除（DELETE）- REJECTED後NEW状態
-- 営業三部の担当者が作成、申請したが却下されNEWに戻った
-- ============================================
-- 操作1：作成（NEW状態）
(7, 4, 'DELETE', 'NEW',
 45, NULL, NULL, NULL, NULL, NULL, NULL,
 '売れ行きが悪いため', NULL, NULL,
 'CREATED', 12, TIMESTAMP '2025-01-05 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(8, 4, 'DELETE', 'APPLIED',
 45, NULL, NULL, NULL, NULL, NULL, NULL,
 '売れ行きが悪いため', NULL, NULL,
 'APPLY', 12, TIMESTAMP '2025-01-05 10:00:00', NULL),

-- 操作3：却下（NEWに戻る）
(9, 4, 'DELETE', 'NEW',
 45, NULL, NULL, NULL, NULL, NULL, NULL,
 '売れ行きが悪いため', NULL, NULL,
 'REJECT', 11, TIMESTAMP '2025-01-06 14:00:00', '在庫が残っているため削除不可');

-- シーケンスをリセット（HSQLDB用）
ALTER TABLE PUBLISHER ALTER COLUMN PUBLISHER_ID RESTART WITH 6;
ALTER TABLE CATEGORY ALTER COLUMN CATEGORY_ID RESTART WITH 10;
ALTER TABLE BOOK ALTER COLUMN BOOK_ID RESTART WITH 51;
ALTER TABLE DEPARTMENT ALTER COLUMN DEPARTMENT_ID RESTART WITH 6;
ALTER TABLE EMPLOYEE ALTER COLUMN EMPLOYEE_ID RESTART WITH 14;
ALTER TABLE BOOK_WORKFLOW ALTER COLUMN OPERATION_ID RESTART WITH 10;
