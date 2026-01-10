package pro.kensait.backoffice.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * 社員エンティティ
 * 
 * テーブル: EMPLOYEE
 */
@Entity
@Table(name = "EMPLOYEE")
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMPLOYEE_ID")
    private Long employeeId;

    @Column(name = "EMPLOYEE_CODE", unique = true, length = 20)
    private String employeeCode;

    @Column(name = "EMPLOYEE_NAME", length = 100)
    private String employeeName;

    @Column(name = "EMAIL", length = 100)
    private String email;

    @Column(name = "PASSWORD", length = 100)
    private String password;

    @Column(name = "JOB_RANK")
    private Integer jobRank;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "DEPARTMENT_ID")
    private Department department;

    // デフォルトコンストラクタ（JPA必須）
    public Employee() {
    }

    // コンストラクタ
    public Employee(Long employeeId, String employeeCode, String employeeName, String email, 
                    String password, Integer jobRank, Department department) {
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

    // equals/hashCode（主キーベース）
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(employeeId, employee.employeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId);
    }

    // toString
    @Override
    public String toString() {
        return "Employee{" +
                "employeeId=" + employeeId +
                ", employeeCode='" + employeeCode + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", email='" + email + '\'' +
                ", jobRank=" + jobRank +
                ", departmentId=" + (department != null ? department.getDepartmentId() : null) +
                '}';
    }
}
