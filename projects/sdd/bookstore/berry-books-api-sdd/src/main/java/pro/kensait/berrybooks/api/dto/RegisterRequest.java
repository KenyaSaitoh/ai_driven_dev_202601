package pro.kensait.berrybooks.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 新規登録リクエストDTO
 * 
 * Java Recordを使用してイミュータブルなDTOを実装
 */
public record RegisterRequest(
    @NotBlank(message = "顧客名は必須です")
    @Size(max = 30, message = "顧客名は30文字以内で入力してください")
    String customerName,
    
    @NotBlank(message = "パスワードは必須です")
    String password,
    
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    @Size(max = 30, message = "メールアドレスは30文字以内で入力してください")
    String email,
    
    LocalDate birthday,
    
    @Size(max = 120, message = "住所は120文字以内で入力してください")
    String address
) {
}

