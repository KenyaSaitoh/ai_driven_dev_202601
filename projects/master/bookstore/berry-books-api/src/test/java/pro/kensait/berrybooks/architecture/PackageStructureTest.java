package pro.kensait.berrybooks.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * パッケージ構造とサイクル依存を検証するテストクラス
 * 
 * 検証内容:
 * - パッケージ間の依存関係
 * - サイクル依存の検出
 * - 禁止パッケージへのアクセス
 */
@AnalyzeClasses(packages = "pro.kensait.berrybooks")
public class PackageStructureTest {

    /**
     * DTOはEntityに依存してはならない
     */
    @ArchTest
    static final ArchRule dto_should_not_depend_on_entity = 
        noClasses()
            .that().resideInAPackage("..api.dto..")
            .should().dependOnClassesThat().resideInAPackage("..entity..")
            .because("DTOとEntityは分離されるべき");
    
    /**
     * EntityはAPIに依存してはならない
     */
    @ArchTest
    static final ArchRule entity_should_not_depend_on_api = 
        noClasses()
            .that().resideInAPackage("..entity..")
            .should().dependOnClassesThat().resideInAPackage("..api..")
            .because("Entityは独立したドメインモデルであるべき");
    
    /**
     * EntityはDTOに依存してはならない
     */
    @ArchTest
    static final ArchRule entity_should_not_depend_on_dto = 
        noClasses()
            .that().resideInAPackage("..entity..")
            .should().dependOnClassesThat().resideInAPackage("..dto..")
            .because("Entityは独立したドメインモデルであるべき");
    
    /**
     * DAOはAPIに依存してはならない
     */
    @ArchTest
    static final ArchRule dao_should_not_depend_on_api = 
        noClasses()
            .that().resideInAPackage("..dao..")
            .should().dependOnClassesThat().resideInAPackage("..api..")
            .because("DAOはAPI層に依存してはならない");
    
    /**
     * DAOはExternalに依存してはならない
     */
    @ArchTest
    static final ArchRule dao_should_not_depend_on_external = 
        noClasses()
            .that().resideInAPackage("..dao..")
            .should().dependOnClassesThat().resideInAPackage("..external..")
            .because("DAOは外部システム統合層に依存してはならない");
    
    /**
     * EntityはExternalに依存してはならない
     */
    @ArchTest
    static final ArchRule entity_should_not_depend_on_external = 
        noClasses()
            .that().resideInAPackage("..entity..")
            .should().dependOnClassesThat().resideInAPackage("..external..")
            .because("Entityは外部システム統合層に依存してはならない");
    
    /**
     * API DTOはExternal DTOに依存してはならない
     */
    @ArchTest
    static final ArchRule api_dto_should_not_depend_on_external_dto = 
        noClasses()
            .that().resideInAPackage("..api.dto..")
            .should().dependOnClassesThat().resideInAPackage("..external.dto..")
            .because("API DTOとExternal DTOは分離されるべき");
    
    /**
     * サイクル依存が存在してはならない
     */
    @ArchTest
    static final ArchRule no_cycles_by_method_calls_between_slices = 
        slices()
            .matching("pro.kensait.berrybooks.(*)..")
            .should().beFreeOfCycles()
            .because("サイクル依存は複雑性を増加させる");
}
