# 画像配信ドメイン - 振る舞い仕様書（単体テスト用）

ドメイン名: images  
バージョン: 1.0.0  
最終更新日: 2026-02-07

記法: シナリオは Gherkin 記法で記述する（Feature, Scenario, Given, When, Then, And, But）。principles/common_rules.md の「振る舞いの記法（Gherkin）」を参照すること。

---

## 1. 概要

本文書は、imagesドメインの単体テスト用の振る舞い、テストシナリオ、受入基準を記述する。

テスト対象:
* ImageResource（JAX-RS Resource）

単体テストの範囲:
* WAR内リソースへのアクセス機能をテスト
* ServletContextはモック化
* Content-Type判定ロジックをテスト
* パストラバーサル攻撃対策をテスト

関連ドキュメント:
* [detailed_design.md](detailed_design.md) - 詳細設計書
* [../../basic_design/images/functional_design.md](../../basic_design/images/functional_design.md) - ドメイン機能設計書
* [../../basic_design/images/behaviors.md](../../basic_design/images/behaviors.md) - ドメイン振る舞い仕様書（結合テスト用）

---

## 2. テストシナリオ

### 2.1 ImageResource - 画像ファイル取得（正常系）

#### Feature: 画像ファイル配信

画像ファイルが存在する場合、正しく配信される

#### Scenario: PNG画像を取得する

* Given（前提条件）:
  * WAR内に `/resources/images/book-cover-1.png` が存在する
  * ServletContextが正常にリソースを返却するようモック設定

* When（操作）:
  * `GET /api/images/book-cover-1.png` をリクエスト

* Then（期待結果）:
  * ステータスコード 200 OK が返される
  * Content-Type が `image/png` である
  * レスポンスボディに画像のバイナリデータが含まれる

#### テストデータ
* 入力:
  ```
  filename: "book-cover-1.png"
  ```
* 期待される出力:
  ```
  Status: 200 OK
  Content-Type: image/png
  Body: [画像バイナリデータ]
  ```

---

### 2.2 ImageResource - JPEG画像を取得する

#### Feature: 画像ファイル配信

JPEGファイルのContent-Typeが正しく判定される

#### Scenario: JPEG画像を取得する

* Given（前提条件）:
  * WAR内に `/resources/images/book-cover-2.jpg` が存在する
  * ServletContextが正常にリソースを返却するようモック設定

* When（操作）:
  * `GET /api/images/book-cover-2.jpg` をリクエスト

* Then（期待結果）:
  * ステータスコード 200 OK が返される
  * Content-Type が `image/jpeg` である
  * レスポンスボディに画像のバイナリデータが含まれる

---

### 2.3 ImageResource - GIF画像を取得する

#### Feature: 画像ファイル配信

GIFファイルのContent-Typeが正しく判定される

#### Scenario: GIF画像を取得する

* Given（前提条件）:
  * WAR内に `/resources/images/icon.gif` が存在する
  * ServletContextが正常にリソースを返却するようモック設定

* When（操作）:
  * `GET /api/images/icon.gif` をリクエスト

* Then（期待結果）:
  * ステータスコード 200 OK が返される
  * Content-Type が `image/gif` である
  * レスポンスボディに画像のバイナリデータが含まれる

---

### 2.4 ImageResource - 画像ファイルが存在しない場合（フォールバック）

#### Feature: 画像ファイル配信

画像ファイルが存在しない場合、フォールバック画像を返却する

#### Scenario: 存在しない画像を取得する

* Given（前提条件）:
  * WAR内に `/resources/images/non-existent.png` が存在しない
  * WAR内に `/resources/images/no-image.jpg` が存在する
  * ServletContextが最初のリクエストでnullを返し、2回目（フォールバック）で画像を返すようモック設定

* When（操作）:
  * `GET /api/images/non-existent.png` をリクエスト

* Then（期待結果）:
  * ステータスコード 200 OK が返される
  * Content-Type が `image/jpeg` である（no-image.jpgのため）
  * レスポンスボディにフォールバック画像のバイナリデータが含まれる

#### テストデータ
* 入力:
  ```
  filename: "non-existent.png"
  ```
* 期待される出力:
  ```
  Status: 200 OK
  Content-Type: image/jpeg
  Body: [no-image.jpgのバイナリデータ]
  ```

---

### 2.5 ImageResource - パストラバーサル攻撃対策

#### Feature: セキュリティ

不正なファイル名を拒否する

#### Scenario: パストラバーサルを試みる

* Given（前提条件）:
  * なし

* When（操作）:
  * `GET /api/images/../../../etc/passwd` をリクエスト

* Then（期待結果）:
  * ステータスコード 400 Bad Request が返される
  * エラーメッセージ「Invalid filename」が含まれる

#### テストデータ（不正な入力）
* `../../../etc/passwd`
* `..\..\..\..\windows\system32\config\sam`
* `image/../../../secret.txt`

---

### 2.6 ImageResource - スラッシュを含むファイル名の拒否

#### Feature: セキュリティ

ディレクトリトラバーサルを防ぐ

#### Scenario: スラッシュを含むファイル名を拒否する

* Given（前提条件）:
  * なし

* When（操作）:
  * `GET /api/images/subdir/image.png` をリクエスト

* Then（期待結果）:
  * ステータスコード 400 Bad Request が返される
  * エラーメッセージ「Invalid filename」が含まれる

---

### 2.7 ImageResource - 不明な拡張子の扱い

#### Feature: 画像ファイル配信

未知の拡張子はapplication/octet-streamとして扱う

#### Scenario: 不明な拡張子のファイルを取得する

* Given（前提条件）:
  * WAR内に `/resources/images/file.unknown` が存在する
  * ServletContextが正常にリソースを返却するようモック設定

* When（操作）:
  * `GET /api/images/file.unknown` をリクエスト

* Then（期待結果）:
  * ステータスコード 200 OK が返される
  * Content-Type が `application/octet-stream` である
  * レスポンスボディにファイルのバイナリデータが含まれる

---

### 2.8 ImageResource - フォールバック画像も存在しない場合

#### Feature: エラーハンドリング

フォールバック画像も存在しない場合はエラーを返す

#### Scenario: フォールバック画像が存在しない

* Given（前提条件）:
  * WAR内に `/resources/images/requested.png` が存在しない
  * WAR内に `/resources/images/no-image.jpg` も存在しない
  * ServletContextが両方のリクエストでnullを返すようモック設定

* When（操作）:
  * `GET /api/images/requested.png` をリクエスト

* Then（期待結果）:
  * ステータスコード 500 Internal Server Error が返される
  * エラーメッセージ「Image not found」が含まれる

---

## 3. モック化の方針

### 3.1 ドメイン内の依存関係
* ImageResourceのみ（モック不要）

### 3.2 ドメイン外の依存関係
* ServletContext → モック化
  * getResourceAsStream()メソッドをモック
  * テストケースに応じてInputStreamまたはnullを返すよう設定

---

## 4. カバレッジ目標

* ステートメントカバレッジ: 90%以上
* ブランチカバレッジ: 85%以上
  * Content-Type判定の全分岐
  * パストラバーサル検証の全分岐
  * フォールバック処理の全分岐

---

## 5. 受入基準

### 5.1 機能要件
- [ ] すべての正常系テストが成功する（PNG、JPEG、GIF）
- [ ] フォールバック処理のテストが成功する
- [ ] セキュリティテスト（パストラバーサル対策）が成功する
- [ ] Content-Type判定のテストが成功する
- [ ] エラーハンドリングのテストが成功する

### 5.2 品質要件
- [ ] カバレッジ目標を達成する
- [ ] テストコードにコメントが適切に記載されている
- [ ] テストケースが独立している（テスト間の依存関係がない）
- [ ] モックの設定が各テストケースで適切に初期化されている

---

## 6. 参考資料

* [detailed_design.md](detailed_design.md) - 詳細設計書
* [../../basic_design/images/functional_design.md](../../basic_design/images/functional_design.md) - ドメイン機能設計書
* [../../basic_design/images/behaviors.md](../../basic_design/images/behaviors.md) - ドメイン振る舞い仕様書（結合テスト用）
* [../../requirements/behaviors.md](../../requirements/behaviors.md) - システム振る舞い仕様書（E2Eテスト用）