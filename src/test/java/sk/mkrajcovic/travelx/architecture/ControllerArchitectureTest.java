package sk.mkrajcovic.travelx.architecture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static sk.mkrajcovic.travelx.architecture.ArchitectureTestUtil.TRAVELX_CLASSES;
import static sk.mkrajcovic.travelx.architecture.ArchitectureTestUtil.createNoFieldInjectionRuleFor;
import static sk.mkrajcovic.travelx.architecture.ArchitectureTestUtil.createRuleForClassesWhithNameEndingAndAnnotations;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import com.tngtech.archunit.library.Architectures;

class ControllerArchitectureTest {

	private final String controllerLayer = "Controller";
	private final String controllerPackages = "..controller..";

	@Test
	@DisplayName("Controller vrstva nesmie byť používaná inými vrstvami")
	void checkControllerLayerAccessibility() {
		var layeredArchitecture = Architectures.layeredArchitecture()
				.consideringAllDependencies()
				.layer(controllerLayer).definedBy(controllerPackages)
				.whereLayer(controllerLayer).mayNotBeAccessedByAnyLayer();

		assertDoesNotThrow(() -> layeredArchitecture.check(TRAVELX_CLASSES));
	}

	@Test
	@DisplayName("Triedy v controller vrstve musia mať názov končiaci na 'Controller'")
	void checkControllerLayerNamingConvention() {
		var archRule = createRuleForClassesWhithNameEndingAndAnnotations(
				controllerPackages,
				controllerLayer,
				Set.of(Controller.class, RestController.class)
		);
		assertDoesNotThrow(() -> archRule.check(TRAVELX_CLASSES));;
	}

	@Test
	@DisplayName("Controller vrstva by nemala používať field injection")
	void controllersShouldNotUseFieldInjection() {
		var archRule = createNoFieldInjectionRuleFor(controllerPackages);
		assertDoesNotThrow(() -> archRule.check(TRAVELX_CLASSES));
	}

}
