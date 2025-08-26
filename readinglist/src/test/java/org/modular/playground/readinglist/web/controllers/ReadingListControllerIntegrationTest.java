package org.modular.playground.readinglist.web.controllers;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@TestHTTPEndpoint(ReadingListController.class)
public class ReadingListControllerIntegrationTest {
    @Inject
    UserRepositoryUtils userRepositoryUtils;

    @Inject
    CatalogRepositoryUtils catalogRepositoryUtils;

    @Inject
    ReadingListRepositoryUtils readingListRepositoryUtils;
    

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    private static final UUID ALICE_UUID = UUID.fromString("eb4123a3-b722-4798-9af5-8957f823657a");
    private static final UUID ADMIN_UUID = UUID.fromString("af134cab-f41c-4675-b141-205f975db679");

    private User alice;
    private User admin;
    private Book createdBook;
    private ReadingList aliceList;
    private ReadingList adminList;

    @BeforeEach
    void setUp() {
        alice = userRepositoryUtils.saveUser(UserTestUtils.createValidUserWithIdAndUsername(ALICE_UUID, "alice"));
        admin = userRepositoryUtils.saveUser(UserTestUtils.createValidUserWithIdAndUsername(ADMIN_UUID, "admin"));
        createdBook = catalogRepositoryUtils.saveBook(CatalogTestUtils.createValidBook());
        aliceList = readingListRepositoryUtils.saveReadingList(ReadingListTestUtils.createValidReadingListForUser(alice, "Alice's List"));
        adminList = readingListRepositoryUtils.saveReadingList(ReadingListTestUtils.createValidReadingListForUser(admin, "Admin's List"));
    }

    @AfterEach
    void tearDown() {  
        readingListRepositoryUtils.deleteReadingList(aliceList.getReadingListId());
        readingListRepositoryUtils.deleteReadingList(adminList.getReadingListId());
        catalogRepositoryUtils.deleteBook(createdBook.getBookId());
        userRepositoryUtils.deleteUser(alice.getKeycloakUserId());
        userRepositoryUtils.deleteUser(admin.getKeycloakUserId());
    }

    protected String getAccessToken(String userName) {
        return keycloakClient.getAccessToken(userName);
    }

    @Test
    void testUserCanCreateReadingList() {
        ReadingListRequestDTO requestBody = ReadingListRequestDTO.builder()
                .name("New List for Alice")
                .description("A new list")
                .build();

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
    void testUserCanDeleteOwnReadingList() {
        
        ReadingList listToDelete = readingListRepositoryUtils.saveReadingList(ReadingListTestUtils.createValidReadingListForUser(alice, "To Delete"));
        
        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", listToDelete.getReadingListId())
        .when()
            .delete("/{readingListId}")
        .then()
            .statusCode(204);
    }

    @Test
    void testUserCanRemoveBookFromOwnReadingList() {
        
        readingListRepositoryUtils.addBookToReadingList(aliceList.getReadingListId(), createdBook.getBookId());

        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", aliceList.getReadingListId())
            .pathParam("bookId", createdBook.getBookId())
        .when()
            .delete("/{readingListId}/books/{bookId}")
        .then()
            .statusCode(204);
    }
    
    @Test
    void testUserCanGetBooksInOwnReadingList() {
        
        readingListRepositoryUtils.addBookToReadingList(aliceList.getReadingListId(), createdBook.getBookId());

        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", aliceList.getReadingListId())
        .when()
            .get("/{readingListId}/books")
        .then()
            .statusCode(200)
            .body("size()", is(1))
            .body("[0].bookId", is(createdBook.getBookId().toString()));
    }
    
    
    @Test
    void testUserCanGetOwnReadingListById() {
        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", aliceList.getReadingListId())
        .when()
            .get("/{readingListId}")
        .then()
            .statusCode(200)
            .body("readingListId", is(aliceList.getReadingListId().toString()))
            .body("name", is("Alice's List"));
    }

    @Test
    void testUserCannotGetOthersReadingListById() {
        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", adminList.getReadingListId())
        .when()
            .get("/{readingListId}")
        .then()
            .statusCode(403);
    }

    @Test
    void testAdminCanGetOthersReadingListById() {
        given()
            .auth().oauth2(getAccessToken("admin"))
            .pathParam("readingListId", aliceList.getReadingListId())
        .when()
            .get("/{readingListId}")
        .then()
            .statusCode(200);
    }
    
    @Test
    void testUserCanUpdateOwnReadingList() {
        ReadingListRequestDTO requestBody = ReadingListRequestDTO.builder()
                .name("Updated Name")
                .description("Updated description")
                .build();

        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", aliceList.getReadingListId())
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
        .when()
            .put("/{readingListId}")
        .then()
            .statusCode(200)
            .body("name", is("Updated Name"));
    }

    @Test
    void testUserCannotUpdateOthersReadingList() {
        ReadingListRequestDTO requestBody = ReadingListRequestDTO.builder().name("Attempted Update").build();

        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", adminList.getReadingListId())
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
            .pathParam("readingListId", adminList.getReadingListId())
        .when()
            .delete("/{readingListId}")
        .then()
            .statusCode(403);
    }

    @Test
    void testUserCanAddBookToOwnReadingList() {
        AddBookRequestDTO requestBody = AddBookRequestDTO.builder()
                .bookId(createdBook.getBookId())
                .build();

        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", aliceList.getReadingListId())
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
        .when()
            .post("/{readingListId}/books")
        .then()
            .statusCode(200);
    }
}