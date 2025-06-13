package modules.readinglist.infrastructure.persistence.in_memory;

import modules.catalog.core.domain.Book;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.domain.ReadingListImpl;
import modules.readinglist.core.usecases.repositories.ReadingListRepository;

import java.util.*;
import java.util.stream.Collectors;

import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@IfBuildProperty(name = "app.repository.type", stringValue = "in-memory", enableIfMissing = true)
public class InMemoryReadingListRepository implements ReadingListRepository {

    private Map<UUID, ReadingList> readingLists = new HashMap<>();

    @Override
    public ReadingList create(ReadingList readingList) {
        readingLists.put(readingList.getReadingListId(), readingList);
        return readingList;
    }

    @Override
    public ReadingList update(ReadingList list) {
        ReadingList existingList = readingLists.get(list.getReadingListId());

        ReadingList updatedList = ReadingListImpl.builder()
                .readingListId(existingList.getReadingListId())
                .userId(existingList.getUserId())
                .name(list.getName())
                .description(list.getDescription())
                .creationDate(existingList.getCreationDate())
                .books(existingList.getBooks())
                .build();

        readingLists.put(updatedList.getReadingListId(), updatedList);
        return updatedList;
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