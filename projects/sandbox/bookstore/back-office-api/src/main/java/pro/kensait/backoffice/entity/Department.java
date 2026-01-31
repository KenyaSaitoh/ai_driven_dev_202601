package pro.kensait.backoffice.entity;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * 部署エンティティ
 * DEPARTMENT テーブルに対応
 */
@Entity
@Table(name = "DEPARTMENT")
public class Department implements Serializable {
    private static final long serialVersionUID = 1L;

    // 部署ID
    @Id
    @Column(name = "DEPARTMENT_ID")
    private Long departmentId;

    // 部署名
    @Column(name = "DEPARTMENT_NAME")
    private String departmentName;

    // 社員リスト（双方向関係）
    @OneToMany(mappedBy = "department")
    private List<Employee> employees;

    // デフォルトコンストラクタ
    public Department() {
    }

    // コンストラクタ
    public Department(Long departmentId, String departmentName) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }

    // Getter/Setter
    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    @Override
    public String toString() {
        return "Department [departmentId=" + departmentId + ", departmentName=" + departmentName + "]";
    }
}


