package pro.kensait.backoffice.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 社員エンティティ
 * EMPLOYEE テーブルに対応
 */
@Entity
@Table(name = "EMPLOYEE")
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    // 社員ID
    @Id
    @Column(name = "EMPLOYEE_ID")
    private Long employeeId;

    // 社員コード
    @Column(name = "EMPLOYEE_CODE")
    private String employeeCode;

    // 社員名
    @Column(name = "EMPLOYEE_NAME")
    private String employeeName;

    // メールアドレス
    @Column(name = "EMAIL")
    private String email;

    // パスワード（BCrypt）
    @Column(name = "PASSWORD")
    private String password;

    // 役職ランク (1=ASSOCIATE, 2=MANAGER, 3=DIRECTOR)
    @Column(name = "JOB_RANK")
    private Integer jobRank;

    // 部署
    @ManyToOne(targetEntity = Department.class)
    @JoinColumn(name = "DEPARTMENT_ID", referencedColumnName = "DEPARTMENT_ID")
    private Department department;

    // デフォルトコンストラクタ
    public Employee() {
    }

    // コンストラクタ
    public Employee(Long employeeId, String employeeCode, String employeeName, 
                   String email, String password, Integer jobRank, Department department) {
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.employeeName = employeeName;
        this.email = email;
        this.password = password;
        this.jobRank = jobRank;
        this.department = department;
    }

    // Getter/Setter
    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getJobRank() {
        return jobRank;
    }

    public void setJobRank(Integer jobRank) {
        this.jobRank = jobRank;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "Employee [employeeId=" + employeeId + ", employeeCode=" + employeeCode 
                + ", employeeName=" + employeeName + ", jobRank=" + jobRank + "]";
    }
}


