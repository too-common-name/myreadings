package modules.readinglist.core.usecases;

import modules.catalog.core.domain.Book;
import modules.catalog.core.usecases.BookService;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.domain.ReadingListImpl;
import modules.readinglist.core.usecases.repositories.ReadingListRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReadingListServiceImpl implements ReadingListService {

    private final ReadingListRepository readingListRepository;
    private final BookService bookService;

    @Inject
    public ReadingListServiceImpl(ReadingListRepository readingListRepository, BookService bookService) {
        this.readingListRepository = readingListRepository;
        this.bookService = bookService;
    }

    @Override
    public Optional<ReadingList> findReadingListById(UUID readingListId) {
        return readingListRepository.findById(readingListId)
                .map(this::enrichReadingListWithBookDetails);
    }

    @Override
    public List<ReadingList> getReadingListsForUser(UUID userId) {
        return readingListRepository.findByUserId(userId).stream()
                .map(this::enrichReadingListWithBookDetails)
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> getBooksInReadingList(UUID readingListId) {
        ReadingList partialList = readingListRepository.findById(readingListId)
                .orElseThrow(() -> new IllegalArgumentException("ReadingList not found: " + readingListId));

        if (partialList.getBooks() == null || partialList.getBooks().isEmpty()) {
            return Collections.emptyList();
        }

        return partialList.getBooks().stream()
                .map(Book::getBookId)
                .map(bookService::getBookById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private ReadingList enrichReadingListWithBookDetails(ReadingList partialList) {
        if (partialList.getBooks() == null || partialList.getBooks().isEmpty()) {
            return partialList;
        }

        List<Book> enrichedBooks = partialList.getBooks().stream()
            .map(bookStub -> bookService.getBookById(bookStub.getBookId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        return ReadingListImpl.builder()
                .readingListId(partialList.getReadingListId())
                .userId(partialList.getUserId())
                .name(partialList.getName())
                .description(partialList.getDescription())
                .creationDate(partialList.getCreationDate())
                .books(enrichedBooks)
                .build();
    }

    @Override
    @Transactional
    public ReadingList createReadingList(ReadingList readingList) {
        return readingListRepository.create(readingList);
    }

    @Override
    @Transactional
    public ReadingList updateReadingList(ReadingList readingList) {
        readingListRepository.findById(readingList.getReadingListId())
                .orElseThrow(() -> new IllegalArgumentException("ReadingList not found for update: " + readingList.getReadingListId()));
        return readingListRepository.update(readingList);
    }

    @Override
    @Transactional
    public void deleteReadingListById(UUID readingListId) {
        if (readingListRepository.findById(readingListId).isEmpty()) {
            throw new IllegalArgumentException("ReadingList not found for deletion: " + readingListId);
        }
        readingListRepository.deleteById(readingListId);
    }

    @Override
    public void addBookToReadingList(UUID readingListId, UUID bookId) {
        ReadingList targetList = readingListRepository.findById(readingListId)
                .orElseThrow(() -> new IllegalArgumentException("ReadingList not found: " + readingListId));

        Book book = bookService.getBookById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found in catalog: " + bookId));

        Optional<ReadingList> currentListForBookOptional = findReadingListForBookAndUser(targetList.getUserId(), bookId);
        if (currentListForBookOptional.isPresent()) {
            throw new IllegalArgumentException(
                "Book is already in reading list: '" + currentListForBookOptional.get().getName() + "'. Use moveBookBetweenReadingLists to change its list."
            );
        } else {
            persistBookInList(readingListId, book);
        }
    }

    @Transactional
    protected void persistBookInList(UUID readingListId, Book book) {
        readingListRepository.addBookToReadingList(readingListId, book);
    }

    @Override
    @Transactional
    public void removeBookFromReadingList(UUID readingListId, UUID bookId) {
        readingListRepository.findById(readingListId)
                .orElseThrow(() -> new IllegalArgumentException("ReadingList not found: " + readingListId));
        readingListRepository.removeBookFromReadingList(readingListId, bookId);
    }

    @Override
    public Optional<ReadingList> findReadingListForBookAndUser(UUID userId, UUID bookId) {
        return readingListRepository.findReadingListContainingBookForUser(userId, bookId);
    }


    @Transactional
    protected void moveBookBetweenReadingLists(Book book, UUID sourceListId, UUID targetListId) {
        readingListRepository.removeBookFromReadingList(sourceListId, book.getBookId());
        readingListRepository.addBookToReadingList(targetListId, book);
    }

    @Override
    public void moveBookBetweenReadingLists(UUID userId, UUID bookId, UUID sourceListId, UUID targetListId) {
        ReadingList sourceList = readingListRepository.findById(sourceListId)
            .orElseThrow(() -> new IllegalArgumentException("Source ReadingList not found: " + sourceListId));
        
        ReadingList targetList = readingListRepository.findById(targetListId)
            .orElseThrow(() -> new IllegalArgumentException("Target ReadingList not found: " + targetListId));

        if (!sourceList.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Source list does not belong to the current user.");
        }
        if (!targetList.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Target list does not belong to the current user.");
        }

        if (!getBooksInReadingList(sourceListId).stream().anyMatch(b -> b.getBookId().equals(bookId))) {
            throw new IllegalArgumentException("Book is not in the source reading list: " + sourceList.getName());
        }
        if (sourceListId.equals(targetListId)) {
            throw new IllegalArgumentException("Cannot move book to the same reading list.");
        }
        
        Book bookToMove = bookService.getBookById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found in catalog for move operation: " + bookId));
        
        moveBookBetweenReadingLists(bookToMove, sourceListId, targetListId);
    }
}