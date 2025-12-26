package pro.kensait.berrybooks.dao;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.kensait.berrybooks.entity.Customer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

// 顧客テーブルへのアクセスを行うDAOクラス
@ApplicationScoped
public class CustomerDao {
    private static final Logger logger = LoggerFactory.getLogger(
            CustomerDao.class);

    @PersistenceContext(unitName = "bookstorePU")
    private EntityManager em;

    // DAOメソッド：顧客を主キーで検索
    public Customer findById(Integer customerId) {
        logger.info("[ CustomerDao#findById ]");
        return em.find(Customer.class, customerId);
    }

    // DAOメソッド：顧客をメールアドレスで検索
    public Customer findCustomerByEmail(String email) {
        logger.info("[ CustomerDao#findCustomerByEmail ]");
        
        TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c WHERE c.email = :email", Customer.class);
        query.setParameter("email", email);
        
        List<Customer> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    // DAOメソッド：誕生日以降の顧客を検索
    public List<Customer> searchCustomersFromBirthday(LocalDate from) {
        logger.info("[ CustomerDao#searchCustomersFromBirthday ]");
        
        TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c WHERE :from <= c.birthday", Customer.class);
        query.setParameter("from", from);
        
        return query.getResultList();
    }

    // DAOメソッド：顧客を新規登録
    public void persist(Customer customer) {
        logger.info("[ CustomerDao#persist ]");
        em.persist(customer);
    }

    // DAOメソッド：顧客を更新
    public Customer merge(Customer customer) {
        logger.info("[ CustomerDao#merge ]");
        return em.merge(customer);
    }

    // DAOメソッド：顧客を削除
    public void remove(Customer customer) {
        logger.info("[ CustomerDao#remove ]");
        em.remove(customer);
    }

    // DAOメソッド：全顧客を取得
    public List<Customer> findAll() {
        logger.info("[ CustomerDao#findAll ]");
        
        TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c", Customer.class);
        
        return query.getResultList();
    }
}

