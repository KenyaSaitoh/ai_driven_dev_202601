# ユースケース: ワークフロー作成・更新・申請・承認・却下・一覧・履歴・書籍反映

ユースケースID: workflow  
バージョン: 1.0.0  
最終更新日: 2026-01-31  
ステータス: 確定

---

## 1. ユーザーストーリー

**As a** 社員  
**I want to** ワークフロー（書籍マスタ変更申請）を作成・一時保存・申請・承認・却下し、一覧・履歴を確認し、承認後に書籍マスタに反映できる  
**So that** 書籍情報の変更を承認フローで管理できる

---

## 2. 受入基準

* AC1: 新規ワークフローを作成できる（3種類: 新規・変更・廃止）。POST /api/workflows
* AC2: ワークフローを一時保存できる（CREATED 状態のみ）。PUT /api/workflows/{id}
* AC3: ワークフローを申請できる（CREATED → APPLIED）。POST /api/workflows/{id}/apply
* AC4: ワークフローを承認できる（APPLIED → APPROVED）。権限（職務ランク等）に応じて許可。POST /api/workflows/{id}/approve
* AC5: ワークフローを却下できる（APPLIED → CREATED）。POST /api/workflows/{id}/reject
* AC6: ワークフロー一覧を取得できる（フィルタリング可）。GET /api/workflows
* AC7: 指定ワークフローの操作履歴を取得できる。GET /api/workflows/{id}/history
* AC8: 承認されたワークフローの内容を書籍マスタに反映する（BOOK/STOCK 更新）

---

## 3. 概要

WORKFLOW テーブルでワークフローと操作履歴を管理。状態: CREATED, APPLIED, APPROVED。操作タイプ・操作者・操作日時・理由を記録。承認後は BookService 等で書籍マスタに反映。

---

## 4. API仕様（概要）

| メソッド | パス | 説明 |
|---------|------|------|
| POST | /api/workflows | ワークフロー作成 |
| PUT | /api/workflows/{id} | ワークフロー更新（一時保存） |
| POST | /api/workflows/{id}/apply | 申請 |
| POST | /api/workflows/{id}/approve | 承認 |
| POST | /api/workflows/{id}/reject | 却下 |
| GET | /api/workflows | 一覧（フィルタ可） |
| GET | /api/workflows/{id}/history | 操作履歴 |
| （内部） | 承認後 | 書籍マスタ反映 |

* 参照: [../../common/data_model.md](../../common/data_model.md)（WORKFLOW）。ワークフロー詳細は本userstoryおよび [behaviors.md](behaviors.md) を参照。

---

## 5. ビジネスルール

* BR-WF-001: 更新・申請・承認・却下は状態に応じて許可（CREATED のときのみ更新・申請可能等）
* BR-WF-002: 承認は権限（職務ランク等）を持つ社員のみ実行可能。違反時は 403 Forbidden（UnauthorizedApprovalException）
* BR-WF-003: 承認されたワークフローは書籍マスタ（BOOK/STOCK）に反映する

---

## 6. 参照

* [../../common/data_model.md](../../common/data_model.md) - WORKFLOW, EMPLOYEE, BOOK, STOCK
* [../../common/architecture_design.md](../../common/architecture_design.md) - 例外マッピング（WorkflowExceptionMapper）
* [behaviors.md](behaviors.md)
