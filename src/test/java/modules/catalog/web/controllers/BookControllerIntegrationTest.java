package modules.catalog.web.controllers;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import modules.catalog.utils.CatalogTestUtils;
import modules.catalog.web.dto.BookRequestDTO;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
@TestTransaction
public class BookControllerIntegrationTest {

        @Test
        @TestSecurity(user = "adminUser", roles = "admin")
        void testCreateBookSuccessful() {
                BookRequestDTO bookRequest = CatalogTestUtils.createValidBookRequestDTO();

                given().contentType(ContentType.JSON).body(bookRequest).when().post("/api/v1/books").then()
                                .statusCode(201).body("isbn", equalTo(bookRequest.getIsbn()))
                                .body("title", equalTo(bookRequest.getTitle()));
        }

        @Test
        void testCreateBookWithoutRoleShouldReturnUnauthorized() {
                given().contentType(ContentType.JSON).body(CatalogTestUtils.createValidBookRequestDTO())
                                .when().post("/api/v1/books")
                                .then().statusCode(401);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = "admin")
        void testCreateBookMissingIsbnShouldReturnBadRequest() {
                BookRequestDTO bookRequest = CatalogTestUtils.createValidBookRequestDTO();
                bookRequest.setIsbn(null);

                given().contentType(ContentType.JSON).body(bookRequest).when().post("/api/v1/books").then()
                                .statusCode(400).body(containsString("ISBN is required"));
        }

        @Test
        @TestSecurity(user = "adminUser", roles = "admin")
        void testCreateBookMissingTitleShouldReturnBadRequest() {
                BookRequestDTO bookRequest = CatalogTestUtils.createValidBookRequestDTO();
                bookRequest.setTitle(null);

                given().contentType(ContentType.JSON).body(bookRequest).when().post("/api/v1/books").then()
                                .statusCode(400).body(containsString("Title is required"));
        }

        @Test
        @TestSecurity(user = "adminUser", roles = "admin")
        void testCreateBookTitleTooLongShouldReturnBadRequest() {
                String longTitle = "This is a very long title that exceeds the maximum allowed length of 255 characters. Let's make it even longer just to be absolutely sure that it will indeed exceed the limit and trigger the validation error. We should probably add some more characters here to be safe.";
                BookRequestDTO bookRequest = CatalogTestUtils.createValidBookRequestDTO();
                bookRequest.setTitle(longTitle);

                given().contentType(ContentType.JSON).body(bookRequest).when().post("/api/v1/books").then()
                                .statusCode(400).body(containsString("Title cannot exceed 255 characters"));
        }

        @Test
        @TestSecurity(user = "adminUser", roles = "admin")
        void testCreateBookPublicationDateInFutureShouldReturnBadRequest() {
                BookRequestDTO bookRequest = CatalogTestUtils.createValidBookRequestDTO();
                bookRequest.setPublicationDate(LocalDate.now().plusDays(1));

                given().contentType(ContentType.JSON).body(bookRequest).when().post("/api/v1/books").then()
                                .statusCode(400)
                                .body(containsString("Publication date must be in the past or present"));
        }

        @Test
        @TestSecurity(user = "adminUser", roles = "admin")
        void testCreateBookNegativePageCountShouldReturnBadRequest() {
                BookRequestDTO bookRequest = CatalogTestUtils.createValidBookRequestDTO();
                bookRequest.setPageCount(-1);

                given().contentType(ContentType.JSON).body(bookRequest).when().post("/api/v1/books").then()
                                .statusCode(400).body(containsString("Page count cannot be negative"));
        }

        @Test
        @TestSecurity(user = "adminUser", roles = "admin")
        void testGetBookByIdExistingIdShouldReturnOkAndBook() {
                BookRequestDTO bookRequest = CatalogTestUtils.createValidBookRequestDTO();
                String bookId = given().contentType(ContentType.JSON).body(bookRequest).when()
                                .post("/api/v1/books").then().statusCode(201).extract().path("bookId");

                given().pathParam("bookId", bookId).when().get("/api/v1/books/{bookId}").then()
                                .statusCode(200).body("isbn", equalTo(bookRequest.getIsbn()))
                                .body("title", equalTo(bookRequest.getTitle()));
        }

        @Test
        @TestSecurity(user = "user", roles = "user")
        void testGetBookByIdNonExistingIdShouldReturnNotFound() {
                UUID nonExistingId = UUID.randomUUID();
                given().pathParam("bookId", nonExistingId).when().get("/api/v1/books/{bookId}").then()
                                .statusCode(404);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = "admin")
        void testDeleteBookByIdExistingIdShouldReturnNoContentAndBookShouldBeGone() {
                BookRequestDTO bookRequest = CatalogTestUtils.createValidBookRequestDTO();
                String bookId = given().contentType(ContentType.JSON).body(bookRequest).when()
                                .post("/api/v1/books").then().statusCode(201).extract().path("bookId");

                given().pathParam("bookId", bookId).when().delete("/api/v1/books/{bookId}").then()
                                .statusCode(204);

                given().pathParam("bookId", bookId).when().get("/api/v1/books/{bookId}").then()
                                .statusCode(404);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = "admin")
        void testDeleteBookByIdNonExistingIdShouldReturnNotFound() {
                UUID nonExistingId = UUID.randomUUID();
                given().pathParam("bookId", nonExistingId).when().delete("/api/v1/books/{bookId}").then()
                                .statusCode(404);
        }

        @Test
        void testDeleteBookByIdExistingIdWithoutRoleShouldReturnUnauthorized() {
                given().pathParam("bookId", "fakeit").when().delete("/api/v1/books/{bookId}")
                                .then().statusCode(401);
        }

        @Test
        @TestSecurity(user = "adminUser", roles = "admin")
        void testGetAllBooksBooksExistShouldReturnOkAndListOfBooks() {
                BookRequestDTO bookRequest1 = CatalogTestUtils.createValidBookRequestDTO();
                bookRequest1.setIsbn("111-111");
                bookRequest1.setTitle("Book 1");
                BookRequestDTO bookRequest2 = CatalogTestUtils.createValidBookRequestDTO();
                bookRequest2.setIsbn("222-222");
                bookRequest2.setTitle("Book 2");

                given().contentType(ContentType.JSON).body(bookRequest1).when().post("/api/v1/books").then()
                                .statusCode(201);

                given().contentType(ContentType.JSON).body(bookRequest2).when().post("/api/v1/books").then()
                                .statusCode(201);

                given().when().get("/api/v1/books").then().statusCode(200).body("size()",
                                org.hamcrest.Matchers.greaterThanOrEqualTo(2));
        }

        @Test
        @TestSecurity(user = "adminUser", roles = "admin")
        void testSearchBooksSuccessful() {

                BookRequestDTO book1 = CatalogTestUtils.createValidBookRequestDTO();
                book1.setTitle("The Great Gatsby");
                book1.setDescription("A novel about the roaring twenties.");
                book1.setIsbn("1111111111111");

                BookRequestDTO book2 = CatalogTestUtils.createValidBookRequestDTO();
                book2.setTitle("1984");
                book2.setDescription("A dystopian novel by George Orwell.");
                book2.setIsbn("2222222222222");

                BookRequestDTO book3 = CatalogTestUtils.createValidBookRequestDTO();
                book3.setTitle("Brave New World");
                book3.setDescription("Another dystopian classic.");
                book3.setIsbn("3333333333333");

                given().contentType(ContentType.JSON).body(book1).when().post("/api/v1/books").then().statusCode(201);
                given().contentType(ContentType.JSON).body(book2).when().post("/api/v1/books").then().statusCode(201);
                given().contentType(ContentType.JSON).body(book3).when().post("/api/v1/books").then().statusCode(201);

                given().queryParam("query", "great")
                                .queryParam("page", 0)
                                .queryParam("size", 10)
                                .when().get("/api/v1/books/search")
                                .then()
                                .statusCode(200)
                                .body("content", hasSize(1))
                                .body("content[0].title", equalTo("The Great Gatsby"))
                                .body("totalElements", equalTo(1))
                                .body("totalPages", equalTo(1));

                given().queryParam("query", "dystopian")
                                .queryParam("page", 0)
                                .queryParam("size", 10)
                                .when().get("/api/v1/books/search")
                                .then()
                                .statusCode(200)
                                .body("content", hasSize(2))
                                .body("totalElements", equalTo(2))
                                .body("totalPages", equalTo(1))
                                .body("content.title", hasItems("1984", "Brave New World"));

                given().queryParam("query", "nonexistent")
                                .queryParam("page", 0)
                                .queryParam("size", 10)
                                .when().get("/api/v1/books/search")
                                .then()
                                .statusCode(200)
                                .body("content", hasSize(0))
                                .body("totalElements", equalTo(0));
        }

        @Test
        @TestSecurity(user = "user", roles = "user")
        void testSearchBooksWithoutQueryShouldReturnBadRequest() {
                given().queryParam("page", 0)
                                .queryParam("size", 10)
                                .when().get("/api/v1/books/search")
                                .then()
                                .statusCode(400);
        }

        @Test
        void testSearchBooksWithoutRoleShouldReturnUnauthorized() {
                given().queryParam("query", "test")
                                .when().get("/api/v1/books/search")
                                .then()
                                .statusCode(401);
        }
}
