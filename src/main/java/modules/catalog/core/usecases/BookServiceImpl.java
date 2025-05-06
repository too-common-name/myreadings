package modules.catalog.core.usecases;

import modules.catalog.core.domain.Book;
import modules.catalog.core.usecases.repositories.BookRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@Transactional
public class BookServiceImpl implements BookService {

    @Inject
    BookRepository bookRepository;

    @Override
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
    public Book updateBook(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public void deleteBookById(UUID bookId) {
        bookRepository.deleteById(bookId);
    }
}