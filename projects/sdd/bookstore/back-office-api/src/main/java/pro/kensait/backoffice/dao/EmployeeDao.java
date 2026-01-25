package pro.kensait.backoffice.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pro.kensait.backoffice.entity.Employee;

/**
 * 社員テーブルへのアクセスを行うDAOクラス
 */
@ApplicationScoped
public class EmployeeDao {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeDao.class);

    @PersistenceContext(unitName = "bookstorePU")
    private EntityManager em;

    /**
     * 社員を主キーで検索
     * @param employeeId 社員ID
     * @return 社員エンティティ
     */
    public Employee findById(Long employeeId) {
        logger.info("[ EmployeeDao#findById ] employeeId={}", employeeId);
        return em.find(Employee.class, employeeId);
    }

    /**
     * 社員コードで検索
     * @param employeeCode 社員コード
     * @return 社員エンティティ
     */
    public Employee findByCode(String employeeCode) {
        logger.info("[ EmployeeDao#findByCode ] employeeCode={}", employeeCode);
        
        TypedQuery<Employee> query = em.createQuery(
                "SELECT e FROM Employee e WHERE e.employeeCode = :employeeCode", Employee.class);
        query.setParameter("employeeCode", employeeCode);
        
        List<Employee> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 全社員を取得
     * @return 社員エンティティのリスト
     */
    public List<Employee> findAll() {
        logger.info("[ EmployeeDao#findAll ]");
        
        TypedQuery<Employee> query = em.createQuery(
                "SELECT e FROM Employee e ORDER BY e.employeeId", Employee.class);
        return query.getResultList();
    }
}


