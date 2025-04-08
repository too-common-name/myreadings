package modules.catalog.web.controllers;

import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import modules.catalog.utils.CatalogTestUtils;
import modules.catalog.web.dto.BookRequestDTO;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class BookControllerIntegrationTest {

        @Inject
        @PersistenceUnit("books-db")
        EntityManager entityManager;

        @AfterEach
        @Transactional
        void setUp() {
                entityManager.createQuery("DELETE FROM BookEntity").executeUpdate();
        }

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
}
