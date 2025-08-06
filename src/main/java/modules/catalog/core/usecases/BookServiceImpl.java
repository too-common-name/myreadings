package modules.catalog.core.usecases;

import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.BookImpl;
import modules.catalog.core.domain.DomainPage;
import modules.catalog.core.usecases.repositories.BookRepository;
import modules.catalog.web.dto.BookRequestDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jboss.logging.Logger;

@ApplicationScoped
public class BookServiceImpl implements BookService {

    private static final Logger LOGGER = Logger.getLogger(BookServiceImpl.class);

    @Inject
    BookRepository bookRepository;

    @Override
    @Transactional
    public Book createBook(BookRequestDTO createBookRequestDTO) {
        LOGGER.info("Starting creation process for a new book.");

        Book book = BookImpl.builder()
                .bookId(UUID.randomUUID())
                .isbn(createBookRequestDTO.getIsbn())
                .title(createBookRequestDTO.getTitle())
                .authors(createBookRequestDTO.getAuthors())
                .publicationDate(createBookRequestDTO.getPublicationDate())
                .publisher(createBookRequestDTO.getPublisher())
                .description(createBookRequestDTO.getDescription())
                .pageCount(createBookRequestDTO.getPageCount())
                .coverImageId(createBookRequestDTO.getCoverImageId())
                .originalLanguage(createBookRequestDTO.getOriginalLanguage())
                .build();

        LOGGER.debugf("Domain object 'Book' created with ID %s, ready for persistence", book.getBookId());

        Book savedBook = bookRepository.save(book);
        LOGGER.infof("Book saved to repository with ID: %s", savedBook.getBookId());

        return savedBook;
    }

    @Override
    public Optional<Book> getBookById(UUID bookId) {
        LOGGER.debugf("Searching for book by ID: %s", bookId);
        return bookRepository.findById(bookId);
    }

    @Override
    public List<Book> getAllBooks(String sort, String order, Integer limit) {
        LOGGER.debugf("Passing 'getAllBooks' request to repository with params [sort: %s, order: %s, limit: %s]",
                sort, order, limit);
        return bookRepository.findAll(sort, order, limit);
    }

    @Override
    @Transactional
    public Book updateBook(Book book) {
        LOGGER.infof("Starting update process for book with ID: %s", book.getBookId());
        Book updatedBook = bookRepository.save(book);
        LOGGER.infof("Book with ID: %s updated successfully.", updatedBook.getBookId());
        return updatedBook;
    }

    @Override
    @Transactional
    public void deleteBookById(UUID bookId) {
        LOGGER.infof("Deleting book with ID: %s", bookId);
        bookRepository.deleteById(bookId);
    }

    @Override
    public DomainPage<Book> searchBooks(String query, int page, int size, String sortBy, String sortOrder) {
        LOGGER.debugf("Passing search to repository with query: '%s'", query);
        return bookRepository.searchBooks(query, page, size, sortBy, sortOrder);
    }
}