package pro.kensait.berrybooks.architecture;

import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import jakarta.ws.rs.Path;
import jakarta.persistence.Entity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * 命名規則を検証するテストクラス
 * 
 * 検証内容:
 * - Resourceクラスのサフィックス
 * - Serviceクラスのサフィックス
 * - DAOクラスのサフィックス
 * - Entityクラスのパッケージ配置
 * - DTOクラスのサフィックス
 */
@AnalyzeClasses(packages = "pro.kensait.berrybooks")
public class NamingConventionTest {

    /**
     * REST APIクラスは "*Resource" で終わること
     */
    @ArchTest
    static final ArchRule resource_classes_should_be_suffixed = 
        classes()
            .that().resideInAPackage("..api..")
            .and().areAnnotatedWith(Path.class)
            .should().haveSimpleNameEndingWith("Resource")
            .because("REST APIクラスは 'Resource' サフィックスを使用する");
    
    /**
     * サービスクラスは "*Service" で終わること
     * (インターフェース、例外、TO、テスト、モデルクラスは除外)
     */
    @ArchTest
    static final ArchRule service_classes_should_be_suffixed = 
        classes()
            .that().resideInAPackage("..service..")
            .and().areNotInterfaces()
            .and().areNotEnums()
            .and().areNotMemberClasses()
            .and().haveSimpleNameContaining("Service")
            .and().haveSimpleNameNotEndingWith("Test")
            .should().haveSimpleNameEndingWith("Service")
            .because("サービス実装クラスは 'Service' サフィックスを使用する");
    
    /**
     * サービスインターフェースは "*ServiceIF" で終わること
     */
    @ArchTest
    static final ArchRule service_interfaces_should_be_suffixed = 
        classes()
            .that().resideInAPackage("..service..")
            .and().areInterfaces()
            .should().haveSimpleNameEndingWith("ServiceIF")
            .because("サービスインターフェースは 'ServiceIF' サフィックスを使用する");
    
    /**
     * DAOクラスは "*Dao" で終わること
     */
    @ArchTest
    static final ArchRule dao_classes_should_be_suffixed = 
        classes()
            .that().resideInAPackage("..dao..")
            .should().haveSimpleNameEndingWith("Dao")
            .because("DAOクラスは 'Dao' サフィックスを使用する");
    
    /**
     * Entityクラスは entity パッケージに配置すること
     */
    @ArchTest
    static final ArchRule entity_classes_should_reside_in_entity_package = 
        classes()
            .that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..entity..")
            .because("Entityクラスは entity パッケージに配置する");
    
    /**
     * API DTOクラスは "*Request" または "*Response" で終わること
     */
    @ArchTest
    static final ArchRule api_dto_classes_should_be_suffixed = 
        classes()
            .that().resideInAPackage("..api.dto..")
            .and().areNotInterfaces()
            .and().areNotEnums()
            .should().haveSimpleNameEndingWith("Request")
            .orShould().haveSimpleNameEndingWith("Response")
            .orShould().haveSimpleNameEndingWith("ErrorResponse")
            .because("API DTOクラスは 'Request' または 'Response' サフィックスを使用する");
    
    /**
     * External DTOクラスは "*TO", "*Request" または "*Response" で終わること
     * (内部クラスは除外)
     */
    @ArchTest
    static final ArchRule external_dto_classes_should_be_suffixed = 
        classes()
            .that().resideInAPackage("..external.dto..")
            .and().areNotInterfaces()
            .and().areNotEnums()
            .and().areNotMemberClasses()
            .should().haveSimpleNameEndingWith("TO")
            .orShould().haveSimpleNameEndingWith("Request")
            .orShould().haveSimpleNameEndingWith("Response")
            .because("External DTOクラスは 'TO'、'Request' または 'Response' サフィックスを使用する");
}
