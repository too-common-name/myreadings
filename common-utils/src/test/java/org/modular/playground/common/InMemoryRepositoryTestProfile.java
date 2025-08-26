package org.modular.playground.common;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class InMemoryRepositoryTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("app.repository.type", "in-memory");
    }
}