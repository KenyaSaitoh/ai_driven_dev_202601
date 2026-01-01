# 在庫API 振る舞い仕様書

## 1. 概要

本ドキュメントは、在庫API（`/api/stocks`）の動的な振る舞いをシーケンス図、状態遷移図、フローチャートで定義する。在庫APIの最大の特徴は、**楽観的ロック（Optimistic Locking）**による並行制御である。

## 2. 在庫一覧取得シーケンス

### 2.1 正常系: 全件取得

```
┌──────┐         ┌────────────┐      ┌────────┐      ┌────────┐
│Client│         │StockResource│      │StockDao│      │Database│
└──┬───┘         └──────┬─────┘      └───┬────┘      └───┬────┘
   │                     │                 │               │
   │GET /api/stocks      │                 │               │
   ├────────────────────►│                 │               │
   │                     │                 │               │
   │                     │getAllStocks()   │               │
   │                     ├────────────────►│               │
   │                     │                 │findAll()      │
   │                     │                 ├──────────────►│
   │                     │                 │SELECT *       │
   │                     │                 │FROM STOCK     │
   │                     │                 │◄──────────────┤
   │                     │                 │List<Stock>    │
   │                     │◄────────────────┤               │
   │                     │List<Stock>      │               │
   │                     │                 │               │
   │                     │Convert to DTO   │               │
   │                     │──────────┐      │               │
   │                     │          │      │               │
   │                     │◄─────────┘      │               │
   │                     │                 │               │
   │◄────────────────────┤200 OK           │               │
   │[{StockTO}]          │                 │               │
   │                     │                 │               │
```

## 3. 書籍IDで在庫取得シーケンス

### 3.1 正常系: 在庫が存在する場合

```
┌──────┐         ┌────────────┐      ┌────────┐      ┌────────┐
│Client│         │StockResource│      │StockDao│      │Database│
└──┬───┘         └──────┬─────┘      └───┬────┘      └───┬────┘
   │                     │                 │               │
   │GET /api/stocks/1    │                 │               │
   ├────────────────────►│                 │               │
   │                     │                 │               │
   │                     │Extract PathParam│               │
   │                     │──────────┐      │               │
   │                     │          │      │               │
   │                     │◄─────────┘      │               │
   │                     │bookId = 1       │               │
   │                     │                 │               │
   │                     │getStock(1)      │               │
   │                     ├────────────────►│               │
   │                     │                 │findById(1)    │
   │                     │                 ├──────────────►│
   │                     │                 │EntityManager  │
   │                     │                 │.find(Stock,1) │
   │                     │                 │◄──────────────┤
   │                     │                 │Stock entity   │
   │                     │◄────────────────┤               │
   │                     │Stock entity     │               │
   │                     │                 │               │
   │                     │Check if null    │               │
   │                     │──────────┐      │               │
   │                     │          │      │               │
   │                     │◄─────────┘      │               │
   │                     │not null ✓       │               │
   │                     │                 │               │
   │                     │Convert to DTO   │               │
   │                     │──────────┐      │               │
   │                     │          │      │               │
   │                     │◄─────────┘      │               │
   │                     │                 │               │
   │◄────────────────────┤200 OK           │               │
   │{StockTO}            │                 │               │
   │ bookId: 1           │                 │               │
   │ quantity: 10        │                 │               │
   │ version: 1          │                 │               │
   │                     │                 │               │
```

### 3.2 異常系: 在庫が存在しない場合

```
┌──────┐         ┌────────────┐      ┌────────┐      ┌────────┐
│Client│         │StockResource│      │StockDao│      │Database│
└──┬───┘         └──────┬─────┘      └───┬────┘      └───┬────┘
   │                     │                 │               │
   │GET /api/stocks/9999 │                 │               │
   ├────────────────────►│                 │               │
   │                     │                 │               │
   │                     │getStock(9999)   │               │
   │                     ├────────────────►│               │
   │                     │                 │findById(9999) │
   │                     │                 ├──────────────►│
   │                     │                 │◄──────────────┤
   │                     │                 │null           │
   │                     │◄────────────────┤               │
   │                     │null             │               │
   │                     │                 │               │
   │                     │Check if null    │               │
   │                     │──────────┐      │               │
   │                     │          │      │               │
   │                     │◄─────────┘      │               │
   │                     │null ✗           │               │
   │                     │                 │               │
   │◄────────────────────┤404 Not Found    │               │
   │{error: "在庫が       │                 │               │
   │ 見つかりません"}     │                 │               │
   │                     │                 │               │
```

## 4. 在庫更新シーケンス（成功）

```
Client      StockResource    StockDao    Database
  │              │              │            │
  ├─PUT /stocks/1              │            │
  │  {ver:1,     │              │            │
  │   qty:15}    │              │            │
  │              ├─findById()──►│            │
  │              │              ├─SELECT────►│
  │              │              │  WHERE id=1│
  │              │              │◄─Stock────┤
  │              │◄─Stock──────┤  (ver=1)   │
  │              │              │            │
  │              │version check │            │
  │              │(1 == 1) ✓    │            │
  │              │              │            │
  │              │setQuantity(15)            │
  │              │              │            │
  │              ├────Commit Transaction────►│
  │              │              ├─UPDATE────►│
  │              │              │  SET qty=15│
  │              │              │  ,ver=ver+1│
  │              │              │  WHERE id=1│
  │              │              │  AND ver=1 │
  │              │              │◄─Success──┤
  │              │              │  (ver→2)   │
  │◄─200 OK─────┤              │            │
  │{bookId:1,    │              │            │
  │ qty:15,      │              │            │
  │ ver:2}       │              │            │
```

## 2. 在庫更新シーケンス（楽観的ロック失敗）

```
Client      StockResource    StockDao    Database
  │              │              │            │
  ├─PUT /stocks/1              │            │
  │  {ver:1,     │              │            │
  │   qty:15}    │              │            │
  │              ├─findById()──►│            │
  │              │              │◄─Stock────┤
  │              │◄─Stock──────┤  (ver=2)   │
  │              │              │  ※他user更新済│
  │              │version check │            │
  │              │(1 != 2) ✗    │            │
  │              │              │            │
  │              │throw OptimisticLockException│
  │              │              │            │
  │◄─409 Conflict              │            │
  │{error: "在庫が更新されました"}│            │
```

## 3. 並行更新フローチャート

```
User A                      User B
  │                           │
  ├─GET Stock (ver=1)         │
  │                           │
  │                           ├─GET Stock (ver=1)
  │                           │
  ├─PUT (ver=1, qty=15)       │
  │  → 成功 (ver→2)           │
  │                           │
  │                           ├─PUT (ver=1, qty=20)
  │                           │  → 409 Conflict
  │                           │
  │                           ├─GET Stock (ver=2)
  │                           │
  │                           ├─PUT (ver=2, qty=20)
  │                           │  → 成功 (ver→3)
```

## 4. 状態遷移図

```
         ┌──────────────┐
         │ 在庫情報      │
         │ (version=N)  │
         └──────┬───────┘
                │
                │ PUT /stocks/{id}
                │ {version: N, ...}
                │
         ┌──────▼───────┐
         │ バージョン    │
         │ チェック      │
         └──┬───────┬───┘
            │       │
       一致  │       │ 不一致
            │       │
         ┌──▼────┐ │
         │UPDATE  │ │
         │実行    │ │
         │ver=N+1 │ │
         └──┬────┘ │
            │      │
         ┌──▼────┐ ┌▼─────────┐
         │200 OK │ │409       │
         │(ver=N+1) │Conflict  │
         └───────┘ └──────────┘
```

## 5. 楽観的ロックのメリット

- **パフォーマンス**: 悲観的ロックより高速（ロック待ちなし）
- **デッドロック回避**: 複数リソースのロック順序問題なし
- **スケーラビリティ**: 大量の同時アクセスに対応

## 6. クライアント実装例

```javascript
async function updateStock(bookId, newQuantity) {
    // 1. 最新の在庫情報を取得
    const stock = await fetch(`/api/stocks/${bookId}`).then(r => r.json());
    
    // 2. 在庫更新をリクエスト
    const response = await fetch(`/api/stocks/${bookId}`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            version: stock.version,  // ← 重要
            quantity: newQuantity
        })
    });
    
    // 3. 409 Conflictの場合は再試行
    if (response.status === 409) {
        alert('他のユーザーが更新しました。再度お試しください。');
        return updateStock(bookId, newQuantity); // 再帰的に再試行
    }
    
    return response.json();
}
```

