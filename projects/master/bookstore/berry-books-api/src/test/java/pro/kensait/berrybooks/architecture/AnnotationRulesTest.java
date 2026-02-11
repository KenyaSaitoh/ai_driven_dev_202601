package pro.kensait.berrybooks.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import jakarta.ws.rs.Path;
import jakarta.persistence.Entity;
import jakarta.enterprise.context.ApplicationScoped;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * アノテーション使用ルールを検証するテストクラス
 * 
 * 検証内容:
 * - JAX-RS アノテーション（@Path）
 * - JPA アノテーション（@Entity）
 * - CDI アノテーション（@ApplicationScoped）
 */
@AnalyzeClasses(packages = "pro.kensait.berrybooks")
public class AnnotationRulesTest {

    /**
     * REST APIクラスは @Path アノテーションを持つこと
     */
    @ArchTest
    static final ArchRule resource_classes_should_be_annotated_with_path = 
        classes()
            .that().resideInAPackage("..api..")
            .and().haveSimpleNameEndingWith("Resource")
            .should().beAnnotatedWith(Path.class)
            .because("REST APIクラスは @Path アノテーションを持つ");
    
    /**
     * Entityクラスは @Entity アノテーションを持つこと
     */
    @ArchTest
    static final ArchRule entity_classes_should_be_annotated_with_entity = 
        classes()
            .that().resideInAPackage("..entity..")
            .and().areNotInterfaces()
            .and().areNotEnums()
            .and().haveSimpleNameNotEndingWith("PK")
            .should().beAnnotatedWith(Entity.class)
            .because("Entityクラスは @Entity アノテーションを持つ");
    
    /**
     * サービスクラスは @ApplicationScoped アノテーションを持つこと
     */
    @ArchTest
    static final ArchRule service_classes_should_be_annotated_with_application_scoped = 
        classes()
            .that().resideInAPackage("..service..")
            .and().haveSimpleNameEndingWith("Service")
            .and().areNotInterfaces()
            .should().beAnnotatedWith(ApplicationScoped.class)
            .because("サービスクラスは @ApplicationScoped アノテーションを持つ");
    
    /**
     * DAOクラスは @ApplicationScoped アノテーションを持つこと
     */
    @ArchTest
    static final ArchRule dao_classes_should_be_annotated_with_application_scoped = 
        classes()
            .that().resideInAPackage("..dao..")
            .and().haveSimpleNameEndingWith("Dao")
            .should().beAnnotatedWith(ApplicationScoped.class)
            .because("DAOクラスは @ApplicationScoped アノテーションを持つ");
    
    /**
     * REST APIクラス（Resource）は @ApplicationScoped アノテーションを持つこと
     */
    @ArchTest
    static final ArchRule resource_classes_should_be_annotated_with_application_scoped = 
        classes()
            .that().resideInAPackage("..api..")
            .and().haveSimpleNameEndingWith("Resource")
            .should().beAnnotatedWith(ApplicationScoped.class)
            .because("REST APIクラスは @ApplicationScoped アノテーションを持つ");
    
    /**
     * External Clientクラスは @ApplicationScoped アノテーションを持つこと
     */
    @ArchTest
    static final ArchRule external_client_classes_should_be_annotated_with_application_scoped = 
        classes()
            .that().resideInAPackage("..external..")
            .and().haveSimpleNameEndingWith("RestClient")
            .should().beAnnotatedWith(ApplicationScoped.class)
            .because("External Clientクラスは @ApplicationScoped アノテーションを持つ");
}
