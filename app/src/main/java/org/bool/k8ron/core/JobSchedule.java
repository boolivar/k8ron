package org.bool.k8ron.core;

import org.bool.k8ron.config.K8ronDeploymentsProperties;
import org.bool.k8ron.config.K8ronDeploymentsProperties.K8ronDeploymentProperties;
import org.bool.k8ron.config.K8ronDeploymentsProperties.K8ronScheduleProperties;

import lombok.RequiredArgsConstructor;
import org.quartz.Trigger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JobSchedule {

    private final K8ronDeploymentsProperties properties;

    private final ScaleJobFactory jobFactory;

    public Flux<Trigger> triggers() {
        return Flux.fromIterable(properties.getConfig().entrySet())
                .flatMap(e -> triggers(e.getKey(), e.getValue(), properties.getNamespace()));
    }

    private Flux<Trigger> triggers(String name, K8ronDeploymentProperties deployment, String defaultNamespace) {
        return Flux.fromIterable(deployment.getSchedule().entrySet())
                .map(e -> createTrigger(name, e.getKey(), e.getValue(), Objects.toString(deployment.getNamespace(), defaultNamespace)));
    }

    private Trigger createTrigger(String name, String key, K8ronScheduleProperties schedule, String namespace) {
        return jobFactory.newTrigger()
                .namespace(namespace).name(name).key(key).replicas(schedule.getReplicas()).cron(schedule.getCron()).timeZone(schedule.getTimeZone())
                .build();
    }
}
