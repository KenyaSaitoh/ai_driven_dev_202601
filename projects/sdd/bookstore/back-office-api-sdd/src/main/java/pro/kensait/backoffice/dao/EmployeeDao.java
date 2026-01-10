package pro.kensait.backoffice.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pro.kensait.backoffice.entity.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 社員データアクセスオブジェクト
 * 
 * 社員情報のCRUD操作を提供する
 */
@ApplicationScoped
public class EmployeeDao {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeDao.class);

    @PersistenceContext
    private EntityManager em;

    /**
     * 社員IDで社員情報を取得
     * 
     * @param employeeId 社員ID
     * @return 社員エンティティ、存在しない場合はnull
     */
    public Employee findById(Long employeeId) {
        logger.debug("findById called with employeeId: {}", employeeId);
        return em.find(Employee.class, employeeId);
    }

    /**
     * 社員コードで社員情報を取得（ログイン時に使用）
     * 
     * 部署情報も同時に取得（JOIN FETCH）
     * 
     * @param employeeCode 社員コード
     * @return 社員エンティティ、存在しない場合はnull
     */
    public Employee findByCode(String employeeCode) {
        logger.debug("findByCode called with employeeCode: {}", employeeCode);
        
        String jpql = "SELECT e FROM Employee e " +
                     "JOIN FETCH e.department " +
                     "WHERE e.employeeCode = :employeeCode";
        
        TypedQuery<Employee> query = em.createQuery(jpql, Employee.class);
        query.setParameter("employeeCode", employeeCode);
        
        try {
            Employee employee = query.getSingleResult();
            logger.debug("Employee found: {}", employee.getEmployeeCode());
            return employee;
        } catch (NoResultException e) {
            logger.debug("Employee not found with code: {}", employeeCode);
            return null;
        }
    }

    /**
     * 社員情報を保存（INSERT）
     * 
     * @param employee 社員エンティティ
     */
    public void insert(Employee employee) {
        logger.info("Inserting employee: {}", employee.getEmployeeCode());
        em.persist(employee);
    }

    /**
     * 社員情報を更新（UPDATE）
     * 
     * @param employee 社員エンティティ
     * @return 更新後の社員エンティティ
     */
    public Employee update(Employee employee) {
        logger.info("Updating employee: {}", employee.getEmployeeCode());
        return em.merge(employee);
    }

    /**
     * 社員情報を削除（DELETE）
     * 
     * @param employeeId 社員ID
     */
    public void delete(Long employeeId) {
        logger.info("Deleting employee with ID: {}", employeeId);
        Employee employee = findById(employeeId);
        if (employee != null) {
            em.remove(employee);
        }
    }
}
