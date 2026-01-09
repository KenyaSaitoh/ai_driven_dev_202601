# 認証API タスク

**担当者:** 担当者A（1名）  
**推奨スキル:** JAX-RS、JWT、BCrypt、MicroProfile Config  
**想定工数:** 4時間  
**依存タスク:** [common_tasks.md](common_tasks.md)

---

## タスクリスト

### T_API001_001: [P] LoginRequestの作成

- **目的**: ログインリクエスト用のDTOクラスを作成する
- **対象**: LoginRequest.java（DTOクラス）
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「4.1 LoginRequest」
- **注意事項**: 
  - employeeCodeフィールド（NotBlank, Size(max=20)）を含める
  - passwordフィールド（NotBlank, Size(max=100)）を含める
  - レコード型（immutable）で実装する

---

### T_API001_002: [P] LoginResponseの作成

- **目的**: ログインレスポンス用のDTOクラスを作成する
- **対象**: LoginResponse.java（DTOクラス）
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「4.2 LoginResponse」
- **注意事項**: 
  - employeeId、employeeCode、employeeName、email、jobRank、departmentId、departmentNameを含める
  - レコード型（immutable）で実装する

---

### T_API001_003: [P] AuthenResourceの作成

- **目的**: 認証APIのエンドポイントを実装するResourceクラスを作成する
- **対象**: AuthenResource.java（JAX-RS Resourceクラス）
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「2. エンドポイント一覧」
  - [functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「3. エンドポイント詳細」
- **注意事項**: 
  - `@Path("/auth")`でベースパスを設定する
  - login()、logout()、getCurrentUser()メソッドを実装する
  - getCurrentUser()は501 Not Implementedを返す（将来実装予定）

---

### T_API001_004: login()メソッドの実装

- **目的**: ログイン機能を実装する
- **対象**: AuthenResource#login()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「3.1 ログイン」
- **注意事項**: 
  - EmployeeDaoで社員コードから社員情報を取得する
  - BCrypt.checkpw()でパスワードを照合する（BCryptハッシュの場合）
  - 平文パスワードの場合は文字列比較する（開発環境のみ）
  - JwtUtilでJWTトークンを生成する
  - HttpOnly Cookieを生成してSet-Cookieヘッダーに設定する
  - 認証失敗時は401 Unauthorizedを返す
  - ログ出力（INFO: 成功、WARN: 失敗）を行う

---

### T_API001_005: logout()メソッドの実装

- **目的**: ログアウト機能を実装する
- **対象**: AuthenResource#logout()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「3.2 ログアウト」
- **注意事項**: 
  - HttpOnly CookieをMaxAge=0で削除する
  - 空のレスポンスを返す
  - ログ出力（INFO）を行う

---

### T_API001_006: getCurrentUser()メソッドの実装（未実装）

- **目的**: 現在のユーザー情報取得機能のスタブを実装する
- **対象**: AuthenResource#getCurrentUser()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「3.3 現在のユーザー情報取得」
- **注意事項**: 
  - 501 Not Implementedを返す
  - 将来的にJWT認証フィルタ実装後に有効化する予定

---

### T_API001_007: [P] 認証API単体テストの作成

- **目的**: 認証APIの単体テストを作成する
- **対象**: AuthenResourceTest.java（テストクラス）
- **参照SPEC**: 
  - [functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「8. テスト仕様」
  - [functional_design.md](../specs/baseline/api/API_001_auth/functional_design.md) の「10. 動的振る舞い」
- **注意事項**: 
  - 正常系: 正しい社員コードとパスワードでログイン成功
  - 異常系: 存在しない社員コード → 401 Unauthorized
  - 異常系: 間違ったパスワード → 401 Unauthorized
  - 異常系: 社員コードが空 → 400 Bad Request
  - 異常系: パスワードが空 → 400 Bad Request
  - ログアウト: Cookie削除を確認
  - 未実装機能: getCurrentUser() → 501 Not Implemented

---

## API実装完了チェックリスト

- [ ] LoginRequestが作成された
- [ ] LoginResponseが作成された
- [ ] AuthenResourceが作成された
- [ ] login()メソッドが実装された
  - [ ] 社員コードで社員情報を取得
  - [ ] BCryptパスワード照合が実装された
  - [ ] 平文パスワード照合が実装された（開発環境用）
  - [ ] JWTトークン生成が実装された
  - [ ] HttpOnly Cookie設定が実装された
  - [ ] 認証失敗時の401エラーハンドリング
  - [ ] ログ出力が実装された
- [ ] logout()メソッドが実装された
  - [ ] Cookie削除が実装された
  - [ ] ログ出力が実装された
- [ ] getCurrentUser()メソッドが実装された（501 Not Implemented）
- [ ] 認証API単体テストが作成された
  - [ ] 正常系テストが実装された
  - [ ] 異常系テストが実装された

---

## 次のステップ

認証APIが完了したら、他のAPI実装タスクと並行して進めることができます：
- [書籍API](API_002_books.md)
- [カテゴリAPI](API_003_categories.md)
- [出版社API](API_004_publishers.md)
- [在庫API](API_005_stocks.md)
- [ワークフローAPI](API_006_workflows.md)

すべてのAPI実装が完了したら、[結合テスト](integration_tasks.md)に進んでください。
