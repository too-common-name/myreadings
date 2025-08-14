package common;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import jakarta.inject.Inject;
import modules.user.core.domain.User;
import modules.user.utils.UserTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;

@QuarkusTest
public abstract class AbstractControllerIntegrationTest {

    @Inject
    protected TransactionalTestHelper helper;

    protected KeycloakTestClient keycloakClient = new KeycloakTestClient();

    protected static final UUID ALICE_UUID = UUID.fromString("eb4123a3-b722-4798-9af5-8957f823657a");
    protected static final UUID ADMIN_UUID = UUID.fromString("af134cab-f41c-4675-b141-205f975db679");

    protected User alice;
    protected User admin;

    @BeforeEach
    void setUpBase() {
        alice = helper.saveUser(UserTestUtils.createValidUserWithIdAndUsername(ALICE_UUID, "alice"));
        admin = helper.saveUser(UserTestUtils.createValidUserWithIdAndUsername(ADMIN_UUID, "admin"));
    }

    @AfterEach
    void tearDownBase() {
        helper.deleteUser(alice.getKeycloakUserId());
        helper.deleteUser(admin.getKeycloakUserId());
    }

    protected String getAccessToken(String userName) {
        return keycloakClient.getAccessToken(userName);
    }
}