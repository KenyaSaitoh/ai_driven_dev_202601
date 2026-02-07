package pro.kensait.berrybooks.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 結合テストの基底クラス
 * 
 * 責務:
 * * Weld SE（CDIコンテナ）の起動・停止
 * * WireMockServerの起動・停止
 * * EntityManagerの管理
 * * トランザクション管理（各テストでロールバック）
 */
@Tag("integration")
public abstract class BaseIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(BaseIntegrationTest.class);
    
    protected static WeldContainer container;
    protected static WireMockServer wireMockServer;
    protected static EntityManagerFactory emf;
    
    protected EntityManager em;
    
    @BeforeAll
    public static void setUpAll() {
        logger.info("[ BaseIntegrationTest#setUpAll ] Starting integration test environment");
        
        // Weld SE の起動（明示的にCDIを有効化）
        Weld weld = new Weld()
            .enableDiscovery()
            .addPackages(true, BaseIntegrationTest.class.getPackage());
        container = weld.initialize();
        logger.info("[ BaseIntegrationTest#setUpAll ] Weld SE container started");
        
        // WireMockServer の起動
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
        logger.info("[ BaseIntegrationTest#setUpAll ] WireMockServer started on port 8089");
        
        // EntityManagerFactory の作成
        emf = Persistence.createEntityManagerFactory("test-pu");
        logger.info("[ BaseIntegrationTest#setUpAll ] EntityManagerFactory created");
    }
    
    @AfterAll
    public static void tearDownAll() {
        logger.info("[ BaseIntegrationTest#tearDownAll ] Shutting down integration test environment");
        
        if (emf != null && emf.isOpen()) {
            emf.close();
            logger.info("[ BaseIntegrationTest#tearDownAll ] EntityManagerFactory closed");
        }
        
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            logger.info("[ BaseIntegrationTest#tearDownAll ] WireMockServer stopped");
        }
        
        if (container != null) {
            container.close();
            logger.info("[ BaseIntegrationTest#tearDownAll ] Weld SE container closed");
        }
    }
    
    @BeforeEach
    public void setUp() {
        logger.info("[ BaseIntegrationTest#setUp ] Setting up test");
        
        // EntityManager の取得
        em = emf.createEntityManager();
        
        // トランザクション開始
        em.getTransaction().begin();
        
        logger.info("[ BaseIntegrationTest#setUp ] EntityManager created, transaction started");
    }
    
    @AfterEach
    public void tearDown() {
        logger.info("[ BaseIntegrationTest#tearDown ] Tearing down test");
        
        // トランザクションのロールバック
        if (em != null && em.getTransaction().isActive()) {
            em.getTransaction().rollback();
            logger.info("[ BaseIntegrationTest#tearDown ] Transaction rolled back");
        }
        
        // EntityManager のクローズ
        if (em != null && em.isOpen()) {
            em.close();
            logger.info("[ BaseIntegrationTest#tearDown ] EntityManager closed");
        }
        
        // WireMock のリセット
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.resetAll();
            logger.info("[ BaseIntegrationTest#tearDown ] WireMock reset");
        }
    }
    
    /**
     * テストデータをDBに投入してフラッシュする
     */
    protected void persistAndFlush(Object entity) {
        em.persist(entity);
        em.flush();
    }
    
    /**
     * エンティティキャッシュをクリアする
     */
    protected void clearEntityCache() {
        em.clear();
    }
}
