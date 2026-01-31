package pro.kensait.backoffice.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * ログインリクエストDTO（社員用）
 */
public record LoginRequest(
    @NotBlank(message = "社員コードは必須です")
    String employeeCode,
    
    @NotBlank(message = "パスワードは必須です")
    String password
) {
}

