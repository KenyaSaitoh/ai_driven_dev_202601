# Berry Books API テストスクリプト

curlコマンドを使用してBerry Books APIの全エンドポイントをテストするためのbashスクリプト集です。

## 📁 ファイル構成

```
test_script/
├── README.md           # このファイル
├── test_auth.sh        # 認証API テスト
├── test_books.sh       # 書籍API テスト
├── test_orders.sh      # 注文API テスト
├── test_images.sh      # 画像API テスト
└── test_all.sh         # 全API統合テスト
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
./gradlew :berry-books-api:setupHsqldb

# 4. Berry Books APIをデプロイ
./gradlew :berry-books-api:war
./gradlew :berry-books-api:deploy

# 5. Customer API（顧客管理）をデプロイ（認証で使用）
./gradlew :customer-api:war
./gradlew :customer-api:deploy
```

### 🌟 推奨：簡易テスト（Windows Git Bash対応）

**最も簡単な方法** - 全APIを一度にテスト：

```bash
cd projects/master/bookstore/berry-books-api/test_script
./simple_test.sh
```

このスクリプトは以下をテストします：
- ログイン
- 書籍一覧・詳細取得
- カテゴリフィルタ
- ユーザー情報取得
- 注文作成・履歴取得
- 画像API
- ログアウト

### 全APIテストの実行（詳細版）

```bash
cd projects/master/bookstore/berry-books-api/test_script
./test_all.sh
```

このスクリプトは各APIテストを順番に実行し、テスト間で確認プロンプトを表示します。

**注意:** Linux/macOS環境を推奨。Windows Git Bashではgrep -Pが利用できないため、代わりに`simple_test.sh`を使用してください。

### 個別APIテストの実行

各APIを個別にテストする場合：

```bash
# 認証API テスト
./test_auth.sh

# 書籍API テスト
./test_books.sh

# 注文API テスト
./test_orders.sh

# 画像API テスト
./test_images.sh
```

**注意:** これらのスクリプトはLinux/macOS環境向けです。Windows Git Bashでは`simple_test.sh`を使用してください。

## 📋 各テストスクリプトの内容

### 1. test_auth.sh - 認証API

- ✅ ユーザー登録 (`POST /api/auth/register`)
- ✅ ログイン (`POST /api/auth/login`)
- ✅ 現在のユーザー情報取得 (`GET /api/auth/me`)
- ✅ ログアウト (`POST /api/auth/logout`)
- ✅ ログアウト後のアクセステスト（認証エラー確認）

**テストユーザー:**
- Email: `alice@gmail.com`
- Password: `password`

### 2. test_books.sh - 書籍API

- ✅ 書籍一覧取得 (`GET /api/books`)
- ✅ 書籍一覧取得（カテゴリフィルタ付き）(`GET /api/books?categoryId=1`)
- ✅ 書籍詳細取得 (`GET /api/books/{id}`)
- ✅ 存在しない書籍IDでのエラーテスト（404確認）
- ✅ 複数の書籍詳細を連続取得

### 3. test_orders.sh - 注文API

- ✅ 注文作成 (`POST /api/orders`)
- ✅ 注文履歴取得 (`GET /api/orders/history`)
- ✅ 注文詳細取得 (`GET /api/orders/{id}`)
- ✅ 存在しない注文IDでのエラーテスト（404確認）
- ✅ 在庫不足エラーのテスト

### 4. test_images.sh - 画像API

- ✅ 書籍表紙画像取得 (`GET /api/images/covers/{bookId}`)
- ✅ 複数の書籍画像を連続取得
- ✅ 存在しない書籍IDでのエラーテスト（404確認）
- ✅ 画像ダウンロードテスト

**注意:** 画像APIは認証不要です。

## 🔧 テスト用データ

### テストユーザー（既存）

| Email | Password | Customer ID |
|-------|----------|-------------|
| alice@gmail.com | password | 1 |
| bob@gmail.com | password | 2 |
| carol@gmail.com | password | 3 |

### 書籍データ

初期データとして約50冊の書籍が登録されています。

### カテゴリデータ

| Category ID | Category Name |
|-------------|---------------|
| 1 | Java |
| 2 | Spring Framework |
| 3 | データベース |
| ... | ... |

## 📝 スクリプトの特徴

### 自動ログイン

`test_books.sh`、`test_orders.sh`は実行時に自動的にログインします。

### HTTPステータスコードの確認

すべてのリクエストでHTTPステータスコードを確認し、成功/失敗を表示します。

### Cookieの管理

JWT認証トークンは一時的なCookieファイルに保存され、テスト終了時に自動削除されます。

### レスポンスの表示

大きなレスポンスは適切に切り詰めて表示されます。

### エラーテスト

404エラー、在庫不足エラーなど、エラーケースも網羅的にテストします。

## 🎯 使用例

### 書籍APIだけをテストする場合

```bash
cd projects/master/bookstore/berry-books-api/test_script
./test_books.sh
```

**出力例:**
```
=========================================
  書籍API テスト
=========================================

🔐 ログイン中...
✅ ログイン成功

1️⃣  書籍一覧取得（全件）
-------------------------------------------
✅ 書籍一覧取得成功 (HTTP 200)

レスポンス（最初の3冊）:
[{"bookId":1,"bookName":"Java SEディープダイブ",...}]
...

📚 取得した書籍数: 50 冊
```

### 注文を作成してテストする場合

```bash
cd projects/master/bookstore/berry-books-api/test_script
./test_orders.sh
```

このスクリプトは：
1. ログイン
2. 新しい注文を作成
3. 注文履歴を取得
4. 作成した注文の詳細を取得

という流れでテストします。

## 💡 カスタマイズ

### テストユーザーの変更

各スクリプトの以下の行を編集してください：

```bash
LOGIN_DATA='{"email":"alice@gmail.com","password":"password"}'
```

### API Base URLの変更

各スクリプトではデフォルトで以下の設定を使用します：

```bash
API_BASE="http://localhost:8080/berry-books-api"
```

異なるホスト/ポート/コンテキストパスを使用する場合は、各スクリプトのこの行を編集してください。

**例：**
- 別のポート: `API_BASE="http://localhost:9090/berry-books-api"`
- ルートコンテキスト: `API_BASE="http://localhost:8080"`
- リモートサーバー: `API_BASE="https://example.com/berry-books-api"`

### 注文内容のカスタマイズ

`test_orders.sh`の`ORDER_DATA`変数を編集してください：

```bash
ORDER_DATA='{
  "cartItems": [
    {
      "bookId": 1,
      "quantity": 2
    }
  ]
}'
```

## 🐛 トラブルシューティング

### ログインに失敗する

**原因:**
- Payara Serverが起動していない
- Berry Books APIまたはCustomer APIがデプロイされていない
- データベースにテストユーザーが登録されていない

**解決策:**
```bash
# Payara Serverが起動しているか確認
./gradlew statusPayara

# Customer APIが正常にデプロイされているか確認
curl http://localhost:8080/customer-api/api/customers/1

# データベースを初期化（テストユーザーを含む）
./gradlew :berry-books-api:setupHsqldb

# APIを再デプロイ
./gradlew :berry-books-api:deploy
./gradlew :customer-api:deploy
```

### 書籍一覧が取得できない

**原因:**
- Berry Books APIがデプロイされていない
- 認証に失敗している

**解決策:**
```bash
# Payara Serverが起動しているか確認
./gradlew statusPayara

# Berry Books APIが正常にデプロイされているか確認
curl http://localhost:8080/berry-books-api/api/books

# APIを再デプロイ
./gradlew :berry-books-api:war
./gradlew :berry-books-api:deploy
```

### 画像が取得できない（404エラー）

**原因:**
- 画像ファイルが配置されていない

**解決策:**

書籍表紙画像を以下の場所に配置してください：

```
projects/master/bookstore/berry-books-api/src/main/webapp/resources/images/covers/
```

ファイル名の規則：
```
書籍のタイトルをそのままファイル名にする（スペースはアンダースコアに変換）

例:
- Java_SEディープダイブ.jpg
- Jakarta_EEによるアーキテクチャ設計.jpg
- SpringBootでのAPI実践.jpg
```

推奨仕様：
- 形式: JPG
- サイズ: 幅400px以上
- アスペクト比: 2:3 または 3:4
- ファイルサイズ: 500KB以下

> **Note:** 画像ファイル名は書籍データベースの`BOOK_NAME`カラムの値と一致させる必要があります。

## 📖 関連ドキュメント

- [Berry Books API README](../README.md)
- [Customer API README](../../customer-api/README.md)

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。

