package modules.readinglist.core.usecases;

import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.domain.ReadingListImpl;
import modules.readinglist.core.usecases.repositories.ReadingListRepository;
import modules.catalog.core.domain.Book;
import modules.catalog.core.usecases.BookService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

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
        Optional<ReadingList> partialListOpt = readingListRepository.findById(readingListId);

        return partialListOpt.map(this::buildEnrichedListByLooping);
    }

    @Override
    public List<ReadingList> getReadingListsForUser(UUID userId) {
        List<ReadingList> partialLists = readingListRepository.findByUserId(userId);

        return partialLists.stream()
                .map(this::buildEnrichedListByLooping)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Book> getBooksInReadingList(UUID readingListId) {
        ReadingList partialList = readingListRepository.findById(readingListId)
                .orElseThrow(() -> new IllegalArgumentException("ReadingList not found: " + readingListId));

        if (partialList.getBooks().isEmpty()) {
            return Collections.emptyList();
        }
        
        return partialList.getBooks().stream()
                .map(Book::getBookId)
                .map(bookService::getBookById) 
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private ReadingList buildEnrichedListByLooping(ReadingList partialList) {
        if (partialList.getBooks() == null || partialList.getBooks().isEmpty()) {
            return partialList; 
        }

        List<Book> enrichedBooks = partialList.getBooks().stream()
                .map(partialBook -> bookService.getBookById(partialBook.getBookId()))
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
        return readingListRepository.save(readingList);
    }
    
    @Override
    @Transactional
    public ReadingList updateReadingList(ReadingList readingList) {
        readingListRepository.findById(readingList.getReadingListId())
                .orElseThrow(() -> new IllegalArgumentException("ReadingList not found for update: " + readingList.getReadingListId()));
        return readingListRepository.save(readingList);
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
    @Transactional
    public void addBookToReadingList(UUID readingListId, UUID bookId) {
        readingListRepository.findById(readingListId)
                .orElseThrow(() -> new IllegalArgumentException("ReadingList not found: " + readingListId));
        Book book = bookService.getBookById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found in catalog: " + bookId));
        readingListRepository.addBookToReadingList(readingListId, book);
    }
    
    @Override
    @Transactional
    public void removeBookFromReadingList(UUID readingListId, UUID bookId) {
        readingListRepository.findById(readingListId)
                .orElseThrow(() -> new IllegalArgumentException("ReadingList not found: " + readingListId));
        readingListRepository.removeBookFromReadingList(readingListId, bookId);
    }
}
