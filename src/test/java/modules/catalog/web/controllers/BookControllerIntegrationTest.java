package modules.catalog.web.controllers;

import common.TransactionalTestHelper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import modules.catalog.core.domain.Book;
import modules.catalog.utils.CatalogTestUtils;
import modules.catalog.web.dto.BookRequestDTO;
import modules.catalog.web.dto.BookUpdateDTO;
import modules.user.core.domain.User;
import modules.user.utils.UserTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
@TestHTTPEndpoint(BookController.class)
public class BookControllerIntegrationTest {

    @Inject
    TransactionalTestHelper helper;

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    private static final UUID ALICE_UUID = UUID.fromString("eb4123a3-b722-4798-9af5-8957f823657a");
    private static final UUID ADMIN_UUID = UUID.fromString("af134cab-f41c-4675-b141-205f975db679");

    private User alice;
    private User admin;
    private final List<Book> booksToCleanup = new ArrayList<>();

    @BeforeEach
    void setUp() {
        booksToCleanup.clear();
        alice = helper.saveUser(UserTestUtils.createValidUserWithIdAndUsername(ALICE_UUID, "alice"));
        admin = helper.saveUser(UserTestUtils.createValidUserWithIdAndUsername(ADMIN_UUID, "admin"));
    }

    @AfterEach
    void tearDown() {
        booksToCleanup.forEach(book -> helper.deleteBook(book.getBookId()));
        helper.deleteUser(alice.getKeycloakUserId());
        helper.deleteUser(admin.getKeycloakUserId());
    }

    private String getAccessToken(String username) {
        return keycloakClient.getAccessToken(username);
    }

    private Book createAndTrackBook(BookRequestDTO request) {
        Book book = helper.saveBook(CatalogTestUtils.fromRequestDTO(request));
        booksToCleanup.add(book);
        return book;
    }
    
    @Test
    void testCreateBookSuccessful() {
        BookRequestDTO request = CatalogTestUtils.createValidBookRequestDTO();
        String bookId = given()
            .auth().oauth2(getAccessToken("admin"))
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
        .when()
            .post()
        .then()
            .statusCode(201)
            .body("isbn", equalTo(request.getIsbn()))
            .body("title", equalTo(request.getTitle()))
            .extract().path("bookId");
        
        helper.deleteBook(UUID.fromString(bookId));
    }

    @Test
    void testCreateBookWithoutRoleShouldReturnUnauthorized() {
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(CatalogTestUtils.createValidBookRequestDTO())
        .when()
            .post()
        .then()
            .statusCode(401);
    }

    @Test
    void testCreateBookMissingIsbnShouldReturnBadRequest() {
        BookRequestDTO request = CatalogTestUtils.createValidBookRequestDTO();
        request.setIsbn(null);
        given()
            .auth().oauth2(getAccessToken("admin"))
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
        .when()
            .post()
        .then()
            .statusCode(400);
    }

    @Test
    void testCreateBookMissingTitleShouldReturnBadRequest() {
        BookRequestDTO request = CatalogTestUtils.createValidBookRequestDTO();
        request.setTitle(null);
        given()
            .auth().oauth2(getAccessToken("admin"))
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
        .when()
            .post()
        .then()
            .statusCode(400);
    }

    @Test
    void testCreateBookTitleTooLongShouldReturnBadRequest() {
        BookRequestDTO request = CatalogTestUtils.createValidBookRequestDTO();
        request.setTitle(".".repeat(256));
        given()
            .auth().oauth2(getAccessToken("admin"))
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
        .when()
            .post()
        .then()
            .statusCode(400);
    }

    @Test
    void testCreateBookPublicationDateInFutureShouldReturnBadRequest() {
        BookRequestDTO request = CatalogTestUtils.createValidBookRequestDTO();
        request.setPublicationDate(LocalDate.now().plusDays(1));
        given()
            .auth().oauth2(getAccessToken("admin"))
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
        .when()
            .post()
        .then()
            .statusCode(400);
    }

    @Test
    void testCreateBookNegativePageCountShouldReturnBadRequest() {
        BookRequestDTO request = CatalogTestUtils.createValidBookRequestDTO();
        request.setPageCount(-1);
        given()
            .auth().oauth2(getAccessToken("admin"))
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
        .when()
            .post()
        .then()
            .statusCode(400);
    }

    @Test
    void testGetBookByIdExistingIdShouldReturnOkAndBook() {
        Book book = createAndTrackBook(CatalogTestUtils.createValidBookRequestDTO());
        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("bookId", book.getBookId())
        .when()
            .get("/{bookId}")
        .then()
            .statusCode(200)
            .body("isbn", equalTo(book.getIsbn()))
            .body("title", equalTo(book.getTitle()));
    }

    @Test
    void testGetBookByIdNonExistingIdShouldReturnNotFound() {
        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("bookId", UUID.randomUUID())
        .when()
            .get("/{bookId}")
        .then()
            .statusCode(404);
    }

    @Test
    void testDeleteBookByIdExistingIdShouldReturnNoContentAndBookShouldBeGone() {
        Book book = createAndTrackBook(CatalogTestUtils.createValidBookRequestDTO());
        given()
            .auth().oauth2(getAccessToken("admin"))
            .pathParam("bookId", book.getBookId())
        .when()
            .delete("/{bookId}")
        .then()
            .statusCode(204);
        
        given()
            .auth().oauth2(getAccessToken("admin"))
            .pathParam("bookId", book.getBookId())
        .when()
            .get("/{bookId}")
        .then()
            .statusCode(404);
    }

    @Test
    void testDeleteBookByIdNonExistingIdShouldReturnNotFound() {
        given()
            .auth().oauth2(getAccessToken("admin"))
            .pathParam("bookId", UUID.randomUUID())
        .when()
            .delete("/{bookId}")
        .then()
            .statusCode(404);
    }

    @Test
    void testDeleteBookByIdExistingIdWithoutRoleShouldReturnUnauthorized() {
        given()
            .pathParam("bookId", UUID.randomUUID())
        .when()
            .delete("/{bookId}")
        .then()
            .statusCode(401);
    }

    @Test
    void testGetAllBooksBooksExistShouldReturnOkAndListOfBooks() {
        createAndTrackBook(CatalogTestUtils.createValidBookRequestDTO());
        createAndTrackBook(CatalogTestUtils.createValidBookRequestDTO());
        given()
            .auth().oauth2(getAccessToken("alice"))
        .when()
            .get()
        .then()
            .statusCode(200)
            .body("size()", is(2));
    }

    @Test
    void testSearchBooksSuccessful() {
        BookRequestDTO request = CatalogTestUtils.createValidBookRequestDTO();
        request.setTitle("The Great Gatsby");
        createAndTrackBook(request);
        given()
            .auth().oauth2(getAccessToken("alice"))
            .queryParam("query", "great")
        .when()
            .get("/search")
        .then()
            .statusCode(200)
            .body("content", hasSize(1))
            .body("content[0].title", equalTo("The Great Gatsby"));
    }

    @Test
    void testSearchBooksWithoutQueryShouldReturnBadRequest() {
        given()
            .auth().oauth2(getAccessToken("alice"))
            .queryParam("query", "")
        .when()
            .get("/search")
        .then()
            .statusCode(400);
    }

    @Test
    void testSearchBooksWithoutRoleShouldReturnUnauthorized() {
        given()
            .queryParam("query", "test")
        .when()
            .get("/search")
        .then()
            .statusCode(401);
    }

    @Test
    void testAdminCanUpdateBookSuccessful() {
        Book book = createAndTrackBook(CatalogTestUtils.createValidBookRequestDTO());
        BookUpdateDTO updateDTO = BookUpdateDTO.builder()
                .title("New Updated Title")
                .description("Updated description.")
                .build();
        given()
            .auth().oauth2(getAccessToken("admin"))
            .pathParam("bookId", book.getBookId())
            .contentType(MediaType.APPLICATION_JSON)
            .body(updateDTO)
        .when()
            .put("/{bookId}")
        .then()
            .statusCode(200)
            .body("title", equalTo("New Updated Title"))
            .body("description", equalTo("Updated description."));
    }

    @Test
    void testUpdateBookReturnsNotFoundForNonExistentId() {
        BookUpdateDTO updateDTO = BookUpdateDTO.builder().title("Non Existent").build();
        given()
            .auth().oauth2(getAccessToken("admin"))
            .pathParam("bookId", UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .body(updateDTO)
        .when()
            .put("/{bookId}")
        .then()
            .statusCode(404);
    }

    @Test
    void testUserCannotUpdateBook() {
        Book book = createAndTrackBook(CatalogTestUtils.createValidBookRequestDTO());
        BookUpdateDTO updateDTO = BookUpdateDTO.builder().title("Forbidden Update").build();
        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("bookId", book.getBookId())
            .contentType(MediaType.APPLICATION_JSON)
            .body(updateDTO)
        .when()
            .put("/{bookId}")
        .then()
            .statusCode(403);
    }
}