# 共通設計 - 基本設計変更管理

プロジェクトID: jsf-person  
分類: common（共通設計）  
バージョン: 1.0.0  
最終更新日: 2026-02-06

---

## 概要

このファイルは、共通設計の基本設計変更を管理するためのものです。

変更内容を記載し、`@agent_skills/struts-to-jsf-migration/instructions/basic_design_change.md` の指示に従って、詳細設計・コード・テストを更新します。

適用完了後は、このファイルの内容を `changes_archive/` フォルダにアーカイブします。

---

## 変更履歴

現時点では変更なし。

---

## 使用方法

1. 変更内容をこのファイルに記載
2. `basic_design_change.md` の指示に従って影響分析
3. 詳細設計・コード・テストを更新
4. 適用完了後、`changes_archive/YYYYMMDD_変更タイトル.md` にアーカイブ

---

## 変更テンプレート

```markdown
## 変更ID: CHANGE_XXX

### 変更日時
YYYY-MM-DD HH:MM:SS

### 変更理由
変更が必要になった理由を記載

### 変更内容

#### 影響を受けるファイル
- architecture_design.md - {変更内容}
- data_model.md - {変更内容}
- functional_design.md - {変更内容}

#### 変更詳細
具体的な変更内容を記載

### 影響範囲
- 影響を受ける画面グループ: common, person_management
- 影響を受ける画面: FUNC_001, FUNC_002, FUNC_003

### 備考
その他の注意事項
```
