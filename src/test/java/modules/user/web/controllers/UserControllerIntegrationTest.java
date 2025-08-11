package modules.user.web.controllers;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import modules.user.core.usecases.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestHTTPEndpoint(UserController.class)
public class UserControllerIntegrationTest {

    private static final String ALICE_UUID = "eb4123a3-b722-4798-9af5-8957f823657a";
    private static final String ADMIN_UUID = "af134cab-f41c-4675-b141-205f975db679";

    @Inject
    UserRepository userRepository;

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    private User alice;
    private User admin;

    @BeforeEach
    @Transactional
    void setUp() {
        userRepository.findAll().forEach(user -> userRepository.deleteById(user.getKeycloakUserId()));

        alice = UserImpl.builder()
                .keycloakUserId(UUID.fromString(ALICE_UUID))
                .firstName("Alice").lastName("Silverstone").username("alice").email("asilverstone@test.com")
                .build();
        admin = UserImpl.builder()
                .keycloakUserId(UUID.fromString(ADMIN_UUID))
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
        .when()
                .get("/{userId}")
        .then()
                .statusCode(200)
                .body("username", equalTo(alice.getUsername()))
                .body("email", equalTo(alice.getEmail()));
    }

    @Test
    void testAdminCanAccessOthersInformation() {
        given()
                .auth().oauth2(getAccessToken(admin.getUsername()))
                .pathParam("userId", alice.getKeycloakUserId())
        .when()
                .get("/{userId}")
        .then()
                .statusCode(200)
                .body("username", equalTo(alice.getUsername()));
    }

    @Test
    void testUserCannotAccessOthersInformation() {
        given()
                .auth().oauth2(getAccessToken(alice.getUsername()))
                .pathParam("userId", admin.getKeycloakUserId())
        .when()
                .get("/{userId}")
        .then()
                .statusCode(403);
    }

    @Test
    void testUserNotFound() {
        UUID nonExistentUserId = UUID.randomUUID();
        given()
                .auth().oauth2(getAccessToken(admin.getUsername()))
                .pathParam("userId", nonExistentUserId)
        .when()
                .get("/{userId}")
        .then()
                .statusCode(404);
    }
}