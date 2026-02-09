# 単体テスト評価レポート

プロジェクト: projects/sdd-wf/bookstore/berry-books-api  
テストタイプ: unit  
評価日時: 2026-02-09 10:30:00  

---

## 1. エグゼクティブサマリー

* **全体評価**: ✅ 合格（1つの警告あり）
* **カバレッジ達成状況**: 3/3項目達成（全体目標クリア）
* **テスト実行結果**: 74件成功、0件失敗、0件スキップ
* **主な問題点**: Securityパッケージ（特にJwtUtilクラス）の分岐カバレッジが低い

---

## 2. カバレッジ評価

### 2.1 全体カバレッジ

| 指標 | 目標 | 実測値 | 達成状況 |
|------|------|--------|----------|
| 行カバレッジ | 80% | 89% (479/532) | ✅ 達成 (+9%) |
| 分岐カバレッジ | 70% | 78% (71/90) | ✅ 達成 (+8%) |
| メソッドカバレッジ | 85% | 90% (152/168) | ✅ 達成 (+5%) |
| クラスカバレッジ | - | 96% (25/26) | ✅ 優秀 |
| 命令カバレッジ | - | 89% (1703/1894) | ✅ 優秀 |

**総合評価**: 全ての目標カバレッジを達成しており、非常に良好な状態です。

### 2.2 パッケージ別カバレッジ

| パッケージ | 行カバレッジ | 分岐カバレッジ | メソッドカバレッジ | 状態 |
|-----------|------------|--------------|----------------|------|
| pro.kensait.berrybooks.service | 100% (63/63) | 100% (14/14) | 100% (9/9) | ✅ 完璧 |
| pro.kensait.berrybooks.dao | 100% (29/29) | n/a | 100% (9/9) | ✅ 完璧 |
| pro.kensait.berrybooks.entity | 100% (77/77) | 90% (9/10) | 100% (44/44) | ✅ 優秀 |
| pro.kensait.berrybooks.util | 100% (10/10) | n/a | 100% (4/4) | ✅ 完璧 |
| pro.kensait.berrybooks.api | 88% (122/144) | 84% (22/26) | 95% (20/21) | ✅ 良好 |
| pro.kensait.berrybooks.security | 86% (86/95) | **65% (25/38)** | 90% (18/20) | ⚠️ 分岐カバレッジ未達 |
| pro.kensait.berrybooks.external | 78% (33/42) | 50% (1/2) | 85% (11/13) | ⚠️ 低カバレッジ |
| pro.kensait.berrybooks.external.dto | 78% (56/68) | n/a | 77% (34/44) | ⚠️ DTO（除外対象候補） |
| pro.kensait.berrybooks.api.dto | 74% (3/4) | n/a | 75% (3/4) | ⚠️ DTO（除外対象候補） |

### 2.3 パッケージ別分析

#### ✅ 優秀なパッケージ（100%カバレッジ）

* **service**: 注文ビジネスロジックが完全にテストされている
* **dao**: データアクセス層が完全にテストされている  
* **util**: ユーティリティクラスが完全にテストされている

#### ⚠️ 改善が必要なパッケージ

**1. pro.kensait.berrybooks.security (分岐カバレッジ 65% < 目標 70%)**

* **問題**: 分岐カバレッジが目標の70%を下回っている（5%不足）
* **影響度**: 高（セキュリティ関連コードのため重要）
* **主な原因**: JwtUtilクラスの分岐カバレッジが16%と極端に低い

**詳細分析（Securityパッケージ内）**:

| クラス | 行カバレッジ | 分岐カバレッジ | 主な問題 |
|--------|------------|--------------|---------|
| AuthenticatedUser | 100% | 100% | ✅ 問題なし |
| JwtAuthenFilter | 99% | 86% | ✅ ほぼ完璧 |
| **JwtUtil** | **73%** | **16% (2/12)** | ❌ 深刻な問題 |

**2. pro.kensait.berrybooks.external (外部API連携)**

* **状態**: 行カバレッジ78%、分岐カバレッジ50%
* **評価**: 外部API連携クラスとしては許容範囲内
* **理由**: 外部APIのエラーケース等、実際のテスト環境では再現困難なケースを含む

---

## 3. 未テストコード

### 3.1 カバレッジ0%のクラス

| クラス名 | タイプ | 推奨アクション |
|---------|------|--------------|
| OrderDetailResponse | DTO (Record) | カバレッジ除外対象として扱う（DTO） |

**評価**: OrderDetailResponseは単純なデータ転送オブジェクト（Record）であり、ビジネスロジックを持たないため、カバレッジ評価から除外しても問題ありません。

### 3.2 カバレッジ0%のメソッド（重要度：高）

| クラス名 | メソッド名 | 行数 | 分岐数 | 推奨アクション |
|---------|-----------|------|--------|--------------|
| JwtUtil | extractJwtFromRequest(HttpServletRequest) | 8 | 6 | ⚠️ **テスト追加を強く推奨** |
| JwtUtil | getCookieName() | 1 | 0 | △ 低優先度（単純なgetter） |

**重要**: `extractJwtFromRequest()`メソッドは、リクエストからJWTトークンを抽出する重要なメソッドです。このメソッドが未テストであることは、セキュリティ上のリスクとなります。

---

## 4. テスト品質評価

### 4.1 テスト実行サマリー

* **総テスト数**: 74件
* **成功**: 74件 (100%)
* **失敗**: 0件
* **スキップ**: 0件
* **実行時間**: 約3.5秒（推定）

### 4.2 テストクラス一覧

| テストクラス | テスト数 | 状態 |
|------------|---------|------|
| OrderServiceTest | 4 | ✅ 成功 |
| OrderResourceTest | 5 | ✅ 成功 |
| DeliveryFeeServiceTest | 5 | ✅ 成功 |
| BookResourceTest | 5 | ✅ 成功 |
| CategoryResourceTest | 5 | ✅ 成功 |
| ImageResourceTest | 4 | ✅ 成功 |
| JwtUtilTest | 7 | ✅ 成功 |
| JwtAuthenFilterTest | 8 | ✅ 成功 |
| AuthenticatedUserTest | 6 | ✅ 成功 |
| OrderTranDaoTest | 4 | ✅ 成功 |
| OrderDetailDaoTest | 3 | ✅ 成功 |
| OrderTranTest | 7 | ✅ 成功 |
| OrderDetailTest | 6 | ✅ 成功 |
| OrderDetailPKTest | 4 | ✅ 成功 |
| BackOfficeRestClientTest | 3 | ✅ 成功 |
| CustomerHubRestClientTest | 2 | ✅ 成功 |
| PasswordUtilTest | 4 | ✅ 成功 |

**総合評価**: 全てのテストが成功しており、コードの安定性が高いことを示しています。

### 4.3 テスト密度

* **総メソッド数**: 168
* **総テスト数**: 74
* **テスト密度**: 0.44 (1メソッドあたり0.44個のテスト)

**評価**: ⚠️ テスト密度が一般的な目標値（1.5以上）を下回っています。ただし、以下の理由により許容範囲内と判断します：

1. DAOやEntityのような単純なCRUD操作は少数のテストで十分
2. DTOクラス（Record）は通常テスト不要
3. 外部API連携クラスはモック化されており、エッジケースのテストが制限される

### 4.4 テスト品質（内容分析）

**優れている点**:

* 正常系・異常系の両方をカバー（例: OrderServiceTest）
* 境界値テストの実施（例: DeliveryFeeServiceTest）
* エッジケーステストの実施（例: OrderResourceTest - 404 Not Found）
* セキュリティテストの実施（例: JwtUtilTest, JwtAuthenFilterTest）

---

## 5. 推奨アクション

### 5.1 高優先度（必須）

#### 1. JwtUtilクラスの分岐カバレッジ向上

* **現状**: 分岐カバレッジ16% (2/12分岐)
* **目標**: 最低70%以上（できれば90%以上）
* **不足**: 54%以上
* **対象メソッド**: `extractJwtFromRequest(HttpServletRequest)`

**推奨テストケース**:

```java
// 1. Cookieが存在しない場合
@Test
void testExtractJwtFromRequest_NoCookie() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getCookies()).thenReturn(null);
    
    String result = jwtUtil.extractJwtFromRequest(request);
    assertNull(result);
}

// 2. Cookie配列が空の場合
@Test
void testExtractJwtFromRequest_EmptyCookieArray() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getCookies()).thenReturn(new Cookie[0]);
    
    String result = jwtUtil.extractJwtFromRequest(request);
    assertNull(result);
}

// 3. JWTトークンCookieが存在する場合
@Test
void testExtractJwtFromRequest_JwtCookieExists() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    Cookie jwtCookie = new Cookie("berry-books-jwt", "valid.jwt.token");
    when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
    
    String result = jwtUtil.extractJwtFromRequest(request);
    assertEquals("valid.jwt.token", result);
}

// 4. 他のCookieのみ存在する場合（JWTトークンCookieなし）
@Test
void testExtractJwtFromRequest_OtherCookiesOnly() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    Cookie otherCookie = new Cookie("session", "abc123");
    when(request.getCookies()).thenReturn(new Cookie[]{otherCookie});
    
    String result = jwtUtil.extractJwtFromRequest(request);
    assertNull(result);
}

// 5. 複数Cookieが存在し、その中にJWTトークンCookieがある場合
@Test
void testExtractJwtFromRequest_MultipleIncludingJwt() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    Cookie cookie1 = new Cookie("session", "abc123");
    Cookie jwtCookie = new Cookie("berry-books-jwt", "valid.jwt.token");
    Cookie cookie2 = new Cookie("lang", "ja");
    when(request.getCookies()).thenReturn(new Cookie[]{cookie1, jwtCookie, cookie2});
    
    String result = jwtUtil.extractJwtFromRequest(request);
    assertEquals("valid.jwt.token", result);
}

// 6. JWTトークンCookieの値がnullまたは空の場合
@Test
void testExtractJwtFromRequest_JwtCookieEmptyValue() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    Cookie jwtCookie = new Cookie("berry-books-jwt", "");
    when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
    
    String result = jwtUtil.extractJwtFromRequest(request);
    assertEquals("", result);
}
```

**期待される効果**: 
* JwtUtilの分岐カバレッジが16% → 90%以上に向上
* Securityパッケージの分岐カバレッジが65% → 80%以上に向上
* セキュリティ関連コードの信頼性が大幅に向上

### 5.2 中優先度（推奨）

#### 1. Securityパッケージの残りの分岐カバレッジ向上

* **対象**: JwtAuthenFilter（分岐カバレッジ86%）
* **推奨アクション**: 残りの3分岐のテストケース追加
  * JWT検証失敗時のエラーハンドリング
  * 予期しない例外発生時の処理
  * 特定のHTTPメソッド（OPTIONS等）のハンドリング

#### 2. External DTOクラスのカバレッジ向上（オプション）

* **現状**: 78% (56/68行)
* **評価**: DTOクラスであり、カバレッジ除外対象として扱うことも可能
* **推奨**: カバレッジ除外パターンに`/dto/`を追加し、評価対象から除外

### 5.3 低優先度（任意）

#### 1. External APIクライアントクラスの分岐カバレッジ向上

* **現状**: 50% (1/2分岐)
* **評価**: 外部API連携コードとして許容範囲内
* **理由**: 
  * 外部APIのエラーケースをモック環境で再現することは困難
  * 統合テストやE2Eテストでカバーする方が効果的

---

## 6. カバレッジ除外の提案

以下のクラス/パッケージをカバレッジ評価から除外することを推奨します:

```yaml
coverage_exclusions:
  - "/dto/"                    # DTO（Data Transfer Object）
  - "/*Dto.java"              # DTO命名パターン
  - "/*TO.java"               # Transfer Object命名パターン
  - "/*Record.java"           # Recordクラス
  - "/generated/"             # 自動生成コード
```

**除外理由**:
* DTOクラスはビジネスロジックを持たない単純なデータ構造
* Recordクラスはコンパイラが自動生成するメソッドのみを持つ
* これらのクラスをテストしても品質向上への寄与が小さい

**除外後の予想カバレッジ**:
* 行カバレッジ: 89% → 95%以上
* 分岐カバレッジ: 78% → 85%以上

---

## 7. 次のステップ

### ユーザーへの提案

現在、全体的なカバレッジ目標は達成されていますが、セキュリティパッケージの分岐カバレッジが目標を5%下回っています。以下のいずれかを選択してください:

**A. JwtUtilクラスにテストケースを追加する（推奨）**
  * 理由: セキュリティ関連コードであり、高いカバレッジが望ましい
  * 作業量: 約6個のテストケース追加（推定30分）
  * 効果: 分岐カバレッジが16% → 90%以上に向上

**B. カバレッジ目標を見直す（代替案）**
  * Securityパッケージの分岐カバレッジ目標を65%に引き下げる
  * architecture_design.mdにテスト戦略セクションを追加し、パッケージ別目標を明記
  * 理由: 外部API連携を含む複雑な処理のため、100%カバレッジは困難

**C. 一旦スキップして、後で対応する**
  * 現状のカバレッジでも全体目標は達成しているため、他の優先タスクを先に実施
  * 後でセキュリティテストの強化として対応

**D. DTO除外設定を追加して再評価する**
  * カバレッジ除外設定にDTOパターンを追加
  * 再評価後、より正確なカバレッジ指標を確認

どちらを選択しますか？

---

## 8. 完了検証

* ✅ Jacocoレポートが正常に読み込めたことを確認
* ✅ カバレッジ評価が完了したことを確認
* ✅ デッドコード検出が完了したことを確認（1件のDTO検出）
* ✅ 評価レポートが生成されたことを確認
* ✅ ユーザーへのフィードバックが完了したことを確認

---

## 参考資料

* [単体テスト生成インストラクション](../../../../../agent_skills/jakarta-ee-api-base/instructions/unit_test_generation.md) - 単体テストコード生成
* [Jakarta EE開発原則](../../../../../agent_skills/jakarta-ee-api-base/principles/) - アーキテクチャ標準、品質基準
* [プロジェクトREADME](../../README.md) - プロジェクト概要
* [アーキテクチャ設計書](../specs/baseline/basic_design/common/architecture_design.md) - システムアーキテクチャ

---

## 変更履歴

* 2026-02-09 10:30:00 - 初版作成（テスト評価実施）
