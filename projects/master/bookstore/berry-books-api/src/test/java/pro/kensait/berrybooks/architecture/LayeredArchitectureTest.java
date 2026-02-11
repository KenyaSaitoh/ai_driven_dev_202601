package pro.kensait.berrybooks.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * レイヤードアーキテクチャの依存関係を検証するテストクラス
 * 
 * Jakarta EE 10の標準的なレイヤー構造を検証:
 * - API/Resource層（JAX-RS）
 * - Service層（ビジネスロジック）
 * - DAO層（データアクセス）
 * - Entity層（JPA エンティティ）
 */
@AnalyzeClasses(packages = "pro.kensait.berrybooks")
public class LayeredArchitectureTest {

    /**
     * レイヤー依存関係ルール
     * 
     * 依存関係:
     * - API層 → Service層、DTO、External、Security
     * - Service層 → DAO層、Entity、DTO、External
     * - DAO層 → Entity
     * - Entity層 → （独立）※Commonは許可
     * - DTO → （独立）
     * - External層 → （独立）※ExternalのDTOは許可
     * - Security層 → （独立）※ServiceとEntityは許可
     */
    @ArchTest
    static final ArchRule layer_dependencies_are_respected = layeredArchitecture()
        .consideringAllDependencies()
        
        // レイヤー定義
        .layer("API").definedBy("..api..")
        .layer("Service").definedBy("..service..")
        .layer("DAO").definedBy("..dao..")
        .layer("Entity").definedBy("..entity..")
        .layer("DTO").definedBy("..api.dto..")
        .layer("External").definedBy("..external..")
        .layer("Security").definedBy("..security..")
        
        // 依存関係ルール
        .whereLayer("API").mayNotBeAccessedByAnyLayer()
        .whereLayer("Service").mayOnlyBeAccessedByLayers("API", "Security")
        .whereLayer("DAO").mayOnlyBeAccessedByLayers("Service")
        .whereLayer("Entity").mayOnlyBeAccessedByLayers("DAO", "Service", "External");
    
    /**
     * DTOとEntityの分離ルール
     * DTOはEntityに依存してはならない
     */
    @ArchTest
    static final ArchRule dto_should_not_depend_on_entity = 
        com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
            .that().resideInAPackage("..api.dto..")
            .should().dependOnClassesThat().resideInAPackage("..entity..")
            .because("DTOとEntityは分離されるべき");
    
    /**
     * EntityはAPI層に依存してはならない
     */
    @ArchTest
    static final ArchRule entity_should_not_depend_on_api = 
        com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
            .that().resideInAPackage("..entity..")
            .should().dependOnClassesThat().resideInAPackage("..api..")
            .because("Entityは独立したドメインモデルであるべき");
}
