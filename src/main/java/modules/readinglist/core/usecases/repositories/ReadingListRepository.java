package modules.readinglist.core.usecases.repositories;

import modules.catalog.core.domain.Book;
import modules.readinglist.core.domain.ReadingList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadingListRepository {
    ReadingList create(ReadingList list);
    ReadingList update(ReadingList list);
    Optional<ReadingList> findById(UUID readingListId);
    List<ReadingList> findByUserId(UUID userId);
    void deleteById(UUID readingListId);

    void addBookToReadingList(UUID readingListId, Book book);
    void removeBookFromReadingList(UUID readingListId, UUID bookId);
    List<Book> getBooksInReadingList(UUID readingListId);
}