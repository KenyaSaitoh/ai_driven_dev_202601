package pro.kensait.backoffice.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.kensait.backoffice.entity.Publisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

// 出版社テーブルへのアクセスを行うDAOクラス
@ApplicationScoped
public class PublisherDao {
    private static final Logger logger = LoggerFactory.getLogger(PublisherDao.class);

    @PersistenceContext(unitName = "bookstorePU")
    private EntityManager em;

    // DAOメソッド：出版社を主キーで検索
    public Publisher findById(Integer publisherId) {
        logger.info("[ PublisherDao#findById ]");
        return em.find(Publisher.class, publisherId);
    }

    // DAOメソッド：全出版社を取得
    public List<Publisher> findAll() {
        logger.info("[ PublisherDao#findAll ]");
        
        TypedQuery<Publisher> query = em.createQuery(
                "SELECT p FROM Publisher p ORDER BY p.publisherId", Publisher.class);
        
        return query.getResultList();
    }
}

