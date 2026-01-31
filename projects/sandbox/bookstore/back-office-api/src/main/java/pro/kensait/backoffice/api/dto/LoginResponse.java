package pro.kensait.backoffice.api.dto;

/**
 * ログイン・社員情報レスポンスDTO
 */
public record LoginResponse(
    Long employeeId,
    String employeeCode,
    String employeeName,
    String email,
    Integer jobRank,  // 1=ASSOCIATE, 2=MANAGER, 3=DIRECTOR
    Long departmentId,
    String departmentName
) {
}

