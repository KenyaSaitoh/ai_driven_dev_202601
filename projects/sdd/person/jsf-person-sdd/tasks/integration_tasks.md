# 結合テストタスク

担当者: 全員（画面実装完了後）
推奨スキル: Selenium、Arquillian、統合テスト
想定工数: 4時間
依存タスク: [SCREEN_001_PersonList.md](SCREEN_001_PersonList.md)、[SCREEN_002_PersonInput.md](SCREEN_002_PersonInput.md)、[SCREEN_003_PersonConfirm.md](SCREEN_003_PersonConfirm.md)

## タスク一覧

### デプロイメントテスト

* [ ] T_INTEGRATION_001: プロジェクトのビルド確認
  * 目的: プロジェクトが正しくビルドできることを確認する
  * 対象: プロジェクト全体
  * 参照SPEC: [requirements.md](../specs/baseline/system/requirements.md) の「4.1 技術スタック」
  * 注意事項: `./gradlew :jsf-person-sdd:war` でビルドが成功することを確認

* [ ] T_INTEGRATION_002: アプリケーションのデプロイ確認
  * 目的: アプリケーションがPayara Serverに正しくデプロイできることを確認する
  * 対象: jsf-person-sdd.war
  * 参照SPEC: [architecture_design.md](../specs/baseline/system/architecture_design.md) の「11. デプロイメント」
  * 注意事項: `./gradlew :jsf-person-sdd:deploy` でデプロイが成功することを確認

* [ ] T_INTEGRATION_003: アプリケーションのアクセス確認
  * 目的: デプロイしたアプリケーションにアクセスできることを確認する
  * 対象: http://localhost:8080/jsf-person-sdd/personList.xhtml
  * 参照SPEC: [requirements.md](../specs/baseline/system/requirements.md)
  * 注意事項: ブラウザでアクセスし、PERSON一覧画面が表示されることを確認

### 画面遷移テスト

* [ ] [P] T_INTEGRATION_004: PERSON一覧画面の表示テスト
  * 目的: PERSON一覧画面が正しく表示されることを確認する
  * 対象: SCREEN_001_PersonList
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_001_PersonList/screen_design.md)
    * [behaviors.md](../specs/baseline/screen/SCREEN_001_PersonList/behaviors.md)
  * 注意事項: 初期データ（3件）が正しく表示されることを確認

* [ ] [P] T_INTEGRATION_005: 新規追加ボタンの遷移テスト
  * 目的: 新規追加ボタンから入力画面に遷移できることを確認する
  * 対象: SCREEN_001_PersonList → SCREEN_002_PersonInput
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「3. 画面遷移図」
    * [behaviors.md](../specs/baseline/screen/SCREEN_001_PersonList/behaviors.md) の「3.1 新規追加フロー」
  * 注意事項: 入力画面が新規追加モードで開かれることを確認

* [ ] [P] T_INTEGRATION_006: 編集ボタンの遷移テスト
  * 目的: 編集ボタンから入力画面に遷移し、既存データが表示されることを確認する
  * 対象: SCREEN_001_PersonList → SCREEN_002_PersonInput
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「3. 画面遷移図」
    * [behaviors.md](../specs/baseline/screen/SCREEN_001_PersonList/behaviors.md) の「3.3 編集フロー」
  * 注意事項: personIdがURLパラメータで渡され、既存データが入力フィールドに表示されることを確認

* [ ] [P] T_INTEGRATION_007: 入力画面から確認画面への遷移テスト
  * 目的: 入力画面から確認画面に遷移できることを確認する
  * 対象: SCREEN_002_PersonInput → SCREEN_003_PersonConfirm
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「3. 画面遷移図」
    * [behaviors.md](../specs/baseline/screen/SCREEN_002_PersonInput/behaviors.md) の「3.1 入力確認フロー」
  * 注意事項: 入力データが確認画面で正しく表示されることを確認

* [ ] [P] T_INTEGRATION_008: 確認画面から一覧画面への遷移テスト
  * 目的: 確認画面で登録ボタンをクリックし、一覧画面にリダイレクトされることを確認する
  * 対象: SCREEN_003_PersonConfirm → SCREEN_001_PersonList
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「3. 画面遷移図」
    * [behaviors.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/behaviors.md) の「3.1 登録フロー」
  * 注意事項: リダイレクト遷移（?faces-redirect=true）が正しく動作することを確認

* [ ] [P] T_INTEGRATION_009: 戻るボタンの動作テスト
  * 目的: 確認画面から入力画面に戻るボタンが正しく動作することを確認する
  * 対象: SCREEN_003_PersonConfirm → SCREEN_002_PersonInput
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「3.3 画面遷移のパターン」
    * [behaviors.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/behaviors.md) の「3.2 戻るボタンフロー」
  * 注意事項: history.back()で入力画面に戻り、入力データが保持されることを確認

* [ ] [P] T_INTEGRATION_010: キャンセルボタンの動作テスト
  * 目的: 入力画面でキャンセルボタンをクリックし、一覧画面に戻ることを確認する
  * 対象: SCREEN_002_PersonInput → SCREEN_001_PersonList
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「3.2 画面遷移詳細」
    * [behaviors.md](../specs/baseline/screen/SCREEN_002_PersonInput/behaviors.md) の「3.2 キャンセルフロー」
  * 注意事項: immediate="true"でバリデーションがスキップされることを確認

### CRUDテスト

* [ ] [P] T_INTEGRATION_011: PERSON新規追加のE2Eテスト
  * 目的: PERSON新規追加の全体フローが正しく動作することを確認する
  * 対象: SCREEN_001 → SCREEN_002 → SCREEN_003 → SCREEN_001
  * 参照SPEC: 
    * [behaviors.md](../specs/baseline/system/behaviors.md) の「3.1 PERSON新規追加フロー」
    * [requirements.md](../specs/baseline/system/requirements.md) の「3.1 主要機能」
  * 注意事項: 新規追加ボタン → 入力 → 確認 → 登録 → 一覧画面で新しいPERSONが表示されることを確認

* [ ] [P] T_INTEGRATION_012: PERSON編集のE2Eテスト
  * 目的: PERSON編集の全体フローが正しく動作することを確認する
  * 対象: SCREEN_001 → SCREEN_002 → SCREEN_003 → SCREEN_001
  * 参照SPEC: 
    * [behaviors.md](../specs/baseline/system/behaviors.md) の「3.2 PERSON編集フロー」
    * [requirements.md](../specs/baseline/system/requirements.md) の「3.1 主要機能」
  * 注意事項: 編集ボタン → 既存データ表示 → 編集 → 確認 → 更新 → 一覧画面で更新されたPERSONが表示されることを確認

* [ ] [P] T_INTEGRATION_013: PERSON削除のE2Eテスト
  * 目的: PERSON削除の全体フローが正しく動作することを確認する
  * 対象: SCREEN_001
  * 参照SPEC: 
    * [behaviors.md](../specs/baseline/system/behaviors.md) の「3.3 PERSON削除フロー」
    * [requirements.md](../specs/baseline/system/requirements.md) の「3.1 主要機能」
  * 注意事項: 削除ボタン → JavaScript確認ダイアログ → 削除 → 一覧画面でPERSONが削除されることを確認

### バリデーションテスト

* [ ] [P] T_INTEGRATION_014: 必須入力バリデーションテスト
  * 目的: 必須項目が未入力の場合、エラーメッセージが表示されることを確認する
  * 対象: SCREEN_002_PersonInput
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「7.1 入力検証ルール」
    * [behaviors.md](../specs/baseline/screen/SCREEN_002_PersonInput/behaviors.md) の「4. エラーケース」
  * 注意事項: personName、age、genderが未入力の場合、@NotNullバリデーションが動作することを確認

* [ ] [P] T_INTEGRATION_015: 文字列長バリデーションテスト
  * 目的: personNameが最大長を超える場合、エラーメッセージが表示されることを確認する
  * 対象: SCREEN_002_PersonInput
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「7.1 入力検証ルール」
    * [behaviors.md](../specs/baseline/screen/SCREEN_002_PersonInput/behaviors.md) の「4. エラーケース」
  * 注意事項: personNameが31文字の場合、@Sizeバリデーションが動作することを確認

* [ ] [P] T_INTEGRATION_016: 数値範囲バリデーションテスト
  * 目的: ageが範囲外の場合、エラーメッセージが表示されることを確認する
  * 対象: SCREEN_002_PersonInput
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「7.1 入力検証ルール」
    * [behaviors.md](../specs/baseline/screen/SCREEN_002_PersonInput/behaviors.md) の「4. エラーケース」
  * 注意事項: ageが-1または151の場合、@Minまたは@Maxバリデーションが動作することを確認

### トランザクションテスト

* [ ] [P] T_INTEGRATION_017: トランザクションコミットテスト
  * 目的: 登録・更新処理が正しくコミットされることを確認する
  * 対象: PersonService
  * 参照SPEC: 
    * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.5 トランザクション管理」
    * [requirements.md](../specs/baseline/system/requirements.md) の「4.3 トランザクション要件」
  * 注意事項: @Transactionalアノテーションが正しく動作し、データベースに変更がコミットされることを確認

* [ ] [P] T_INTEGRATION_018: トランザクションロールバックテスト
  * 目的: 例外が発生した場合、トランザクションが正しくロールバックされることを確認する
  * 対象: PersonService
  * 参照SPEC: 
    * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「3.5 トランザクション管理」
    * [requirements.md](../specs/baseline/system/requirements.md) の「4.3 トランザクション要件」
  * 注意事項: RuntimeExceptionが発生した場合、データベースへの変更がロールバックされることを確認

### セッション管理テスト

* [ ] [P] T_INTEGRATION_019: ViewScopedライフサイクルテスト
  * 目的: @ViewScopedのManaged Beanが画面単位で正しく管理されることを確認する
  * 対象: PersonInputBean、PersonConfirmBean
  * 参照SPEC: 
    * [architecture_design.md](../specs/baseline/system/architecture_design.md) の「5.1 Presentation Layer」
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「6.2 スコープの使い分け」
  * 注意事項: 入力画面から確認画面への遷移時、PersonInputBeanのデータが保持されることを確認

* [ ] [P] T_INTEGRATION_020: データ受け渡しテスト
  * 目的: 画面間でデータが正しく受け渡されることを確認する
  * 対象: PersonInputBean → PersonConfirmBean
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「6. 画面間データ受け渡し」
    * [behaviors.md](../specs/baseline/system/behaviors.md) の「3.1 PERSON新規追加フロー」
  * 注意事項: PersonConfirmBeanがPersonInputBeanを@Injectし、入力データを参照できることを確認

### パフォーマンステスト

* [ ] [P] T_INTEGRATION_021: 一覧画面のレスポンスタイムテスト
  * 目的: 一覧画面が1秒以内に表示されることを確認する
  * 対象: SCREEN_001_PersonList
  * 参照SPEC: [requirements.md](../specs/baseline/system/requirements.md) の「4.5 パフォーマンス要件」
  * 注意事項: 初期データ3件の場合、1秒以内に画面が表示されることを確認

* [ ] [P] T_INTEGRATION_022: 登録・更新・削除のレスポンスタイムテスト
  * 目的: 登録・更新・削除処理が2秒以内に完了することを確認する
  * 対象: PersonService
  * 参照SPEC: [requirements.md](../specs/baseline/system/requirements.md) の「4.5 パフォーマンス要件」
  * 注意事項: データベース操作が2秒以内に完了することを確認

### 最終検証

* [ ] T_INTEGRATION_023: 全画面の表示確認
  * 目的: すべての画面が正しく表示され、CSSが適用されていることを確認する
  * 対象: SCREEN_001、SCREEN_002、SCREEN_003
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_001_PersonList/screen_design.md)
    * [screen_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/screen_design.md)
    * [screen_design.md](../specs/baseline/screen/SCREEN_003_PersonConfirm/screen_design.md)
  * 注意事項: style.cssが正しく読み込まれ、スタイルが適用されていることを確認

* [ ] T_INTEGRATION_024: ブラウザ互換性確認
  * 目的: 主要なブラウザで正しく動作することを確認する
  * 対象: Chrome、Firefox、Edge
  * 参照SPEC: [requirements.md](../specs/baseline/system/requirements.md)
  * 注意事項: 教育目的のため、最新版ブラウザで動作すれば可

* [ ] T_INTEGRATION_025: 最終動作確認
  * 目的: すべての機能が正しく動作することを最終確認する
  * 対象: プロジェクト全体
  * 参照SPEC: [requirements.md](../specs/baseline/system/requirements.md) の「3. 機能要件」
  * 注意事項: 新規追加、編集、削除の全機能が正常に動作することを確認

## 並行実行の注意

* T_INTEGRATION_001〜T_INTEGRATION_003は順次実行
* T_INTEGRATION_004〜T_INTEGRATION_022は並行実行可能
* T_INTEGRATION_023〜T_INTEGRATION_025は順次実行

## 完了基準

* すべての画面遷移が正しく動作する
* CRUD操作（新規追加、編集、削除）が正しく動作する
* バリデーションが正しく動作し、エラーメッセージが表示される
* トランザクション管理が正しく動作する
* セッション管理が正しく動作し、画面間でデータが受け渡される
* パフォーマンス要件を満たしている
* すべてのブラウザで正しく動作する

## 参考資料

* [システム要件定義](../specs/baseline/system/requirements.md)
* [アーキテクチャ設計書](../specs/baseline/system/architecture_design.md)
* [機能設計書](../specs/baseline/system/functional_design.md)
* [システム振る舞い仕様書](../specs/baseline/system/behaviors.md)
* [Jakarta EE開発憲章](../../../agent_skills/jakarta-ee-standard/principles/constitution.md)
