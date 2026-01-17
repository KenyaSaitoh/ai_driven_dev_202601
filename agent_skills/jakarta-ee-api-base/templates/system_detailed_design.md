# システム詳細設計書

ファイル名: detailed_design.md（system/配下）  
プロジェクトID: [PROJECT_ID]  
バージョン: 1.0.0  
最終更新: [DATE]

---

## 1. 概要

本文書は、システム全体の共通処理、ドメインモデル（JPAエンティティ）、共通サービスの詳細設計を記述する。

* API固有の詳細設計は、各api/{api_id}/detailed_design.mdを参照すること

---

## 2. ドメインモデル（JPAエンティティ）

### 2.1 [Entity名1]

* テーブル: `[TABLE_NAME]`

* 主要フィールド:

| フィールド名 | 型 | カラム名 | 制約 | 説明 |
|------------|---|---------|-----|------|
| `[フィールド]` | `[型]` | `[カラム]` | `[制約]` | [説明] |

* アノテーション:
```java
@Entity
@Table(name = "[TABLE_NAME]")
```

* リレーション:
  * `@ManyToOne` - [関連エンティティ]
  * `@OneToMany` - [関連エンティティ]

* 楽観的ロック（該当する場合）:
```java
@Version
private Integer version;
```

---

### 2.2 [Entity名2]

[必要に応じて追加のエンティティを記述]

---

## 3. データアクセス層（Dao）

### 3.1 [Dao名1]

* 責務: [責務の説明]

* アノテーション:
  * `@ApplicationScoped`（依存性注入）

* 主要メソッド:

#### [メソッド名]()

* シグネチャ:
```java
public [戻り値型] [メソッド名]([引数])
```

* JPQL:
```sql
[JPQLクエリ]
```

* 処理:
  1. [ステップ1]
  2. [ステップ2]

---

### 3.2 [Dao名2]

[必要に応じて追加のDaoを記述]

---

## 4. ビジネスロジック層（共通Service）

注意: このセクションには、複数のAPIで共有されるビジネスロジックのみを記述します。API固有のビジネスロジックは、api/{api_id}/detailed_design.mdに記述してください。

### 4.1 [共通Service名1]

* 責務: [責務の説明]（複数のAPIで共有される）

* アノテーション:
  * `@ApplicationScoped`（依存性注入）
  * `@Transactional`（該当する場合）

* 主要メソッド:

#### [メソッド名]()

* シグネチャ:
```java
public [戻り値型] [メソッド名]([引数])
```

* 処理:
  1. [ステップ1]
  2. [ステップ2]

* トランザクション境界: [説明]

* 使用API: [API_001, API_002, ...] （どのAPIで使用されるか）

---

### 4.2 [共通Service名2]

[必要に応じて追加の共通Serviceを記述]

注意: API固有のServiceメソッドは、このセクションには含めません。

---

## 5. セキュリティコンポーネント

### 5.1 JwtUtil

* 責務: JWT生成・検証

* 主要メソッド:

#### generateToken()

* シグネチャ:
```java
public String generateToken(Long userId, String email)
```

* 処理:
  1. ペイロード作成（userId, email, iat, exp）
  2. HMAC-SHA256で署名
  3. JWT文字列を返却

#### validateToken()

* シグネチャ:
```java
public boolean validateToken(String token)
```

* 処理:
  1. 署名検証
  2. 有効期限チェック
  3. 検証結果を返却

---

### 5.2 JwtAuthenFilter

* 責務: JWT認証フィルター

* 処理フロー:
  1. リクエストURIからコンテキストパスを除外
  2. 公開エンドポイントかどうかを判定
  3. 公開エンドポイントの場合は認証をスキップ
  4. JWT Cookieを抽出
  5. JWTトークンを検証
  6. 認証情報をスレッドローカルに保存
  7. 次の処理にチェーンする

---

## 6. ユーティリティクラス

### 6.1 [Utility名]

* 責務: [責務の説明]

* 主要メソッド:

#### [メソッド名]()

* シグネチャ:
```java
public static [戻り値型] [メソッド名]([引数])
```

* 処理:
  1. [ステップ1]
  2. [ステップ2]

---

## 7. 共通例外クラス

### 7.1 [Exception名]

* 責務: [責務の説明]

* 実装:
```java
public class [Exception名] extends RuntimeException {
    public [Exception名](String message) {
        super(message);
    }
}
```

---

## 8. Exception Mapper

### 8.1 [ExceptionMapper名]

* 責務: [例外名]のHTTPレスポンスへのマッピング

* 実装:
```java
@Provider
public class [ExceptionMapper名] implements ExceptionMapper<[Exception名]> {
    @Override
    public Response toResponse([Exception名] exception) {
        // エラーレスポンス作成
        return Response.status([STATUS])
                .entity(new ErrorResponse(...))
                .build();
    }
}
```

---

## 9. 設定情報

### 9.1 MicroProfile Config

* ファイル: `src/main/resources/META-INF/microprofile-config.properties`

```properties
[設定項目]
```

### 9.2 persistence.xml

* ファイル: `src/main/resources/META-INF/persistence.xml`

```xml
<persistence-unit name="[PU_NAME]" transaction-type="JTA">
    <jta-data-source>jdbc/HsqldbDS</jta-data-source>
    <properties>
        [設定項目]
    </properties>
</persistence-unit>
```

---

## 10. 参考資料

* [data_model.md](data_model.md) - データモデル仕様書
* [functional_design.md](functional_design.md) - システム機能設計書
* [architecture_design.md](architecture_design.md) - アーキテクチャ設計書
* [../api/[API_ID]/detailed_design.md](../api/[API_ID]/detailed_design.md) - API詳細設計書
