package org.bool.k8ron.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties("k8ron.deployments")
public class K8ronDeploymentsProperties {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class K8ronDeploymentProperties {

        private String namespace;

        @Builder.Default
        private Map<String, K8ronScheduleProperties> schedule = new LinkedHashMap<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class K8ronScheduleProperties {

        private String cron;

        private String timeZone;

        private int replicas;
    }

    private String namespace;

    @Builder.Default
    private Map<String, K8ronDeploymentProperties> config = new LinkedHashMap<>();
}
