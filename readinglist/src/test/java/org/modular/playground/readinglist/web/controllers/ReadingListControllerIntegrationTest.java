package org.modular.playground.readinglist.web.controllers;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.utils.CatalogRepositoryUtils;
import org.modular.playground.catalog.utils.CatalogTestUtils;
import org.modular.playground.readinglist.core.domain.ReadingList;
import org.modular.playground.readinglist.utils.ReadingListRepositoryUtils;
import org.modular.playground.readinglist.utils.ReadingListTestUtils;
import org.modular.playground.readinglist.web.dto.AddBookRequestDTO;
import org.modular.playground.readinglist.web.dto.ReadingListRequestDTO;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.utils.UserRepositoryUtils;
import org.modular.playground.user.utils.UserTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@TestHTTPEndpoint(ReadingListController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReadingListControllerIntegrationTest {

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
        ReadingListRequestDTO requestBody = ReadingListRequestDTO.builder().name("New List for Alice").build();

        String newListId = given()
            .auth().oauth2(getAccessToken("alice"))
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
        .when()
            .post()
        .then()
            .statusCode(201)
            .body("readingListId", notNullValue())
            .body("name", is("New List for Alice"))
            .extract().path("readingListId");
        
        readingListRepositoryUtils.deleteReadingList(UUID.fromString(newListId));
    }

    @Test
    void testUserCanGetOwnReadingLists() {
        given()
            .auth().oauth2(getAccessToken("alice"))
        .when()
            .get()
        .then()
            .statusCode(200)
            .body("$", hasSize(2))
            .body("[0].readingListId", is(getListId("listAlice1").toString()))
            .body("[0].name", is(getList("listAlice1").getName()))
            .body("[1].readingListId", is(getListId("listAlice2").toString()))
            .body("[1].name", is(getList("listAlice2").getName()));
    }

    @Test
    void testUserCanGetOwnReadingListById() {
        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", getListId("listAlice1"))
        .when()
            .get("/{readingListId}")
        .then()
            .statusCode(200)
            .body("readingListId", is(getListId("listAlice1").toString()))
            .body("name", is(getList("listAlice1").getName()));
    }
    
    @Test
    void testUserCanUpdateOwnReadingList() {
        ReadingListRequestDTO requestBody = ReadingListRequestDTO.builder().name("Updated Name").build();

        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", getListId("listAlice1"))
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
        .when()
            .put("/{readingListId}")
        .then()
            .statusCode(200)
            .body("name", is("Updated Name"));
    }

    @Test
    void testUserCanDeleteOwnReadingList() {
        ReadingList listToDelete = readingListRepositoryUtils.saveReadingList(ReadingListTestUtils.createValidReadingListForUser(getUser("userAlice"), "To Delete"));
        
        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", listToDelete.getReadingListId())
        .when()
            .delete("/{readingListId}")
        .then()
            .statusCode(204);
    }

    @Test
    void testUserCanAddBookToOwnReadingList() {
        AddBookRequestDTO requestBody = AddBookRequestDTO.builder().bookId(getBookId("book1")).build();

        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", getListId("listAlice1"))
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
        .when()
            .post("/{readingListId}/books")
        .then()
            .statusCode(200);
    }

    @Test
    void testUserCanGetBooksInOwnReadingList() {
        readingListRepositoryUtils.addBookToReadingList(getListId("listAlice1"), getBookId("book1"));

        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", getListId("listAlice1"))
        .when()
            .get("/{readingListId}/books")
        .then()
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].bookId", is(getBookId("book1").toString()));
    }
    
    @Test
    void testUserCanRemoveBookFromOwnReadingList() {
        readingListRepositoryUtils.addBookToReadingList(getListId("listAlice1"), getBookId("book1"));

        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", getListId("listAlice1"))
            .pathParam("bookId", getBookId("book1"))
        .when()
            .delete("/{readingListId}/books/{bookId}")
        .then()
            .statusCode(204);
    }

    @Test
    void testUserCanGetOwnReadingListByIdWithBooksId() {
        readingListRepositoryUtils.addBookToReadingList(getListId("listAlice1"), getBookId("book1"));

        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", getListId("listAlice1"))
        .when()
            .get("/{readingListId}")
        .then()
            .statusCode(200)
            .body("readingListId", is(getListId("listAlice1").toString()))
            .body("name", is(getList("listAlice1").getName()))
            .body("books", hasSize(1))
            .body("books[0]", is(getBookId("book1").toString()));
    }

    @Test
    void testUserCanGetOwnReadingListsWithBooksId() {
        readingListRepositoryUtils.addBookToReadingList(getListId("listAlice1"), getBookId("book1"));
        readingListRepositoryUtils.addBookToReadingList(getListId("listAlice2"), getBookId("book2"));
        
        given()
            .auth().oauth2(getAccessToken("alice"))
        .when()
            .get()
        .then()
            .statusCode(200)
            .body("$", hasSize(2))
            .body("[0].name", is(getList("listAlice1").getName()))
            .body("[0].books", hasSize(1))
            .body("[0].books[0]", is(getBookId("book1").toString()))
            .body("[1].name", is(getList("listAlice2").getName()))
            .body("[1].books", hasSize(1))
            .body("[1].books[0]", is(getBookId("book2").toString()));
    }

    @Test
    void testUserCannotGetOthersReadingListById() {
        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", getListId("listAdmin1"))
        .when()
            .get("/{readingListId}")
        .then()
            .statusCode(403);
    }

    @Test
    void testUserCannotUpdateOthersReadingList() {
        ReadingListRequestDTO requestBody = ReadingListRequestDTO.builder().name("Attempted Update").build();

        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", getListId("listAdmin1"))
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
        .when()
            .put("/{readingListId}")
        .then()
            .statusCode(403);
    }

    @Test
    void testUserCannotDeleteOthersReadingList() {
        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", getListId("listAdmin1"))
        .when()
            .delete("/{readingListId}")
        .then()
            .statusCode(403);
    }

    @Test
    void testAdminCanGetOthersReadingListById() {
        given()
            .auth().oauth2(getAccessToken("admin"))
            .pathParam("readingListId", getListId("listAlice1"))
        .when()
            .get("/{readingListId}")
        .then()
            .statusCode(200);
    }
}