package modules.readinglist.usecases;
import modules.catalog.core.domain.Book;
import modules.readinglist.domain.ReadingList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadingListService {

    ReadingList createReadingList(ReadingList readingList);
    Optional<ReadingList> findReadingListById(UUID readingListId);
    List<ReadingList> getReadingListsForUser(UUID userId);
    ReadingList updateReadingList(ReadingList readingList);
    void deleteReadingListById(UUID readingListId);

    void addBookToReadingList(UUID readingListId, UUID bookId);
    void removeBookFromReadingList(UUID readingListId, UUID bookId);

    List<Book> getBooksInReadingList(UUID readingListId);
}