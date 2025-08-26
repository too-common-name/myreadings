package org.modular.playground.readinglist.core.usecases;

import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.readinglist.core.domain.ReadingList;
import org.modular.playground.readinglist.web.dto.ReadingListRequestDTO;
import org.eclipse.microprofile.jwt.JsonWebToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadingListService {
    ReadingList createReadingListInternal(ReadingList readingList);
    
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