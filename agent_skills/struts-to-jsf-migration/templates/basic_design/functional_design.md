# [SCREEN_GROUP_NAME] - 機能設計書（画面グループ）

画面グループ名: [SCREEN_GROUP_NAME]  
バージョン: 1.0.0  
最終更新日: [DATE]  
ステータス: [STATUS]

---

## 1. 概要

本文書は、[SCREEN_GROUP_NAME]画面グループの機能設計を記述する。画面一覧、画面遷移、画面の役割と機能を論理レベルで定義する。

* 実装クラス設計（Managed Bean、Service、Dao等）、メソッドシグネチャ、アノテーションは詳細設計（detailed_design/FUNC_XXX/detailed_design.md）で記述する
* 共通機能（認証、セキュリティ、共通Service等）はcommonドメインを参照すること
* JSFは画面中心のサーバーサイドMVCフレームワーク
* 画面グループ: 関連する画面群（一覧、入力、確認等）をまとめたもの

---

## 2. 画面一覧

### 2.1 画面リスト

* SCREEN_001_[ScreenName1]: [画面名1]
  * URL: /[path].xhtml
  * 目的: [画面の目的を1-2行で記述]
  * 主要機能: [主要な機能を列挙]

* SCREEN_002_[ScreenName2]: [画面名2]
  * URL: /[path].xhtml
  * 目的: [画面の目的を1-2行で記述]
  * 主要機能: [主要な機能を列挙]

* SCREEN_003_[ScreenName3]: [画面名3]
  * URL: /[path].xhtml
  * 目的: [画面の目的を1-2行で記述]
  * 主要機能: [主要な機能を列挙]

---

## 3. 画面遷移図

### 3.1 全体画面遷移

```mermaid
graph TD
    Start([システム起動])
    Screen1[SCREEN_001_[ScreenName1]<br/>[画面名1]<br/>[path1].xhtml]
    Screen2[SCREEN_002_[ScreenName2]<br/>[画面名2]<br/>[path2].xhtml]
    Screen3[SCREEN_003_[ScreenName3]<br/>[画面名3]<br/>[path3].xhtml]
    
    Start -->|初回アクセス| Screen1
    
    Screen1 -->|新規追加ボタン| Screen2
    Screen1 -->|編集ボタン<br/>id指定| Screen2
    Screen1 -->|削除ボタン<br/>id指定| Screen1
    
    Screen2 -->|確認画面へボタン| Screen3
    Screen2 -->|キャンセルボタン| Screen1
    
    Screen3 -->|登録ボタン| Screen1
    Screen3 -->|戻るボタン| Screen2
    
    style Screen1 fill:#e1f5ff
    style Screen2 fill:#fff4e1
    style Screen3 fill:#e8f5e9
```

### 3.2 画面遷移詳細

* [画面名1]（[path1].xhtml）
  * → [画面名2]（[path2].xhtml）: [遷移トリガー]
  * → [画面名2]（[path2].xhtml?id=xxx）: [遷移トリガー]
  * → [画面名1]（[path1].xhtml）: [遷移トリガー]

* [画面名2]（[path2].xhtml）
  * → [画面名3]（[path3].xhtml）: [遷移トリガー]
  * → [画面名1]（[path1].xhtml）: [遷移トリガー]

* [画面名3]（[path3].xhtml）
  * → [画面名1]（[path1].xhtml）: [遷移トリガー]
  * → [画面名2]（[path2].xhtml）: [遷移トリガー]

### 3.3 画面遷移のパターン

* 通常遷移（Forward）
  * [遷移元] → [遷移先]
  * データはFlash ScopeまたはView Scopeで引き継ぐ

* リダイレクト遷移（Redirect）
  * [遷移元] → [遷移先]（登録・更新後）
  * 戻り値に "?faces-redirect=true" を付加

* ブラウザ履歴を使用した戻る
  * [遷移元] → [遷移先]（戻るボタン）
  * JavaScript: history.back()

---

## 4. 画面機能詳細

注意: 以下は論理レベルの記述です。実装クラス（Managed Bean、Service、Dao）の詳細は詳細設計書（detailed_design/FUNC_XXX/detailed_design.md）を参照してください。

### 4.1 SCREEN_001_[ScreenName1]（[画面名1]）

#### 4.1.1 画面の役割

[画面の役割を1-2行で記述]

#### 4.1.2 画面初期表示

1. [初期表示の処理フローをステップで記述]
2. [データ取得方法を記述]
3. [画面表示内容を記述]

#### 4.1.3 画面機能

##### 4.1.3.1 [機能1]

* トリガー: [ボタンクリック等]
* 処理内容:
  1. [処理ステップ1]
  2. [処理ステップ2]
  3. [処理ステップ3]
* 画面遷移: [遷移先]
* データ受け渡し: [受け渡し方法]

##### 4.1.3.2 [機能2]

[同様の形式で記述]

#### 4.1.4 ビジネスルール

* [ビジネスルール1]
* [ビジネスルール2]

#### 4.1.5 エラーハンドリング

* [エラーケース1]: [エラーメッセージ、画面動作]
* [エラーケース2]: [エラーメッセージ、画面動作]

---

### 4.2 SCREEN_002_[ScreenName2]（[画面名2]）

#### 4.2.1 画面の役割

[画面の役割を1-2行で記述]

#### 4.2.2 画面初期表示

[同様の形式で記述]

#### 4.2.3 画面機能

[同様の形式で記述]

#### 4.2.4 ビジネスルール

[同様の形式で記述]

#### 4.2.5 エラーハンドリング

[同様の形式で記述]

---

### 4.3 SCREEN_003_[ScreenName3]（[画面名3]）

[同様の形式で記述]

---

## 5. Managed Bean設計（論理レベル）

注意: 実装クラス名、メソッドシグネチャ、アノテーションは詳細設計書で記述します。

### 5.1 [ScreenName1]Bean

* 責務: [画面名1]の画面状態管理とアクション処理
* スコープ: ViewScoped（推奨）
* 主要プロパティ:
  * [プロパティ名]: [説明]
* 主要アクションメソッド:
  * [メソッド名](): [目的]

### 5.2 [ScreenName2]Bean

[同様の形式で記述]

### 5.3 [ScreenName3]Bean

[同様の形式で記述]

---

## 6. ビジネスロジック設計（論理レベル）

注意: 実装クラス名、メソッドシグネチャは詳細設計書で記述します。

### 6.1 [EntityName]Service

* 責務: [エンティティ]のビジネスロジック
* 主要機能:
  * [機能1]: [説明]
  * [機能2]: [説明]

### 6.2 トランザクション管理

* トランザクション境界: Service層
* 例外時の動作: 自動ロールバック

---

## 7. データモデル

注意: テーブル定義の詳細はcommon/data_model.mdを参照してください。JPAエンティティクラス設計は詳細設計で記述します。

### 7.1 使用するエンティティ

* [ENTITY_NAME]: [テーブルの説明]
  * 主要フィールド: [フィールド1], [フィールド2]

---

## 8. セキュリティ要件

### 8.1 認証・認可

* 認証要件: [要/不要]
* 認可要件: [ロール、権限]
* 保護が必要な画面: [該当する画面一覧]

### 8.2 入力検証

* バリデーション: [必須項目、形式、長さ等]
* サニタイゼーション: [XSS対策、SQLインジェクション対策等]

---

## 9. 非機能要件

### 9.1 パフォーマンス

* 応答時間: [目標値]
* 大量データ対策: [ページネーション、lazy loading等]

### 9.2 ユーザビリティ

* エラーメッセージ: [明確で分かりやすいメッセージ]
* 操作性: [ボタン配置、入力補助等]

---

## 10. 参考資料

* [screen_design.md](screen_design.md) - 画面設計書（レイアウト、入力項目詳細）
* [behaviors.md](behaviors.md) - 振る舞い仕様書（E2Eテスト用）
* [../common/functional_design.md](../common/functional_design.md) - 共通機能設計書
* [../common/data_model.md](../common/data_model.md) - データモデル仕様書
* [../common/architecture_design.md](../common/architecture_design.md) - アーキテクチャ設計書
* [../../detailed_design/FUNC_XXX/detailed_design.md](../../detailed_design/FUNC_XXX/detailed_design.md) - 詳細設計書
