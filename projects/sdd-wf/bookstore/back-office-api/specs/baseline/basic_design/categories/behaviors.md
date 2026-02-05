# カテゴリ管理ドメイン - 結合テスト仕様書

プロジェクトID: back-office-api  
ドメイン: categories  
バージョン: 1.0.0  
最終更新日: 2026-02-05

---

## 1. 概要

本文書は、カテゴリ管理ドメインのService層以下（Service + DAO + Entity + DB）の結合テスト仕様を記述する。

---

## 2. CategoryService のシナリオ

### 2.1 カテゴリ一覧取得

#### Feature: カテゴリ一覧を取得

```gherkin
Feature: カテゴリ一覧取得
  すべてのカテゴリを取得する

  Scenario: カテゴリ一覧を取得
    Given DBに以下のカテゴリが存在する:
      | categoryId | categoryName      |
      | 1          | プログラミング      |
      | 2          | 文学              |
    When CategoryService.getAllCategories()を呼び出す
    Then すべてのカテゴリが返される
```

---

## 3. 参考資料

* [functional_design.md](functional_design.md) - カテゴリ管理機能設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書
