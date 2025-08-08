package modules.review.web.controllers;

import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;
import modules.catalog.core.domain.Book;
import modules.catalog.core.usecases.repositories.BookRepository;
import modules.catalog.utils.CatalogTestUtils;
import modules.review.core.domain.Review;
import modules.review.core.usecases.repositories.ReviewRepository;
import modules.review.utils.ReviewTestUtils;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import modules.user.core.usecases.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import common.JpaRepositoryTestProfile;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestProfile(JpaRepositoryTestProfile.class)
@TestHTTPEndpoint(ReviewController.class)
public class ReviewControllerIntegrationTest {

    @Inject
    ReviewRepository reviewRepository;
    @Inject
    BookRepository bookRepository;
    @Inject
    UserRepository userRepository;

    @Inject
    @PersistenceUnit("review-db")
    EntityManager reviewsEntityManager;
    @Inject
    @PersistenceUnit("books-db")
    EntityManager booksEntityManager;
    @Inject
    @PersistenceUnit("users-db")
    EntityManager usersEntityManager;

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    private User alice;
    private User admin;
    private User anotherUser;
    private Book createdBook;
    private Book createdAnotherBook;
    private Book newBook;
    private Review aliceReview;

    @BeforeEach
    void setUp() {
        setupUsers();
        setupBooks();
        setupReviews();
    }

    @AfterEach
    void tearDown() {
        cleanUpReviews();
        cleanUpBooks();
        cleanUpUsers();
    }

    @Transactional
    void cleanUpReviews() {
        reviewsEntityManager.createQuery("DELETE FROM ReviewEntity").executeUpdate();
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
    void setupUsers() {
        alice = userRepository.save(UserImpl.builder()
                .keycloakUserId(UUID.fromString("eb4123a3-b722-4798-9af5-8957f823657a"))
                .firstName("Alice").lastName("Silverstone").username("alice").email("asilverstone@test.com")
                .build());
        admin = userRepository.save(UserImpl.builder()
                .keycloakUserId(UUID.fromString("af134cab-f41c-4675-b141-205f975db679"))
                .firstName("Bruce").lastName("Wayne").username("admin").email("bwayne@test.com")
                .build());
        anotherUser = userRepository
                .save(UserImpl.builder()
                        .keycloakUserId(UUID.fromString("1eed6a8e-a853-4597-b4c6-c4c2533546a0"))
                        .firstName("John")
                        .lastName("Doe")
                        .username("jdoe")
                        .email("john.doe@example.com")
                        .build());
    }

    @Transactional
    void setupBooks() {
        createdBook = bookRepository.save(CatalogTestUtils.createValidBook());
        createdAnotherBook = bookRepository.save(CatalogTestUtils.createValidBook());
        newBook = bookRepository.save(CatalogTestUtils.createValidBook());
    }

    @Transactional
    void setupReviews() {
        aliceReview = reviewRepository.create(ReviewTestUtils.createValidReviewForUserAndBook(alice.getKeycloakUserId(),
                createdBook.getBookId(), "Excellent book!", 5));
    }

    protected String getAccessToken(String userName) {
        return keycloakClient.getAccessToken(userName);
    }

    @Test
    void testUserCanCreateReview() {
        given()
                .auth().oauth2(getAccessToken(alice.getUsername()))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"bookId\": \"" + newBook.getBookId()
                        + "\", \"rating\": 5, \"reviewText\": \"Great!\"}")
                .when().post()
                .then()
                .statusCode(201)
                .body("rating", is(5))
                .body("reviewText", equalTo("Great!"))
                .body("userId", equalTo(alice.getKeycloakUserId().toString()))
                .body("bookId", equalTo(newBook.getBookId().toString()));
    }

    @Test
    void testUserCanAccessOwnReview() {
        given()
                .auth().oauth2(getAccessToken(alice.getUsername()))
                .pathParam("reviewId", aliceReview.getReviewId())
                .when().get("/{reviewId}")
                .then()
                .statusCode(200)
                .body("reviewId", equalTo(aliceReview.getReviewId().toString()))
                .body("userId", equalTo(alice.getKeycloakUserId().toString()));
    }

    @Test
    void testAdminCanAccessOthersReview() {
        given()
                .auth().oauth2(getAccessToken(admin.getUsername()))
                .pathParam("reviewId", aliceReview.getReviewId())
                .when().get("/{reviewId}")
                .then()
                .statusCode(200)
                .body("reviewId", equalTo(aliceReview.getReviewId().toString()));
    }

    @Test
    void testUserCanUpdateOwnReview() {
        given()
                .auth().oauth2(getAccessToken(alice.getUsername()))
                .pathParam("reviewId", aliceReview.getReviewId())
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"bookId\": \"" + createdBook.getBookId()
                        + "\", \"rating\": 3, \"reviewText\": \"Updated review text.\"}")
                .when().put("/{reviewId}")
                .then()
                .statusCode(200)
                .body("rating", is(3))
                .body("reviewText", equalTo("Updated review text."));
    }

    @Test
    void testUserCannotUpdateOthersReview() {
        given()
                .auth().oauth2(getAccessToken(anotherUser.getUsername()))
                .pathParam("reviewId", aliceReview.getReviewId())
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
                .pathParam("reviewId", aliceReview.getReviewId())
                .when().delete("/{reviewId}")
                .then()
                .statusCode(204);
    }

    @Test
    void testAdminCanDeleteOthersReview() {
        given()
                .auth().oauth2(getAccessToken(admin.getUsername()))
                .pathParam("reviewId", aliceReview.getReviewId())
                .when().delete("/{reviewId}")
                .then()
                .statusCode(204);
    }

    @Test
    void testUserCannotDeleteOthersReview() {
        given()
                .auth().oauth2(getAccessToken(anotherUser.getUsername()))
                .pathParam("reviewId", aliceReview.getReviewId())
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
                .body("size()", is(1))
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
                .body("size()", is(1))
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
                .body("totalReviews", is(0))
                .body("averageRating", equalTo(0.0f));
    }

    @Test
    void testGetMyReviewForBookShouldReturnOk() {
        given()
                .auth().oauth2(getAccessToken(alice.getUsername()))
                .pathParam("bookId", createdBook.getBookId())
                .when().get("/books/{bookId}/my-review")
                .then()
                .statusCode(200)
                .body("reviewId", equalTo(aliceReview.getReviewId().toString()));
    }
}