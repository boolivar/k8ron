package org.bool.k8ron.core;

import org.bool.k8ron.service.ScheduleService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class K8ronInitializer implements ApplicationListener<RefreshScopeRefreshedEvent> {

    private final JobSchedule schedule;

    private final ScheduleService scheduleService;

    @Override
    public void onApplicationEvent(RefreshScopeRefreshedEvent event) {
        scheduleJobs();
    }

    @PostConstruct
    public void scheduleJobs() {
        scheduleService.schedule(schedule.triggers()).block();
    }
}
