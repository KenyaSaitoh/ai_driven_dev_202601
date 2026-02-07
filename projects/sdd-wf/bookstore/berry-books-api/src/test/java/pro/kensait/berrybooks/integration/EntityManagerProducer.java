package pro.kensait.berrybooks.integration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EntityManager Producer for Integration Tests
 * 
 * Weld SEでEntityManagerをCDI経由で注入できるようにする
 */
@ApplicationScoped
public class EntityManagerProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(EntityManagerProducer.class);
    
    private static EntityManagerFactory emf;
    
    static {
        emf = Persistence.createEntityManagerFactory("test-pu");
        logger.info("[ EntityManagerProducer ] EntityManagerFactory created for test-pu");
    }
    
    @Produces
    @RequestScoped
    @PersistenceContext(unitName = "BerryBooksPU")
    public EntityManager createEntityManager() {
        logger.debug("[ EntityManagerProducer#createEntityManager ] Creating EntityManager");
        return emf.createEntityManager();
    }
    
    public void closeEntityManager(@Disposes EntityManager em) {
        if (em != null && em.isOpen()) {
            logger.debug("[ EntityManagerProducer#closeEntityManager ] Closing EntityManager");
            em.close();
        }
    }
    
    public static void closeEntityManagerFactory() {
        if (emf != null && emf.isOpen()) {
            logger.info("[ EntityManagerProducer ] Closing EntityManagerFactory");
            emf.close();
        }
    }
}
