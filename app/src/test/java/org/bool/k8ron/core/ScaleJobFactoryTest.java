package org.bool.k8ron.core;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(Lifecycle.PER_CLASS)
class ScaleJobFactoryTest {

    private static final String JOB_NAME = "TestJob";

    private final ScaleJobFactory jobFactory = new ScaleJobFactory("test-ns"::toString, JOB_NAME);

    @Test
    void testDefaultJobName() {
        assertThat(new ScaleJobFactory(null))
            .hasFieldOrPropertyWithValue("jobName", "ScaleJob");
    }

    @Test
    void testTrigger() {
        assertThat(jobFactory.newTrigger().name("test").key("key").cron("0 0 0 ? * *").build())
            .returns(new TriggerKey("key", "test-ns/test"), Trigger::getKey)
            .returns(new JobKey(JOB_NAME, "test-ns/test"), Trigger::getJobKey)
            .extracting(Trigger::getJobDataMap, InstanceOfAssertFactories.MAP)
                .containsEntry("replicas", 0)
            ;
        assertThat(jobFactory.newTrigger()
                .namespace("namespace").name("deployment").key("trigger").replicas(20).cron("0 0 0 ? * *").build())
            .returns(new TriggerKey("trigger", "namespace/deployment"), Trigger::getKey)
            .returns(new JobKey(JOB_NAME, "namespace/deployment"), Trigger::getJobKey)
            .extracting(Trigger::getJobDataMap, InstanceOfAssertFactories.MAP)
                .containsEntry("replicas", 20)
            ;
    }

    @MethodSource
    @ParameterizedTest
    void testJobDetails(JobDetail jobDetail) {
        assertThat(jobDetail)
            .returns(false, JobDetail::isDurable)
            .returns(ScaleJob.class, JobDetail::getJobClass)
            .extracting(JobDetail::getKey)
                .returns("ns/name", JobKey::getGroup)
                .returns(JOB_NAME, JobKey::getName)
            ;
    }

    Stream<JobDetail> testJobDetails() {
        return Stream.of(
            jobFactory.createJobDetail("ns", "name"),
            jobFactory.createJobDetail("ns/name"),
            jobFactory.createJobDetail(TriggerKey.triggerKey("any", "ns/name"))
        );
    }
}
