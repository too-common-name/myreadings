package modules.catalog.infrastructure;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class InMemoryRepositoryTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("app.book.repository.type", "in-memory");
    }
}