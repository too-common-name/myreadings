package modules.catalog.utils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;
import modules.catalog.domain.Book;
import modules.catalog.domain.BookImpl;
import modules.catalog.web.dto.BookRequestDTO;
import modules.catalog.web.dto.BookResponseDTO;

public class CatalogTestUtils {
    public static Book createValidBook() {
        return createBookWithText("Generic test book");
    }

    public static Book createValidBookWithId(UUID bookId) {
        return new BookImpl.BookBuilder().bookId(bookId).isbn("978-0321765723")
                .title("Test Book Title").authors(Arrays.asList("Test Author"))
                .publicationDate(LocalDate.now().minusYears(5)).publisher("Test Publisher")
                .description("Test book description").pageCount(300).coverImageId("coverTest123")
                .originalLanguage("en").build();
    }


    public static Book createBookWithText(String reviewText) {
        return new BookImpl.BookBuilder().bookId(UUID.randomUUID()).isbn("978-0321765723")
                .title("Test Book Title").authors(Arrays.asList("Test Author"))
                .publicationDate(LocalDate.now().minusYears(5)).publisher("Test Publisher")
                .description("Test book description").pageCount(300).coverImageId("coverTest123")
                .originalLanguage("en").build();
    }

    public static BookRequestDTO createValidBookRequestDTO() {
        return BookRequestDTO.builder()
                .isbn("1234567890")
                .title("Test Title")
                .authors(Arrays.asList("Test author"))
                .publicationDate(LocalDate.of(2023, 1, 1))
                .publisher("Test Publisher")
                .description("Test Description")
                .pageCount(200)
                .coverImageId("CoverId")
                .originalLanguage("EN")
                .build();
    }

    public static BookImpl.BookBuilder createValidBookBuilder(BookRequestDTO requestDTO) {
        return new BookImpl.BookBuilder()
                .bookId(UUID.randomUUID())
                .isbn(requestDTO.getIsbn())
                .title(requestDTO.getTitle())
                .authors(requestDTO.getAuthors())
                .publicationDate(requestDTO.getPublicationDate())
                .publisher(requestDTO.getPublisher())
                .description(requestDTO.getDescription())
                .pageCount(requestDTO.getPageCount())
                .coverImageId(requestDTO.getCoverImageId())
                .originalLanguage(requestDTO.getOriginalLanguage());
    }

    public static BookResponseDTO mapBookToResponseDto(Book book) {
        return BookResponseDTO.builder()
                .bookId(book.getBookId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .authors(book.getAuthors())
                .publicationDate(book.getPublicationDate())
                .publisher(book.getPublisher())
                .description(book.getDescription())
                .pageCount(book.getPageCount())
                .coverImageId(book.getCoverImageId())
                .originalLanguage(book.getOriginalLanguage())
                .build();
    }
}
