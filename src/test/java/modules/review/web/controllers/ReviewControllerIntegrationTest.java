package modules.review.web.controllers;

import common.AbstractControllerIntegrationTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
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
public class ReviewControllerIntegrationTest extends AbstractControllerIntegrationTest {

    private static final UUID JDOE_UUID = UUID.fromString("1eed6a8e-a853-4597-b4c6-c4c2533546a0");

    private User jdoe;
    private Book createdBook;
    private Book anotherBook;
    private Review aliceReview;

    @BeforeEach
    void setUp() {
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
        helper.deleteUser(jdoe.getKeycloakUserId());
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