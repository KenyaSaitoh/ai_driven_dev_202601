# berry-books-mvc-vibe - 設定ガイド

## REST API URL設定

berry-books-mvc-vibeアプリケーションは、顧客情報の取得にberry-books-rest APIを使用します。
API URLは環境に応じて柔軟に設定できます。

### 設定の優先順位

設定値は以下の優先順位で読み込まれます（上位が優先）：

1. **システムプロパティ** - JVM起動オプション（`-D`オプション）
2. **環境変数** - OSの環境変数
3. **プロパティファイル** - `META-INF/microprofile-config.properties`
4. **デフォルト値** - コード内のデフォルト値

---

## 環境別の設定方法

### 開発環境（デフォルト）

プロパティファイルの設定が使用されます。

**設定ファイル**: `src/main/resources/META-INF/microprofile-config.properties`

```properties
customer.api.base-url=http://localhost:8080/berry-books-rest/customers
```

特に何も設定しなければ、この値が使用されます。

---

### テスト環境

#### 方法1: Payara Serverの`domain.xml`に設定（推奨）

Payara Serverの設定ファイルにシステムプロパティを追加します。

**設定ファイル**: `payara6/glassfish/domains/domain1/config/domain.xml`

`<java-config>`セクションに以下を追加：

```xml
<java-config ...>
  <!-- 既存の設定 -->
  
  <!-- テスト環境のAPI URL -->
  <jvm-options>-Dcustomer.api.base-url=http://test-server:8080/berry-books-rest/customers</jvm-options>
</java-config>
```

設定後、Payara Serverを再起動：

```bash
./gradlew stopPayara
./gradlew startPayara
```

#### 方法2: デプロイ時にシステムプロパティを指定

デプロイ時にシステムプロパティを指定することも可能です（ただし、Gradleタスクのカスタマイズが必要）。

---

### 本番環境

#### 方法1: Payara Serverの`domain.xml`に設定（推奨）

テスト環境と同様に、`domain.xml`にシステムプロパティを追加します。

```xml
<java-config ...>
  <!-- 本番環境のAPI URL -->
  <jvm-options>-Dcustomer.api.base-url=https://prod-server.example.com/berry-books-rest/customers</jvm-options>
</java-config>
```

#### 方法2: 環境変数を設定

OSの環境変数を設定してからPayara Serverを起動します。

**Linux/macOS:**

```bash
export CUSTOMER_API_BASE_URL=https://prod-server.example.com/berry-books-rest/customers
./gradlew startPayara
```

**Windows PowerShell:**

```powershell
$env:CUSTOMER_API_BASE_URL = "https://prod-server.example.com/berry-books-rest/customers"
./gradlew startPayara
```

**Windows CMD:**

```cmd
set CUSTOMER_API_BASE_URL=https://prod-server.example.com/berry-books-rest/customers
gradlew startPayara
```

---

## 設定値の確認

アプリケーション起動時に、ログに以下のような出力が表示されます：

```
[INFO] CustomerRestClient initialized with baseUrl: http://localhost:8080/berry-books-rest/customers
```

このログで、実際に使用されているURL設定を確認できます。

---

## トラブルシューティング

### 設定が反映されない

1. **Payara Serverを再起動したか確認**
   ```bash
   ./gradlew stopPayara
   ./gradlew startPayara
   ```

2. **ログで設定値を確認**
   ```bash
   tail -f -n 50 payara6/glassfish/domains/domain1/logs/server.log | grep CustomerRestClient
   ```

3. **システムプロパティの構文を確認**
   - プロパティ名: `customer.api.base-url`（ハイフン区切り）
   - 環境変数名: `CUSTOMER_API_BASE_URL`（アンダースコア区切り、大文字）

### URLが間違っている

REST APIサーバー（berry-books-rest）が以下のURLでアクセス可能か確認：

```bash
# 開発環境
curl http://localhost:8080/berry-books-rest/customers/1

# テスト環境
curl http://test-server:8080/berry-books-rest/customers/1

# 本番環境
curl https://prod-server.example.com/berry-books-rest/customers/1
```

---

## まとめ

| 環境 | 推奨設定方法 | 設定場所 |
|------|------------|---------|
| **開発** | プロパティファイル | `src/main/resources/META-INF/microprofile-config.properties` |
| **テスト** | システムプロパティ（domain.xml） | `payara6/glassfish/domains/domain1/config/domain.xml` |
| **本番** | システムプロパティ（domain.xml） | `payara6/glassfish/domains/domain1/config/domain.xml` |

この設定方法により、Spring Bootのプロファイル機能と同様に、環境ごとに異なる設定を柔軟に適用できます。

