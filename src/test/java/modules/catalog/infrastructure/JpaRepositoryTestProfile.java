package modules.catalog.infrastructure;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class JpaRepositoryTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("app.book.repository.type", "jpa");
    }
}