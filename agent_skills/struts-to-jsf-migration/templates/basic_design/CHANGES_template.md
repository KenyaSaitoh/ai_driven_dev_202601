# 基本設計変更記録

このファイルは、基本設計SPEC（functional_design.md、screen_design.md、data_model.md等）の変更内容を記録するためのものです。

## 使い方

1. 基本設計SPECのマスターファイル（functional_design.md等）を自由に編集
2. このファイルをコピーして `CHANGES.md` として保存
3. 変更内容を以下のテンプレートに従って記載
4. `basic_design_change.md` を実行

---

## [YYYY-MM-DD] 変更タイトル

### 変更対象
- functional_design.md (または .xlsx)
- screen_design.md (または .xlsx)
- data_model.md (または .xlsx)
- behaviors.md (または .xlsx)

### 変更内容

#### functional_design.md の変更

##### セクション「画面一覧」
**追加**:
- SCREEN_003_PersonEdit に「削除」ボタン追加
  - 機能: 人物情報を削除する
  - 確認ダイアログ表示後、削除を実行

**変更**:
- SCREEN_002_PersonDetail の表示項目を追加（住所、電話番号）

**削除**:
- （該当なし）

##### セクション「Managed Bean設計」
**追加**:
- PersonEditBean に deletePerson() アクションメソッド追加
  - 引数: なし
  - 戻り値: String (画面遷移先)
  - 処理: 確認後、PersonService.delete() を呼び出し、一覧画面に遷移

#### screen_design.md の変更

##### 画面「SCREEN_003_PersonEdit」
**追加**:
- フォーム下部に「削除」ボタンを追加
  - ラベル: "削除"
  - スタイル: btn-danger
  - アクション: #{personEditBean.deletePerson()}
  - 確認ダイアログ: "この人物情報を削除してもよろしいですか？"

#### data_model.md の変更

##### テーブル「PERSON」
**追加**:
| カラム名 | 型 | NULL | デフォルト | 説明 |
|---------|-----|------|-----------|------|
| address | VARCHAR(255) | YES | NULL | 住所 |
| phone | VARCHAR(20) | YES | NULL | 電話番号 |

**変更**:
- （該当なし）

**削除**:
- （該当なし）

#### behaviors.md の変更

##### シナリオ
**追加**:
- 人物情報削除シナリオ
  ```
  Given: 人物ID=123の人物が存在する
  When: PersonEditBean.deletePerson() を実行
  Then: 人物が削除される
  And: 一覧画面に遷移する
  And: 成功メッセージが表示される
  ```

### 変更理由

結合テストで「人物情報の削除ができない」という問題が発覚したため。
また、住所・電話番号の情報も管理したいという要望があったため。

### 影響範囲（推定）

#### 詳細設計
- FUNC_001_entity/detailed_design.md - PERSON エンティティの更新
- SCREEN_003_PersonEdit/detailed_design.md - PersonEditBean、edit.xhtml の更新

#### コード
- src/main/java/.../entity/Person.java - address、phone フィールド追加
- src/main/java/.../service/PersonService.java - delete() メソッド追加（必要に応じて）
- src/main/java/.../bean/PersonEditBean.java - deletePerson() アクションメソッド追加
- src/main/webapp/person/edit.xhtml - 削除ボタン、住所・電話番号フィールド追加

#### テスト
- src/test/java/.../PersonServiceTest.java - 削除テスト追加
- src/test/java/.../PersonEditBeanTest.java - deletePerson()テスト追加
- src/test/java/.../PersonServiceIntegrationTest.java - 結合テスト追加
- src/test/java/.../PersonEditE2ETest.java - E2Eテスト追加（Playwright）

---

## 記載のポイント

### 変更種別の明確化
- **追加**: 新規に追加されたもの
- **変更**: 既存のものが変更されたもの
- **削除**: 削除されたもの

### 詳細度
- セクション名、画面名、Managed Bean名、メソッド名など、できるだけ具体的に記載
- EXCEL形式の場合は、シート名、行番号なども記載
- AIが正確に理解できるよう、具体的に記載

### JSF特有の情報
- 画面（XHTML）の変更: UI部品、レイアウト、アクション
- Managed Bean の変更: プロパティ、アクションメソッド、画面遷移
- スコープ: ViewScoped、SessionScoped等

### 影響範囲
- 推定で構わないので、影響を受けると思われるファイルをリストアップ
- AIが影響分析時に参考にする

### 変更理由
- なぜこの変更が必要になったのかを明確に記載
- テストで発覚した問題、顧客要望、設計の誤り、マイグレーション過程での発見等
