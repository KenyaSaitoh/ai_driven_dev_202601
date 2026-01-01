# 振る舞い仕様書

## 1. 概要

本ドキュメントは、Books Stock API - バックオフィス書籍在庫管理システムの動的な振る舞いをシーケンス図とユースケースで定義する。

## 2. 主要ユースケース

### 2.1 UC-001: ログイン

**アクター**: 社員

**事前条件**: 
- 社員マスタに社員情報が登録されている

**基本フロー**:
1. 社員が社員コードとパスワードを入力
2. システムが社員コードで社員情報を検索
3. システムがパスワードを検証（BCryptまたは平文）
4. システムがJWTトークンを生成
5. システムがHttpOnly Cookieを設定
6. システムが社員情報を返却
7. 社員がログイン成功

**代替フロー**:
- 3a. 社員が見つからない場合
  - システムは401 Unauthorizedを返す
- 3b. パスワードが一致しない場合
  - システムは401 Unauthorizedを返す

**事後条件**:
- 社員がログイン済み状態
- JWTトークンがCookieに保存される

---

### 2.2 UC-002: 書籍検索

**アクター**: 社員

**事前条件**: 
- 社員がログイン済み

**基本フロー**:
1. 社員がカテゴリとキーワードを指定
2. システムが検索条件に基づいて書籍を検索
3. システムが書籍一覧を返却
4. 社員が書籍一覧を確認

**代替フロー**:
- 1a. カテゴリのみ指定
  - システムはカテゴリで絞り込む
- 1b. キーワードのみ指定
  - システムはキーワードで検索
- 1c. 条件未指定
  - システムはすべての書籍を返す

**事後条件**:
- 社員が書籍情報を確認できる

---

### 2.3 UC-003: 在庫更新

**アクター**: 社員

**事前条件**: 
- 社員がログイン済み
- 更新対象の在庫情報が存在する

**基本フロー**:
1. 社員が書籍の在庫情報を表示
2. 社員が在庫数を変更
3. システムが楽観的ロックでバージョンをチェック
4. システムが在庫数を更新
5. システムがバージョンをインクリメント
6. システムが更新後の在庫情報を返却
7. 社員が更新成功を確認

**代替フロー**:
- 3a. バージョンが一致しない場合（他のユーザーが更新済み）
  - システムは409 Conflictを返す
  - 社員が最新情報を再取得して再試行

**事後条件**:
- 在庫数が更新される
- バージョンがインクリメントされる

---

### 2.4 UC-004: ワークフロー作成（新規書籍追加）

**アクター**: 一般社員（ASSOCIATE）

**事前条件**: 
- 社員がログイン済み

**基本フロー**:
1. 社員が「新規書籍追加」ワークフローを選択
2. 社員が書籍情報（書籍名、著者、価格、カテゴリ、出版社）を入力
3. 社員が申請理由を入力
4. 社員が「一時保存」を実行
5. システムがワークフローを作成（状態: CREATED）
6. システムが次のワークフローIDを採番
7. システムがワークフロー履歴を保存
8. システムがワークフロー情報を返却
9. 社員が作成成功を確認

**代替フロー**:
- 4a. 入力内容に不備がある場合
  - システムは400 Bad Requestを返す
  - 社員が入力内容を修正

**事後条件**:
- ワークフローが作成される（状態: CREATED）
- ワークフロー履歴に操作が記録される

---

### 2.5 UC-005: ワークフロー申請

**アクター**: 一般社員（ASSOCIATE）

**事前条件**: 
- 社員がログイン済み
- CREATED状態のワークフローが存在する
- 社員がワークフローの作成者本人

**基本フロー**:
1. 社員が自分が作成したワークフロー一覧を表示
2. 社員が申請するワークフローを選択
3. 社員が「申請」を実行
4. システムがワークフローの状態をチェック（CREATED?）
5. システムが新しい操作履歴を作成（状態: APPLIED、操作: APPLY）
6. システムがワークフロー履歴を保存
7. システムがワークフロー情報を返却
8. 社員が申請成功を確認

**代替フロー**:
- 4a. ワークフローの状態がCREATEDでない場合
  - システムは400 Bad Requestを返す

**事後条件**:
- ワークフローの状態がAPPLIEDに変更される
- 承認者が承認可能になる

---

### 2.6 UC-006: ワークフロー承認

**アクター**: マネージャー（MANAGER）またはディレクター（DIRECTOR）

**事前条件**: 
- 承認者がログイン済み
- APPLIED状態のワークフローが存在する
- 承認者が承認権限を持つ

**基本フロー**:
1. 承認者が承認待ちワークフロー一覧を表示
2. 承認者がワークフローの詳細を確認
3. 承認者が「承認」を実行
4. システムがワークフローの状態をチェック（APPLIED?）
5. システムが承認権限をチェック
   - 職務ランク: MANAGER以上
   - 部署: DIRECTORは全部署、MANAGERは同一部署のみ
6. システムが新しい操作履歴を作成（状態: APPROVED、操作: APPROVE）
7. システムがワークフロー履歴を保存
8. システムが書籍マスタに反映
   - ADD_NEW_BOOK → 書籍と在庫を追加
   - REMOVE_BOOK → 書籍を論理削除
   - ADJUST_BOOK_PRICE → 価格を更新
9. システムがトランザクションをコミット
10. システムがワークフロー情報を返却
11. 承認者が承認成功を確認

**代替フロー**:
- 4a. ワークフローの状態がAPPLIEDでない場合
  - システムは400 Bad Requestを返す
- 5a. 承認権限がない場合（職務ランク不足）
  - システムは403 Forbiddenを返す
- 5b. 承認権限がない場合（部署が異なる）
  - システムは403 Forbiddenを返す
- 8a. 書籍マスタ反映中にエラーが発生
  - システムはトランザクションをロールバック
  - ワークフロー履歴も保存されない

**事後条件**:
- ワークフローの状態がAPPROVEDに変更される
- 書籍マスタが更新される

---

### 2.7 UC-007: ワークフロー却下

**アクター**: マネージャー（MANAGER）またはディレクター（DIRECTOR）

**事前条件**: 
- 承認者がログイン済み
- APPLIED状態のワークフローが存在する
- 承認者が承認権限を持つ

**基本フロー**:
1. 承認者が承認待ちワークフロー一覧を表示
2. 承認者がワークフローの詳細を確認
3. 承認者が却下理由を入力
4. 承認者が「却下」を実行
5. システムがワークフローの状態をチェック（APPLIED?）
6. システムが承認権限をチェック
7. システムが新しい操作履歴を作成（状態: CREATED、操作: REJECT）
8. システムがワークフロー履歴を保存
9. システムがワークフロー情報を返却
10. 承認者が却下成功を確認

**代替フロー**:
- 5a. ワークフローの状態がAPPLIEDでない場合
  - システムは400 Bad Requestを返す
- 6a. 承認権限がない場合
  - システムは403 Forbiddenを返す

**事後条件**:
- ワークフローの状態がCREATEDに戻る
- 作成者が再編集・再申請可能になる

---

## 3. シーケンス図

### 3.1 ログインシーケンス

```
社員           AuthenResource    EmployeeDao     JwtUtil
 │                  │                │              │
 ├─POST /login────►│                │              │
 │  {employeeCode, │                │              │
 │   password}     │                │              │
 │                 ├─findByCode()─►│              │
 │                 │                ├──Query DB───►│
 │                 │◄──Employee────┤              │
 │                 │                │              │
 │                 ├─BCrypt.checkpw()              │
 │                 │  (password validation)        │
 │                 │                │              │
 │                 ├─generateToken()──────────────►│
 │                 │  (employeeId, code, dept)     │
 │                 │◄──JWT Token──────────────────┤
 │                 │                │              │
 │                 ├─NewCookie()                   │
 │                 │  (back-office-jwt)            │
 │                 │                │              │
 │◄─200 OK────────┤                │              │
 │  {LoginResponse}                │              │
 │  Set-Cookie: JWT                │              │
 │                 │                │              │
```

---

### 3.2 書籍検索シーケンス（JPQL）

```
社員        BookResource    BookService    BookDao    Database
 │               │              │             │           │
 ├─GET /search─►│              │             │           │
 │  ?category=1  │              │             │           │
 │  &keyword=xxx │              │             │           │
 │               ├─searchBook()►│             │           │
 │               │  (category,  │             │           │
 │               │   keyword)   ├─find()─────►│           │
 │               │              │  (JPQL)     ├─SELECT───►│
 │               │              │             │  WHERE... │
 │               │              │             │◄─Result───┤
 │               │              │◄─Books─────┤           │
 │               │◄─Books──────┤             │           │
 │               ├─convert()                  │           │
 │               │  (Book → BookTO)           │           │
 │◄─200 OK──────┤              │             │           │
 │  [{BookTO}]   │              │             │           │
 │               │              │             │           │
```

---

### 3.3 在庫更新シーケンス（楽観的ロック）

```
社員        StockResource    StockDao     Database
 │               │              │             │
 ├─PUT /stocks/1►│              │             │
 │  {version: 1, │              │             │
 │   quantity: 15}│             │             │
 │               ├─findById()──►│             │
 │               │              ├─SELECT─────►│
 │               │              │  WHERE id=1 │
 │               │              │◄─Stock─────┤
 │               │◄─Stock──────┤  (version=1)│
 │               │              │             │
 │               ├─version check               │
 │               │  (request.version == stock.version?)│
 │               │                            │
 │               ├─setQuantity(15)            │
 │               │  (JPA auto-detects change) │
 │               │                            │
 │               ├──────Commit Transaction────►│
 │               │              ├─UPDATE─────►│
 │               │              │  SET qty=15 │
 │               │              │  WHERE id=1 │
 │               │              │  AND ver=1  │
 │               │              │◄─Success───┤
 │               │              │  (ver→2)    │
 │◄─200 OK──────┤              │             │
 │  {bookId: 1,  │              │             │
 │   quantity: 15,│             │             │
 │   version: 2} │              │             │
 │               │              │             │
```

**楽観的ロック失敗時**:
```
社員        StockResource    StockDao     Database
 │               │              │             │
 ├─PUT /stocks/1►│              │             │
 │  {version: 1, │              │             │
 │   quantity: 15}│             │             │
 │               ├─findById()──►│             │
 │               │              │◄─Stock─────┤
 │               │◄─Stock──────┤  (version=2)│
 │               │              │  (他user更新済)│
 │               ├─version check               │
 │               │  (1 != 2)    │             │
 │               ├─throw OptimisticLockException│
 │               │              │             │
 │◄─409 Conflict┤              │             │
 │  {error: "在庫が更新されました"}│         │
 │               │              │             │
```

---

### 3.4 ワークフロー承認シーケンス

```
承認者   WorkflowResource   WorkflowService   WorkflowDao   EmployeeDao   BookDao   Database
 │            │                  │                │              │            │           │
 ├─POST /approve─►│                │                │              │            │           │
 │  {operatedBy:2,│                │                │              │            │           │
 │   reason}      ├─approveWorkflow()             │              │            │           │
 │                │  (workflowId)  │                │              │            │           │
 │                │                ├─findLatest()─►│              │            │           │
 │                │                │                ├─SELECT──────────────────────────────►│
 │                │                │◄─Workflow────┤  (state=APPLIED)                       │
 │                │                │                │              │            │           │
 │                │                ├─checkApprovalAuthority()     │            │           │
 │                │                │                │              │            │           │
 │                │                ├─findById()───────────────────►│            │           │
 │                │                │                │              ├─SELECT────────────────►│
 │                │                │◄─Employee────────────────────┤  (approver)            │
 │                │                │                │  (jobRank=2) │            │           │
 │                │                ├─jobRank check                │            │           │
 │                │                │  (MANAGER以上?)               │            │           │
 │                │                ├─department check             │            │           │
 │                │                │  (同一部署?)                   │            │           │
 │                │                │                │              │            │           │
 │                │                ├─copyWorkflowData()           │            │           │
 │                │                ├─setState(APPROVED)           │            │           │
 │                │                ├─setOperationType(APPROVE)    │            │           │
 │                │                │                │              │            │           │
 │                │                ├─insert()──────►│              │            │           │
 │                │                │                ├─INSERT WORKFLOW────────────────────►│
 │                │                │                │  (new operation history)            │
 │                │                │                │              │            │           │
 │                │                ├─applyToBookMaster()          │            │           │
 │                │                │  (ADD_NEW_BOOK)               │            │           │
 │                │                │                │              ├─persist()──►│           │
 │                │                │                │              │  (new Book)├─INSERT──►│
 │                │                │                │              │            │  BOOK+STOCK│
 │                │                │                │              │            │           │
 │                │                ├──────Commit Transaction──────────────────────────────►│
 │                │                │                │              │            │  (ワークフロー│
 │                │                │                │              │            │   +書籍反映)│
 │                │                │                │              │            │           │
 │                │◄─WorkflowTO───┤                │              │            │           │
 │◄─200 OK───────┤                │                │              │            │           │
 │  {workflow}    │                │                │              │            │           │
 │                │                │                │              │            │           │
```

---

### 3.5 ワークフロー状態遷移図

```
         ┌─────────────┐
         │   CREATED   │◄──────┐
         │  (作成済み)   │       │
         └──────┬──────┘       │
                │               │
                │ apply()       │ reject()
                │               │
         ┌──────▼──────┐       │
         │   APPLIED   │───────┘
         │  (申請中)     │
         └──────┬──────┘
                │
                │ approve()
                │
         ┌──────▼──────┐
         │  APPROVED   │
         │  (承認済み)   │
         └─────────────┘
```

**状態遷移の詳細**:
- **CREATE**: CREATED状態のワークフローを新規作成
- **UPDATE**: CREATED状態のワークフローを更新（状態は変わらない）
- **APPLY**: CREATED → APPLIED（申請）
- **APPROVE**: APPLIED → APPROVED（承認、書籍マスタ反映）
- **REJECT**: APPLIED → CREATED（却下、差し戻し）

---

## 4. 状態管理

### 4.1 ワークフローの状態

| 状態 | 説明 | 可能な操作 | 操作者 |
|------|------|-----------|--------|
| CREATED | 作成済み | UPDATE, APPLY | 作成者本人 |
| APPLIED | 申請中 | APPROVE, REJECT | 承認権限のある上司 |
| APPROVED | 承認済み | なし（終了状態） | - |

### 4.2 在庫のバージョン管理

在庫情報は楽観的ロックでバージョン管理:
- 初期バージョン: 0
- 更新ごとに自動インクリメント（JPA `@Version`）
- 更新時にバージョンが一致しない場合は`OptimisticLockException`

---

## 5. 並行制御

### 5.1 在庫更新の並行制御

**シナリオ**: 2人のユーザーが同時に同じ書籍の在庫を更新

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

### 5.2 ワークフロー操作の並行制御

ワークフローの操作は履歴として保存されるため、基本的に競合しない:
- 各操作は新しい行として追加（OPERATION_ID自動採番）
- 最新の状態は最新のOPERATION_IDを持つ行
- 状態チェック時に最新の状態を取得してバリデーション

**シナリオ**: 作成者が更新中に別のユーザーが申請

このシナリオは発生しない（作成者本人のみが操作可能、UI側で制御）

---

## 6. エラーハンドリングの振る舞い

### 6.1 認証エラー

```
社員          AuthenResource
 │                 │
 ├─POST /login────►│
 │  {wrong code}   ├─findByCode()
 │                 │  → null
 │                 ├─generate ErrorResponse
 │◄─401 Unauthorized
 │  {error: "社員コードまたはパスワードが正しくありません"}
 │                 │
```

### 6.2 権限エラー

```
一般社員      WorkflowResource   WorkflowService
 │                 │                  │
 ├─POST /approve──►│                  │
 │                 ├─approveWorkflow()
 │                 │                  ├─checkApprovalAuthority()
 │                 │                  │  jobRank=1 (ASSOCIATE)
 │                 │                  │  → MANAGER未満
 │                 │                  ├─throw UnauthorizedApprovalException
 │                 │◄─Exception──────┤
 │◄─403 Forbidden──┤
 │  {error: "承認権限がありません"}
 │                 │
```

### 6.3 楽観的ロックエラー

```
ユーザー      StockResource
 │                 │
 ├─PUT /stocks/1──►│
 │  {ver:1, qty:15}├─findById()
 │                 │  → Stock (ver=2)
 │                 ├─version check (1 != 2)
 │                 ├─throw OptimisticLockException
 │◄─409 Conflict───┤
 │  {error: "在庫が他のユーザーによって更新されました"}
 │                 │
```

---

## 7. トランザクションの振る舞い

### 7.1 ワークフロー承認のトランザクション

**成功時**:
```
┌─────────────────────────────────────┐
│ Transaction Scope                   │
│                                     │
│  1. INSERT WORKFLOW                 │
│     (operation_id, state=APPROVED)  │
│                                     │
│  2. INSERT/UPDATE BOOK              │
│     (書籍マスタ反映)                  │
│                                     │
│  3. COMMIT                          │
│                                     │
└─────────────────────────────────────┘
```

**失敗時（書籍マスタ反映でエラー）**:
```
┌─────────────────────────────────────┐
│ Transaction Scope                   │
│                                     │
│  1. INSERT WORKFLOW                 │
│     (operation_id, state=APPROVED)  │
│                                     │
│  2. INSERT/UPDATE BOOK              │
│     → Exception発生                 │
│                                     │
│  3. ROLLBACK                        │
│     (ワークフロー履歴も保存されない)   │
│                                     │
└─────────────────────────────────────┘
```

---

## 8. 非機能要件の振る舞い

### 8.1 レスポンスタイム

通常時:
- 単純なGET（一覧取得）: 200ms以下
- 検索（条件あり）: 500ms以下
- 更新系（POST, PUT）: 500ms以下
- ワークフロー承認（書籍マスタ反映あり）: 1秒以下

### 8.2 エラーレスポンス

すべてのエラーは統一的なJSON形式:
```json
{
  "error": "ErrorType",
  "message": "日本語のエラーメッセージ"
}
```

適切なHTTPステータスコードとともに返却される。

---

## 9. ログ出力の振る舞い

### 9.1 ログレベル

- **INFO**: API呼び出しの開始/終了、重要な処理
- **WARN**: 楽観的ロック失敗、認証失敗
- **ERROR**: 予期しないエラー、例外

### 9.2 ログ形式

```
[クラス名#メソッド名] メッセージ: パラメータ
```

例:
```
[ AuthenResource#login ] employeeCode: E0001
[ WorkflowService#approveWorkflow ] workflowId=123
[ StockResource#updateStock ] bookId: 1, version: 1, quantity: 15
```

---

## 10. 監査ログ

### 10.1 ワークフロー操作ログ

すべてのワークフロー操作は履歴として保存:
- 操作ID（OPERATION_ID）
- ワークフローID（WORKFLOW_ID）
- 操作タイプ（OPERATION_TYPE）
- 操作者（OPERATED_BY）
- 操作日時（OPERATED_AT）
- 操作理由（OPERATION_REASON）

これにより、誰がいつ何をしたかを完全に追跡可能。

---

## 11. 将来の拡張

### 11.1 JWT認証フィルタ

将来的に実装予定の認証フィルタ:
```
Client          Filter          Resource
 │                │                │
 ├─GET /books────►│                │
 │  Cookie: JWT   ├─extractJWT()   │
 │                ├─validateJWT()  │
 │                ├─getClaims()    │
 │                │  (employeeId)  │
 │                ├─setContext()   │
 │                │  (SecurityContext)│
 │                ├────────────────►│
 │                │                ├─処理
 │◄───────────────┴────────────────┤
 │                                 │
```

### 11.2 通知機能

ワークフロー操作時の通知:
- 申請時 → 承認者にメール通知
- 承認時 → 申請者にメール通知
- 却下時 → 申請者にメール通知

これらは将来の拡張として検討中。
