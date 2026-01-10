-- ============================================
-- Books Stock API: データ投入
-- ============================================
-- 書籍マスターと在庫データのみ

-- ============================================
-- データ一括削除
-- ============================================
DELETE FROM WORKFLOW;
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
(1, '営業一部'),
(2, '営業二部'),
(3, '企画部');

-- ============================================
-- データ投入：EMPLOYEE
-- ============================================
-- パスワードは全て "password" をBCryptハッシュ化したもの
-- BCrypt Hash: $2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2
-- （customer-hub-apiと同じハッシュを使用）
-- JOB_RANK: 1=ASSOCIATE, 2=MANAGER, 3=DIRECTOR
INSERT INTO EMPLOYEE (EMPLOYEE_ID, EMPLOYEE_CODE, EMPLOYEE_NAME, EMAIL, PASSWORD, DEPARTMENT_ID, JOB_RANK) VALUES
-- 営業一部：5人（ASSOCIATE 3人、MANAGER 1人、DIRECTOR 1人）
(1, 'E00001', '渡辺 誠', 'watanabe@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 1, 1),
(2, 'E00002', '中村 里奈', 'nakamura@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 1, 1),
(3, 'E00003', '小林 達也', 'kobayashi@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 1, 1),
(4, 'E00004', '坂本 徹', 'sakamoto@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 1, 2),
(5, 'E00005', '佐々木 良太', 'sasaki@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 1, 3),
-- 営業二部：5人（ASSOCIATE 3人、MANAGER 1人、DIRECTOR 1人）
(6, 'E00006', '加藤 恵', 'kato@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 2, 1),
(7, 'E00007', '吉田 麻美', 'yoshida@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 2, 1),
(8, 'E00008', '山本 修', 'yamamoto@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 2, 1),
(9, 'E00009', '井上 真理子', 'inoue@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 2, 2),
(10, 'E00010', '木村 真也', 'kimura@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 2, 3),
-- 企画部：3人（ASSOCIATE 1人、MANAGER 1人、DIRECTOR 1人）
(11, 'E00011', '高橋 健太', 'takahashi@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 3, 1),
(12, 'E00012', '佐藤 花子', 'sato@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 3, 2),
(13, 'E00013', '山田 太郎', 'yamada@example.com', '$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2', 3, 3);

-- ============================================
-- データ投入：WORKFLOW（履歴形式）
-- ============================================
-- 1つのワークフローに対して複数の操作履歴行を持つ
-- WORKFLOW_IDで同じワークフローの操作をグループ化

INSERT INTO WORKFLOW (
    OPERATION_ID, WORKFLOW_ID, WORKFLOW_TYPE, STATE,
    BOOK_ID, BOOK_NAME, AUTHOR, CATEGORY_ID, PUBLISHER_ID, PRICE, IMAGE_URL,
    APPLY_REASON, START_DATE, END_DATE,
    OPERATION_TYPE, OPERATED_BY, OPERATED_AT, OPERATION_REASON
) VALUES
-- ============================================
-- ワークフロー1：書籍追加（ADD_NEW_BOOK）- CREATED状態
-- 営業一部の渡辺が作成、まだ申請していない
-- ============================================
(1, 1, 'ADD_NEW_BOOK', 'CREATED',
 NULL, 'Java実践プログラミング入門', '田中一郎', 1, 1, 3200, 'https://example.com/images/java-practice.jpg',
 NULL, NULL, NULL,
 'CREATE', 1, TIMESTAMP '2025-01-15 09:00:00', NULL),

-- ============================================
-- ワークフロー2：割引キャンペーン（ADJUST_BOOK_PRICE）- APPLIED状態
-- 営業二部の加藤が作成・申請、承認待ち
-- ============================================
-- 操作1：作成（CREATED状態）
(2, 2, 'ADJUST_BOOK_PRICE', 'CREATED',
 1, NULL, NULL, NULL, NULL, 2400, NULL,
 NULL, DATE '2025-01-20', DATE '2025-01-31',
 'CREATE', 6, TIMESTAMP '2025-01-10 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(3, 2, 'ADJUST_BOOK_PRICE', 'APPLIED',
 1, NULL, NULL, NULL, NULL, 2400, NULL,
 NULL, DATE '2025-01-20', DATE '2025-01-31',
 'APPLY', 6, TIMESTAMP '2025-01-10 10:00:00', '期間限定の割引キャンペーンとして申請します'),

-- ============================================
-- ワークフロー3：書籍削除（REMOVE_BOOK）- REJECTED後CREATED状態
-- 営業二部の山本が作成、申請したが却下されCREATEDに戻った
-- ============================================
-- 操作1：作成（CREATED状態）
(4, 3, 'REMOVE_BOOK', 'CREATED',
 45, NULL, NULL, NULL, NULL, NULL, NULL,
 '売れ行きが悪いため', NULL, NULL,
 'CREATE', 8, TIMESTAMP '2025-01-05 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(5, 3, 'REMOVE_BOOK', 'APPLIED',
 45, NULL, NULL, NULL, NULL, NULL, NULL,
 '売れ行きが悪いため', NULL, NULL,
 'APPLY', 8, TIMESTAMP '2025-01-05 10:00:00', '売上実績が低いため削除を申請します'),

-- 操作3：却下（CREATEDに戻る）
(6, 3, 'REMOVE_BOOK', 'CREATED',
 45, NULL, NULL, NULL, NULL, NULL, NULL,
 '売れ行きが悪いため', NULL, NULL,
 'REJECT', 9, TIMESTAMP '2025-01-06 14:00:00', '在庫が残っているため削除不可'),

-- ============================================
-- ワークフロー4：書籍追加（ADD_NEW_BOOK）- CREATED状態
-- 営業一部の中村が作成、まだ申請していない
-- ============================================
(7, 4, 'ADD_NEW_BOOK', 'CREATED',
 NULL, 'SpringBoot実践ガイド', '鈴木花子', 2, 2, 3800, 'https://example.com/images/spring-guide.jpg',
 NULL, NULL, NULL,
 'CREATE', 2, TIMESTAMP '2025-01-14 10:30:00', NULL),

-- ============================================
-- ワークフロー5：書籍追加（ADD_NEW_BOOK）- APPLIED状態
-- 営業一部の小林が作成・申請、承認待ち
-- ============================================
-- 操作1：作成（CREATED状態）
(8, 5, 'ADD_NEW_BOOK', 'CREATED',
 NULL, 'SQL最適化テクニック', '佐藤健二', 3, 3, 3600, 'https://example.com/images/sql-tech.jpg',
 NULL, NULL, NULL,
 'CREATE', 3, TIMESTAMP '2025-01-13 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(9, 5, 'ADD_NEW_BOOK', 'APPLIED',
 NULL, 'SQL最適化テクニック', '佐藤健二', 3, 3, 3600, 'https://example.com/images/sql-tech.jpg',
 NULL, NULL, NULL,
 'APPLY', 3, TIMESTAMP '2025-01-13 10:00:00', 'SQLパフォーマンス改善のニーズが高まっているため申請します'),

-- ============================================
-- ワークフロー6：書籍追加（ADD_NEW_BOOK）- APPROVED状態
-- 営業一部の渡辺が作成・申請し、坂本マネージャーが承認
-- 既存書籍ID 32: Pythonで学ぶアルゴリズムとデータ構造
-- ============================================
-- 操作1：作成（CREATED状態）
(10, 6, 'ADD_NEW_BOOK', 'CREATED',
 NULL, 'Pythonで学ぶアルゴリズムとデータ構造', 'Charlotte Evans', 6, 1, 3200, '/api/images/covers/32',
 NULL, NULL, NULL,
 'CREATE', 1, TIMESTAMP '2025-01-12 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(11, 6, 'ADD_NEW_BOOK', 'APPLIED',
 NULL, 'Pythonで学ぶアルゴリズムとデータ構造', 'Charlotte Evans', 6, 1, 3200, '/api/images/covers/32',
 NULL, NULL, NULL,
 'APPLY', 1, TIMESTAMP '2025-01-12 10:00:00', 'アルゴリズム学習の基礎教材として有用なため申請します'),

-- 操作3：承認（APPROVED状態）→BOOK_ID 32として追加済み
(12, 6, 'ADD_NEW_BOOK', 'APPROVED',
 32, 'Pythonで学ぶアルゴリズムとデータ構造', 'Charlotte Evans', 6, 1, 3200, '/api/images/covers/32',
 NULL, NULL, NULL,
 'APPROVE', 4, TIMESTAMP '2025-01-12 15:00:00', '需要が高いカテゴリであり、内容も充実しているため承認します'),

-- ============================================
-- ワークフロー7：削除申請（REMOVE_BOOK）- CREATED状態
-- 営業一部の中村が作成、まだ申請していない
-- ============================================
(13, 7, 'REMOVE_BOOK', 'CREATED',
 7, NULL, NULL, NULL, NULL, NULL, NULL,
 '需要が少なく在庫過多', NULL, NULL,
 'CREATE', 2, TIMESTAMP '2025-01-11 11:00:00', NULL),

-- ============================================
-- ワークフロー8：削除申請（REMOVE_BOOK）- APPLIED状態
-- 営業一部の小林が作成・申請、承認待ち
-- ============================================
-- 操作1：作成（CREATED状態）
(14, 8, 'REMOVE_BOOK', 'CREATED',
 16, NULL, NULL, NULL, NULL, NULL, NULL,
 '改訂版が出版されるため', NULL, NULL,
 'CREATE', 3, TIMESTAMP '2025-01-10 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(15, 8, 'REMOVE_BOOK', 'APPLIED',
 16, NULL, NULL, NULL, NULL, NULL, NULL,
 '改訂版が出版されるため', NULL, NULL,
 'APPLY', 3, TIMESTAMP '2025-01-10 10:30:00', '新版への切り替えのため旧版の削除を申請します'),

-- ============================================
-- ワークフロー9：価格改定（ADJUST_BOOK_PRICE）- CREATED状態
-- 営業一部の渡辺が作成、まだ申請していない
-- ============================================
(16, 9, 'ADJUST_BOOK_PRICE', 'CREATED',
 2, NULL, NULL, NULL, NULL, 3800, NULL,
 NULL, DATE '2025-01-25', DATE '2025-02-28',
 'CREATE', 1, TIMESTAMP '2025-01-09 14:00:00', NULL),

-- ============================================
-- ワークフロー10：価格改定（ADJUST_BOOK_PRICE）- APPLIED状態
-- 営業一部の中村が作成・申請、承認待ち
-- ============================================
-- 操作1：作成（CREATED状態）
(17, 10, 'ADJUST_BOOK_PRICE', 'CREATED',
 5, NULL, NULL, NULL, NULL, 2500, NULL,
 NULL, DATE '2025-01-22', DATE '2025-02-15',
 'CREATE', 2, TIMESTAMP '2025-01-08 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(18, 10, 'ADJUST_BOOK_PRICE', 'APPLIED',
 5, NULL, NULL, NULL, NULL, 2500, NULL,
 NULL, DATE '2025-01-22', DATE '2025-02-15',
 'APPLY', 2, TIMESTAMP '2025-01-08 10:00:00', '期間限定セールとして価格改定を申請します'),

-- ============================================
-- ワークフロー11：書籍追加（ADD_NEW_BOOK）- CREATED状態
-- 営業一部の小林が作成、まだ申請していない
-- ============================================
(19, 11, 'ADD_NEW_BOOK', 'CREATED',
 NULL, 'React実践開発', '伊藤太郎', 5, 5, 3900, 'https://example.com/images/react-dev.jpg',
 NULL, NULL, NULL,
 'CREATE', 3, TIMESTAMP '2025-01-07 16:00:00', NULL),

-- ============================================
-- ワークフロー12：書籍追加（ADD_NEW_BOOK）- APPLIED状態
-- 営業一部の渡辺が作成・申請、承認待ち
-- ============================================
-- 操作1：作成（CREATED状態）
(20, 12, 'ADD_NEW_BOOK', 'CREATED',
 NULL, 'クラウドネイティブ設計', '山本真紀', 8, 1, 4500, 'https://example.com/images/cloud-native.jpg',
 NULL, NULL, NULL,
 'CREATE', 1, TIMESTAMP '2025-01-06 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(21, 12, 'ADD_NEW_BOOK', 'APPLIED',
 NULL, 'クラウドネイティブ設計', '山本真紀', 8, 1, 4500, 'https://example.com/images/cloud-native.jpg',
 NULL, NULL, NULL,
 'APPLY', 1, TIMESTAMP '2025-01-06 10:30:00', 'クラウド開発の需要増加に対応するため申請します'),

-- ============================================
-- ワークフロー13：削除申請（REMOVE_BOOK）- CREATED状態
-- 営業一部の渡辺が作成、まだ申請していない
-- ============================================
(22, 13, 'REMOVE_BOOK', 'CREATED',
 22, NULL, NULL, NULL, NULL, NULL, NULL,
 '販売不振のため', NULL, NULL,
 'CREATE', 1, TIMESTAMP '2025-01-05 13:00:00', NULL),

-- ============================================
-- ワークフロー14：削除申請（REMOVE_BOOK）- APPLIED状態
-- 営業一部の中村が作成・申請、承認待ち
-- ============================================
-- 操作1：作成（CREATED状態）
(23, 14, 'REMOVE_BOOK', 'CREATED',
 33, NULL, NULL, NULL, NULL, NULL, NULL,
 '出版社との契約終了', NULL, NULL,
 'CREATE', 2, TIMESTAMP '2025-01-04 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(24, 14, 'REMOVE_BOOK', 'APPLIED',
 33, NULL, NULL, NULL, NULL, NULL, NULL,
 '出版社との契約終了', NULL, NULL,
 'APPLY', 2, TIMESTAMP '2025-01-04 11:00:00', '契約終了に伴い取扱中止のため削除を申請します'),

-- ============================================
-- ワークフロー15：価格改定（ADJUST_BOOK_PRICE）- CREATED状態
-- 営業一部の小林が作成、まだ申請していない
-- ============================================
(25, 15, 'ADJUST_BOOK_PRICE', 'CREATED',
 10, NULL, NULL, NULL, NULL, 3500, NULL,
 NULL, DATE '2025-01-28', DATE '2025-03-10',
 'CREATE', 3, TIMESTAMP '2025-01-03 14:00:00', NULL),

-- ============================================
-- ワークフロー16：価格改定（ADJUST_BOOK_PRICE）- APPLIED状態
-- 営業一部の渡辺が作成・申請、承認待ち
-- ============================================
-- 操作1：作成（CREATED状態）
(26, 16, 'ADJUST_BOOK_PRICE', 'CREATED',
 15, NULL, NULL, NULL, NULL, 2900, NULL,
 NULL, DATE '2025-02-01', DATE '2025-02-28',
 'CREATE', 1, TIMESTAMP '2025-01-02 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(27, 16, 'ADJUST_BOOK_PRICE', 'APPLIED',
 15, NULL, NULL, NULL, NULL, 2900, NULL,
 NULL, DATE '2025-02-01', DATE '2025-02-28',
 'APPLY', 1, TIMESTAMP '2025-01-02 10:00:00', '2月のキャンペーン価格として申請します'),

-- ============================================
-- ワークフロー17：書籍追加（ADD_NEW_BOOK）- APPROVED状態
-- 営業一部の中村が作成・申請し、佐々木ディレクターが承認
-- 既存書籍ID 36: 生成AIシステム設計ガイド
-- ============================================
-- 操作1：作成（CREATED状態）
(28, 17, 'ADD_NEW_BOOK', 'CREATED',
 NULL, '生成AIシステム設計ガイド', 'Fiona Walker', 7, 2, 4200, '/api/images/covers/36',
 NULL, NULL, NULL,
 'CREATE', 2, TIMESTAMP '2024-12-28 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(29, 17, 'ADD_NEW_BOOK', 'APPLIED',
 NULL, '生成AIシステム設計ガイド', 'Fiona Walker', 7, 2, 4200, '/api/images/covers/36',
 NULL, NULL, NULL,
 'APPLY', 2, TIMESTAMP '2024-12-28 10:00:00', '生成AI分野の重要書籍として新規追加を申請します'),

-- 操作3：承認（APPROVED状態）→BOOK_ID 36として追加済み
(30, 17, 'ADD_NEW_BOOK', 'APPROVED',
 36, '生成AIシステム設計ガイド', 'Fiona Walker', 7, 2, 4200, '/api/images/covers/36',
 NULL, NULL, NULL,
 'APPROVE', 5, TIMESTAMP '2024-12-28 16:00:00', '生成AIは今後の重要分野であり、市場ニーズも高いため承認します。価格設定も適切です'),

-- ============================================
-- ワークフロー18：削除申請（REMOVE_BOOK）- APPLIED状態
-- 営業一部の小林が作成・申請、承認待ち
-- ============================================
-- 操作1：作成（CREATED状態）
(31, 18, 'REMOVE_BOOK', 'CREATED',
 40, NULL, NULL, NULL, NULL, NULL, NULL,
 '類似書籍との統合のため', NULL, NULL,
 'CREATE', 3, TIMESTAMP '2024-12-27 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(32, 18, 'REMOVE_BOOK', 'APPLIED',
 40, NULL, NULL, NULL, NULL, NULL, NULL,
 '類似書籍との統合のため', NULL, NULL,
 'APPLY', 3, TIMESTAMP '2024-12-27 11:00:00', 'より新しい類似書籍があるため削除を申請します'),

-- ============================================
-- ワークフロー19：価格改定（ADJUST_BOOK_PRICE）- CREATED状態
-- 営業一部の中村が作成、まだ申請していない
-- ============================================
(33, 19, 'ADJUST_BOOK_PRICE', 'CREATED',
 25, NULL, NULL, NULL, NULL, 3200, NULL,
 NULL, DATE '2025-02-05', DATE '2025-03-15',
 'CREATE', 2, TIMESTAMP '2024-12-26 14:00:00', NULL),

-- ============================================
-- ワークフロー20：価格改定（ADJUST_BOOK_PRICE）- APPLIED状態
-- 営業一部の小林が作成・申請、承認待ち
-- 既存書籍ID 31: Pythonデータ分析パターン
-- ============================================
-- 操作1：作成（CREATED状態）
(34, 20, 'ADJUST_BOOK_PRICE', 'CREATED',
 31, NULL, NULL, NULL, NULL, 3400, NULL,
 NULL, DATE '2025-02-10', DATE '2025-03-31',
 'CREATE', 3, TIMESTAMP '2024-12-25 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(35, 20, 'ADJUST_BOOK_PRICE', 'APPLIED',
 31, NULL, NULL, NULL, NULL, 3400, NULL,
 NULL, DATE '2025-02-10', DATE '2025-03-31',
 'APPLY', 3, TIMESTAMP '2024-12-25 10:00:00', '春季キャンペーンとして価格改定を申請します'),

-- ============================================
-- ワークフロー21：書籍追加（ADD_NEW_BOOK）- CREATED状態
-- 営業一部の渡辺が作成、まだ申請していない
-- ============================================
(36, 21, 'ADD_NEW_BOOK', 'CREATED',
 NULL, 'Dockerコンテナ実践', '林和也', 8, 3, 3700, 'https://example.com/images/docker.jpg',
 NULL, NULL, NULL,
 'CREATE', 1, TIMESTAMP '2024-12-24 15:00:00', NULL),

-- ============================================
-- ワークフロー22：書籍追加（ADD_NEW_BOOK）- APPLIED状態
-- 営業一部の中村が作成・申請、承認待ち
-- ============================================
-- 操作1：作成（CREATED状態）
(37, 22, 'ADD_NEW_BOOK', 'CREATED',
 NULL, 'AWS Lambda入門', '松本香織', 9, 5, 4100, 'https://example.com/images/lambda.jpg',
 NULL, NULL, NULL,
 'CREATE', 2, TIMESTAMP '2024-12-23 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(38, 22, 'ADD_NEW_BOOK', 'APPLIED',
 NULL, 'AWS Lambda入門', '松本香織', 9, 5, 4100, 'https://example.com/images/lambda.jpg',
 NULL, NULL, NULL,
 'APPLY', 2, TIMESTAMP '2024-12-23 10:30:00', 'サーバーレス開発の需要が高いため申請します'),

-- ============================================
-- ワークフロー23：削除申請（REMOVE_BOOK）- CREATED状態
-- 営業一部の渡辺が作成、まだ申請していない
-- ============================================
(39, 23, 'REMOVE_BOOK', 'CREATED',
 49, NULL, NULL, NULL, NULL, NULL, NULL,
 '在庫切れで再入荷予定なし', NULL, NULL,
 'CREATE', 1, TIMESTAMP '2024-12-22 13:00:00', NULL),

-- ============================================
-- ワークフロー24：書籍追加（ADD_NEW_BOOK）- APPROVED状態
-- 営業一部の小林が作成・申請し、坂本マネージャーが承認
-- 既存書籍ID 25: JavaScriptアルゴリズム実践集
-- ============================================
-- 操作1：作成（CREATED状態）
(40, 24, 'ADD_NEW_BOOK', 'CREATED',
 NULL, 'JavaScriptアルゴリズム実践集', 'Aaron Scott', 5, 1, 3500, '/api/images/covers/25',
 NULL, NULL, NULL,
 'CREATE', 3, TIMESTAMP '2024-12-20 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(41, 24, 'ADD_NEW_BOOK', 'APPLIED',
 NULL, 'JavaScriptアルゴリズム実践集', 'Aaron Scott', 5, 1, 3500, '/api/images/covers/25',
 NULL, NULL, NULL,
 'APPLY', 3, TIMESTAMP '2024-12-20 10:00:00', 'プログラミング基礎力向上のため申請します'),

-- 操作3：承認（APPROVED状態）→BOOK_ID 25として追加済み
(42, 24, 'ADD_NEW_BOOK', 'APPROVED',
 25, 'JavaScriptアルゴリズム実践集', 'Aaron Scott', 5, 1, 3500, '/api/images/covers/25',
 NULL, NULL, NULL,
 'APPROVE', 4, TIMESTAMP '2024-12-20 16:00:00', 'JavaScriptの基礎力向上に役立つ内容であり、承認します'),

-- ============================================
-- 営業二部のワークフロー（8件）
-- ============================================
-- ============================================
-- ワークフロー25：書籍追加（ADD_NEW_BOOK）- CREATED状態
-- 営業二部の加藤が作成、まだ申請していない
-- ============================================
(43, 25, 'ADD_NEW_BOOK', 'CREATED',
 NULL, 'Vue.js 3実践入門', '大野智子', 5, 2, 3500, 'https://example.com/images/vue3.jpg',
 NULL, NULL, NULL,
 'CREATE', 6, TIMESTAMP '2025-01-16 10:00:00', NULL),

-- ============================================
-- ワークフロー26：価格改定（ADJUST_BOOK_PRICE）- APPLIED状態
-- 営業二部の吉田が作成・申請、承認待ち
-- ============================================
-- 操作1：作成（CREATED状態）
(44, 26, 'ADJUST_BOOK_PRICE', 'CREATED',
 20, NULL, NULL, NULL, NULL, 2200, NULL,
 NULL, DATE '2025-02-01', DATE '2025-02-28',
 'CREATE', 7, TIMESTAMP '2025-01-14 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(45, 26, 'ADJUST_BOOK_PRICE', 'APPLIED',
 20, NULL, NULL, NULL, NULL, 2200, NULL,
 NULL, DATE '2025-02-01', DATE '2025-02-28',
 'APPLY', 7, TIMESTAMP '2025-01-14 10:00:00', '2月限定の特別価格として申請します'),

-- ============================================
-- ワークフロー27：削除申請（REMOVE_BOOK）- CREATED状態
-- 営業二部の山本が作成、まだ申請していない
-- ============================================
(46, 27, 'REMOVE_BOOK', 'CREATED',
 28, NULL, NULL, NULL, NULL, NULL, NULL,
 '類似書籍が多数あるため', NULL, NULL,
 'CREATE', 8, TIMESTAMP '2025-01-13 14:00:00', NULL),

-- ============================================
-- ワークフロー28：書籍追加（ADD_NEW_BOOK）- APPLIED状態
-- 営業二部の加藤が作成・申請、承認待ち
-- ============================================
-- 操作1：作成（CREATED状態）
(47, 28, 'ADD_NEW_BOOK', 'CREATED',
 NULL, 'Kubernetes運用ガイド', '中島健', 8, 3, 4300, 'https://example.com/images/k8s-ops.jpg',
 NULL, NULL, NULL,
 'CREATE', 6, TIMESTAMP '2025-01-12 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(48, 28, 'ADD_NEW_BOOK', 'APPLIED',
 NULL, 'Kubernetes運用ガイド', '中島健', 8, 3, 4300, 'https://example.com/images/k8s-ops.jpg',
 NULL, NULL, NULL,
 'APPLY', 6, TIMESTAMP '2025-01-12 10:30:00', 'コンテナ運用のニーズが高まっているため申請します'),

-- ============================================
-- ワークフロー29：書籍追加（ADD_NEW_BOOK）- APPROVED状態
-- 営業二部の吉田が作成・申請し、井上マネージャーが承認
-- 既存書籍ID 14: データベースの科学
-- ============================================
-- 操作1：作成（CREATED状態）
(49, 29, 'ADD_NEW_BOOK', 'CREATED',
 NULL, 'データベースの科学', 'Mark Jackson', 3, 4, 2500, '/api/images/covers/14',
 NULL, NULL, NULL,
 'CREATE', 7, TIMESTAMP '2025-01-11 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(50, 29, 'ADD_NEW_BOOK', 'APPLIED',
 NULL, 'データベースの科学', 'Mark Jackson', 3, 4, 2500, '/api/images/covers/14',
 NULL, NULL, NULL,
 'APPLY', 7, TIMESTAMP '2025-01-11 10:00:00', 'データベース基礎教材として有用なため申請します'),

-- 操作3：承認（APPROVED状態）→BOOK_ID 14として追加済み
(51, 29, 'ADD_NEW_BOOK', 'APPROVED',
 14, 'データベースの科学', 'Mark Jackson', 3, 4, 2500, '/api/images/covers/14',
 NULL, NULL, NULL,
 'APPROVE', 9, TIMESTAMP '2025-01-11 15:00:00', 'データベース基礎書として有用であり、価格も手頃なため承認します'),

-- ============================================
-- ワークフロー30：価格改定（ADJUST_BOOK_PRICE）- CREATED状態
-- 営業二部の山本が作成、まだ申請していない
-- ============================================
(52, 30, 'ADJUST_BOOK_PRICE', 'CREATED',
 35, NULL, NULL, NULL, NULL, 3400, NULL,
 NULL, DATE '2025-02-15', DATE '2025-03-20',
 'CREATE', 8, TIMESTAMP '2025-01-10 11:00:00', NULL),

-- ============================================
-- ワークフロー31：削除申請（REMOVE_BOOK）- APPLIED状態
-- 営業二部の加藤が作成・申請、承認待ち
-- ============================================
-- 操作1：作成（CREATED状態）
(53, 31, 'REMOVE_BOOK', 'CREATED',
 44, NULL, NULL, NULL, NULL, NULL, NULL,
 '改訂版リリース予定のため', NULL, NULL,
 'CREATE', 6, TIMESTAMP '2025-01-09 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(54, 31, 'REMOVE_BOOK', 'APPLIED',
 44, NULL, NULL, NULL, NULL, NULL, NULL,
 '改訂版リリース予定のため', NULL, NULL,
 'APPLY', 6, TIMESTAMP '2025-01-09 10:00:00', '新版への移行のため旧版の削除を申請します'),

-- ============================================
-- ワークフロー32：書籍追加（ADD_NEW_BOOK）- APPROVED状態
-- 営業二部の山本が作成・申請し、木村ディレクターが承認
-- 既存書籍ID 30: Pythonプログラミング実践入門
-- ============================================
-- 操作1：作成（CREATED状態）
(55, 32, 'ADD_NEW_BOOK', 'CREATED',
 NULL, 'Pythonプログラミング実践入門', 'Alice Carter', 6, 2, 3000, '/api/images/covers/30',
 NULL, NULL, NULL,
 'CREATE', 8, TIMESTAMP '2025-01-08 09:00:00', NULL),

-- 操作2：申請（APPLIED状態）
(56, 32, 'ADD_NEW_BOOK', 'APPLIED',
 NULL, 'Pythonプログラミング実践入門', 'Alice Carter', 6, 2, 3000, '/api/images/covers/30',
 NULL, NULL, NULL,
 'APPLY', 8, TIMESTAMP '2025-01-08 10:00:00', 'Python初学者向けの良書として申請します'),

-- 操作3：承認（APPROVED状態）→BOOK_ID 30として追加済み
(57, 32, 'ADD_NEW_BOOK', 'APPROVED',
 30, 'Pythonプログラミング実践入門', 'Alice Carter', 6, 2, 3000, '/api/images/covers/30',
 NULL, NULL, NULL,
 'APPROVE', 10, TIMESTAMP '2025-01-08 16:00:00', 'Python入門書として適切な内容であり、全部署で活用できるため承認します');

-- シーケンスをリセット（HSQLDB用）
ALTER TABLE PUBLISHER ALTER COLUMN PUBLISHER_ID RESTART WITH 6;
ALTER TABLE CATEGORY ALTER COLUMN CATEGORY_ID RESTART WITH 10;
ALTER TABLE BOOK ALTER COLUMN BOOK_ID RESTART WITH 51;
ALTER TABLE DEPARTMENT ALTER COLUMN DEPARTMENT_ID RESTART WITH 4;
ALTER TABLE EMPLOYEE ALTER COLUMN EMPLOYEE_ID RESTART WITH 14;
ALTER TABLE WORKFLOW ALTER COLUMN OPERATION_ID RESTART WITH 58;
