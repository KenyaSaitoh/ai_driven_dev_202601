package pro.kensait.backoffice.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.backoffice.entity.Publisher;

import java.util.List;

/**
 * 出版社データアクセスオブジェクト
 * 
 * 出版社情報のCRUD操作を提供する
 */
@ApplicationScoped
public class PublisherDao {

    private static final Logger logger = LoggerFactory.getLogger(PublisherDao.class);

    @PersistenceContext
    private EntityManager em;

    /**
     * 全出版社を取得
     * 
     * @return 出版社エンティティのリスト（publisherId昇順）
     */
    public List<Publisher> findAll() {
        logger.debug("[ PublisherDao#findAll ]");
        
        String jpql = "SELECT p FROM Publisher p ORDER BY p.publisherId";
        
        TypedQuery<Publisher> query = em.createQuery(jpql, Publisher.class);
        List<Publisher> publishers = query.getResultList();
        
        logger.debug("[ PublisherDao#findAll ] Found {} publishers", publishers.size());
        return publishers;
    }

    /**
     * 出版社IDで出版社を取得
     * 
     * @param publisherId 出版社ID
     * @return 出版社エンティティ、存在しない場合はnull
     */
    public Publisher findById(Integer publisherId) {
        logger.debug("[ PublisherDao#findById ] publisherId: {}", publisherId);
        return em.find(Publisher.class, publisherId);
    }

    /**
     * 出版社情報を保存（INSERT）
     * 
     * @param publisher 出版社エンティティ
     */
    public void insert(Publisher publisher) {
        logger.info("[ PublisherDao#insert ] publisherName: {}", publisher.getPublisherName());
        em.persist(publisher);
    }

    /**
     * 出版社情報を更新（UPDATE）
     * 
     * @param publisher 出版社エンティティ
     * @return 更新後の出版社エンティティ
     */
    public Publisher update(Publisher publisher) {
        logger.info("[ PublisherDao#update ] publisherId: {}", publisher.getPublisherId());
        return em.merge(publisher);
    }

    /**
     * 出版社情報を削除（DELETE）
     * 
     * @param publisherId 出版社ID
     */
    public void delete(Integer publisherId) {
        logger.info("[ PublisherDao#delete ] publisherId: {}", publisherId);
        Publisher publisher = findById(publisherId);
        if (publisher != null) {
            em.remove(publisher);
        }
    }
}
