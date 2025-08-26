package org.modular.playground.readinglist.infrastructure.persistence.in_memory;

import jakarta.enterprise.context.ApplicationScoped;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.domain.BookImpl;
import org.modular.playground.readinglist.core.domain.ReadingList;
import org.modular.playground.readinglist.core.domain.ReadingListImpl;
import org.modular.playground.readinglist.core.usecases.repositories.ReadingListRepository;

import io.quarkus.arc.properties.IfBuildProperty;

import org.jboss.logging.Logger;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@IfBuildProperty(name = "app.repository.type", stringValue = "in-memory", enableIfMissing = true)
public class InMemoryReadingListRepository implements ReadingListRepository {

    private static final Logger LOGGER = Logger.getLogger(InMemoryReadingListRepository.class);
    private final Map<UUID, ReadingList> readingLists = new HashMap<>();

    @Override
    public ReadingList create(ReadingList list) {
        LOGGER.debugf("In-memory: Creating reading list with ID: %s", list.getReadingListId());
        readingLists.put(list.getReadingListId(), list);
        return list;
    }

    @Override
    public ReadingList update(ReadingList list) {
        LOGGER.debugf("In-memory: Updating reading list with ID: %s", list.getReadingListId());
        if (!readingLists.containsKey(list.getReadingListId())) {
            throw new IllegalArgumentException("ReadingList with ID " + list.getReadingListId() + " not found for update.");
        }
        readingLists.put(list.getReadingListId(), list);
        return list;
    }

    @Override
    public Optional<ReadingList> findById(UUID readingListId) {
        LOGGER.debugf("In-memory: Finding reading list by ID: %s", readingListId);
        return Optional.ofNullable(readingLists.get(readingListId));
    }

    @Override
    public List<ReadingList> findByUserId(UUID userId) {
        LOGGER.debugf("In-memory: Finding reading lists for user ID: %s", userId);
        return readingLists.values().stream()
                .filter(list -> list.getUser().getKeycloakUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID readingListId) {
        LOGGER.debugf("In-memory: Deleting reading list with ID: %s", readingListId);
        readingLists.remove(readingListId);
    }

    @Override
    public void addBookToReadingList(UUID readingListId, UUID bookId) {
        LOGGER.debugf("In-memory: Adding book %s to list %s", bookId, readingListId);
        ReadingList currentList = readingLists.get(readingListId);
        if (currentList == null) {
            throw new IllegalArgumentException("ReadingList with ID " + readingListId + " not found.");
        }
        
        boolean bookExists = currentList.getBooks().stream().anyMatch(b -> b.getBookId().equals(bookId));
        if (bookExists) {
            return; 
        }

        List<Book> newBooks = new ArrayList<>(currentList.getBooks());
        newBooks.add(BookImpl.builder().bookId(bookId).build());

        ReadingList updatedList = ReadingListImpl.builder()
                .readingListId(currentList.getReadingListId())
                .user(currentList.getUser())
                .name(currentList.getName())
                .description(currentList.getDescription())
                .creationDate(currentList.getCreationDate())
                .books(newBooks)
                .build();
        
        readingLists.put(readingListId, updatedList);
    }

    @Override
    public void removeBookFromReadingList(UUID readingListId, UUID bookId) {
        LOGGER.debugf("In-memory: Removing book %s from list %s", bookId, readingListId);
        ReadingList currentList = readingLists.get(readingListId);
        if (currentList == null) {
            throw new IllegalArgumentException("ReadingList with ID " + readingListId + " not found.");
        }

        List<Book> currentBooks = new ArrayList<>(currentList.getBooks());
        boolean removed = currentBooks.removeIf(book -> book.getBookId().equals(bookId));
        if (!removed) {
            throw new IllegalArgumentException("Book with ID " + bookId + " not found in reading list " + readingListId + ".");
        }

        ReadingList updatedList = ReadingListImpl.builder()
                .readingListId(currentList.getReadingListId())
                .user(currentList.getUser())
                .name(currentList.getName())
                .description(currentList.getDescription())
                .creationDate(currentList.getCreationDate())
                .books(currentBooks)
                .build();

        readingLists.put(readingListId, updatedList);
    }

    @Override
    public List<UUID> getBookIdsInReadingList(UUID readingListId) {
        LOGGER.debugf("In-memory: Getting book IDs for list %s", readingListId);
        ReadingList list = readingLists.get(readingListId);
        if (list == null) {
            return Collections.emptyList();
        }
        return list.getBooks().stream().map(Book::getBookId).collect(Collectors.toList());
    }

    @Override
    public Optional<ReadingList> findReadingListContainingBookForUser(UUID userId, UUID bookId) {
        LOGGER.debugf("In-memory: Finding if user %s has book %s in a list", userId, bookId);
        return readingLists.values().stream()
                .filter(list -> list.getUser().getKeycloakUserId().equals(userId))
                .filter(list -> list.getBooks().stream().anyMatch(b -> b.getBookId().equals(bookId)))
                .findFirst();
    }
}