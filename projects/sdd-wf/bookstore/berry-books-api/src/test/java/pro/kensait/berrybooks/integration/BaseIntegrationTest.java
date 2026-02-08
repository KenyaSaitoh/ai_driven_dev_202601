package pro.kensait.berrybooks.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 結合テストのベースクラス
 * 
 * このクラスは以下を提供する:
 * - Weld SE（CDIコンテナ）の起動・停止
 * - WireMockServer（外部APIモック）の起動・停止
 * - EntityManagerFactoryの作成・破棄
 * - テストごとのEntityManagerとトランザクション管理
 * 
 * @Tag("integration")を付与し、単体テストと分離
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
        
        // Weld SE の起動（enableDiscovery()で明示的にCDIを有効化）
        // pro.kensait.berrybooks パッケージ全体をスキャン対象にする
        Weld weld = new Weld()
            .enableDiscovery()
            .addPackages(true, pro.kensait.berrybooks.integration.BaseIntegrationTest.class);
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
        em = emf.createEntityManager();
        em.getTransaction().begin();
        logger.debug("[ BaseIntegrationTest#setUp ] EntityManager created and transaction started");
    }
    
    @AfterEach
    public void tearDown() {
        if (em != null && em.getTransaction().isActive()) {
            em.getTransaction().rollback();
            logger.debug("[ BaseIntegrationTest#tearDown ] Transaction rolled back");
        }
        if (em != null && em.isOpen()) {
            em.close();
            logger.debug("[ BaseIntegrationTest#tearDown ] EntityManager closed");
        }
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.resetAll();
            logger.debug("[ BaseIntegrationTest#tearDown ] WireMock stubs reset");
        }
    }
    
    /**
     * エンティティを永続化してフラッシュする
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