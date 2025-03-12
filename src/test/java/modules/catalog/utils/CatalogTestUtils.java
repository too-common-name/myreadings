package modules.catalog.utils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;
import modules.catalog.domain.Book;
import modules.catalog.domain.BookImpl;

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
}
