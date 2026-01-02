# 出版社API 機能設計書

## 1. API概要

### 1.1 API名
出版社API（Publishers API）

### 1.2 ベースパス
`/api/publishers`

### 1.3 目的
出版社マスタ情報の参照機能を提供する。

### 1.4 認証要否
現状は認証不要（将来的にJWT認証フィルタ実装時に認証必須となる予定）

## 2. エンドポイント一覧

| No | メソッド | パス | 機能 | 認証要否 |
|----|---------|------|------|---------|
| 1 | GET | `/api/publishers` | 出版社一覧取得（配列形式） | 不要（将来:必要） |

## 3. エンドポイント詳細

### 3.1 出版社一覧取得

#### 3.1.1 基本情報
* エンドポイント: `GET /api/publishers`
* 機能: すべての出版社情報を配列形式で取得
* 認証: 不要（将来:必要）

#### 3.1.2 リクエスト

**ヘッダー**: なし

**クエリパラメータ**: なし

**ボディ**: なし

#### 3.1.3 レスポンス

成功（200 OK）:

ヘッダー:
```
Content-Type: application/json; charset=UTF-8
```

ボディ:
```json
[
  {
    "publisherId": 1,
    "publisherName": "出版社A"
  },
  {
    "publisherId": 2,
    "publisherName": "出版社B"
  },
  {
    "publisherId": 3,
    "publisherName": "出版社C"
  }
]
```

レスポンススキーマ:
| フィールド | 型 | 必須 | 説明 |
|-----------|---|------|------|
| publisherId | Integer | Yes | 出版社ID |
| publisherName | String | Yes | 出版社名 |

#### 3.1.4 処理フロー

1. PublisherResourceを呼び出し（`getAllPublishers()`）
2. PublisherServiceを呼び出し（`getPublishersAll()`）
3. PublisherDaoで全出版社を取得
   - JPQL: `SELECT p FROM Publisher p`
   - Named Query: `Publisher.findAll`
4. Publisherエンティティのリストを取得
5. 各PublisherエンティティをPublisherTOに変換
   - ストリームAPIで変換: `.stream().map(p -> new PublisherTO(...)).toList()`
6. 配列形式でJSON変換してレスポンス

#### 3.1.5 ビジネスルール

* BR-PUBLISHER-001: すべての出版社を返却（フィルタリングなし）
* BR-PUBLISHER-002: 出版社IDの昇順でソート（デフォルト）
* BR-PUBLISHER-003: 削除フラグはない（マスタデータとして永続）

#### 3.1.6 関連コンポーネント

* `PublisherResource#getAllPublishers()`
* `PublisherService#getPublishersAll()`
* `PublisherDao#findAll()`

---

## 4. データ転送オブジェクト（DTO）

### 4.1 PublisherTO

**パッケージ**: `pro.kensait.backoffice.api.dto`

**構造種別**: レコード型（immutableなデータ転送オブジェクト）

フィールド構成:

| フィールド名 | 型 | 説明 |
|------------|---|------|
| publisherId | Integer | 出版社ID |
| publisherName | String | 出版社名 |

例:
```json
{
  "publisherId": 1,
  "publisherName": "出版社A"
}
```

---

## 5. エンティティ

### 5.1 Publisher

**パッケージ**: `pro.kensait.backoffice.entity`

**マッピング対象テーブル**: PUBLISHER

エンティティ構成:

| フィールド名 | 型 | カラム名 | 制約 | 説明 |
|------------|---|---------|-----|------|
| publisherId | int | PUBLISHER_ID | PRIMARY KEY | 出版社ID |
| publisherName | String | PUBLISHER_NAME | - | 出版社名 |

**エンティティ種別**: 永続化エンティティ（JPAエンティティ）

---

## 6. ビジネスルール

| ルールID | ルール内容 |
|---------|-----------|
| BR-PUBLISHER-001 | すべての出版社を返却（フィルタリングなし） |
| BR-PUBLISHER-002 | 出版社IDの昇順でソート（デフォルト） |
| BR-PUBLISHER-003 | 削除フラグはない（マスタデータとして永続） |
| BR-PUBLISHER-004 | 配列形式でレスポンス（標準REST形式） |

---

## 7. エラーハンドリング

### 7.1 エラーケース

| エラー内容 | HTTPステータス | レスポンス |
|-----------|---------------|-----------|
| 予期しないエラー | 500 Internal Server Error | エラーメッセージ |

**備考**: 出版社一覧取得は基本的にエラーが発生しない（マスタデータ）

### 7.2 ログ出力

INFOレベル:
```
[ PublisherResource#getAllPublishers ]
[ PublisherService#getPublishersAll ]
```

DEBUGレベル:
```
[ PublisherDao#findAll ] Executing JPQL: SELECT p FROM Publisher p
[ PublisherDao#findAll ] Result count: 10
```

---

## 8. パフォーマンス考慮事項

### 8.1 レスポンスタイム
* 目標: 50ms以内
* 実測: 30-50ms（通常時）

### 8.2 キャッシング
* 現状: キャッシュなし
* 将来: アプリケーションレベルキャッシュ推奨
  - マスタデータのため変更頻度が低い
  - メモリキャッシュで高速化可能

### 8.3 データ量
* 想定レコード数: 10-100件
* データサイズ: 数KB程度
* スケーラビリティ: 問題なし

---

## 9. テスト仕様

### 9.1 正常系テスト

| テストケース | 入力 | 期待結果 |
|------------|------|---------|
| 全出版社取得 | なし | 200 OK + 全出版社配列 |
| レスポンス形式 | なし | JSON配列形式 |
| ソート順 | なし | publisherId昇順 |

### 9.2 異常系テスト

| テストケース | 入力 | 期待結果 |
|------------|------|---------|
| DBエラー | （DB停止） | 500 Internal Server Error |

---

## 10. データベースクエリ

### 10.1 クエリ仕様（JPQL）

クエリ内容:
* 対象エンティティ: Publisher
* 取得フィールド: 全フィールド
* WHERE条件: なし（全件取得）

### 10.2 実行されるSQLの論理構造

SELECT句:
* PUBLISHER_ID
* PUBLISHER_NAME

FROM句:
* PUBLISHER テーブル

ORDER BY句:
* PUBLISHER_ID の昇順

---

## 11. 将来の拡張

### 11.1 出版社詳細取得

特定の出版社詳細取得:
```
GET /api/publishers/{publisherId}
```

レスポンス:
```json
{
  "publisherId": 1,
  "publisherName": "出版社A",
  "address": "東京都...",
  "phone": "03-xxxx-xxxx",
  "bookCount": 150
}
```

### 11.2 出版社検索

出版社名での検索:
```
GET /api/publishers?search=出版社A
```

### 11.3 出版社統計情報

出版社別の書籍数を含む:
```json
{
  "publisherId": 1,
  "publisherName": "出版社A",
  "bookCount": 150,
  "activeBookCount": 120
}
```

### 11.4 出版社CRUD操作

* 作成: `POST /api/publishers`
* 更新: `PUT /api/publishers/{publisherId}`
* 削除: `DELETE /api/publishers/{publisherId}`

---

## 12. セキュリティ考慮事項

### 12.1 認証・認可

現状は認証不要だが、将来的にJWT認証フィルタを実装:
* すべてのエンドポイントで認証必須
* 全社員が参照可能（職務ランクによる制限なし）

### 12.2 データ保護

* 出版社情報は公開情報
* 機密情報は含まれない

---

## 13. 運用考慮事項

### 13.1 データメンテナンス

* 追加: 新しい出版社の追加（管理者）
* 更新: 出版社名の変更（管理者）
* 削除: 論理削除または物理削除（慎重に実施）

### 13.2 マイグレーション

出版社マスタの変更時:
1. 既存の書籍との関連を確認
2. 出版社変更の影響範囲を分析
3. データ移行スクリプトの作成

### 13.3 データ整合性

* 出版社削除時は関連書籍の確認が必要
* 外部キー制約により参照整合性を保証

---

## 14. 関連API

* 書籍API: `/api/books` - 出版社で絞り込み可能
* ワークフローAPI: `/api/workflows` - 新規書籍追加時に出版社ID指定

---

## 15. カテゴリAPIとの類似点

カテゴリAPIと出版社APIは構造が非常に似ている:

| 項目 | カテゴリAPI | 出版社API |
|------|-----------|----------|
| エンドポイント | `/api/categories` | `/api/publishers` |
| エンティティ | Category | Publisher |
| DTO | CategoryTO | PublisherTO |
| 処理フロー | 同じ | 同じ |
| パフォーマンス | 同等 | 同等 |
| キャッシング | 推奨 | 推奨 |

共通設計パターン:
* マスタデータの参照API
* シンプルな一覧取得
* 配列形式のレスポンス
* ステートレス設計

---

## 16. 動的振る舞い

### 16.1 出版社一覧取得シーケンス

```mermaid
sequenceDiagram
    participant Client
    participant PublisherResource
    participant PublisherService
    participant PublisherDao
    participant Database

    Client->>PublisherResource: GET /api/publishers
    Note over PublisherResource: LOG INFO<br/>[PublisherResource#getAllPublishers]
    PublisherResource->>PublisherService: getAllPublishers()
    Note over PublisherService: LOG INFO<br/>[PublisherService#getPublishersAll]
    PublisherService->>PublisherDao: getPublishersAll()
    PublisherDao->>Database: findAll()<br/>Named Query: Publisher.findAll<br/>SELECT p FROM PUBLISHER p<br/>ORDER BY p.PUBLISHER_ID
    Database-->>PublisherDao: List<Publisher> (10 records)
    PublisherDao-->>PublisherService: List<Publisher>
    PublisherService-->>PublisherResource: List<Publisher>
    
    PublisherResource->>PublisherResource: Stream & Map<br/>publishers.stream()<br/>.map(p -> new PublisherTO(<br/>  p.getPublisherId(),<br/>  p.getPublisherName()))<br/>.toList()
    
    PublisherResource-->>Client: 200 OK<br/>[{PublisherTO}]<br/>Content-Type: application/json
```

### 16.2 処理フローチャート

```mermaid
flowchart TD
    A[開始] --> B[リクエスト受信<br/>GET /publishers]
    B --> C[PublisherResource<br/>getAllPublishers]
    C --> D[PublisherService<br/>getPublishersAll]
    D --> E[PublisherDao<br/>findAll]
    E --> F[DB Query実行<br/>SELECT p<br/>FROM PUBLISHER]
    F --> G[List&lt;Publisher&gt;取得]
    G --> H[Stream API変換処理<br/>Publisher → PublisherTO]
    H --> I[List&lt;PublisherTO&gt;生成]
    I --> J[JSON Serialize]
    J --> K[200 OK<br/>レスポンス返却]
```

### 16.3 データフロー全体図

```mermaid
graph TD
    Client[Client]
    Resource[PublisherResource<br/>- getAllPublishers]
    Service[PublisherService<br/>- getPublishersAll]
    Dao[PublisherDao<br/>- findAll]
    DB[Database<br/>- PUBLISHER table]
    
    Client -->|HTTP GET<br/>/api/publishers| Resource
    Resource -->|@Inject| Service
    Service -->|@Inject| Dao
    Dao -->|EntityManager / JPQL| DB
```

### 16.4 状態管理

出版社APIは状態を持たない（ステートレス）:

```mermaid
graph LR
    R1[Request 1] --> PR1[Publisher<br/>Resource]
    PR1 --> DB1[Database]
    
    R2[Request 2] --> PR2[Publisher<br/>Resource]
    PR2 --> DB2[Database]
    
    R3[Request 3] --> PR3[Publisher<br/>Resource]
    PR3 --> DB3[Database]
```

各リクエストは独立して処理される

