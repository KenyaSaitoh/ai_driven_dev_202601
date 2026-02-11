@unit @orders
Feature: 注文管理ドメイン（単体テスト）
  注文トランザクション、注文明細、配送料金計算、注文ビジネスロジック、注文APIのテスト

  Scenario: 注文トランザクションを正常に登録
    Given EntityManagerがモック化されている
    And 注文トランザクションが準備されている
    When OrderTranDao.insert(orderTran)を呼び出す
    Then EntityManager.persist()が呼び出される
    And 登録された注文トランザクションが返される

  Scenario: 注文IDで注文が見つかる
    Given EntityManagerがモック化されている
    And 注文ID=1の注文トランザクションが存在する
    When OrderTranDao.findById(1)を呼び出す
    Then 注文トランザクションが返される
    And 注文ID=1である

  Scenario: 注文IDで注文が見つからない
    Given EntityManagerがモック化されている
    And 注文ID=999の注文トランザクションが存在しない
    When OrderTranDao.findById(999)を呼び出す
    Then nullが返される

  Scenario: 顧客の注文履歴が見つかる（複数件、降順）
    Given EntityManagerがモック化されている
    And 顧客ID=1の注文が複数存在する
    And JPQLクエリが注文日の降順でリストを返す
    When OrderTranDao.findByCustomerId(1)を呼び出す
    Then 注文トランザクションのリストが返される
    And リストのサイズは2である
    And 最初の要素の注文日が2番目の要素より新しい

  Scenario: 顧客の注文履歴が見つからない
    Given EntityManagerがモック化されている
    And 顧客ID=999の注文が存在しない
    When OrderTranDao.findByCustomerId(999)を呼び出す
    Then 空のリストが返される

  Scenario: 注文明細を正常に登録
    Given EntityManagerがモック化されている
    And 注文明細が準備されている
    When OrderDetailDao.insert(orderDetail)を呼び出す
    Then EntityManager.persist()が呼び出される
    And 登録された注文明細が返される

  Scenario: スナップショット値が保持される
    Given EntityManagerがモック化されている
    And スナップショット値が設定された注文明細が準備されている
    When OrderDetailDao.insert(orderDetail)を呼び出す
    Then 書籍名="Java入門"が保持される
    And 出版社名="技術評論社"が保持される
    And 価格=3000が保持される

  Scenario: 注文明細が見つかる（複数件、明細ID昇順）
    Given EntityManagerがモック化されている
    And 注文ID=1の注文明細が複数存在する
    When OrderDetailDao.findByOrderTranId(1)を呼び出す
    Then 注文明細のリストが返される
    And リストのサイズは2である
    And 明細IDが昇順に並んでいる

  Scenario: 注文明細が見つからない
    Given EntityManagerがモック化されている
    And 注文ID=999の注文明細が存在しない
    When OrderDetailDao.findByOrderTranId(999)を呼び出す
    Then 空のリストが返される

  Scenario: 注文明細を追加する
    Given OrderTranエンティティが準備されている
    And OrderDetailエンティティが準備されている
    When OrderTran.addOrderDetail(orderDetail)を呼び出す
    Then OrderTran.orderDetailsリストに注文明細が追加される
    And OrderDetail.orderTranに親の注文トランザクションが設定される

  Scenario: 注文明細を削除する
    Given OrderTranエンティティが準備されている
    And OrderDetailエンティティが注文トランザクションに追加されている
    When OrderTran.removeOrderDetail(orderDetail)を呼び出す
    Then OrderTran.orderDetailsリストから注文明細が削除される
    And OrderDetail.orderTranがnullに設定される

  Scenario: 同じ値を持つ複合主キーは等しい
    Given OrderDetailPK(orderTranId=1, orderDetailId=1)が準備されている
    And OrderDetailPK(orderTranId=1, orderDetailId=1)がもう一つ準備されている
    When equals()メソッドで比較する
    Then trueが返される
    And hashCode()が同じ値を返す

  Scenario: 異なる値を持つ複合主キーは等しくない
    Given OrderDetailPK(orderTranId=1, orderDetailId=1)が準備されている
    And OrderDetailPK(orderTranId=1, orderDetailId=2)が準備されている
    When equals()メソッドで比較する
    Then falseが返される

  Scenario: 購入金額が5000円以上で配送料無料
    Given 購入金額=5000円
    And 配送先住所="東京都渋谷区1-1-1"
    When DeliveryFeeService.calculateDeliveryFee(5000, "東京都渋谷区1-1-1")を呼び出す
    Then 配送料金=0円が返される

  Scenario: 配送先が沖縄県の場合は800円
    Given 購入金額=3000円
    And 配送先住所="沖縄県那覇市1-1-1"
    When DeliveryFeeService.calculateDeliveryFee(3000, "沖縄県那覇市1-1-1")を呼び出す
    Then 配送料金=800円が返される

  Scenario: その他の地域は400円
    Given 購入金額=3000円
    And 配送先住所="東京都渋谷区1-1-1"
    When DeliveryFeeService.calculateDeliveryFee(3000, "東京都渋谷区1-1-1")を呼び出す
    Then 配送料金=400円が返される

  Scenario: 注文を正常に作成
    Given OrderTranDao、OrderDetailDao、BackOfficeRestClient、DeliveryFeeService、AuthenticatedUserがモック化されている
    And 在庫が十分にある
    And 配送料金が計算される
    When OrderService.createOrder(orderRequest)を呼び出す
    Then orderTranDao.insert()が呼び出される
    And orderDetailDao.insert()が注文明細数分呼び出される
    And backOfficeClient.updateStock()が在庫更新のために呼び出される

  Scenario: 在庫不足時に例外をスロー
    Given backOfficeClient.findStockById()が在庫数=5を返す
    And 注文数=10
    When OrderService.createOrder(orderRequest)を呼び出す
    Then RuntimeExceptionがスローされる
    And エラーメッセージに"在庫不足"が含まれる

  Scenario: 顧客の注文履歴を取得
    Given OrderTranDaoがモック化されている
    And 顧客ID=1の注文履歴が存在する
    When OrderService.getOrderHistory(1)を呼び出す
    Then orderTranDao.findByCustomerId(1)が呼び出される
    And 注文履歴のリストが返される

  Scenario: 注文作成が成功
    Given OrderServiceがモック化されている
    And orderService.createOrder()が正常にOrderTranを返す
    When OrderResource.createOrder(orderRequest)を呼び出す
    Then HTTPステータス201 Createdが返される
    And レスポンスボディにOrderResponseが含まれる

  Scenario: 在庫不足時に400 Bad Requestを返す
    Given OrderServiceがモック化されている
    And orderService.createOrder()が"在庫不足"を含むRuntimeExceptionをスローする
    When OrderResource.createOrder(orderRequest)を呼び出す
    Then HTTPステータス400 Bad Requestが返される
    And エラーメッセージに"在庫不足"が含まれる

  Scenario: 注文履歴を取得
    Given OrderServiceがモック化されている
    And orderService.getOrderHistory(1)が注文リストを返す
    When OrderResource.getOrderHistory()を呼び出す
    Then HTTPステータス200 OKが返される
    And レスポンスボディにList<OrderResponse>が含まれる

  Scenario: 注文詳細を取得
    Given OrderServiceがモック化されている
    And orderService.getOrderDetail(1)が注文情報を返す
    When OrderResource.getOrderDetail(1)を呼び出す
    Then HTTPステータス200 OKが返される
    And レスポンスボディにOrderResponseが含まれる

  Scenario: 注文が見つからない場合に404 Not Foundを返す
    Given OrderServiceがモック化されている
    And orderService.getOrderDetail(999)がnullを返す
    When OrderResource.getOrderDetail(999)を呼び出す
    Then HTTPステータス404 Not Foundが返される
    And エラーメッセージに"注文が見つかりません"が含まれる
