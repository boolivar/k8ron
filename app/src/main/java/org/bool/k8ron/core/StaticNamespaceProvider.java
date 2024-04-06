package org.bool.k8ron.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
public class StaticNamespaceProvider implements NamespaceProvider {

    private final String namespace;

    @Autowired
    public StaticNamespaceProvider() {
        this("default");
    }
}
