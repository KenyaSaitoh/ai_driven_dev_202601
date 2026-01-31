package pro.kensait.backoffice.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pro.kensait.backoffice.entity.Department;

/**
 * 部署テーブルへのアクセスを行うDAOクラス
 */
@ApplicationScoped
public class DepartmentDao {
    private static final Logger logger = LoggerFactory.getLogger(DepartmentDao.class);

    @PersistenceContext(unitName = "bookstorePU")
    private EntityManager em;

    /**
     * 部署を主キーで検索
     * @param departmentId 部署ID
     * @return 部署エンティティ
     */
    public Department findById(Long departmentId) {
        logger.info("[ DepartmentDao#findById ] departmentId={}", departmentId);
        return em.find(Department.class, departmentId);
    }

    /**
     * 全部署を取得
     * @return 部署エンティティのリスト
     */
    public List<Department> findAll() {
        logger.info("[ DepartmentDao#findAll ]");
        
        TypedQuery<Department> query = em.createQuery(
                "SELECT d FROM Department d ORDER BY d.departmentId", Department.class);
        return query.getResultList();
    }
}


