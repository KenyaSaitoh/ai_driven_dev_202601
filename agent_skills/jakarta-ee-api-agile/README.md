# Jakarta EE API アジャイル開発 Agent Skill

## 概要

Jakarta EE 10とJAX-RS 3.1を使ったREST APIのアジャイル・仕様駆動開発を支援するAgent Skillです。

* 業務共通先行: 共通SPEC（data_model, external_interface, architecture_design）を先に作成
* ユースケース単位: 各ユースケースを `usecases/{フォルダ名}/` に userstory.md と behaviors.md で定義
* 駆動元: 業務共通SPEC + 各ユースケースSPEC に従って実装。合流ポイントは結合テスト

対象プロジェクト例: `projects/sdd-agile/bookstore/berry-books-api`, `projects/sdd-agile/bookstore/back-office-api`

## クイックスタート

1. 業務共通SPEC作成: `@agent_skills/jakarta-ee-api-agile/instructions/common_spec.md` で common/ に3SPECを作成
2. ユースケースSPEC作成: `@agent_skills/jakarta-ee-api-agile/instructions/usecase_spec.md` で各ユースケースに userstory.md, behaviors.md を作成
3. 実装: `code_generation.md` で target=common または target=usecases/{名} を指定してコード生成
4. 合流: 結合テストで common + 全ユースケースを検証

## 含まれるもの

* instructions/: 業務共通SPEC作成、ユースケースSPEC作成、コード生成、単体テスト実行、結合・E2Eテスト生成（合流ポイント）
* instructions/archive/: 詳細設計書・タスク分解（task_breakdown）・SPEC変更対応（spec_change）はアーカイブ済み（本フローでは不要。SPEC 更新後は code_generation を再実行して差分反映）
* principles/: Jakarta EE開発の原則（アーキテクチャ、セキュリティ、共通ルール）
* templates/: common用3ファイル、usecases用サンプル（userstory.md, behaviors.md）

## ウォーターフォール版との違い

| 観点 | ウォーターフォール (jakarta-ee-api-base) | アジャイル (本スキル) |
|------|------------------------------------------|------------------------|
| SPEC配置 | baseline/basic_design/ 一枚岩 + requirements/ | baseline/common/ + baseline/usecases/{名}/ |
| 機能単位 | タスク分解で FUNC_XXX を抽出 | ユースケースフォルダで事前に単位を定義 |
| 駆動元 | functional_design + basic_design 全体 | common の3SPEC + 各 usecases/{名}/ の userstory + behaviors |

詳細は [SKILL.md](SKILL.md) を参照してください。
