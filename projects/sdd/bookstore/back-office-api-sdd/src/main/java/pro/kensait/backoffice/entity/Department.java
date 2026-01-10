package pro.kensait.backoffice.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 部署エンティティ
 * 
 * テーブル: DEPARTMENT
 */
@Entity
@Table(name = "DEPARTMENT")
public class Department implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DEPARTMENT_ID")
    private Long departmentId;

    @Column(name = "DEPARTMENT_NAME", length = 100)
    private String departmentName;

    @OneToMany(mappedBy = "department")
    private List<Employee> employees = new ArrayList<>();

    // デフォルトコンストラクタ（JPA必須）
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

    // equals/hashCode（主キーベース）
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return Objects.equals(departmentId, that.departmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(departmentId);
    }

    // toString
    @Override
    public String toString() {
        return "Department{" +
                "departmentId=" + departmentId +
                ", departmentName='" + departmentName + '\'' +
                '}';
    }
}
