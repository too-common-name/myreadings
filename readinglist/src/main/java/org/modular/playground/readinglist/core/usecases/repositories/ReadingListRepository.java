package org.modular.playground.readinglist.core.usecases.repositories;

import org.modular.playground.readinglist.core.domain.ReadingList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadingListRepository {
    ReadingList create(ReadingList list);
    ReadingList update(ReadingList list);
    Optional<ReadingList> findById(UUID readingListId);
    List<ReadingList> findByUserId(UUID userId);
    void deleteById(UUID readingListId);
    void addBookToReadingList(UUID readingListId, UUID bookId);
    void removeBookFromReadingList(UUID readingListId, UUID bookId);
    List<UUID> getBookIdsInReadingList(UUID readingListId);
    Optional<ReadingList> findReadingListContainingBookForUser(UUID userId, UUID bookId);
}