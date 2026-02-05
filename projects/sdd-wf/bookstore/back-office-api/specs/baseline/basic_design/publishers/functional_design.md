# 出版社管理ドメイン - 機能設計書

プロジェクトID: back-office-api  
ドメイン: publishers  
バージョン: 1.0.0  
最終更新日: 2026-02-05

---

## 1. 概要

本ドキュメントは、出版社管理ドメインの機能を定義する。

---

## 2. 機能一覧

### 2.1 出版社管理機能

| 機能ID | 機能名 | 説明 |
|--------|--------|------|
| F-PUBLISHER-001 | 出版社一覧取得 | 出版社の配列形式一覧を取得 |

---

## 3. API詳細設計

### 3.1 F-PUBLISHER-001: 出版社一覧取得

#### 3.1.1 エンドポイント

* メソッド: GET
* パス: `/api/publishers`
* 認証: 必要（JWT）

#### 3.1.2 入力

なし

#### 3.1.3 処理フロー

1. PublisherServiceを呼び出し
2. PublisherDaoで全出版社を取得
3. PublisherエンティティをPublisherTOに変換
4. リスト形式でレスポンス生成

#### 3.1.4 出力

* 成功（200 OK）: List<PublisherTO>
* エラー（500 Internal Server Error）: ErrorResponse

---

## 4. 参考資料

* [../common/architecture_design.md](../common/architecture_design.md) - アーキテクチャ設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書
* [behaviors.md](behaviors.md) - 出版社管理ドメインの振る舞い仕様書（結合テスト用）
