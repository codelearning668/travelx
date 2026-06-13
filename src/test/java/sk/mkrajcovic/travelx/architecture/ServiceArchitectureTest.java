package sk.mkrajcovic.travelx.architecture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static sk.mkrajcovic.travelx.architecture.ArchitectureTestUtil.TRAVELX_CLASSES;
import static sk.mkrajcovic.travelx.architecture.ArchitectureTestUtil.createNoFieldInjectionRuleFor;
import static sk.mkrajcovic.travelx.architecture.ArchitectureTestUtil.createRuleForClassesWithNameEnding;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

class ServiceArchitectureTest {

	private final String serviceLayer = "Service";
	private final String servicePackages = "..service..";

	@Test
	@DisplayName("Triedy v service vrstve by mali mať názov končiaci na 'Service'")
	void serviceNamingConvention() {
		var archRule = createRuleForClassesWithNameEnding(servicePackages, serviceLayer);
	    assertDoesNotThrow(() -> archRule.check(TRAVELX_CLASSES));
	}

	@Test
	@DisplayName("Service vrstva nesmie závisieť od controller vrstvy")
	void servicesShouldNotDependOnControllers() {
	    var archRrule = ArchRuleDefinition.noClasses()
	            .that().resideInAPackage(servicePackages)
	            .should().dependOnClassesThat().resideInAPackage("..controller..");

	    assertDoesNotThrow(() -> archRrule.check(TRAVELX_CLASSES));
	}

	@Test
	@DisplayName("Service vrstva by nemala používať field injection")
	void servicesShouldNotUseFieldInjection() {
		var archRule = createNoFieldInjectionRuleFor(servicePackages);
	    assertDoesNotThrow(() -> archRule.check(TRAVELX_CLASSES));
	}

	@Test
	@DisplayName("Service triedy by nemali obsahovať stav okrem závislostí")
	void servicesShouldBeStateless() {
	    var archRule = ArchRuleDefinition.fields()
	            .that().areDeclaredInClassesThat().resideInAPackage(servicePackages)
	            .should().beFinal();

	    assertDoesNotThrow(() -> archRule.check(TRAVELX_CLASSES));
	}
}
