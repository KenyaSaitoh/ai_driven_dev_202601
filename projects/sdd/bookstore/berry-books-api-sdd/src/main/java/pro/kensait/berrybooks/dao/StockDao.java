package pro.kensait.berrybooks.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import pro.kensait.berrybooks.entity.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 在庫データアクセスクラス
 * 
 * 楽観的ロック制御を実装
 */
@ApplicationScoped
public class StockDao {
    
    private static final Logger logger = LoggerFactory.getLogger(StockDao.class);
    
    @PersistenceContext(unitName = "BerryBooksPU")
    private EntityManager em;
    
    /**
     * 書籍IDで在庫を検索
     * 
     * @param bookId 書籍ID
     * @return Stock（見つからない場合はnull）
     */
    public Stock findByBookId(Integer bookId) {
        logger.info("[ StockDao#findByBookId ] bookId={}", bookId);
        return em.find(Stock.class, bookId);
    }
    
    /**
     * 在庫を更新（楽観的ロック）
     * 
     * @param bookId 書籍ID
     * @param quantity 減算数量
     * @param version バージョン番号
     * @return 更新件数（0の場合は楽観的ロック競合）
     * @throws OptimisticLockException バージョン不一致の場合
     */
    public int updateStock(Integer bookId, Integer quantity, Long version) {
        logger.info("[ StockDao#updateStock ] bookId={}, quantity={}, version={}", 
                   bookId, quantity, version);
        
        Query query = em.createQuery(
            "UPDATE Stock s SET s.quantity = s.quantity - :quantity " +
            "WHERE s.bookId = :bookId AND s.version = :version"
        );
        query.setParameter("bookId", bookId);
        query.setParameter("quantity", quantity);
        query.setParameter("version", version);
        
        int updatedCount = query.executeUpdate();
        
        if (updatedCount == 0) {
            logger.warn("[ StockDao#updateStock ] Optimistic lock conflict: bookId={}, version={}", 
                       bookId, version);
            throw new OptimisticLockException(
                "他のユーザーが購入済みです。最新の在庫情報を確認してください。"
            );
        }
        
        logger.info("[ StockDao#updateStock ] Stock updated successfully: updatedCount={}", updatedCount);
        return updatedCount;
    }
}

