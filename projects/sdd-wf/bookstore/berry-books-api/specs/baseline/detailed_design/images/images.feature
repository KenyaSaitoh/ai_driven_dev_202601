@unit @images
Feature: 画像配信ドメイン（単体テスト）
  画像ファイルの配信、Content-Type判定、セキュリティ対策のテスト

  Scenario: PNG画像を取得する
    Given WAR内に "/resources/images/book-cover-1.png" が存在する
    And ServletContextが正常にリソースを返却するようモック設定
    When GET /api/images/book-cover-1.png をリクエスト
    Then ステータスコード 200 OK が返される
    And Content-Type が "image/png" である
    And レスポンスボディに画像のバイナリデータが含まれる

  Scenario: JPEG画像を取得する
    Given WAR内に "/resources/images/book-cover-2.jpg" が存在する
    And ServletContextが正常にリソースを返却するようモック設定
    When GET /api/images/book-cover-2.jpg をリクエスト
    Then ステータスコード 200 OK が返される
    And Content-Type が "image/jpeg" である
    And レスポンスボディに画像のバイナリデータが含まれる

  Scenario: GIF画像を取得する
    Given WAR内に "/resources/images/icon.gif" が存在する
    And ServletContextが正常にリソースを返却するようモック設定
    When GET /api/images/icon.gif をリクエスト
    Then ステータスコード 200 OK が返される
    And Content-Type が "image/gif" である
    And レスポンスボディに画像のバイナリデータが含まれる

  Scenario: 存在しない画像を取得する
    Given WAR内に "/resources/images/non-existent.png" が存在しない
    And WAR内に "/resources/images/no-image.jpg" が存在する
    When GET /api/images/non-existent.png をリクエスト
    Then ステータスコード 200 OK が返される
    And Content-Type が "image/jpeg" である
    And レスポンスボディにフォールバック画像のバイナリデータが含まれる

  Scenario: パストラバーサルを試みる
    When GET /api/images/../../../etc/passwd をリクエスト
    Then ステータスコード 400 Bad Request が返される
    And エラーメッセージ「Invalid filename」が含まれる

  Scenario: スラッシュを含むファイル名を拒否する
    When GET /api/images/subdir/image.png をリクエスト
    Then ステータスコード 400 Bad Request が返される
    And エラーメッセージ「Invalid filename」が含まれる

  Scenario: 不明な拡張子のファイルを取得する
    Given WAR内に "/resources/images/file.unknown" が存在する
    And ServletContextが正常にリソースを返却するようモック設定
    When GET /api/images/file.unknown をリクエスト
    Then ステータスコード 200 OK が返される
    And Content-Type が "application/octet-stream" である
    And レスポンスボディにファイルのバイナリデータが含まれる

  Scenario: フォールバック画像が存在しない
    Given WAR内に "/resources/images/requested.png" が存在しない
    And WAR内に "/resources/images/no-image.jpg" も存在しない
    When GET /api/images/requested.png をリクエスト
    Then ステータスコード 500 Internal Server Error が返される
    And エラーメッセージ「Image not found」が含まれる
