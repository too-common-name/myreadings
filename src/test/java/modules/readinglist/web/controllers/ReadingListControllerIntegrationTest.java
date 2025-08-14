package modules.readinglist.web.controllers;

import common.AbstractControllerIntegrationTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import modules.catalog.core.domain.Book;
import modules.catalog.utils.CatalogTestUtils;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.utils.ReadingListTestUtils;
import modules.readinglist.web.dto.AddBookRequestDTO;
import modules.readinglist.web.dto.ReadingListRequestDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@TestHTTPEndpoint(ReadingListController.class)
public class ReadingListControllerIntegrationTest extends AbstractControllerIntegrationTest {

    private Book createdBook;
    private ReadingList aliceList;
    private ReadingList adminList;

    @BeforeEach
    void setUp() {
        createdBook = helper.saveBook(CatalogTestUtils.createValidBook());
        aliceList = helper.saveReadingList(ReadingListTestUtils.createValidReadingListForUser(alice, "Alice's List"));
        adminList = helper.saveReadingList(ReadingListTestUtils.createValidReadingListForUser(admin, "Admin's List"));
    }

    @AfterEach
    void tearDown() {
        helper.deleteReadingList(aliceList.getReadingListId());
        helper.deleteReadingList(adminList.getReadingListId());
        helper.deleteBook(createdBook.getBookId());
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
        
        helper.deleteReadingList(UUID.fromString(newListId));
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
    void testUserCanDeleteOwnReadingList() {
        ReadingList listToDelete = helper.saveReadingList(ReadingListTestUtils.createValidReadingListForUser(alice, "To Delete"));
        
        given()
            .auth().oauth2(getAccessToken("alice"))
            .pathParam("readingListId", listToDelete.getReadingListId())
        .when()
            .delete("/{readingListId}")
        .then()
            .statusCode(204);
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

    @Test
    void testUserCanRemoveBookFromOwnReadingList() {
        helper.addBookToReadingList(aliceList.getReadingListId(), createdBook.getBookId());

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
        helper.addBookToReadingList(aliceList.getReadingListId(), createdBook.getBookId());

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
}