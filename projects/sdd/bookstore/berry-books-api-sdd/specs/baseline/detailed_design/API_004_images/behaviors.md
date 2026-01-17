# API_004_images - 画像API受入基準

* API ID: API_004_images  
* API名: 画像API  
* ベースパス:/api/images`  
* バージョン: 2.0.0  
* 最終更新日: 2025-12-27

---

## 1. 概要

本文書は、画像APIの受入基準を記述する。エンドポイントについて、正常系・異常系の振る舞いを定義し、テストシナリオの基礎とする。

---

## 2. 書籍表紙画像取得 (`GE/api/images/covers/{bookId}`)

### 2.1 正常系

| シナリオID | 説明 | Given（前提条件） | When（操作） | Then（期待結果） |
|-----------|------|----------------|------------|---------------|
| IMAGE-001 | 書籍表紙画像を取得できる | 書籍ID=1<br/>画像ファイルが存在する /api/images/covers/1にリクエスト | 200 OK<br/>Content-Type: image/jpeg<br/>画像バイナリが返される |
| IMAGE-002 | 画像が存在しない場合、フォールバック画像を返す | 書籍ID=999<br/>画像ファイルが存在しない /api/images/covers/999にリクエスト | 200 OK<br/>no-image.jpgが返される |

---

## 3. パフォーマンス受入基準

### 3.1 レスポンスタイム

| シナリオID | API | 受入基準 |
|-----------|-----|---------|
| PERF-001 | GE/api/images/covers/{bookId} | 500ms以内（95パーセンタイル） |

---

## 4. 関連ドキュメント

* [functional_design.md](functional_design.md) - 画像API機能設計書
* [../../basic_design/behaviors.md](../../basic_design/behaviors.md) - 全体受入基準
* [../../basic_design/architecture_design.md](../../basic_design/architecture_design.md) - アーキテクチャ設計書
