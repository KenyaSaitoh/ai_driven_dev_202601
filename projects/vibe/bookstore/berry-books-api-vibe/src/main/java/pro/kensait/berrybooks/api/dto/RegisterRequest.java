package pro.kensait.berrybooks.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 新規登録リクエストDTO
 */
public record RegisterRequest(
    @NotBlank(message = "顧客名は必須です")
    @Size(max = 50, message = "顧客名は50文字以内で入力してください")
    String customerName,
    
    @NotBlank(message = "パスワードは必須です")
    @Size(min = 6, message = "パスワードは6文字以上で入力してください")
    String password,
    
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "正しいメールアドレス形式で入力してください")
    String email,
    
    @NotNull(message = "生年月日は必須です")
    LocalDate birthday,
    
    @NotBlank(message = "住所は必須です")
    @Size(max = 200, message = "住所は200文字以内で入力してください")
    String address
) {
}

