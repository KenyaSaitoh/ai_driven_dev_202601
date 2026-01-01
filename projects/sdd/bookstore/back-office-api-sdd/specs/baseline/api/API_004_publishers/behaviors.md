# 出版社API 振る舞い仕様書

## 1. 概要

本ドキュメントは、出版社API（`/api/publishers`）の動的な振る舞いをシーケンス図とフローチャートで定義する。

## 2. 出版社一覧取得シーケンス

### 2.1 正常系: 全件取得

```
┌──────┐         ┌─────────────┐      ┌──────────────┐      ┌───────────┐      ┌────────┐
│Client│         │PublisherResource     │PublisherService      │PublisherDao      │Database│
└──┬───┘         └──────┬──────┘      └───────┬──────┘      └─────┬─────┘      └───┬────┘
   │                     │                      │                    │               │
   │GET /api/publishers  │                      │                    │               │
   ├────────────────────►│                      │                    │               │
   │                     │                      │                    │               │
   │                     │LOG INFO              │                    │               │
   │                     │──────────┐           │                    │               │
   │                     │          │           │                    │               │
   │                     │[ PublisherResource#  │                    │               │
   │                     │  getAllPublishers ]  │                    │               │
   │                     │◄─────────┘           │                    │               │
   │                     │                      │                    │               │
   │                     │getAllPublishers()    │                    │               │
   │                     ├─────────────────────►│                    │               │
   │                     │                      │                    │               │
   │                     │                      │LOG INFO            │               │
   │                     │                      │──────────┐         │               │
   │                     │                      │          │         │               │
   │                     │                      │[ PublisherService# │               │
   │                     │                      │  getPublishersAll ]│               │
   │                     │                      │◄─────────┘         │               │
   │                     │                      │                    │               │
   │                     │                      │getPublishersAll()  │               │
   │                     │                      ├───────────────────►│               │
   │                     │                      │                    │               │
   │                     │                      │                    │findAll()      │
   │                     │                      │                    ├──────────────►│
   │                     │                      │                    │               │
   │                     │                      │                    │Named Query:   │
   │                     │                      │                    │Publisher.     │
   │                     │                      │                    │ findAll       │
   │                     │                      │                    │               │
   │                     │                      │                    │SELECT p       │
   │                     │                      │                    │FROM PUBLISHER │
   │                     │                      │                    │ p             │
   │                     │                      │                    │ORDER BY       │
   │                     │                      │                    │ p.PUBLISHER_ID│
   │                     │                      │                    │               │
   │                     │                      │                    │◄──────────────┤
   │                     │                      │                    │List<Publisher>│
   │                     │                      │                    │ (10 records)  │
   │                     │                      │◄───────────────────┤               │
   │                     │                      │List<Publisher>     │               │
   │                     │◄─────────────────────┤                    │               │
   │                     │List<Publisher>       │                    │               │
   │                     │                      │                    │               │
   │                     │Stream & Map          │                    │               │
   │                     │──────────┐           │                    │               │
   │                     │          │           │                    │               │
   │                     │publishers.stream()   │                    │               │
   │                     │ .map(p -> new        │                    │               │
   │                     │   PublisherTO(       │                    │               │
   │                     │    p.getPublisherId(),                    │               │
   │                     │    p.getPublisherName()                   │               │
   │                     │   ))                 │                    │               │
   │                     │ .toList()            │                    │               │
   │                     │◄─────────┘           │                    │               │
   │                     │                      │                    │               │
   │◄────────────────────┤200 OK                │                    │               │
   │[{PublisherTO}]      │Content-Type:         │                    │               │
   │                     │application/json      │                    │               │
   │                     │                      │                    │               │
```

### 2.2 データ変換の詳細

```
Publisher Entity        →    PublisherTO
───────────────────────────────────────────
publisherId: 1          →    publisherId: 1
publisherName: "出版社A" →    publisherName: "出版社A"

publisherId: 2          →    publisherId: 2
publisherName: "出版社B" →    publisherName: "出版社B"

publisherId: 3          →    publisherId: 3
publisherName: "出版社C" →    publisherName: "出版社C"
```

## 3. 処理フローチャート

```
┌──────────────┐
│  開始         │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│リクエスト受信  │
│GET /publishers│
└──────┬───────┘
       │
       ▼
┌──────────────┐
│PublisherResource│
│getAllPublishers()│
└──────┬───────┘
       │
       ▼
┌──────────────┐
│PublisherService│
│getPublishersAll()│
└──────┬───────┘
       │
       ▼
┌──────────────┐
│PublisherDao  │
│findAll()     │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│DB Query実行   │
│SELECT p      │
│FROM PUBLISHER│
└──────┬───────┘
       │
       ▼
┌──────────────┐
│List<Publisher>│
│取得           │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│Stream API    │
│変換処理       │
│Publisher→    │
│PublisherTO   │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│List<PublisherTO│
│生成           │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│JSON Serialize│
└──────┬───────┘
       │
       ▼
┌──────────────┐
│200 OK        │
│レスポンス返却│
└──────────────┘
```

## 4. データフロー全体図

```
┌──────────────────────────────────────────────────────┐
│                       Client                         │
└───────────────────────┬──────────────────────────────┘
                        │ HTTP GET
                        │ /api/publishers
                        ▼
┌──────────────────────────────────────────────────────┐
│                 PublisherResource                    │
│  - getAllPublishers()                                │
└───────────────────────┬──────────────────────────────┘
                        │ @Inject
                        ▼
┌──────────────────────────────────────────────────────┐
│                 PublisherService                     │
│  - getPublishersAll()                                │
└───────────────────────┬──────────────────────────────┘
                        │ @Inject
                        ▼
┌──────────────────────────────────────────────────────┐
│                   PublisherDao                       │
│  - findAll()                                         │
└───────────────────────┬──────────────────────────────┘
                        │ EntityManager / JPQL
                        ▼
┌──────────────────────────────────────────────────────┐
│                     Database                         │
│  - PUBLISHER table                                   │
└──────────────────────────────────────────────────────┘
```

## 5. パフォーマンス特性

### 5.1 レスポンスタイム分析

```
出版社一覧取得 (Total: ~50ms)

┌────────────────────────────────────────────────────────┐
│ DB Query (SELECT)                        20ms (40%)   │
├────────────────────────────────────────────────────────┤
│ Entity → DTO Conversion                  10ms (20%)   │
├────────────────────────────────────────────────────────┤
│ JSON Serialization                       10ms (20%)   │
├────────────────────────────────────────────────────────┤
│ その他（ネットワーク、オーバーヘッド）     10ms (20%)   │
└────────────────────────────────────────────────────────┘
```

### 5.2 キャッシング効果（将来）

```
キャッシュなし:
┌────────────────────────────────────────┐
│ First Request   ████████████  50ms    │
│ Second Request  ████████████  50ms    │
│ Third Request   ████████████  50ms    │
└────────────────────────────────────────┘

キャッシュあり（実装後）:
┌────────────────────────────────────────┐
│ First Request   ████████████  50ms    │
│ Second Request  █░░░░░░░░░░░  5ms     │
│ Third Request   █░░░░░░░░░░░  5ms     │
└────────────────────────────────────────┘
```

**推奨**: マスタデータのため、アプリケーションレベルキャッシュを実装

## 6. ログ出力シーケンス

### 6.1 正常系ログ

```
Time    Level   Message
─────────────────────────────────────────────────────────────
t1      INFO    [ PublisherResource#getAllPublishers ]
t2      INFO    [ PublisherService#getPublishersAll ]
t3      DEBUG   [ PublisherDao#findAll ] Executing JPQL: SELECT p FROM Publisher p
t4      DEBUG   [ PublisherDao#findAll ] Result count: 10
t5      DEBUG   [ PublisherResource ] Converting 10 entities to DTOs
t6      INFO    [ PublisherResource#getAllPublishers ] Success: 10 publishers returned
```

### 6.2 DBエラー時のログ

```
Time    Level   Message
─────────────────────────────────────────────────────────────
t1      INFO    [ PublisherResource#getAllPublishers ]
t2      INFO    [ PublisherService#getPublishersAll ]
t3      DEBUG   [ PublisherDao#findAll ] Executing JPQL: SELECT p FROM Publisher p
t4      ERROR   [ PublisherDao#findAll ] Database error
                java.sql.SQLException: Connection refused
t5      ERROR   [ PublisherResource#getAllPublishers ] Error retrieving publishers
t6      INFO    [ PublisherResource#getAllPublishers ] Returning 500 Internal Server Error
```

## 7. 並行処理シナリオ

### 7.1 同時リクエスト

```
時刻   ユーザーA                    ユーザーB                    ユーザーC
t1    GET /api/publishers
t2                                GET /api/publishers
t3                                                            GET /api/publishers
t4    → DB Query (1)
t5                                → DB Query (2)
t6                                                            → DB Query (3)
t7    ← Result (10 publishers)
t8                                ← Result (10 publishers)
t9                                                            ← Result (10 publishers)
```

**結果**: 独立したトランザクションで並行実行可能

### 7.2 大量リクエスト時

```
1000 concurrent requests

┌────────────────────────────────────────┐
│ Connection Pool (Max: 20)              │
├────────────────────────────────────────┤
│ All requests complete in ~3 seconds    │
│ Average response time: 150ms           │
│ (50ms query + 100ms wait)              │
└────────────────────────────────────────┘
```

**最適化案**:
- アプリケーションレベルキャッシュの実装
- CDNでの静的コンテンツ配信
- レスポンスの圧縮（gzip）

## 8. カテゴリAPIとの類似性

### 8.1 共通アーキテクチャパターン

```
┌─────────────────────────────────────────┐
│     マスタデータ参照APIパターン           │
├─────────────────────────────────────────┤
│                                         │
│  ┌───────────────┐  ┌──────────────┐   │
│  │Category API   │  │Publisher API │   │
│  ├───────────────┤  ├──────────────┤   │
│  │GET /categories│  │GET /publishers   │
│  │               │  │              │   │
│  │Resource       │  │Resource      │   │
│  │  ↓            │  │  ↓           │   │
│  │Service        │  │Service       │   │
│  │  ↓            │  │  ↓           │   │
│  │Dao            │  │Dao           │   │
│  │  ↓            │  │  ↓           │   │
│  │Database       │  │Database      │   │
│  └───────────────┘  └──────────────┘   │
│                                         │
│  同じ処理フロー                          │
│  同じデータ変換パターン                   │
│  同じエラーハンドリング                   │
└─────────────────────────────────────────┘
```

### 8.2 パフォーマンス比較

```
┌────────────────────────────────────────┐
│ Category API   ████████████  50ms     │
├────────────────────────────────────────┤
│ Publisher API  ████████████  50ms     │
└────────────────────────────────────────┘

ほぼ同等のパフォーマンス
```

## 9. 状態管理

出版社APIは状態を持たない（ステートレス）:

```
┌──────────────┐
│ Request 1    │ → ┌──────────┐ → ┌──────────┐
└──────────────┘   │ Publisher│   │ Database │
                   │ Resource │   └──────────┘
┌──────────────┐   └──────────┘
│ Request 2    │ → ┌──────────┐ → ┌──────────┐
└──────────────┘   │ Publisher│   │ Database │
                   │ Resource │   └──────────┘
┌──────────────┐   └──────────┘
│ Request 3    │ → ┌──────────┐ → ┌──────────┐
└──────────────┘   │ Publisher│   │ Database │
                   │ Resource │   └──────────┘
                   └──────────┘

各リクエストは独立して処理される
```

## 10. 将来の拡張シーケンス

### 10.1 出版社詳細取得（実装予定）

```
┌──────┐         ┌─────────────┐      ┌──────────────┐      ┌───────────┐
│Client│         │PublisherResource     │PublisherService      │PublisherDao
└──┬───┘         └──────┬──────┘      └───────┬──────┘      └─────┬─────┘
   │                     │                      │                    │
   │GET /api/publishers/1│                      │                    │
   ├────────────────────►│                      │                    │
   │                     │                      │                    │
   │                     │getPublisher(1)       │                    │
   │                     ├─────────────────────►│                    │
   │                     │                      │findById(1)         │
   │                     │                      ├───────────────────►│
   │                     │                      │                    │
   │                     │                      │◄───────────────────┤
   │                     │                      │Publisher           │
   │                     │◄─────────────────────┤                    │
   │                     │Publisher             │                    │
   │                     │                      │                    │
   │◄────────────────────┤200 OK                │                    │
   │{PublisherTO}        │                      │                    │
   │                     │                      │                    │
```

### 10.2 出版社統計情報（実装予定）

```
┌──────┐         ┌─────────────┐      ┌──────────────┐      ┌────────┐
│Client│         │PublisherResource     │PublisherService      │BookDao │
└──┬───┘         └──────┬──────┘      └───────┬──────┘      └───┬────┘
   │                     │                      │                 │
   │GET /api/publishers  │                      │                 │
   │  ?includeStats=true │                      │                 │
   ├────────────────────►│                      │                 │
   │                     │                      │                 │
   │                     │getPublishersWithStats()                │
   │                     ├─────────────────────►│                 │
   │                     │                      │                 │
   │                     │                      │countByPublisher()│
   │                     │                      ├────────────────►│
   │                     │                      │                 │
   │                     │                      │◄────────────────┤
   │                     │                      │Map<PublisherId, │
   │                     │                      │     Count>      │
   │                     │◄─────────────────────┤                 │
   │                     │                      │                 │
   │◄────────────────────┤200 OK                │                 │
   │[{publisherId: 1,    │                      │                 │
   │  publisherName:     │                      │                 │
   │   "出版社A",         │                      │                 │
   │  bookCount: 150}]   │                      │                 │
   │                     │                      │                 │
```

### 10.3 出版社CRUD操作（実装予定）

#### 10.3.1 出版社作成

```
┌──────┐         ┌─────────────┐      ┌──────────────┐
│Client│         │PublisherResource     │PublisherService
└──┬───┘         └──────┬──────┘      └───────┬──────┘
   │                     │                      │
   │POST /api/publishers │                      │
   │{                    │                      │
   │ publisherName:      │                      │
   │  "出版社D"           │                      │
   │}                    │                      │
   ├────────────────────►│                      │
   │                     │                      │
   │                     │createPublisher()     │
   │                     ├─────────────────────►│
   │                     │                      │
   │                     │                      │validate & persist
   │                     │                      │──────────┐
   │                     │                      │          │
   │                     │                      │◄─────────┘
   │                     │◄─────────────────────┤
   │                     │Publisher created     │
   │                     │                      │
   │◄────────────────────┤201 Created           │
   │{PublisherTO}        │Location:             │
   │                     │ /api/publishers/4    │
   │                     │                      │
```

## 11. エラーハンドリング

### 11.1 404 Not Found（出版社が見つからない - 将来）

```
┌──────┐         ┌─────────────┐      ┌──────────────┐
│Client│         │PublisherResource     │PublisherService
└──┬───┘         └──────┬──────┘      └───────┬──────┘
   │                     │                      │
   │GET /api/publishers/9999                    │
   ├────────────────────►│                      │
   │                     │getPublisher(9999)    │
   │                     ├─────────────────────►│
   │                     │                      │
   │                     │                      │findById() → null
   │                     │                      │──────────┐
   │                     │                      │          │
   │                     │                      │◄─────────┘
   │                     │◄─────────────────────┤null
   │                     │                      │
   │                     │Build Error Response  │
   │                     │──────────┐           │
   │                     │          │           │
   │                     │◄─────────┘           │
   │                     │                      │
   │◄────────────────────┤404 Not Found         │
   │{error: "出版社が      │                      │
   │ 見つかりません"}      │                      │
   │                     │                      │
```

## 12. キャッシング戦略（将来実装）

### 12.1 キャッシュフロー

```
┌──────┐         ┌─────────────┐      ┌───────┐      ┌────────┐
│Client│         │PublisherResource     │Cache  │      │Database│
└──┬───┘         └──────┬──────┘      └───┬───┘      └───┬────┘
   │                     │                  │              │
   │GET /api/publishers  │                  │              │
   ├────────────────────►│                  │              │
   │                     │                  │              │
   │                     │Check Cache       │              │
   │                     ├─────────────────►│              │
   │                     │                  │              │
   │                     │◄─────────────────┤Cache Miss    │
   │                     │                  │              │
   │                     │                  │              │
   │                     │Query DB──────────┼─────────────►│
   │                     │                  │              │
   │                     │◄─────────────────┼──────────────┤
   │                     │List<Publisher>   │              │
   │                     │                  │              │
   │                     │Store in Cache────►│              │
   │                     │                  │              │
   │◄────────────────────┤200 OK            │              │
   │                     │                  │              │
   │                     │                  │              │
   │GET /api/publishers  │                  │              │
   ├────────────────────►│                  │              │
   │                     │Check Cache       │              │
   │                     ├─────────────────►│              │
   │                     │◄─────────────────┤Cache Hit ✓   │
   │◄────────────────────┤200 OK (from cache)              │
   │                     │                  │              │
```
