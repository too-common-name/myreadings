package org.modular.playground.review.web.controllers;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.utils.CatalogRepositoryUtils;
import org.modular.playground.catalog.utils.CatalogTestUtils;
import org.modular.playground.review.core.domain.Review;
import org.modular.playground.review.utils.ReviewRepositoryUtils;
import org.modular.playground.review.utils.ReviewTestUtils;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.utils.UserRepositoryUtils;
import org.modular.playground.user.utils.UserTestUtils;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class ReviewGraphQLControllerIntegrationTest {

    @Inject
    UserRepositoryUtils userRepositoryUtils;

    @Inject
    CatalogRepositoryUtils bookRepositoryUtils;

    @Inject
    ReviewRepositoryUtils reviewRepositoryUtils;

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
    private String newReviewIdToDelete;

    @BeforeEach
    void setUp() {
        newReviewIdToDelete = null;
        alice = userRepositoryUtils.saveUser(UserTestUtils.createValidUserWithIdAndUsername(ALICE_UUID, "alice"));
        admin = userRepositoryUtils.saveUser(UserTestUtils.createValidUserWithIdAndUsername(ADMIN_UUID, "admin"));
        jdoe = userRepositoryUtils.saveUser(UserTestUtils.createValidUserWithIdAndUsername(JDOE_UUID, "jdoe"));

        createdBook = bookRepositoryUtils.saveBook(CatalogTestUtils.createValidBook());
        anotherBook = bookRepositoryUtils.saveBook(CatalogTestUtils.createValidBook());

        aliceReview = reviewRepositoryUtils.saveReview(ReviewTestUtils.createValidReviewForUserAndBook(alice.getKeycloakUserId(),
                createdBook.getBookId(), "Excellent book!", 5));
    }

    @AfterEach
    void tearDown() {
        if (newReviewIdToDelete != null) {
            reviewRepositoryUtils.deleteReview(UUID.fromString(newReviewIdToDelete));
        }
        reviewRepositoryUtils.deleteReview(aliceReview.getReviewId());
        bookRepositoryUtils.deleteBook(createdBook.getBookId());
        bookRepositoryUtils.deleteBook(anotherBook.getBookId());
        userRepositoryUtils.deleteUser(alice.getKeycloakUserId());
        userRepositoryUtils.deleteUser(admin.getKeycloakUserId());
        userRepositoryUtils.deleteUser(jdoe.getKeycloakUserId());
    }

    protected String getAccessToken(String userName) {
        return keycloakClient.getAccessToken(userName);
    }

    @Test
    void testUserCanCreateReview() {
        String body = String.format("""
                {
                  "query": "mutation { createReview(review: { bookId: \\"%s\\", rating: 5, reviewText: \\"Great!\\" }) { reviewId rating reviewText } }"
                }
                """, anotherBook.getBookId());

        newReviewIdToDelete = given()
            .auth().oauth2(getAccessToken(alice.getUsername()))
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.createReview.rating", is(5))
            .body("data.createReview.reviewText", equalTo("Great!"))
            .extract().path("data.createReview.reviewId");
    }

    @Test
    void testUserCanUpdateOwnReview() {
        String body = String.format("""
                {
                  "query": "mutation { updateReview(reviewId: \\"%s\\", updates: { bookId: \\"%s\\", rating: 3, reviewText: \\"Updated review text.\\" }) { rating reviewText } }"
                }
                """, aliceReview.getReviewId(), createdBook.getBookId());

        given()
            .auth().oauth2(getAccessToken(alice.getUsername()))
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.updateReview.rating", is(3))
            .body("data.updateReview.reviewText", equalTo("Updated review text."));
    }

    @Test
    void testUserCannotUpdateOthersReview() {
        String body = String.format("""
                {
                  "query": "mutation { updateReview(reviewId: \\"%s\\", updates: { bookId: \\"%s\\", rating: 2, reviewText: \\"Attempting to update.\\" }) { reviewId } }"
                }
                """, aliceReview.getReviewId(), createdBook.getBookId());

        given()
            .auth().oauth2(getAccessToken(jdoe.getUsername()))
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("errors[0].extensions.classification", equalTo("DataFetchingException"));
    }

    @Test
    void testAdminCanDeleteOthersReview() {
        String body = String.format("""
                {
                  "query": "mutation { deleteReview(reviewId: \\"%s\\") }"
                }
                """, aliceReview.getReviewId());

        given()
            .auth().oauth2(getAccessToken(admin.getUsername()))
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.deleteReview", is(true));
    }

    @Test
    void testUserCannotDeleteOthersReview() {
        String body = String.format("""
                {
                  "query": "mutation { deleteReview(reviewId: \\"%s\\") }"
                }
                """, aliceReview.getReviewId());

        given()
            .auth().oauth2(getAccessToken(jdoe.getUsername()))
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("errors[0].extensions.classification", equalTo("DataFetchingException"));
    }

    @Test
    void testGetReviewsByBookId() {
        String body = String.format("""
                {
                  "query": "query { reviewsByBookId(bookId: \\"%s\\") { bookId } }"
                }
                """, createdBook.getBookId());

        given()
            .auth().oauth2(getAccessToken(alice.getUsername()))
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.reviewsByBookId.size()", is(1))
            .body("data.reviewsByBookId[0].bookId", equalTo(createdBook.getBookId().toString()));
    }
}