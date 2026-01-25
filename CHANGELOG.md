# Changelog

## [Unreleased]

### Changed
- `run-berry-books-all.sh` を `run-bookstore-all.sh` にリネームし、`projects/master/bookstore/` に移動
- `restart-berry-books-spa.sh` を `run-bookstore-spa.sh` にリネームし、`projects/master/bookstore/` に移動
- ルート直下の `README.md` のクイックスタートセクションを更新（スクリプト名・実行パス・Bookstore表記に統一）
- `projects/master/bookstore/README.md` にフルスタック自動起動（`run-bookstore-all.sh` / `run-bookstore-spa.sh`）の説明を追加
- 各スクリプトは `projects/master/bookstore` から実行し、プロジェクトルートを相対パスで参照するように変更
- `agent_skills/jakarta-ee-api-base/instructions/code_generation.md` を修正: 本番コード生成後に単体テスト生成まで確実に完了するよう実行順序と完了条件を明確化
- `agent_skills/struts-to-jsf-migration/instructions/code_generation.md` を修正: 本番コード生成後に単体テスト生成まで確実に完了するよう実行順序と完了条件を明確化
- `projects/master/bookstore/back-office-spa/vite.config.ts` のプロキシ設定を変更: `back-office-api` → `back-office-api-sdd`
- `projects/master/bookstore/berry-books-spa/vite.config.ts` のプロキシ設定を変更: `berry-books-api` → `berry-books-api-sdd`

### Fixed
- `agent_skills/jakarta-ee-api-base/instructions/code_generation.md` の番号重複を修正（「11. 静的リソース」を「14.」に修正）

### Added
- `projects/master/person/jsf-person/run-jsf-person-all.sh` を追加: JSF Person アプリケーションの一括起動スクリプト
- `projects/master/person/struts-person/run-struts-person-all.sh` を追加: Struts Person アプリケーションの一括起動スクリプト
- `projects/sdd/bookstore/run-bookstore-all.sh` を追加: SDD版Bookstoreフルスタック一括起動スクリプト（back-office-api-sdd、berry-books-api-sdd、customer-hub-api + 3つのSPA）
- `projects/sdd/bookstore/run-bookstore-spa.sh` を追加: SDD版Bookstore SPA再起動スクリプト
- `projects/sdd/person/jsf-person-sdd/run-jsf-person-all.sh` を追加: SDD版JSF Personアプリケーションの一括起動スクリプト
