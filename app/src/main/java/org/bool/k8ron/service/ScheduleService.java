package org.bool.k8ron.service;

import org.bool.k8ron.core.Quartz;

import lombok.RequiredArgsConstructor;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final Quartz quartz;

    public Mono<Long> schedule(Flux<Trigger> triggers) {
        return quartz.unschedule(retrieveKeys()).then(quartz.schedule(triggers));
    }

    public Flux<TriggerKey> retrieveKeys() {
        return quartz.retrieveKeys(GroupMatcher.groupContains("/"));
    }

    public Flux<TriggerKey> retrieveKeys(String namespace) {
        return quartz.retrieveKeys(GroupMatcher.groupStartsWith(namespace + "/"));
    }

    public Flux<TriggerKey> retrieveKeys(String namespace, String name) {
        return quartz.retrieveKeys(GroupMatcher.groupEquals(namespace + "/" + name));
    }
}
