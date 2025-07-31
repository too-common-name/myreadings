package modules.catalog.core.usecases;

import modules.catalog.core.domain.Book;
import modules.catalog.core.domain.DomainPage;
import modules.catalog.core.usecases.repositories.BookRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BookServiceImpl implements BookService {

    @Inject
    BookRepository bookRepository;

    @Override
    @Transactional
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public Optional<Book> getBookById(UUID bookId) {
        return bookRepository.findById(bookId);
    }

    @Override
    public List<Book> getAllBooks(String sort, String order, Integer limit) {
        return bookRepository.findAll(sort, order, limit);
    }

    @Override
    @Transactional
    public Book updateBook(Book book) {
        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteBookById(UUID bookId) {
        bookRepository.deleteById(bookId);
    }

    @Override
    public DomainPage<Book> searchBooks(String query, int page, int size, String sortBy, String sortOrder) {
        return bookRepository.searchBooks(query, page, size, sortBy, sortOrder);
    }
}