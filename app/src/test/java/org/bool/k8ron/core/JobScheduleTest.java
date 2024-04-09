package org.bool.k8ron.core;

import org.bool.k8ron.config.K8ronDeploymentsProperties;
import org.bool.k8ron.config.K8ronDeploymentsProperties.K8ronDeploymentProperties;
import org.bool.k8ron.config.K8ronDeploymentsProperties.K8ronScheduleProperties;

import org.junit.jupiter.api.Test;
import org.quartz.CronTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.util.Map;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class JobScheduleTest {

    private final K8ronDeploymentsProperties properties = new K8ronDeploymentsProperties();

    private final ScaleJobFactory jobFactory = new ScaleJobFactory("testNs"::toString, "testJob");

    private final JobSchedule jobSchedule = new JobSchedule(properties, jobFactory);

    @Test
    void testEmptySchedule() {
        assertThat(jobSchedule.triggers().toIterable())
            .isEmpty();
    }

    @Test
    void testTrigger() {
        var app = K8ronDeploymentProperties.builder()
                .schedule(Map.of("x", K8ronScheduleProperties.builder().timeZone("America/Los_Angeles").replicas(39).cron("* * * ? * *").build()))
                .build();
        properties.setConfig(Map.of("app", app));

        assertThat(jobSchedule.triggers().toIterable())
            .singleElement()
                .isInstanceOf(CronTrigger.class)
                .hasFieldOrPropertyWithValue("timeZone", TimeZone.getTimeZone("America/Los_Angeles"))
                .hasFieldOrPropertyWithValue("cronExpression", "* * * ? * *")
                .extracting(Trigger::getKey).isEqualTo(new TriggerKey("x", "testNs/app"));
    }

    @Test
    void testTriggers() {
        var app = K8ronDeploymentProperties.builder()
                .schedule(Map.of("on", K8ronScheduleProperties.builder().cron("1 * * ? * *").build()))
                .build();
        var service = K8ronDeploymentProperties.builder()
                .namespace("svc")
                .schedule(Map.of("on", K8ronScheduleProperties.builder().cron("2 * * ? * *").build()))
                .build();
        properties.setNamespace("root");
        properties.setConfig(Map.of("app", app, "service", service));

        assertThat(jobSchedule.triggers().toIterable())
            .hasSize(2)
            .allSatisfy(trigger -> assertThat(trigger).isInstanceOf(CronTrigger.class))
            .anySatisfy(trigger -> assertThat(trigger)
                    .hasFieldOrPropertyWithValue("cronExpression", "1 * * ? * *")
                    .extracting(Trigger::getKey).isEqualTo(new TriggerKey("on", "root/app")))
            .anySatisfy(trigger -> assertThat(trigger)
                    .hasFieldOrPropertyWithValue("cronExpression", "2 * * ? * *")
                    .extracting(Trigger::getKey).isEqualTo(new TriggerKey("on", "svc/service")));
    }
}
