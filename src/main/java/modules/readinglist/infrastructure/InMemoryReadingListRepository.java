package modules.readinglist.infrastructure;

import modules.catalog.core.domain.Book;
import modules.readinglist.domain.ReadingList;

import java.util.*;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InMemoryReadingListRepository implements ReadingListRepository {

    private final Map<UUID, ReadingList> readingLists = new HashMap<>();

    @Override
    public ReadingList save(ReadingList readingList) {
        readingLists.put(readingList.getReadingListId(), readingList);
        return readingList;
    }

    @Override
    public Optional<ReadingList> findById(UUID readingListId) {
        return Optional.ofNullable(readingLists.get(readingListId));
    }


    @Override
    public List<ReadingList> findByUserId(UUID userId) {
        return readingLists.values().stream()
                .filter(readingList -> readingList.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(UUID readingListId) {
        readingLists.remove(readingListId);
    }

    @Override
    public void addBookToReadingList(UUID readingListId, Book book) {
        ReadingList readingList = readingLists.get(readingListId);
        if (readingList != null) {
            readingList.getBooks().add(book);
        }
    }

    @Override
    public void removeBookFromReadingList(UUID readingListId, UUID bookId) {
        ReadingList readingListToRemoveFrom = readingLists.get(readingListId);
        if (readingListToRemoveFrom != null) {
            readingListToRemoveFrom.getBooks().removeIf(book -> book.getBookId().equals(bookId));
        }
    }

    @Override
    public List<Book> getBooksInReadingList(UUID readingListId) {
        ReadingList readingList = readingLists.get(readingListId);
        if (readingList != null) {
            return readingList.getBooks();
        }
        return new ArrayList<>();
    }
}