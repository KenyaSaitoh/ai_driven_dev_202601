# language: ja
機能: JwtAuthenFilter - 認証フィルター

  シナリオ: 有効なJWTトークンで認証成功
    前提 JwtUtilがモック化されている
    かつ AuthenInfoが注入されている
    かつ リクエストに有効なJWTトークン（Cookie）が含まれている
    かつ モック設定: JwtUtil.validateToken()がtrueを返す
    かつ モック設定: JwtUtil.getCustomerIdFromToken()が1を返す
    かつ モック設定: JwtUtil.getEmailFromToken()が"test@example.com"を返す
    もし JwtAuthenFilter.doFilter(request, response, chain)を呼び出す
    ならば AuthenInfo.customerIdが1に設定される
    かつ AuthenInfo.emailが"test@example.com"に設定される
    かつ chain.doFilter()が呼び出される

  シナリオ: JWTトークンが存在しない
    前提 JwtUtilがモック化されている
    かつ リクエストにJWTトークン（Cookie）が含まれていない
    かつ リクエストパス: /api/orders（認証必須）
    もし JwtAuthenFilter.doFilter(request, response, chain)を呼び出す
    ならば HTTPステータス401（Unauthorized）が返される
    かつ chain.doFilter()が呼び出されない

  シナリオ: 認証除外パスへのアクセス
    前提 JwtUtilがモック化されている
    かつ リクエストパス: /api/auth/login（認証除外）
    かつ リクエストにJWTトークンが含まれていない
    もし JwtAuthenFilter.doFilter(request, response, chain)を呼び出す
    ならば 認証処理がスキップされる
    かつ chain.doFilter()が呼び出される
