package org.bool.k8ron;

import org.bool.k8ron.service.ScheduleService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quartz.TriggerKey.triggerKey;

@SpringBootTest(properties = "spring.quartz.auto-startup = false")
class K8ronApplicationTest {

    @Autowired
    private ContextRefresher contextRefresher;

    @Autowired
    private ConfigurableEnvironment env;

    @Autowired
    private ScheduleService scheduleService;

    @DirtiesContext
    @Test
    void testProperties() throws IOException {
        assertThat(scheduleService.retrieveKeys().toIterable())
            .isEmpty();

        new YamlPropertySourceLoader().load("k8ron-config", new ClassPathResource("/k8ron-config.yml"))
            .forEach(env.getPropertySources()::addLast);

        contextRefresher.refresh();

        assertThat(scheduleService.retrieveKeys().toIterable())
            .containsOnly(
                triggerKey("on", "awesome/application"),
                triggerKey("off", "awesome/application"),
                triggerKey("up", "some/k8ron"),
                triggerKey("down", "some/k8ron")
            );
    }
}
