package com.power4j.boot.autoconfigure.idempotentguard;

import com.power4j.idempotentguard.api.guard.AccessGuard;
import com.power4j.idempotentguard.api.guard.KeyEncoder;
import com.power4j.idempotentguard.api.handler.GuardExceptionTranslator;
import com.power4j.idempotentguard.spring.aspect.HoldResourceAspect;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class IdempotentGuardAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(IdempotentGuardAutoConfiguration.class));

	@Test
	void autoConfig_loadedByDefault_withoutEnabledProperty() {
		contextRunner.run(context -> assertThat(context).hasSingleBean(KeyEncoder.class));
	}

	@Test
	void autoConfig_notLoaded_whenDisabled() {
		contextRunner.withPropertyValues("idempotent-guard.enabled=false")
			.run(context -> assertThat(context).doesNotHaveBean(KeyEncoder.class));
	}

	@Test
	void autoConfig_lockExpire_injectedToAspect() {
		contextRunner.withPropertyValues("idempotent-guard.global-config.lock-expire=60")
			.withBean(GuardExceptionTranslator.class, () -> mock(GuardExceptionTranslator.class))
			.withBean(AccessGuard.class, () -> mock(AccessGuard.class))
			.run(context -> {
				assertThat(context).hasSingleBean(HoldResourceAspect.class);
				HoldResourceAspect aspect = context.getBean(HoldResourceAspect.class);
				assertThat(aspect.getDefaultExpire()).isEqualTo(Duration.ofSeconds(60));
			});
	}

}
