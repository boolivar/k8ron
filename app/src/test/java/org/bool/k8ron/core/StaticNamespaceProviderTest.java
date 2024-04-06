package org.bool.k8ron.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class StaticNamespaceProviderTest {

    @Test
    void testDefaultNamespace() {
        assertThat(new StaticNamespaceProvider().getNamespace())
            .isEqualTo("default");
    }

    @ValueSource(strings = { "default", "_all" })
    @ParameterizedTest
    void testNamespace(String namespace) {
        assertThat(new StaticNamespaceProvider(namespace).getNamespace())
            .isEqualTo(namespace);
    }
}
