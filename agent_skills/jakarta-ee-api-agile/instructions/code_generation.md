# コード生成インストラクション（アジャイル）

## パラメータ設定

```yaml
project_root: "ここにプロジェクトルートのパスを入力"
spec_directory: "SPECディレクトリのパス（オプション、デフォルト: {project_root}/specs/baseline）"
target: "common"  # または "usecases/order-creation" のように usecases/{フォルダ名}
skip_infrastructure: false  # setup 時のみ: true の場合、インフラセットアップをスキップ
```

* 例: 業務共通（common）の実装
```yaml
project_root: "projects/sdd-agile/bookstore/berry-books-api"
target: "common"
```

* 例: ユースケースの実装
```yaml
project_root: "projects/sdd-agile/bookstore/berry-books-api"
target: "usecases/order-creation"
```

注意: パス区切りはOS環境に応じて調整する。以降、`{project_root}`, `{spec_directory}`, `{target}` はパラメータで設定した値に置き換える。`spec_directory` 未指定時は `{project_root}/specs/baseline` とする。

---

## 概要

このインストラクションは、業務共通SPEC（common/）と各ユースケースSPEC（usecases/{名}/）を駆動元に、指定された対象（common または usecases/{名}）の実装コードと単体テストを生成・更新するためのものである。タスクファイル（tasks/）は不要である。

アジャイルにおける位置づけ
* 本インストラクションは、何度でも繰り返し実行することを前提とする。既に出来上がっているコードに対して、SPEC に基づく差分を反映させ、漸進的にコードを更新していく
* 基本設計SPECの「変更管理」は行わない。SPEC を編集したうえで、本インストラクションで target を指定して再実行すればよい
* 既存のコード・テストがある場合は、SPEC との差分を反映する形で更新する。ゼロから全体を再生成するのではなく、変更が必要な箇所を漸進的に更新する

重要: 指定された target のみを実行し、完了したら停止する。次の対象に自動的に進んではいけない。詳細設計書（detailed_design）は必須としない。

---

## 1. 対象の判別

* `target` パラメータから、common 用かユースケース用かを判別する
* `target` が `common` → 業務共通の実装
* `target` が `usecases/{フォルダ名}` → そのユースケースの実装（usecase_folder はフォルダ名とする）

---

## 2. 読み込むドキュメント（共通）

1. Agent Skillsルール: @agent_skills/jakarta-ee-api-agile/principles/ を最優先で確認する
2. 共通SPEC（常に参照）:
   * `{spec_directory}/common/architecture_design.md` - 技術スタック、レイヤー、パッケージ、セキュリティ、テスト戦略
   * `{spec_directory}/common/data_model.md` - テーブル定義、ERD
   * `{spec_directory}/common/external_interface.md` - 外部連携（該当時）

---

## 3. 読み込むドキュメント（target=common の場合）

* 上記「共通」のみ。common の3SPECを駆動元に実装する。
* 機能要件の「唯一の真実」は common の3SPEC。

---

## 4. 読み込むドキュメント（target=usecases/{名} の場合）

* 上記「共通」に加え:
* `{spec_directory}/usecases/{usecase_folder}/userstory.md` - ユーザーストーリー、受入基準、API仕様、ビジネスルール
* `{spec_directory}/usecases/{usecase_folder}/behaviors.md` - 振る舞い・テストシナリオ
* common の3SPECで既に定義された Entity/Dao 等を参照する

---

## 5. 実装の実行

* 指定された target（common または usecases/{名}）のみを実行する
* 既存コードがある場合: 現在の SPEC と既存コードの差分を検出し、必要な追加・修正・削除のみを反映する（漸進的更新）。既存の手書きコードや他 target で生成したコードは破壊しない
* 既存コードがない場合: 本番コード生成 → 単体テスト生成の順で新規作成する
* target=common で setup に相当する場合: skip_infrastructure に応じてインフラをスキップするかどうか判断する。アプリケーション固有のセットアップ（スキーマ、初期データ、静的リソース）は常に実行する
* 技術スタック・パッケージ・命名規則は common/architecture_design.md に厳密に従う
* 上位SPEC（common の3ファイル、usecases/{名}/userstory.md, behaviors.md）は修正しない

---

## 6. コンポーネント別参照

| コンポーネント | common タスク時の参照 | ユースケースタスク時の参照 |
|---------------|----------------------|---------------------------|
| Entity | data_model.md, architecture_design.md | data_model.md, userstory.md |
| Dao | data_model.md, architecture_design.md | userstory.md, common/（data_model 等） |
| Service | architecture_design.md, data_model.md | userstory.md, behaviors.md, common/ |
| Resource | architecture_design.md | userstory.md（API仕様）, behaviors.md |
| DTO | data_model.md | userstory.md, data_model.md |
| Filter/外部連携 | architecture_design.md, external_interface.md | architecture_design.md, external_interface.md, userstory.md |

---

## 7. 単体テスト生成

* タスク粒度内の単体テストを生成する
* target=common: common の3SPECからメソッド・振る舞いを抽出し、単体テストを作成する。data_model のテーブル/エンティティ、architecture_design の共通コンポーネントに基づくテストとする
* target=usecases/{名}: usecases/{名}/behaviors.md の Gherkin シナリオを単体テストに変換する。同一対象内は実連携、他はモック
* テストフレームワーク・カバレッジ目標は common/architecture_design.md に従う

---

## 8. 参考

* ウォーターフォール版: @agent_skills/jakarta-ee-api-base/instructions/code_generation.md（パス・参照元の違いを除き実行方針は同一）
