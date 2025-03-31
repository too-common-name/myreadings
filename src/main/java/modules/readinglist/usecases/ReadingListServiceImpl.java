package modules.readinglist.usecases;

import modules.readinglist.domain.ReadingList;
import modules.readinglist.infrastructure.ReadingListRepository;
import modules.catalog.domain.Book;
import modules.catalog.usecases.BookService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReadingListServiceImpl implements ReadingListService {

    private final ReadingListRepository readingListRepository;
    private final BookService bookService;

    public ReadingListServiceImpl(ReadingListRepository readingListRepository, BookService bookService) {
        this.readingListRepository = readingListRepository;
        this.bookService = bookService;
    }

    @Override
    public ReadingList createReadingList(ReadingList readingList) {
        return readingListRepository.save(readingList);
    }

    @Override
    public Optional<ReadingList> findReadingListById(UUID readingListId) {
        return readingListRepository.findById(readingListId);
    }

    @Override
    public List<ReadingList> getReadingListsForUser(UUID userId) {
        return readingListRepository.findByUserId(userId);
    }

    @Override
    public ReadingList updateReadingList(ReadingList readingList) {
        if (readingListRepository.findById(readingList.getReadingListId()).isEmpty()) {
            throw new IllegalArgumentException("ReadingList not found for update: " + readingList.getReadingListId());
        }
        return readingListRepository.save(readingList);
    }

    @Override
    public void deleteReadingListById(UUID readingListId) {
        if (readingListRepository.findById(readingListId).isEmpty()) {
            throw new IllegalArgumentException("ReadingList not found for deletion: " + readingListId);
        }
        readingListRepository.deleteById(readingListId);
    }

    @Override
    public void addBookToReadingList(UUID readingListId, UUID bookId) {
        if (readingListRepository.findById(readingListId).isEmpty()) {
            throw new IllegalArgumentException("ReadingList not found: " + readingListId + ". Cannot add book.");
        }
        Optional<Book> bookOptional = bookService.getBookById(bookId);
        if (bookOptional.isEmpty()) {
            throw new IllegalArgumentException("Book not found in catalog: " + bookId + ". Cannot add to reading list.");
        }
        readingListRepository.addBookToReadingList(readingListId, bookOptional.get());
    }

    @Override
    public void removeBookFromReadingList(UUID readingListId, UUID bookId) {
        if (readingListRepository.findById(readingListId).isEmpty()) {
            throw new IllegalArgumentException("ReadingList not found: " + readingListId + ". Cannot remove book.");
        }
        readingListRepository.removeBookFromReadingList(readingListId, bookId);
    }

    @Override
    public List<Book> getBooksInReadingList(UUID readingListId) {
        return readingListRepository.getBooksInReadingList(readingListId);
    }
}