package pro.kensait.backoffice.api.dto;

/**
 * ログインレスポンスDTO
 * 
 * ログイン成功時に返却する社員情報
 */
public record LoginResponse(
    Long employeeId,
    String employeeCode,
    String employeeName,
    String email,
    Integer jobRank,
    Long departmentId,
    String departmentName
) {}
