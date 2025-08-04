package modules.review.web.controllers;

import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;
import modules.catalog.core.domain.Book;
import modules.catalog.core.usecases.BookServiceImpl;
import modules.catalog.utils.CatalogTestUtils;
import modules.catalog.web.dto.BookRequestDTO;
import modules.review.core.domain.Review;

import modules.review.core.usecases.ReviewServiceImpl;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import modules.user.core.usecases.UserServiceImpl;

import org.junit.jupiter.api.AfterEach;
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

        @Inject
        @PersistenceUnit("books-db")
        EntityManager booksEntityManager;

        @Inject
        @PersistenceUnit("users-db")
        EntityManager usersEntityManager;

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

        private final BookRequestDTO testBookDTO = CatalogTestUtils.createValidBookRequestDTO();
        private final BookRequestDTO anotherTestBookDTO = CatalogTestUtils.createValidBookRequestDTO();

        private UUID aliceReviewId;
        private Book createdBook;
        private Book createdAnotherBook;

        @BeforeEach
        void setUp() {
                setUpUsers();
                setUpBooks();

                Review aliceReview = Review.builder()
                                .reviewId(UUID.randomUUID())
                                .book(createdBook)
                                .user(alice)
                                .rating(5)
                                .reviewText("Excellent book!")
                                .publicationDate(LocalDateTime.now())
                                .build();
                aliceReviewId = reviewService.createReview(aliceReview).getReviewId();
        }

        @AfterEach
        void cleanUp() {
                cleanUpReviews();
                cleanUpBooks();
                cleanUpUsers();
        }

        @Transactional
        void cleanUpReviews() {
                try {
                        reviewService.deleteReviewById(aliceReviewId);
                } catch (Exception ex) {
                }
        }

        @Transactional
        void cleanUpBooks() {
                booksEntityManager.createQuery("DELETE FROM BookEntity").executeUpdate();
        }

        @Transactional
        void cleanUpUsers() {
                usersEntityManager.createQuery("DELETE FROM UserEntity").executeUpdate();
        }

        @Transactional
        void setUpUsers() {
                userService.createUserProfile(alice);
                userService.createUserProfile(admin);
        }

        @Transactional
        void setUpBooks() {
                createdBook = bookService.createBook(testBookDTO);
                createdAnotherBook = bookService.createBook(anotherTestBookDTO);
        }

        protected String getAccessToken(String userName) {
                return keycloakClient.getAccessToken(userName);
        }

        @Test
        void testUserCanCreateReview() {
                BookRequestDTO bookRequest = CatalogTestUtils.createValidBookRequestDTO();
                Book created = bookService.createBook(bookRequest);
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"bookId\": \"" + created.getBookId()
                                                + "\", \"rating\": 5, \"reviewText\": \"Great!\"}")
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

        @Test
        void testGetBookReviewStatsForBookWithReviews() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("bookId", createdBook.getBookId())
                                .when().get("/books/{bookId}/stats")
                                .then()
                                .statusCode(200)
                                .body("bookId", equalTo(createdBook.getBookId().toString()))
                                .body("totalReviews", is(1))
                                .body("averageRating", equalTo(5.0f));
        }

        @Test
        void testGetBookReviewStatsForBookWithoutReviews() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("bookId", createdAnotherBook.getBookId())
                                .when().get("/books/{bookId}/stats")
                                .then()
                                .statusCode(200)
                                .body("bookId", equalTo(createdAnotherBook.getBookId().toString()))
                                .body("totalReviews", is(0))
                                .body("averageRating", equalTo(0.0f));
        }

        @Test
        void testGetBookReviewStatsForNonExistentBook() {
                UUID nonExistentBookId = UUID.randomUUID();
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("bookId", nonExistentBookId)
                                .when().get("/books/{bookId}/stats")
                                .then()
                                .statusCode(404)
                                .body(equalTo("Book not found with ID: " + nonExistentBookId));
        }

        @Test
        void testGetMyReviewForBookShouldReturnOkAndReviewDTOWhenReviewExists() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("bookId", createdBook.getBookId())
                                .when().get("/books/{bookId}/my-review")
                                .then()
                                .statusCode(200)
                                .body("reviewId", equalTo(aliceReviewId.toString()))
                                .body("bookId", equalTo(createdBook.getBookId().toString()))
                                .body("userId", equalTo(alice.getKeycloakUserId().toString()))
                                .body("rating", is(5))
                                .body("reviewText", equalTo("Excellent book!"));
        }

        @Test
        void testGetMyReviewForBookShouldReturnNotFoundWhenReviewDoesNotExist() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("bookId", createdAnotherBook.getBookId()) // Use a book Alice hasn't reviewed
                                .when().get("/books/{bookId}/my-review")
                                .then()
                                .statusCode(404)
                                .body(equalTo("Review not found for this user and book."));
        }

        @Test
        void testGetMyReviewForBookShouldReturnNotFoundIfBookDoesNotExist() {
                UUID nonExistentBookId = UUID.randomUUID();
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("bookId", nonExistentBookId)
                                .when().get("/books/{bookId}/my-review")
                                .then()
                                .statusCode(404)
                                .body(equalTo("Book not found."));
        }

        @Test
        void testGetMyReviewForBookShouldReturnUnauthorizedIfUserIsNotAuthenticated() {
                given()
                                .pathParam("bookId", createdBook.getBookId())
                                .when().get("/books/{bookId}/my-review")
                                .then()
                                .statusCode(401);
        }

}