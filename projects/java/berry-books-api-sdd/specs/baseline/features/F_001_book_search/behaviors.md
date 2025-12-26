# F-001: 書籍検索・閲覧 - 振る舞い仕様書

**機能ID:** F-001  
**機能名:** 書籍検索・閲覧  
**バージョン:** 1.0.0  
**最終更新日:** 2025-12-16

---

## 概要

このドキュメントは、書籍検索・閲覧機能の外形的な振る舞いを定義する。
各シナリオはGiven-When-Then形式で記述され、ブラックボックステストの基礎となる。

**関連ドキュメント:**
- [functional_design.md](functional_design.md) - 機能設計
- [screen_design.md](screen_design.md) - 画面設計
- [../../system/requirements.md](../../system/requirements.md) - システム要件定義書

---

## 画面マッピング

| 画面ID | 画面名 | ファイル名 | 認証要否 |
|--------|--------|-----------|---------|
| SC-004 | 書籍検索画面 | bookSearch.xhtml | 必要 |
| SC-005 | 検索結果画面 | bookSelect.xhtml | 必要 |

---

## シナリオ一覧

### Scenario 1: カテゴリで書籍を絞り込む

```gherkin
Given ユーザーが書籍検索画面（bookSearch.xhtml）にアクセスしている
When ユーザーがカテゴリドロップダウンから「Java」を選択して検索する
Then 検索結果画面（bookSelect.xhtml）に遷移する
And Java カテゴリの書籍一覧がh:dataTableで表示される
And 各書籍には以下の情報が表示される
  | フィールド | データソース |
  | 書籍ID | Book.bookId |
  | 書籍名 | Book.bookName |
  | 著者 | Book.author |
  | カテゴリ | Category.categoryName |
  | 出版社 | Publisher.publisherName |
  | 価格 | Book.price |
  | 在庫数 | Stock.quantity |
And 検索結果は書籍ID昇順（BR-003）でソートされる
```

**受入基準:**
- [ ] カテゴリドロップダウンに全カテゴリが表示される
- [ ] 選択されたカテゴリの書籍のみが表示される
- [ ] 書籍リストが空の場合、「該当する書籍がありません」メッセージが表示される

---

### Scenario 2: キーワードで書籍を検索する

```gherkin
Given ユーザーが書籍検索画面（bookSearch.xhtml）にアクセスしている
When ユーザーがキーワードフィールドに「SpringBoot」を入力して検索する
Then 検索結果画面（bookSelect.xhtml）に遷移する
And 書籍名（Book.bookName）または著者（Book.author）に「SpringBoot」を含む書籍一覧が表示される
And 検索は大文字小文字を区別しない（LIKE検索）
```

**受入基準:**
- [ ] 部分一致検索が機能する（例: "SpringBoot" → "SpringBoot in Cloud", "SpringBootによるエンタープライズ開発"）
- [ ] 書籍名と著者名の両方が検索対象となる（BR-002）
- [ ] カテゴリが未選択の場合、全カテゴリが対象となる（BR-001）

---

### Scenario 3: カテゴリとキーワードを組み合わせて検索する

```gherkin
Given ユーザーが書籍検索画面（bookSearch.xhtml）にアクセスしている
When ユーザーがカテゴリ「SpringBoot」とキーワード「Cloud」を入力して検索する
Then SpringBoot カテゴリかつ「Cloud」を含む書籍のみが表示される
And 検索結果はBookDao.findByCategoryAndKeyword()で取得される
```

**受入基準:**
- [ ] カテゴリとキーワードの両方の条件がAND条件で適用される
- [ ] WHERE句が正しく構築される: `WHERE category_id = ? AND (book_name LIKE ? OR author LIKE ?)`

---

### Scenario 4: 在庫なしの書籍も表示される

```gherkin
Given ユーザーが検索結果画面（bookSelect.xhtml）を閲覧している
When 在庫数が0の書籍（Stock.quantity = 0）が存在する
Then その書籍も検索結果に表示される（BR-004）
And 在庫数カラムに「0」が表示される
But 「カートへ」ボタンが無効化される（disabled属性）
And ボタンのスタイルがグレーアウトされる
```

**受入基準:**
- [ ] 在庫0の書籍が検索結果から除外されない
- [ ] 在庫0の書籍の「カートへ」ボタンがクリック不可
- [ ] ユーザーが在庫状況を視覚的に判断できる

---

## ビジネスルール参照

| ルールID | 説明 | 影響するシナリオ |
|---------|-------------|-----------------|
| BR-001 | カテゴリ未選択の場合、全カテゴリが検索対象 | Scenario 2 |
| BR-002 | キーワード未入力の場合、書籍名と著者の両方を検索 | Scenario 2 |
| BR-003 | 検索結果は書籍ID昇順でソート | 全シナリオ |
| BR-004 | 在庫0の書籍も表示（購入不可） | Scenario 4 |

---

## テスト実行チェックリスト

- [ ] Scenario 1: カテゴリで書籍を絞り込む
- [ ] Scenario 2: キーワードで書籍を検索する
- [ ] Scenario 3: カテゴリとキーワードを組み合わせて検索する
- [ ] Scenario 4: 在庫なしの書籍も表示される

