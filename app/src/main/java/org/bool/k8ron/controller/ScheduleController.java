package org.bool.k8ron.controller;

import org.bool.k8ron.service.ScheduleService;

import lombok.RequiredArgsConstructor;
import org.quartz.TriggerKey;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collector;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService schedulerService;

    @GetMapping("/schedule")
    public Mono<Map<String, List<String>>> getSchedule() {
        return toResponse(schedulerService.retrieveKeys());
    }

    @GetMapping("/schedule/{namespace}")
    public Mono<Map<String, List<String>>> getSchedule(@PathVariable String namespace) {
        return toResponse(schedulerService.retrieveKeys(namespace));
    }

    @GetMapping("/schedule/{namespace}/{name}")
    public Mono<Map<String, List<String>>> getSchedule(@PathVariable String namespace, @PathVariable String name) {
        return toResponse(schedulerService.retrieveKeys(namespace, name));
    }

    private Mono<Map<String, List<String>>> toResponse(Flux<TriggerKey> triggerKeys) {
        return triggerKeys.collect(toMap()).subscribeOn(Schedulers.boundedElastic());
    }

    private Collector<TriggerKey, ?, Map<String, List<String>>> toMap() {
        return groupingBy(TriggerKey::getGroup, TreeMap::new, mapping(TriggerKey::getName, toList()));
    }
}
