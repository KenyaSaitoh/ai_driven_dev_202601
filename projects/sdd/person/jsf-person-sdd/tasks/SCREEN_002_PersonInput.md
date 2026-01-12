# SCREEN_002_PersonInput タスク

担当者: 1名（担当者B）
推奨スキル: JSF、Bean Validation、Facelets XHTML
想定工数: 4時間
依存タスク: [common_tasks.md](common_tasks.md)

## タスク一覧

### Managed Bean

* [ ] T_SCREEN002_001: PersonInputBeanの作成
  * 目的: PERSON入力画面のManaged Beanを作成する
  * 対象: pro.kensait.jsf.person.bean.PersonInputBean
  * 参照SPEC: 
    * [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「2.1 PersonInputBean」
    * [functional_design.md](../specs/baseline/system/functional_design.md) の「4.2 SCREEN_002_PersonInput」
  * 注意事項: @Named("personInputBean")、@ViewScoped、implements Serializableを使用。PersonServiceを@Injectで注入

* [ ] T_SCREEN002_002: PersonInputBean - フィールドの定義
  * 目的: PERSON入力項目のフィールドを定義し、Bean Validationアノテーションを付加する
  * 対象: PersonInputBean - personId、personName、age、gender
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「2.1.1 フィールド」
  * 注意事項: @NotNull、@Size、@Min、@Maxアノテーションを使用してバリデーションルールを定義

* [ ] T_SCREEN002_003: PersonInputBean.init() の実装
  * 目的: 画面初期表示時にpersonIdが指定されている場合、既存データを取得する
  * 対象: PersonInputBean.init()
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「2.1.2 メソッド」
  * 注意事項: @PostConstructアノテーションを使用。personIdがnull以外の場合はpersonService.getPersonById()を呼び出す

* [ ] T_SCREEN002_004: PersonInputBean.confirm() の実装
  * 目的: 確認画面に遷移するメソッドを実装する
  * 対象: PersonInputBean.confirm()
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「2.1.2 メソッド」
  * 注意事項: "personConfirm"を返す。入力データはPersonInputBeanのフィールドに保持される

* [ ] T_SCREEN002_005: PersonInputBean.cancel() の実装
  * 目的: 一覧画面に戻るメソッドを実装する
  * 対象: PersonInputBean.cancel()
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「2.1.2 メソッド」
  * 注意事項: "personList?faces-redirect=true"を返す（リダイレクト遷移）

### Facelets XHTML

* [ ] T_SCREEN002_006: personInput.xhtmlの作成
  * 目的: PERSON入力画面のXHTMLファイルを作成する
  * 対象: src/main/webapp/personInput.xhtml
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/screen_design.md) の「2. 画面レイアウト」
    * [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「5.2 主要コンポーネント」
  * 注意事項: <f:metadata>で<f:viewParam>を定義し、personIdを受け取る

* [ ] T_SCREEN002_007: personInput.xhtml - ViewParameterの定義
  * 目的: URLパラメータからpersonIdを受け取る
  * 対象: personInput.xhtml - <f:metadata>と<f:viewParam>
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「5.2 主要コンポーネント」
  * 注意事項: <f:viewParam name="personId" value="#{personInputBean.personId}"/>を定義

* [ ] T_SCREEN002_008: personInput.xhtml - メッセージ表示の実装
  * 目的: エラーメッセージとバリデーションメッセージを表示する
  * 対象: personInput.xhtml - <h:messages>コンポーネント
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「5.2 主要コンポーネント」
  * 注意事項: globalOnly="false"、showSummary="true"、showDetail="false"を設定

* [ ] T_SCREEN002_009: personInput.xhtml - 名前入力フィールドの実装
  * 目的: 名前入力フィールドを実装する
  * 対象: personInput.xhtml - <h:inputText>コンポーネント
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/screen_design.md) の「3. 入力項目」
    * [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「5.2 主要コンポーネント」
  * 注意事項: value="#{personInputBean.personName}"、size="30"を設定

* [ ] T_SCREEN002_010: personInput.xhtml - 年齢入力フィールドの実装
  * 目的: 年齢入力フィールドを実装する
  * 対象: personInput.xhtml - <h:inputText>コンポーネント
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/screen_design.md) の「3. 入力項目」
    * [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「5.2 主要コンポーネント」
  * 注意事項: value="#{personInputBean.age}"、size="10"を設定

* [ ] T_SCREEN002_011: personInput.xhtml - 性別ラジオボタンの実装
  * 目的: 性別選択ラジオボタンを実装する
  * 対象: personInput.xhtml - <h:selectOneRadio>コンポーネント
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/screen_design.md) の「3. 入力項目」
    * [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「5.2 主要コンポーネント」
  * 注意事項: value="#{personInputBean.gender}"、<f:selectItem itemValue="male" itemLabel="男性"/>と<f:selectItem itemValue="female" itemLabel="女性"/>を定義

* [ ] T_SCREEN002_012: personInput.xhtml - 確認画面へボタンの実装
  * 目的: 確認画面に遷移するボタンを実装する
  * 対象: personInput.xhtml - <h:commandButton>コンポーネント
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/screen_design.md) の「4. ボタンアクション」
    * [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「6.1 遷移先」
  * 注意事項: action="#{personInputBean.confirm}"を設定。バリデーションが実行される（immediate="false"）

* [ ] T_SCREEN002_013: personInput.xhtml - キャンセルボタンの実装
  * 目的: 一覧画面に戻るキャンセルボタンを実装する
  * 対象: personInput.xhtml - <h:commandButton>コンポーネント
  * 参照SPEC: 
    * [screen_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/screen_design.md) の「4. ボタンアクション」
    * [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「6.1 遷移先」
  * 注意事項: action="#{personInputBean.cancel}"、immediate="true"を設定してバリデーションをスキップ

* [ ] T_SCREEN002_014: personInput.xhtml - hiddenフィールドの実装
  * 目的: personIdをhiddenフィールドで保持する
  * 対象: personInput.xhtml - <h:inputHidden>コンポーネント
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「5.2 主要コンポーネント」
  * 注意事項: value="#{personInputBean.personId}"を設定。編集モードの場合に値を保持

### 単体テスト

* [ ] [P] T_SCREEN002_015: PersonInputBeanTestの作成
  * 目的: PersonInputBeanの単体テストを作成する
  * 対象: pro.kensait.jsf.person.bean.PersonInputBeanTest
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md)
  * 注意事項: MockitoでPersonServiceをモック化してテスト

* [ ] [P] T_SCREEN002_016: PersonInputBeanTest - init()のテスト（新規追加モード）
  * 目的: init()メソッドが新規追加モードで正しく動作することをテストする
  * 対象: PersonInputBeanTest.testInitNewMode()
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「2.1.2 メソッド」
  * 注意事項: personIdがnullの場合、フィールドが初期化されることを確認

* [ ] [P] T_SCREEN002_017: PersonInputBeanTest - init()のテスト（編集モード）
  * 目的: init()メソッドが編集モードで正しく既存データを取得することをテストする
  * 対象: PersonInputBeanTest.testInitEditMode()
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「2.1.2 メソッド」
  * 注意事項: personIdが指定されている場合、personService.getPersonById()が呼び出されることを確認

* [ ] [P] T_SCREEN002_018: PersonInputBeanTest - confirm()のテスト
  * 目的: confirm()メソッドが正しく確認画面に遷移することをテストする
  * 対象: PersonInputBeanTest.testConfirm()
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「2.1.2 メソッド」
  * 注意事項: 戻り値が"personConfirm"であることを確認

* [ ] [P] T_SCREEN002_019: PersonInputBeanTest - cancel()のテスト
  * 目的: cancel()メソッドが正しく一覧画面に遷移することをテストする
  * 対象: PersonInputBeanTest.testCancel()
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「2.1.2 メソッド」
  * 注意事項: 戻り値が"personList?faces-redirect=true"であることを確認

* [ ] [P] T_SCREEN002_020: PersonInputBeanTest - Bean Validationのテスト
  * 目的: Bean Validationアノテーションが正しく動作することをテストする
  * 対象: PersonInputBeanTest.testBeanValidation()
  * 参照SPEC: [functional_design.md](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md) の「2.1.1 フィールド」
  * 注意事項: @NotNull、@Size、@Min、@Maxのバリデーションを確認

## 並行実行の注意

* T_SCREEN002_001〜T_SCREEN002_005は順次実行
* T_SCREEN002_006〜T_SCREEN002_014は順次実行
* T_SCREEN002_015〜T_SCREEN002_020（単体テスト）はT_SCREEN002_005完了後に並行実行可能

## 完了基準

* PersonInputBeanが作成され、init()、confirm()、cancel()メソッドが実装されている
* personInput.xhtmlが作成され、名前、年齢、性別の入力フィールドが実装されている
* Bean Validationアノテーションが定義され、入力検証が動作する
* PersonInputBeanの単体テストが作成され、すべてのメソッドがテストされている

## 参考資料

* [SCREEN_002_PersonInput画面設計](../specs/baseline/screen/SCREEN_002_PersonInput/screen_design.md)
* [SCREEN_002_PersonInput機能設計](../specs/baseline/screen/SCREEN_002_PersonInput/functional_design.md)
* [SCREEN_002_PersonInput振る舞い仕様](../specs/baseline/screen/SCREEN_002_PersonInput/behaviors.md)
* [システム機能設計書](../specs/baseline/system/functional_design.md)
