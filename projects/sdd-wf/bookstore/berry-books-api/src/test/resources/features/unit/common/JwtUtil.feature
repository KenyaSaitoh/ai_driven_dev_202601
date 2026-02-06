# language: ja
機能: JwtUtil - JWT生成・検証

  シナリオ: 顧客IDとメールアドレスからJWTを生成
    前提 JwtUtilが初期化されている
    かつ 秘密鍵が設定されている
    かつ customerId=1, email="test@example.com"
    もし JwtUtil.generateToken(1, "test@example.com")を呼び出す
    ならば JWTトークン文字列が返される
    かつ トークンが3つのパート（ヘッダー、ペイロード、署名）で構成されている
    かつ ペイロードにcustomerId=1が含まれている
    かつ ペイロードにemail="test@example.com"が含まれている

  シナリオ: 有効なJWTトークンを検証
    前提 JwtUtilが初期化されている
    かつ 有効なJWTトークンが存在する
    もし JwtUtil.validateToken(validToken)を呼び出す
    ならば trueが返される

  シナリオ: 期限切れのJWTトークンを検証
    前提 JwtUtilが初期化されている
    かつ 期限切れのJWTトークンが存在する
    もし JwtUtil.validateToken(expiredToken)を呼び出す
    ならば falseが返される
    かつ 例外はスローされない
