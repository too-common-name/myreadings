package org.modular.playground.readinglist.web.controllers;

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
import org.modular.playground.readinglist.core.domain.ReadingList;
import org.modular.playground.readinglist.utils.ReadingListRepositoryUtils;
import org.modular.playground.readinglist.utils.ReadingListTestUtils;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.utils.UserRepositoryUtils;
import org.modular.playground.user.utils.UserTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class ReadingGraphQLControllerIntegrationTest {

    @Inject
    UserRepositoryUtils userRepositoryUtils;
    @Inject
    CatalogRepositoryUtils catalogRepositoryUtils;
    @Inject
    ReadingListRepositoryUtils readingListRepositoryUtils;

    KeycloakTestClient keycloakClient = new KeycloakTestClient();
    private Map<String, Object> testData;

    private User getUser(String key) { return (User) testData.get(key); }
    private Book getBook(String key) { return (Book) testData.get(key); }
    private ReadingList getList(String key) { return (ReadingList) testData.get(key); }
    private UUID getBookId(String key) { return getBook(key).getBookId(); }
    private UUID getListId(String key) { return getList(key).getReadingListId(); }
    protected String getAccessToken(String userName) { return keycloakClient.getAccessToken(userName); }

    @BeforeEach
    void setUp() {
        testData = new HashMap<>();

        User alice = userRepositoryUtils.saveUser(UserTestUtils.createValidUserWithIdAndUsername(UUID.fromString("eb4123a3-b722-4798-9af5-8957f823657a"), "alice"));
        User admin = userRepositoryUtils.saveUser(UserTestUtils.createValidUserWithIdAndUsername(UUID.fromString("af134cab-f41c-4675-b141-205f975db679"), "admin"));
        testData.put("userAlice", alice);
        testData.put("userAdmin", admin);
        
        testData.put("book1", catalogRepositoryUtils.saveBook(CatalogTestUtils.createValidBook()));
        testData.put("book2", catalogRepositoryUtils.saveBook(CatalogTestUtils.createValidBook()));

        testData.put("listAlice1", readingListRepositoryUtils.saveReadingList(ReadingListTestUtils.createValidReadingListForUser(alice, "Alice's List")));
        testData.put("listAlice2", readingListRepositoryUtils.saveReadingList(ReadingListTestUtils.createValidReadingListForUser(alice, "Alice's Second List")));
        testData.put("listAdmin1", readingListRepositoryUtils.saveReadingList(ReadingListTestUtils.createValidReadingListForUser(admin, "Admin's List")));
    }

    @AfterEach
    void tearDown() {
        readingListRepositoryUtils.deleteReadingList(getListId("listAlice1"));
        readingListRepositoryUtils.deleteReadingList(getListId("listAlice2"));
        readingListRepositoryUtils.deleteReadingList(getListId("listAdmin1"));
        
        catalogRepositoryUtils.deleteBook(getBookId("book1"));
        catalogRepositoryUtils.deleteBook(getBookId("book2"));
        
        userRepositoryUtils.deleteUser(getUser("userAlice").getKeycloakUserId());
        userRepositoryUtils.deleteUser(getUser("userAdmin").getKeycloakUserId());
        
        testData.clear();
    }

    @Test
    void testUserCanCreateReadingList() {
        String body = """
                {
                  "query": "mutation { createReadingList(readingList: { name: \\"New List for Alice\\" }) { readingListId name } }"
                }
                """;
        
        String newListId = given()
            .auth().oauth2(getAccessToken("alice"))
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.createReadingList.readingListId", notNullValue())
            .body("data.createReadingList.name", is("New List for Alice"))
            .extract().path("data.createReadingList.readingListId");

        // Assicura la pulizia della nuova lista creata
        readingListRepositoryUtils.deleteReadingList(UUID.fromString(newListId));
    }

    @Test
    void testUserCanGetOwnReadingLists() {
        String body = """
                {
                  "query": "query { myReadingLists { readingListId name } }"
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
            .body("data.myReadingLists", hasSize(2));
    }

    @Test
    void testUserCanGetOwnReadingListById() {
        String body = String.format("""
                {
                  "query": "query { readingListById(readingListId: \\"%s\\") { readingListId name } }"
                }
                """, getListId("listAlice1"));

        given()
            .auth().oauth2(getAccessToken("alice"))
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.readingListById.readingListId", is(getListId("listAlice1").toString()))
            .body("data.readingListById.name", is(getList("listAlice1").getName()));
    }

    @Test
    void testUserCanUpdateOwnReadingList() {
        String body = String.format("""
                {
                  "query": "mutation { updateReadingList(readingListId: \\"%s\\", updates: { name: \\"Updated Name\\" }) { name } }"
                }
                """, getListId("listAlice1"));

        given()
            .auth().oauth2(getAccessToken("alice"))
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.updateReadingList.name", is("Updated Name"));
    }

    @Test
    void testUserCanDeleteOwnReadingList() {
        ReadingList listToDelete = readingListRepositoryUtils.saveReadingList(ReadingListTestUtils.createValidReadingListForUser(getUser("userAlice"), "To Delete"));
        String body = String.format("""
                {
                  "query": "mutation { deleteReadingList(readingListId: \\"%s\\") }"
                }
                """, listToDelete.getReadingListId());

        given()
            .auth().oauth2(getAccessToken("alice"))
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.deleteReadingList", is(true));
    }

    @Test
    void testUserCanAddBookToOwnReadingList() {
        String body = String.format("""
                {
                  "query": "mutation { addBookToReadingList(readingListId: \\"%s\\", bookId: \\"%s\\") }"
                }
                """, getListId("listAlice1"), getBookId("book1"));

        given()
            .auth().oauth2(getAccessToken("alice"))
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.addBookToReadingList", is(true));
    }

    @Test
    void testUserCanGetBooksInOwnReadingList() {
        readingListRepositoryUtils.addBookToReadingList(getListId("listAlice1"), getBookId("book1"));
        String body = String.format("""
                {
                  "query": "query { booksInReadingList(readingListId: \\"%s\\") { bookId } }"
                }
                """, getListId("listAlice1"));

        given()
            .auth().oauth2(getAccessToken("alice"))
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.booksInReadingList", hasSize(1))
            .body("data.booksInReadingList[0].bookId", is(getBookId("book1").toString()));
    }

    @Test
    void testUserCanRemoveBookFromOwnReadingList() {
        readingListRepositoryUtils.addBookToReadingList(getListId("listAlice1"), getBookId("book1"));
        String body = String.format("""
                {
                  "query": "mutation { removeBookFromReadingList(readingListId: \\"%s\\", bookId: \\"%s\\") }"
                }
                """, getListId("listAlice1"), getBookId("book1"));

        given()
            .auth().oauth2(getAccessToken("alice"))
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.removeBookFromReadingList", is(true));
    }

    @Test
    void testUserCannotGetOthersReadingListById() {
        String body = String.format("""
                {
                  "query": "query { readingListById(readingListId: \\"%s\\") { readingListId } }"
                }
                """, getListId("listAdmin1"));
        
        given()
            .auth().oauth2(getAccessToken("alice"))
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("errors", hasSize(greaterThanOrEqualTo(1)))
            .body("errors[0].extensions.classification", is("DataFetchingException"));
    }

    @Test
    void testAdminCanGetOthersReadingListById() {
        String body = String.format("""
                {
                  "query": "query { readingListById(readingListId: \\"%s\\") { readingListId } }"
                }
                """, getListId("listAlice1"));
        
        given()
            .auth().oauth2(getAccessToken("admin"))
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.readingListById.readingListId", is(getListId("listAlice1").toString()));
    }
}