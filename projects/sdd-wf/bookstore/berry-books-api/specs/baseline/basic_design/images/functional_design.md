# 画像配信ドメイン - 機能設計書

プロジェクトID: berry-books-api  
ドメイン: images  
バージョン: 1.0.0  
最終更新日: 2026-02-05

---

## 1. 概要

本ドキュメントは、画像配信ドメインの機能を定義する。

* 実装方式: 独自実装（WAR内リソースを直接配信）
* エンティティ実装: なし
* 外部API連携: なし

---

## 2. 機能一覧

### 2.1 画像配信機能

| 機能ID | 機能名 | 説明 |
|--------|--------|------|
| API_004 | 画像API | WAR内リソースを直接配信 |

---

## 3. API詳細設計

### 3.1 画像取得

#### 3.1.1 エンドポイント

* メソッド: GET
* パス: `/api/images/{filename}`
* 認証: 不要

#### 3.1.2 入力

* filename: String（パスパラメータ）

#### 3.1.3 処理フロー

1. WAR内の `/WEB-INF/images/{filename}` から画像ファイルを読み込む
2. ファイルが存在しない場合 → 404 Not Found
3. ファイルの Content-Type を判定（.png → image/png, .jpg → image/jpeg等）
4. ファイルをストリームで返却

#### 3.1.4 出力

* 成功（200 OK）: 画像バイナリ（Content-Type: image/png 等）
* ファイルなし（404 Not Found）: ErrorResponse

---

## 4. 配置リソース

* 画像ファイル配置先: `src/main/webapp/WEB-INF/images/`
* サポートする形式: PNG, JPEG, GIF
* 例:
  * `/WEB-INF/images/book-cover-1.png`
  * `/WEB-INF/images/book-cover-2.jpg`

---

## 5. 参考資料

* [../common/architecture_design.md](../common/architecture_design.md) - アーキテクチャ設計書
* [behaviors.md](behaviors.md) - 画像配信ドメインの振る舞い仕様書（結合テスト用）
