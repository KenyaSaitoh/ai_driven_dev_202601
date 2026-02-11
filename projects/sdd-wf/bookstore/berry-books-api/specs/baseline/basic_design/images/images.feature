@integration @images
Feature: 画像配信
  WAR内のリソースから画像ファイルを配信する

  Scenario: 画像ファイルを取得
    Given WAR内に画像ファイルが存在する:
      | filename           | contentType |
      | book-cover-1.png   | image/png   |
    When ImageService.getImage("book-cover-1.png")を呼び出す
    Then 画像バイナリが返される
    And Content-Type="image/png"

  Scenario: 存在しない画像ファイルを取得
    Given WAR内に画像ファイルが存在しない
    When ImageService.getImage("nonexistent.png")を呼び出す
    Then FileNotFoundExceptionがスローされる
