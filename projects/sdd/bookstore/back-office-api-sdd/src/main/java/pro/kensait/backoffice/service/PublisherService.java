package pro.kensait.backoffice.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.kensait.backoffice.api.dto.PublisherTO;
import pro.kensait.backoffice.dao.PublisherDao;
import pro.kensait.backoffice.entity.Publisher;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 出版社サービス
 * 
 * 出版社管理のビジネスロジックを提供する
 */
@ApplicationScoped
@Transactional
public class PublisherService {

    private static final Logger logger = LoggerFactory.getLogger(PublisherService.class);

    @Inject
    private PublisherDao publisherDao;

    /**
     * 全出版社を取得
     * 
     * @return 出版社TOのリスト（publisherId昇順）
     */
    public List<PublisherTO> getAllPublishers() {
        logger.info("[ PublisherService#getAllPublishers ]");
        
        List<Publisher> publishers = publisherDao.findAll();
        
        return publishers.stream()
                .map(this::convertToTO)
                .collect(Collectors.toList());
    }

    /**
     * 出版社IDで出版社を取得
     * 
     * @param publisherId 出版社ID
     * @return 出版社TO、存在しない場合はnull
     */
    public PublisherTO getPublisherById(Integer publisherId) {
        logger.info("[ PublisherService#getPublisherById ] publisherId: {}", publisherId);
        
        Publisher publisher = publisherDao.findById(publisherId);
        
        if (publisher == null) {
            logger.warn("[ PublisherService#getPublisherById ] Publisher not found: {}", publisherId);
            return null;
        }
        
        logger.info("[ PublisherService#getPublisherById ] Success: publisherId={}", publisherId);
        return convertToTO(publisher);
    }

    /**
     * PublisherエンティティをPublisherTOに変換
     * 
     * @param publisher Publisherエンティティ
     * @return PublisherTO
     */
    private PublisherTO convertToTO(Publisher publisher) {
        if (publisher == null) {
            return null;
        }
        
        return new PublisherTO(
            publisher.getPublisherId(),
            publisher.getPublisherName()
        );
    }
}
