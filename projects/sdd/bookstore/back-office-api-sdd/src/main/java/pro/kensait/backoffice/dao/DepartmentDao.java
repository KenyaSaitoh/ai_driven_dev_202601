package pro.kensait.backoffice.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pro.kensait.backoffice.entity.Department;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 部署データアクセスオブジェクト
 * 
 * 部署情報のCRUD操作を提供する
 */
@ApplicationScoped
public class DepartmentDao {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentDao.class);

    @PersistenceContext
    private EntityManager em;

    /**
     * 部署IDで部署情報を取得
     * 
     * @param departmentId 部署ID
     * @return 部署エンティティ、存在しない場合はnull
     */
    public Department findById(Long departmentId) {
        logger.debug("findById called with departmentId: {}", departmentId);
        return em.find(Department.class, departmentId);
    }

    /**
     * 部署情報を保存（INSERT）
     * 
     * @param department 部署エンティティ
     */
    public void insert(Department department) {
        logger.info("Inserting department: {}", department.getDepartmentName());
        em.persist(department);
    }

    /**
     * 部署情報を更新（UPDATE）
     * 
     * @param department 部署エンティティ
     * @return 更新後の部署エンティティ
     */
    public Department update(Department department) {
        logger.info("Updating department: {}", department.getDepartmentName());
        return em.merge(department);
    }

    /**
     * 部署情報を削除（DELETE）
     * 
     * @param departmentId 部署ID
     */
    public void delete(Long departmentId) {
        logger.info("Deleting department with ID: {}", departmentId);
        Department department = findById(departmentId);
        if (department != null) {
            em.remove(department);
        }
    }
}
