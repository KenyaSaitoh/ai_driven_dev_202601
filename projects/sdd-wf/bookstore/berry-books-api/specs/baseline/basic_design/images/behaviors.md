# 画像配信ドメイン - 結合テスト仕様書

プロジェクトID: berry-books-api  
ドメイン: images  
バージョン: 1.0.0  
最終更新日: 2026-02-05

---

## 1. 概要

本文書は、画像配信ドメインのService層（静的リソース配信）の結合テスト仕様を記述する。

---

## 2. ImageService のシナリオ

### 2.1 画像配信

#### Feature: 画像ファイルを配信

```gherkin
Feature: 画像配信
  WAR内のリソースから画像ファイルを配信する

  Scenario: 画像ファイルを取得
    Given WAR内に画像ファイルが存在する:
      | filename           | contentType |
      | book-cover-1.png   | image/png   |
    When ImageService.getImage("book-cover-1.png")を呼び出す
    Then 画像バイナリが返される
    And Content-Type="image/png"

  Scenario: 存在しない画像ファイルを取得
    Given WAR内に画像ファイルが存在しない
    When ImageService.getImage("nonexistent.png")を呼び出す
    Then FileNotFoundExceptionがスローされる
```

---

## 3. 参考資料

* [functional_design.md](functional_design.md) - 画像配信機能設計書
* [../common/architecture_design.md](../common/architecture_design.md) - アーキテクチャ設計書
