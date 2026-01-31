package pro.kensait.backoffice.service.publisher;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.kensait.backoffice.dao.PublisherDao;
import pro.kensait.backoffice.entity.Publisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

// 出版社を取得するサービスクラス
@ApplicationScoped
public class PublisherService {
    private static final Logger logger = LoggerFactory.getLogger(PublisherService.class);

    @Inject
    private PublisherDao publisherDao;

    // サービスメソッド：出版社の取得（全件検索）
    public List<Publisher> getPublishersAll() {
        logger.info("[ PublisherService#getPublishersAll ]");
        return publisherDao.findAll();
    }
}

