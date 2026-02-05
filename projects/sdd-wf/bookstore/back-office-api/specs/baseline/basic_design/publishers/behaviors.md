# 出版社管理ドメイン - 結合テスト仕様書

プロジェクトID: back-office-api  
ドメイン: publishers  
バージョン: 1.0.0  
最終更新日: 2026-02-05

---

## 1. 概要

本文書は、出版社管理ドメインのService層以下（Service + DAO + Entity + DB）の結合テスト仕様を記述する。

---

## 2. PublisherService のシナリオ

### 2.1 出版社一覧取得

#### Feature: 出版社一覧を取得

```gherkin
Feature: 出版社一覧取得
  すべての出版社を取得する

  Scenario: 出版社一覧を取得
    Given DBに以下の出版社が存在する:
      | publisherId | publisherName |
      | 1           | 技術評論社     |
      | 2           | オライリー     |
    When PublisherService.getAllPublishers()を呼び出す
    Then すべての出版社が返される
```

---

## 3. 参考資料

* [functional_design.md](functional_design.md) - 出版社管理機能設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書
