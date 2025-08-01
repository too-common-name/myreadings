package modules.readinglist.core.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import modules.catalog.core.domain.Book;


public interface ReadingList {

    UUID getReadingListId();

    UUID getUserId();

    String getName();

    String getDescription();

    LocalDateTime getCreationDate();

    List<Book> getBooks();

}
