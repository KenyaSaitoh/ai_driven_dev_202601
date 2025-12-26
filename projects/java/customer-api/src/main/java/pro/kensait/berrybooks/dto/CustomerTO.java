package pro.kensait.berrybooks.dto;

import java.time.LocalDate;

public record CustomerTO (
        // 顧客ID
        Integer customerId,
        // 顧客名
        String customerName,
        // パスワード
        String password,
        // メールアドレス
        String email,
        // 生年月日
        LocalDate birthday,
        // 住所
        String address) {
}

