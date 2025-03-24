package modules.user.web.controllers;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import modules.user.domain.User;
import modules.user.domain.UserImpl;
import modules.user.usecases.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class UserControllerIntegrationTest {

    @Inject
    UserServiceImpl userService;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        User testUser = new UserImpl.UserBuilder()
                .userId(UUID.randomUUID())
                .firstName("Integration")
                .lastName("Test")
                .username("integration_test")
                .email("integration@test.com")
                .build();
        userService.createUserProfile(testUser);
        testUserId = testUser.getUserId();
    }

    @Test
    void testGetUserByIdExistingIdShouldReturnOkAndUser() {
        given()
                .pathParam("userId", testUserId)
                .when().get("/api/v1/users/{userId}")
                .then()
                .statusCode(200)
                .body("userId", notNullValue())
                .body("firstName", equalTo("Integration"))
                .body("lastName", equalTo("Test"))
                .body("username", equalTo("integration_test"))
                .body("email", equalTo("integration@test.com"));
    }

    @Test
    void testGetUserByIdNonExistingIdShouldReturnNotFound() {
        UUID nonExistingId = UUID.randomUUID();
        given()
                .pathParam("userId", nonExistingId)
                .when().get("/api/v1/users/{userId}")
                .then()
                .statusCode(404);
    }
}