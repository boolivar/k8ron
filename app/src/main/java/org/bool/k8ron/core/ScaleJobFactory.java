package org.bool.k8ron.core;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.TimeZone;

@Component
@RequiredArgsConstructor
public class ScaleJobFactory {

    @Setter
    @Accessors(fluent = true, chain = true)
    public class TriggerBuilder {

        private String namespace;

        @NonNull
        private String name;

        private String key;

        private int replicas;

        private String cron;

        private String timeZone;

        public Trigger build() {
            var group = groupName(namespace, name);
            return org.quartz.TriggerBuilder.newTrigger()
                    .forJob(JobKey.jobKey(jobName, group))
                    .withIdentity(TriggerKey.triggerKey(key, group))
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron)
                            .inTimeZone(timeZone != null ? TimeZone.getTimeZone(timeZone) : null))
                    .usingJobData("replicas", replicas)
                    .build();
        }
    }

    private final NamespaceProvider namespaceProvider;

    private final String jobName;

    @Autowired
    public ScaleJobFactory(NamespaceProvider namespaceProvider) {
        this(namespaceProvider, ScaleJob.class.getSimpleName());
    }

    public TriggerBuilder newTrigger() {
        return new TriggerBuilder();
    }

    public JobDetail createJobDetail(TriggerKey triggerKey) {
        return createJobDetail(triggerKey.getGroup());
    }

    public JobDetail createJobDetail(String namespace, String name) {
        return createJobDetail(groupName(namespace, name));
    }

    public JobDetail createJobDetail(String group) {
        return JobBuilder.newJob(ScaleJob.class)
                .withIdentity(jobName, group)
                .build();
    }

    private String groupName(String namespace, String name) {
        return (namespace != null ? namespace : namespaceProvider.getNamespace()) + "/" + name;
    }
}
