package modules.readinglist.core.usecases;

import modules.catalog.core.domain.Book;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.web.dto.ReadingListRequestDTO;
import org.eclipse.microprofile.jwt.JsonWebToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadingListService {
    ReadingList createReadingList(ReadingList readingList);
    
    ReadingList createReadingList(ReadingListRequestDTO request, JsonWebToken principal);

    Optional<ReadingList> findReadingListById(UUID readingListId, JsonWebToken principal);

    List<ReadingList> getReadingListsForUser(UUID userId);

    ReadingList updateReadingList(UUID readingListId, ReadingListRequestDTO request, JsonWebToken principal);

    void deleteReadingListById(UUID readingListId, JsonWebToken principal);

    void addBookToReadingList(UUID readingListId, UUID bookId, JsonWebToken principal);

    void removeBookFromReadingList(UUID readingListId, UUID bookId, JsonWebToken principal);

    List<Book> getBooksInReadingList(UUID readingListId, JsonWebToken principal);

    Optional<ReadingList> findReadingListForBookAndUser(UUID userId, UUID bookId);

    void moveBookBetweenReadingLists(UUID userId, UUID bookId, UUID sourceListId, UUID targetListId,
            JsonWebToken principal);
}