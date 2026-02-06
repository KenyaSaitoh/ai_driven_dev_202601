# language: ja
機能: Exception Mapper - 例外マッピング

  シナリオ: OutOfStockExceptionを409 Conflictにマッピング
    前提 OutOfStockExceptionがスローされる
    かつ message="在庫が不足しています"
    もし OutOfStockExceptionMapper.toResponse(exception)を呼び出す
    ならば HTTPステータス409（Conflict）が返される
    かつ ErrorResponseボディが含まれる: status=409, error="Conflict", message="在庫が不足しています"

  シナリオ: OptimisticLockExceptionを409 Conflictにマッピング
    前提 OptimisticLockExceptionがスローされる
    もし OptimisticLockExceptionMapper.toResponse(exception)を呼び出す
    ならば HTTPステータス409（Conflict）が返される
    かつ ErrorResponseボディが含まれる: status=409, error="Conflict", message="データが他のユーザーによって更新されました。再度お試しください。"

  シナリオ: ConstraintViolationExceptionを400 Bad Requestにマッピング
    前提 ConstraintViolationExceptionがスローされる
    かつ 違反: "メールアドレスは必須です"
    もし ValidationExceptionMapper.toResponse(exception)を呼び出す
    ならば HTTPステータス400（Bad Request）が返される
    かつ ErrorResponseボディが含まれる: status=400, error="Bad Request"
