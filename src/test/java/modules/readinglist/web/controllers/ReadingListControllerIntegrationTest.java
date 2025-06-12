package modules.readinglist.web.controllers;

import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;
import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import modules.catalog.core.usecases.BookService;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.domain.ReadingListImpl;
import modules.readinglist.core.usecases.ReadingListService;
import modules.user.core.domain.User;
import modules.user.core.domain.UserImpl;
import modules.user.core.usecases.UserService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@TestHTTPEndpoint(ReadingListController.class)
public class ReadingListControllerIntegrationTest {

        @Inject
        ReadingListService readingListService;

        @Inject
        UserService userService;

        @Inject
        BookService bookService;

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

        private final Book testBook = BookImpl.builder()
                        .isbn("123-456")
                        .title("Test Book")
                        .build();

        private UUID aliceListId;
        private UUID adminListId;
        private Book createdBook;

        @BeforeEach
        void setUp() {
                setUpUsers();
                setUpBooks();

                ReadingList aliceReadingList = ReadingListImpl.builder()
                                .readingListId(UUID.randomUUID())
                                .userId(alice.getKeycloakUserId())
                                .name("Alice's List")
                                .description("Alice's personal list")
                                .creationDate(java.time.LocalDateTime.now())
                                .books(new ArrayList<>())
                                .build();
                aliceListId = readingListService.createReadingList(aliceReadingList).getReadingListId();

                ReadingList adminReadingList = ReadingListImpl.builder()
                                .readingListId(UUID.randomUUID())
                                .userId(admin.getKeycloakUserId())
                                .name("Admin's List")
                                .description("Admin's personal list")
                                .creationDate(java.time.LocalDateTime.now())
                                .books(new ArrayList<>())
                                .build();
                adminListId = readingListService.createReadingList(adminReadingList).getReadingListId();
        }

        @AfterEach
        void cleanUp() {
                cleanUpReadingLists();
                cleanUpBooks();
                cleanUpUsers();
        }

        @Transactional
        void cleanUpReadingLists() {
                try {
                        readingListService.deleteReadingListById(aliceListId);
                        readingListService.deleteReadingListById(adminListId);
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
                createdBook = bookService.createBook(testBook);
        }

        protected String getAccessToken(String userName) {
                return keycloakClient.getAccessToken(userName);
        }

        @Test
        void testUserCanCreateReadingList() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"name\": \"New List\", \"description\": \"A new list\"}")
                                .when().post()
                                .then()
                                .statusCode(201)
                                .body("readingListId", notNullValue())
                                .body("name", is("New List"))
                                .body("description", is("A new list"));
        }

        @Test
        void testUserCanGetOwnReadingListById() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("readingListId", aliceListId)
                                .when().get("/{readingListId}")
                                .then()
                                .statusCode(200)
                                .body("readingListId", is(aliceListId.toString()))
                                .body("name", is("Alice's List"));
        }

        @Test
        void testUserCannotGetOthersReadingListById() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("readingListId", adminListId)
                                .when().get("/{readingListId}")
                                .then()
                                .statusCode(403);
        }

        @Test
        void testUserCanGetAllOwnReadingLists() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .when().get()
                                .then()
                                .statusCode(200)
                                .body("any { it.readingListId == '" + aliceListId.toString() + "' }", is(true));
        }

        @Test
        void testUserCanUpdateOwnReadingList() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("readingListId", aliceListId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"name\": \"Updated List\", \"description\": \"Updated description\"}")
                                .when().put("/{readingListId}")
                                .then()
                                .statusCode(200)
                                .body("readingListId", is(aliceListId.toString()))
                                .body("name", is("Updated List"))
                                .body("description", is("Updated description"));
        }

        @Test
        void testUserCannotUpdateOthersReadingList() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("readingListId", adminListId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"name\": \"Attempted Update\", \"description\": \"Attempted\"}")
                                .when().put("/{readingListId}")
                                .then()
                                .statusCode(403);
        }

        @Test
        void testUserCanDeleteOwnReadingList() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("readingListId", aliceListId)
                                .when().delete("/{readingListId}")
                                .then()
                                .statusCode(204);
        }

        @Test
        void testUserCannotDeleteOthersReadingList() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("readingListId", adminListId)
                                .when().delete("/{readingListId}")
                                .then()
                                .statusCode(403);
        }

        @Test
        void testUserCanAddBookToOwnReadingList() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("readingListId", aliceListId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"bookId\": \"" + createdBook.getBookId() + "\"}")
                                .when().post("/{readingListId}/books")
                                .then()
                                .log().all()
                                .statusCode(200)
                                .body(is("Book added to reading list."));
        }

        @Test
        void testUserCannotAddBookToOthersReadingList() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("readingListId", adminListId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"bookId\": \"" + createdBook.getBookId() + "\"}")
                                .when().post("/{readingListId}/books")
                                .then()
                                .statusCode(403);
        }

        @Test
        void testUserCanRemoveBookFromOwnReadingList() {
                String token = getAccessToken(alice.getUsername());
                given()
                                .auth().oauth2(token)
                                .pathParam("readingListId", aliceListId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"bookId\": \"" + createdBook.getBookId() + "\"}")
                                .when().post("/{readingListId}/books");

                given()
                                .auth().oauth2(token)
                                .pathParam("readingListId", aliceListId)
                                .pathParam("bookId", createdBook.getBookId())
                                .when().delete("/{readingListId}/books/{bookId}")
                                .then()
                                .statusCode(204);
        }

        @Test
        void testUserCannotRemoveBookFromOthersReadingList() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("readingListId", adminListId)
                                .pathParam("bookId", createdBook.getBookId())
                                .when().delete("/{readingListId}/books/{bookId}")
                                .then()
                                .statusCode(403);
        }

        @Test
        void testUserCanGetBooksInOwnReadingList() {
                String token = getAccessToken(alice.getUsername());
                given()
                                .auth().oauth2(token)
                                .pathParam("readingListId", aliceListId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"bookId\": \"" + createdBook.getBookId() + "\"}")
                                .when().post("/{readingListId}/books");

                given()
                                .auth().oauth2(token)
                                .pathParam("readingListId", aliceListId)
                                .when().get("/{readingListId}/books")
                                .then()
                                .statusCode(200)
                                .body("any { it.bookId == '" + createdBook.getBookId().toString() + "' }", is(true));
        }

        @Test
        void testUserCannotGetBooksInOthersReadingList() {
                given()
                                .auth().oauth2(getAccessToken(alice.getUsername()))
                                .pathParam("readingListId", adminListId)
                                .when().get("/{readingListId}/books")
                                .then()
                                .statusCode(403);
        }
}