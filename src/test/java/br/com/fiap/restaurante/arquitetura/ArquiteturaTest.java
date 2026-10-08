package br.com.fiap.restaurante.arquitetura;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Regras de dependência entre as camadas, verificadas sobre o bytecode de
 * produção.
 *
 * <p>Transformam a Clean Architecture de afirmação do relatório em teste: se uma
 * classe do núcleo importar o Spring, o build falha. As regras aceitam camadas
 * ainda vazias porque o projeto é construído em fatias; sem essa permissão, o
 * ArchUnit reprovaria uma regra que ainda não tem classe para verificar.
 */
@DisplayName("Arquitetura")
class ArquiteturaTest {

    private static final String RAIZ = "br.com.fiap.restaurante";

    private static final JavaClasses CLASSES_DE_PRODUCAO = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(RAIZ);

    private static final String[] PACOTES_DE_FRAMEWORK = {
            "org.springframework..",
            "jakarta..",
            "org.hibernate..",
            "com.fasterxml.jackson..",
            "tools.jackson.."
    };

    @Test
    @DisplayName("ARQ-01 · domain e application não dependem de framework")
    void nucleoNaoDependeDeFramework() {
        /* arrange */
        ArchRule regra = noClasses()
                .that().resideInAnyPackage(RAIZ + ".domain..", RAIZ + ".application..")
                .should().dependOnClassesThat().resideInAnyPackage(PACOTES_DE_FRAMEWORK)
                .allowEmptyShould(true);

        /* act + assert */
        regra.check(CLASSES_DE_PRODUCAO);
    }

    @Test
    @DisplayName("ARQ-02 · domain, application e interfaceadapter não dependem de infrastructure")
    void nucleoNaoDependeDaInfraestrutura() {
        /* arrange */
        ArchRule regra = noClasses()
                .that().resideInAnyPackage(
                        RAIZ + ".domain..", RAIZ + ".application..", RAIZ + ".interfaceadapter..")
                .should().dependOnClassesThat().resideInAPackage(RAIZ + ".infrastructure..")
                .allowEmptyShould(true);

        /* act + assert */
        regra.check(CLASSES_DE_PRODUCAO);
    }

    @Test
    @DisplayName("ARQ-03 · as camadas respeitam a direção das dependências")
    void camadasRespeitamADirecaoDasDependencias() {
        /* arrange */
        ArchRule regra = layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .withOptionalLayers(true)
                .layer("Domínio").definedBy(RAIZ + ".domain..")
                .layer("Aplicação").definedBy(RAIZ + ".application..")
                .layer("Adaptadores").definedBy(RAIZ + ".interfaceadapter..")
                .layer("Infraestrutura").definedBy(RAIZ + ".infrastructure..")
                .whereLayer("Infraestrutura").mayNotBeAccessedByAnyLayer()
                .whereLayer("Adaptadores").mayOnlyBeAccessedByLayers("Infraestrutura")
                .whereLayer("Aplicação").mayOnlyBeAccessedByLayers("Adaptadores", "Infraestrutura")
                .whereLayer("Domínio").mayOnlyBeAccessedByLayers("Aplicação", "Adaptadores", "Infraestrutura")
                .allowEmptyShould(true);

        /* act + assert */
        regra.check(CLASSES_DE_PRODUCAO);
    }

    @Test
    @DisplayName("ARQ-04 · interfaceadapter não depende de framework")
    void adaptadoresNaoDependemDeFramework() {
        /* arrange */
        ArchRule regra = noClasses()
                .that().resideInAPackage(RAIZ + ".interfaceadapter..")
                .should().dependOnClassesThat().resideInAnyPackage(PACOTES_DE_FRAMEWORK)
                .allowEmptyShould(true);

        /* act + assert */
        regra.check(CLASSES_DE_PRODUCAO);
    }
}
