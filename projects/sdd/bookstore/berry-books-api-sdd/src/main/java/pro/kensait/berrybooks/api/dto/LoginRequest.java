package pro.kensait.berrybooks.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * ログインリクエストDTO
 * 
 * Java Recordを使用してイミュータブルなDTOを実装
 */
public record LoginRequest(
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    String email,
    
    @NotBlank(message = "パスワードは必須です")
    String password
) {
}

