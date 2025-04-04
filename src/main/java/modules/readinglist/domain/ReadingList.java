package modules.readinglist.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import modules.catalog.core.domain.Book;
import modules.user.domain.User;


public interface ReadingList {

    UUID getReadingListId();

    User getUser();

    String getName();

    String getDescription();

    LocalDateTime getCreationDate();

    List<Book> getBooks();

}
