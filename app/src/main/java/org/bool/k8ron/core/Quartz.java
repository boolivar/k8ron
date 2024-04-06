package org.bool.k8ron.core;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Quartz {

    private final Scheduler scheduler;

    private final ScaleJobFactory jobFactory;

    public Flux<TriggerKey> retrieveKeys(GroupMatcher<TriggerKey> groupMatcher) {
        return Flux.defer(() -> triggerKeys(groupMatcher));
    }

    public Mono<Long> schedule(Flux<Trigger> triggers) {
        return triggers.collect(Collectors.groupingBy(Trigger::getKey, Collectors.toSet()))
                .flatMapIterable(Map::entrySet)
                .doOnNext(this::schedule).count();
    }

    public Mono<Boolean> unschedule(Flux<TriggerKey> keys) {
        return keys.collectList().map(this::unschedule);
    }

    @SneakyThrows
    private Flux<TriggerKey> triggerKeys(GroupMatcher<TriggerKey> groupMatcher) {
        return Flux.fromIterable(scheduler.getTriggerKeys(groupMatcher));
    }

    @SneakyThrows
    private void schedule(Map.Entry<TriggerKey, Set<Trigger>> triggers) {
        var jobDetail = jobFactory.createJobDetail(triggers.getKey());
        scheduler.scheduleJob(jobDetail, triggers.getValue(), false);
    }

    @SneakyThrows
    private boolean unschedule(List<TriggerKey> triggerKeys) {
        return scheduler.unscheduleJobs(triggerKeys);
    }
}
