package org.bool.k8ron.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.quartz.JobBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean.MethodInvokingJob;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class QuartzTest {

    @Mock
    private ScaleJobFactory jobFactory;
    
    @Mock
    private Scheduler scheduler;

    @InjectMocks
    private Quartz quartz;

    @Test
    void testTriggerKeys() throws SchedulerException {
        var executionThread = new AtomicReference<Thread>();
        var matcher = GroupMatcher.anyTriggerGroup();
        var output = Set.of(new TriggerKey("a", "b"), new TriggerKey("c", "d"));

        given(scheduler.getTriggerKeys(matcher))
            .willAnswer(new Answer<Set<TriggerKey>>() {
                @Override
                public Set<TriggerKey> answer(InvocationOnMock invocation) {
                    executionThread.set(Thread.currentThread());
                    return output;
                }
            });

        assertThat(quartz.retrieveKeys(matcher).toIterable())
            .containsAll(output);
        assertThat(executionThread)
            .hasValue(Thread.currentThread());
    }

    @Test
    void testSchedule() throws SchedulerException {
        var trigger = TriggerBuilder.newTrigger().build();
        var jobDetail = JobBuilder.newJob(MethodInvokingJob.class).build();

        given(jobFactory.createJobDetail(trigger.getKey().getGroup()))
            .willReturn(jobDetail);

        assertThat(quartz.schedule(Flux.just(trigger)).block())
            .isEqualTo(1);

        then(scheduler).should().scheduleJob(jobDetail, Set.of(trigger), false);
    }

    @Test
    void testUnschedule() throws SchedulerException {
        var triggerKey = new TriggerKey("name", "group");
        given(scheduler.unscheduleJobs(List.of(triggerKey)))
            .willReturn(true);
        assertThat(quartz.unschedule(Flux.just(triggerKey)).block())
            .isTrue();
    }
}
