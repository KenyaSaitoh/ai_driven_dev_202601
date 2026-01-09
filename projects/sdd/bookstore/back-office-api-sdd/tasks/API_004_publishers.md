# 出版社API タスク

**担当者:** 担当者D（1名）  
**推奨スキル:** JAX-RS、JPA  
**想定工数:** 2時間  
**依存タスク:** [common_tasks.md](common_tasks.md)

---

## タスクリスト

### T_API004_001: [P] PublisherTOの作成

- **目的**: 出版社情報のデータ転送オブジェクトを作成する
- **対象**: PublisherTO.java（DTOクラス）
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_004_publishers/functional_design.md) の「4. データ転送オブジェクト（DTO）」
- **注意事項**: 
  - publisherId、publisherNameを含める
  - レコード型（immutable）で実装する

---

### T_API004_002: [P] PublisherServiceの作成

- **目的**: 出版社管理のビジネスロジッククラスを作成する
- **対象**: PublisherService.java（Serviceクラス）
- **参照SPEC**: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.2 Business Logic Layer」
- **注意事項**: 
  - `@ApplicationScoped`でCDI管理する
  - getPublishersAll()メソッドを実装する
  - PublisherDaoを使用する

---

### T_API004_003: [P] PublisherResourceの作成

- **目的**: 出版社APIのエンドポイントを実装するResourceクラスを作成する
- **対象**: PublisherResource.java（JAX-RS Resourceクラス）
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_004_publishers/functional_design.md) の「2. エンドポイント一覧」
- **注意事項**: 
  - `@Path("/publishers")`でベースパスを設定する
  - getAllPublishers()メソッドを実装する

---

### T_API004_004: getAllPublishers()メソッドの実装

- **目的**: 出版社一覧取得機能を実装する
- **対象**: PublisherResource#getAllPublishers()
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_004_publishers/functional_design.md) の「3.1 出版社一覧取得」
- **注意事項**: 
  - PublisherServiceのgetPublishersAll()を呼び出す
  - PublisherエンティティをPublisherTOに変換する
  - 配列形式でレスポンスを返す
  - ログ出力（INFO）を行う

---

### T_API004_005: [P] 出版社API単体テストの作成

- **目的**: 出版社APIの単体テストを作成する
- **対象**: PublisherResourceTest.java（テストクラス）
- **参照SPEC**: [functional_design.md](../specs/baseline/api/API_004_publishers/functional_design.md) の「6. テスト仕様」
- **注意事項**: 
  - 正常系: 出版社一覧取得 → 200 OK + 出版社配列

---

## API実装完了チェックリスト

- [ ] PublisherTOが作成された
- [ ] PublisherServiceが作成された
  - [ ] getPublishersAll()が実装された
- [ ] PublisherResourceが作成された
- [ ] getAllPublishers()メソッドが実装された
  - [ ] 配列形式のレスポンス
  - [ ] ログ出力が実装された
- [ ] 出版社API単体テストが作成された
  - [ ] 正常系テストが実装された

---

## 次のステップ

出版社APIが完了したら、他のAPI実装タスクと並行して進めることができます：
- [認証API](API_001_auth.md)
- [書籍API](API_002_books.md)
- [カテゴリAPI](API_003_categories.md)
- [在庫API](API_005_stocks.md)
- [ワークフローAPI](API_006_workflows.md)

すべてのAPI実装が完了したら、[結合テスト](integration_tasks.md)に進んでください。
