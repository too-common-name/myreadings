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
        
        ReadingListImpl newReadingList = ReadingListImpl.builder()
                .readingListId(readingList.getReadingListId())
                .userId(readingList.getUserId())
                .name(readingList.getName())
                .description(readingList.getDescription())
                .creationDate(readingList.getCreationDate())
                .books(new ArrayList<>(readingList.getBooks())) 
                .build();
        readingLists.put(newReadingList.getReadingListId(), newReadingList);
        return newReadingList;
    }

    @Override
    public ReadingList update(ReadingList list) {
        
        ReadingList existingList = readingLists.get(list.getReadingListId());

        if (existingList == null) {
            throw new IllegalArgumentException("ReadingList with ID " + list.getReadingListId() + " not found for update.");
        }

        
        
        ReadingListImpl updatedList = ReadingListImpl.builder()
                .readingListId(list.getReadingListId()) 
                .userId(list.getUserId()) 
                .name(list.getName())
                .description(list.getDescription())
                .creationDate(list.getCreationDate()) 
                .books(new ArrayList<>(list.getBooks())) 
                .build();

        readingLists.put(updatedList.getReadingListId(), updatedList); 
        return updatedList;
    }

    @Override
    public Optional<ReadingList> findById(UUID readingListId) {
        return Optional.ofNullable(readingLists.get(readingListId))
                .map(list -> (ReadingList) ReadingListImpl.builder()
                        .readingListId(list.getReadingListId())
                        .userId(list.getUserId())
                        .name(list.getName())
                        .description(list.getDescription())
                        .creationDate(list.getCreationDate())
                        .books(new ArrayList<>(list.getBooks()))
                        .build());
    }

    @Override
    public List<ReadingList> findByUserId(UUID userId) {
        return readingLists.values().stream()
                .filter(readingList -> readingList.getUserId().equals(userId))
                .map(list -> (ReadingList) ReadingListImpl.builder()
                        .readingListId(list.getReadingListId())
                        .userId(list.getUserId())
                        .name(list.getName())
                        .description(list.getName()) 
                        .creationDate(list.getCreationDate())
                        .books(new ArrayList<>(list.getBooks()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID readingListId) {
        readingLists.remove(readingListId);
    }

    @Override
    public void addBookToReadingList(UUID readingListId, Book book) {
        ReadingList readingList = readingLists.get(readingListId);
        if (readingList == null) {
            throw new IllegalArgumentException("ReadingList with ID " + readingListId + " not found."); 
        }

        
        boolean bookExists = readingList.getBooks().stream().anyMatch(b -> b.getBookId().equals(book.getBookId()));
        if (bookExists) {
            
            return;
        }

        
        List<Book> updatedBooks = new ArrayList<>(readingList.getBooks());
        updatedBooks.add(book);

        ReadingListImpl updatedReadingList = ReadingListImpl.builder()
                .readingListId(readingList.getReadingListId())
                .userId(readingList.getUserId())
                .name(readingList.getName())
                .description(readingList.getDescription())
                .creationDate(readingList.getCreationDate())
                .books(updatedBooks)
                .build();
        
        readingLists.put(updatedReadingList.getReadingListId(), updatedReadingList); 
    }

    @Override
    public void removeBookFromReadingList(UUID readingListId, UUID bookId) {
        ReadingList readingList = readingLists.get(readingListId);
        if (readingList == null) {
            throw new IllegalArgumentException("ReadingList with ID " + readingListId + " not found."); 
        }

        List<Book> currentBooks = new ArrayList<>(readingList.getBooks()); 
        boolean removed = currentBooks.removeIf(book -> book.getBookId().equals(bookId));
        if (!removed) {
            throw new IllegalArgumentException("Book with ID " + bookId + " not found in reading list " + readingListId + "."); 
        }

        
        ReadingListImpl updatedReadingList = ReadingListImpl.builder()
                .readingListId(readingList.getReadingListId())
                .userId(readingList.getUserId())
                .name(readingList.getName())
                .description(readingList.getDescription())
                .creationDate(readingList.getCreationDate())
                .books(currentBooks)
                .build();
            
        readingLists.put(updatedReadingList.getReadingListId(), updatedReadingList); 
    }

    @Override
    public List<Book> getBooksInReadingList(UUID readingListId) {
        ReadingList readingList = readingLists.get(readingListId);
        if (readingList != null) {
            return new ArrayList<>(readingList.getBooks()); 
        }
        return new ArrayList<>(); 
    }

    
    @Override
    public Optional<ReadingList> findReadingListContainingBookForUser(UUID userId, UUID bookId) {
        return readingLists.values().stream()
                .filter(readingList -> readingList.getUserId().equals(userId))
                .filter(readingList -> readingList.getBooks().stream()
                                                    .anyMatch(bookInList -> bookInList.getBookId().equals(bookId)))
                .map(list -> (ReadingList) ReadingListImpl.builder()
                        .readingListId(list.getReadingListId())
                        .userId(list.getUserId())
                        .name(list.getName())
                        .description(list.getDescription())
                        .creationDate(list.getCreationDate())
                        .books(new ArrayList<>(list.getBooks()))
                        .build())
                .findFirst();
    }
}