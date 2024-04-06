package org.bool.k8ron.core;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KubernetesClientNamespaceProviderTest {

    @Mock
    private KubernetesClient k8sClient;

    @InjectMocks
    private KubernetesClientNamespaceProvider namespaceProvider;

    @Test
    void testNamespace() {
        given(k8sClient.getNamespace())
            .willReturn("ns");
        assertThat(namespaceProvider.getNamespace())
            .isEqualTo("ns");
    }
}
