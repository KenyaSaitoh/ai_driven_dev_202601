package pro.kensait.berrybooks.external.dto;

import java.time.LocalDate;

/**
 * CustomerTO - 顧客情報転送オブジェクト
 * 
 * 責務:
 * - customer-hub-apiとの顧客情報のやり取り
 * - イミュータブルなデータ転送オブジェクト
 * 
 * 構造種別:
 * - Java Record（イミュータブル）
 * 
 * フィールド:
 * - customerId: 顧客ID
 * - customerName: 顧客名
 * - password: パスワード（BCryptハッシュ化済み）
 * - email: メールアドレス
 * - birthday: 生年月日
 * - address: 住所
 */
public record CustomerTO(
    Integer customerId,
    String customerName,
    String password,
    String email,
    LocalDate birthday,
    String address
) {}
