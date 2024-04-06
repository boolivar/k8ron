package org.bool.k8ron.core;

import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@Slf4j
@ToString
@RequiredArgsConstructor
public class ScaleJob implements Job {

    @ToString.Exclude
    private final KubernetesClient k8sClient;

    @Getter
    @Setter
    private int replicas;

    @Override
    public void execute(JobExecutionContext context) {
        var group = context.getJobDetail().getKey().getGroup();
        var parts = StringUtils.split(group, '/');
        var uid = k8sClient.apps().deployments()
                .inNamespace(parts[0]).withName(parts[1]).scale(replicas).getMetadata().getUid();
        log.info("Deployment {} uid={} scaled to {}", group, uid, replicas);
    }
}
