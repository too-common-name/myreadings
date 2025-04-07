package modules.review.web.controllers;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import modules.catalog.core.usecases.BookServiceImpl;
import modules.review.domain.Review;
import modules.review.domain.ReviewImpl;
import modules.review.usecases.ReviewServiceImpl;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import modules.user.core.usecases.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(ReviewController.class)
public class ReviewControllerIntegrationTest {

        @Inject
        ReviewServiceImpl reviewService;

        @Inject
        BookServiceImpl bookService;

        @Inject
        UserServiceImpl userService;

        KeycloakTestClient keycloakClient = new KeycloakTestClient();

        private final User alice = UserImpl.builder()
                        .keycloakUserId(UUID.fromString("eb4123a3-b722-4798-9af5-8957f823657a"))
                        .firstName("Alice")
                        .lastName("Silverstone")
                        .username("alice")
                        .email("asilverstone@test.com")
                        .build();
        private final User admin = UserImpl.builder()
                        .keycloakUserId(UUID.fromString("af134cab-f41c-4675-b141-205f975db679"))
                        .firstName("Bruce")
                        .lastName("Wayne")
                        .username("admin")
                        .email("bwayne@test.com")
                        .build();

        private final Book testBook = BookImpl.builder()
                        .isbn("978-0321765723")
                        .title("The Lord of the Rings")
                        .build();

        private UUID aliceReviewId;
        private Book createdBook;

        @BeforeEach
        void setUp() {
                userService.createUserProfile(alice);
                userService.createUserProfile(admin);
                createdBook = bookService.createBook(testBook);

                Review aliceReview = ReviewImpl.builder()
                                .reviewId(UUID.randomUUID())
                                .book(createdBook)
                                .user(alice)
                                .rating(5)
                                .reviewText("Excellent book!")
                                .publicationDate(LocalDateTime.now())
                                .build();
                aliceReviewId = reviewService.createReview(aliceReview).getReviewId();
        }

        @Test
        void testUserCanCreateReview() {
                Book created = bookService.createBook(
                                BookImpl.builder().isbn("123").title("New Book").build());
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"bookId\": \"" + created.getBookId() + "\", \"rating\": 5, \"reviewText\": \"Great!\"}")
                                .when().post()
                                .then()
                                .statusCode(201)
                                .body("rating", is(5))
                                .body("reviewText", equalTo("Great!"))
                                .body("userId", equalTo(alice.getKeycloakUserId().toString()))
                                .body("bookId", equalTo(created.getBookId().toString()));
        }

        @Test
        void testUserCanAccessReview() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("reviewId", aliceReviewId)
                                .when().get("/{reviewId}")
                                .then()
                                .statusCode(200)
                                .body("reviewId", equalTo(aliceReviewId.toString()))
                                .body("userId", equalTo(alice.getKeycloakUserId().toString()));
        }


        @Test
        void testUserCanUpdateOwnReview() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("reviewId", aliceReviewId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"bookId\": \"" + createdBook.getBookId()
                                                + "\", \"rating\": 3, \"reviewText\": \"Updated review text.\"}")
                                .when().put("/{reviewId}")
                                .then()
                                .statusCode(200)
                                .body("reviewId", equalTo(aliceReviewId.toString()))
                                .body("rating", is(3))
                                .body("reviewText", equalTo("Updated review text."))
                                .body("userId", equalTo(alice.getKeycloakUserId().toString()));
        }

        @Test
        void testNoOneCannotUpdateOthersReview() {
                given()
                                .auth().oauth2(getAccessToken(admin.getUsername()))
                                .pathParam("reviewId", aliceReviewId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"bookId\": \"" + createdBook.getBookId()
                                                + "\", \"rating\": 2, \"reviewText\": \"Attempting to update.\"}")
                                .when().put("/{reviewId}")
                                .then()
                                .statusCode(403);
        }

        @Test
        void testUserCanDeleteOwnReview() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("reviewId", aliceReviewId)
                                .when().delete("/{reviewId}")
                                .then()
                                .statusCode(204);
        }

        @Test
        void testNoOneCannotDeleteOthersReview() {
                given()
                                .auth().oauth2(getAccessToken(admin.getUsername()))
                                .pathParam("reviewId", aliceReviewId)
                                .when().delete("/{reviewId}")
                                .then()
                                .statusCode(403);
        }

        @Test
        void testGetReviewsByBookId() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("bookId", createdBook.getBookId())
                                .when().get("/books/{bookId}")
                                .then()
                                .statusCode(200)
                                .body("[0].bookId", equalTo(createdBook.getBookId().toString()));
        }

        @Test
        void testGetReviewsByUserId() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("userId", alice.getKeycloakUserId())
                                .when().get("/users/{userId}")
                                .then()
                                .statusCode(200)
                                .body("[0].userId", equalTo(alice.getKeycloakUserId().toString()));
        }

        protected String getAccessToken(String userName) {
                return keycloakClient.getAccessToken(userName);
        }
}