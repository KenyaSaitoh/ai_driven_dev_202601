# 在庫API 機能設計書

## 1. API概要

- **API名**: 在庫API（Stocks API）
- **ベースパス**: `/api/stocks`
- **目的**: 書籍在庫情報の参照・更新機能を提供（楽観的ロック対応）

## 2. エンドポイント一覧

| メソッド | パス | 機能 |
|---------|------|------|
| GET | `/api/stocks` | 在庫一覧取得 |
| GET | `/api/stocks/{bookId}` | 在庫情報取得 |
| PUT | `/api/stocks/{bookId}` | 在庫更新（楽観的ロック） |

## 3. エンドポイント詳細

### 3.1 在庫一覧取得

- **エンドポイント**: `GET /api/stocks`
- **認証**: 不要（将来:必要）

**レスポンス（200 OK）**:
```json
[
  {
    "bookId": 1,
    "quantity": 10,
    "version": 1
  },
  {
    "bookId": 2,
    "quantity": 5,
    "version": 2
  }
]
```

---

### 3.2 在庫情報取得

- **エンドポイント**: `GET /api/stocks/{bookId}`
- **認証**: 不要（将来:必要）

**レスポンス（200 OK）**:
```json
{
  "bookId": 1,
  "quantity": 10,
  "version": 1
}
```

**レスポンス（404 Not Found）**:
```json
{
  "error": "在庫情報が見つかりません"
}
```

---

### 3.3 在庫更新（楽観的ロック）

- **エンドポイント**: `PUT /api/stocks/{bookId}`
- **認証**: 不要（将来:必要）

**リクエスト**:
```json
{
  "version": 1,
  "quantity": 15
}
```

**処理フロー**:
1. パスパラメータから書籍IDを取得
2. StockDaoで在庫情報を取得
3. 在庫が存在しない → 404 Not Found
4. **バージョンチェック**: リクエストのversionとDBのversionを比較
5. バージョンが一致しない → 409 Conflict（楽観的ロック失敗）
6. 在庫数を更新（`stock.setQuantity()`）
7. JPA `@Version`により自動的にversionがインクリメント
8. トランザクションコミット時にUPDATE実行

**レスポンス（200 OK）**:
```json
{
  "bookId": 1,
  "quantity": 15,
  "version": 2
}
```

**レスポンス（409 Conflict）**:
```json
{
  "error": "在庫が他のユーザーによって更新されました。再度お試しください。"
}
```

## 4. 楽観的ロックの仕組み

### 4.1 JPAのバージョン管理

```java
@Entity
@Table(name = "STOCK")
public class Stock {
    @Id
    @Column(name = "BOOK_ID")
    private Integer bookId;
    
    @Column(name = "QUANTITY")
    private Integer quantity;
    
    @Version  // ← 楽観的ロック
    @Column(name = "VERSION")
    private Long version;
}
```

### 4.2 UPDATE時のバージョンチェック

実行されるSQL:
```sql
UPDATE STOCK 
SET QUANTITY = ?, 
    VERSION = VERSION + 1 
WHERE BOOK_ID = ? 
  AND VERSION = ?
```

- `VERSION = ?`の条件により、バージョンが一致しない場合はUPDATE件数が0になる
- JPAは`OptimisticLockException`をスロー

### 4.3 並行更新の例

```
時刻   ユーザーA                           ユーザーB
t1    在庫取得 (id=1, qty=10, ver=1)
t2                                       在庫取得 (id=1, qty=10, ver=1)
t3    数量変更 (qty=15)
t4    PUT /stocks/1 {ver:1, qty:15}
t5    → 成功 (ver=2に更新)
t6                                       数量変更 (qty=20)
t7                                       PUT /stocks/1 {ver:1, qty:20}
t8                                       → 409 Conflict (ver不一致)
t9                                       最新情報を再取得 (ver=2)
t10                                      PUT /stocks/1 {ver:2, qty:20}
t11                                      → 成功 (ver=3に更新)
```

## 5. ビジネスルール

| ルールID | ルール内容 |
|---------|-----------|
| BR-STOCK-001 | 在庫更新時はバージョン番号を必ず指定 |
| BR-STOCK-002 | バージョンが一致しない場合は409 Conflict |
| BR-STOCK-003 | クライアント側で再取得して再試行 |
| BR-STOCK-004 | 在庫数は0以上の整数 |
| BR-STOCK-005 | 負の値は許可しない |

## 6. DTO

```java
public class StockTO {
    private Integer bookId;
    private Integer quantity;
    private Long version;
}

public class StockUpdateRequest {
    private Long version;      // 必須: 楽観的ロック用
    private Integer quantity;  // 必須: 新しい在庫数
}
```

## 7. エラーハンドリング

| エラー | HTTPステータス | メッセージ |
|-------|---------------|-----------|
| 在庫が見つからない | 404 Not Found | 在庫情報が見つかりません |
| バージョン不一致 | 409 Conflict | 在庫が他のユーザーによって更新されました |
| 予期しないエラー | 500 Internal Server Error | 在庫更新中にエラーが発生しました |

## 8. 関連コンポーネント

- `StockResource#updateStock()`
- `StockDao#findById()`
- JPA `@Version`
- `OptimisticLockExceptionMapper`

## 9. テスト仕様

### 9.1 正常系

| テストケース | 期待結果 |
|------------|---------|
| 在庫更新（正しいバージョン） | 200 OK + versionインクリメント |

### 9.2 異常系

| テストケース | 期待結果 |
|------------|---------|
| 在庫更新（古いバージョン） | 409 Conflict |
| 存在しない書籍ID | 404 Not Found |

## 10. パフォーマンス要件

- **在庫更新レスポンスタイム**: 200ms以内
- **楽観的ロック失敗時の再試行**: クライアント側で実装

