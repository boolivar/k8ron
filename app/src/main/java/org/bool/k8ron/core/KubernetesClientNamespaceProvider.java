package org.bool.k8ron.core;

import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
@Primary
@Component
@RequiredArgsConstructor
public class KubernetesClientNamespaceProvider implements NamespaceProvider {

    private final KubernetesClient k8sClient;

    @Override
    public String getNamespace() {
        return k8sClient.getNamespace();
    }
}
