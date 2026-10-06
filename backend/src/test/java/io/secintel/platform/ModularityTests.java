package io.secintel.platform;

import org.junit.jupiter.api.Test;

import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

	@Test
	void modulesRespectDeclaredBoundaries() {
		ApplicationModules.of(BackendApplication.class).verify();
	}

}
