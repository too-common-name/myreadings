package modules.review.web.controllers;

import common.TransactionalTestHelper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import modules.catalog.core.domain.Book;
import modules.catalog.utils.CatalogTestUtils;
import modules.review.core.domain.Review;
import modules.review.utils.ReviewTestUtils;
import modules.review.web.dto.ReviewRequestDTO;
import modules.user.core.domain.User;
import modules.user.utils.UserTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(ReviewController.class)
public class ReviewControllerIntegrationTest {

    @Inject
    TransactionalTestHelper helper;

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    private static final UUID ALICE_UUID = UUID.fromString("eb4123a3-b722-4798-9af5-8957f823657a");
    private static final UUID ADMIN_UUID = UUID.fromString("af134cab-f41c-4675-b141-205f975db679");
    private static final UUID JDOE_UUID = UUID.fromString("1eed6a8e-a853-4597-b4c6-c4c2533546a0");

    private User alice;
    private User admin;
    private User jdoe;
    private Book createdBook;
    private Book anotherBook;
    private Review aliceReview;

    @BeforeEach
    void setUp() {
        alice = helper.saveUser(UserTestUtils.createValidUserWithIdAndUsername(ALICE_UUID, "alice"));
        admin = helper.saveUser(UserTestUtils.createValidUserWithIdAndUsername(ADMIN_UUID, "admin"));
        jdoe = helper.saveUser(UserTestUtils.createValidUserWithIdAndUsername(JDOE_UUID, "jdoe"));
        
        createdBook = helper.saveBook(CatalogTestUtils.createValidBook());
        anotherBook = helper.saveBook(CatalogTestUtils.createValidBook());

        aliceReview = helper.saveReview(ReviewTestUtils.createValidReviewForUserAndBook(alice.getKeycloakUserId(),
                createdBook.getBookId(), "Excellent book!", 5));
    }

    @AfterEach
    void tearDown() {
        helper.deleteReview(aliceReview.getReviewId());
        helper.deleteBook(createdBook.getBookId());
        helper.deleteBook(anotherBook.getBookId());
        helper.deleteUser(alice.getKeycloakUserId());
        helper.deleteUser(admin.getKeycloakUserId());
        helper.deleteUser(jdoe.getKeycloakUserId());
    }

    protected String getAccessToken(String userName) {
        return keycloakClient.getAccessToken(userName);
    }

    @Test
    void testUserCanCreateReview() {
        ReviewRequestDTO requestBody = ReviewRequestDTO.builder()
                .bookId(anotherBook.getBookId())
                .rating(5)
                .reviewText("Great!")
                .build();

        String newReviewId = given()
            .auth().oauth2(getAccessToken(alice.getUsername()))
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
        .when()
            .post()
        .then()
            .statusCode(201)
            .body("rating", is(5))
            .body("reviewText", equalTo("Great!"))
            .extract().path("reviewId");
        
        helper.deleteReview(UUID.fromString(newReviewId));
    }

    @Test
    void testUserCanUpdateOwnReview() {
        ReviewRequestDTO requestBody = ReviewRequestDTO.builder()
                .bookId(createdBook.getBookId())
                .rating(3)
                .reviewText("Updated review text.")
                .build();

        given()
            .auth().oauth2(getAccessToken(alice.getUsername()))
            .pathParam("reviewId", aliceReview.getReviewId())
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
        .when()
            .put("/{reviewId}")
        .then()
            .statusCode(200)
            .body("rating", is(3))
            .body("reviewText", equalTo("Updated review text."));
    }

    @Test
    void testUserCannotUpdateOthersReview() {
        ReviewRequestDTO requestBody = ReviewRequestDTO.builder()
                .bookId(createdBook.getBookId())
                .rating(2)
                .reviewText("Attempting to update.")
                .build();

        given()
            .auth().oauth2(getAccessToken(jdoe.getUsername()))
            .pathParam("reviewId", aliceReview.getReviewId())
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
        .when()
            .put("/{reviewId}")
        .then()
            .statusCode(403);
    }
    
    @Test
    void testAdminCanDeleteOthersReview() {
        given()
            .auth().oauth2(getAccessToken(admin.getUsername()))
            .pathParam("reviewId", aliceReview.getReviewId())
        .when()
            .delete("/{reviewId}")
        .then()
            .statusCode(204);
    }

    @Test
    void testUserCannotDeleteOthersReview() {
        given()
            .auth().oauth2(getAccessToken(jdoe.getUsername()))
            .pathParam("reviewId", aliceReview.getReviewId())
        .when()
            .delete("/{reviewId}")
        .then()
            .statusCode(403);
    }

    @Test
    void testGetReviewsByBookId() {
        given()
            .auth().oauth2(getAccessToken(alice.getUsername()))
            .pathParam("bookId", createdBook.getBookId())
        .when()
            .get("/books/{bookId}")
        .then()
            .statusCode(200)
            .body("size()", is(1))
            .body("[0].bookId", equalTo(createdBook.getBookId().toString()));
    }
}