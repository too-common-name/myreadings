package modules.catalog.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import modules.catalog.web.dto.BookRequestDTO;

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
                .title("Test Book Title").authors(new ArrayList<>(Arrays.asList("Test Author")))
                .publicationDate(LocalDate.now().minusYears(5)).publisher("Test Publisher")
                .description("Test book description").pageCount(300).coverImageId("coverTest123")
                .originalLanguage("en").build();
    }

    public static Book createTestBook(String title, String description) {
        return BookImpl.builder().bookId(UUID.randomUUID()).isbn(generateSimpleRandomISBN13())
                .title(title).authors(new ArrayList<>(Arrays.asList("Test Author")))
                .publicationDate(LocalDate.now().minusYears(5)).publisher("Test Publisher")
                .description(description).pageCount(300).coverImageId("coverTest123")
                .originalLanguage("en").build();
    }

    public static Book createTestBookWithDate(String title, String description, LocalDate publicationDate) {
        return BookImpl.builder().bookId(UUID.randomUUID()).isbn(generateSimpleRandomISBN13())
                .title(title).authors(new ArrayList<>(Arrays.asList("Test Author")))
                .publicationDate(publicationDate).publisher("Test Publisher")
                .description(description).pageCount(300).coverImageId("coverTest123")
                .originalLanguage("en").genre("Fiction")
                .build();
    }

    public static BookRequestDTO createValidBookRequestDTO() {
        return BookRequestDTO.builder()
                .isbn(generateSimpleRandomISBN13())
                .title("Test Title")
                .authors(new ArrayList<>(Arrays.asList("Test author")))
                .publicationDate(LocalDate.of(2023, 1, 1))
                .publisher("Test Publisher")
                .description("Test Description")
                .pageCount(200)
                .coverImageId("CoverId")
                .originalLanguage("EN")
                .build();
    }

    public static BookImpl.BookImplBuilder createValidBookBuilder() {
        return BookImpl.builder()
                .bookId(UUID.randomUUID())
                .isbn("978-0321765723")
                .title("Refactoring")
                .authors(Arrays.asList("Martin Fowler", "Kent Beck"))
                .publicationDate(LocalDate.now().minusYears(10))
                .publisher("Addison-Wesley")
                .description("Valid description")
                .pageCount(400)
                .coverImageId("cover123")
                .originalLanguage("en");
    }

    public static Book fromRequestDTO(BookRequestDTO dto) {
        return BookImpl.builder()
                .bookId(UUID.randomUUID())
                .isbn(dto.getIsbn())
                .title(dto.getTitle())
                .authors(dto.getAuthors() != null ? new ArrayList<>(dto.getAuthors()) : new ArrayList<>())
                .publicationDate(dto.getPublicationDate())
                .publisher(dto.getPublisher())
                .description(dto.getDescription())
                .pageCount(dto.getPageCount() != null ? dto.getPageCount() : 0)
                .coverImageId(dto.getCoverImageId())
                .originalLanguage(dto.getOriginalLanguage())
                .genre(dto.getGenre())
                .build();
    }
}
