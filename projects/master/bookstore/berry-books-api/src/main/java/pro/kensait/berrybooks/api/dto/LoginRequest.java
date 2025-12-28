package pro.kensait.berrybooks.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * ログインリクエストDTO
 */
public record LoginRequest(
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "正しいメールアドレス形式で入力してください")
    String email,
    
    @NotBlank(message = "パスワードは必須です")
    String password
) {
}

