package modules.readinglist.infrastructure;

import modules.catalog.core.domain.Book;
import modules.readinglist.domain.ReadingList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadingListRepository {
    ReadingList save(ReadingList readingList);
    Optional<ReadingList> findById(UUID readingListId);
    List<ReadingList> findByUserId(UUID userId);
    void deleteById(UUID readingListId);

    void addBookToReadingList(UUID readingListId, Book book);
    void removeBookFromReadingList(UUID readingListId, UUID bookId);
    List<Book> getBooksInReadingList(UUID readingListId);
}