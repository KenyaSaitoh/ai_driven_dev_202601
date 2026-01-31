package pro.kensait.backoffice.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.kensait.backoffice.entity.Stock;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

// 在庫テーブルへのアクセスを行うDAOクラス
@ApplicationScoped
public class StockDao {
    private static final Logger logger = LoggerFactory.getLogger(
            StockDao.class);

    @PersistenceContext(unitName = "bookstorePU")
    private EntityManager em;

    // DAOメソッド：在庫を主キーで検索（悲観的ロック）
    public Stock findByIdWithLock(Integer bookId) {
        logger.info("[ StockDao#findByIdWithLock ]");
        return em.find(Stock.class, bookId, LockModeType.PESSIMISTIC_WRITE);
    }

    // DAOメソッド：在庫を主キーで検索
    public Stock findById(Integer bookId) {
        logger.info("[ StockDao#findById ]");
        return em.find(Stock.class, bookId);
    }

    // DAOメソッド：在庫一覧取得
    public java.util.List<Stock> findAll() {
        logger.info("[ StockDao#findAll ]");
        return em.createQuery("SELECT s FROM Stock s ORDER BY s.bookId", Stock.class)
                .getResultList();
    }

    // DAOメソッド：在庫を更新
    public void update(Stock stock) {
        logger.info("[ StockDao#update ]");
        em.merge(stock);
    }
}


