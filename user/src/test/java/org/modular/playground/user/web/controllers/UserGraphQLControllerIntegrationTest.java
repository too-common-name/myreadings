package org.modular.playground.user.web.controllers;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import jakarta.inject.Inject;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.domain.UserImpl;
import org.modular.playground.user.utils.UserRepositoryUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
public class UserGraphQLControllerIntegrationTest {

    private static final String ALICE_UUID = "eb4123a3-b722-4798-9af5-8957f823657a";
    private static final String ADMIN_UUID = "af134cab-f41c-4675-b141-205f975db679";

    @Inject
    UserRepositoryUtils userRepositoryUtils;

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    private User alice;
    private User admin;

    @BeforeEach
    void setUp() {
        alice = UserImpl.builder()
                .keycloakUserId(UUID.fromString(ALICE_UUID))
                .firstName("Alice").lastName("Silverstone").username("alice").email("asilverstone@test.com")
                .build();
        admin = UserImpl.builder()
                .keycloakUserId(UUID.fromString(ADMIN_UUID))
                .firstName("Bruce").lastName("Wayne").username("admin").email("bwayne@test.com")
                .build();
        userRepositoryUtils.saveUser(alice);
        userRepositoryUtils.saveUser(admin);
    }

    @AfterEach
    void tearDown() {
        userRepositoryUtils.findAllUsers().forEach(user -> userRepositoryUtils.deleteUser(user.getKeycloakUserId()));
    }


    protected String getAccessToken(String userName) {
        return keycloakClient.getAccessToken(userName);
    }

    @Test
    void testUserCanAccessOwnInformation() {
        String query = String.format("""
                {
                  "query": "query { userById(userId: \\"%s\\") { userId username email } }"
                }
                """, ALICE_UUID);

        given()
                .auth().oauth2(getAccessToken("alice"))
                .contentType("application/json")
                .body(query)
        .when()
                .post("/graphql")
        .then()
                .statusCode(200)
                .body("data.userById.username", equalTo("alice"))
                .body("data.userById.email", equalTo("asilverstone@test.com"));
    }

    @Test
    void testAdminCanAccessOthersInformation() {
        String query = String.format("""
                {
                  "query": "query { userById(userId: \\"%s\\") { userId username } }"
                }
                """, ALICE_UUID);

        given()
                .auth().oauth2(getAccessToken("admin"))
                .contentType("application/json")
                .body(query)
        .when()
                .post("/graphql")
        .then()
                .statusCode(200)
                .body("data.userById.username", equalTo("alice"));
    }

    @Test
    void testUserCannotAccessOthersInformation() {
        String query = String.format("""
                {
                  "query": "query { userById(userId: \\"%s\\") { userId username } }"
                }
                """, ADMIN_UUID);

        given()
                .auth().oauth2(getAccessToken("alice"))
                .contentType("application/json")
                .body(query)
        .when()
                .post("/graphql")
        .then()
                .statusCode(200)
                .body("data.userById", nullValue())
                .body("errors", notNullValue());
    }

    @Test
    void testUserNotFound() {
        String nonExistentUuid = UUID.randomUUID().toString();
        String query = String.format("""
                {
                  "query": "query { userById(userId: \\"%s\\") { userId } }"
                }
                """, nonExistentUuid);

        given()
                .auth().oauth2(getAccessToken("admin"))
                .contentType("application/json")
                .body(query)
        .when()
                .post("/graphql")
        .then()
                .statusCode(200)
                .body("data.userById", nullValue())
                .body("errors", nullValue());
    }
}