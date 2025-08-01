package modules.catalog.utils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import modules.catalog.web.dto.BookRequestDTO;
import modules.catalog.web.dto.BookResponseDTO;

public class CatalogTestUtils {
    
    private static final Random random = new Random();

    public static String generateSimpleRandomISBN13() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static Book createValidBook() {
        return createTestBook("Generic test book", "Generic test book");
    }

    public static Book createValidBookWithId(UUID bookId) {
        return BookImpl.builder().bookId(bookId).isbn(generateSimpleRandomISBN13())
                .title("Test Book Title").authors(Arrays.asList("Test Author"))
                .publicationDate(LocalDate.now().minusYears(5)).publisher("Test Publisher")
                .description("Test book description").pageCount(300).coverImageId("coverTest123")
                .originalLanguage("en").build();
    }


    public static Book createTestBook(String title, String description) {
        return BookImpl.builder().bookId(UUID.randomUUID()).isbn(generateSimpleRandomISBN13())
                .title(title).authors(Arrays.asList("Test Author"))
                .publicationDate(LocalDate.now().minusYears(5)).publisher("Test Publisher")
                .description(description).pageCount(300).coverImageId("coverTest123")
                .originalLanguage("en").build();
    }

    public static Book createTestBookWithDate(String title, String description, LocalDate publicationDate) {
        return BookImpl.builder().bookId(UUID.randomUUID()).isbn(generateSimpleRandomISBN13())
                .title(title).authors(Arrays.asList("Test Author"))
                .publicationDate(publicationDate).publisher("Test Publisher")
                .description(description).pageCount(300).coverImageId("coverTest123")
                .originalLanguage("en").genre("Fiction") // Aggiunto genere di default
                .build();
    }

    public static BookRequestDTO createValidBookRequestDTO() {
        return BookRequestDTO.builder()
                .isbn(generateSimpleRandomISBN13())
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

    public static BookImpl.BookImplBuilder createValidBookBuilder(BookRequestDTO requestDTO) {
        return BookImpl.builder()
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
