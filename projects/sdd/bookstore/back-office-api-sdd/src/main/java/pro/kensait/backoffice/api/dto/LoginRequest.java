package pro.kensait.backoffice.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ログインリクエストDTO
 * 
 * 社員コードとパスワードによるログインリクエスト
 */
public record LoginRequest(
    @NotBlank(message = "社員コードは必須です")
    @Size(max = 20, message = "社員コードは20文字以内で入力してください")
    String employeeCode,
    
    @NotBlank(message = "パスワードは必須です")
    @Size(max = 100, message = "パスワードは100文字以内で入力してください")
    String password
) {}
