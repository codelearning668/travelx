package sk.mkrajcovic.travelx.architecture;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption.Predefined;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

import jakarta.annotation.Resource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ArchitectureTestUtil {

	/**
	 * Import produkcnych tried pre ArchUnit testy.
	 * <p>
	 * Testovacie triedy su vylucene pomocou DO_NOT_INCLUDE_TESTS, aby sa zabranilo
	 * poruseniam pravidiel (napr. field injection).
	 * <p>
	 * V Spring Boot testoch sa bezne pouziva field injection, kedze constructor
	 * injection tam nie je implementovana ako default a zaroven nie je spolahlivo
	 * podporovana.<br>
	 * Preto testy nechceme zahrnut do analyzy.
	 */
	static final JavaClasses TRAVELX_CLASSES = new ClassFileImporter()
			.withImportOption(Predefined.DO_NOT_INCLUDE_TESTS)
			.importPackages("sk.mkrajcovic.travelx");

	static ArchRule createRuleForClassesWithNameEnding(String packages, String nameEnding) {
		return createRuleForClassesWhithNameEndingAndAnnotations(packages, nameEnding, Set.of());
	}

	static ArchRule createRuleForClassesWhithNameEndingAndAnnotations(String packages, String nameEnding, Set<Class<? extends Annotation>> annotations) {
		var archRuleConjuction = ArchRuleDefinition.classes()
			.that().resideInAPackage(packages);

		int annotationCnt = 1;
		for (var annotation : annotations) {
			archRuleConjuction = (annotationCnt == 1 ? archRuleConjuction.and() : archRuleConjuction.or()).areAnnotatedWith(annotation);
			++annotationCnt;
		}

		return archRuleConjuction
			.should().haveSimpleNameEndingWith(nameEnding);
	}

	static ArchRule createNoFieldInjectionRuleFor(String packages) {
		return ArchRuleDefinition.noFields()
			.that().areDeclaredInClassesThat().resideInAPackage(packages)
			.should().beAnnotatedWith(Resource.class)
			.orShould().beAnnotatedWith(Autowired.class)
			.orShould().beAnnotatedWith(Value.class);
	}

}
