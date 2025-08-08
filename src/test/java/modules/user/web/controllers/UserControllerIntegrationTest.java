package modules.user.web.controllers;

import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import modules.user.core.usecases.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(UserController.class)
public class UserControllerIntegrationTest {

    @Inject
    UserRepository userRepository;

    @Inject
    @PersistenceUnit("users-db")
    EntityManager usersEntityManager;

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    private User alice;
    private User admin;

    @BeforeEach
    @Transactional
    void setUp() {
        alice = UserImpl.builder()
                .keycloakUserId(UUID.fromString("eb4123a3-b722-4798-9af5-8957f823657a"))
                .firstName("Alice").lastName("Silverstone").username("alice").email("asilverstone@test.com")
                .build();
        admin = UserImpl.builder()
                .keycloakUserId(UUID.fromString("af134cab-f41c-4675-b141-205f975db679"))
                .firstName("Bruce").lastName("Wayne").username("admin").email("bwayne@test.com")
                .build();
        userRepository.save(alice);
        userRepository.save(admin);
    }

    protected String getAccessToken(String userName) {
        return keycloakClient.getAccessToken(userName);
    }

    @Test
    void testUserCanAccessOwnInformation() {
        given()
                .auth().oauth2(getAccessToken(alice.getUsername()))
                .pathParam("userId", alice.getKeycloakUserId())
                .when().get("/{userId}")
                .then()
                .statusCode(200);
    }

    @Test
    void testAdminCanAccessOthersInformation() {
        given()
                .auth().oauth2(getAccessToken(admin.getUsername()))
                .pathParam("userId", alice.getKeycloakUserId())
                .when().get("/{userId}")
                .then()
                .statusCode(200);
    }

    @Test
    void testUserCannotAccessOthersInformation() {
        given()
                .auth().oauth2(getAccessToken(alice.getUsername()))
                .pathParam("userId", admin.getKeycloakUserId())
                .when().get("/{userId}")
                .then()
                .statusCode(403);
    }

    @Test
    void testUserNotFound() {
        UUID nonExistentUserId = UUID.randomUUID();
        given()
                .auth().oauth2(getAccessToken(admin.getUsername()))
                .pathParam("userId", nonExistentUserId)
                .when().get("/{userId}")
                .then()
                .statusCode(404);
    }
}