# ユースケース: カテゴリ - 振る舞い仕様書

ユースケースID: category  
バージョン: 1.0.0  
最終更新日: 2026-01-31

記法: Gherkin。

---

## 1. 概要

カテゴリ一覧取得の振る舞い。

---

## 2. テストシナリオ（Gherkin）

### Feature: カテゴリ一覧

```gherkin
Scenario: カテゴリ一覧を取得する
  Given DBに以下のカテゴリが存在する:
    テーブル: CATEGORY
    件数: 4件
    データセット: /datasets/category/initial-categories.xml
    データ:
      | CATEGORY_ID | CATEGORY_NAME    | DESCRIPTION          |
      | 1           | プログラミング   | プログラミング関連書籍 |
      | 2           | 文学             | 文学作品             |
      | 3           | ビジネス         | ビジネス書籍         |
      | 4           | 自己啓発         | 自己啓発書籍         |
  
  When GET /api/categories を送る
  
  Then レスポンスは 200 OK
  And カテゴリ一覧が返る:
    件数: 4件
    データ:
      | categoryId | categoryName     |
      | 1          | プログラミング   |
      | 2          | 文学             |
      | 3          | ビジネス         |
      | 4          | 自己啓発         |
  
  And DBの状態は変化しない:
    テーブル: CATEGORY
    件数: 4件（変更なし）
    検証:
      - READ操作のため、DBは更新されない
```

---

## 3. DBUnitデータセット対応表

| シナリオ | 初期データセット | 期待データセット | 検証テーブル |
|---------|----------------|----------------|------------|
| カテゴリ一覧を取得する | `/datasets/category/initial-categories.xml` | （変更なし） | CATEGORY |

---

## 4. 参照

* [userstory.md](userstory.md)
* [../../common/data_model.md](../../common/data_model.md)
* DBUnit公式ドキュメント: http://dbunit.sourceforge.net/
