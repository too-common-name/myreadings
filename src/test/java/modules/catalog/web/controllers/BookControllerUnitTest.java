package modules.catalog.web.controllers;

import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.DomainPage;
import modules.catalog.core.usecases.BookService;
import modules.catalog.utils.CatalogTestUtils;
import modules.catalog.web.dto.BookRequestDTO;
import modules.catalog.web.dto.BookResponseDTO;
import modules.catalog.web.dto.PagedResponse;
import jakarta.ws.rs.core.Response;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class BookControllerUnitTest {

    @InjectMocks
    private BookController bookController;

    @Mock
    private BookService bookService;

    private Validator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testCreateBookSuccessful() {
        BookRequestDTO bookRequest = CatalogTestUtils.createValidBookRequestDTO();
        Book createdBook = CatalogTestUtils.createValidBookBuilder(bookRequest).build();
        BookResponseDTO expectedResponse = BookResponseDTO.builder()
                .isbn(bookRequest.getIsbn())
                .title(bookRequest.getTitle())
                .authors(bookRequest.getAuthors())
                .publicationDate(bookRequest.getPublicationDate())
                .publisher(bookRequest.getPublisher())
                .description(bookRequest.getDescription())
                .pageCount(bookRequest.getPageCount())
                .coverImageId(bookRequest.getCoverImageId())
                .originalLanguage(bookRequest.getOriginalLanguage())
                .build();

        when(bookService.createBook(any(Book.class))).thenReturn(createdBook);

        Response response = bookController.createBook(bookRequest);
        expectedResponse.setBookId(((BookResponseDTO) response.getEntity()).getBookId());
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals(expectedResponse, response.getEntity());
        verify(bookService, times(1)).createBook(any(Book.class));
    }

    @Test
    void testGetBookByIdShouldReturnOkAndBookDTO() {
        UUID bookId = UUID.randomUUID();
        Book mockBook = CatalogTestUtils.createValidBookBuilder(CatalogTestUtils.createValidBookRequestDTO())
                .bookId(bookId).title("Test Title").isbn("123-456").build();
        BookResponseDTO expectedResponse = CatalogTestUtils.mapBookToResponseDto(mockBook);
        when(bookService.getBookById(bookId)).thenReturn(Optional.of(mockBook));

        Response response = bookController.getBookById(bookId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedResponse, response.getEntity());
        verify(bookService, times(1)).getBookById(bookId);
    }

    @Test
    void testGetBookByIdShouldReturnNotFound() {
        UUID bookId = UUID.randomUUID();
        when(bookService.getBookById(bookId)).thenReturn(Optional.empty());
        Response response = bookController.getBookById(bookId);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
        verify(bookService, times(1)).getBookById(bookId);
    }

    @Test
    void testGetAllBooksShouldReturnOkAndListOfBookDTOs() {
        List<Book> mockBooks = Arrays.asList(
                CatalogTestUtils.createValidBookBuilder(CatalogTestUtils.createValidBookRequestDTO()).title("Book 1")
                        .isbn("111-111").build(),
                CatalogTestUtils.createValidBookBuilder(CatalogTestUtils.createValidBookRequestDTO()).title("Book 2")
                        .isbn("222-222").build());
        List<BookResponseDTO> expectedResponses = mockBooks.stream()
                .map(book -> CatalogTestUtils.mapBookToResponseDto(book)).collect(Collectors.toList());
        when(bookService.getAllBooks(null, null, null)).thenReturn(mockBooks);
        Response response = bookController.getAllBooks(null, null, null);
        @SuppressWarnings("unchecked")
        List<BookResponseDTO> actualResponses = (List<BookResponseDTO>) response.getEntity();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedResponses, actualResponses);
        verify(bookService, times(1)).getAllBooks(null, null, null);
    }

    @Test
    void testBookRequestDTOMissingIsbnShouldHaveViolation() {
        BookRequestDTO bookRequest = CatalogTestUtils.createValidBookRequestDTO();
        bookRequest.setIsbn(null);
        Set<ConstraintViolation<BookRequestDTO>> violations = validator.validate(bookRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(
                v -> v.getPropertyPath().toString().equals("isbn") && v.getMessage().equals("ISBN is required")));
    }

    @Test
    void testBookRequestDTOMissingTitleShouldHaveViolation() {
        BookRequestDTO bookRequest = CatalogTestUtils.createValidBookRequestDTO();
        bookRequest.setTitle(null);
        Set<ConstraintViolation<BookRequestDTO>> violations = validator.validate(bookRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(
                v -> v.getPropertyPath().toString().equals("title") && v.getMessage().equals("Title is required")));
    }

    @Test
    void testBookRequestDTOTitleTooLongShouldHaveViolation() {
        String longTitle = "This is a very long title that exceeds the maximum allowed length of 255 characters. Let's make it even longer just to be absolutely sure that it will indeed exceed the limit and trigger the validation error. We should probably add some more characters here to be safe.";
        BookRequestDTO bookRequest = CatalogTestUtils.createValidBookRequestDTO();
        bookRequest.setTitle(longTitle);
        Set<ConstraintViolation<BookRequestDTO>> violations = validator.validate(bookRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")
                && v.getMessage().equals("Title cannot exceed 255 characters")));
    }

    @Test
    void testBookRequestDTOPublicationDateInFutureShouldHaveViolation() {
        BookRequestDTO bookRequest = CatalogTestUtils.createValidBookRequestDTO();
        bookRequest.setPublicationDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<BookRequestDTO>> violations = validator.validate(bookRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("publicationDate")
                && v.getMessage().equals("Publication date must be in the past or present")));
    }

    @Test
    void testBookRequestDTONegativePageCountShouldHaveViolation() {
        BookRequestDTO bookRequest = CatalogTestUtils.createValidBookRequestDTO();
        bookRequest.setPageCount(-1);
        Set<ConstraintViolation<BookRequestDTO>> violations = validator.validate(bookRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("pageCount")
                && v.getMessage().equals("Page count cannot be negative")));
    }

    @Test
    void testSearchBooksSuccessful() {
        String query = "test";
        int page = 0;
        int size = 10;
        String sortBy = "title";
        String sortOrder = "asc";

        List<Book> mockDomainBooks = Arrays.asList(
                CatalogTestUtils.createTestBook("Test Book A", "Description A"),
                CatalogTestUtils.createTestBook("Another Test Book B", "Description B"));

        DomainPage<Book> mockDomainPage = new DomainPage<>(mockDomainBooks, 2, 1, page, size, true, true);

        when(bookService.searchBooks(query, page, size, sortBy, sortOrder)).thenReturn(mockDomainPage);

        PagedResponse<BookResponseDTO> response = bookController.searchBooks(query, page, size, sortBy, sortOrder);

        verify(bookService, times(1)).searchBooks(query, page, size, sortBy, sortOrder);

        assertNotNull(response);
        assertEquals(2, response.content().size());
        assertEquals(2, response.totalElements());
        assertEquals(1, response.totalPages());
        assertEquals(page, response.page());
        assertEquals(size, response.size());
        assertTrue(response.last());
        assertTrue(response.first());

        assertEquals(mockDomainBooks.get(0).getTitle(), response.content().get(0).getTitle());
        assertEquals(mockDomainBooks.get(1).getTitle(), response.content().get(1).getTitle());
    }
}