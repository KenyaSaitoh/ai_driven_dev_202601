# Back Office API テストスクリプト

curlコマンドを使用してBack Office APIの全エンドポイントをテストするためのbashスクリプト集です。

## 📁 ファイル構成

```
test_script/
├── README.md           # このファイル
├── _common.sh          # 共通関数ライブラリ
├── simple_test.sh      # 簡易テスト（Windows Git Bash対応）
├── test_all.sh         # 全API統合テスト
├── test_authen.sh      # 認証API テスト
├── test_books.sh       # 書籍API テスト
├── test_publishers.sh  # 出版社API テスト
├── test_stocks.sh      # 在庫API テスト
└── test_workflow.sh    # ワークフローAPI テスト
```

## 🚀 使用方法

### 前提条件

以下のサービスが起動している必要があります：

```bash
# リポジトリルートで実行

# 1. HSQLDBサーバー
./gradlew startHsqldb

# 2. Payara Server
./gradlew startPayara

# 3. データベースのセットアップ（初回のみ）
./gradlew :back-office-api-sdd:setupHsqldb

# 4. Back Office APIをデプロイ
./gradlew :back-office-api-sdd:war
./gradlew :back-office-api-sdd:deploy
```

### 🌟 推奨：簡易テスト（Windows Git Bash対応）

**最も簡単な方法** - 全APIを一度にテスト：

```bash
cd projects/sdd/bookstore/back-office-api-sdd/test_script
./simple_test.sh
```

このスクリプトは以下をテストします：
- 書籍一覧・詳細取得
- カテゴリ一覧取得
- 在庫一覧・詳細取得
- ワークフロー作成・一覧取得
- HTTPステータスコード確認

### 全APIテストの実行（詳細版）

```bash
cd projects/sdd/bookstore/back-office-api-sdd/test_script
./test_all.sh
```

このスクリプトは各APIテストを順番に実行し、テスト間で確認プロンプトを表示します。

**注意:** Linux/macOS環境を推奨。Windows Git Bashでは`simple_test.sh`を使用してください。

### 個別APIテストの実行

各APIを個別にテストする場合：

```bash
# 認証API テスト
./test_authen.sh

# 書籍API テスト
./test_books.sh

# 出版社API テスト
./test_publishers.sh

# 在庫API テスト
./test_stocks.sh

# ワークフローAPI テスト
./test_workflow.sh
```

**注意:** これらのスクリプトはLinux/macOS環境向けです。Windows Git Bashでは`simple_test.sh`を使用してください。

## 📋 各テストスクリプトの内容

### 1. test_authen.sh - 認証API

- ✅ ログイン（成功） (`POST /api/auth/login`)
- ✅ ログイン（失敗 - 存在しない社員コード）
- ✅ ログイン（失敗 - 間違ったパスワード）
- ✅ 現在のログインユーザー情報取得 (`GET /api/auth/me`) - 未実装
- ✅ ログアウト (`POST /api/auth/logout`)
- ✅ 複数社員でのログインテスト

### 2. test_books.sh - 書籍API

- ✅ 書籍一覧取得 (`GET /api/books`)
- ✅ 書籍詳細取得 (`GET /api/books/{id}`)
- ✅ 存在しない書籍IDでのエラーテスト（404確認）
- ✅ 複数の書籍詳細を連続取得
- ✅ カテゴリ一覧取得 (`GET /api/categories`)
- ✅ 書籍検索（デフォルト - JPQL） (`GET /api/books/search`)
  - キーワードのみで検索
  - カテゴリ + キーワードで検索
  - カテゴリのみで検索
- ✅ 書籍検索（JPQL - 明示的） (`GET /api/books/search/jpql`)
- ✅ 書籍検索（Criteria API - 動的クエリ） (`GET /api/books/search/criteria`)
- ✅ 書籍のカテゴリ一覧取得 (`GET /api/books/categories`)

### 3. test_publishers.sh - 出版社API

- ✅ 出版社一覧取得 (`GET /api/publishers`)
- ✅ 出版社データの妥当性チェック

### 4. test_stocks.sh - 在庫API

- ✅ 在庫一覧取得 (`GET /api/stocks`)
- ✅ 在庫詳細取得 (`GET /api/stocks/{bookId}`)
- ✅ 在庫更新 (`PUT /api/stocks/{bookId}`)
- ✅ 楽観ロックテスト（バージョン競合エラー確認）
- ✅ 存在しない書籍IDでのエラーテスト（404確認）

### 5. test_workflow.sh - ワークフローAPI

- ✅ ワークフロー作成 (`POST /api/workflows`)
  - 新規書籍の追加（ADD_NEW_BOOK）
  - 書籍価格の改定（ADJUST_BOOK_PRICE）
  - 既存書籍の削除（REMOVE_BOOK）
- ✅ ワークフロー一覧取得 (`GET /api/workflows?state={state}`)
  - NEW状態
  - APPLIED状態
  - APPROVED状態
  - 全件取得
- ✅ ワークフロー更新 (`PUT /api/workflows/{workflowId}`)
- ✅ ワークフロー履歴取得 (`GET /api/workflows/{workflowId}/history`)
- ✅ ワークフロー申請 (`POST /api/workflows/{workflowId}/apply`)
- ✅ ワークフロー承認 (`POST /api/workflows/{workflowId}/approve`)
- ✅ ワークフロー却下 (`POST /api/workflows/{workflowId}/reject`)

**注意:** ワークフローAPIは認証不要でテスト可能ですが、本番環境では認証が必要になります。

**ワークフロータイプの詳細:**
- `ADD_NEW_BOOK`: 新規書籍の追加（書籍名、著者、カテゴリ、出版社、価格を指定）
- `ADJUST_BOOK_PRICE`: 書籍価格の改定（書籍ID、新価格、開始日、終了日を指定）
- `REMOVE_BOOK`: 既存書籍の削除（書籍IDを指定）

## 🔧 テスト用データ

### 社員データ（ワークフロー用）

ワークフローの作成・申請・承認には社員IDが必要です：

| Employee ID | Employee Code | Employee Name | Department | Job Rank |
|-------------|---------------|---------------|------------|----------|
| 1 | E00001 | 山田太郎 | 企画部 | 3 (DIRECTOR) |
| 2 | E00002 | 佐藤花子 | 企画部 | 2 (MANAGER) |
| 6 | E00006 | 木村健太 | 営業一部 | 1 (ASSOCIATE) |
| 9 | E00009 | 中村さくら | 営業二部 | 1 (ASSOCIATE) |
| 11 | E00011 | 山口修 | 営業三部 | 2 (MANAGER) |

### 書籍データ

初期データとして50冊の書籍が登録されています。

### カテゴリデータ

| Category ID | Category Name |
|-------------|---------------|
| 1 | Java |
| 2 | Spring Framework |
| 3 | データベース |
| ... | ... |

## 📝 スクリプトの特徴

### HTTPステータスコードの確認

すべてのリクエストでHTTPステータスコードを確認し、成功/失敗を表示します。

### レスポンスの表示

大きなレスポンスは適切に切り詰めて表示されます。

### エラーテスト

404エラー、楽観ロックエラーなど、エラーケースも網羅的にテストします。

### 共通関数ライブラリ

`_common.sh`に共通関数を定義し、各テストスクリプトで再利用しています。

## 🎯 使用例

### 認証APIをテストする場合

```bash
cd projects/sdd/bookstore/back-office-api-sdd/test_script
./test_authen.sh
```

**出力例:**
```
===========================================
  認証API テスト
===========================================

1️⃣  ログイン（成功）
-------------------------------------------
✅ ログイン成功 (HTTP 200)

レスポンス:
{"employeeId":1,"employeeCode":"E00001","employeeName":"山田太郎",...}

📝 ログインした社員ID: 1
✅ 認証Cookieが保存されました
```

### 書籍APIだけをテストする場合

```bash
cd projects/sdd/bookstore/back-office-api-sdd/test_script
./test_books.sh
```

**出力例:**
```
===========================================
  書籍API テスト
===========================================

1️⃣  書籍一覧取得（全件）
-------------------------------------------
✅ 書籍一覧取得成功 (HTTP 200)

レスポンス（最初の800文字）:
[{"bookId":1,"bookName":"Java SEディープダイブ",...}]
...

📚 取得した書籍数: 50 冊
```

### ワークフローを作成してテストする場合

```bash
cd projects/sdd/bookstore/back-office-api-sdd/test_script
./test_workflow.sh
```

このスクリプトは：
1. 書籍追加ワークフローを作成
2. 価格調整ワークフローを作成
3. 書籍削除ワークフローを作成
4. ワークフロー一覧を取得（NEW状態）
5. ワークフローを更新（一時保存）
6. ワークフローを申請
7. ワークフロー履歴を取得
8. ワークフローを承認
9. ワークフローを却下
10. 承認済みワークフロー一覧を取得
11. ワークフロー全件取得

という流れでテストします。

## 💡 カスタマイズ

### API Base URLの変更

各スクリプトではデフォルトで以下の設定を使用します：

```bash
API_BASE="http://localhost:8080/back-office-api-sdd"
```

異なるホスト/ポート/コンテキストパスを使用する場合は、各スクリプトのこの行を編集してください。

**例：**
- 別のポート: `API_BASE="http://localhost:9090/back-office-api"`
- ルートコンテキスト: `API_BASE="http://localhost:8080"`
- リモートサーバー: `API_BASE="https://example.com/back-office-api"`

### ワークフロー作成内容のカスタマイズ

`test_workflow.sh`の各ワークフロー作成データを編集してください：

```bash
WORKFLOW_CREATE_DATA='{
  "workflowType": "CREATE",
  "bookName": "新Javaプログラミングガイド",
  "author": "山田太郎",
  "categoryId": 1,
  "publisherId": 1,
  "price": 3500,
  "createdBy": 6
}'
```

### 在庫更新内容のカスタマイズ

`test_stocks.sh`の在庫更新データを編集してください：

```bash
UPDATE_DATA="{
  \"quantityInStock\": $NEW_QUANTITY
}"
```

## 🐛 トラブルシューティング

### 書籍一覧が取得できない

**原因:**
- Payara Serverが起動していない
- Back Office APIがデプロイされていない
- データベースが初期化されていない

**解決策:**
```bash
# Payara Serverが起動しているか確認
./gradlew statusPayara

# Back Office APIが正常にデプロイされているか確認
curl http://localhost:8080/back-office-api/api/books

# データベースを初期化
./gradlew :back-office-api-sdd:setupHsqldb

# APIを再デプロイ
./gradlew :back-office-api-sdd:war
./gradlew :back-office-api-sdd:deploy
```

### 在庫更新で楽観ロックエラーが発生する

**原因:**
- 他のユーザーまたはプロセスが同時に在庫を更新した
- バージョン番号が古い

**解決策:**

これは正常な動作です。楽観ロックは同時更新を防ぐための仕組みです。

正しい手順：
1. 在庫詳細を取得して現在のバージョン番号を確認
2. そのバージョン番号を含めて更新リクエストを送信
3. 409エラーが返された場合は再度1から実行

### ワークフロー承認で403エラーが発生する

**原因:**
- 承認権限のない社員IDで承認しようとしている
- 承認者の所属部署や役職が不適切

**解決策:**

承認ルール：
- 申請と同じ部署の社員は承認不可
- 役職が2（MANAGER）以上の社員のみ承認可能

適切な承認者を選択してください：
```bash
APPROVE_DATA='{
  "operatedBy": 1  # DIRECTOR (企画部)
}'
```

### ワークフローが作成できない

**原因:**
- 必須パラメータが不足している
- 存在しないカテゴリIDや出版社IDを指定している
- 書籍削除（DELETE）で存在しない書籍IDを指定している

**解決策:**
```bash
# カテゴリ一覧を確認
curl http://localhost:8080/back-office-api-sdd/api/categories

# 書籍一覧を確認
curl http://localhost:8080/back-office-api-sdd/api/books

# 正しいIDを使用してワークフローを作成
```

## 🔄 ワークフロー状態遷移

ワークフローの状態遷移は以下の通りです：

```
NEW (作成)
  ↓ APPLY (申請)
APPLIED
  ↓ APPROVE (承認) または REJECT (却下)
APPROVED  ←→  NEW (却下時)
```

- **NEW**: 作成直後の状態。APPLY操作のみ可能。
- **APPLIED**: 申請済みの状態。APPROVE または REJECT 操作が可能。
- **APPROVED**: 承認済みの状態。書籍マスターに反映される。
- **REJECT**: 却下すると状態がNEWに戻る。

## 🆕 新機能（2025年1月版）

### 追加されたテストスクリプト

1. **test_authen.sh** - 認証API（`/api/auth`）の包括的なテスト
   - ログイン成功/失敗のケース
   - ログアウト
   - Cookie認証の確認

2. **test_publishers.sh** - 出版社API（`/api/publishers`）のテスト
   - 出版社一覧取得
   - データ妥当性チェック

### 拡張されたテストスクリプト

1. **test_books.sh** - 書籍検索機能を追加
   - `/api/books/search` - デフォルト検索（JPQL）
   - `/api/books/search/jpql` - JPQL明示的検索
   - `/api/books/search/criteria` - Criteria API動的検索
   - キーワード、カテゴリ、組み合わせ検索のテスト

2. **test_workflow.sh** - ワークフロー更新機能を追加
   - `PUT /api/workflows/{workflowId}` - 一時保存機能のテスト
   - ワークフロー履歴取得の正しいエンドポイント（`/history`）を使用

## 📖 関連ドキュメント

- [Back Office API README](../README.md)
- [Berry Books API README](../../berry-books-api/README.md)
- [Customer Hub API README](../../customer-hub-api/README.md)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。

