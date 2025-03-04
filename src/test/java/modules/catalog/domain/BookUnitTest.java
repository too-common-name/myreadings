package modules.catalog.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*; 

public class BookUnitTest {

    @Test
    void createBookSuccessful() {
        UUID bookId = UUID.randomUUID();
        String isbn = "978-0321765723";
        String title = "Refactoring: Improving the Design of Existing Code";
        List<String> authors = Arrays.asList("Martin Fowler", "Kent Beck", "John Brant");
        LocalDate publicationDate = LocalDate.of(1999, 6, 1);
        String publisher = "Addison-Wesley";
        String description = "Widely known as the definitive book on refactoring,...";
        int pageCount = 448;
        String coverImageId = "cover-refactoring-1234.jpg";
        String originalLanguage = "en";

        Book book = new BookImpl.BookBuilder()
                .bookId(bookId)
                .isbn(isbn)
                .title(title)
                .authors(authors)
                .publicationDate(publicationDate)
                .publisher(publisher)
                .description(description)
                .pageCount(pageCount)
                .coverImageId(coverImageId)
                .originalLanguage(originalLanguage)
                .build();

        assertNotNull(book);
        assertEquals(bookId, book.getBookId());
        assertEquals(isbn, book.getIsbn());
        assertEquals(title, book.getTitle());
        assertEquals(authors, book.getAuthors());
        assertEquals(publicationDate, book.getPublicationDate());
        assertEquals(publisher, book.getPublisher());
        assertEquals(description, book.getDescription());
        assertEquals(pageCount, book.getPageCount());
        assertEquals(coverImageId, book.getCoverImageId());
        assertEquals(originalLanguage, book.getOriginalLanguage());
    }

}