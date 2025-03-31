package modules.user.web.controllers;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import jakarta.inject.Inject;
import modules.user.domain.User;
import modules.user.domain.UserImpl;
import modules.user.usecases.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
@TestHTTPEndpoint(UserController.class)
public class UserControllerIntegrationTest {

    @Inject
    UserServiceImpl userService;
    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    // TODO: This info should be fetched from config/quarkus-realm.json
    private final User alice = new UserImpl.UserBuilder()
    .userId(UUID.fromString( "eb4123a3-b722-4798-9af5-8957f823657a"))
    .firstName("Alice")
    .lastName("Silverstone")
    .username("alice")
    .email("asilverstone@test.com")
    .build();
    private final User admin = new UserImpl.UserBuilder()
    .userId(UUID.fromString("af134cab-f41c-4675-b141-205f975db679"))
    .firstName("Bruce")
    .lastName("Wayne")
    .username("admin")
    .email("bwayne@test.com")
    .build();

    @BeforeEach
    void setUp() {
        userService.createUserProfile(alice);
        userService.createUserProfile(admin);
    }

    @Test
    void testUserCanAccessHisInformations() {
        given()
                .auth().oauth2(getAccessToken(alice.getUsername()))
                .pathParam("userId", alice.getUserId().toString())
                .when().get("/{userId}")
                .then()
                .statusCode(200)
                .body("userId", equalTo(alice.getUserId().toString()))
                .body("firstName", equalTo(alice.getFirstName()))
                .body("lastName", equalTo(alice.getLastName()))
                .body("username", equalTo(alice.getUsername()))
                .body("email", equalTo(alice.getEmail()));
    }

    @Test
    void testAdminCanAccessOthersInformations() {
        given()
                .auth().oauth2(getAccessToken(admin.getUsername()))
                .pathParam("userId", alice.getUserId())
                .when().get("/{userId}")
                .then()
                .statusCode(200)
                .body("userId", equalTo(alice.getUserId().toString()))
                .body("firstName", equalTo(alice.getFirstName()))
                .body("lastName", equalTo(alice.getLastName()))
                .body("username", equalTo(alice.getUsername()))
                .body("email", equalTo(alice.getEmail()));
    }

    @Test
    void testUserCannotAccessOthersInformations() {
        given()
                .auth().oauth2(getAccessToken(alice.getUsername()))
                .pathParam("userId", admin.getUserId())
                .when().get("/{userId}")
                .then()
                .statusCode(403);
    }

    @Test
    void testUserNotFound() {
        given()
                .auth().oauth2(getAccessToken(admin.getUsername()))
                .pathParam("userId", UUID.randomUUID())
                .when().get("/{userId}")
                .then()
                .statusCode(404);
    }


    protected String getAccessToken(String userName) {
        return keycloakClient.getAccessToken(userName);
    }
}