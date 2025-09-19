package org.modular.playground.catalog.web.controllers;

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
import org.modular.playground.catalog.web.dto.BookRequestDTO;
import org.modular.playground.catalog.web.dto.BookUpdateDTO;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.utils.UserRepositoryUtils;
import org.modular.playground.user.utils.UserTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class BookGraphQLControllerIntegrationTest {

    @Inject
    UserRepositoryUtils userRepositoryUtils;
    @Inject
    CatalogRepositoryUtils catalogRepositoryUtils;

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    private static final UUID ALICE_UUID = UUID.fromString("eb4123a3-b722-4798-9af5-8957f823657a");
    private static final UUID ADMIN_UUID = UUID.fromString("af134cab-f41c-4675-b141-205f975db679");

    private User alice;
    private User admin;
    private final List<Book> booksToCleanup = new ArrayList<>();

    @BeforeEach
    void setUp() {
        booksToCleanup.clear();
        alice = userRepositoryUtils.saveUser(UserTestUtils.createValidUserWithIdAndUsername(ALICE_UUID, "alice"));
        admin = userRepositoryUtils.saveUser(UserTestUtils.createValidUserWithIdAndUsername(ADMIN_UUID, "admin"));
    }

    @AfterEach
    void tearDown() {
        booksToCleanup.forEach(book -> catalogRepositoryUtils.deleteBook(book.getBookId()));
        userRepositoryUtils.deleteUser(alice.getKeycloakUserId());
        userRepositoryUtils.deleteUser(admin.getKeycloakUserId());
    }

    private String getAccessToken(String username) {
        return keycloakClient.getAccessToken(username);
    }

    private Book createAndTrackBook(BookRequestDTO request) {
        Book book = catalogRepositoryUtils.saveBook(CatalogTestUtils.fromRequestDTO(request));
        booksToCleanup.add(book);
        return book;
    }

    @Test
    void testCreateBookSuccessful() {
        BookRequestDTO request = new BookRequestDTO();
        request.setTitle("Test Title");
        request.setIsbn("1234567890");
        request.setAuthors(List.of("Author One"));
        request.setPublisher("Test Publisher");

        String authorsString = request.getAuthors().stream().map(a -> "\\\"" + a + "\\\"").collect(Collectors.joining(", "));
        String body = String.format("""
                {
                  "query": "mutation { createBook(book: { title: \\"%s\\", isbn: \\"%s\\", authors: [%s], publisher: \\"%s\\" }) { bookId title isbn } }"
                }
                """, request.getTitle(), request.getIsbn(), authorsString, request.getPublisher());

        given()
                .auth().oauth2(getAccessToken("admin"))
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/graphql")
        .then()
                .statusCode(200)
                .body("data.createBook.title", equalTo(request.getTitle()));
    }

    @Test
    void testCreateBookWithoutAuthShouldReturnUnauthorized() {
        String body = """
                {
                  "query": "mutation { createBook(book: { title: \\"Unauthorized Title\\", isbn: \\"1111111111\\" }) { bookId } }"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/graphql")
        .then()
                .statusCode(200)
                .body("errors[0].extensions.classification", equalTo("DataFetchingException"));
    }

    @Test
    void testCreateBookMissingIsbnShouldReturnBadRequest() {
        String body = """
                {
                  "query": "mutation { createBook(book: { title: \\"No ISBN\\" }) { bookId } }"
                }
                """;

        given()
                .auth().oauth2(getAccessToken("admin"))
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/graphql")
        .then()
                .statusCode(200)
                .body("errors[0].extensions.classification", equalTo("ValidationError"));
    }

    @Test
    void testGetBookByIdExistingIdShouldReturnOkAndBook() {
        Book book = createAndTrackBook(CatalogTestUtils.createValidBookRequestDTO());
        String body = String.format("""
                {
                  "query": "query { bookById(bookId: \\"%s\\") { bookId title } }"
                }
                """, book.getBookId());

        given()
                .auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/graphql")
        .then()
                .statusCode(200)
                .body("data.bookById.title", equalTo(book.getTitle()));
    }

    @Test
    void testGetBookByIdNonExistingIdShouldReturnNotFound() {
        String body = String.format("""
                {
                  "query": "query { bookById(bookId: \\"%s\\") { bookId } }"
                }
                """, UUID.randomUUID());

        given()
                .auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/graphql")
        .then()
                .statusCode(200)
                .body("data.bookById", nullValue());
    }

    @Test
    void testDeleteBookByIdExistingIdShouldReturnNoContentAndBookShouldBeGone() {
        Book book = createAndTrackBook(CatalogTestUtils.createValidBookRequestDTO());
        String mutationBody = String.format("""
                {
                  "query": "mutation { deleteBook(bookId: \\"%s\\") }"
                }
                """, book.getBookId());

        given()
                .auth().oauth2(getAccessToken("admin"))
                .contentType(ContentType.JSON)
                .body(mutationBody)
        .when()
                .post("/graphql")
        .then()
                .statusCode(200)
                .body("data.deleteBook", equalTo(true));

        String queryBody = String.format("""
                {
                  "query": "query { bookById(bookId: \\"%s\\") { bookId } }"
                }
                """, book.getBookId());

        given()
                .auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .body(queryBody)
        .when()
                .post("/graphql")
        .then()
                .statusCode(200)
                .body("data.bookById", nullValue());
    }

    @Test
    void testSearchBooksSuccessful() {
        BookRequestDTO request = new BookRequestDTO();
        request.setTitle("The Great Gatsby");
        request.setIsbn("2222222222");
        createAndTrackBook(request);

        String body = """
                {
                  "query": "query { searchBooks(query: \\"Gatsby\\") { content { title } totalElements } }"
                }
                """;

        given()
                .auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/graphql")
        .then()
                .statusCode(200)
                .body("data.searchBooks.totalElements", equalTo(1));
    }

    @Test
    void testAdminCanUpdateBookSuccessful() {
        Book book = createAndTrackBook(CatalogTestUtils.createValidBookRequestDTO());
        BookUpdateDTO updateDTO = new BookUpdateDTO();
        updateDTO.setTitle("New Updated Title");

        String body = String.format("""
                {
                  "query": "mutation { updateBook(bookId: \\"%s\\", updates: { title: \\"%s\\" }) { title } }"
                }
                """, book.getBookId(), updateDTO.getTitle());

        given()
                .auth().oauth2(getAccessToken("admin"))
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/graphql")
        .then()
                .statusCode(200)
                .body("data.updateBook.title", equalTo("New Updated Title"));
    }

    @Test
    void testUserCannotUpdateBook() {
        Book book = createAndTrackBook(CatalogTestUtils.createValidBookRequestDTO());
        String body = String.format("""
                {
                  "query": "mutation { updateBook(bookId: \\"%s\\", updates: { title: \\"Forbidden Update\\" }) { title } }"
                }
                """, book.getBookId());

        given()
                .auth().oauth2(getAccessToken("alice"))
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/graphql")
        .then()
                .statusCode(200)
                .body("errors[0].extensions.classification", equalTo("DataFetchingException"));
    }
}