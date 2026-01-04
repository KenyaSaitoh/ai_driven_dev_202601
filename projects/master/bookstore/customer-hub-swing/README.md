# customer-hub-swing プロジェクト

## 📖 概要

Berry Books オンライン書店の顧客管理デスクトップアプリケーション（Java Swing版）です。
顧客一覧を表示し、顧客情報の編集、注文件数と購入冊数の統計情報を確認できます。

**💡 開発環境について:**
- **Windows環境では Git Bash を使用してください**（Mac/Linuxと同じコマンドが使えます）

## 🚀 セットアップとコマンド実行ガイド

### 前提条件

- **Java Runtime Environment (JRE) 8以降** または **JDK 8以降**
- **Gradle** (プロジェクトルートのGradle Wrapperを使用)
- **Git Bash** (Windows環境でBashコマンドを使用する場合)
- **customer-hub-api（バックエンド）が起動していること**

> **Note:** 
> - Java 8, 11, 17, 21など、任意のバージョンで動作します
> - ① と ② の手順は、ルートの`README.md`または [bookstoreのREADME.md](../README.md) を参照してください
> - **Windows環境では Git Bash を使用してください**（Mac/Linuxと同じコマンドが使えます）

### ③ 依存関係の確認

このプロジェクトを開始する前に、以下が起動していることを確認してください：

**プロジェクトルートで実行:**

- **① HSQLDBサーバー** （`./gradlew startHsqldb`）
- **② Payara Server** （`./gradlew startPayara`）
- **③ データソース** （`./gradlew setupDataSource`）
- **④ データベース初期化済み** （`./gradlew :customer-hub-api:setupHsqldb`）
- **⑤ customer-hub-api がデプロイ済み** （`./gradlew :customer-hub-api:deploy`）

### ④ プロジェクトを開始するときに1回だけ実行

#### A. 自動実行スクリプトを使用する方法（最も簡単）

**Git Bash / Mac / Linux の場合:**

```bash
cd projects/master/bookstore/customer-hub-swing
chmod +x run-app.sh
./run-app.sh
```

> **Note**: Windows Git Bashでも上記のスクリプトがそのまま動作します

#### B. 手動でビルド・実行する方法

```bash
# プロジェクトルートで実行（Git Bash / Mac / Linux）

# 1. アプリケーションをビルド
./gradlew :customer-hub-swing:clean :customer-hub-swing:buildApp
```

> **Note**: Windows環境では Git Bash を使用してください

ビルドが成功すると、以下のファイルが生成されます：
- `build/libs/customer-hub-swing-1.0.0.jar` (実行可能JAR、依存関係含む)

### ⑤ アプリケーションを実行する方法

#### Git Bash / Mac / Linux での実行手順

```bash
# 1. customer-hub-swingディレクトリに移動（プロジェクトルートから）
cd projects/master/bookstore/customer-hub-swing

# 2. アプリケーションを実行
java -jar build/libs/customer-hub-swing-1.0.0.jar

# または、API URLを指定する場合:
java -jar build/libs/customer-hub-swing-1.0.0.jar http://localhost:8080/customer-hub-api
```

> **Note**: 
> - **Windows環境では Git Bash を使用してください**（上記コマンドがそのまま動作します）
> - Mac/Linuxでも同じコマンドで実行できます

### ⑥ プロジェクトを終了するときに1回だけ実行（CleanUp）

```bash
# アプリケーションウィンドウを閉じる
# または、ターミナルで Ctrl+C を押す
```

### ⑦ アプリケーション更新のたびに実行

コードを変更した場合：

```bash
# プロジェクトルートで実行（Git Bash / Mac / Linux）

# 1. 再ビルド
./gradlew :customer-hub-swing:clean :customer-hub-swing:buildApp

# 2. 再実行
cd projects/master/bookstore/customer-hub-swing
java -jar build/libs/customer-hub-swing-1.0.0.jar
```

## 🎯 プロジェクト構成

```
projects/master/bookstore/customer-hub-swing/
├── src/
│   └── main/
│       └── java/
│           └── pro/
│               └── kensait/
│                   └── berrybooks/
│                       ├── BerryBooksSwingApp.java        # メインアプリケーションクラス（JFrame）
│                       ├── api/
│                       │   └── BerryBooksApiClient.java   # REST APIクライアント
│                       ├── model/
│                       │   ├── CustomerStats.java         # 顧客統計モデル
│                       │   └── CustomerTO.java            # 顧客基本情報モデル
│                       └── ui/
│                           └── CustomerEditDialog.java    # 編集ダイアログ
├── build.gradle                                          # Gradleビルド設定
├── run-app.sh                                            # 自動実行スクリプト（Unix/Mac/Git Bash）
├── run-app.bat                                           # 自動実行スクリプト（Windows）
└── README.md
```

## 🔧 使用している技術

- **Java 8以降** - Swing GUI
- **Swing** - GUI（JFrame, JTable, JDialog）
- **HttpURLConnection** - REST API通信
- **org.json** - JSONパース
- **Gradle** - ビルドツール

## 🎨 デザイン仕様

- **テーマカラー**: `#CF3F4E` (ストロベリーレッド)
- **アプリケーション名**: Berry Books 管理者画面
- Berry Booksブランドと統一されたデザインテーマ
- Swingコンポーネントのカスタムカラー設定

## 🎯 主な機能

### 1. 顧客一覧表示

- 全顧客の情報をテーブル表示（JTable）
- 顧客ID、顧客名、メールアドレス、生年月日、住所を表示

### 2. 顧客情報編集

- 各顧客行の「編集」ボタンをクリックして編集ダイアログを表示（JDialog）
- 顧客名、メールアドレス、生年月日、住所を編集可能
- フォームバリデーション機能搭載
- 編集後、REST API (`customer-api`) に更新データを送信

### 3. 統計情報表示

- 各顧客の注文件数
- 各顧客の購入冊数（合計）

### 4. リアルタイムデータ取得

- REST API (`customer-api`) からデータを取得
- バックグラウンドスレッドで非同期取得
- エラーハンドリング（JOptionPane）

## 🌐 API仕様

このアプリケーションは以下のAPIを使用します：

### 1. 顧客一覧取得
- **エンドポイント**: `GET /customers/`
- **レスポンス**: `CustomerStatsTO[]`

### 2. 顧客情報更新
- **エンドポイント**: `PUT /customers/{customerId}`
- **リクエストボディ**: 
```json
{
  "customerName": "山田太郎",
  "email": "yamada@example.com",
  "birthday": "1990-01-01",
  "address": "東京都渋谷区"
}
```
- **レスポンス**: 成功時は200 OK

> **Note:** デフォルトのAPIベースURLは `http://localhost:8080/customer-hub-api` です。

### データモデル (CustomerStatsTO)

```json
{
  "customerId": 1,
  "customerName": "山田太郎",
  "email": "yamada@example.com",
  "birthday": "1990-01-01",
  "address": "東京都渋谷区",
  "orderCount": 5,
  "totalBooks": 12
}
```

## ⚙️ 起動手順（全体）

以下は、システム全体を起動する完全な手順です。

### ① HSQLDBサーバーを起動

```bash
# プロジェクトルートで実行（Git Bash / Mac / Linux）
./gradlew startHsqldb
```

### ② Payara Serverを起動

```bash
# プロジェクトルートで実行（Git Bash / Mac / Linux）
./gradlew startPayara
```

### ③ データソースをセットアップ（初回のみ）

```bash
# プロジェクトルートで実行（Git Bash / Mac / Linux）
./gradlew setupDataSource
```

### ④ データベースを初期化（初回のみ）

```bash
# プロジェクトルートで実行（Git Bash / Mac / Linux）
./gradlew :customer-hub-api:setupHsqldb
```

### ⑤ customer-hub-api をデプロイ

```bash
# プロジェクトルートで実行（Git Bash / Mac / Linux）
./gradlew :customer-hub-api:war
./gradlew :customer-hub-api:deploy
```

### ⑥ Swingアプリケーションをビルド

```bash
# プロジェクトルートで実行（Git Bash / Mac / Linux）
./gradlew :customer-hub-swing:buildApp
```

### ⑦ Swingアプリケーションを実行

```bash
# customer-hub-swingディレクトリで実行（Git Bash / Mac / Linux）
cd projects/master/bookstore/customer-hub-swing
java -jar build/libs/customer-hub-swing-1.0.0.jar
```

### ⑧ アプリケーションで顧客一覧を確認

デスクトップアプリケーションウィンドウが開き、顧客一覧が表示されます。

## 🛑 アプリケーションを停止する

### 停止手順

```bash
# Git Bash / Mac / Linux で実行

# 1. Swingアプリケーションを閉じる（ウィンドウを閉じるか Ctrl+C）

# 2. バックエンドをアンデプロイ（プロジェクトルートで実行）
./gradlew :customer-hub-api:undeploy

# 3. Payara Serverを停止（プロジェクトルートで実行）
./gradlew stopPayara

# 4. HSQLDBサーバーを停止（プロジェクトルートで実行）
./gradlew stopHsqldb
```

## 🔍 トラブルシューティング

### 1. ClassNotFoundExceptionが発生

**症状:** `java.lang.ClassNotFoundException: pro.kensait.berrybooks.BerryBooksSwingApp`

**原因:** JARファイルがビルドされていないか、パスが間違っています。

**解決方法:**

```bash
# Git Bash / Mac / Linux で実行

# JARファイルが存在するか確認
ls -la build/libs/customer-hub-swing-1.0.0.jar

# 存在しない場合は再ビルド
cd ../../..  # プロジェクトルートへ
./gradlew :customer-hub-swing:clean :customer-hub-swing:buildApp
```

### 2. REST APIに接続できない

**症状:** 「読み込み中...」のまま止まる、またはエラーダイアログが表示される

**原因:** バックエンド（customer-hub-api）が起動していません。

**解決方法:**

- バックエンド（customer-hub-api）が起動しているか確認
- `http://localhost:8080/customer-hub-api/customers/` にブラウザでアクセスできるか確認
- REST APIのステータスを確認（`./gradlew statusPayara`）

### 3. JSON-lib not foundエラー

**症状:** `java.lang.NoClassDefFoundError: org/json/JSONArray`

**原因:** Fat JAR（依存関係を含むJAR）が正しくビルドされていません。

**解決方法:**

```bash
# 再ビルド（clean してから build）
./gradlew :customer-hub-swing:clean :customer-hub-swing:buildApp

# JARのサイズを確認（依存関係を含むため100KB以上あるはず）
ls -lh build/libs/customer-hub-swing-1.0.0.jar
```

### 4. ウィンドウが表示されない

**症状:** コマンドを実行してもウィンドウが表示されない

**原因:** Javaのヘッドレスモードが有効になっている可能性があります。

**解決方法:**

```bash
# GUIが使用可能か確認
java -version

# 明示的にヘッドレスを無効化して実行
java -Djava.awt.headless=false -jar build/libs/berry-books-swing-1.0.0.jar
```

### 5. テーブルヘッダーが表示されない

**症状:** テーブルのカラム名（顧客ID、顧客名など）が表示されない

**原因:** JScrollPaneのヘッダー設定の問題（既に修正済み）

**解決方法:**

```bash
# 最新版を再ビルド
./gradlew :customer-hub-swing:clean :customer-hub-swing:buildApp
```

## 📖 参考リンク

- [Java Swing Documentation](https://docs.oracle.com/javase/tutorial/uiswing/)
- [customer-hub-api](../customer-hub-api/README.md)
- [customer-hub-spa (React版)](../customer-hub-spa/README.md)
- [bookstoreルートREADME](../README.md)

## 🆚 他のクライアントとの比較

| 機能 | React版 (推奨) | Java Swing版 (本プロジェクト) |
|------|----------------|------------------------------|
| 現代のブラウザサポート | ✅ Yes | N/A (デスクトップアプリ) |
| モダンUI/UX | ✅ Yes | ⚠️ OS標準 |
| セキュリティ | ✅ Safe | ✅ Safe |
| モバイル対応 | ✅ Yes | ❌ No |
| 開発環境 | Node.js, npm | JDK/JRE 8+ |
| 配布の容易さ | ✅ Easy | ⚠️ JAR配布 |
| オフライン動作 | ❌ No | ✅ Yes (APIは必要) |
| 学習価値 | ✅ Modern | 🎓 Desktop GUI |

**結論**: 
- **Webアプリケーションが必要な場合**: React版を使用してください
- **デスクトップアプリケーションが必要な場合**: 本プロジェクト（Java Swing版）を使用してください

## 📄 ライセンス

このプロジェクトは教育目的で作成されています。

---

**Happy Coding! 🍓**


