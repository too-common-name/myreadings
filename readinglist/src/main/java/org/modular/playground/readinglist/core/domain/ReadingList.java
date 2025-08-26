package org.modular.playground.readinglist.core.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.user.core.domain.User;

public interface ReadingList {
    UUID getReadingListId();
    User getUser();
    String getName();
    String getDescription();
    LocalDateTime getCreationDate();
    List<Book> getBooks();
}